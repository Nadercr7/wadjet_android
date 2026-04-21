package com.wadjet.core.common

/**
 * Converts Egyptological transliteration (Manuel de Codage / MdC ASCII) to
 * pronounceable English text optimized for TTS engines. Follows reconstructed
 * Middle Egyptian pronunciation conventions used in modern Egyptology.
 *
 * Three-level conversion:
 * 1. Known whole-word lookup — hand-tuned pronunciations for ~70 common words
 * 2. Phoneme-by-phoneme mapping — MdC special characters to English sounds
 * 3. Vowel epenthesis — inserts 'e' between consonant clusters per convention
 */
object EgyptianPronunciation {

    /** TTS voice for hieroglyphic content — Orus (voice of Thoth, god of writing). */
    const val VOICE = "Orus"

    /** TTS style instruction for authentic ancient Egyptian character. */
    const val STYLE =
        "Speak as a wise ancient Egyptian sage and master scribe of the House of Life. " +
            "Voice should be calm, measured, and deeply knowing — " +
            "like a scholar who has spent a lifetime studying sacred hieroglyphs. " +
            "Pronounce each word with quiet reverence and weight, " +
            "unhurried, as if sharing timeless wisdom passed down through millennia."

    /** Server context tag signaling hieroglyphic pronunciation mode. */
    const val CONTEXT = "hieroglyph_pronunciation"

    /**
     * Converts MdC transliteration to TTS-ready pronounceable text.
     * Safe to call on already-pronounceable text — detects non-MdC vowels
     * (e, o, u) and returns the input unchanged.
     *
     * @param transliteration MdC ASCII string, e.g. `"anx nfr Htp"`
     * @return Pronounceable English text, e.g. `"ankh nefer hetep"`
     */
    fun toSpeech(transliteration: String): String {
        if (transliteration.isBlank()) return ""
        val trimmed = transliteration.trim()
        val converted = trimmed
            .split(Regex("\\s+"))
            .joinToString(" ") { word ->
                val stripped = word.removeSuffix(".")
                WORD_MAP[stripped]
                    ?: WORD_MAP[word]
                    ?: if (stripped.any { it in "eouEOU" }) stripped   // already pronounceable
                    else convertWord(stripped)
            }
        // For single-character inputs (the alphabet-sign case), repeat the
        // utterance twice. This guarantees the TTS engine produces clear audio
        // in the configured Orus voice instead of either reading the raw
        // character as an English letter name ("d" → "dee") or falling back
        // to a generic voice due to too-little content.
        return if (trimmed.length == 1 && converted.isNotBlank()) {
            "$converted. $converted."
        } else {
            converted
        }
    }

    // ── Phoneme-level fallback for unknown words ──

    private fun convertWord(word: String): String {
        val tokens = tokenize(word)
        val sounds = tokens.map { PHONEME_MAP[it] ?: it }
        return buildString {
            for (i in sounds.indices) {
                append(sounds[i])
                if (i < sounds.lastIndex &&
                    isConsonantSound(sounds[i]) &&
                    isConsonantSound(sounds[i + 1])
                ) {
                    append('e')
                }
            }
            // For single-phoneme consonant inputs (alphabet signs like d, t, s),
            // append "eh" so the TTS engine reads a proper syllable ("deh")
            // instead of the English letter name ("dee"). Multi-phoneme words
            // and vowel-only inputs are unaffected — matching standard
            // Egyptological reading convention for uniliteral signs.
            if (sounds.size == 1 && sounds[0].isNotEmpty() && isConsonantSound(sounds[0])) {
                append("eh")
            }
        }
    }

    /** Greedy left-to-right tokenizer — strips MdC notation markers, keeps phonemes. */
    private fun tokenize(word: String): List<String> =
        word.filter { it !in MdC_STRIP }.map { it.toString() }

    /** Characters to strip: MdC structural notation markers, digits, hyphens, damaged-text markers. */
    private val MdC_STRIP = setOf(
        '.', ':', '=', '*', '(', ')', '<', '>', '!',
        '-',                                          // compound separator
        '#', '&',                                     // damaged-text / special-block markers
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', // Gardiner code digits
    )

    private val VOWEL_SOUNDS = setOf("a", "ee", "oo", "e")

    private fun isConsonantSound(sound: String): Boolean = sound !in VOWEL_SOUNDS

    // ── MdC single-character phoneme map ──

