package com.wadjet.core.common

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Comprehensive tests for [EgyptianPronunciation].
 * Covers: WORD_MAP entries, PHONEME_MAP entries, vowel epenthesis, tokenizer
 * edge cases, and the "already pronounceable" detection.
 */
class EgyptianPronunciationTest {

    // ── Helper ──

    private fun assertSpeech(expected: String, input: String) {
        assertEquals("toSpeech(\"$input\")", expected, EgyptianPronunciation.toSpeech(input))
    }

    // ══════════════════════════════════════════════════
    //  T008 — WORD_MAP exhaustive verification
    // ══════════════════════════════════════════════════

    // ─── Gods & divine names ───

    @Test fun `nTr produces netjer`() = assertSpeech("netjer", "nTr")
    @Test fun `nTrt produces netcheret`() = assertSpeech("netcheret", "nTrt")
    @Test fun `nTrw produces netcheru`() = assertSpeech("netcheru", "nTrw")
    @Test fun `imn produces amun`() = assertSpeech("amun", "imn")
    @Test fun `imn-ra produces amun-ra`() = assertSpeech("amun-ra", "imn-ra")
    @Test fun `inpw produces anpu`() = assertSpeech("anpu", "inpw")
    @Test fun `wsjr produces weseer`() = assertSpeech("weseer", "wsjr")
    @Test fun `Ast produces aset`() = assertSpeech("aset", "Ast")
    @Test fun `DHwty produces djehuti`() = assertSpeech("djehuti", "DHwty")
    @Test fun `ptH produces ptah`() = assertSpeech("ptah", "ptH")
    @Test fun `stX produces setekh`() = assertSpeech("setekh", "stX")
    @Test fun `jtn produces aten`() = assertSpeech("aten", "jtn")
    @Test fun `xpri produces khepri`() = assertSpeech("khepri", "xpri")
    @Test fun `bAstt produces bastet`() = assertSpeech("bastet", "bAstt")
    @Test fun `mnTw produces montu`() = assertSpeech("montu", "mnTw")
    @Test fun `sbk produces sobek`() = assertSpeech("sobek", "sbk")
    @Test fun `Gbb produces geb`() = assertSpeech("geb", "Gbb")
    @Test fun `nwt produces noot`() = assertSpeech("noot", "nwt")
    @Test fun `Sw produces shoo`() = assertSpeech("shoo", "Sw")
    @Test fun `tfnwt produces tefnoot`() = assertSpeech("tefnoot", "tfnwt")
    @Test fun `wDAt produces wadjet`() = assertSpeech("wadjet", "wDAt")
    @Test fun `sxmt produces sekhmet`() = assertSpeech("sekhmet", "sxmt")
    @Test fun `Hwt-Hr produces hat-hor`() = assertSpeech("hat-hor", "Hwt-Hr")
    @Test fun `Xnmw produces khnum`() = assertSpeech("khnum", "Xnmw")
    @Test fun `mHyt produces mehit`() = assertSpeech("mehit", "mHyt")
    @Test fun `ra produces ra`() = assertSpeech("ra", "ra")

    // ─── Horus forms ───

    @Test fun `Hr produces hor`() = assertSpeech("hor", "Hr")
    @Test fun `Hr-Axty produces hor-akhti`() = assertSpeech("hor-akhti", "Hr-Axty")

    // ─── Royal & titles ───

    @Test fun `nsw produces nesu`() = assertSpeech("nesu", "nsw")
    @Test fun `bit produces beet`() = assertSpeech("beet", "bit")
    @Test fun `nsw-bit produces nesu-beet`() = assertSpeech("nesu-beet", "nsw-bit")
    @Test fun `Hm produces hem`() = assertSpeech("hem", "Hm")
    @Test fun `nb produces neb`() = assertSpeech("neb", "nb")
    @Test fun `nbt produces nebet`() = assertSpeech("nebet", "nbt")
    @Test fun `HqA produces heqa`() = assertSpeech("heqa", "HqA")
    @Test fun `sA produces sa`() = assertSpeech("sa", "sA")
    @Test fun `sAt produces sat`() = assertSpeech("sat", "sAt")
    @Test fun `sA-ra produces sa-ra`() = assertSpeech("sa-ra", "sA-ra")
    @Test fun `smr produces semer`() = assertSpeech("semer", "smr")
    @Test fun `Sps produces sheps`() = assertSpeech("sheps", "Sps")
    @Test fun `Spst produces shepset`() = assertSpeech("shepset", "Spst")
    @Test fun `iry-pat produces iri-pat`() = assertSpeech("iri-pat", "iry-pat")
    @Test fun `sS produces sesh`() = assertSpeech("sesh", "sS")
    @Test fun `twt produces tut`() = assertSpeech("tut", "twt")
    @Test fun `HAty-a produces hati-a`() = assertSpeech("hati-a", "HAty-a")

