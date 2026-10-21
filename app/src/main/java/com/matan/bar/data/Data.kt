package com.matan.bar.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.withTransaction
import com.matan.bar.logic.Cocktail
import com.matan.bar.logic.Family
import com.matan.bar.logic.Glass
import com.matan.bar.logic.Ice
import com.matan.bar.logic.Measure
import com.matan.bar.logic.Method
import com.matan.bar.logic.Part
import com.matan.bar.logic.Stage
import com.matan.bar.logic.Tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream

// ---------- Room ----------

/** An ingredient you have. [since] = epoch day it was added / made (for homemade syrups). */
@Entity(tableName = "stock")
data class StockItem(@PrimaryKey val key: String, val since: Long)

@Entity(tableName = "meta")
data class CocktailMeta(@PrimaryKey val key: String, val favorite: Boolean = false, val rating: Int = 0, val notes: String = "")

@Entity(tableName = "custom")
data class CustomCocktail(@PrimaryKey(autoGenerate = true) val id: Long = 0, val json: String)

@Dao
interface BarDao {
    @Query("SELECT * FROM stock") fun stock(): Flow<List<StockItem>>
    @Query("SELECT * FROM meta") fun meta(): Flow<List<CocktailMeta>>
    @Query("SELECT * FROM custom ORDER BY id") fun custom(): Flow<List<CustomCocktail>>

    @Query("SELECT * FROM stock") suspend fun allStock(): List<StockItem>
    @Query("SELECT * FROM meta") suspend fun allMeta(): List<CocktailMeta>
    @Query("SELECT * FROM custom") suspend fun allCustom(): List<CustomCocktail>

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun saveStock(s: StockItem)
    @Query("DELETE FROM stock WHERE `key` = :key") suspend fun deleteStock(key: String)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun saveMeta(m: CocktailMeta)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun saveCustom(c: CustomCocktail): Long
    @Query("DELETE FROM custom WHERE id = :id") suspend fun deleteCustom(id: Long)

    @Insert suspend fun insertStock(l: List<StockItem>)
    @Insert suspend fun insertMeta(l: List<CocktailMeta>)
    @Insert suspend fun insertCustom(l: List<CustomCocktail>)
    @Query("DELETE FROM stock") suspend fun clearStock()
    @Query("DELETE FROM meta") suspend fun clearMeta()
    @Query("DELETE FROM custom") suspend fun clearCustom()
}

@Database(entities = [StockItem::class, CocktailMeta::class, CustomCocktail::class], version = 1, exportSchema = false)
abstract class BarDatabase : RoomDatabase() {
    abstract fun dao(): BarDao
}

// ---------- state ----------

data class BarState(
    val stock: Map<String, StockItem> = emptyMap(),
    val meta: Map<String, CocktailMeta> = emptyMap(),
    val custom: List<Cocktail> = emptyList(),
) {
    fun meta(key: String) = meta[key] ?: CocktailMeta(key)
}

class Repo(val db: BarDatabase) {
    val dao = db.dao()

    fun state(): Flow<BarState> = combine(dao.stock(), dao.meta(), dao.custom()) { s, m, c ->
        BarState(
            s.associateBy { it.key },
            m.associateBy { it.key },
            c.mapNotNull { row -> runCatching { CocktailJson.decode(row.json).copy(customId = row.id, key = "custom_${row.id}") }.getOrNull() },
        )
    }

    suspend fun setStock(key: String, has: Boolean, today: Long) {
        if (has) dao.saveStock(StockItem(key, today)) else dao.deleteStock(key)
    }

    suspend fun saveMeta(m: CocktailMeta) = dao.saveMeta(m)

    suspend fun saveCustom(c: Cocktail): Long = dao.saveCustom(CustomCocktail(c.customId, CocktailJson.encode(c)))

    suspend fun deleteCustom(id: Long) = dao.deleteCustom(id)

