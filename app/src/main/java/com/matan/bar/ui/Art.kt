package com.matan.bar.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import com.matan.bar.logic.Cocktail
import com.matan.bar.logic.Glass
import com.matan.bar.logic.Ice
import com.matan.bar.logic.Logic
import com.matan.bar.logic.Stage
import com.matan.bar.logic.Tag
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

// ======================= drink colour =======================

/** Colour of each ingredient in the glass and how strongly it tints (clear spirits ~0). */
private data class Tint(val color: Color, val strength: Float)

private val tints: Map<String, Tint> = mapOf(
    "gin" to Tint(Color(0xFFEFF6F8), 0.02f), "vodka" to Tint(Color(0xFFEFF6F8), 0.02f),
    "white_rum" to Tint(Color(0xFFF3F0E6), 0.03f), "cachaca" to Tint(Color(0xFFF3F0E6), 0.03f),
    "tequila" to Tint(Color(0xFFF1EBD8), 0.05f), "tequila_blanco" to Tint(Color(0xFFF1EBD8), 0.04f),
    "tequila_reposado" to Tint(Color(0xFFE2B866), 0.35f), "mezcal" to Tint(Color(0xFFEDE6D2), 0.06f),
    "pisco" to Tint(Color(0xFFF3F0E6), 0.03f),
    "whiskey" to Tint(Color(0xFFC77B2B), 0.6f), "american_whiskey" to Tint(Color(0xFFC0702A), 0.65f),
    "bourbon" to Tint(Color(0xFFC0702A), 0.65f), "rye" to Tint(Color(0xFFB8692C), 0.65f),
    "scotch" to Tint(Color(0xFFD08F3A), 0.55f), "blended_scotch" to Tint(Color(0xFFD08F3A), 0.55f),
    "islay_scotch" to Tint(Color(0xFFC99A4A), 0.5f), "irish_whiskey" to Tint(Color(0xFFD69A45), 0.5f),
    "japanese_whisky" to Tint(Color(0xFFD69A45), 0.5f), "rum" to Tint(Color(0xFFC88A3E), 0.4f),
    "aged_rum" to Tint(Color(0xFFB8702E), 0.65f), "dark_rum" to Tint(Color(0xFF5A2E14), 1.0f),
    "brandy" to Tint(Color(0xFFA9581F), 0.7f), "cognac" to Tint(Color(0xFFA9581F), 0.7f), "calvados" to Tint(Color(0xFFC9893A), 0.55f),
    "orange_liqueur" to Tint(Color(0xFFF7EFD9), 0.04f), "maraschino" to Tint(Color(0xFFF3F0EA), 0.03f),
    "amaretto" to Tint(Color(0xFFB8611F), 0.7f), "campari" to Tint(Color(0xFFD7192B), 1.3f),
    "aperol" to Tint(Color(0xFFFF6A1A), 1.1f), "green_chartreuse" to Tint(Color(0xFF8DBA2C), 0.9f),
    "yellow_chartreuse" to Tint(Color(0xFFE8D246), 0.7f), "benedictine" to Tint(Color(0xFFC5832E), 0.6f),
    "st_germain" to Tint(Color(0xFFF4EBB8), 0.2f), "coffee_liqueur" to Tint(Color(0xFF2A160C), 1.3f),
    "creme_de_violette" to Tint(Color(0xFF7D5BC9), 1.2f), "creme_de_cassis" to Tint(Color(0xFF5A1238), 1.3f),
    "cherry_liqueur" to Tint(Color(0xFF8E1028), 1.2f), "drambuie" to Tint(Color(0xFFC98A2E), 0.6f),
    "amaro" to Tint(Color(0xFF4A2412), 1.0f), "amaro_nonino" to Tint(Color(0xFFB0602A), 0.7f),
    "averna" to Tint(Color(0xFF3A1A0E), 1.1f), "fernet" to Tint(Color(0xFF2B1A10), 1.2f),
    "sweet_vermouth" to Tint(Color(0xFF6E1A14), 1.0f), "dry_vermouth" to Tint(Color(0xFFF1EDC8), 0.08f),
    "lillet_blanc" to Tint(Color(0xFFF0D47A), 0.35f), "sparkling_wine" to Tint(Color(0xFFF3E0A0), 0.15f),
    "red_wine" to Tint(Color(0xFF6B0F2A), 1.4f),
    "angostura" to Tint(Color(0xFF7A1410), 1.6f), "orange_bitters" to Tint(Color(0xFFD06A1A), 1.0f), "peychauds" to Tint(Color(0xFFC0182A), 1.4f),
    "lemon" to Tint(Color(0xFFF5E37A), 0.35f), "lime" to Tint(Color(0xFFD3E88E), 0.35f),
    "orange" to Tint(Color(0xFFFFA62B), 0.8f), "grapefruit" to Tint(Color(0xFFF7A9A0), 0.45f),
    "egg" to Tint(Color(0xFFFFF1CF), 0.6f), "cream" to Tint(Color(0xFFFFF8EC), 1.2f),
    "pineapple_juice" to Tint(Color(0xFFF8D35E), 0.6f), "cranberry_juice" to Tint(Color(0xFFC2183A), 1.0f),
    "coconut_cream" to Tint(Color(0xFFFFFBF2), 1.2f), "coffee" to Tint(Color(0xFF2A1408), 1.4f),
    "simple_syrup" to Tint(Color(0xFFF6F4EE), 0.02f), "demerara_syrup" to Tint(Color(0xFFB8732A), 0.5f),
    "honey_syrup" to Tint(Color(0xFFE6B23C), 0.45f), "honey_ginger_syrup" to Tint(Color(0xFFE2AC40), 0.45f),
    "ginger_syrup" to Tint(Color(0xFFEEDB8A), 0.3f), "maple_syrup" to Tint(Color(0xFFAE6420), 0.6f),
    "agave_syrup" to Tint(Color(0xFFE9C46A), 0.25f), "orgeat" to Tint(Color(0xFFF3EAD8), 0.45f),
    "grenadine" to Tint(Color(0xFFC4102E), 1.4f), "cinnamon_syrup" to Tint(Color(0xFFB66A2C), 0.5f),
    "raspberry_syrup" to Tint(Color(0xFFD81B60), 1.3f),
    "soda" to Tint(Color(0xFFEFF6F8), 0.0f), "tonic" to Tint(Color(0xFFEFF6F8), 0.01f),
    "ginger_beer" to Tint(Color(0xFFF0DEA2), 0.2f), "ginger_ale" to Tint(Color(0xFFE7C77A), 0.25f),
    "cola" to Tint(Color(0xFF3A1A0C), 1.2f), "grapefruit_soda" to Tint(Color(0xFFF6CFC7), 0.2f),
)

