package com.matan.bar.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.matan.bar.logic.Tag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.matan.bar.logic.Foam
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.matan.bar.logic.Bar
import com.matan.bar.logic.Cocktail
import com.matan.bar.logic.Ingredients
import com.matan.bar.logic.Logic
import com.matan.bar.logic.Stage
import com.matan.bar.logic.label
import kotlin.math.roundToInt

fun pct(v: Double) = "${(v * 100).roundToInt()}%"

/** Small rounded label. */
@Composable
fun Pill(text: String, color: Color = MaterialTheme.colorScheme.primary) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        modifier = Modifier
            .background(color.copy(alpha = 0.14f), RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

/** The drink drawing on a glowing tile. */
@Composable
fun ArtTile(c: Cocktail, modifier: Modifier = Modifier, animate: Boolean = false) {
    val color = remember(c) { drinkColor(c) }
    Box(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.verticalGradient(listOf(color.copy(alpha = 0.28f), MaterialTheme.colorScheme.surfaceContainerLowest))),
        contentAlignment = Alignment.Center,
    ) {
        CocktailArt(c, Modifier.fillMaxSize().padding(8.dp), animate)
    }
}

/** List row for a cocktail. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CocktailRow(c: Cocktail, favorite: Boolean, rating: Int, sub: String? = null, subColor: Color? = null, onClick: () -> Unit) {
    val strength = remember(c) { Logic.strength(c) }
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            ArtTile(c, Modifier.size(width = 76.dp, height = 92.dp))
            Column(Modifier.weight(1f).padding(start = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(c.name, style = MaterialTheme.typography.titleLarge.copy(fontSize = MaterialTheme.typography.titleMedium.fontSize),
                        modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (favorite) Text("♥", color = Red)
                }
                if (c.desc.isNotBlank()) {
                    Text(c.desc, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Pill(pct(strength.abv), MaterialTheme.colorScheme.secondary)
                    c.tags.filter { it != Tag.CLASSIC && it != Tag.MODERN }.take(2).forEach { Pill(it.label()) }
                    if (rating > 0) Pill("★".repeat(rating), Orange)
                }
                if (sub != null) Text(sub, style = MaterialTheme.typography.labelMedium, color = subColor ?: MaterialTheme.colorScheme.primary)
            }
        }
    }
}

/** Big card for horizontal "for you" rows. */
@Composable
fun CocktailCard(c: Cocktail, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(150.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        ArtTile(c, Modifier.fillMaxWidth().height(170.dp))
        Column(Modifier.padding(10.dp)) {
            Text(c.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(c.family.label(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}

/** Header for a drink: big animated drawing over a glow in the drink's colour. */
@Composable
fun DrinkHero(c: Cocktail) {
    val color = remember(c) { drinkColor(c) }
    Box(
        Modifier.fillMaxWidth().height(300.dp)
            .background(Brush.verticalGradient(listOf(color.copy(alpha = 0.45f), MaterialTheme.colorScheme.background))),
        contentAlignment = Alignment.BottomCenter,
    ) {
        CocktailArt(c, Modifier.fillMaxHeight().fillMaxWidth(0.62f).padding(top = 16.dp, bottom = 56.dp), animate = true)
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(c.name, style = MaterialTheme.typography.headlineMedium, color = Gold)
            Text(c.family.label(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Full recipe: info, strength, ingredients with what you have, servings, steps. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecipeView(original: Cocktail, bar: Bar, oz: Boolean, servings: Int, onServings: (Int) -> Unit, onPrepClick: (String) -> Unit = {}) {
    val eggy = Foam.hasEgg(original)
    val veganKey = Foam.veganInBar(bar)
    // no eggs but a vegan foamer in the bar -> start with the vegan version
    var vegan by remember(original.key) { mutableStateOf(eggy && !bar.has(Foam.EGG) && veganKey != null) }
    val c = if (eggy && vegan) Foam.veganize(original, veganKey ?: Foam.AQUAFABA) else original
    val strength = remember(c) { Logic.strength(c) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (c.desc.isNotBlank()) Text(c.desc, style = MaterialTheme.typography.bodyLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Pill(c.method.label())
            Pill("כוס ${c.glass.label()}")
            Pill(c.ice.label())
            Pill("קושי ${"●".repeat(c.difficulty)}${"○".repeat(3 - c.difficulty.coerceIn(1, 3))}", MaterialTheme.colorScheme.secondary)
            c.tags.forEach { Pill(it.label(), MaterialTheme.colorScheme.tertiary) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Stat("~${pct(strength.abv)}", "אלכוהול", Modifier.weight(1f))
            Stat("~${"%.1f".format(strength.standardDrinks)}", "מנות סטנדרטיות", Modifier.weight(1f))
            Stat("~${strength.volumeMl.roundToInt()}", "מ\"ל במשקה", Modifier.weight(1f))
        }

        val missing = bar.missing(c)
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (missing.isEmpty()) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
            ),
        ) {
            Text(
                if (missing.isEmpty()) "יש לך הכול – אפשר להכין" else "חסר: " + missing.joinToString(", ") { Ingredients.name(it.key) },
                Modifier.padding(12.dp),
            )
        }

        if (eggy) VeganCard(vegan, veganKey, Foam.stillAnimal(c)) { vegan = it }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("רכיבים", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            OutlinedButton(onClick = { if (servings > 1) onServings(servings - 1) }) { Text("−") }
            Text("$servings ${if (servings == 1) "מנה" else "מנות"}", Modifier.padding(horizontal = 12.dp))
            OutlinedButton(onClick = { if (servings < 12) onServings(servings + 1) }) { Text("+") }
        }
        val ordered = c.parts.sortedBy { listOf(Stage.RINSE, Stage.IN, Stage.TOP, Stage.FLOAT).indexOf(it.stage) }
        ordered.forEach { p ->
            val ok = bar.partOk(p)
            val prep = Ingredients[p.key]?.prep
            Row(
                Modifier.fillMaxWidth().then(if (prep != null) Modifier.clickable { onPrepClick(p.key) } else Modifier),
                verticalAlignment = Alignment.Top,
            ) {
                Text(if (ok) "✓" else if (p.optional) "○" else "✗", color = if (ok) Green else if (p.optional) MaterialTheme.colorScheme.outline else Red,
                    modifier = Modifier.width(22.dp))
                Column(Modifier.weight(1f)) {
                    val stage = when (p.stage) {
                        Stage.TOP -> "להשלים: "
                        Stage.FLOAT -> "לצוף מעל: "
                        else -> ""
                    }
                    Text(
                        stage + (if (p.stage == Stage.RINSE) "" else Logic.amountText(p, servings, oz) + " ") + Logic.partName(p) +
                            (if (p.stage == Stage.RINSE) " (לשטיפת הכוס)" else "") + (if (p.optional) " (לא חובה)" else ""),
                    )
                    val extra = buildList {
                        if (p.note.isNotBlank()) add(p.note)
                        if (p.alts.isNotEmpty()) add("או: " + p.alts.joinToString(", ") { Ingredients.name(it) })
                        if (prep != null) add(if (!ok && bar.canPrep(p.key)) "הכנה ביתית – יש לך הכול להכין · מתכון" else "הכנה ביתית · מתכון")
                    }
                    if (extra.isNotEmpty()) Text(extra.joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                }
            }
        }

        HorizontalDivider()
        Text("הכנה", style = MaterialTheme.typography.titleMedium)
        Logic.steps(c, servings, oz).forEachIndexed { i, s ->
            Row {
                Text("${i + 1}.", Modifier.width(26.dp), fontWeight = FontWeight.Bold)
                Text(s, Modifier.weight(1f))
            }
        }
        Logic.dryShakeTip(c)?.let {
            Text("💡 $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}

/** Swap the egg white for aquafaba / a vegan foamer. */
@Composable
private fun VeganCard(vegan: Boolean, inBar: String?, stillAnimal: List<String>, onChange: (Boolean) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            SwitchRow("🌱 גרסה טבעונית (בלי ביצה)", vegan, onChange)
            val text = when {
                vegan && inBar != null -> "החלבון הוחלף ב${Ingredients.name(inBar)} שיש לך בבר. הניעור היבש נשאר אותו דבר."
                vegan -> "החלבון הוחלף במי חומוס – הנוזל מקופסת חומוס, 30 מ\"ל לכל חלבון. אין לך אותם בבר עדיין."
                inBar != null -> "יש לך ${Ingredients.name(inBar)} בבר – אפשר להחליף בו את החלבון ולקבל קצף טבעוני."
                else -> "אם תרצה גרסה טבעונית – החלף את החלבון ב-30 מ\"ל מי חומוס (הנוזל מקופסת חומוס), או בכמה דאשים של מקציף צמחי."
            }
            Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (vegan && stillAnimal.isNotEmpty()) {
                Text(
                    "שים לב: במשקה נשאר גם ${stillAnimal.joinToString(", ")} – לגרסה טבעונית מלאה צריך תחליף גם לזה.",
                    style = MaterialTheme.typography.bodySmall, color = Orange,
                )
            }
        }
    }
}

/** Recipe for a homemade syrup. */
@Composable
fun PrepDialog(key: String, bar: Bar, onDismiss: () -> Unit) {
    val ing = Ingredients[key] ?: return
    val prep = ing.prep ?: return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(ing.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(prep.recipe)
                Text(
                    "צריך: " + prep.needs.joinToString(", ") { (if (bar.has(it)) "✓ " else "✗ ") + Ingredients.name(it) },
                    style = MaterialTheme.typography.bodySmall,
                )
                Text("נשמר במקרר עד ${prep.shelfDays} ימים", style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("סגור") } },
    )
}

@Composable
fun Stars(rating: Int, onChange: (Int) -> Unit) {
    Row {
        (1..5).forEach { n ->
            Text(
                if (n <= rating) "★" else "☆",
                style = MaterialTheme.typography.headlineSmall,
                color = Orange,
                modifier = Modifier.clickable { onChange(if (n == rating) 0 else n) }.padding(4.dp),
            )
        }
    }
}

@Composable
fun Stat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(16.dp))
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, style = MaterialTheme.typography.titleLarge, color = Gold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

fun com.matan.bar.logic.Family.emoji() = when (this) {
    com.matan.bar.logic.Family.SOUR -> "🍋"
    com.matan.bar.logic.Family.EQUAL_PARTS -> "⚖️"
    com.matan.bar.logic.Family.OLD_FASHIONED -> "🥃"
    com.matan.bar.logic.Family.MARTINI_MANHATTAN -> "🍸"
    com.matan.bar.logic.Family.NEGRONI -> "🍊"
    com.matan.bar.logic.Family.HIGHBALL -> "🧊"
    com.matan.bar.logic.Family.SPRITZ -> "🥂"
    com.matan.bar.logic.Family.TIKI -> "🍍"
    com.matan.bar.logic.Family.CREAMY -> "☕"
}

fun com.matan.bar.logic.IngCat.emoji() = when (this) {
    com.matan.bar.logic.IngCat.SPIRIT -> "🥃"
    com.matan.bar.logic.IngCat.LIQUEUR -> "🍾"
    com.matan.bar.logic.IngCat.WINE -> "🍷"
    com.matan.bar.logic.IngCat.BITTERS -> "💧"
    com.matan.bar.logic.IngCat.FRESH -> "🍋"
    com.matan.bar.logic.IngCat.FOAMER -> "🥚"
    com.matan.bar.logic.IngCat.SWEETENER -> "🍯"
    com.matan.bar.logic.IngCat.MIXER -> "🫧"
    com.matan.bar.logic.IngCat.STAPLE -> "🧂"
}
