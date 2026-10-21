package com.matan.bar.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.matan.bar.logic.Cocktail
import com.matan.bar.logic.Ingredients
import com.matan.bar.logic.Logic
import com.matan.bar.logic.Suggestion
import com.matan.bar.logic.label
import com.matan.bar.ui.AppViewModel
import com.matan.bar.ui.ArtTile
import com.matan.bar.ui.Gold
import com.matan.bar.ui.Loading
import com.matan.bar.ui.Pill
import com.matan.bar.ui.PrepDialog
import com.matan.bar.ui.Routes
import com.matan.bar.ui.Screen
import com.matan.bar.ui.SectionTitle
import com.matan.bar.ui.pct
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Home-screen teaser: the single best thing to buy next. */
@Composable
fun BestBuyCard(s: Suggestion, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(if (s.canPrep) "🫙 הכי שווה להכין עכשיו" else "🛒 הקנייה הכי משתלמת", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer)
            Text("${Ingredients.name(s.key)} → +${s.unlocks.size} קוקטיילים", style = MaterialTheme.typography.titleLarge, color = Gold)
            Text(s.unlocks.take(5).joinToString(" · ") { it.name }, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer)
            Text("לכל ההמלצות ←", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
fun ShoppingScreen(vm: AppViewModel, nav: NavController) {
    val st = vm.state.collectAsStateWithLifecycle().value ?: return Loading()
    val scope = rememberCoroutineScope()
    val all = vm.all(st)
    val bar = vm.bar(st)
    val singles = Logic.suggestions(all, bar)
    val pairs = Logic.pairSuggestions(all, bar)
    val readyNow = Logic.availability(all, bar).ready.size
    var open by remember { mutableStateOf<String?>(singles.firstOrNull()?.key) }
    var prepKey by remember { mutableStateOf<String?>(null) }
    val today = LocalDate.now().toEpochDay()

    fun have(key: String) = scope.launch { vm.repo.setStock(key, true, today) }
    fun buyable(key: String) = Ingredients[key]?.let { !it.group } == true

    Screen("מה שווה לקנות", nav) { pad ->
        LazyColumn(
            Modifier.padding(pad).fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 32.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Text(
                    "עכשיו אפשר להכין $readyNow קוקטיילים. ככה כל רכיב מגדיל את הבר שלך:",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (singles.isEmpty() && pairs.isEmpty()) {
                item { Text("אין כרגע רכיב אחד שפותח קוקטייל חדש. תוסיף כמה בסיסים לבר (ג'ין, בורבון, לימון, סירופ סוכר) ונתחיל.") }
            }
            if (singles.isNotEmpty()) item { SectionTitle("רכיב אחד – קוקטיילים חדשים") }
            items(singles, key = { "s_" + it.key }) { s ->
                val expanded = open == s.key
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                ) {
                    Column {
                        Row(
                            Modifier.fillMaxWidth().clickable { open = if (expanded) null else s.key }.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "+${s.unlocks.size}",
                                style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.background(Gold, RoundedCornerShape(14.dp)).padding(horizontal = 12.dp, vertical = 6.dp),
                            )
                            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                                Text(Ingredients.name(s.key), style = MaterialTheme.typography.titleMedium)
                                Text(
                                    if (s.canPrep) "הכנה ביתית – יש לך הכול להכין" else Ingredients[s.key]?.cat?.label() ?: "",
                                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(if (expanded) "▲" else "▼", color = MaterialTheme.colorScheme.outline)
                        }
                        AnimatedVisibility(expanded) {
                            Column(Modifier.padding(start = 14.dp, end = 14.dp, bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("זה יפתח לך:", style = MaterialTheme.typography.labelLarge)
                                s.unlocks.forEach { c -> UnlockRow(c) { nav.navigate(Routes.cocktail(c.key)) } }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (Ingredients[s.key]?.prep != null) TextButton(onClick = { prepKey = s.key }) { Text("מתכון") }
                                    if (buyable(s.key)) FilledTonalButton(onClick = { have(s.key) }) { Text(if (s.canPrep) "הכנתי ✓" else "קניתי ✓") }
                                    else Text("כל סוג מהקטגוריה מתאים – סמן ב\"הבר שלי\" את מה שקנית", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
            if (pairs.isNotEmpty()) {
                item { SectionTitle("שווה לקנות ביחד") }
                item {
                    Text("זוגות שכל אחד לבד לא מספיק, אבל יחד הם פותחים קוקטיילים חדשים",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                items(pairs, key = { "p_" + it.a + it.b }) { p ->
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "${Ingredients.name(p.a)} + ${Ingredients.name(p.b)}",
                                    style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f),
                                )
                                Pill("+${p.unlocks.size}", Gold)
                            }
                            p.unlocks.forEach { c -> UnlockRow(c) { nav.navigate(Routes.cocktail(c.key)) } }
                            if (buyable(p.a) && buyable(p.b)) {
                                FilledTonalButton(onClick = { have(p.a); have(p.b) }) { Text("קניתי את שניהם ✓") }
                            }
                        }
                    }
                }
            }
        }
    }
    prepKey?.let { PrepDialog(it, bar) { prepKey = null } }
}

@Composable
private fun UnlockRow(c: Cocktail, onClick: () -> Unit) {
    val strength = remember(c) { Logic.strength(c) }
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick)
            .background(Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.colorScheme.surfaceContainer))),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ArtTile(c, Modifier.size(width = 48.dp, height = 58.dp))
        Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
            Text(c.name, style = MaterialTheme.typography.titleSmall)
            Text("${c.family.label()} · ${pct(strength.abv)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        }
        Text("←", Modifier.padding(end = 10.dp), color = MaterialTheme.colorScheme.outline)
    }
}

