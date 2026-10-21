package com.matan.bar.logic

import com.matan.bar.logic.Family.CREAMY
import com.matan.bar.logic.Family.EQUAL_PARTS
import com.matan.bar.logic.Family.HIGHBALL
import com.matan.bar.logic.Family.MARTINI_MANHATTAN
import com.matan.bar.logic.Family.NEGRONI
import com.matan.bar.logic.Family.OLD_FASHIONED
import com.matan.bar.logic.Family.SOUR
import com.matan.bar.logic.Family.SPRITZ
import com.matan.bar.logic.Family.TIKI
import com.matan.bar.logic.Glass.COUPE
import com.matan.bar.logic.Glass.FLUTE
import com.matan.bar.logic.Glass.MUG
import com.matan.bar.logic.Glass.NICK_NORA
import com.matan.bar.logic.Glass.ROCKS
import com.matan.bar.logic.Glass.WINE
import com.matan.bar.logic.Ice.CRUSHED
import com.matan.bar.logic.Ice.CUBES
import com.matan.bar.logic.Ice.LARGE_CUBE
import com.matan.bar.logic.Ice.NONE
import com.matan.bar.logic.Method.BUILD
import com.matan.bar.logic.Method.DRY_SHAKE
import com.matan.bar.logic.Method.HOT
import com.matan.bar.logic.Method.MUDDLE
import com.matan.bar.logic.Method.SHAKE
import com.matan.bar.logic.Method.STIR
import com.matan.bar.logic.Method.SWIZZLE
import com.matan.bar.logic.Tag.BITTER
import com.matan.bar.logic.Tag.BUBBLY
import com.matan.bar.logic.Tag.CLASSIC
import com.matan.bar.logic.Tag.FOAMY
import com.matan.bar.logic.Tag.FRUITY
import com.matan.bar.logic.Tag.HERBAL
import com.matan.bar.logic.Tag.MODERN
import com.matan.bar.logic.Tag.REFRESHING
import com.matan.bar.logic.Tag.SMOKY
import com.matan.bar.logic.Tag.SPIRIT_FORWARD
import com.matan.bar.logic.Tag.SWEET
import com.matan.bar.logic.Glass.HIGHBALL as HB

// part helpers
private fun ml(key: String, amount: Double, optional: Boolean = false, alts: List<String> = emptyList(), note: String = "") =
    Part(key, amount, Measure.ML, Stage.IN, optional, alts, note)
private fun dash(key: String, n: Double, optional: Boolean = false) = Part(key, n, Measure.DASH, optional = optional)
private fun egg(amount: Double = 1.0, optional: Boolean = false) = Part("egg", amount, Measure.EGG_WHITE, optional = optional)
private fun leaves(key: String, n: Double) = Part(key, n, Measure.LEAF)
private fun top(key: String, amount: Double) = Part(key, amount, Measure.ML, Stage.TOP)
private fun float(key: String, amount: Double, note: String = "") = Part(key, amount, Measure.ML, Stage.FLOAT, note = note)
private fun rinse(key: String) = Part(key, 0.0, Measure.RINSE, Stage.RINSE)

private fun c(
    key: String, name: String, desc: String, family: Family, method: Method, glass: Glass, ice: Ice,
    parts: List<Part>, garnish: String, tags: Set<Tag>, difficulty: Int = 1,
    steps: List<String> = emptyList(), extra: List<String> = emptyList(),
) = Cocktail(key, name, desc, family, method, glass, ice, parts, garnish, tags, difficulty, steps, extra)

/**
 * Built-in recipes. Specs follow the commonly accepted modern versions of each drink
 * (ml, single serving). Sours use the 2 : ¾ : ¾ backbone unless the drink's own spec differs.
 */