    private val PHONEME_MAP: Map<String, String> = mapOf(
        // Vowels & semi-vowels
        "A" to "a",    // ꜣ aleph — glottal stop, open "a"
        "a" to "a",    // ꜥ ayin — pharyngeal, "a"
        "i" to "ee",   // reed leaf — long "ee"
        "j" to "ee",   // reed leaf variant
        "y" to "ee",   // double reed — "ee"
        "w" to "oo",   // quail chick — "oo/u"
        // Consonants
        "b" to "b",
        "p" to "p",
        "f" to "f",
        "m" to "m",
        "n" to "n",
        "r" to "r",
        "h" to "h",    // shelter — plain "h"
        "H" to "h",    // wick — emphatic ḥ (pharyngeal, like Arabic ح)
        "x" to "kh",   // placenta — velar fricative (like Arabic خ / Scottish "loch")
        "X" to "kh",   // animal belly — palatal fricative
        "z" to "z",    // bolt — /z~s/ (merged with s in Middle Egyptian)
        "s" to "s",
        "S" to "sh",   // pool — "sh" (š)
        "q" to "q",    // slope — uvular stop (deep back of throat)
        "k" to "k",
        "g" to "g",
        "t" to "t",
        "T" to "ch",   // tether — "ch" as in "church" (ṯ)
        "d" to "d",
        "D" to "dj",   // snake — /dʒ/ as in "judge" (ḏ). Use "dj" (standard
                        // Egyptological digraph) instead of bare "j" because
                        // multilingual TTS voices read "j" as /j/ (the "y" sound)
                        // in German/Dutch-influenced models — causing "deh" to
                        // be heard as "yeh". "dj" forces the English /dʒ/ sound.
        "l" to "l",    // not standard 24 but appears in some texts
    )

    // ── Comprehensive word map: MdC → accepted Egyptological pronunciation ──