    // ─── Life & blessings ───

    @Test fun `anx produces ankh`() = assertSpeech("ankh", "anx")
    @Test fun `wDA produces wedja`() = assertSpeech("wedja", "wDA")
    @Test fun `snb produces seneb`() = assertSpeech("seneb", "snb")
    @Test fun `Htp produces hotep`() = assertSpeech("hotep", "Htp")
    @Test fun `Dd produces djed`() = assertSpeech("djed", "Dd")
    @Test fun `wAs produces was`() = assertSpeech("was", "wAs")
    @Test fun `mAat produces maat`() = assertSpeech("maat", "mAat")
    @Test fun `nfr produces nefer`() = assertSpeech("nefer", "nfr")
    @Test fun `nfrt produces neferet`() = assertSpeech("neferet", "nfrt")
    @Test fun `nfrw produces neferu`() = assertSpeech("neferu", "nfrw")

    // ─── Common vocabulary ───

    @Test fun `pr produces per`() = assertSpeech("per", "pr")
    @Test fun `aA produces aa`() = assertSpeech("aa", "aA")
    @Test fun `wsr produces weser`() = assertSpeech("weser", "wsr")
    @Test fun `mn produces men`() = assertSpeech("men", "mn")
    @Test fun `ms produces mes`() = assertSpeech("mes", "ms")
    @Test fun `msi produces mesi`() = assertSpeech("mesi", "msi")
    @Test fun `ib produces eeb`() = assertSpeech("eeb", "ib")
    @Test fun `kA produces ka`() = assertSpeech("ka", "kA")
    @Test fun `bA produces ba`() = assertSpeech("ba", "bA")
    @Test fun `Ax produces akh`() = assertSpeech("akh", "Ax")
    @Test fun `xpr produces kheper`() = assertSpeech("kheper", "xpr")
    @Test fun `sxm produces sekhem`() = assertSpeech("sekhem", "sxm")
    @Test fun `tp produces tep`() = assertSpeech("tep", "tp")
    @Test fun `xt produces khet`() = assertSpeech("khet", "xt")
    @Test fun `Hs produces hes`() = assertSpeech("hes", "Hs")
    @Test fun `Hw produces hoo`() = assertSpeech("hoo", "Hw")
    @Test fun `sjA produces sia`() = assertSpeech("sia", "sjA")
    @Test fun `smA produces sema`() = assertSpeech("sema", "smA")
    @Test fun `Hna produces hena`() = assertSpeech("hena", "Hna")
    @Test fun `mi produces mee`() = assertSpeech("mee", "mi")
    @Test fun `jrj produces eeri`() = assertSpeech("eeri", "jrj")
    @Test fun `sDm produces sedjem`() = assertSpeech("sedjem", "sDm")
    @Test fun `mAA produces maa`() = assertSpeech("maa", "mAA")
    @Test fun `jj produces ee-ee`() = assertSpeech("ee-ee", "jj")
    @Test fun `jw produces eew`() = assertSpeech("eew", "jw")
    @Test fun `nn produces nen`() = assertSpeech("nen", "nn")
    @Test fun `pw produces poo`() = assertSpeech("poo", "pw")

    // ─── Nature & cosmos ───