object Cocktails {
    val all: List<Cocktail> = listOf(
        // ================= SOURS =================
        c("whiskey_sour", "Whiskey Sour", "הסאוור הקלאסי: חמוץ-מתוק מאוזן, עם קצף משיי מהחלבון.",
            SOUR, DRY_SHAKE, ROCKS, LARGE_CUBE,
            listOf(ml("american_whiskey", 60.0, note = "בורבון לרך, ריי לחריף"), ml("lemon", 30.0), ml("simple_syrup", 22.5), egg(1.0, optional = true)),
            "3 טיפות אנגוסטורה על הקצף, ואפשר גם פרוסת לימון", setOf(Tag.SOUR, FOAMY, CLASSIC)),
        c("new_york_sour", "New York Sour", "וויסקי סאוור עם שכבת יין אדום שצפה מעל. יפה ומורכב.",
            SOUR, DRY_SHAKE, ROCKS, CUBES,
            listOf(ml("american_whiskey", 60.0, note = "עדיף ריי"), ml("lemon", 30.0), ml("simple_syrup", 22.5), egg(1.0, optional = true),
                float("red_wine", 15.0, "יין יבש ופירותי, כמו מלבק")),
            "", setOf(Tag.SOUR, FOAMY, FRUITY, CLASSIC), 2),
        c("amaretto_sour", "Amaretto Sour", "הגרסה של ג'פרי מורגנטלר: הבורבון מייבש ומאזן את המתיקות של האמרטו.",
            SOUR, DRY_SHAKE, ROCKS, CUBES,
            listOf(ml("amaretto", 45.0), ml("american_whiskey", 22.5, note = "בורבון חזק, אם יש"), ml("lemon", 30.0), ml("simple_syrup", 7.5), egg(0.5)),
            "קליפת לימון ודובדבן", setOf(Tag.SOUR, FOAMY, SWEET, MODERN)),
        c("pisco_sour", "Pisco Sour", "הקלאסיקה הפרואנית: ליים, פיסקו וקצף עבה עם טיפות ביטרס.",
            SOUR, DRY_SHAKE, COUPE, NONE,
            listOf(ml("pisco", 60.0), ml("lime", 30.0), ml("simple_syrup", 22.5), egg(1.0)),
            "3 טיפות אנגוסטורה על הקצף", setOf(Tag.SOUR, FOAMY, CLASSIC)),
        c("clover_club", "Clover Club", "ג'ין, פטל וקצף. ורוד, פירותי ויבש יותר ממה שנראה.",
            SOUR, DRY_SHAKE, COUPE, NONE,
            listOf(ml("gin", 45.0), ml("lemon", 22.5), ml("raspberry_syrup", 22.5), ml("dry_vermouth", 15.0, optional = true), egg(1.0)),
            "3 פטלים על שיפוד", setOf(Tag.SOUR, FOAMY, FRUITY, CLASSIC)),
        c("white_lady", "White Lady", "ג'ין, ליקר תפוזים ולימון. אלגנטי, עם קצף.",
            SOUR, DRY_SHAKE, COUPE, NONE,
            listOf(ml("gin", 50.0), ml("orange_liqueur", 20.0), ml("lemon", 20.0), egg(1.0)),
            "קליפת לימון", setOf(Tag.SOUR, FOAMY, CLASSIC)),
        c("silver_fizz", "Gin Silver Fizz", "ג'ין סאוור עם קצף שמוארך בסודה. קליל ומבעבע.",
            SOUR, DRY_SHAKE, HB, NONE,
            listOf(ml("gin", 60.0), ml("lemon", 30.0), ml("simple_syrup", 22.5), egg(1.0), top("soda", 60.0)),
            "פרוסת לימון", setOf(Tag.SOUR, FOAMY, BUBBLY, REFRESHING, CLASSIC)),
        c("ramos_gin_fizz", "Ramos Gin Fizz", "האתגר של הבר: שמנת, חלבון, הדרים ומי זהר. קצף שעולה מעל הכוס.",
            SOUR, DRY_SHAKE, HB, NONE,
            listOf(ml("gin", 60.0), ml("lemon", 15.0), ml("lime", 15.0), ml("simple_syrup", 22.5), ml("cream", 30.0), egg(1.0),
                Part("orange_flower_water", 3.0, Measure.DROP), top("soda", 30.0)),
            "", setOf(FOAMY, Tag.CREAMY, BUBBLY, Tag.SOUR, CLASSIC), 3,
            steps = listOf(
                "להכניס לשייקר את כל הרכיבים חוץ מהסודה, בלי קרח",
                "נעירה יבשה ארוכה וחזקה – 30 שניות",
                "להוסיף 2–3 קוביות קרח ולנער עד שהן כמעט נמסות. ככל שמנערים יותר, הקצף גבוה יותר",
                "לסנן לכוס הייבול צרה, בלי קרח, ולתת לה לנוח דקה במקפיא",
                "למזוג את הסודה לאט, ישר לאמצע – הקצף יעלה מעל שפת הכוס",
            )),
        c("penicillin", "Penicillin", "סאם רוס, 2005: סקוטש, דבש-ג'ינג'ר ושכבה מעושנת מעל. קלאסיקה מודרנית.",
            SOUR, SHAKE, ROCKS, LARGE_CUBE,
            listOf(ml("scotch", 60.0, note = "בלנדד"), ml("lemon", 22.5), ml("honey_ginger_syrup", 22.5), float("islay_scotch", 7.5)),
            "ג'ינג'ר מסוכר או פרוסת ג'ינג'ר", setOf(Tag.SOUR, SMOKY, MODERN)),
        c("gold_rush", "Gold Rush", "וויסקי סאוור עם דבש במקום סוכר. פשוט ומעולה.",
            SOUR, SHAKE, ROCKS, LARGE_CUBE,
            listOf(ml("american_whiskey", 60.0, note = "בורבון"), ml("lemon", 22.5), ml("honey_syrup", 22.5)),
            "קליפת לימון", setOf(Tag.SOUR, MODERN)),
        c("bees_knees", "Bee's Knees", "ג'ין, לימון ודבש. מתקופת היובש.",
            SOUR, SHAKE, COUPE, NONE,
            listOf(ml("gin", 60.0), ml("lemon", 22.5), ml("honey_syrup", 22.5)),
            "קליפת לימון", setOf(Tag.SOUR, CLASSIC)),
        c("daiquiri", "Daiquiri", "רום, ליים וסוכר. המבחן של כל ברמן – הכול באיזון.",
            SOUR, SHAKE, COUPE, NONE,
            listOf(ml("white_rum", 60.0), ml("lime", 25.0), ml("simple_syrup", 20.0)),
            "גלגל ליים", setOf(Tag.SOUR, REFRESHING, CLASSIC)),
        c("hemingway_daiquiri", "Hemingway Daiquiri", "דאקירי יבש עם אשכולית ומרסקינו.",
            SOUR, SHAKE, COUPE, NONE,
            listOf(ml("white_rum", 60.0), ml("lime", 22.5), ml("grapefruit", 15.0), ml("maraschino", 7.5)),
            "גלגל ליים", setOf(Tag.SOUR, REFRESHING, CLASSIC)),
        c("margarita", "Margarita", "טקילה, ליקר תפוזים וליים, עם שוליים של מלח.",
            SOUR, SHAKE, ROCKS, CUBES,
            listOf(ml("tequila_blanco", 50.0), ml("orange_liqueur", 20.0), ml("lime", 25.0), ml("agave_syrup", 5.0, optional = true)),
            "שוליים של מלח על חצי מהכוס, וגלגל ליים", setOf(Tag.SOUR, REFRESHING, CLASSIC)),
        c("tommys_margarita", "Tommy's Margarita", "מרגריטה בלי ליקר: רק טקילה, ליים ואגבה. הטקילה בולטת.",
            SOUR, SHAKE, ROCKS, CUBES,
            listOf(ml("tequila", 60.0), ml("lime", 30.0), ml("agave_syrup", 15.0)),
            "גלגל ליים, ומלח אם רוצים", setOf(Tag.SOUR, REFRESHING, MODERN)),
        c("sidecar", "Sidecar", "קוניאק, ליקר תפוזים ולימון. קלאסיקה פריזאית.",
            SOUR, SHAKE, COUPE, NONE,
            listOf(ml("brandy", 50.0, note = "קוניאק"), ml("orange_liqueur", 25.0), ml("lemon", 20.0)),
            "שוליים של סוכר (לא חובה) וקליפת תפוז", setOf(Tag.SOUR, CLASSIC)),
        c("gimlet", "Gimlet", "ג'ין וליים, חד ונקי.",
            SOUR, SHAKE, COUPE, NONE,
            listOf(ml("gin", 60.0), ml("lime", 22.5), ml("simple_syrup", 22.5)),
            "גלגל ליים", setOf(Tag.SOUR, REFRESHING, CLASSIC)),
        c("southside", "Southside", "גימלט עם נענע. מרענן במיוחד.",
            SOUR, SHAKE, COUPE, NONE,
            listOf(ml("gin", 60.0), ml("lime", 22.5), ml("simple_syrup", 22.5), leaves("mint", 8.0)),
            "עלה נענע", setOf(Tag.SOUR, REFRESHING, HERBAL, CLASSIC),
            extra = listOf("מנערים יחד עם העלים בלי לכתוש. הסינון הכפול חשוב, כדי שלא יישארו שברי נענע")),
        c("gin_basil_smash", "Gin Basil Smash", "יורג מאייר, 2008: הרבה בזיליקום, לימון וג'ין. ירוק ורענן.",
            SOUR, MUDDLE, ROCKS, CUBES,
            listOf(ml("gin", 60.0), ml("lemon", 22.5), ml("simple_syrup", 15.0), leaves("basil", 12.0)),
            "ענף בזיליקום", setOf(REFRESHING, HERBAL, Tag.SOUR, MODERN),
            steps = listOf(
                "לכתוש בעדינות בשייקר את הבזיליקום עם מיץ הלימון – רק לשחרר את השמנים, לא לטחון",
                "להוסיף ג'ין, סירופ וקרח ולנער חזק 12 שניות",
                "סינון כפול לכוס רוקס על קוביות קרח טריות",
            )),
        c("whiskey_smash", "Whiskey Smash", "לימון כתוש, נענע ובורבון על קרח כתוש.",
            SOUR, MUDDLE, ROCKS, CRUSHED,
            listOf(ml("american_whiskey", 60.0), Part("lemon", 0.5, Measure.PIECE, note = "חצי לימון חתוך לפלחים"), ml("simple_syrup", 22.5), leaves("mint", 8.0)),
            "ענף נענע גדול", setOf(REFRESHING, Tag.SOUR, CLASSIC),
            steps = listOf(
                "לכתוש בשייקר את פלחי הלימון עם הסירופ – כדי להוציא מיץ ושמנים מהקליפה",
                "להוסיף את הנענע, הוויסקי וקרח, ולנער",
                "סינון כפול לכוס מלאה קרח כתוש",
            )),
        c("trinidad_sour", "Trinidad Sour", "ג'וזפה גונזלס: 45 מ\"ל אנגוסטורה – באמת. מתובל, מריר ומפתיע.",
            SOUR, SHAKE, COUPE, NONE,
            listOf(ml("angostura", 45.0), ml("orgeat", 30.0), ml("lemon", 22.5), ml("rye", 15.0, alts = listOf("american_whiskey"))),
            "", setOf(BITTER, Tag.SOUR, MODERN), 2),
        c("brown_derby", "Brown Derby", "בורבון, אשכולית ודבש.",
            SOUR, SHAKE, COUPE, NONE,
            listOf(ml("american_whiskey", 45.0), ml("grapefruit", 30.0), ml("honey_syrup", 15.0)),
            "קליפת אשכולית", setOf(Tag.SOUR, FRUITY, CLASSIC)),
        c("army_navy", "Army & Navy", "ג'ין סאוור עם אורז'ה. אגוזי ומשיי.",
            SOUR, SHAKE, COUPE, NONE,
            listOf(ml("gin", 60.0), ml("lemon", 22.5), ml("orgeat", 15.0)),
            "קליפת לימון", setOf(Tag.SOUR, CLASSIC)),
        c("aviation", "Aviation", "ג'ין, מרסקינו וליקר סיגליות. פרחוני, בצבע שמיים.",
            SOUR, SHAKE, COUPE, NONE,
            listOf(ml("gin", 60.0), ml("lemon", 22.5), ml("maraschino", 15.0), ml("creme_de_violette", 7.5)),
            "דובדבן", setOf(Tag.SOUR, FRUITY, CLASSIC), 2),
        c("division_bell", "Division Bell", "פיל וורד: מזקל, אפרול ומרסקינו. מעושן ומריר-מתוק.",
            SOUR, SHAKE, COUPE, NONE,
            listOf(ml("mezcal", 30.0), ml("aperol", 22.5), ml("maraschino", 15.0), ml("lime", 22.5)),
            "קליפת אשכולית", setOf(SMOKY, BITTER, Tag.SOUR, MODERN)),
        c("siesta", "Siesta", "קייטי סטיפ: מרגריטה עם קמפרי ואשכולית.",
            SOUR, SHAKE, COUPE, NONE,
            listOf(ml("tequila_blanco", 45.0), ml("campari", 15.0), ml("lime", 15.0), ml("grapefruit", 15.0), ml("simple_syrup", 15.0)),
            "קליפת אשכולית", setOf(BITTER, Tag.SOUR, REFRESHING, MODERN)),
        c("jack_rose", "Jack Rose", "ברנדי תפוחים, לימון וגרנדין.",
            SOUR, SHAKE, COUPE, NONE,
            listOf(ml("calvados", 45.0), ml("lemon", 22.5), ml("grenadine", 15.0)),
            "פרוסת תפוח", setOf(FRUITY, Tag.SOUR, CLASSIC)),
        c("pink_lady", "Pink Lady", "ג'ין, ברנדי תפוחים, גרנדין וקצף.",
            SOUR, DRY_SHAKE, COUPE, NONE,
            listOf(ml("gin", 45.0), ml("calvados", 15.0), ml("lemon", 22.5), ml("grenadine", 15.0), egg(1.0)),
            "דובדבן", setOf(FOAMY, FRUITY, Tag.SOUR, CLASSIC)),
        c("cosmopolitan", "Cosmopolitan", "וודקה, ליקר תפוזים, ליים וחמוציות.",
            SOUR, SHAKE, COUPE, NONE,
            listOf(ml("vodka", 40.0), ml("orange_liqueur", 15.0), ml("lime", 15.0), ml("cranberry_juice", 30.0)),
            "קליפת תפוז מולהבת", setOf(FRUITY, Tag.SOUR, MODERN)),
        c("tom_collins", "Tom Collins", "ג'ין סאוור מוארך בסודה. קר, קליל ומבעבע.",
            SOUR, SHAKE, HB, CUBES,
            listOf(ml("gin", 60.0), ml("lemon", 30.0), ml("simple_syrup", 22.5), top("soda", 60.0)),
            "פרוסת לימון ודובדבן", setOf(REFRESHING, BUBBLY, Tag.SOUR, CLASSIC)),
        c("french_75", "French 75", "ג'ין, לימון ובועות.",
            SOUR, SHAKE, FLUTE, NONE,
            listOf(ml("gin", 30.0), ml("lemon", 15.0), ml("simple_syrup", 15.0), top("sparkling_wine", 60.0)),
            "קליפת לימון", setOf(BUBBLY, Tag.SOUR, CLASSIC)),
        c("espresso_martini", "Espresso Martini", "דיק ברדסל: וודקה, ליקר קפה ואספרסו טרי, עם קצף טבעי.",
            CREAMY, SHAKE, COUPE, NONE,
            listOf(ml("vodka", 50.0), ml("coffee_liqueur", 20.0), ml("coffee", 30.0, note = "אספרסו טרי"), ml("simple_syrup", 10.0, optional = true)),
            "3 פולי קפה", setOf(SWEET, FOAMY, MODERN),
            extra = listOf("לנער חזק במיוחד – הקצף נוצר מהשמנים של האספרסו הטרי")),

        // ================= EQUAL PARTS =================
        c("last_word", "Last Word", "ג'ין, שרטרז ירוק, מרסקינו וליים בחלקים שווים. צמחי, חמוץ ומושלם.",
            EQUAL_PARTS, SHAKE, COUPE, NONE,
            listOf(ml("gin", 22.5), ml("green_chartreuse", 22.5), ml("maraschino", 22.5), ml("lime", 22.5)),
            "דובדבן (לא חובה)", setOf(HERBAL, Tag.SOUR, CLASSIC)),
        c("naked_and_famous", "Naked and Famous", "חואקין סימו: מזקל, שרטרז צהוב, אפרול וליים. מעושן ופירותי.",
            EQUAL_PARTS, SHAKE, COUPE, NONE,
            listOf(ml("mezcal", 22.5), ml("yellow_chartreuse", 22.5), ml("aperol", 22.5), ml("lime", 22.5)),
            "", setOf(SMOKY, HERBAL, Tag.SOUR, MODERN)),
        c("paper_plane", "Paper Plane", "סאם רוס: בורבון, אפרול, אמרו נונינו ולימון. מריר-מתוק ומאוזן.",
            EQUAL_PARTS, SHAKE, COUPE, NONE,
            listOf(ml("american_whiskey", 22.5), ml("aperol", 22.5), ml("amaro_nonino", 22.5, alts = listOf("amaro")), ml("lemon", 22.5)),
            "", setOf(BITTER, Tag.SOUR, MODERN)),
        c("corpse_reviver_2", "Corpse Reviver #2", "ג'ין, ליקר תפוזים, ליֶה ולימון, עם שטיפת אבסינת.",
            EQUAL_PARTS, SHAKE, COUPE, NONE,
            listOf(rinse("absinthe"), ml("gin", 22.5), ml("orange_liqueur", 22.5), ml("lillet_blanc", 22.5), ml("lemon", 22.5)),
            "קליפת תפוז", setOf(HERBAL, Tag.SOUR, CLASSIC)),
        c("blood_and_sand", "Blood and Sand", "סקוטש, ליקר דובדבנים, ורמוט ומיץ תפוז.",
            EQUAL_PARTS, SHAKE, COUPE, NONE,
            listOf(ml("scotch", 22.5), ml("cherry_liqueur", 22.5), ml("sweet_vermouth", 22.5), ml("orange", 22.5)),
            "קליפת תפוז", setOf(FRUITY, CLASSIC)),
        c("bijou", "Bijou", "ג'ין, שרטרז ירוק וורמוט אדום. עשיר וצמחי.",
            EQUAL_PARTS, STIR, COUPE, NONE,
            listOf(ml("gin", 30.0), ml("green_chartreuse", 30.0), ml("sweet_vermouth", 30.0), dash("orange_bitters", 1.0, optional = true)),
            "קליפת לימון ודובדבן", setOf(HERBAL, SPIRIT_FORWARD, CLASSIC)),

        // ================= OLD FASHIONED =================
        c("old_fashioned", "Old Fashioned", "וויסקי, סוכר וביטרס. הקוקטייל במובן המקורי של המילה.",
            OLD_FASHIONED, STIR, ROCKS, LARGE_CUBE,
            listOf(ml("american_whiskey", 60.0), ml("demerara_syrup", 7.5, alts = listOf("simple_syrup"), note = "או 10 מ\"ל סירופ סוכר"),
                dash("angostura", 2.0), dash("orange_bitters", 1.0, optional = true)),
            "קליפת תפוז – לסחוט את השמנים מעל הכוס ולהכניס", setOf(SPIRIT_FORWARD, CLASSIC)),
        c("sazerac", "Sazerac", "ניו אורלינס: ריי, פישו ושטיפת אבסינת. מוגש בלי קרח.",
            OLD_FASHIONED, STIR, ROCKS, NONE,
            listOf(rinse("absinthe"), ml("american_whiskey", 60.0, note = "ריי, או חצי ריי וחצי קוניאק"), ml("simple_syrup", 7.5),
                dash("peychauds", 3.0), dash("angostura", 1.0, optional = true)),
            "קליפת לימון – לסחוט מעל ולזרוק", setOf(SPIRIT_FORWARD, HERBAL, CLASSIC), 2),
        c("oaxaca_old_fashioned", "Oaxaca Old Fashioned", "פיל וורד: טקילה רפוסדו עם נגיעה של מזקל.",
            OLD_FASHIONED, STIR, ROCKS, LARGE_CUBE,
            listOf(ml("tequila_reposado", 45.0, alts = listOf("tequila")), ml("mezcal", 15.0), ml("agave_syrup", 7.5), dash("angostura", 2.0)),
            "קליפת תפוז מולהבת", setOf(SMOKY, SPIRIT_FORWARD, MODERN)),
        c("rusty_nail", "Rusty Nail", "סקוטש ודרמבוי. דבשי ומחמם.",
            OLD_FASHIONED, BUILD, ROCKS, LARGE_CUBE,
            listOf(ml("scotch", 45.0), ml("drambuie", 22.5)),
            "קליפת לימון", setOf(SPIRIT_FORWARD, SWEET, CLASSIC),
            extra = listOf("לערבב בכוס עם הקרח 15 שניות")),

        // ================= MARTINI / MANHATTAN =================
        c("manhattan", "Manhattan", "ריי, ורמוט אדום וביטרס. עשיר, חלק ומלכותי.",
            MARTINI_MANHATTAN, STIR, COUPE, NONE,
            listOf(ml("american_whiskey", 60.0, note = "עדיף ריי"), ml("sweet_vermouth", 30.0), dash("angostura", 2.0)),
            "דובדבן", setOf(SPIRIT_FORWARD, CLASSIC)),
        c("black_manhattan", "Black Manhattan", "מנהטן עם אמרו במקום ורמוט. כהה ומריר.",
            MARTINI_MANHATTAN, STIR, COUPE, NONE,
            listOf(ml("american_whiskey", 60.0), ml("averna", 30.0, alts = listOf("amaro")), dash("angostura", 1.0), dash("orange_bitters", 1.0, optional = true)),
            "דובדבן", setOf(BITTER, SPIRIT_FORWARD, MODERN)),
        c("rob_roy", "Rob Roy", "מנהטן עם סקוטש.",
            MARTINI_MANHATTAN, STIR, COUPE, NONE,
            listOf(ml("scotch", 60.0), ml("sweet_vermouth", 30.0), dash("angostura", 2.0)),
            "דובדבן", setOf(SPIRIT_FORWARD, CLASSIC)),
        c("vieux_carre", "Vieux Carré", "ניו אורלינס: ריי, קוניאק, ורמוט ובנדיקטין.",
            MARTINI_MANHATTAN, STIR, ROCKS, LARGE_CUBE,
            listOf(ml("american_whiskey", 22.5, note = "ריי"), ml("brandy", 22.5, note = "קוניאק"), ml("sweet_vermouth", 22.5), ml("benedictine", 7.5),
                dash("angostura", 1.0), dash("peychauds", 1.0)),
            "קליפת לימון או דובדבן", setOf(SPIRIT_FORWARD, HERBAL, CLASSIC), 2),
        c("martini", "Dry Martini", "ג'ין וורמוט יבש, קר כקרח.",
            MARTINI_MANHATTAN, STIR, NICK_NORA, NONE,
            listOf(ml("gin", 60.0), ml("dry_vermouth", 15.0), dash("orange_bitters", 1.0, optional = true)),
            "קליפת לימון או זית", setOf(SPIRIT_FORWARD, CLASSIC)),
        c("vesper", "Vesper", "ג'יימס בונד: ג'ין, וודקה וליֶה.",
            MARTINI_MANHATTAN, STIR, COUPE, NONE,
            listOf(ml("gin", 45.0), ml("vodka", 15.0), ml("lillet_blanc", 7.5)),
            "קליפת לימון רחבה", setOf(SPIRIT_FORWARD, CLASSIC)),
        c("martinez", "Martinez", "האבא של המרטיני: ג'ין, ורמוט אדום ומרסקינו.",
            MARTINI_MANHATTAN, STIR, COUPE, NONE,
            listOf(ml("gin", 45.0), ml("sweet_vermouth", 45.0), Part("maraschino", 1.0, Measure.BARSPOON), dash("angostura", 2.0)),
            "קליפת תפוז", setOf(SPIRIT_FORWARD, CLASSIC)),
        c("hanky_panky", "Hanky Panky", "עדה קולמן: ג'ין, ורמוט ונגיעה של פרנה.",
            MARTINI_MANHATTAN, STIR, COUPE, NONE,
            listOf(ml("gin", 45.0), ml("sweet_vermouth", 45.0), ml("fernet", 7.5)),
            "קליפת תפוז", setOf(BITTER, HERBAL, CLASSIC)),

        // ================= NEGRONI =================
        c("negroni", "Negroni", "ג'ין, קמפרי וורמוט בחלקים שווים. מריר, מתוק ומושלם.",
            NEGRONI, STIR, ROCKS, LARGE_CUBE,
            listOf(ml("gin", 30.0), ml("campari", 30.0), ml("sweet_vermouth", 30.0)),
            "קליפת תפוז", setOf(BITTER, CLASSIC)),
        c("boulevardier", "Boulevardier", "נגרוני עם בורבון במקום ג'ין. עגול וחם יותר.",
            NEGRONI, STIR, ROCKS, LARGE_CUBE,
            listOf(ml("american_whiskey", 45.0), ml("campari", 30.0), ml("sweet_vermouth", 30.0)),
            "קליפת תפוז", setOf(BITTER, SPIRIT_FORWARD, CLASSIC)),
        c("mezcal_negroni", "Mezcal Negroni", "נגרוני עם מזקל. מעושן ומריר.",
            NEGRONI, STIR, ROCKS, LARGE_CUBE,
            listOf(ml("mezcal", 30.0), ml("campari", 30.0), ml("sweet_vermouth", 30.0)),
            "קליפת תפוז", setOf(BITTER, SMOKY, MODERN)),
        c("jungle_bird", "Jungle Bird", "קואלה לומפור, 1978: רום כהה, קמפרי ואננס. טיקי מריר.",
            TIKI, SHAKE, ROCKS, CUBES,
            listOf(ml("dark_rum", 45.0), ml("campari", 22.5), ml("pineapple_juice", 45.0), ml("lime", 15.0), ml("simple_syrup", 15.0)),
            "פלח אננס", setOf(BITTER, FRUITY, MODERN)),

        // ================= TIKI =================
        c("mai_tai", "Mai Tai", "טרייד ויק, 1944: רום, ליים, אורז'ה וליקר תפוזים.",
            TIKI, SHAKE, ROCKS, CRUSHED,
            listOf(ml("aged_rum", 60.0, alts = listOf("dark_rum")), ml("lime", 22.5), ml("orange_liqueur", 15.0), ml("orgeat", 15.0), ml("simple_syrup", 7.5)),
            "ענף נענע וקליפת ליים חצויה", setOf(FRUITY, CLASSIC)),
        c("queens_park_swizzle", "Queen's Park Swizzle", "טרינידד: רום, ליים, נענע ושכבה של ביטרס מעל קרח כתוש.",
            TIKI, SWIZZLE, HB, CRUSHED,
            listOf(ml("aged_rum", 60.0, alts = listOf("dark_rum")), ml("lime", 22.5), ml("demerara_syrup", 15.0, alts = listOf("simple_syrup")),
                leaves("mint", 8.0), Part("angostura", 4.0, Measure.DASH, Stage.TOP)),
            "ענף נענע", setOf(REFRESHING, HERBAL, CLASSIC), 2,
            steps = listOf(
                "לשים את הנענע בתחתית הכוס ולהכות קלות",
                "להוסיף רום, ליים וסירופ, ולמלא קרח כתוש עד חצי הכוס",
                "לסובב מקל סוויזל (או כף בר) בין כפות הידיים עד שהכוס מתכסה כפור",
                "למלא עוד קרח כתוש, ולטפטף מלמעלה 4 דאשים אנגוסטורה ליצירת שכבה אדומה",
            )),
        c("pina_colada", "Piña Colada", "רום, אננס וקרם קוקוס. קרמי וטרופי.",
            TIKI, SHAKE, HB, CRUSHED,
            listOf(ml("rum", 50.0), ml("coconut_cream", 30.0), ml("pineapple_juice", 90.0), ml("lime", 15.0)),
            "פלח אננס", setOf(Tag.CREAMY, FRUITY, SWEET, CLASSIC),
            extra = listOf("אפשר גם לטחון בבלנדר עם כוס קרח")),

        // ================= HIGHBALLS =================
        c("gin_tonic", "Gin & Tonic", "", HIGHBALL, BUILD, HB, CUBES,
            listOf(ml("gin", 50.0), top("tonic", 150.0)), "פלח ליים או קליפת לימון", setOf(REFRESHING, BUBBLY)),
        c("whisky_highball", "Whisky Highball", "הסגנון היפני: וויסקי וסודה קרה מאוד. פשוט, אבל רק אם עושים נכון.",
            HIGHBALL, BUILD, HB, CUBES,
            listOf(ml("whiskey", 45.0, note = "וויסקי יפני או סקוטש עדין"), top("soda", 135.0)),
            "קליפת לימון", setOf(REFRESHING, BUBBLY),
            extra = listOf("לערבב את הוויסקי עם הקרח 10 שניות, ואז למזוג סודה קרה מאוד לאורך הקרח")),
        c("paloma", "Paloma", "המשקה הלאומי של מקסיקו: טקילה, אשכולית ומלח.",
            HIGHBALL, BUILD, HB, CUBES,
            listOf(ml("tequila_blanco", 50.0, alts = listOf("tequila")), ml("lime", 15.0), Part("salt", 1.0, Measure.PINCH), top("grapefruit_soda", 120.0)),
            "פלח אשכולית, ושוליים של מלח אם רוצים", setOf(REFRESHING, BUBBLY)),
        c("moscow_mule", "Moscow Mule", "וודקה, ליים וג'ינג'ר ביר.", HIGHBALL, BUILD, MUG, CUBES,
            listOf(ml("vodka", 50.0), ml("lime", 15.0), top("ginger_beer", 120.0)), "פלח ליים", setOf(REFRESHING, BUBBLY)),
        c("dark_n_stormy", "Dark 'n' Stormy", "ג'ינג'ר ביר ורום כהה שצף מעליו.", HIGHBALL, BUILD, HB, CUBES,
            listOf(ml("lime", 15.0, optional = true), top("ginger_beer", 100.0), float("dark_rum", 60.0)),
            "פלח ליים", setOf(REFRESHING, BUBBLY)),
        c("el_diablo", "El Diablo", "טקילה, קסיס, ליים וג'ינג'ר ביר.", HIGHBALL, SHAKE, HB, CUBES,
            listOf(ml("tequila", 45.0, note = "רפוסדו"), ml("lime", 15.0), ml("creme_de_cassis", 15.0), top("ginger_beer", 90.0)),
            "פלח ליים", setOf(FRUITY, REFRESHING, BUBBLY, CLASSIC)),
        c("cuba_libre", "Cuba Libre", "רום, קולה וליים.", HIGHBALL, BUILD, HB, CUBES,
            listOf(ml("rum", 50.0), ml("lime", 10.0), top("cola", 120.0)), "פלח ליים", setOf(REFRESHING, BUBBLY, CLASSIC)),
        c("presbyterian", "Presbyterian", "סקוטש, ג'ינג'ר אייל וסודה.", HIGHBALL, BUILD, HB, CUBES,
            listOf(ml("scotch", 50.0), top("ginger_ale", 60.0), top("soda", 60.0)), "קליפת לימון", setOf(REFRESHING, BUBBLY)),
        c("gin_rickey", "Gin Rickey", "ג'ין, ליים וסודה, בלי סוכר. יבש מאוד.", HIGHBALL, BUILD, HB, CUBES,
            listOf(ml("gin", 50.0), ml("lime", 15.0), top("soda", 120.0)), "חצי ליים סחוט בכוס", setOf(REFRESHING, BUBBLY, Tag.SOUR)),
        c("mojito", "Mojito", "רום, ליים, נענע וסודה.", HIGHBALL, MUDDLE, HB, CRUSHED,
            listOf(ml("white_rum", 50.0), ml("lime", 25.0), ml("simple_syrup", 20.0), leaves("mint", 8.0), top("soda", 45.0)),
            "ענף נענע גדול", setOf(REFRESHING, BUBBLY, HERBAL, CLASSIC),
            steps = listOf(
                "לשים את הנענע בכוס ולהכות קלות – לא לטחון, אחרת יוצאת מרירות",
                "להוסיף מיץ ליים, סירופ ורום",
                "למלא קרח כתוש ולערבב מלמטה למעלה",
                "להשלים בסודה ולהוסיף עוד קרח כתוש מלמעלה",
            )),
        c("caipirinha", "Caipirinha", "המשקה הלאומי של ברזיל: קשאסה, ליים כתוש וסוכר.", HIGHBALL, MUDDLE, ROCKS, CRUSHED,
            listOf(ml("cachaca", 60.0), Part("lime", 0.5, Measure.PIECE, note = "חצי ליים חתוך ל-4 פלחים"), Part("sugar", 2.0, Measure.BARSPOON)),
            "", setOf(REFRESHING, Tag.SOUR, CLASSIC),
            steps = listOf(
                "לכתוש בכוס את פלחי הליים עם הסוכר, עד שהסוכר נמס במיץ",
                "למלא קרח כתוש ולהוסיף קשאסה",
                "לערבב היטב",
            )),

        // ================= SPRITZ =================
        c("aperol_spritz", "Aperol Spritz", "", SPRITZ, BUILD, WINE, CUBES,
            listOf(top("sparkling_wine", 90.0), ml("aperol", 60.0), top("soda", 30.0)),
            "פרוסת תפוז", setOf(BUBBLY, BITTER, REFRESHING)),
        c("americano", "Americano", "קמפרי, ורמוט וסודה. הקודם של הנגרוני.", SPRITZ, BUILD, HB, CUBES,
            listOf(ml("campari", 30.0), ml("sweet_vermouth", 30.0), top("soda", 90.0)), "פלח תפוז", setOf(BITTER, BUBBLY, CLASSIC)),
        c("hugo", "Hugo", "ספריץ עם ליקר סמבוק ונענע.", SPRITZ, BUILD, WINE, CUBES,
            listOf(ml("st_germain", 30.0), leaves("mint", 6.0), top("sparkling_wine", 90.0), top("soda", 30.0)),
            "ענף נענע ופלח ליים", setOf(BUBBLY, REFRESHING, HERBAL),
            extra = listOf("להכות את הנענע קלות בין כפות הידיים לפני שמכניסים לכוס")),

        // ================= CREAMY / COFFEE =================
        c("white_russian", "White Russian", "וודקה, ליקר קפה ושמנת.", CREAMY, BUILD, ROCKS, CUBES,
            listOf(ml("vodka", 50.0), ml("coffee_liqueur", 20.0), float("cream", 30.0)), "", setOf(Tag.CREAMY, SWEET, CLASSIC)),
        c("irish_coffee", "Irish Coffee", "קפה חם, וויסקי אירי וסוכר, מתחת לשכבה של שמנת קרה.", CREAMY, HOT, MUG, NONE,
            listOf(ml("irish_whiskey", 40.0, alts = listOf("whiskey")), ml("coffee", 120.0, note = "חם וחזק"), ml("demerara_syrup", 15.0, alts = listOf("simple_syrup")),
                float("cream", 30.0, "מוקצפת קלות")),
            "", setOf(Tag.CREAMY, CLASSIC), 2,
            steps = listOf(
                "לחמם את הכוס במים רותחים ולשפוך אותם",
                "למזוג קפה חם, סירופ וויסקי, ולערבב",
                "להקציף את השמנת קלות – שתהיה סמיכה אבל עדיין נוזלית",
                "לצוף את השמנת על גב כפית, בלי לערבב. שותים את הקפה החם דרך השמנת הקרה",
            )),
    )

    private val byKey = all.associateBy { it.key }
    operator fun get(key: String) = byKey[key]
}
