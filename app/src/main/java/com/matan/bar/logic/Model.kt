package com.matan.bar.logic

enum class IngCat { SPIRIT, LIQUEUR, WINE, BITTERS, FRESH, SWEETENER, MIXER, STAPLE }

/** Homemade preparation: what it needs, how to make it, how long it keeps (fridge). */
data class Prep(val needs: List<String>, val recipe: String, val shelfDays: Int)

data class Ingredient(
    val key: String,
    val name: String,
    val cat: IngCat,
    /** e.g. bourbon -> american_whiskey -> whiskey. A bottle satisfies any of its ancestors. */
    val parent: String? = null,
    val abv: Double = 0.0,
    /** a category node (e.g. "whiskey") - used in recipes, not checked in the bar */
    val group: Boolean = false,
    /** citrus: an ML amount means fresh juice */
    val juice: Boolean = false,
    val prep: Prep? = null,
)

enum class Measure { ML, DASH, DROP, BARSPOON, EGG_WHITE, LEAF, PIECE, PINCH, RINSE }

/** Where the part goes: into the shaker/glass, rinse first, top up after, or float on top. */
enum class Stage { IN, RINSE, TOP, FLOAT }

data class Part(
    val key: String,
    val amount: Double,
    val unit: Measure = Measure.ML,
    val stage: Stage = Stage.IN,
    val optional: Boolean = false,
    val alts: List<String> = emptyList(),
    val note: String = "",
)

enum class Method { SHAKE, DRY_SHAKE, STIR, BUILD, MUDDLE, SWIZZLE, HOT }
enum class Glass { COUPE, NICK_NORA, ROCKS, HIGHBALL, FLUTE, WINE, MUG }
enum class Ice { NONE, LARGE_CUBE, CUBES, CRUSHED }
enum class Family { SOUR, EQUAL_PARTS, OLD_FASHIONED, MARTINI_MANHATTAN, NEGRONI, HIGHBALL, SPRITZ, TIKI, CREAMY }
enum class Tag { SOUR, FOAMY, BITTER, SWEET, REFRESHING, SPIRIT_FORWARD, SMOKY, HERBAL, FRUITY, CREAMY, BUBBLY, CLASSIC, MODERN }

data class Cocktail(
    val key: String,
    val name: String,
    val desc: String,
    val family: Family,
    val method: Method,
    val glass: Glass,
    val ice: Ice,
    val parts: List<Part>,
    val garnish: String = "",
    val tags: Set<Tag> = emptySet(),
    val difficulty: Int = 1,
    /** replaces the generated method steps (muddled / swizzled / special drinks) */
    val steps: List<String> = emptyList(),
    /** added after the generated method steps */
    val extraSteps: List<String> = emptyList(),
    /** id in the custom table, 0 for built-in */
    val customId: Long = 0,
)

// ---------- Hebrew labels ----------

fun IngCat.label() = when (this) {
    IngCat.SPIRIT -> "אלכוהול בסיס"
    IngCat.LIQUEUR -> "ליקרים ואמרו"
    IngCat.WINE -> "ורמוט ויין"
    IngCat.BITTERS -> "ביטרס"
    IngCat.FRESH -> "פירות, עשבים וטריים"
    IngCat.SWEETENER -> "סירופים וממתיקים"
    IngCat.MIXER -> "מיקסרים"
    IngCat.STAPLE -> "בסיסי"
}

fun Method.label() = when (this) {
    Method.SHAKE -> "ניעור"
    Method.DRY_SHAKE -> "ניעור יבש + ניעור (קצף)"
    Method.STIR -> "ערבוב"
    Method.BUILD -> "בנייה בכוס"
    Method.MUDDLE -> "כתישה"
    Method.SWIZZLE -> "סוויזל"
    Method.HOT -> "משקה חם"
}

fun Glass.label() = when (this) {
    Glass.COUPE -> "קופה"
    Glass.NICK_NORA -> "ניק אנד נורה"
    Glass.ROCKS -> "רוקס"
    Glass.HIGHBALL -> "הייבול"
    Glass.FLUTE -> "פלוט"
    Glass.WINE -> "כוס יין"
    Glass.MUG -> "ספל"
}

fun Ice.label() = when (this) {
    Ice.NONE -> "בלי קרח (Up)"
    Ice.LARGE_CUBE -> "קוביית קרח גדולה"
    Ice.CUBES -> "קוביות קרח"
    Ice.CRUSHED -> "קרח כתוש"
}

fun Family.label() = when (this) {
    Family.SOUR -> "סאוורים"
    Family.EQUAL_PARTS -> "חלקים שווים"
    Family.OLD_FASHIONED -> "אולד פאשנד"
    Family.MARTINI_MANHATTAN -> "מרטיני ומנהטן"
    Family.NEGRONI -> "נגרוני וקרובים"
    Family.HIGHBALL -> "הייבולים"
    Family.SPRITZ -> "ספריץ ובועות"
    Family.TIKI -> "טיקי וטרופי"
    Family.CREAMY -> "קרמיים וקפה"
}

fun Tag.label() = when (this) {
    Tag.SOUR -> "חמוץ"
    Tag.FOAMY -> "קצף"
    Tag.BITTER -> "מריר"
    Tag.SWEET -> "מתוק"
    Tag.REFRESHING -> "מרענן"
    Tag.SPIRIT_FORWARD -> "חזק"
    Tag.SMOKY -> "מעושן"
    Tag.HERBAL -> "צמחי"
    Tag.FRUITY -> "פירותי"
    Tag.CREAMY -> "קרמי"
    Tag.BUBBLY -> "מבעבע"
    Tag.CLASSIC -> "קלאסיקה"
    Tag.MODERN -> "מודרני"
}
