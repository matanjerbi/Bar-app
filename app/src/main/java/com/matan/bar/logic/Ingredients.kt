package com.matan.bar.logic

import com.matan.bar.logic.IngCat.BITTERS
import com.matan.bar.logic.IngCat.FRESH
import com.matan.bar.logic.IngCat.LIQUEUR
import com.matan.bar.logic.IngCat.MIXER
import com.matan.bar.logic.IngCat.SPIRIT
import com.matan.bar.logic.IngCat.STAPLE
import com.matan.bar.logic.IngCat.SWEETENER
import com.matan.bar.logic.IngCat.WINE

object Ingredients {
    private fun i(key: String, name: String, cat: IngCat, abv: Double = 0.0, parent: String? = null) =
        Ingredient(key, name, cat, parent, abv)

    private fun g(key: String, name: String, parent: String? = null) =
        Ingredient(key, name, SPIRIT, parent, 0.40, group = true)

    private fun citrus(key: String, name: String) = Ingredient(key, name, FRESH, juice = true)

    private fun prep(key: String, name: String, shelfDays: Int, needs: List<String>, recipe: String) =
        Ingredient(key, name, SWEETENER, prep = Prep(needs, recipe, shelfDays))

    val all: List<Ingredient> = listOf(
        // ---- spirits ----
        i("gin", "ג'ין", SPIRIT, 0.42),
        i("vodka", "וודקה", SPIRIT, 0.40),
        g("whiskey", "וויסקי"),
        g("american_whiskey", "וויסקי אמריקאי (בורבון / ריי)", "whiskey"),
        i("bourbon", "בורבון", SPIRIT, 0.45, "american_whiskey"),
        i("rye", "ריי וויסקי", SPIRIT, 0.45, "american_whiskey"),
        g("scotch", "סקוטש", "whiskey"),
        i("blended_scotch", "סקוטש בלנדד", SPIRIT, 0.40, "scotch"),
        i("islay_scotch", "סקוטש מעושן (Islay)", SPIRIT, 0.43, "scotch"),
        i("irish_whiskey", "וויסקי אירי", SPIRIT, 0.40, "whiskey"),
        i("japanese_whisky", "וויסקי יפני", SPIRIT, 0.43, "whiskey"),
        g("rum", "רום"),
        i("white_rum", "רום לבן", SPIRIT, 0.40, "rum"),
        i("aged_rum", "רום מיושן", SPIRIT, 0.40, "rum"),
        i("dark_rum", "רום כהה", SPIRIT, 0.40, "rum"),
        i("cachaca", "קשאסה", SPIRIT, 0.40),
        g("tequila", "טקילה"),
        i("tequila_blanco", "טקילה בלאנקו", SPIRIT, 0.40, "tequila"),
        i("tequila_reposado", "טקילה רפוסדו", SPIRIT, 0.40, "tequila"),
        i("mezcal", "מזקל", SPIRIT, 0.42),
        g("brandy", "ברנדי"),
        i("cognac", "קוניאק", SPIRIT, 0.40, "brandy"),
        i("calvados", "קלבדוס (ברנדי תפוחים)", SPIRIT, 0.40, "brandy"),
        i("pisco", "פיסקו", SPIRIT, 0.40),

        // ---- liqueurs ----
        i("orange_liqueur", "ליקר תפוזים (קואנטרו / טריפל סק)", LIQUEUR, 0.38),
        i("maraschino", "ליקר מרסקינו", LIQUEUR, 0.32),
        i("amaretto", "אמרטו", LIQUEUR, 0.28),
        i("campari", "קמפרי", LIQUEUR, 0.25),
        i("aperol", "אפרול", LIQUEUR, 0.11),
        i("green_chartreuse", "שרטרז ירוק", LIQUEUR, 0.55),
        i("yellow_chartreuse", "שרטרז צהוב", LIQUEUR, 0.40),
        i("benedictine", "בנדיקטין", LIQUEUR, 0.40),
        i("st_germain", "ליקר סמבוק (St-Germain)", LIQUEUR, 0.20),
        i("coffee_liqueur", "ליקר קפה", LIQUEUR, 0.20),
        i("creme_de_violette", "ליקר סיגליות", LIQUEUR, 0.20),
        i("creme_de_cassis", "קרם דה קסיס", LIQUEUR, 0.15),
        i("cherry_liqueur", "ליקר דובדבנים (Cherry Heering)", LIQUEUR, 0.24),
        i("drambuie", "דרמבוי", LIQUEUR, 0.40),
        i("absinthe", "אבסינת", LIQUEUR, 0.60),
        Ingredient("amaro", "אמרו", LIQUEUR, abv = 0.30, group = true),
        i("amaro_nonino", "אמרו נונינו", LIQUEUR, 0.35, "amaro"),
        i("averna", "אמרו אוורנה", LIQUEUR, 0.29, "amaro"),
        i("fernet", "פרנה בראנקה", LIQUEUR, 0.39, "amaro"),

        // ---- vermouth & wine ----
        i("sweet_vermouth", "ורמוט אדום (מתוק)", WINE, 0.16),
        i("dry_vermouth", "ורמוט יבש", WINE, 0.18),
        i("lillet_blanc", "ליֶה בלאן", WINE, 0.17),
        i("sparkling_wine", "פרוסקו / קאווה / שמפניה", WINE, 0.11),
        i("red_wine", "יין אדום", WINE, 0.13),

        // ---- bitters ----
        i("angostura", "אנגוסטורה ביטרס", BITTERS, 0.45),
        i("orange_bitters", "אורנג' ביטרס", BITTERS, 0.28),
        i("peychauds", "פישו ביטרס (Peychaud's)", BITTERS, 0.35),

        // ---- fresh ----
        citrus("lemon", "לימון"),
        citrus("lime", "ליים"),
        citrus("orange", "תפוז"),
        citrus("grapefruit", "אשכולית"),
        i("mint", "נענע", FRESH),
        i("basil", "בזיליקום", FRESH),
        i("egg", "ביצים (חלבון)", FRESH),
        i("cream", "שמנת מתוקה", FRESH),
        i("ginger_root", "ג'ינג'ר טרי", FRESH),
        i("raspberries", "פטל", FRESH),
        i("pineapple_juice", "מיץ אננס", FRESH),
        i("cranberry_juice", "מיץ חמוציות", FRESH),
        i("pomegranate_juice", "מיץ רימונים", FRESH),
        i("coconut_cream", "קרם קוקוס", FRESH),
        i("coffee", "קפה / אספרסו", FRESH),
        i("orange_flower_water", "מי זהר", FRESH),
        i("cinnamon", "מקלות קינמון", FRESH),

        // ---- sweeteners ----
        i("honey", "דבש", SWEETENER),
        i("demerara_sugar", "סוכר דמררה / חום", SWEETENER),
        i("maple_syrup", "סירופ מייפל", SWEETENER),
        i("agave_syrup", "סירופ אגבה", SWEETENER),
        i("orgeat", "אורז'ה (סירופ שקדים)", SWEETENER),
        prep("simple_syrup", "סירופ סוכר (1:1)", 30, listOf("sugar"),
            "סוכר ומים ביחס 1:1 במשקל. לערבב עד שהסוכר נמס (אפשר לחמם קלות), לקרר ולהעביר לבקבוק. במקרר עד חודש."),
        prep("demerara_syrup", "סירופ דמררה (2:1)", 60, listOf("demerara_sugar"),
            "2 חלקי סוכר דמררה או חום לחלק אחד מים, במשקל. לחמם תוך ערבוב עד שהסוכר נמס, לקרר. עשיר וקרמלי – מצוין לאולד פאשנד. במקרר עד חודשיים."),
        prep("honey_syrup", "סירופ דבש (1:1)", 14, listOf("honey"),
            "דבש ומים חמים ביחס 1:1. לערבב עד שהדבש נמס לגמרי, לקרר. דבש נקי לא מתערבב טוב עם קרח – לכן מדללים. במקרר עד שבועיים."),
        prep("honey_ginger_syrup", "סירופ דבש-ג'ינג'ר", 14, listOf("honey", "ginger_root"),
            "200 גרם דבש, 100 מ\"ל מים ו-60 גרם ג'ינג'ר טרי קלוף ופרוס דק. להביא לרתיחה עדינה, לבשל 5 דקות על אש קטנה, להשרות חצי שעה ולסנן. זה הסירופ של הפניצילין. במקרר עד שבועיים."),
        prep("ginger_syrup", "סירופ ג'ינג'ר", 14, listOf("ginger_root", "sugar"),
            "לסחוט מיץ ג'ינג'ר טרי (מסחטה, או פומפייה וסחיטה בבד). לערבב עם סוכר ביחס 1:1 במשקל עד שהסוכר נמס, בלי לחמם – ככה נשמרת החריפות. במקרר עד שבועיים."),
        prep("grenadine", "גרנדין ביתי", 21, listOf("pomegranate_juice", "sugar"),
            "מיץ רימונים 100% וסוכר ביחס 1:1 במשקל. לחמם בעדינות בלי להרתיח עד שהסוכר נמס. אפשר להוסיף כמה טיפות מי זהר. במקרר עד 3 שבועות."),
        prep("cinnamon_syrup", "סירופ קינמון", 30, listOf("cinnamon", "sugar"),
            "250 גרם סוכר, 250 מ\"ל מים ו-3 מקלות קינמון שבורים. להביא לרתיחה, לבשל 5 דקות על אש קטנה, להשרות לילה ולסנן. במקרר עד חודש."),
        prep("raspberry_syrup", "סירופ פטל", 14, listOf("raspberries", "sugar"),
            "250 גרם פטל (גם קפוא), 250 גרם סוכר ו-250 מ\"ל מים. לבשל 5 דקות, למעוך, ולסנן היטב דרך מסננת דקה. במקרר עד שבועיים."),

        // ---- mixers ----
        i("soda", "סודה", MIXER),
        i("tonic", "טוניק", MIXER),
        i("ginger_beer", "ג'ינג'ר ביר", MIXER),
        i("ginger_ale", "ג'ינג'ר אייל", MIXER),
        i("cola", "קולה", MIXER),
        i("grapefruit_soda", "סודה אשכוליות", MIXER),

        // ---- always available ----
        i("sugar", "סוכר", STAPLE),
        i("salt", "מלח", STAPLE),
        i("water", "מים", STAPLE),
    )

    private val byKey = all.associateBy { it.key }

    operator fun get(key: String): Ingredient? = byKey[key]
    fun name(key: String) = byKey[key]?.name ?: key

    /** true if [key] is [ancestor] or one of its descendants (bourbon isA american_whiskey isA whiskey). */
    fun isA(key: String, ancestor: String): Boolean {
        var k: String? = key
        while (k != null) {
            if (k == ancestor) return true
            k = byKey[k]?.parent
        }
        return false
    }

    /** Ingredients you can tick in "my bar" (no category nodes, no staples). */
    val stockable = all.filter { !it.group && it.cat != STAPLE }
}
