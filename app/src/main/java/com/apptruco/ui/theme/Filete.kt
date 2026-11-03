package com.apptruco.ui.theme

import android.graphics.Matrix
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.apptruco.R

/** Una línea de filete pintada por dentro del borde: arranca a [inset] del borde y mide [width]. */
@Immutable
data class Filete(val inset: Dp, val width: Dp, val color: Color)

/**
 * Pinta las líneas por dentro de un rectángulo de esquinas [radius], como los `inset box-shadow`
 * del mockup: cada línea redondea con el radio que le queda a esa distancia del borde.
 */
fun DrawScope.drawFiletes(radius: Dp, filetes: List<Filete>) {
    val outer = minOf(radius.toPx(), size.minDimension / 2f)
    filetes.forEach { filete ->
        val center = filete.inset.toPx() + filete.width.toPx() / 2f
        drawRoundRect(
            color = filete.color,
            topLeft = Offset(center, center),
            size = Size(size.width - 2 * center, size.height - 2 * center),
            cornerRadius = CornerRadius((outer - center).coerceAtLeast(0f)),
            style = Stroke(width = filete.width.toPx()),
        )
    }
}

fun Modifier.filetes(radius: Dp, filetes: List<Filete>): Modifier = drawBehind { drawFiletes(radius, filetes) }

/** Línea de oro afuera y de rojo adentro: el marco de los carteles y de los botones del encabezado. */
val MarcoOroRojo = listOf(Filete(0.dp, 1.5.dp, Oro), Filete(4.dp, 1.2.dp, Rojo))

/**
 * Luz radial elíptica, como el `radial-gradient(rx ry at x y, …)` de CSS: el centro y los radios
 * van en fracción del tamaño de lo que se pinta.
 */
class Luz(
    private val x: Float,
    private val y: Float,
    private val radioX: Float,
    private val radioY: Float,
    private vararg val stops: Pair<Float, Color>,
) : ShaderBrush() {
    override fun createShader(size: Size): Shader {
        val center = Offset(size.width * x, size.height * y)
        val rx = size.width * radioX
        val ry = size.height * radioY
        return RadialGradientShader(
            center = center,
            radius = rx,
            colors = stops.map { it.second },
            colorStops = stops.map { it.first },
        ).apply { setLocalMatrix(Matrix().apply { setScale(1f, ry / rx, center.x, center.y) }) }
    }
}

/** Fondo de los carteles: un negro con la luz arriba al medio. */
val LuzCartel = Luz(0.5f, 0f, 1.2f, 1.2f, 0f to NegroCartel, 0.7f to Negro)

/**
 * Cartel fileteado: fondo negro con luz arriba, doble línea de oro y rojo por dentro y volutas en
 * las cuatro esquinas. El contenido va por encima de las volutas.
 */
@Composable
fun Cartel(
    modifier: Modifier = Modifier,
    radius: Dp = 20.dp,
    fondo: Brush = LuzCartel,
    filetes: List<Filete> = MarcoOroRojo,
    voluta: Dp = 30.dp,
    volutaInset: Dp = 7.dp,
    volutasAbajo: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(start = 18.dp, top = 18.dp, end = 18.dp, bottom = 16.dp),
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .background(fondo, RoundedCornerShape(radius))
            .filetes(radius, filetes),
    ) {
        VolutasEnEsquinas(size = voluta, inset = volutaInset, abajo = volutasAbajo)
        Box(modifier = Modifier.padding(contentPadding)) { content() }
    }
}

/** Cinta con las puntas en cola de golondrina; [muesca] es cuánto entra la punta, en fracción del ancho. */
fun cintaShape(muesca: Float) = GenericShape { size, _ ->
    moveTo(0f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width * (1f - muesca), size.height / 2f)
    lineTo(size.width, size.height)
    lineTo(0f, size.height)
    lineTo(size.width * muesca, size.height / 2f)
    close()
}

val CintaEquipoShape = cintaShape(0.09f)
val CintaCelesteShape = cintaShape(0.07f)

private val CelesteYBlanca = Brush.verticalGradient(
    0f to CelesteLuz,
    0.3f to Celeste,
    0.3f to Blanco,
    0.7f to Blanco,
    0.7f to Celeste,
    1f to CelesteSombra,
)

