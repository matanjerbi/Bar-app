package com.matan.bar.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.matan.bar.logic.Cocktail
import com.matan.bar.logic.Cocktails
import com.matan.bar.logic.Family
import com.matan.bar.logic.Glass
import com.matan.bar.logic.Ice
import com.matan.bar.logic.Ingredients
import com.matan.bar.logic.Measure
import com.matan.bar.logic.Method
import com.matan.bar.logic.Part
import com.matan.bar.logic.Stage
import com.matan.bar.logic.Tag
import com.matan.bar.logic.label
import com.matan.bar.ui.AppViewModel
import com.matan.bar.ui.ConfirmDialog
import com.matan.bar.ui.Dropdown
import com.matan.bar.ui.Loading
import com.matan.bar.ui.MultiChips
import com.matan.bar.ui.NumberField
import com.matan.bar.ui.PrepDialog
import com.matan.bar.ui.RecipeView
import com.matan.bar.ui.Red
import com.matan.bar.ui.Routes
import com.matan.bar.ui.Screen
import com.matan.bar.ui.SectionTitle
import com.matan.bar.ui.Stars
import com.matan.bar.ui.SwitchRow
import com.matan.bar.ui.TextInput
import com.matan.bar.ui.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

// ======================= Cocktail =======================

@Composable
fun CocktailScreen(vm: AppViewModel, nav: NavController, key: String) {
    val st = vm.state.collectAsStateWithLifecycle().value ?: return Loading()
    val settings by vm.settings.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val c = vm.find(st, key)
    var servings by remember { mutableIntStateOf(1) }
    var prepKey by remember { mutableStateOf<String?>(null) }
    var askDelete by remember { mutableStateOf(false) }
    if (c == null) {
        Screen("לא נמצא", nav) { }
        return
    }
    val meta = st.meta(c.key)
    var notes by remember(c.key) { mutableStateOf(meta.notes) }
    val bar = vm.bar(st)

    Screen(
        c.name, nav,
        actions = {
            IconButton(onClick = { scope.launch { vm.repo.saveMeta(meta.copy(favorite = !meta.favorite)) } }) {
                Icon(if (meta.favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, "מועדף", tint = if (meta.favorite) Red else androidx.compose.material3.LocalContentColor.current)
            }
            if (c.customId > 0) {
                IconButton(onClick = { nav.navigate(Routes.custom(c.customId)) }) { Icon(Icons.Default.Edit, "עריכה") }
                IconButton(onClick = { askDelete = true }) { Icon(Icons.Default.Delete, "מחק") }
            }
        },
    ) { pad ->
        Column(
            Modifier.padding(pad).fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RecipeView(c, bar, settings.useOz, servings, { servings = it }, onPrepClick = { prepKey = it })
            HorizontalDivider()
            Text("הדירוג שלי", style = MaterialTheme.typography.titleMedium)
            Stars(meta.rating) { r -> scope.launch { vm.repo.saveMeta(meta.copy(rating = r)) } }
            Text("הדירוגים משפיעים על הסדר של ההמלצות", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            TextInput("הערות אישיות (למשל: פחות סירופ)", notes, { notes = it }, multiline = true)
            if (notes != meta.notes) {
                Button(onClick = { scope.launch { vm.repo.saveMeta(meta.copy(notes = notes)) } }) { Text("שמור הערות") }
            }
            if (c.customId == 0L) {
                OutlinedButton(onClick = {
                    scope.launch {
                        val id = vm.repo.saveCustom(c.copy(name = c.name + " (שלי)", customId = 0))
                        nav.navigate(Routes.custom(id))
                    }
                }) { Text("צור גרסה משלי") }
            }
        }
    }
    prepKey?.let { PrepDialog(it, bar) { prepKey = null } }
    if (askDelete) {
        ConfirmDialog("למחוק את ${c.name}?", onConfirm = {
            scope.launch { vm.repo.deleteCustom(c.customId) }
            nav.popBackStack()
        }, onDismiss = { askDelete = false })
    }
}

// ======================= Custom recipe editor =======================

@Composable
fun CustomEditScreen(vm: AppViewModel, nav: NavController, id: Long) {
    val st = vm.state.collectAsStateWithLifecycle().value ?: return Loading()
    val existing = st.custom.find { it.customId == id }
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf(existing?.name ?: "") }
    var desc by remember { mutableStateOf(existing?.desc ?: "") }
    var family by remember { mutableStateOf(existing?.family ?: Family.SOUR) }
    var method by remember { mutableStateOf(existing?.method ?: Method.SHAKE) }
    var glass by remember { mutableStateOf(existing?.glass ?: Glass.COUPE) }
    var ice by remember { mutableStateOf(existing?.ice ?: Ice.NONE) }
    var tags by remember { mutableStateOf(existing?.tags ?: emptySet()) }
    var parts by remember { mutableStateOf(existing?.parts ?: emptyList()) }
    var garnish by remember { mutableStateOf(existing?.garnish ?: "") }
    var steps by remember { mutableStateOf((existing?.steps ?: emptyList()).joinToString("\n")) }
    var pickerFor by remember { mutableStateOf<Int?>(null) }

    Screen(
        if (existing == null) "מתכון חדש" else "עריכת מתכון", nav,
        actions = {
            TextButton(enabled = name.isNotBlank() && parts.isNotEmpty(), onClick = {
                val c = Cocktail(
                    key = "custom", name = name.trim(), desc = desc.trim(), family = family, method = method, glass = glass, ice = ice,
                    parts = parts.filter { it.key.isNotBlank() }, garnish = garnish.trim(), tags = tags.ifEmpty { setOf(Tag.MODERN) },
                    steps = steps.lines().map { it.trim() }.filter { it.isNotEmpty() },
                    extraSteps = existing?.extraSteps ?: emptyList(), difficulty = existing?.difficulty ?: 1,
                    customId = existing?.customId ?: 0,
                )
                scope.launch {
                    val newId = vm.repo.saveCustom(c)
                    nav.popBackStack()
                    if (existing == null) nav.navigate(Routes.cocktail("custom_$newId"))
                }
            }) { Text("שמור") }
        },
    ) { pad ->
        Column(
            Modifier.padding(pad).fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TextInput("שם", name, { name = it })
            TextInput("תיאור קצר", desc, { desc = it })
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Dropdown("משפחה", Family.entries, family, { it.label() }, { family = it }, Modifier.weight(1f))
                Dropdown("שיטה", Method.entries, method, { it.label() }, { method = it }, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Dropdown("כוס", Glass.entries, glass, { it.label() }, { glass = it }, Modifier.weight(1f))
                Dropdown("קרח", Ice.entries, ice, { it.label() }, { ice = it }, Modifier.weight(1f))
            }

            SectionTitle("רכיבים")
            parts.forEachIndexed { i, p ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                if (p.key.isBlank()) "בחר רכיב" else Ingredients.name(p.key),
                                Modifier.weight(1f).clickable { pickerFor = i }.padding(8.dp),
                                color = MaterialTheme.colorScheme.primary,
                            )
                            IconButton(onClick = { parts = parts.filterIndexed { j, _ -> j != i } }) { Icon(Icons.Default.Close, "הסר") }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            var amountText by remember(i, p.key) { mutableStateOf(if (p.amount % 1.0 == 0.0) p.amount.toInt().toString() else p.amount.toString()) }
                            NumberField("כמות", amountText, { t ->
                                amountText = t
                                t.replace(",", ".").toDoubleOrNull()?.let { v -> parts = parts.mapIndexed { j, x -> if (j == i) x.copy(amount = v) else x } }
                            }, Modifier.weight(1f))
                            Dropdown("יחידה", Measure.entries, p.unit, { it.label() }, { u -> parts = parts.mapIndexed { j, x -> if (j == i) x.copy(unit = u) else x } }, Modifier.weight(1f))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Dropdown("שלב", Stage.entries, p.stage, { it.label() }, { s -> parts = parts.mapIndexed { j, x -> if (j == i) x.copy(stage = s) else x } }, Modifier.weight(1f))
                            Checkbox(p.optional, { o -> parts = parts.mapIndexed { j, x -> if (j == i) x.copy(optional = o) else x } })
                            Text("לא חובה")
                        }
                    }
                }
            }
            OutlinedButton(onClick = { parts = parts + Part("", 30.0); pickerFor = parts.size - 1 }) { Text("+ רכיב") }

            TextInput("קישוט", garnish, { garnish = it })
            TextInput("שלבי הכנה – שורה לכל שלב (ריק = הוראות אוטומטיות לפי השיטה)", steps, { steps = it }, multiline = true)
            SectionTitle("סגנון")
            MultiChips(Tag.entries, tags, { it.label() }) { tags = it }
            if (existing != null) {
                TextButton(onClick = {
                    scope.launch { vm.repo.deleteCustom(existing.customId) }
                    nav.popBackStack(Routes.MAKE, false)
                }) { Text("מחק מתכון", color = Red) }
            }
        }
    }

    pickerFor?.let { index ->
        IngredientPicker(onPick = { key ->
            parts = parts.mapIndexed { j, x -> if (j == index) x.copy(key = key, unit = defaultUnit(key)) else x }
            pickerFor = null
        }, onDismiss = { pickerFor = null })
    }
}

