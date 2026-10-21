package com.matan.bar.logic

/**
 * Builds new drinks only inside proven templates (sour, old fashioned, highball),
 * and only with pairings that are known to work. No random combinations.
 */
object Builder {
    enum class Template(val label: String) { SOUR("סאוור"), OLD_FASHIONED("אולד פאשנד"), HIGHBALL("הייבול") }

    /** An option allowed only with bases that are (descendants of) [allowed]; null = any base. */
    data class Opt(val key: String, val amount: Double, val allowed: List<String>? = null)

    private fun fits(base: String, allowed: List<String>?) = allowed == null || allowed.any { Ingredients.isA(base, it) }

    // ================= SOUR =================
    val sourBases = listOf(
        "gin", "vodka", "bourbon", "rye", "blended_scotch", "islay_scotch", "irish_whiskey", "japanese_whisky",
        "white_rum", "aged_rum", "dark_rum", "cachaca", "tequila_blanco", "tequila_reposado", "mezcal",
        "cognac", "calvados", "pisco",
    )

    private val limeBases = listOf("rum", "cachaca", "tequila", "mezcal", "pisco")
    fun defaultCitrus(base: String) = if (limeBases.any { Ingredients.isA(base, it) }) "lime" else "lemon"

    private val sourSweeteners = listOf(
        Opt("simple_syrup", 22.5),
        Opt("demerara_syrup", 15.0),
        Opt("honey_syrup", 22.5),
        Opt("honey_ginger_syrup", 22.5, listOf("whiskey", "rum", "mezcal", "tequila", "brandy")),
        Opt("ginger_syrup", 22.5),
        Opt("maple_syrup", 15.0, listOf("american_whiskey", "scotch", "aged_rum", "dark_rum", "brandy")),
        Opt("agave_syrup", 15.0, listOf("tequila", "mezcal")),
        Opt("orgeat", 22.5, listOf("rum", "brandy", "american_whiskey", "pisco", "gin", "tequila")),
        Opt("cinnamon_syrup", 22.5, listOf("american_whiskey", "rum", "tequila", "mezcal", "brandy")),
        Opt("raspberry_syrup", 22.5, listOf("gin", "vodka", "pisco", "white_rum", "tequila")),
        Opt("grenadine", 22.5, listOf("brandy", "gin", "american_whiskey", "tequila", "rum")),
    )

    /** Liqueur modifiers: 15 ml, and the syrup drops by 7.5 ml to keep the balance. */
    private val sourModifiers = listOf(
        Opt("orange_liqueur", 15.0),
        Opt("maraschino", 15.0, listOf("gin", "rum", "tequila", "mezcal")),
        Opt("st_germain", 15.0, listOf("gin", "vodka", "tequila", "pisco")),
        Opt("amaretto", 15.0, listOf("american_whiskey", "brandy", "aged_rum", "dark_rum")),
        Opt("green_chartreuse", 15.0, listOf("gin", "mezcal", "tequila")),
        Opt("yellow_chartreuse", 15.0, listOf("gin", "mezcal", "tequila", "american_whiskey")),
        Opt("aperol", 15.0, listOf("gin", "tequila", "mezcal", "american_whiskey")),
        Opt("campari", 15.0, listOf("gin", "tequila", "mezcal")),
        Opt("benedictine", 15.0, listOf("whiskey", "brandy")),
    )

    enum class Accent(val label: String, val needsEgg: Boolean, val needs: String?, val allowed: List<String>?) {
        BITTERS_ON_FOAM("טיפות אנגוסטורה על הקצף", true, "angostura", null),
        ISLAY_FLOAT("ציפה של סקוטש מעושן", false, "islay_scotch", listOf("american_whiskey", "blended_scotch", "irish_whiskey", "japanese_whisky")),
        WINE_FLOAT("ציפה של יין אדום (בסגנון ניו יורק)", false, "red_wine", listOf("american_whiskey")),
        ABSINTHE_RINSE("שטיפת אבסינת", false, "absinthe", listOf("gin", "american_whiskey", "brandy")),
        MINT("נענע בניעור (בסגנון סמאש)", false, "mint", listOf("whiskey", "gin", "rum", "vodka", "cachaca")),
        SALT("קורט מלח (מחדד את ההדרים)", false, null, listOf("tequila", "mezcal", "gin", "vodka")),
    }

    fun sourSweetenersFor(base: String) = sourSweeteners.filter { fits(base, it.allowed) }
    fun sourModifiersFor(base: String) = sourModifiers.filter { fits(base, it.allowed) }
    fun accentsFor(base: String, egg: Boolean) =
        Accent.entries.filter { fits(base, it.allowed) && (!it.needsEgg || egg) && it.needs != base }

    data class SourSpec(
        val base: String,
        val citrus: String,
        val sweetener: String,
        val modifier: String? = null,
        val egg: Boolean = true,
        val accent: Accent? = null,
    )