private val clear = Color(0xFFDDEFF5)

/** Blend of everything in the glass, weighted by volume and tint strength. */
fun drinkColor(c: Cocktail): Color {
    var r = 0f; var g = 0f; var b = 0f; var wSum = 0f; var vSum = 0f
    c.parts.filter { it.stage == Stage.IN || it.stage == Stage.TOP }.forEach { p ->
        val v = Logic.volumeMl(p).toFloat()
        if (v <= 0f) return@forEach
        val t = tints[p.key] ?: Tint(clear, 0.05f)
        val w = v * t.strength
        r += t.color.red * w; g += t.color.green * w; b += t.color.blue * w
        wSum += w; vSum += v
    }
    if (wSum <= 0f || vSum <= 0f) return clear.copy(alpha = 0.38f)
    val alpha = (0.38f + min(1f, wSum / vSum * 1.8f) * 0.57f).coerceIn(0.38f, 0.95f)
    val mixed = Color(r / wSum, g / wSum, b / wSum)
    // little strength -> mostly clear
    val k = min(1f, wSum / vSum * 2.5f)
    return lerp(clear, mixed, k).copy(alpha = alpha)
}

// ======================= art spec =======================

private enum class Garnish { LIME_WHEEL, LEMON_WHEEL, ORANGE_WHEEL, LEMON_TWIST, ORANGE_TWIST, GRAPEFRUIT_TWIST, CHERRY, LEAVES, PINEAPPLE, BEANS, RASPBERRIES, OLIVE, APPLE, GINGER }

private class ArtSpec(
    val glass: Glass,
    val ice: Ice,
    val liquid: Color,
    val foam: Color?,
    val float: Color?,
    val bubbles: Boolean,
    val garnishes: List<Garnish>,
    val rim: Color?,
    val drops: Boolean,
    val seed: Int,
)