/** Cinta con los colores de la bandera, para los dichos y avisos. */
@Composable
fun Cinta(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier
            .background(CelesteYBlanca, CintaCelesteShape)
            .padding(horizontal = 26.dp, vertical = 4.dp),
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
        color = CelesteTinta,
        textAlign = TextAlign.Center,
    )
}

private val LuzPantalla = Luz(0.5f, 0.35f, 1.2f, 0.8f, 0f to NegroLuz, 0.65f to Negro, 1f to NegroHondo)

// A 5 dp del borde de la pantalla; el radio acompaña las esquinas redondeadas de los teléfonos.
private val MarcoPantalla = listOf(Filete(5.dp, 1.5.dp, Oro), Filete(9.5.dp, 1.2.dp, Rojo))

/** Fondo de las pantallas: negro con la luz arriba y el marco de filete, oro afuera y rojo adentro. */
fun Modifier.pantallaFileteada(): Modifier = background(LuzPantalla).filetes(radius = 35.dp, filetes = MarcoPantalla)

// El dibujo de la voluta ocupa 60 de un lienzo de 68: 4 de margen por lado.
private const val VOLUTA_MARGEN = 4f / 60f

/** Esquina fileteada; se dibuja para arriba a la izquierda y las demás esquinas se espejan. */
@Composable
fun Voluta(
    modifier: Modifier = Modifier,
    espejoX: Boolean = false,
    espejoY: Boolean = false,
) {
    val painter = painterResource(R.drawable.voluta)
    Spacer(
        modifier = modifier
            .graphicsLayer {
                scaleX = if (espejoX) -1f else 1f
                scaleY = if (espejoY) -1f else 1f
            }
            .drawBehind {
                val margen = size.width * VOLUTA_MARGEN
                translate(-margen, -margen) {
                    with(painter) { draw(Size(size.width + 2 * margen, size.height + 2 * margen)) }
                }
            },
    )
}

/** Volutas en las esquinas de un cartel: las de la derecha y las de abajo van espejadas. */
@Composable
fun BoxScope.VolutasEnEsquinas(size: Dp, inset: Dp, abajo: Boolean = true) {
    Voluta(Modifier.align(Alignment.TopStart).padding(inset).size(size))
    Voluta(Modifier.align(Alignment.TopEnd).padding(inset).size(size), espejoX = true)
    if (abajo) {
        Voluta(Modifier.align(Alignment.BottomStart).padding(inset).size(size), espejoY = true)
        Voluta(Modifier.align(Alignment.BottomEnd).padding(inset).size(size), espejoX = true, espejoY = true)
    }
}

private val OroDegrade = Brush.verticalGradient(0f to OroClaro, 0.55f to Oro, 1f to OroOscuro)

/**
 * Letra fileteada: relleno de oro en degradé, contorno negro fino y una sombra roja corrida.
 * Son tres capas del mismo texto pintadas con un solo layout: la sombra y el contorno reusan lo que
 * midió el relleno, así el texto (y el autoajuste) se calcula una vez y no tres.
 */
@Composable
fun LetraFileteada(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    autoSize: TextAutoSize? = null,
    sombra: Dp = 2.dp,
    contorno: Dp = 0.8.dp,
) {
    val stroke = Stroke(width = with(LocalDensity.current) { contorno.toPx() }, join = StrokeJoin.Round)
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    BasicText(
        text = text,
        modifier = modifier.drawWithContent {
            val medido = layout
            // La sombra va corrida en píxeles enteros, como cuando se ubicaba con offset.
            val corrimiento = sombra.roundToPx().toFloat()
            // Fill explícito: el párrafo comparte la pintura y quedaría en trazo por el contorno anterior.
            medido?.let {
                drawText(it, color = SombraLetra, topLeft = Offset(corrimiento, corrimiento), drawStyle = Fill)
            }
            drawContent()
            medido?.let { drawText(it, color = Contorno, drawStyle = stroke) }
        },
        style = style.merge(textAlign = textAlign).copy(brush = OroDegrade),
        onTextLayout = { layout = it },
        overflow = TextOverflow.Visible,
        maxLines = maxLines,
        autoSize = autoSize,
    )
}