    fun buildSour(s: SourSpec): Cocktail {
        val sweet = sourSweeteners.find { it.key == s.sweetener } ?: sourSweeteners[0]
        val syrup = if (s.modifier != null) sweet.amount - 7.5 else sweet.amount
        val parts = mutableListOf<Part>()
        if (s.accent == Accent.ABSINTHE_RINSE) parts += Part("absinthe", 0.0, Measure.RINSE, Stage.RINSE)
        parts += Part(s.base, 60.0)
        parts += Part(s.citrus, if (s.egg) 25.0 else 22.5)
        parts += Part(sweet.key, syrup)
        if (s.modifier != null) parts += Part(s.modifier, 15.0)
        if (s.egg) parts += Part("egg", 1.0, Measure.EGG_WHITE)
        when (s.accent) {
            Accent.MINT -> parts += Part("mint", 8.0, Measure.LEAF)
            Accent.SALT -> parts += Part("salt", 1.0, Measure.PINCH)
            Accent.ISLAY_FLOAT -> parts += Part("islay_scotch", 7.5, stage = Stage.FLOAT)
            Accent.WINE_FLOAT -> parts += Part("red_wine", 15.0, stage = Stage.FLOAT)
            else -> {}
        }
        val onRocks = listOf("american_whiskey", "scotch", "irish_whiskey", "aged_rum", "dark_rum", "brandy").any { Ingredients.isA(s.base, it) }
        val garnish = when {
            s.accent == Accent.BITTERS_ON_FOAM -> "3 טיפות אנגוסטורה על הקצף"
            s.accent == Accent.MINT -> "ענף נענע"
            s.citrus == "lime" -> "גלגל ליים"
            else -> "קליפת לימון"
        }
        val name = buildString {
            append("סאוור ${short(s.base)}")
            append(" · ${short(sweet.key)}")
            s.modifier?.let { append(" · ${short(it)}") }
        }
        return Cocktail(
            key = "built_sour", name = name,
            desc = "נבנה בתבנית סאוור: 60 בסיס, ${if (s.egg) "25" else "22.5"} חמוץ, ${Logic.mlText(syrup, false)} סירופ" +
                (if (s.modifier != null) ", 15 ליקר" else "") + (if (s.egg) ", חלבון לקצף" else "") + ".",
            family = Family.SOUR,
            method = if (s.egg) Method.DRY_SHAKE else Method.SHAKE,
            glass = if (onRocks) Glass.ROCKS else Glass.COUPE,
            ice = if (onRocks) Ice.LARGE_CUBE else Ice.NONE,
            parts = parts,
            garnish = garnish,
            tags = buildSet {
                add(Tag.SOUR)
                if (s.egg) add(Tag.FOAMY)
                if (s.base == "mezcal" || s.base == "islay_scotch" || s.accent == Accent.ISLAY_FLOAT) add(Tag.SMOKY)
                if (s.accent == Accent.MINT) add(Tag.REFRESHING)
                if (s.modifier == "campari" || s.modifier == "aperol") add(Tag.BITTER)
                if (s.modifier in listOf("green_chartreuse", "yellow_chartreuse", "benedictine")) add(Tag.HERBAL)
                if (s.sweetener in listOf("raspberry_syrup", "grenadine")) add(Tag.FRUITY)
            },
        )
    }

    // ================= OLD FASHIONED =================
    val ofBases = listOf(
        "bourbon", "rye", "blended_scotch", "islay_scotch", "irish_whiskey", "japanese_whisky",
        "aged_rum", "dark_rum", "tequila_reposado", "mezcal", "cognac", "calvados",
    )

    private val ofSweeteners = listOf(
        Opt("demerara_syrup", 7.5),
        Opt("simple_syrup", 10.0),
        Opt("maple_syrup", 5.0, listOf("american_whiskey", "scotch", "rum", "brandy")),
        Opt("honey_syrup", 10.0),
        Opt("agave_syrup", 5.0, listOf("tequila", "mezcal")),
        Opt("cinnamon_syrup", 7.5, listOf("american_whiskey", "rum", "tequila", "mezcal", "brandy")),
    )
    private val ofBitters = listOf(
        Opt("angostura", 2.0),
        Opt("orange_bitters", 2.0),
        Opt("peychauds", 3.0, listOf("american_whiskey", "brandy")),
    )

    fun ofSweetenersFor(base: String) = ofSweeteners.filter { fits(base, it.allowed) }
    fun ofBittersFor(base: String) = ofBitters.filter { fits(base, it.allowed) }

    data class OfSpec(val base: String, val sweetener: String, val bitters: String, val addOrange: Boolean = false)

