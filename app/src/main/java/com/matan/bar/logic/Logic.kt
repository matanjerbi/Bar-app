package com.matan.bar.logic

import kotlin.math.floor
import kotlin.math.roundToInt

/** What's in the bar right now. [stock] = ingredient keys the user has. */
class Bar(val stock: Set<String>) {
    fun has(key: String): Boolean {
        if (Ingredients[key]?.cat == IngCat.STAPLE) return true
        return stock.any { Ingredients.isA(it, key) }
    }

    fun partOk(p: Part) = has(p.key) || p.alts.any { has(it) }

    /** Required parts that are missing. */
    fun missing(c: Cocktail): List<Part> = c.parts.filter { !it.optional && !partOk(it) }

    /** A missing homemade prep you could make right now from what you have. */
    fun canPrep(key: String): Boolean {
        val prep = Ingredients[key]?.prep ?: return false
        return prep.needs.all { has(it) }
    }
}

data class Availability(val ready: List<Cocktail>, val oneAway: List<Pair<Cocktail, Part>>)

data class Suggestion(val key: String, val unlocks: List<Cocktail>, val canPrep: Boolean)

object Logic {
    fun availability(all: List<Cocktail>, bar: Bar): Availability {
        val ready = mutableListOf<Cocktail>()
        val one = mutableListOf<Pair<Cocktail, Part>>()
        for (c in all) {
            val m = bar.missing(c)
            when (m.size) {
                0 -> ready += c
                1 -> one += c to m[0]
            }
        }
        return Availability(ready, one)
    }

    /** What to buy (or make) next: ingredients that each complete the most "one away" cocktails. */
    fun suggestions(all: List<Cocktail>, bar: Bar, limit: Int = 30): List<Suggestion> =
        availability(all, bar).oneAway
            .groupBy { it.second.key }
            .map { (key, list) -> Suggestion(key, list.map { it.first }, bar.canPrep(key)) }
            .sortedByDescending { it.unlocks.size }
            .take(limit)

    data class PairSuggestion(val a: String, val b: String, val unlocks: List<Cocktail>)

    /**
     * Two ingredients worth buying together: they unlock drinks that neither unlocks alone
     * (cocktails missing exactly these two). Ranked by how many drinks the pair opens in total.
     */
    fun pairSuggestions(all: List<Cocktail>, bar: Bar, limit: Int = 5): List<PairSuggestion> {
        val missing = all.associateWith { c -> bar.missing(c).map { it.key }.toSet() }
        val needTwo = missing.filter { it.value.size == 2 }
        val pairs = needTwo.values.map { it.sorted() }.distinct()
        return pairs.map { (a, b) ->
            val unlocks = missing.filter { (_, m) -> m.isNotEmpty() && (m - a - b).isEmpty() }.keys.toList()
            PairSuggestion(a, b, unlocks)
        }
            // only pairs that open something new together
            .filter { p -> p.unlocks.any { missing[it]!!.size == 2 } }
            .sortedByDescending { it.unlocks.size }
            .take(limit)
    }

    // ---------- amounts ----------

    /** Approximate liquid volume of a part, for ABV/volume estimates. */
    fun volumeMl(p: Part): Double = when (p.unit) {
        Measure.ML -> p.amount
        Measure.DASH -> p.amount * 0.8
        Measure.DROP -> p.amount * 0.05
        Measure.BARSPOON -> p.amount * 5
        Measure.EGG_WHITE -> p.amount * 30
        Measure.PIECE -> if (Ingredients[p.key]?.juice == true) p.amount * 30 else 0.0
        Measure.LEAF, Measure.PINCH, Measure.RINSE -> 0.0
    }

    private fun num(v: Double): String {
        val r = (v * 2).roundToInt() / 2.0 // nearest 0.5
        return if (r % 1.0 == 0.0) r.toInt().toString() else r.toString()
    }

