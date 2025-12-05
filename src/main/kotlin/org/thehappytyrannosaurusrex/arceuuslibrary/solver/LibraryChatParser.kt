package org.thehappytyrannosaurusrex.arceuuslibrary.solver

import org.thehappytyrannosaurusrex.arceuuslibrary.data.Books
import org.thehappytyrannosaurusrex.api.chat.ChatSource
import org.thehappytyrannosaurusrex.api.utils.Logger
import java.text.Normalizer

object LibraryChatParser {

    private val REQUEST_PREFIXES = listOf(
        "i'm looking for:",
        "i think i'd like to read:",
        "have you found my book yet?",
        "have you found my book, human?",
        "you are very kind. please find:",
        "do you have the book i require, traveller?",
        "do you have the book i require, traveler?"
    )

    private val OTHER_CUSTOMER_LINES = listOf(
        "i'll grab you later when you're not busy helping someone else.",
        "i believe you are currently assisting another customer of this library. i shall not trouble you with my needs at this time.",
        "aren't you helping someone else at the moment? don't let me interrupt."
    )

    private val REWARD_LINES = listOf(
        "well, isn't that handy? thanks, human! you can have this other book that i don't want.",
        "that's handy, thanks, i'll get on with reading it. meanwhile, you can have this other book of mine - i won't be wanting to read it again.",
        "thanks, human. you can have this other book i don't want.",
        "thank you very much. in return, please accept a token of my thanks.",
        "what a marvellous coincidence. thank you very much, traveller. in return, please accept a token of my thanks.",
        "thanks, i'll get on with reading it. meanwhile you can have this other book of mine - i won't be wanting to read it again."
    )

    private val RECENTLY_HELPED_LINES = listOf(
        "thank you for finding my book. it is most interesting.",
        "thanks for finding the book. i'll have to think up what i need next.",
        "thanks for finding my book. i'm learning such a lot here."
    )

    fun parse(message: String, source: ChatSource): LibraryChatEvent? {
        if (message.isBlank()) return null

        // Shelf search results
        parseShelfBookFound(message)?.let { return it }
        parseShelfEmpty(message)?.let { return it }
        parseLayoutReset(message)?.let { return it }

        // NPC dialogue
        parseCustomerRequest(message)?.let { return it }
        parseNpcMetaDialogue(message)?.let { return it }

        return null
    }

    // --- Shelf parsing ---

    private fun parseShelfBookFound(message: String): LibraryChatEvent? {
        if (!message.startsWith("You find:", ignoreCase = true)) return null

        val afterPrefix = message.removePrefix("You find:").trim()
        val normalised = afterPrefix
            .replace("<br>", " ", ignoreCase = true)
            .replace("\\s+".toRegex(), " ")
            .trim()

        val colouredMatch = Regex("<col=[0-9a-fA-F]{6}>.*?</col>", RegexOption.IGNORE_CASE)
            .find(normalised)

        val titleSegment = colouredMatch?.value ?: normalised

        var book = Books.fromChatTitle(titleSegment)
        if (book == null) {
            val plainTitle = titleSegment
                .replace(Regex("<col=[0-9a-fA-F]{6}>", RegexOption.IGNORE_CASE), "")
                .replace("</col>", "", ignoreCase = true)
                .trim()
            book = Books.fromChatTitle(plainTitle)
        }

        if (book == null) {
            Logger.warn("[ChatParser] Could not resolve book: '$titleSegment'")
            return null
        }

        return LibraryChatEvent.ShelfBookFound(book, titleSegment)
    }

    private fun parseShelfEmpty(message: String): LibraryChatEvent? {
        val lower = message.lowercase()
        if (lower.contains("don't find anything useful") || lower.contains("find nothing of interest")) {
            return LibraryChatEvent.ShelfEmpty
        }
        return null
    }

    private fun parseLayoutReset(message: String): LibraryChatEvent? {
        val lower = message.lowercase()
        if (lower.contains("books in the library have been reshuffled") ||
            lower.contains("layout has changed")) {
            return LibraryChatEvent.LayoutReset
        }
        return null
    }

    // --- NPC dialogue parsing ---

    private fun parseCustomerRequest(message: String): LibraryChatEvent? {
        val norm = normaliseNpcText(message)
        val looksLikeRequest = REQUEST_PREFIXES.any { norm.startsWith(it) }
        if (!looksLikeRequest) return null

        val rawTitle = extractQuotedTitle(message)
        if (rawTitle == null) {
            Logger.warn("[ChatParser] Could not extract title from: '$message'")
            return null
        }

        val book = Books.fromRequestedTitle(rawTitle)
        return LibraryChatEvent.CustomerRequested(rawTitle, book)
    }

    private fun parseNpcMetaDialogue(message: String): LibraryChatEvent? {
        val norm = normaliseNpcText(message)

        OTHER_CUSTOMER_LINES.forEach { line ->
            if (norm.contains(line)) {
                return LibraryChatEvent.NpcMetaDialogue(LibraryChatEvent.NpcMetaKind.BUSY_WITH_OTHER_CUSTOMER)
            }
        }

        REWARD_LINES.forEach { line ->
            if (norm.contains(line)) {
                return LibraryChatEvent.NpcMetaDialogue(LibraryChatEvent.NpcMetaKind.REWARD_TOKEN)
            }
        }

        RECENTLY_HELPED_LINES.forEach { line ->
            if (norm.contains(line)) {
                return LibraryChatEvent.NpcMetaDialogue(LibraryChatEvent.NpcMetaKind.RECENTLY_HELPED_ALREADY)
            }
        }

        return null
    }

    // --- Helpers ---

    private fun extractQuotedTitle(message: String): String? {
        val colourMatch = Regex("<col=[0-9a-fA-F]{6}>(.*?)</col>", RegexOption.IGNORE_CASE)
            .find(message)
        if (colourMatch != null) {
            return colourMatch.groupValues[1].trim()
        }

        val quoteMatch = Regex("\"([^\"]+)\"").find(message)
        return quoteMatch?.groupValues?.get(1)?.trim()
    }

    private fun normaliseNpcText(s: String): String {
        val noTags = s
            .replace(Regex("<col=[0-9a-fA-F]{6}>", RegexOption.IGNORE_CASE), "")
            .replace("</col>", "", ignoreCase = true)
            .replace("<br>", " ", ignoreCase = true)
            .replace("'", "'")
            .replace("'", "'")

        val decomposed = Normalizer.normalize(noTags, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")

        return decomposed.lowercase().replace(Regex("\\s+"), " ").trim()
    }
}