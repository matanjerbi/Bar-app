package com.matan.bar.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.matan.bar.logic.Family
import com.matan.bar.logic.IngCat
import com.matan.bar.logic.Ingredients
import com.matan.bar.logic.Logic
import com.matan.bar.logic.Tag
import com.matan.bar.logic.label
import com.matan.bar.ui.AppViewModel
import com.matan.bar.ui.CocktailRow
import com.matan.bar.ui.Dropdown
import com.matan.bar.ui.Loading
import com.matan.bar.ui.MultiChips
import com.matan.bar.ui.Orange
import com.matan.bar.ui.PrepDialog
import com.matan.bar.ui.Red
import com.matan.bar.ui.Routes
import com.matan.bar.ui.Screen
import com.matan.bar.ui.SectionTitle
import com.matan.bar.ui.SwitchRow
import kotlinx.coroutines.launch
import java.time.LocalDate

private val listPad = PaddingValues(16.dp, 8.dp, 16.dp, 24.dp)

@Composable
private fun SettingsAction(nav: NavController) {
    IconButton(onClick = { nav.navigate(Routes.SETTINGS) }) { Icon(Icons.Default.Settings, "הגדרות") }
}

// ======================= Make now =======================

@Composable
fun MakeScreen(vm: AppViewModel, nav: NavController) {
    val st = vm.state.collectAsStateWithLifecycle().value ?: return Loading()
    val settings by vm.settings.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val all = vm.all(st)
    val bar = vm.bar(st)
    val score = vm.scorer(st, settings)
    val av = Logic.availability(all, bar)
    val ready = av.ready.sortedByDescending(score)
    val oneAway = av.oneAway.sortedByDescending { score(it.first) }
    val suggestions = Logic.suggestions(all, bar)

    Screen("מה אפשר להכין", tab = true, actions = { SettingsAction(nav) }) { pad ->
        LazyColumn(Modifier.padding(pad).fillMaxSize(), contentPadding = listPad, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (st.stock.isEmpty()) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("בוא נתחיל", fontWeight = FontWeight.Bold)
                            Text("סמן ב\"הבר שלי\" אילו בקבוקים, פירות וסירופים יש לך, ואראה לך מה אפשר להכין ומה כדאי לקנות.")
                            Button(onClick = { nav.navigate(Routes.BAR) }) { Text("לבר שלי") }
                        }
                    }
                }
            }
            item { SectionTitle("אפשר להכין עכשיו (${ready.size})") }
            if (ready.isEmpty() && st.stock.isNotEmpty()) item { Text("עוד אין קוקטייל שיש לו את כל הרכיבים – תסתכל למטה מה חסר") }
            items(ready, key = { "r_" + it.key }) { c ->
                val m = st.meta(c.key)
                CocktailRow(c, m.favorite, m.rating) { nav.navigate(Routes.cocktail(c.key)) }
            }
            if (oneAway.isNotEmpty()) {
                item { SectionTitle("חסר רכיב אחד (${oneAway.size})") }
                items(oneAway, key = { "o_" + it.first.key }) { (c, part) ->
                    val m = st.meta(c.key)
                    val prep = bar.canPrep(part.key)
                    CocktailRow(
                        c, m.favorite, m.rating,
                        sub = "חסר: ${Ingredients.name(part.key)}" + if (prep) " · אפשר להכין בבית" else "",
                        subColor = if (prep) Orange else Red,
                    ) { nav.navigate(Routes.cocktail(c.key)) }
                }
            }
            if (suggestions.isNotEmpty()) {
                item { SectionTitle("מה כדאי לקנות או להכין") }
                items(suggestions, key = { "s_" + it.key }) { s ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    (if (s.canPrep) "להכין: " else "") + Ingredients.name(s.key),
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    "+${s.unlocks.size} קוקטיילים: " + s.unlocks.take(4).joinToString(", ") { it.name },
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                            val ing = Ingredients[s.key]
                            if (ing != null && !ing.group) {
                                TextButton(onClick = { scope.launch { vm.repo.setStock(s.key, true, LocalDate.now().toEpochDay()) } }) { Text("יש לי") }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ======================= Catalog =======================

@Composable
fun CatalogScreen(vm: AppViewModel, nav: NavController) {
    val st = vm.state.collectAsStateWithLifecycle().value ?: return Loading()
    val settings by vm.settings.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    var base by rememberSaveable { mutableStateOf("") }
    var family by rememberSaveable { mutableStateOf("") }
    var tags by remember { mutableStateOf(emptySet<Tag>()) }
    var onlyReady by rememberSaveable { mutableStateOf(false) }
    var onlyFav by rememberSaveable { mutableStateOf(false) }
    var showFilters by rememberSaveable { mutableStateOf(false) }

    val bar = vm.bar(st)
    val score = vm.scorer(st, settings)
    val list = vm.all(st).filter { c ->
        (query.isBlank() || c.name.contains(query, true) || c.desc.contains(query) ||
            c.parts.any { Ingredients.name(it.key).contains(query) }) &&
            (base.isEmpty() || Logic.hasBase(c, base)) &&
            (family.isEmpty() || c.family.name == family) &&
            c.tags.containsAll(tags) &&
            (!onlyReady || bar.missing(c).isEmpty()) &&
            (!onlyFav || st.meta(c.key).favorite)
    }.sortedByDescending(score)

    Screen("קטלוג", tab = true, actions = {
        TextButton(onClick = { nav.navigate(Routes.custom(0)) }) { Text("+ מתכון שלי") }
        SettingsAction(nav)
    }) { pad ->
        LazyColumn(Modifier.padding(pad).fillMaxSize(), contentPadding = listPad, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                OutlinedTextField(
                    query, { query = it }, Modifier.fillMaxWidth(), singleLine = true,
                    label = { Text("חיפוש לפי שם או רכיב") },
                )
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${list.size} קוקטיילים", Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = { showFilters = !showFilters }) { Text(if (showFilters) "הסתר סינון" else "סינון") }
                }
            }
            if (showFilters) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Dropdown("בסיס", listOf("") + Logic.baseFilters, base, { if (it.isEmpty()) "הכל" else Ingredients.name(it) }, { base = it }, Modifier.weight(1f))
                            Dropdown("משפחה", listOf("") + Family.entries.map { it.name }, family,
                                { if (it.isEmpty()) "הכל" else Family.valueOf(it).label() }, { family = it }, Modifier.weight(1f))
                        }
                        MultiChips(Tag.entries, tags, { it.label() }) { tags = it }
                        SwitchRow("רק מה שאפשר להכין עכשיו", onlyReady) { onlyReady = it }
                        SwitchRow("רק מועדפים", onlyFav) { onlyFav = it }
                    }
                }
            }
            items(list, key = { it.key }) { c ->
                val m = st.meta(c.key)
                val missing = bar.missing(c)
                CocktailRow(
                    c, m.favorite, m.rating,
                    sub = if (missing.isEmpty()) "✓ אפשר להכין" else "חסרים ${missing.size}",
                    subColor = if (missing.isEmpty()) com.matan.bar.ui.Green else MaterialTheme.colorScheme.outline,
                ) { nav.navigate(Routes.cocktail(c.key)) }
            }
        }
    }
}

