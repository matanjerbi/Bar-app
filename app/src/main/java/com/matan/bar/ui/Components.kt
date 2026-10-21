package com.matan.bar.ui

import androidx.compose.foundation.clickable
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

/** List row for a cocktail. */
@Composable
fun CocktailRow(c: Cocktail, favorite: Boolean, rating: Int, sub: String? = null, subColor: androidx.compose.ui.graphics.Color? = null, onClick: () -> Unit) {
    val strength = remember(c) { Logic.strength(c) }
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(c.name, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                if (rating > 0) Text("★".repeat(rating), color = Orange, style = MaterialTheme.typography.bodySmall)
                if (favorite) Text(" ♥", color = Red)
            }
            if (c.desc.isNotBlank()) {
                Text(c.desc, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(
                "${c.family.label()} · ${pct(strength.abv)} · " + c.tags.take(3).joinToString(" · ") { it.label() },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
            )
            if (sub != null) Text(sub, style = MaterialTheme.typography.bodySmall, color = subColor ?: MaterialTheme.colorScheme.primary)
        }
    }
}

/** Full recipe: info, strength, ingredients with what you have, servings, steps. */
@Composable
fun RecipeView(c: Cocktail, bar: Bar, oz: Boolean, servings: Int, onServings: (Int) -> Unit, onPrepClick: (String) -> Unit = {}) {
    val strength = remember(c) { Logic.strength(c) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (c.desc.isNotBlank()) Text(c.desc)
        Text(
            "${c.family.label()} · ${c.method.label()} · כוס ${c.glass.label()} · ${c.ice.label()}" +
                " · קושי ${"●".repeat(c.difficulty)}${"○".repeat(3 - c.difficulty.coerceIn(1, 3))}",
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            "~${pct(strength.abv)} אלכוהול · ~${"%.1f".format(strength.standardDrinks)} מנות סטנדרטיות · ~${strength.volumeMl.roundToInt()} מ\"ל במשקה",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
        )

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
