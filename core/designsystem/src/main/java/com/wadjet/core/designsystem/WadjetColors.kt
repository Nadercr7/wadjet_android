package com.wadjet.core.designsystem

import androidx.compose.ui.graphics.Color

object WadjetColors {
    // Gold palette — bespoke gilded gold (U6), replaces generic template-gold #D4AF37
    val Gold = Color(0xFFC8A24B)
    val GoldLight = Color(0xFFE2C97E)
    val GoldDark = Color(0xFF9E7C33)
    val GoldMuted = Color(0xFF7E661F)
    val GoldGlow = Color(0x1FC8A24B) // 12% opacity

    // Surfaces
    val Night = Color(0xFF0A0A0A)
    val Surface = Color(0xFF141414)
    val SurfaceAlt = Color(0xFF1E1E1E)
    val SurfaceHover = Color(0xFF252525)

    // Borders
    val Border = Color(0xFF2A2A2A)
    val BorderLight = Color(0xFF3A3A3A)

    // Text — U2: distinct, WCAG-AA-passing ramp on Night; Dust lightened to clear K-02 debt
    val Text = Color(0xFFF0F0F0)
    val TextMuted = Color(0xFFB4B4B4)
    val Ivory = Color(0xFFF5F0E8)
    val Sand = Color(0xFFC4A265)
    val Dust = Color(0xFFB39B76)

    // Semantic
    val Success = Color(0xFF4CAF50)
    val Error = Color(0xFFEF4444)
    val Warning = Color(0xFFF59E0B)
    val ErrorContainer = Color(0xFF93000A)
    val OnErrorContainer = Color(0xFFFFDAD6)

    // Difficulty — Egyptian pigment palette
    val DifficultyBeginner = Color(0xFFC8A24B)       // Gold — like new gold jewelry
    val DifficultyBeginnerDark = Color(0xFF8B6914)
    val DifficultyIntermediate = Color(0xFF26648B)    // Lapis Lazuli — the prized deep blue
    val DifficultyIntermediateDark = Color(0xFF133245)
    val DifficultyAdvanced = Color(0xFFA63A28)        // Carnelian — semi-precious red stone
    val DifficultyAdvancedDark = Color(0xFF5C1F16)
}