    @Test fun `tA produces ta`() = assertSpeech("ta", "tA")
    @Test fun `pt produces pet`() = assertSpeech("pet", "pt")
    @Test fun `mw produces moo`() = assertSpeech("moo", "mw")
    @Test fun `Hrt produces heret`() = assertSpeech("heret", "Hrt")
    @Test fun `dwAt produces duat`() = assertSpeech("duat", "dwAt")
    @Test fun `wbn produces weben`() = assertSpeech("weben", "wbn")
    @Test fun `hrw produces heru`() = assertSpeech("heru", "hrw")
    @Test fun `grH produces gereh`() = assertSpeech("gereh", "grH")
    @Test fun `rnpt produces renepet`() = assertSpeech("renepet", "rnpt")
    @Test fun `Axt produces akhet`() = assertSpeech("akhet", "Axt")
    @Test fun `prt produces peret`() = assertSpeech("peret", "prt")
    @Test fun `Smw produces shemu`() = assertSpeech("shemu", "Smw")

    // ─── Body ───

    @Test fun `rA produces ra`() = assertSpeech("ra", "rA")
    @Test fun `irt produces eeret`() = assertSpeech("eeret", "irt")
    @Test fun `Drt produces djeret`() = assertSpeech("djeret", "Drt")
    @Test fun `rd produces red`() = assertSpeech("red", "rd")
    @Test fun `Xrd produces khered`() = assertSpeech("khered", "Xrd")
    @Test fun `DrDr produces djerjer`() = assertSpeech("djerjer", "DrDr")

    // ─── Places ───

    @Test fun `kmt produces kemet`() = assertSpeech("kemet", "kmt")
    @Test fun `dSrt produces deshret`() = assertSpeech("deshret", "dSrt")
    @Test fun `tAwy produces tawy`() = assertSpeech("tawy", "tAwy")
    @Test fun `wAst produces waset`() = assertSpeech("waset", "wAst")
    @Test fun `jwnw produces eewnu`() = assertSpeech("eewnu", "jwnw")
    @Test fun `Abw produces abu`() = assertSpeech("abu", "Abw")
    @Test fun `Abdw produces abdu`() = assertSpeech("abdu", "Abdw")
    @Test fun `mdw produces medu`() = assertSpeech("medu", "mdw")

    // ─── Buildings & objects ───

    @Test fun `Hwt produces hut`() = assertSpeech("hut", "Hwt")
    @Test fun `Hmt produces hemet`() = assertSpeech("hemet", "Hmt")
    @Test fun `jmAx produces eemakh`() = assertSpeech("eemakh", "jmAx")
    @Test fun `wAD produces wadj`() = assertSpeech("wadj", "wAD")
    @Test fun `HqAt produces heqat`() = assertSpeech("heqat", "HqAt")
    @Test fun `sxmty produces sekhemti`() = assertSpeech("sekhemti", "sxmty")
    @Test fun `HDt produces hedjet`() = assertSpeech("hedjet", "HDt")
    @Test fun `xaw produces khau`() = assertSpeech("khau", "xaw")

    // ─── Compound phrases ───

    @Test fun `di-anx produces dee-ankh`() = assertSpeech("dee-ankh", "di-anx")
    @Test fun `anx-wDA-snb produces ankh-wedja-seneb`() =
        assertSpeech("ankh-wedja-seneb", "anx-wDA-snb")
    @Test fun `Htp-di-nsw produces hotep-dee-nesu`() =
        assertSpeech("hotep-dee-nesu", "Htp-di-nsw")
    @Test fun `mdw-nTr produces medu-netjer`() = assertSpeech("medu-netjer", "mdw-nTr")

    // ─── Multi-word phrases ───

    @Test fun `anx nfr Htp as multi-word phrase`() =
        assertSpeech("ankh nefer hotep", "anx nfr Htp")

    @Test fun `nsw-bit nfr produces nesu-beet nefer`() =
        assertSpeech("nesu-beet nefer", "nsw-bit nfr")

    // ─── Word with trailing dot stripped ───

    @Test fun `trailing dot is stripped for WORD_MAP lookup`() =
        assertSpeech("ankh", "anx.")

    // ══════════════════════════════════════════════════
    //  T009 — Single-character (uniliteral) sign pronunciation
    // ══════════════════════════════════════════════════

    // Single-character inputs are the alphabet-sign case. They get:
    //   • vowel letters (A, a, i, j, y, w) → mapped via WORD_MAP to Egyptological
    //     syllables (ah, ee, oo)
    //   • consonant letters → mapped via PHONEME_MAP, then "eh" appended so the
    //     TTS engine reads "deh" not the English letter name "dee"
    //   • always repeated twice so the engine produces clear Orus-voice audio
    //     instead of falling back to a generic voice on too-short content.

