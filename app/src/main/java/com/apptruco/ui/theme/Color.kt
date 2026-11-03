package com.apptruco.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Fileteado porteño: un naipe español pintado como cartel de bodegón.
internal val Negro = Color(0xFF0B0B0B)
internal val NegroHondo = Color(0xFF050505)
internal val NegroAlto = Color(0xFF151515)
internal val NegroLuz = Color(0xFF1A1A1A)
internal val NegroCartel = Color(0xFF1E1E1E)
internal val Crema = Color(0xFFF5EAD2)
internal val CremaPintada = Color(0xFFFFF3D6)
internal val CremaTenue = Color(0xFFCDBF9F)
internal val CremaGastada = Color(0xFFB9AB8C)
internal val CartaArriba = Color(0xFFFBF3E0)
internal val CartaAbajo = Color(0xFFEFE2C3)
internal val Tinta = Color(0xFF1B1410)
internal val TintaApagada = Color(0xFFA8957A)
internal val Contorno = Color(0xFF1A0D05)
internal val LineaGris = Color(0xFF6F6450)
internal val LineaGrisClara = Color(0xFF8F7D62)
internal val PerillaApagada = Color(0xFF4A4438)

internal val Rojo = Color(0xFFC8202F)
internal val RojoOscuro = Color(0xFF7E0F19)
internal val RojoClaro = Color(0xFFEC5560)
internal val Azul = Color(0xFF1D5FAE)
internal val AzulOscuro = Color(0xFF0D3570)
internal val AzulClaro = Color(0xFF4F8FDC)
internal val Verde = Color(0xFF1F8A4C)
internal val VerdeClaro = Color(0xFF2AA35F)
internal val Celeste = Color(0xFF74B9E7)
internal val CelesteLuz = Color(0xFFA7D4F3)
internal val CelesteSombra = Color(0xFF4F9BD2)
internal val CelesteTinta = Color(0xFF10284A)
internal val Blanco = Color(0xFFFFFFFF)
internal val Oro = Color(0xFFE3B23C)
internal val OroClaro = Color(0xFFFFE08A)
internal val OroOscuro = Color(0xFFB07A12)
internal val OroQuemado = Color(0xFF8A5D0C)
internal val OroContorno = Color(0xFF6B4708)
internal val OroTinta = Color(0xFF2A1A03)
internal val SombraLetra = Color(0xFF8E0F1B)

internal val BotonCremaArriba = Color(0xFFFFF8E8)
internal val BotonCremaAbajo = Color(0xFFECDCB8)

// Fósforos pintados de las casitas.
internal val FosforoPalo = Color(0xFFF0C75A)
internal val FosforoCabezaLuz = Color(0xFFE8604A)
internal val FosforoCabeza = Color(0xFFB3261C)
internal val FosforoCabezaSombra = Color(0xFF5E110B)
internal val FosforoSombra = Color(0xFF3B2A12)
internal val GuiaCasita = Color(0xFFCDB98F)

/** Los tres tonos con que se pinta un equipo: claro arriba, base al medio y oscuro abajo. */
@Immutable
data class Tonos(val claro: Color, val base: Color, val oscuro: Color)

internal val TonosRojo = Tonos(claro = RojoClaro, base = Rojo, oscuro = RojoOscuro)
internal val TonosAzul = Tonos(claro = AzulClaro, base = Azul, oscuro = AzulOscuro)