private fun specOf(c: Cocktail): ArtSpec {
    val g = c.garnish
    val list = mutableListOf<Garnish>()
    fun add(x: Garnish) { if (x !in list) list += x }
    val peel = g.contains("קליפ")
    if (g.contains("ליים")) add(Garnish.LIME_WHEEL)
    if (g.contains("לימון")) add(if (peel) Garnish.LEMON_TWIST else Garnish.LEMON_WHEEL)
    if (g.contains("תפוז")) add(if (peel) Garnish.ORANGE_TWIST else Garnish.ORANGE_WHEEL)
    if (g.contains("אשכולית")) add(Garnish.GRAPEFRUIT_TWIST)
    if (g.contains("דובדבן")) add(Garnish.CHERRY)
    if (g.contains("נענע") || g.contains("בזיליקום")) add(Garnish.LEAVES)
    if (g.contains("אננס")) add(Garnish.PINEAPPLE)
    if (g.contains("פולי קפה")) add(Garnish.BEANS)
    if (g.contains("פטל")) add(Garnish.RASPBERRIES)
    if (g.contains("זית") && list.isEmpty()) add(Garnish.OLIVE)
    if (g.contains("תפוח")) add(Garnish.APPLE)
    if (g.contains("ג'ינג'ר")) add(Garnish.GINGER)

    val floatPart = c.parts.firstOrNull { it.stage == Stage.FLOAT }
    val coffee = c.parts.any { it.key == "coffee" }
    return ArtSpec(
        glass = c.glass,
        ice = c.ice,
        liquid = drinkColor(c),
        foam = if (Tag.FOAMY in c.tags && floatPart == null) (if (coffee) Color(0xFFC9A273) else Color(0xFFF6F0E4)) else null,
        float = floatPart?.let { (tints[it.key]?.color ?: clear).copy(alpha = 0.92f) },
        bubbles = Tag.BUBBLY in c.tags,
        garnishes = list.take(2),
        rim = when {
            g.contains("מלח") -> Color(0xFFF7F7F7)
            g.contains("סוכר") -> Color(0xFFFFF3D6)
            else -> null
        },
        drops = g.contains("אנגוסטורה") && Tag.FOAMY in c.tags,
        seed = c.key.hashCode(),
    )
}

// ======================= geometry =======================

private class Geo(
    val bowl: Path, val top: Float, val bottom: Float, val liquidTop: Float,
    val rimL: Float, val rimR: Float, val hasStem: Boolean, val stemTop: Float, val baseY: Float,
    val baseHalf: Float, val handle: Boolean,
)

