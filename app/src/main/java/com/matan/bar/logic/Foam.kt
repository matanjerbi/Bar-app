package com.matan.bar.logic

import kotlin.math.ceil

/** Foaming agents: egg white and its vegan replacements. */
object Foam {
    const val EGG = "egg"
    const val AQUAFABA = "aquafaba"
    const val FOAMER = "vegan_foamer"

    /** Vegan replacements for egg white, in order of preference. */
    val vegan = listOf(AQUAFABA, FOAMER)

    /** Animal products that stay in the drink after swapping the egg. */
    private val animal = listOf("cream", "honey", "honey_syrup", "honey_ginger_syrup")

    fun hasEgg(c: Cocktail) = c.parts.any { it.key == EGG }

    /** The vegan foamer you have in the bar, or null. */
    fun veganInBar(bar: Bar): String? = vegan.firstOrNull { bar.has(it) }

    /** Egg white -> 30 ml aquafaba per white, or ~3 dashes of a vegan foamer per white. */
    fun replace(p: Part, key: String): Part = when (key) {
        FOAMER -> p.copy(
            key = FOAMER, amount = ceil(p.amount * 3).coerceAtLeast(2.0), unit = Measure.DASH, alts = emptyList(),
            note = "במקום חלבון · כמה דאשים מספיקים לקצף מלא",
        )
        else -> p.copy(
            key = AQUAFABA, amount = p.amount * 30.0, unit = Measure.ML, alts = emptyList(),
            note = "במקום חלבון · הנוזל מקופסת חומוס, נשמר במקרר כ-3 ימים",
        )
    }

    fun veganize(c: Cocktail, key: String): Cocktail =
        c.copy(parts = c.parts.map { if (it.key == EGG) replace(it, key) else it })

    /** Names of non-vegan parts left in the drink (cream, honey...). */
    fun stillAnimal(c: Cocktail): List<String> =
        c.parts.filter { it.key in animal }.map { Ingredients.name(it.key) }.distinct()
}