    private fun frac(v: Double): String {
        val q = (v * 4).roundToInt() / 4.0
        val whole = floor(q).toInt()
        val f = when (((q - whole) * 4).roundToInt()) { 1 -> "¼"; 2 -> "½"; 3 -> "¾"; else -> "" }
        return when {
            whole == 0 && f.isEmpty() -> "0"
            whole == 0 -> f
            else -> "$whole$f"
        }
    }

    fun mlText(ml: Double, oz: Boolean) = if (oz) "${frac(ml / 30.0)} oz" else "${num(ml)} מ\"ל"

    /** "22.5 מ"ל", "2 דאשים", "½ חלבון"... scaled by [servings]. */
    fun amountText(p: Part, servings: Int, oz: Boolean): String {
        val a = p.amount * servings
        return when (p.unit) {
            Measure.ML -> mlText(a, oz)
            Measure.DASH -> if (a == 1.0) "דאש" else "${num(a)} דאשים"
            Measure.DROP -> "${num(a)} טיפות"
            Measure.BARSPOON -> if (a == 1.0) "כפית בר" else "${num(a)} כפיות בר"
            Measure.EGG_WHITE -> if (a == 0.5) "½ חלבון" else if (a == 1.0) "חלבון" else "${frac(a)} חלבונים"
            Measure.LEAF -> "${num(a)} עלים"
            Measure.PIECE -> frac(a)
            Measure.PINCH -> "קורט"
            Measure.RINSE -> "שטיפה"
        }
    }

    /** Name as used in a recipe line: citrus in ml means fresh juice. */
    fun partName(p: Part): String {
        val ing = Ingredients[p.key]
        val base = ing?.name ?: p.key
        return if (ing?.juice == true && p.unit == Measure.ML) "מיץ $base סחוט טרי" else base
    }

    // ---------- method steps ----------

    fun steps(c: Cocktail, servings: Int, oz: Boolean): List<String> {
        val s = mutableListOf<String>()
        val glass = c.glass.label()
        if (c.ice == Ice.NONE && c.method != Method.HOT) s += "לקרר את כוס ה$glass – במקפיא, או עם קרח ומים בזמן ההכנה"
        c.parts.filter { it.stage == Stage.RINSE }.forEach {
            s += "לשטוף את הכוס ב${partName(it)}: לסובב מעט בתוך הכוס ולשפוך את העודף"
        }
        val tops = c.parts.filter { it.stage == Stage.TOP }
        val floats = c.parts.filter { it.stage == Stage.FLOAT }

        if (c.steps.isNotEmpty()) {
            s += c.steps
        } else {
            val strain = if (c.ice == Ice.NONE) "סינון כפול (מסננת שייקר ומסננת דקה) לכוס ה$glass המקוררת"
            else "לסנן לכוס $glass עם ${c.ice.label()} טריים"
            when (c.method) {
                Method.SHAKE -> {
                    s += "להכניס לשייקר את כל הרכיבים" + if (tops.isNotEmpty()) " (חוץ ממה שמשלימים בסוף)" else ""
                    s += "למלא קרח ולנער חזק 10–12 שניות, עד שהשייקר קפוא מבחוץ"
                    s += strain
                }
                Method.DRY_SHAKE -> {
                    s += "להכניס לשייקר את כל הרכיבים, בלי קרח"
                    s += "ניעור יבש חזק כ-15 שניות – זה מה שבונה את הקצף"
                    s += "להוסיף קרח ולנער שוב 10–12 שניות"
                    s += if (c.ice == Ice.NONE) strain else "סינון כפול לכוס $glass עם ${c.ice.label()}"
                }
                Method.STIR -> {
                    s += "בכוס ערבוב: כל הרכיבים"
                    s += "למלא קרח ולערבב 20–30 שניות, עד שהכוס קרה מאוד מבחוץ"
                    s += if (c.ice == Ice.NONE) "לסנן לכוס ה$glass המקוררת" else "לסנן לכוס $glass עם ${c.ice.label()}"
                }
                else -> {
                    s += "למלא כוס $glass ב${c.ice.label()}"
                    if (c.parts.any { it.stage == Stage.IN }) s += "למזוג פנימה את הרכיבים"
                }
            }
            if (tops.isNotEmpty()) {
                s += "להשלים ב${tops.joinToString(" ו") { "${partName(it)} (${amountText(it, servings, oz)})" }} " +
                    "ולערבב בעדינות פעם אחת – כדי לא לאבד בועות"
            }
            floats.forEach {
                s += "לצוף מעל ${amountText(it, servings, oz)} ${partName(it)}: לשפוך לאט על גב כפית"
            }
        }
        s += c.extraSteps
        if (c.garnish.isNotBlank()) s += "קישוט: ${c.garnish}"
        return s
    }