    @Suppress("SpellCheckingInspection")
    private val WORD_MAP: Map<String, String> = mapOf(
        // ─── Uniliteral vowels & semi-vowels (standard Egyptological reading) ───
        // Single-character overrides ensure TTS gets a full, unambiguous syllable
        // rather than a bare phoneme the engine would read as an English letter name.
        "A" to "ah",                // ꜣ aleph — open "a"
        "a" to "ah",                // ꜥ ayin — pharyngeal "a"
        "i" to "ee",                // reed leaf
        "j" to "ee",                // reed leaf variant
        "y" to "ee",                // double reed
        "w" to "oo",                // quail chick

        // ─── Gods & divine names ───
        "nTr" to "netjer",          // god
        "nTrt" to "netcheret",      // goddess
        "nTrw" to "netcheru",       // gods (plural)
        "imn" to "amun",            // Amun
        "imn-ra" to "amun-ra",      // Amun-Ra
        "inpw" to "anpu",           // Anubis
        "wsjr" to "weseer",         // Osiris
        "Ast" to "aset",            // Isis
        "DHwty" to "djehuti",       // Thoth
        "ptH" to "ptah",           // Ptah — Coptic ⲡⲗⲁⲳ confirms /ptah/ (universal spelling: Ptah)
        "stX" to "setekh",          // Seth
        "jtn" to "aten",            // Aten (sun disk)
        "xpri" to "khepri",        // Khepri (scarab god)
        "bAstt" to "bastet",        // Bastet
        "mnTw" to "montu",          // Montu (war god)
        "sbk" to "sobek",           // Sobek (crocodile god)
        "Gbb" to "geb",             // Geb (earth god)
        "nwt" to "noot",            // Nut (sky goddess)
        "Sw" to "shoo",             // Shu (air god)
        "tfnwt" to "tefnoot",       // Tefnut (moisture goddess)
        "wDAt" to "wadjet",         // Wadjet / Eye of Horus
        "sxmt" to "sekhmet",        // Sekhmet (lioness goddess)
        "Hwt-Hr" to "hat-hor",      // Hathor
        "Xnmw" to "khnum",          // Khnum (ram-headed god)
        "mHyt" to "mehit",           // Mehit (lioness goddess)
        "ra" to "ra",               // Ra (sun god)

        // ─── Horus forms ───
        "Hr" to "hor",              // Horus / face
        "Hr-Axty" to "hor-akhti",   // Horakhty

        // ─── Royal & titles ───
        "nsw" to "nesu",            // king (Upper Egypt)
        "bit" to "beet",            // bee (Lower Egypt)
        "nsw-bit" to "nesu-beet",   // King of Upper & Lower Egypt
        "Hm" to "hem",              // majesty
        "nb" to "neb",              // lord / all
        "nbt" to "nebet",           // lady
        "HqA" to "heqa",            // ruler
        "sA" to "sa",               // son / protection
        "sAt" to "sat",             // daughter
        "sA-ra" to "sa-ra",         // Son of Ra
        "smr" to "semer",           // courtier
        "Sps" to "sheps",           // noble
        "Spst" to "shepset",        // noble woman
        "iry-pat" to "iri-pat",     // hereditary prince
        "sS" to "sesh",              // scribe
        "twt" to "tut",              // image / statue
        "HAty-a" to "hati-a",        // mayor / count

        // ─── Life & blessings ───
        "anx" to "ankh",            // life
        "wDA" to "wedja",           // prosperity / flourish
        "snb" to "seneb",           // health
        "Htp" to "hotep",           // peace / offering — Coptic ⲩⲟⲗⲕ /hotp/ (universal: Amenhotep, Hotepsekhemwy)
        "Dd" to "djed",             // stability / endurance
        "wAs" to "was",             // power / dominion
        "mAat" to "maat",           // truth / justice / cosmic order
        "nfr" to "nefer",           // beautiful / good / perfect
        "nfrt" to "neferet",        // beautiful (feminine)
        "nfrw" to "neferu",         // beauty / perfection

        // ─── Common vocabulary ───
        "pr" to "per",              // house
        "aA" to "aa",               // great
        "wsr" to "weser",           // powerful
        "mn" to "men",              // established / enduring
        "ms" to "mes",              // born / child
        "msi" to "mesi",            // to give birth
        "ib" to "eeb",              // heart
        "kA" to "ka",               // spirit / life force
        "bA" to "ba",               // soul
        "Ax" to "akh",              // transfigured spirit
        "xpr" to "kheper",          // to become / scarab
        "sxm" to "sekhem",          // power / scepter
        "tp" to "tep",              // head / upon
        "xt" to "khet",             // thing / wood / fire
        "Hs" to "hes",              // praise / favor
        "Hw" to "hoo",              // divine utterance — "hu" would be read as "hyoo" in English TTS
        "sjA" to "sia",             // divine perception
        "smA" to "sema",            // to unite
        "Hna" to "hena",            // together / with
        "mi" to "mee",              // like / as — "mi" would be read as "my" (/maɪ/) by English TTS
        "jrj" to "eeri",            // to do / make
        "sDm" to "sedjem",          // to hear / listen
        "mAA" to "maa",             // to see / behold
        "jj" to "ee-ee",            // to come
        "jw" to "eew",              // is / are (particle)
        "nn" to "nen",              // these / this
        "pw" to "poo",              // this (demonstrative) — "pu" would be read as "pyoo" in English TTS

        // ─── Nature & cosmos ───
        "tA" to "ta",               // land / earth
        "pt" to "pet",              // sky / heaven
        "mw" to "moo",              // water — "mu" would be read as "myoo" (like Greek letter) in English TTS
        "Hrt" to "heret",           // sky above
        "dwAt" to "duat",           // underworld / Duat
        "wbn" to "weben",           // to rise / shine
        "hrw" to "heru",            // day
        "grH" to "gereh",           // night
        "rnpt" to "renepet",        // year
        "Axt" to "akhet",           // horizon / season of inundation
        "prt" to "peret",           // season of emergence
        "Smw" to "shemu",           // season of harvest

        // ─── Body ───
        "rA" to "ra",               // mouth
        "irt" to "eeret",           // eye
        "Drt" to "djeret",          // hand
        "rd" to "red",              // foot
        "Xrd" to "khered",          // child
        "DrDr" to "djerjer",         // very much / altogether

        // ─── Places ───
        "kmt" to "kemet",           // Black Land (Egypt)
        "dSrt" to "deshret",        // Red Land (desert)
        "tAwy" to "tawy",           // Two Lands (Upper & Lower Egypt)
        "wAst" to "waset",          // Thebes
        "jwnw" to "eewnu",          // Heliopolis
        "Abw" to "abu",             // Elephantine
        "Abdw" to "abdu",           // Abydos
        "mdw" to "medu",            // words / speech (as in medu netjer)

        // ─── Buildings & objects ───
        "Hwt" to "hut",             // temple / mansion
        "Hmt" to "hemet",           // wife / copper
        "jmAx" to "eemakh",         // revered / honored
        "wAD" to "wadj",             // papyrus scepter
        "HqAt" to "heqat",           // scepter of authority
        "sxmty" to "sekhemti",       // Double Crown
        "HDt" to "hedjet",           // White Crown
        "xaw" to "khau",             // crown / appearance

        // ─── Compound phrases ───
        "di-anx" to "dee-ankh",                  // given life
        "anx-wDA-snb" to "ankh-wedja-seneb",     // life prosperity health
        "Htp-di-nsw" to "hotep-dee-nesu",         // royal offering formula
        "mdw-nTr" to "medu-netjer",               // divine words (hieroglyphs)
    )
}
