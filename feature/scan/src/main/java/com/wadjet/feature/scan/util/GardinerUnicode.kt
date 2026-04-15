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

// Most common signs for quick lookup — codepoints verified against Unicode 15.1 standard
private val COMMON_GLYPHS: Map<String, String> = mapOf(
    "A1"  to 0x13000,  // Seated man
    "A2"  to 0x13001,  // Man with hand to mouth
    "D1"  to 0x13076,  // Head in profile
    "D4"  to 0x13079,  // Eye (Horus eye)
    "D21" to 0x1308B,  // Mouth
    "D36" to 0x1309D,  // Forearm
    "D46" to 0x130A7,  // Hand
    "D58" to 0x130C0,  // Foot
    "G1"  to 0x1313F,  // Vulture (aleph)
    "G5"  to 0x13143,  // Falcon
    "G17" to 0x13153,  // Owl
    "G43" to 0x13171,  // Quail chick
    "I9"  to 0x13191,  // Horned viper
    "I10" to 0x13193,  // Cobra
    "M17" to 0x131CB,  // Reed
    "M23" to 0x131D3,  // Sedge
    "N1"  to 0x131EF,  // Sky
    "N5"  to 0x131F3,  // Sun
    "N29" to 0x1320E,  // Sand hill
    "N35" to 0x13216,  // Water
    "O1"  to 0x13250,  // House
    "O4"  to 0x13254,  // Shelter
    "O34" to 0x13283,  // Door bolt
    "Q3"  to 0x132AA,  // Stool
    "R4"  to 0x132B5,  // Hotep
    "S29" to 0x132F4,  // Folded cloth
    "S34" to 0x132F9,  // Ankh
    "T14" to 0x13319,  // Throwstick
    "U1"  to 0x13333,  // Sickle
    "V13" to 0x1337F,  // Tethering rope
    "V28" to 0x1339B,  // Wick
    "V31" to 0x133A1,  // Basket
    "W11" to 0x133BC,  // Jar stand
    "X1"  to 0x133CF,  // Bread loaf
    "Y1"  to 0x133DB,  // Papyrus roll
    "Z1"  to 0x133E4,  // Single stroke
    "Z4"  to 0x133ED,  // Dual strokes
    "Aa1" to 0x1340D,  // Placenta
).mapValues { (_, cp) -> String(Character.toChars(cp)) }