private fun geo(glass: Glass, w: Float, h: Float): Geo {
    val cx = w / 2
    val p = Path()
    fun mirrorCurve(top: Float, half: Float, bottom: Float, c1x: Float, c1y: Float, c2x: Float) {
        p.moveTo(cx - half, top)
        p.cubicTo(cx - c1x, c1y, cx - c2x, bottom, cx, bottom)
        p.cubicTo(cx + c2x, bottom, cx + c1x, c1y, cx + half, top)
        p.close()
    }
    return when (glass) {
        Glass.COUPE -> {
            val top = 0.24f * h; val bottom = 0.46f * h; val half = 0.42f * w
            mirrorCurve(top, half, bottom, half, top + 0.24f * h, 0.12f * w)
            Geo(p, top, bottom, top + 0.04f * h, cx - half, cx + half, true, bottom, 0.88f * h, 0.22f * w, false)
        }
        Glass.NICK_NORA -> {
            val top = 0.16f * h; val bottom = 0.5f * h; val half = 0.28f * w
            mirrorCurve(top, half, bottom, half, top + 0.28f * h, 0.13f * w)
            Geo(p, top, bottom, top + 0.05f * h, cx - half, cx + half, true, bottom, 0.88f * h, 0.2f * w, false)
        }
        Glass.FLUTE -> {
            val top = 0.06f * h; val bottom = 0.6f * h; val half = 0.14f * w
            mirrorCurve(top, half, bottom, half * 1.05f, top + 0.4f * h, 0.08f * w)
            Geo(p, top, bottom, top + 0.06f * h, cx - half, cx + half, true, bottom, 0.9f * h, 0.17f * w, false)
        }
        Glass.WINE -> {
            val top = 0.12f * h; val bottom = 0.52f * h; val half = 0.24f * w
            p.moveTo(cx - half, top)
            p.cubicTo(cx - half * 1.45f, top + 0.22f * h, cx - half * 0.9f, bottom, cx, bottom)
            p.cubicTo(cx + half * 0.9f, bottom, cx + half * 1.45f, top + 0.22f * h, cx + half, top)
            p.close()
            Geo(p, top, bottom, top + 0.17f * h, cx - half, cx + half, true, bottom, 0.9f * h, 0.2f * w, false)
        }
        Glass.ROCKS -> {
            val top = 0.36f * h; val bottom = 0.9f * h; val ht = 0.33f * w; val hb = 0.3f * w
            p.moveTo(cx - ht, top); p.lineTo(cx - hb, bottom); p.lineTo(cx + hb, bottom); p.lineTo(cx + ht, top); p.close()
            Geo(p, top, bottom, top + 0.12f * h, cx - ht, cx + ht, false, 0f, bottom, hb, false)
        }
        Glass.HIGHBALL -> {
            val top = 0.06f * h; val bottom = 0.94f * h; val ht = 0.21f * w; val hb = 0.19f * w
            p.moveTo(cx - ht, top); p.lineTo(cx - hb, bottom); p.lineTo(cx + hb, bottom); p.lineTo(cx + ht, top); p.close()
            Geo(p, top, bottom, top + 0.08f * h, cx - ht, cx + ht, false, 0f, bottom, hb, false)
        }
        Glass.MUG -> {
            val top = 0.28f * h; val bottom = 0.9f * h; val half = 0.25f * w
            p.moveTo(cx - half, top); p.lineTo(cx - half, bottom); p.lineTo(cx + half, bottom); p.lineTo(cx + half, top); p.close()
            Geo(p, top, bottom, top + 0.07f * h, cx - half, cx + half, false, 0f, bottom, half, true)
        }
    }
}

// ======================= drawing =======================

@Composable
fun CocktailArt(c: Cocktail, modifier: Modifier = Modifier, animate: Boolean = false) {
    val spec = remember(c) { specOf(c) }
    val phase = if (animate) {
        val t = rememberInfiniteTransition(label = "bubbles")
        t.animateFloat(0f, 1f, infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart), label = "phase").value
    } else 0.35f
    Canvas(modifier) { drawDrink(spec, phase) }
}