    fun buildOldFashioned(s: OfSpec): Cocktail {
        val sweet = ofSweeteners.find { it.key == s.sweetener } ?: ofSweeteners[0]
        val bit = ofBitters.find { it.key == s.bitters } ?: ofBitters[0]
        val parts = mutableListOf(Part(s.base, 60.0), Part(sweet.key, sweet.amount), Part(bit.key, bit.amount, Measure.DASH))
        if (s.addOrange && bit.key != "orange_bitters") parts += Part("orange_bitters", 1.0, Measure.DASH)
        val peel = when {
            listOf("tequila", "mezcal").any { Ingredients.isA(s.base, it) } -> "קליפת אשכולית"
            s.base == "rye" || s.base == "cognac" -> "קליפת לימון"
            else -> "קליפת תפוז"
        }
        return Cocktail(
            key = "built_of", name = "אולד פאשנד ${short(s.base)} · ${short(sweet.key)}",
            desc = "נבנה בתבנית אולד פאשנד: 60 בסיס, מעט סירופ עשיר וביטרס, בערבוב ארוך על קרח.",
            family = Family.OLD_FASHIONED, method = Method.STIR, glass = Glass.ROCKS, ice = Ice.LARGE_CUBE,
            parts = parts, garnish = "$peel – לסחוט את השמנים מעל הכוס",
            tags = buildSet {
                add(Tag.SPIRIT_FORWARD)
                if (s.base == "mezcal" || s.base == "islay_scotch") add(Tag.SMOKY)
            },
        )
    }

    // ================= HIGHBALL =================
    data class Pairing(val base: String, val mixer: String, val citrus: String? = null, val salt: Boolean = false, val garnish: String)

    private val pairings = listOf(
        Pairing("gin", "tonic", garnish = "פלח ליים או קליפת לימון"),
        Pairing("gin", "soda", "lemon", garnish = "קליפת לימון"),
        Pairing("gin", "ginger_ale", "lime", garnish = "פלח ליים"),
        Pairing("vodka", "tonic", garnish = "פלח ליים"),
        Pairing("vodka", "ginger_beer", "lime", garnish = "פלח ליים"),
        Pairing("vodka", "soda", "lime", garnish = "פלח ליים"),
        Pairing("whiskey", "soda", garnish = "קליפת לימון"),
        Pairing("whiskey", "ginger_ale", garnish = "פלח לימון"),
        Pairing("american_whiskey", "ginger_beer", "lime", garnish = "פלח ליים וענף נענע"),
        Pairing("rum", "cola", "lime", garnish = "פלח ליים"),
        Pairing("rum", "ginger_beer", "lime", garnish = "פלח ליים"),
        Pairing("white_rum", "soda", "lime", garnish = "פלח ליים"),
        Pairing("tequila", "grapefruit_soda", "lime", salt = true, garnish = "פלח אשכולית"),
        Pairing("tequila", "soda", "lime", salt = true, garnish = "פלח ליים"),
        Pairing("mezcal", "grapefruit_soda", "lime", salt = true, garnish = "פלח אשכולית"),
        Pairing("mezcal", "ginger_beer", "lime", garnish = "פלח ליים"),
        Pairing("brandy", "ginger_ale", "lemon", garnish = "קליפת לימון"),
        Pairing("campari", "soda", garnish = "פלח תפוז"),
    )

    val hbBases = listOf(
        "gin", "vodka", "bourbon", "rye", "blended_scotch", "irish_whiskey", "japanese_whisky",
        "white_rum", "aged_rum", "dark_rum", "tequila_blanco", "tequila_reposado", "mezcal", "cognac", "calvados", "campari",
    )

    fun mixersFor(base: String) = pairings.filter { Ingredients.isA(base, it.base) }

    fun buildHighball(base: String, p: Pairing): Cocktail {
        val parts = mutableListOf(Part(base, 50.0))
        p.citrus?.let { parts += Part(it, 15.0) }
        if (p.salt) parts += Part("salt", 1.0, Measure.PINCH)
        parts += Part(p.mixer, if (p.citrus != null) 135.0 else 150.0, stage = Stage.TOP)
        return Cocktail(
            key = "built_hb", name = "${short(base)} ו${short(p.mixer)}",
            desc = "הייבול: אלכוהול ומיקסר קר מאוד, בכוס גבוהה מלאה קרח.",
            family = Family.HIGHBALL, method = Method.BUILD, glass = Glass.HIGHBALL, ice = Ice.CUBES,
            parts = parts, garnish = p.garnish, tags = setOf(Tag.REFRESHING, Tag.BUBBLY),
            extraSteps = listOf("כמה שיותר קרח, מיקסר מבקבוק פתוח טרי – זה כל הסוד"),
        )
    }

    /** Built-in classics close to a built drink: same family, same base family and same sweetener/mixer. */
    fun similar(built: Cocktail, catalog: List<Cocktail>): List<Cocktail> {
        val base = built.parts.firstOrNull { Ingredients[it.key]?.cat == IngCat.SPIRIT || it.stage == Stage.IN }?.key ?: return emptyList()
        val others = built.parts.drop(1).map { it.key }.toSet()
        return catalog.filter { c ->
            c.family == built.family &&
                c.parts.any { it.stage == Stage.IN && (Ingredients.isA(base, it.key)) } &&
                c.parts.any { it.key in others && Ingredients[it.key]?.cat in listOf(IngCat.SWEETENER, IngCat.MIXER, IngCat.LIQUEUR) }
        }.take(3)
    }

    /** Short name for generated titles (drops the parenthesis part). */
    private fun short(key: String) = Ingredients.name(key).substringBefore(" (").trim()
}
