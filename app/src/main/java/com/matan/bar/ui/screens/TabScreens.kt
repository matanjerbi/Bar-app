package com.matan.bar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.ui.graphics.Brush
import com.matan.bar.ui.CocktailCard
import com.matan.bar.ui.Gold
import com.matan.bar.ui.emoji
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

    Screen("Bar", tab = true, actions = { SettingsAction(nav) }) { pad ->
        LazyColumn(Modifier.padding(pad).fillMaxSize(), contentPadding = listPad, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                val hour = java.time.LocalTime.now().hour
                val hello = when (hour) { in 5..11 -> "בוקר טוב"; in 12..16 -> "צהריים טובים"; in 17..21 -> "ערב טוב"; else -> "לילה טוב" }
                Column(
                    Modifier.fillMaxWidth()
                        .background(
                            Brush.linearGradient(listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.tertiaryContainer)),
                            RoundedCornerShape(24.dp),
                        )
                        .padding(20.dp),
                ) {
                    Text("$hello 🍸", style = MaterialTheme.typography.headlineSmall, color = Gold)
                    Text(
                        if (st.stock.isEmpty()) "הבר עוד ריק" else "${ready.size} קוקטיילים מוכנים · ${oneAway.size} במרחק רכיב אחד",
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
            if (ready.isNotEmpty()) {
                item { SectionTitle("מומלץ לך עכשיו") }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(ready.take(8), key = { "f_" + it.key }) { c -> CocktailCard(c) { nav.navigate(Routes.cocktail(c.key)) } }
                    }
                }
            }
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
            item { SectionTitle("כל מה שאפשר להכין (${ready.size})") }
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
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { FilterChip(selected = family.isEmpty(), onClick = { family = "" }, label = { Text("✨ הכל") }) }
                    items(Family.entries) { f ->
                        FilterChip(selected = family == f.name, onClick = { family = if (family == f.name) "" else f.name }, label = { Text("${f.emoji()} ${f.label()}") })
                    }
                }
            }
            if (showFilters) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Dropdown("בסיס", listOf("") + Logic.baseFilters, base, { if (it.isEmpty()) "הכל" else Ingredients.name(it) }, { base = it }, Modifier.weight(1f))
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

@OptIn(ExperimentalLayoutApi::class)
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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    com.matan.bar.ui.Stat("${st.stock.size}", "רכיבים בבר", Modifier.weight(1f))
                    com.matan.bar.ui.Stat("$readyCount", "קוקטיילים מוכנים", Modifier.weight(1f))
                }
            }
            item {
                OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth().padding(vertical = 8.dp), singleLine = true, label = { Text("חיפוש") })
            }
            IngCat.entries.filter { it != IngCat.STAPLE }.forEach { cat ->
                val inCat = items.filter { it.cat == cat }
                if (inCat.isNotEmpty()) {
                    item(key = "h_${cat.name}") {
                        val have = inCat.count { it.key in st.stock }
                        SectionTitle("${cat.emoji()} ${cat.label()} ($have/${inCat.size})")
                    }
                    val (preps, plain) = inCat.partition { it.prep != null }
                    if (plain.isNotEmpty()) item(key = "c_${cat.name}") {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            plain.forEach { ing ->
                                val checked = ing.key in st.stock
                                FilterChip(
                                    selected = checked,
                                    onClick = { scope.launch { vm.repo.setStock(ing.key, !checked, today) } },
                                    label = { Text(ing.name) },
                                    leadingIcon = if (checked) { { Text("✓") } } else null,
                                )
                            }
                        }
                    }
                    items(preps, key = { it.key }) { ing ->
                        val stock = st.stock[ing.key]
                        val checked = stock != null
                        val prep = ing.prep!!
                        Card(
                            Modifier.fillMaxWidth().padding(vertical = 3.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (checked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer
                            ),
                        ) {
                            Row(Modifier.padding(start = 4.dp, end = 8.dp, top = 4.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = checked, onCheckedChange = { scope.launch { vm.repo.setStock(ing.key, it, today) } })
                                Column(Modifier.weight(1f)) {
                                    Text("🫙 " + ing.name)
                                    val age = stock?.let { today - it.since }
                                    val text = when {
                                        age == null -> if (bar.canPrep(ing.key)) "הכנה ביתית · יש לך הכול להכין" else "הכנה ביתית"
                                        age > prep.shelfDays -> "הוכן לפני $age ימים – כנראה פג, כדאי להכין מחדש"
                                        else -> "הוכן לפני $age ימים · טוב עוד ${prep.shelfDays - age} ימים"
                                    }
                                    Text(
                                        text, style = MaterialTheme.typography.bodySmall,
                                        color = if (age != null && age > prep.shelfDays) Red else MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                if (checked) TextButton(onClick = { scope.launch { vm.repo.setStock(ing.key, true, today) } }) { Text("הכנתי") }
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