private fun DrawScope.drawDrink(s: ArtSpec, phase: Float) {
    val w = size.width
    val h = size.height
    val cx = w / 2
    val g = geo(s.glass, w, h)
    val sw = (w * 0.018f).coerceAtLeast(1.5f)
    val rnd = Random(s.seed)
    val glassLine = Color.White.copy(alpha = 0.6f)

    // soft glow behind the glass
    drawCircle(
        Brush.radialGradient(listOf(s.liquid.copy(alpha = 0.35f), Color.Transparent), Offset(cx, (g.top + g.bottom) / 2), w * 0.55f),
        radius = w * 0.55f, center = Offset(cx, (g.top + g.bottom) / 2),
    )

    clipPath(g.bowl) {
        // glass body tint
        drawRect(Color.White.copy(alpha = 0.05f))
        // liquid
        val liquidH = g.bottom - g.liquidTop
        drawRect(
            Brush.verticalGradient(
                listOf(lerp(s.liquid, Color.White, 0.18f).copy(alpha = s.liquid.alpha), s.liquid, lerp(s.liquid, Color.Black, 0.15f).copy(alpha = s.liquid.alpha)),
                startY = g.liquidTop, endY = g.bottom,
            ),
            topLeft = Offset(0f, g.liquidTop), size = Size(w, liquidH),
        )
        // ice
        when (s.ice) {
            Ice.CUBES -> {
                val n = if (s.glass == Glass.HIGHBALL) 5 else 3
                val cube = (g.rimR - g.rimL) * (if (s.glass == Glass.HIGHBALL) 0.42f else 0.3f)
                repeat(n) { i ->
                    val x = g.rimL + (g.rimR - g.rimL) * (0.12f + 0.38f * ((i * 0.61f + rnd.nextFloat() * 0.3f) % 1f))
                    val y = g.liquidTop + (if (s.glass == Glass.HIGHBALL) i * cube * 0.95f else (i % 2) * cube * 0.6f) - cube * 0.15f
                    rotate(rnd.nextFloat() * 30f - 15f, Offset(x + cube / 2, y + cube / 2)) {
                        drawRoundRect(Color.White.copy(alpha = 0.18f), Offset(x, y), Size(cube, cube), CornerRadius(cube * 0.18f))
                        drawRoundRect(Color.White.copy(alpha = 0.4f), Offset(x, y), Size(cube, cube), CornerRadius(cube * 0.18f), style = Stroke(sw * 0.7f))
                    }
                }
            }
            Ice.LARGE_CUBE -> {
                val cube = (g.rimR - g.rimL) * 0.55f
                val x = cx - cube / 2
                val y = g.liquidTop - cube * 0.12f
                drawRoundRect(Color.White.copy(alpha = 0.16f), Offset(x, y), Size(cube, cube), CornerRadius(cube * 0.1f))
                drawRoundRect(Color.White.copy(alpha = 0.45f), Offset(x, y), Size(cube, cube), CornerRadius(cube * 0.1f), style = Stroke(sw * 0.8f))
                drawLine(Color.White.copy(alpha = 0.35f), Offset(x + cube * 0.15f, y + cube * 0.2f), Offset(x + cube * 0.15f, y + cube * 0.7f), sw * 0.6f, StrokeCap.Round)
            }
            Ice.CRUSHED -> {
                repeat(38) {
                    val x = g.rimL + rnd.nextFloat() * (g.rimR - g.rimL)
                    val y = g.liquidTop - 0.03f * h + rnd.nextFloat() * (g.bottom - g.liquidTop) * 0.75f
                    drawCircle(Color.White.copy(alpha = 0.18f + rnd.nextFloat() * 0.2f), w * (0.018f + rnd.nextFloat() * 0.022f), Offset(x, y))
                }
            }
            Ice.NONE -> {}
        }
        // bubbles
        if (s.bubbles) {
            repeat(14) { i ->
                val x = g.rimL + (g.rimR - g.rimL) * (0.1f + rnd.nextFloat() * 0.8f)
                val off = (phase + i / 14f) % 1f
                val y = g.bottom - off * (g.bottom - g.liquidTop)
                drawCircle(Color.White.copy(alpha = 0.5f * (1f - off * 0.6f)), w * (0.007f + rnd.nextFloat() * 0.007f), Offset(x, y))
            }
        }
        // float layer
        s.float?.let { f ->
            drawRect(Brush.verticalGradient(listOf(f, f.copy(alpha = 0f)), g.liquidTop, g.liquidTop + 0.12f * h), Offset(0f, g.liquidTop), Size(w, 0.12f * h))
        }
        // foam
        s.foam?.let { foam ->
            val fh = 0.05f * h
            drawRect(foam, Offset(0f, g.liquidTop - fh * 0.2f), Size(w, fh))
            drawRect(Brush.verticalGradient(listOf(foam, foam.copy(alpha = 0f)), g.liquidTop + fh * 0.8f, g.liquidTop + fh * 1.6f), Offset(0f, g.liquidTop + fh * 0.8f), Size(w, fh * 0.8f))
            if (s.drops) {
                listOf(-0.08f, 0f, 0.08f).forEach { dx ->
                    drawCircle(Color(0xFF8E1A12), w * 0.016f, Offset(cx + dx * w, g.liquidTop + fh * 0.35f))
                }
            }
        }
        // reflection
        drawLine(Color.White.copy(alpha = 0.18f), Offset(g.rimL + w * 0.06f, g.top + (g.bottom - g.top) * 0.15f),
            Offset(g.rimL + w * 0.07f, g.top + (g.bottom - g.top) * 0.75f), sw * 1.6f, StrokeCap.Round)
    }

    // glass outline, stem, base, handle
    drawPath(g.bowl, glassLine, style = Stroke(sw))
    if (g.hasStem) {
        drawLine(glassLine, Offset(cx, g.stemTop), Offset(cx, g.baseY), sw * 1.2f)
        drawLine(glassLine, Offset(cx - g.baseHalf, g.baseY), Offset(cx + g.baseHalf, g.baseY), sw * 1.4f, StrokeCap.Round)
    } else {
        drawLine(Color.White.copy(alpha = 0.35f), Offset(cx - g.baseHalf, g.bottom - sw * 2), Offset(cx + g.baseHalf, g.bottom - sw * 2), sw * 2.5f)
    }
    if (g.handle) {
        val hh = (g.bottom - g.top) * 0.55f
        drawArc(glassLine, -90f, 180f, false, Offset(g.rimR - w * 0.1f, g.top + (g.bottom - g.top) * 0.18f), Size(w * 0.24f, hh), style = Stroke(sw * 1.4f))
    }
    // rim of salt / sugar
    s.rim?.let { rim ->
        var x = g.rimL
        while (x < cx) {
            drawCircle(rim, w * 0.012f, Offset(x, g.top + rnd.nextFloat() * w * 0.012f))
            x += w * 0.018f
        }
    }
    // garnish
    s.garnishes.forEachIndexed { i, gar -> drawGarnish(gar, g, w, h, sw, i) }
}

