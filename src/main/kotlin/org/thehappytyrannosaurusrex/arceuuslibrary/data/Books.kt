/*
 * Project: Arceuus Library Script (PowBot)
 * File: Books.kt
 * Purpose: Added review header and standardized logging to Logger.info.
 * Notes: Generated comments + logging normalization on 2025-11-12.
 */

package org.thehappytyrannosaurusrex.arceuuslibrary.data
import org.thehappytyrannosaurusrex.arceuuslibrary.utils.Logger

import java.text.Normalizer

enum class Books(
    val displayName: String,
    val requestTitle: String,
    val itemId: Int
) {
    KILLING_OF_A_KING(
        "Killing of a King",
        "Killing of a King, by Griselle.",
        13527
    ),
    IDEOLOGY_OF_DARKNESS(
        "Ideology of Darkness",
        "The Ideology of Darkness, by Philophaire.",
        13532
    ),
    RADAS_JOURNEY(
        "Rada’s Journey",
        "The Journey of Rada, by Griselle.",
        13533
    ),
    TRANSVERGENCE_THEORY(
        "Transvergence Theory",
        "The Theory of Transvergence, by Amon Ducot.",
        13534
    ),
    TRISTESSAS_TRAGEDY(
        "Tristessa’s Tragedy",
        "The Tragedy of Tristessa.",
        13535
    ),
    RADAS_CENSUS(
        "Rada’s Census",
        "Census of King Rada III, by Matthias Vorseth.",
        13524
    ),
    TREACHERY_OF_ROYALTY(
        "Treachery of Royalty",
        "The Treachery of Royalty, by Professor Answith.",
        13536
    ),
    HOSIDIUS_LETTER(
        "Hosidius Letter",
        "A letter from Lord Hosidius to the Council of Elders.",
        13528
    ),
    RICKTORS_DIARY_7(
        "Ricktor’s Diary 7",
        "Diary of Steklan Ricktor, volume 7.",
        13525
    ),
    EATHRAM_RADA_EXTRACT(
        "Eathram Rada Extract",
        "An extract from Eathram & Rada, by Anonymous.",
        13526
    ),
    VARLAMORE_ENVOY(
        "Varlamore Envoy",
        "The Envoy to Varlamore, by Deryk Paulson.",
        21756
    ),
    WINTERTODT_PARABLE(
        "Wintertodt Parable",
        "The Parable of the Wintertodt, by Anonymous.",
        13529
    ),
    TWILL_ACCORD(
        "Twill Accord",
        "The Royal Accord of Twill.",
        13530
    ),
    BYRNES_CORONATION_SPEECH(
        "Byrne’s Coronation Speech",
        "Speech of King Byrne I, on the occasion of his coronation.",
        13531
    ),
    SOUL_JOURNEY(
        "Soul Journey",
        "The Journey of Souls, by Aretha.",
        19637
    ),
    TRANSPORTATION_INCANTATIONS(
        "Transportation Incantations",
        "Transportation Incantations, by Amon Ducot.",
        13537
    );

    companion object {

        fun fromItemId(itemId: Int?): Books? =
            if (itemId == null) null else values().firstOrNull { it.itemId == itemId }

        fun fromInventoryName(name: String?): Books? {
            if (name.isNullOrBlank()) return null
            val c = canon(name)
            return values().firstOrNull { canon(it.displayName) == c }
        }

        fun fromDisplayName(name: String?): Books? {
            if (name.isNullOrBlank()) return null
            val c = canon(name)

            // Try the short inventory name first
            values().firstOrNull { canon(it.displayName) == c }?.let { return it }

            // Then try the full NPC request title
            values().firstOrNull { canon(it.requestTitle) == c }?.let { return it }

            // Finally, try request title without the ", by ..." author clause
            values().firstOrNull {
                val req = canon(it.requestTitle)
                val reqNoBy = req.substringBefore(" by ").trim()
                reqNoBy == c
            }?.let { return it }

            return null
        }

        fun fromChatTitle(raw: String?): Books? {
            if (raw.isNullOrBlank()) return null

            // 1) Clean HTML-ish tags & quotes
            var s = stripColorTagsAndQuotes(raw)

            // 2) Remove trailing author clause: ", by <anything>"
            s = s.replace(Regex("""(?i),\s*by\s+[^.]+$"""), "").trim()

            // 3) Canonical form
            val c = canon(s)

            // 4) Direct match on displayName
            values().firstOrNull { canon(it.displayName) == c }?.let { return it }

            // 5) Match on full request title
            values().firstOrNull { canon(it.requestTitle) == c }?.let { return it }

            // 6) Match on request title (without author clause)
            values().firstOrNull {
                val req = canon(it.requestTitle)
                val reqNoBy = req.substringBefore(" by ").trim()
                req == c || reqNoBy == c
            }?.let { return it }

            // 7) Known alias forms seen in chat (long-form etc.)
            ALIASES[c]?.let { return it }

            // 8) Final soft fallback: remove leading articles and retry displayName
            val cNoArticle = c.removePrefix("the ").removePrefix("a ").removePrefix("an ").trim()
            values().firstOrNull { canon(it.displayName) == cNoArticle }?.let { return it }

            return null
        }

        fun fromRequestedTitle(raw: String?): Books? {
            if (raw.isNullOrBlank()) return null
            val stripped = stripColorTagsAndQuotes(raw)
            val c = canon(stripped)

            // Try exact requested titles first
            values().firstOrNull { canon(it.requestTitle) == c }?.let { return it }

            // Fallback: try matching on the part before ", by ..."
            val beforeBy = c.substringBefore(", by").trim()
            return values().firstOrNull {
                val req = canon(it.requestTitle)
                val reqBeforeBy = req.substringBefore(", by").trim()
                req == c || reqBeforeBy == c || reqBeforeBy == beforeBy
            }
        }

        fun allItemIds(): IntArray = values().map { it.itemId }.toIntArray()

        // ---------- Helpers ----------

        private fun stripColorTagsAndQuotes(s: String): String =
            s.replace(Regex("""(?i)<\s*col\s*=\s*[^>]+>"""), "") // any <col=...>
                .replace(Regex("""(?i)</\s*col\s*>"""), "")       // closing </col>
                .replace("‘", "")
                .replace("’", "")
                .replace("“", "")
                .replace("”", "")
                .replace("\"", "")
                .trim()

        private fun canon(s: String): String {
            // 1) remove colour tags (if any) and enclosing quotes
            val noTags = stripColorTagsAndQuotes(s)

            // 2) normalise diacritics, unify apostrophes
            val decomposed = Normalizer.normalize(noTags, Normalizer.Form.NFD)
                .replace(Regex("\\p{M}+"), "")
                .replace("’", "'")

            // 3) lowercase and delete punctuation except letters/digits/spaces/&
            val lowered = decomposed.lowercase()
            val cleaned = lowered
                .replace("&", " and ")
                .replace(Regex("""[^a-z0-9\s]"""), " ")
                .replace(Regex("""\s+"""), " ")
                .trim()

            return cleaned
        }

        // Canonical string → Book for long/alternate forms commonly seen in chat.
        private val ALIASES: Map<String, Books> by lazy {
            fun key(s: String) = canon(s)
            mapOf(
                // Explicit long-form speech title
                key("Speech of King Byrne I, on the occasion of his coronation") to BYRNES_CORONATION_SPEECH,

                // Long → short equivalences found in requests/chat
                key("The Journey of Rada") to RADAS_JOURNEY,
                key("The Theory of Transvergence") to TRANSVERGENCE_THEORY,
                key("The Tragedy of Tristessa") to TRISTESSAS_TRAGEDY,
                key("Census of King Rada III") to RADAS_CENSUS,
                key("A letter from Lord Hosidius to the Council of Elders") to HOSIDIUS_LETTER,
                key("Diary of Steklan Ricktor, volume 7") to RICKTORS_DIARY_7,
                key("An extract from Eathram and Rada") to EATHRAM_RADA_EXTRACT,
                key("The Envoy to Varlamore") to VARLAMORE_ENVOY,
                key("The Parable of the Wintertodt") to WINTERTODT_PARABLE,
                key("The Royal Accord of Twill") to TWILL_ACCORD,
                key("The Journey of Souls") to SOUL_JOURNEY,

                // Sometimes chat shows the same name without leading articles
                key("Ideology of Darkness") to IDEOLOGY_OF_DARKNESS,
                key("Killing of a King") to KILLING_OF_A_KING,
                key("Treachery of Royalty") to TREACHERY_OF_ROYALTY,
                key("Transportation Incantations") to TRANSPORTATION_INCANTATIONS
            )
        }
    }
}