    fun dryShakeTip(c: Cocktail) = if (c.method == Method.DRY_SHAKE)
        "לקצף צפוף עוד יותר: Reverse dry shake – קודם לנער עם קרח, לסנן, להוציא את הקרח ולנער שוב בלי קרח."
    else null

    // ---------- ABV ----------

    data class Strength(val abv: Double, val volumeMl: Double, val standardDrinks: Double)

    /**
     * Final ABV after dilution. Dilution formulas from Dave Arnold (Liquid Intelligence):
     * shaken -1.567a² + 1.742a + 0.203, stirred -1.21a² + 1.246a + 0.145 (a = ABV before dilution).
     * Standard drink = 10 g ethanol.
     */
    fun strength(c: Cocktail): Strength {
        val inner = c.parts.filter { it.stage == Stage.IN }
        val after = c.parts.filter { it.stage == Stage.TOP || it.stage == Stage.FLOAT }
        val vol = inner.sumOf { volumeMl(it) }
        val eth = inner.sumOf { volumeMl(it) * (Ingredients[it.key]?.abv ?: 0.0) }
        val a = if (vol > 0) eth / vol else 0.0
        val dilution = when (c.method) {
            Method.SHAKE, Method.DRY_SHAKE -> -1.567 * a * a + 1.742 * a + 0.203
            Method.STIR -> -1.21 * a * a + 1.246 * a + 0.145
            Method.BUILD -> if (c.ice == Ice.NONE) 0.0 else 0.15
            Method.MUDDLE, Method.SWIZZLE -> 0.3
            Method.HOT -> 0.0
        }
        val totalVol = vol * (1 + dilution) + after.sumOf { volumeMl(it) }
        val totalEth = eth + after.sumOf { volumeMl(it) * (Ingredients[it.key]?.abv ?: 0.0) }
        val abv = if (totalVol > 0) totalEth / totalVol else 0.0
        return Strength(abv, totalVol, totalEth * 0.789 / 10.0)
    }

    // ---------- taste ----------

    /** Liked tags from settings + learning from ratings and favourites. Higher = more your style. */
    fun tasteScore(c: Cocktail, liked: Set<Tag>, rated: List<Pair<Cocktail, Int>>, favorites: List<Cocktail>): Double {
        val w = HashMap<Tag, Double>()
        liked.forEach { w[it] = (w[it] ?: 0.0) + 2.0 }
        rated.forEach { (rc, stars) -> rc.tags.forEach { w[it] = (w[it] ?: 0.0) + (stars - 3) } }
        favorites.forEach { fc -> fc.tags.forEach { w[it] = (w[it] ?: 0.0) + 1.0 } }
        return c.tags.sumOf { w[it] ?: 0.0 }
    }

    /** Main spirits of a cocktail (for filtering by base). */
    fun spirits(c: Cocktail): List<String> =
        c.parts.filter { Ingredients[it.key]?.cat == IngCat.SPIRIT && it.stage != Stage.RINSE }.map { it.key }

    /** Top-level spirit groups for the catalog filter. */
    val baseFilters = listOf("gin", "vodka", "whiskey", "rum", "tequila", "mezcal", "brandy", "pisco", "cachaca")

    fun hasBase(c: Cocktail, base: String) = spirits(c).any { Ingredients.isA(it, base) }
}