private fun DrawScope.wheel(center: Offset, r: Float, peel: Color, flesh: Color) {
    drawCircle(peel, r, center)
    drawCircle(flesh, r * 0.84f, center)
    repeat(8) { k ->
        val a = Math.toRadians(k * 45.0)
        drawLine(Color.White.copy(alpha = 0.55f), center, Offset(center.x + (cos(a) * r * 0.8f).toFloat(), center.y + (sin(a) * r * 0.8f).toFloat()), r * 0.07f)
    }
    drawCircle(Color.White.copy(alpha = 0.6f), r * 0.12f, center)
}

private fun DrawScope.twist(start: Offset, w: Float, color: Color, sw: Float) {
    // a short curled peel resting on the rim
    val p = Path().apply {
        moveTo(start.x + w * 0.03f, start.y + w * 0.02f)
        cubicTo(start.x - w * 0.02f, start.y - w * 0.1f, start.x - w * 0.14f, start.y - w * 0.04f, start.x - w * 0.1f, start.y - w * 0.13f)
        cubicTo(start.x - w * 0.07f, start.y - w * 0.2f, start.x - w * 0.16f, start.y - w * 0.21f, start.x - w * 0.15f, start.y - w * 0.16f)
    }
    drawPath(p, lerp(color, Color.Black, 0.25f), style = Stroke(sw * 4.6f, cap = StrokeCap.Round))
    drawPath(p, color, style = Stroke(sw * 3.6f, cap = StrokeCap.Round))
    drawPath(p, Color.White.copy(alpha = 0.35f), style = Stroke(sw * 0.9f, cap = StrokeCap.Round))
}

