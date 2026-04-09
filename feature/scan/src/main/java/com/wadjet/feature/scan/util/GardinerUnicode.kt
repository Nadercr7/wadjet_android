package com.wadjet.feature.scan.util

/**
 * Convert a Gardiner code (e.g. "G1", "D21", "M17") to its Unicode hieroglyph character.
 *
 * Unicode Egyptian Hieroglyphs block: U+13000–U+1342E
 * Unicode names follow pattern "EGYPTIAN HIEROGLYPH X001" (zero-padded to 3 digits).
 */
fun gardinerToUnicode(gardinerCode: String): String {
    // Common mappings for the most frequently detected signs
    COMMON_GLYPHS[gardinerCode]?.let { return it }

    // Algorithmic fallback: parse prefix+number, build Unicode name
    val match = GARDINER_REGEX.matchEntire(gardinerCode) ?: return gardinerCode
    val prefix = match.groupValues[1].uppercase()
    val number = match.groupValues[2].toIntOrNull() ?: return gardinerCode
    val suffix = match.groupValues[3]
    val paddedName = "$prefix${number.toString().padStart(3, '0')}$suffix"

    return UNICODE_MAP[paddedName]?.let { String(Character.toChars(it)) } ?: gardinerCode
}

private val GARDINER_REGEX = Regex("^([A-Za-z]+?)(\\d+)([A-Z]?)$")

// Pre-built map from Unicode standard (13000–1342E)
private val UNICODE_MAP: Map<String, Int> by lazy {
    val map = mutableMapOf<String, Int>()
    for (cp in 0x13000..0x1342E) {
        val name = try {
            Character.getName(cp)
        } catch (_: Exception) {
            null
        } ?: continue
        if (name.startsWith("EGYPTIAN HIEROGLYPH ")) {
            val code = name.removePrefix("EGYPTIAN HIEROGLYPH ")
            map[code] = cp
        }
    }
    map
}

// Most common signs for quick lookup
private val COMMON_GLYPHS = mapOf(
    "A1" to "\uD80C\uDC00",     // 𓀀 Seated man
    "A2" to "\uD80C\uDC01",     // 𓀁
    "D1" to "\uD80C\uDC81",     // 𓂁 Head
    "D4" to "\uD80C\uDC84",     // 𓂄 Eye
    "D21" to "\uD80C\uDC95",    // 𓂕 Mouth
    "D36" to "\uD80C\uDCA4",    // 𓂤 Forearm
    "D46" to "\uD80C\uDCAE",    // 𓂮 Hand
    "D58" to "\uD80C\uDCBA",    // 𓂺 Foot
    "G1" to "\uD80C\uDD80",     // 𓆀 Vulture
    "G5" to "\uD80C\uDD84",     // Falcon
    "G17" to "\uD80C\uDD90",    // Owl
    "G43" to "\uD80C\uDDAA",    // Quail chick
    "I9" to "\uD80C\uDE09",     // Horned viper
    "I10" to "\uD80C\uDE0A",    // Cobra
    "M17" to "\uD80C\uDE51",    // Reed
    "M23" to "\uD80C\uDE57",    // Sedge
    "N1" to "\uD80C\uDEC0",     // Sky
    "N5" to "\uD80C\uDEC4",     // Sun
    "N29" to "\uD80C\uDEDD",    // Sand hill
    "N35" to "\uD80C\uDEE3",    // Water
    "O1" to "\uD80C\uDF00",     // House
    "O4" to "\uD80C\uDF03",     // Shelter
    "O34" to "\uD80C\uDF22",    // Door bolt
    "Q3" to "\uD80C\uDFE3",     // Stool
    "R4" to "\uD80C\uE004",     // Hotep
    "S29" to "\uD80C\uE05D",    // Folded cloth
    "S34" to "\uD80C\uE062",    // Ankh
    "T14" to "\uD80C\uE08E",    // Throwstick
    "U1" to "\uD80C\uE0C0",     // Sickle
    "V13" to "\uD80C\uE12D",    // Tethering rope
    "V28" to "\uD80C\uE13C",    // Wick
    "V31" to "\uD80C\uE13F",    // Basket
    "W11" to "\uD80C\uE16B",    // Jar stand
    "X1" to "\uD80C\uE181",     // Bread loaf
    "Y1" to "\uD80C\uE1C0",     // Papyrus roll
    "Z1" to "\uD80C\uE1E1",     // Single stroke
    "Z4" to "\uD80C\uE1E4",     // Dual strokes
    "Aa1" to "\uD80C\uE200",    // Placenta?
)
