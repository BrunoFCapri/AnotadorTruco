package com.apptruco.ui.theme

import android.content.Context
import androidx.compose.material3.Typography
import androidx.compose.ui.text.Paragraph
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp
import com.apptruco.R
import kotlinx.coroutines.runBlocking

// Playfair y Alegreya vienen como fuente variable: cada peso es un punto del eje wght.
private fun peso(weight: FontWeight) = FontVariation.Settings(FontVariation.weight(weight.weight))

@OptIn(ExperimentalTextApi::class)
private fun variable(resId: Int, weight: FontWeight, style: FontStyle = FontStyle.Normal) =
    Font(resId, weight = weight, style = style, variationSettings = peso(weight))

/** Letra de cartel: títulos, números y todo lo que va fileteado. */
private val PlayfairDisplay = FontFamily(
    variable(R.font.playfair_display_italic, FontWeight.ExtraBold, FontStyle.Italic),
    variable(R.font.playfair_display_italic, FontWeight.Black, FontStyle.Italic),
)

/** Letra de texto corrido, casi siempre en cursiva. */
private val Alegreya = FontFamily(
    variable(R.font.alegreya, FontWeight.Medium),
    variable(R.font.alegreya, FontWeight.Bold),
    variable(R.font.alegreya_italic, FontWeight.Medium, FontStyle.Italic),
    variable(R.font.alegreya_italic, FontWeight.Bold, FontStyle.Italic),
)

/** Versalitas para rótulos y botones: las minúsculas salen como mayúsculas chicas. */
private val AlegreyaSC = FontFamily(
    Font(R.font.alegreya_sc_bold, FontWeight.Bold),
    Font(R.font.alegreya_sc_extrabold, FontWeight.ExtraBold),
)

private val Cartel = TextStyle(fontFamily = PlayfairDisplay, fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)
private val Texto = TextStyle(fontFamily = Alegreya, fontWeight = FontWeight.Medium, fontStyle = FontStyle.Italic)
private val Rotulo = TextStyle(fontFamily = AlegreyaSC, fontWeight = FontWeight.ExtraBold)

internal val TrucoTypography = Typography(
    displaySmall = Cartel.copy(fontSize = 34.sp, lineHeight = 44.sp),
    headlineLarge = Cartel.copy(fontSize = 42.sp, lineHeight = 48.sp),
    headlineMedium = Cartel.copy(fontSize = 38.sp, lineHeight = 42.sp),
    headlineSmall = Cartel.copy(fontSize = 26.sp, lineHeight = 32.sp),
    titleLarge = Cartel.copy(fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = Cartel.copy(fontWeight = FontWeight.ExtraBold, fontSize = 19.sp, lineHeight = 24.sp),
    titleSmall = Cartel.copy(fontSize = 17.sp, lineHeight = 20.sp, letterSpacing = 0.3.sp),
    bodyLarge = Texto.copy(fontSize = 17.sp, lineHeight = 22.sp),
    bodyMedium = Texto.copy(fontSize = 15.sp, lineHeight = 19.sp),
    bodySmall = Texto.copy(fontSize = 13.sp, lineHeight = 17.sp),
    labelLarge = Rotulo.copy(fontSize = 17.sp, lineHeight = 22.sp, letterSpacing = 1.5.sp),
    labelMedium = Rotulo.copy(fontSize = 14.sp, lineHeight = 18.sp, letterSpacing = 1.5.sp),
    labelSmall = Rotulo.copy(fontSize = 13.sp, lineHeight = 16.sp, letterSpacing = 1.5.sp),
)

/**
 * Prepara las letras fuera del hilo principal mientras arranca la app. Crear cada peso de las fuentes
 * variables y el cortador de líneas cuesta caro la primera vez; si no se adelanta, lo paga el primer
 * cuadro de la partida. Las cachés de fuentes de Compose son globales: la UI encuentra todo listo.
 */
fun precalentarLetras(context: Context) {
    val resolver = createFontFamilyResolver(context)
    runBlocking { listOf(PlayfairDisplay, Alegreya, AlegreyaSC).forEach { resolver.preload(it) } }
    // Un párrafo por estilo arma el cortador de líneas con la misma configuración que usa la UI.
    val density = Density(context)
    val constraints = Constraints(maxWidth = context.resources.displayMetrics.widthPixels)
    with(TrucoTypography) {
        listOf(
            displaySmall, headlineLarge, headlineMedium, headlineSmall, titleLarge, titleMedium, titleSmall,
            bodyLarge, bodyMedium, bodySmall, labelLarge, labelMedium, labelSmall,
        )
    }.forEach { style ->
        Paragraph(
            text = "¿Quién ganó la mano? Nosotros +3",
            style = style,
            constraints = constraints,
            density = density,
            fontFamilyResolver = resolver,
        )
    }
}