    @Test fun `phoneme A maps to ah repeated`() = assertSpeech("ah. ah.", "A")
    @Test fun `phoneme a maps to ah repeated`() = assertSpeech("ah. ah.", "a")
    @Test fun `phoneme i maps to ee repeated`() = assertSpeech("ee. ee.", "i")
    @Test fun `phoneme j maps to ee repeated`() = assertSpeech("ee. ee.", "j")
    @Test fun `phoneme y maps to ee repeated`() = assertSpeech("ee. ee.", "y")
    @Test fun `phoneme w maps to oo repeated`() = assertSpeech("oo. oo.", "w")
    @Test fun `phoneme b maps to beh repeated`() = assertSpeech("beh. beh.", "b")
    @Test fun `phoneme p maps to peh repeated`() = assertSpeech("peh. peh.", "p")
    @Test fun `phoneme f maps to feh repeated`() = assertSpeech("feh. feh.", "f")
    @Test fun `phoneme m maps to meh repeated`() = assertSpeech("meh. meh.", "m")
    @Test fun `phoneme n maps to neh repeated`() = assertSpeech("neh. neh.", "n")
    @Test fun `phoneme r maps to reh repeated`() = assertSpeech("reh. reh.", "r")
    @Test fun `phoneme h maps to heh repeated`() = assertSpeech("heh. heh.", "h")
    @Test fun `phoneme H maps to heh repeated`() = assertSpeech("heh. heh.", "H")
    @Test fun `phoneme x maps to kheh repeated`() = assertSpeech("kheh. kheh.", "x")
    @Test fun `phoneme X maps to kheh repeated`() = assertSpeech("kheh. kheh.", "X")
    @Test fun `phoneme z maps to zeh repeated`() = assertSpeech("zeh. zeh.", "z")
    @Test fun `phoneme s maps to seh repeated`() = assertSpeech("seh. seh.", "s")
    @Test fun `phoneme S maps to sheh repeated`() = assertSpeech("sheh. sheh.", "S")
    @Test fun `phoneme q maps to qeh repeated`() = assertSpeech("qeh. qeh.", "q")
    @Test fun `phoneme k maps to keh repeated`() = assertSpeech("keh. keh.", "k")
    @Test fun `phoneme g maps to geh repeated`() = assertSpeech("geh. geh.", "g")
    @Test fun `phoneme t maps to teh repeated`() = assertSpeech("teh. teh.", "t")
    @Test fun `phoneme T maps to cheh repeated`() = assertSpeech("cheh. cheh.", "T")
    @Test fun `phoneme d maps to deh repeated`() = assertSpeech("deh. deh.", "d")
    @Test fun `phoneme D maps to djeh repeated`() = assertSpeech("djeh. djeh.", "D")
    @Test fun `phoneme l maps to leh repeated`() = assertSpeech("leh. leh.", "l")

    // ══════════════════════════════════════════════════
    //  T010 — Vowel epenthesis tests
    // ══════════════════════════════════════════════════

    @Test fun `two consonants get epenthetic e`() {
        // "pr" → p + r → "per" (e inserted between consonants)
        // "pr" is in WORD_MAP ("per"), so use a word NOT in WORD_MAP
        // "fd" → f + d → "fed"
        assertSpeech("fed", "fd")
    }

    @Test fun `three consonants get two epenthetic vowels`() {
        // "fdk" → f + d + k → "fedek"
        assertSpeech("fedek", "fdk")
    }

    @Test fun `vowel sounds do NOT trigger epenthesis`() {
        // "Aw" → a + oo → "aoo" (no 'e' — both are vowel-sounds)
        assertSpeech("aoo", "Aw")
    }

    @Test fun `nfr fallback produces nefer`() {
        // "nfr" is in WORD_MAP, so test the expected output
        assertSpeech("nefer", "nfr")
    }

    @Test fun `Htp fallback produces hotep`() {
        assertSpeech("hotep", "Htp")
    }