private fun DrawScope.drawGarnish(gar: Garnish, g: Geo, w: Float, h: Float, sw: Float, index: Int) {
    val rimR = Offset(g.rimR - w * 0.02f, g.top)
    val rimL = Offset(g.rimL + w * 0.02f, g.top)
    val at = if (index == 0) rimR else rimL
    val r = w * 0.11f
    val pick = Color(0xFFE7D3A8)
    when (gar) {
        Garnish.LIME_WHEEL -> wheel(at, r, Color(0xFF4E8A1E), Color(0xFFB7DB6B))
        Garnish.LEMON_WHEEL -> wheel(at, r, Color(0xFFE9C21A), Color(0xFFFFEE8A))
        Garnish.ORANGE_WHEEL -> wheel(at, r, Color(0xFFE77A12), Color(0xFFFFB648))
        Garnish.LEMON_TWIST -> twist(at.copy(y = at.y + h * 0.02f), w, Color(0xFFF4CF2A), sw)
        Garnish.ORANGE_TWIST -> twist(at.copy(y = at.y + h * 0.02f), w, Color(0xFFF08A1C), sw)
        Garnish.GRAPEFRUIT_TWIST -> twist(at.copy(y = at.y + h * 0.02f), w, Color(0xFFF27B78), sw)
        Garnish.CHERRY -> {
            val c = Offset(at.x - w * 0.04f, at.y - h * 0.035f)
            drawPath(Path().apply { moveTo(c.x, c.y - w * 0.04f); quadTo(c.x + w * 0.02f, c.y - w * 0.14f, c.x + w * 0.1f, c.y - w * 0.16f) },
                Color(0xFF5B3A1A), style = Stroke(sw * 1.2f, cap = StrokeCap.Round))
            drawCircle(Color(0xFF7A0A1C), w * 0.05f, c)
            drawCircle(Color.White.copy(alpha = 0.35f), w * 0.014f, Offset(c.x - w * 0.015f, c.y - w * 0.015f))
        }
        Garnish.LEAVES -> {
            val base = Offset(at.x - w * 0.06f, at.y + h * 0.02f)
            drawLine(Color(0xFF2E6B2A), base, Offset(base.x + w * 0.02f, base.y - h * 0.16f), sw)
            listOf(-35f, 25f, -10f).forEachIndexed { k, deg ->
                val c = Offset(base.x + w * (0.02f + k * 0.01f), base.y - h * (0.06f + k * 0.045f))
                rotate(deg, c) {
                    drawOval(Color(0xFF3F9A3A), Offset(c.x - w * 0.06f, c.y - w * 0.028f), Size(w * 0.12f, w * 0.056f))
                    drawLine(Color(0xFF2E6B2A), Offset(c.x - w * 0.05f, c.y), Offset(c.x + w * 0.05f, c.y), sw * 0.5f)
                }
            }
        }
        Garnish.PINEAPPLE -> {
            val p = Path().apply {
                moveTo(at.x - w * 0.12f, at.y); lineTo(at.x + w * 0.04f, at.y - h * 0.1f); lineTo(at.x + w * 0.06f, at.y + h * 0.02f); close()
            }
            drawPath(p, Color(0xFFF5C842))
            drawPath(p, Color(0xFFC79A1A), style = Stroke(sw))
            drawOval(Color(0xFF3E8E3A), Offset(at.x + w * 0.02f, at.y - h * 0.18f), Size(w * 0.04f, h * 0.09f))
        }
        Garnish.BEANS -> listOf(-0.06f, 0f, 0.06f).forEach { dx ->
            val c = Offset(w / 2 + dx * w, g.liquidTop + h * 0.005f)
            drawOval(Color(0xFF3A1E0E), Offset(c.x - w * 0.025f, c.y - w * 0.015f), Size(w * 0.05f, w * 0.03f))
            drawLine(Color(0xFF1A0C05), Offset(c.x - w * 0.018f, c.y), Offset(c.x + w * 0.018f, c.y), sw * 0.5f)
        }
        Garnish.RASPBERRIES -> {
            drawLine(pick, Offset(at.x + w * 0.05f, at.y - h * 0.08f), Offset(at.x - w * 0.3f, at.y + h * 0.04f), sw)
            listOf(0.0f, 0.1f, 0.2f).forEach { t ->
                drawCircle(Color(0xFFC2185B), w * 0.035f, Offset(at.x + w * 0.03f - t * w * 1.1f, at.y - h * 0.07f + t * h * 0.35f))
            }
        }
        Garnish.OLIVE -> {
            drawLine(pick, Offset(at.x, at.y - h * 0.08f), Offset(w / 2 - w * 0.05f, g.bottom - (g.bottom - g.top) * 0.35f), sw)
            drawOval(Color(0xFF7C8A2A), Offset(w / 2 - w * 0.1f, g.bottom - (g.bottom - g.top) * 0.45f), Size(w * 0.09f, w * 0.07f))
        }
        Garnish.APPLE -> {
            drawArc(Color(0xFFF6EBC8), 180f, 180f, true, Offset(at.x - w * 0.1f, at.y - w * 0.08f), Size(w * 0.2f, w * 0.16f))
            drawArc(Color(0xFF9CCC3A), 180f, 180f, false, Offset(at.x - w * 0.1f, at.y - w * 0.08f), Size(w * 0.2f, w * 0.16f), style = Stroke(sw * 1.5f))
        }
        Garnish.GINGER -> drawRoundRect(Color(0xFFE9D08A), Offset(at.x - w * 0.05f, at.y - h * 0.04f), Size(w * 0.1f, w * 0.07f), CornerRadius(w * 0.02f))
    }
}
