package com.matan.bar.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LogicTest {
    @Test fun catalog_everyKeyExists() {
        val keys = Ingredients.all.map { it.key }
        assertEquals("duplicate ingredient keys", keys.size, keys.toSet().size)
        Ingredients.all.forEach { i ->
            i.parent?.let { assertTrue("${i.key} parent $it", Ingredients[it] != null) }
            i.prep?.needs?.forEach { assertTrue("${i.key} needs $it", Ingredients[it] != null) }
        }
        val ck = Cocktails.all.map { it.key }
        assertEquals("duplicate cocktail keys", ck.size, ck.toSet().size)
        Cocktails.all.forEach { c ->
            c.parts.forEach { p ->
                assertTrue("${c.key}: ${p.key}", Ingredients[p.key] != null)
                p.alts.forEach { assertTrue("${c.key}: alt $it", Ingredients[it] != null) }
            }
            assertTrue("${c.key} has parts", c.parts.isNotEmpty())
            assertTrue("${c.key} has tags", c.tags.isNotEmpty())
        }
    }

    @Test fun hierarchy_bottleSatisfiesGroup() {
        val bar = Bar(setOf("bourbon", "lemon", "simple_syrup"))
        assertTrue(bar.has("american_whiskey"))
        assertTrue(bar.has("whiskey"))
        assertFalse(bar.has("rye"))
        assertTrue(bar.has("sugar")) // staple
        // whiskey sour: egg is optional
        assertTrue(bar.missing(Cocktails["whiskey_sour"]!!).isEmpty())
    }

    @Test fun alternatives_count() {
        val bar = Bar(setOf("bourbon", "simple_syrup", "angostura"))
        // old fashioned asks demerara syrup, simple syrup is an accepted alternative
        assertTrue(bar.missing(Cocktails["old_fashioned"]!!).isEmpty())
    }

    @Test fun suggestions_rankByUnlocks() {
        val bar = Bar(setOf("gin", "sweet_vermouth", "american_whiskey".let { "bourbon" }, "mezcal"))
        val s = Logic.suggestions(Cocktails.all, bar)
        assertEquals("campari", s.first().key) // negroni, boulevardier, mezcal negroni
        assertTrue(s.first().unlocks.size >= 3)
    }

    @Test fun strength_daiquiriAroundFifteenPercent() {
        val st = Logic.strength(Cocktails["daiquiri"]!!)
        assertTrue("abv ${st.abv}", st.abv in 0.13..0.18)
        assertTrue(st.standardDrinks in 1.5..2.2)
    }

    @Test fun amounts_formatting() {
        val p = Part("lemon", 22.5)
        assertEquals("22.5 מ\"ל", Logic.amountText(p, 1, false))
        assertEquals("¾ oz", Logic.amountText(p, 1, true))
        assertEquals("45 מ\"ל", Logic.amountText(p, 2, false))
        assertEquals("2 oz", Logic.amountText(Part("gin", 60.0), 1, true))
    }

    @Test fun builder_sourBalanced() {
        val c = Builder.buildSour(Builder.SourSpec("mezcal", "lime", "agave_syrup", modifier = "orange_liqueur", egg = true))
        assertEquals(Method.DRY_SHAKE, c.method)
        assertEquals(7.5, c.parts.first { it.key == "agave_syrup" }.amount, 0.001) // 15 - 7.5 for the liqueur
        assertTrue(Tag.SMOKY in c.tags)
        // honey-ginger is not offered for vodka; agave only for agave spirits
        assertFalse(Builder.sourSweetenersFor("vodka").any { it.key == "honey_ginger_syrup" })
        assertFalse(Builder.sourSweetenersFor("gin").any { it.key == "agave_syrup" })
    }

    @Test fun builder_similarFindsClassic() {
        val c = Builder.buildSour(Builder.SourSpec("bourbon", "lemon", "honey_syrup", egg = false))
        assertTrue(Builder.similar(c, Cocktails.all).any { it.key == "gold_rush" })
    }

    @Test fun steps_generated() {
        val s = Logic.steps(Cocktails["pisco_sour"]!!, 1, false)
        assertTrue(s.any { it.contains("ניעור יבש") })
        assertTrue(s.last().startsWith("קישוט"))
    }

    @Test fun pairs_unlockTogether() {
        val bar = Bar(setOf("gin"))
        val pairs = Logic.pairSuggestions(Cocktails.all, bar, limit = 50)
        val negroni = pairs.find { setOf(it.a, it.b) == setOf("campari", "sweet_vermouth") }
        assertTrue(negroni != null && negroni.unlocks.any { it.key == "negroni" })
    }
}
