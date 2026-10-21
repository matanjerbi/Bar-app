package com.matan.bar.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.matan.bar.logic.Builder
import com.matan.bar.logic.Cocktail
import com.matan.bar.logic.Cocktails
import com.matan.bar.logic.Ingredients
import com.matan.bar.ui.AppViewModel
import com.matan.bar.ui.ChoiceChips
import com.matan.bar.ui.Dropdown
import com.matan.bar.ui.Loading
import com.matan.bar.ui.PrepDialog
import com.matan.bar.ui.RecipeView
import com.matan.bar.ui.Routes
import com.matan.bar.ui.Screen
import com.matan.bar.ui.SwitchRow
import com.matan.bar.ui.toast
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

@Composable
fun BuilderScreen(vm: AppViewModel, nav: NavController) {
    val st = vm.state.collectAsStateWithLifecycle().value ?: return Loading()
    val settings by vm.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val bar = vm.bar(st)

    var template by rememberSaveable { mutableStateOf(Builder.Template.SOUR) }
    var onlyMine by rememberSaveable { mutableStateOf(true) }
    var baseSel by rememberSaveable { mutableStateOf("") }
    var citrusSel by rememberSaveable { mutableStateOf("") }
    var sweetSel by rememberSaveable { mutableStateOf("") }
    var modSel by rememberSaveable { mutableStateOf("") }
    var egg by rememberSaveable { mutableStateOf(true) }
    var accentSel by rememberSaveable { mutableStateOf("") }
    var bittersSel by rememberSaveable { mutableStateOf("") }
    var addOrange by rememberSaveable { mutableStateOf(false) }
    var mixerSel by rememberSaveable { mutableStateOf("") }
    var servings by remember { mutableIntStateOf(1) }
    var prepKey by remember { mutableStateOf<String?>(null) }

    fun ok(key: String?) = key == null || !onlyMine || bar.has(key) || Ingredients[key]?.prep?.let { bar.canPrep(key) } == true

    // pick a valid value from options, keeping the user's choice when still valid
    fun <T> pick(options: List<T>, sel: String, keyOf: (T) -> String): T? = options.find { keyOf(it) == sel } ?: options.firstOrNull()

    val bases = when (template) {
        Builder.Template.SOUR -> Builder.sourBases
        Builder.Template.OLD_FASHIONED -> Builder.ofBases
        Builder.Template.HIGHBALL -> Builder.hbBases
    }.filter { ok(it) }
    val base = pick(bases, baseSel) { it }

    Screen("בונה קוקטיילים", tab = true) { pad ->
        Column(
            Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "בונה ריפים חדשים רק בתוך תבניות מוכחות ועם שילובי טעמים שעובדים. בלי ניסויים אקראיים.",
                style = MaterialTheme.typography.bodySmall,
            )
            ChoiceChips(Builder.Template.entries, template, { it.label }) { template = it }
            SwitchRow("רק ממה שיש לי (כולל סירופים שאפשר להכין)", onlyMine) { onlyMine = it }

            if (base == null) {
                Text("אין בבר שלך אלכוהול בסיס שמתאים לתבנית הזו. אפשר לכבות את \"רק ממה שיש לי\".")
                return@Column
            }
            Dropdown("בסיס", bases, base, { Ingredients.name(it) }, { baseSel = it })

            val built: Cocktail? = when (template) {
                Builder.Template.SOUR -> {
                    val citrusOpts = listOf("lemon", "lime").filter { ok(it) }
                    val citrus = citrusOpts.find { it == citrusSel } ?: Builder.defaultCitrus(base).takeIf { it in citrusOpts } ?: citrusOpts.firstOrNull()
                    val sweets = Builder.sourSweetenersFor(base).filter { ok(it.key) }
                    val sweet = pick(sweets, sweetSel) { it.key }
                    val mods = listOf<Builder.Opt?>(null) + Builder.sourModifiersFor(base).filter { ok(it.key) }
                    val mod = mods.find { it?.key == modSel.ifEmpty { null } } ?: null
                    val eggOk = ok("egg")
                    val useEgg = egg && eggOk
                    val accents = listOf<Builder.Accent?>(null) + Builder.accentsFor(base, useEgg).filter { ok(it.needs) }
                    val accent = accents.find { it?.name == accentSel.ifEmpty { null } }

                    if (citrus == null || sweet == null) {
                        Text("חסר לימון/ליים או ממתיק שמתאים. אפשר לכבות את \"רק ממה שיש לי\".")
                        null
                    } else {
                        ChoiceChips(citrusOpts, citrus, { Ingredients.name(it) }) { citrusSel = it }
                        Dropdown("ממתיק", sweets, sweet, { Ingredients.name(it.key) }, { sweetSel = it.key })
                        Dropdown("ליקר משלים (לא חובה)", mods, mod, { it?.let { m -> Ingredients.name(m.key) } ?: "בלי" }, { modSel = it?.key ?: "" })
                        if (eggOk) SwitchRow("חלבון לקצף", egg) { egg = it }
                        Dropdown("נגיעה (לא חובה)", accents, accent, { it?.label ?: "בלי" }, { accentSel = it?.name ?: "" })
                        Builder.buildSour(Builder.SourSpec(base, citrus, sweet.key, mod?.key, useEgg, accent))
                    }
                }
                Builder.Template.OLD_FASHIONED -> {
                    val sweets = Builder.ofSweetenersFor(base).filter { ok(it.key) }
                    val sweet = pick(sweets, sweetSel) { it.key }
                    val bitters = Builder.ofBittersFor(base).filter { ok(it.key) }
                    val bit = pick(bitters, bittersSel) { it.key }
                    if (sweet == null || bit == null) {
                        Text("חסר ממתיק או ביטרס. אפשר לכבות את \"רק ממה שיש לי\".")
                        null
                    } else {
                        Dropdown("ממתיק", sweets, sweet, { Ingredients.name(it.key) }, { sweetSel = it.key })
                        Dropdown("ביטרס", bitters, bit, { Ingredients.name(it.key) }, { bittersSel = it.key })
                        if (bit.key != "orange_bitters" && ok("orange_bitters")) SwitchRow("+ דאש אורנג' ביטרס", addOrange) { addOrange = it }
                        Builder.buildOldFashioned(Builder.OfSpec(base, sweet.key, bit.key, addOrange && ok("orange_bitters")))
                    }
                }
                Builder.Template.HIGHBALL -> {
                    val pairs = Builder.mixersFor(base).filter { ok(it.mixer) && ok(it.citrus) }
                    val pair = pick(pairs, mixerSel) { it.mixer }
                    if (pair == null) {
                        Text("אין מיקסר מתאים בבר. אפשר לכבות את \"רק ממה שיש לי\".")
                        null
                    } else {
                        Dropdown("מיקסר", pairs, pair, { Ingredients.name(it.mixer) }, { mixerSel = it.mixer })
                        Builder.buildHighball(base, pair)
                    }
                }
            }

            built?.let { c ->
                HorizontalDivider()
                Text(c.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                val similar = Builder.similar(c, Cocktails.all)
                if (similar.isNotEmpty()) {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text("קרוב לקלאסיקה:", style = MaterialTheme.typography.bodySmall)
                            similar.forEach { s -> TextButton(onClick = { nav.navigate(Routes.cocktail(s.key)) }) { Text(s.name) } }
                        }
                    }
                }
                RecipeView(c, bar, settings.useOz, servings, { servings = it }, onPrepClick = { prepKey = it })
                Button(
                    onClick = {
                        scope.launch {
                            val id = vm.repo.saveCustom(c)
                            context.toast("נשמר במתכונים שלי")
                            nav.navigate(Routes.cocktail("custom_$id"))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("שמור למתכונים שלי") }
            }
        }
    }
    prepKey?.let { PrepDialog(it, bar) { prepKey = null } }
}