private fun defaultUnit(key: String) = when {
    key == "egg" -> Measure.EGG_WHITE
    Ingredients[key]?.cat == com.matan.bar.logic.IngCat.BITTERS -> Measure.DASH
    key == "mint" || key == "basil" -> Measure.LEAF
    else -> Measure.ML
}

fun Measure.label() = when (this) {
    Measure.ML -> "מ\"ל"
    Measure.DASH -> "דאש"
    Measure.DROP -> "טיפות"
    Measure.BARSPOON -> "כפית בר"
    Measure.EGG_WHITE -> "חלבון"
    Measure.LEAF -> "עלים"
    Measure.PIECE -> "יחידה"
    Measure.PINCH -> "קורט"
    Measure.RINSE -> "שטיפה"
}

fun Stage.label() = when (this) {
    Stage.IN -> "בשייקר / בכוס"
    Stage.RINSE -> "שטיפת כוס"
    Stage.TOP -> "להשלים בסוף"
    Stage.FLOAT -> "לצוף מעל"
}

@Composable
private fun IngredientPicker(onPick: (String) -> Unit, onDismiss: () -> Unit) {
    var q by remember { mutableStateOf("") }
    val list = Ingredients.all.filter { q.isBlank() || it.name.contains(q, true) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("בחר רכיב") },
        text = {
            Column {
                OutlinedTextField(q, { q = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("חיפוש") })
                LazyColumn(Modifier.heightIn(max = 360.dp)) {
                    items(list, key = { it.key }) { ing ->
                        Text(
                            ing.name + if (ing.group) " (כל סוג)" else "",
                            Modifier.fillMaxWidth().clickable { onPick(ing.key) }.padding(vertical = 10.dp),
                        )
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("ביטול") } },
    )
}

// ======================= Settings =======================

@Composable
fun SettingsScreen(vm: AppViewModel, nav: NavController) {
    val s by vm.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pendingImport by remember { mutableStateOf<Uri?>(null) }

    val exporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) scope.launch {
            val ok = withContext(Dispatchers.IO) {
                runCatching { context.contentResolver.openOutputStream(uri)!!.use { vm.repo.export(it) } }.isSuccess
            }
            context.toast(if (ok) "הגיבוי נשמר" else "שמירת הגיבוי נכשלה")
        }
    }
    val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> pendingImport = uri }

    Screen("הגדרות", nav) { pad ->
        Column(
            Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionTitle("יחידות")
            SwitchRow("להציג באונקיות (oz) במקום מ\"ל", s.useOz) { vm.settingsStore.update(s.copy(useOz = it)) }
            SectionTitle("הסגנונות שאני אוהב")
            Text("משפיע על סדר ההמלצות, יחד עם הדירוגים שלך", style = MaterialTheme.typography.bodySmall)
            MultiChips(Tag.entries, s.liked, { it.label() }) { vm.settingsStore.update(s.copy(liked = it)) }
            HorizontalDivider()
            SectionTitle("גיבוי")
            Text("הבר, הדירוגים, ההערות והמתכונים שלך נשמרים רק בטלפון.", style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { exporter.launch("bar-backup-${LocalDate.now()}.json") }) { Text("ייצוא גיבוי") }
                OutlinedButton(onClick = { importer.launch(arrayOf("application/json", "application/octet-stream", "*/*")) }) { Text("שחזור") }
            }
            HorizontalDivider()
            Text(
                "${Cocktails.all.size} מתכונים מובנים · ${Ingredients.stockable.size} רכיבים",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline,
            )
        }
    }

    pendingImport?.let { uri ->
        ConfirmDialog("השחזור יחליף את כל הנתונים הנוכחיים. להמשיך?", confirm = "שחזר", onConfirm = {
            scope.launch {
                val r = withContext(Dispatchers.IO) { runCatching { context.contentResolver.openInputStream(uri)!!.use { vm.repo.import(it) } } }
                context.toast(if (r.isSuccess) "השחזור הושלם" else "השחזור נכשל")
            }
        }, onDismiss = { pendingImport = null })
    }
}