    // ---------- backup (one JSON file) ----------
    suspend fun export(out: OutputStream) {
        val json = JSONObject()
            .put("version", 1)
            .put("stock", JSONArray().apply { dao.allStock().forEach { put(JSONObject().put("key", it.key).put("since", it.since)) } })
            .put("meta", JSONArray().apply {
                dao.allMeta().forEach { put(JSONObject().put("key", it.key).put("favorite", it.favorite).put("rating", it.rating).put("notes", it.notes)) }
            })
            .put("custom", JSONArray().apply { dao.allCustom().forEach { put(JSONObject().put("id", it.id).put("json", it.json)) } })
        out.write(json.toString(2).toByteArray(Charsets.UTF_8))
    }

    suspend fun import(input: InputStream) {
        val json = JSONObject(input.readBytes().toString(Charsets.UTF_8))
        val stock = json.getJSONArray("stock").objects().map { StockItem(it.getString("key"), it.getLong("since")) }
        val meta = json.getJSONArray("meta").objects().map {
            CocktailMeta(it.getString("key"), it.optBoolean("favorite"), it.optInt("rating"), it.optString("notes"))
        }
        val custom = json.getJSONArray("custom").objects().map { CustomCocktail(it.getLong("id"), it.getString("json")) }
        db.withTransaction {
            dao.clearStock(); dao.clearMeta(); dao.clearCustom()
            dao.insertStock(stock); dao.insertMeta(meta); dao.insertCustom(custom)
        }
    }
}

fun JSONArray.objects(): List<JSONObject> = (0 until length()).map { getJSONObject(it) }

/** Custom cocktails are stored as JSON. */
object CocktailJson {
    fun encode(c: Cocktail): String = JSONObject()
        .put("name", c.name).put("desc", c.desc).put("family", c.family.name).put("method", c.method.name)
        .put("glass", c.glass.name).put("ice", c.ice.name).put("garnish", c.garnish).put("difficulty", c.difficulty)
        .put("tags", JSONArray(c.tags.map { it.name }))
        .put("steps", JSONArray(c.steps)).put("extraSteps", JSONArray(c.extraSteps))
        .put("parts", JSONArray().apply {
            c.parts.forEach { p ->
                put(
                    JSONObject().put("key", p.key).put("amount", p.amount).put("unit", p.unit.name).put("stage", p.stage.name)
                        .put("optional", p.optional).put("alts", JSONArray(p.alts)).put("note", p.note)
                )
            }
        })
        .toString()

    fun decode(s: String): Cocktail {
        val o = JSONObject(s)
        fun strings(k: String) = o.optJSONArray(k)?.let { a -> (0 until a.length()).map { a.getString(it) } } ?: emptyList()
        return Cocktail(
            key = "custom",
            name = o.getString("name"),
            desc = o.optString("desc"),
            family = Family.valueOf(o.getString("family")),
            method = Method.valueOf(o.getString("method")),
            glass = Glass.valueOf(o.getString("glass")),
            ice = Ice.valueOf(o.getString("ice")),
            parts = o.getJSONArray("parts").objects().map { p ->
                Part(
                    key = p.getString("key"), amount = p.getDouble("amount"),
                    unit = Measure.valueOf(p.getString("unit")), stage = Stage.valueOf(p.getString("stage")),
                    optional = p.optBoolean("optional"),
                    alts = p.optJSONArray("alts")?.let { a -> (0 until a.length()).map { a.getString(it) } } ?: emptyList(),
                    note = p.optString("note"),
                )
            },
            garnish = o.optString("garnish"),
            tags = strings("tags").mapNotNull { runCatching { Tag.valueOf(it) }.getOrNull() }.toSet(),
            difficulty = o.optInt("difficulty", 1),
            steps = strings("steps"),
            extraSteps = strings("extraSteps"),
        )
    }
}

// ---------- settings ----------

data class Settings(val useOz: Boolean = false, val liked: Set<Tag> = emptySet())

class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private val state = MutableStateFlow(
        Settings(
            useOz = prefs.getBoolean("useOz", false),
            liked = prefs.getStringSet("liked", emptySet())!!.mapNotNull { runCatching { Tag.valueOf(it) }.getOrNull() }.toSet(),
        )
    )
    val flow: StateFlow<Settings> = state

    fun update(s: Settings) {
        prefs.edit().putBoolean("useOz", s.useOz).putStringSet("liked", s.liked.map { it.name }.toSet()).apply()
        state.value = s
    }
}