// ======================= My bar =======================

@Composable
fun BarScreen(vm: AppViewModel, nav: NavController) {
    val st = vm.state.collectAsStateWithLifecycle().value ?: return Loading()
    val scope = rememberCoroutineScope()
    var query by rememberSaveable { mutableStateOf("") }
    var prepKey by remember { mutableStateOf<String?>(null) }
    val bar = vm.bar(st)
    val today = LocalDate.now().toEpochDay()
    val readyCount = Logic.availability(vm.all(st), bar).ready.size
    val items = Ingredients.stockable.filter { query.isBlank() || it.name.contains(query, true) }

    Screen("הבר שלי", tab = true, actions = { SettingsAction(nav) }) { pad ->
        LazyColumn(Modifier.padding(pad).fillMaxSize(), contentPadding = listPad, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            item {
                Text("${st.stock.size} רכיבים · אפשר להכין $readyCount קוקטיילים", style = MaterialTheme.typography.bodyMedium)
            }
            item {
                OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth().padding(vertical = 8.dp), singleLine = true, label = { Text("חיפוש") })
            }
            IngCat.entries.filter { it != IngCat.STAPLE }.forEach { cat ->
                val inCat = items.filter { it.cat == cat }
                if (inCat.isNotEmpty()) {
                    item(key = "h_${cat.name}") {
                        val have = inCat.count { it.key in st.stock }
                        SectionTitle("${cat.label()} ($have/${inCat.size})")
                    }
                    items(inCat, key = { it.key }) { ing ->
                        val stock = st.stock[ing.key]
                        val checked = stock != null
                        Row(
                            Modifier.fillMaxWidth().clickable {
                                scope.launch { vm.repo.setStock(ing.key, !checked, today) }
                            },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(checked = checked, onCheckedChange = { scope.launch { vm.repo.setStock(ing.key, it, today) } })
                            Column(Modifier.weight(1f)) {
                                Text(ing.name)
                                val prep = ing.prep
                                if (prep != null) {
                                    val age = stock?.let { today - it.since }
                                    val text = when {
                                        age == null -> if (bar.canPrep(ing.key)) "הכנה ביתית · יש לך הכול להכין" else "הכנה ביתית"
                                        age > prep.shelfDays -> "הוכן לפני $age ימים – כנראה פג, כדאי להכין מחדש"
                                        else -> "הוכן לפני $age ימים · טוב עוד ${prep.shelfDays - age} ימים"
                                    }
                                    Text(
                                        text, style = MaterialTheme.typography.bodySmall,
                                        color = if (age != null && age > prep.shelfDays) Red else MaterialTheme.colorScheme.outline,
                                    )
                                }
                            }
                            if (ing.prep != null) {
                                if (checked) TextButton(onClick = { scope.launch { vm.repo.setStock(ing.key, true, today) } }) { Text("הכנתי עכשיו") }
                                TextButton(onClick = { prepKey = ing.key }) { Text("מתכון") }
                            }
                        }
                    }
                }
            }
        }
    }
    prepKey?.let { PrepDialog(it, bar) { prepKey = null } }
}