    @Test fun `consonant-vowel-consonant no extra epenthesis`() {
        // "bAk" → b + a + k → "bak" (a is vowel, no insertion needed)
        assertSpeech("bak", "bAk")
    }

    @Test fun `long consonant cluster`() {
        // "nfrk" → n-f-r-k → "neferek" (e between each consonant pair)
        // But "nfr" is a WORD_MAP entry... use something not in map
        // "fdkp" → f + d + k + p → "fedekep"
        assertSpeech("fedekep", "fdkp")
    }

    @Test fun `w between consonants treated as vowel`() {
        // "bwn" → b + oo + n → "boon" (oo is vowel, no epenthesis around it)
        assertSpeech("boon", "bwn")
    }

    // ══════════════════════════════════════════════════
    //  Tokenizer — MdC_STRIP & edge cases
    // ══════════════════════════════════════════════════

    @Test fun `hyphens are stripped in fallback path`() {
        // Unknown compound: "fk-bd" → strips hyphen → f+k+b+d → "fekebed"
        assertSpeech("fekebed", "fk-bd")
    }

    @Test fun `digits are stripped`() {
        // "A40" → strips '4','0' → just "A" → "a"
        assertSpeech("a", "A40")
    }

    @Test fun `hash and ampersand stripped`() {
        assertSpeech("a", "A#")
        assertSpeech("a", "A&")
    }

    @Test fun `structural characters stripped`() {
        // "A." has its trailing dot stripped at WORD_MAP lookup → becomes "A"
        // which hits WORD_MAP["A"]="ah". Input length is 2 so no single-char
        // padding is applied.
        assertSpeech("ah", "A.")
        // These retain non-dot structural chars so input length > 1 → no padding,
        // convertWord strips them and emits the raw phoneme "a".
        assertSpeech("a", "A:")
        assertSpeech("a", "A=")
        assertSpeech("a", "A*")
        assertSpeech("a", "(A)")
        assertSpeech("a", "<A>")
        assertSpeech("a", "A!")
    }

    // ══════════════════════════════════════════════════
    //  "Already pronounceable" detection (per-word)
    // ══════════════════════════════════════════════════

    @Test fun `already pronounceable English text returned as-is`() =
        assertSpeech("hello world", "hello world")

    @Test fun `mixed MdC and English processes per word`() {
        // "ankh nTr" — "ankh" has no e/o/u... wait, it has no MdC e/o/u
        // Actually "ankh" does not contain e/o/u so it goes through convertWord
        // BUT it IS not in WORD_MAP (lowercase)... hmm
        // Let's test something clearer: "hello nTr"
        // "hello" contains 'e' and 'o' → returned as-is
        // "nTr" has no e/o/u → WORD_MAP lookup → "netjer"
        assertSpeech("hello netjer", "hello nTr")
    }

    @Test fun `word with u detected as pronounceable`() {
        // "museum" contains 'u' → returned as-is per-word
        // "nfr" → WORD_MAP → "nefer"
        assertSpeech("museum nefer", "museum nfr")
    }

    // ══════════════════════════════════════════════════
    //  Edge cases
    // ══════════════════════════════════════════════════

    @Test fun `empty string returns empty`() =
        assertSpeech("", "")

    @Test fun `blank string returns empty`() =
        assertSpeech("", "   ")

    @Test fun `whitespace trimmed`() =
        assertSpeech("ankh", "  anx  ")

    @Test fun `multiple spaces between words`() =
        assertSpeech("ankh nefer", "anx   nfr")

    @Test fun `single unknown character passes through with padding`() {
        // 'G' is not in PHONEME_MAP → kept as raw "G" as a single consonant
        // sound, then "eh" suffix + padding applied per single-char rule.
        assertSpeech("Geh. Geh.", "G")
    }

    // ══════════════════════════════════════════════════
    //  Constants
    // ══════════════════════════════════════════════════

    @Test fun `VOICE constant is Orus`() =
        assertEquals("Orus", EgyptianPronunciation.VOICE)

    @Test fun `CONTEXT constant is hieroglyph_pronunciation`() =
        assertEquals("hieroglyph_pronunciation", EgyptianPronunciation.CONTEXT)

    @Test fun `STYLE is non-empty`() {
        assert(EgyptianPronunciation.STYLE.isNotBlank())
    }
}
