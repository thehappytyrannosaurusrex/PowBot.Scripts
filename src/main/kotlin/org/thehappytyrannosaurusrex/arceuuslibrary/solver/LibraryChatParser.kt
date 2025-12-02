package org.thehappytyrannosaurusrex.arceuuslibrary.solver

import org.thehappytyrannosaurusrex.arceuuslibrary.data.Books
import org.thehappytyrannosaurusrex.api.utils.Logger
import org.thehappytyrannosaurusrex.api.chat.ChatTextUtils
import org.thehappytyrannosaurusrex.api.chat.ChatSource

object LibraryChatParser {

    // -------------------------------------------------------------------------
    // Public entry point
    // -------------------------------------------------------------------------

    // Public entry point. pass the raw message and where it came from.
    fun parse(message: String?, source: ChatSource): LibraryChatEvent? {
        if (message.isNullOrBlank()) return null
        val m = message.trim()

        return when (source) {
            ChatSource.SERVER -> parseServerMessage(m)
            ChatSource.CHAT -> parseChatMessage(m)
        }
    }

    // -------------------------------------------------------------------------
    // SERVER MESSAGES (MessageEvent)
    // -------------------------------------------------------------------------

    private fun parseServerMessage(message: String): LibraryChatEvent? {
        val lower = message.lowercase()

        // " don't find anything useful here."
        // Handle both straight and curly apostrophes just in case.
        if (
            "you don't find anything useful here" in lower ||
            "you don’t find anything useful here" in lower
        ) {
            return LibraryChatEvent.ShelfEmpty
        }

        // Layout reset
        if (lower.contains("you hear the shifting of books") &&
            lower.contains("mysterious force")
        ) {
            return LibraryChatEvent.LayoutReset
        }

        return null
    }


    // -------------------------------------------------------------------------
    // CHAT MESSAGES (Chat box)
    // -------------------------------------------------------------------------

    private fun parseChatMessage(message: String): LibraryChatEvent? {
        // Keep shelf detection exactly as before.
        parseShelfBookFound(message)?.let { return it }

        // NPC request (with book title).
        parseCustomerRequest(message)?.let { return it }

        // NPC meta-dialogue: busy / reward / recently helped.
        parseNpcMetaDialogue(message)?.let { return it }

        // No longer care about player dialogue (player replies).
        return null
    }

    // " find: <col=00007f>BOOK NAME AND AUTHOR.</col> take a copy."
    // " find: <col=00007f>BOOK NAME AND AUTHOR.</col> already have a copy."
    private fun parseShelfBookFound(message: String): LibraryChatEvent? {
        if (!message.startsWith("You find:", ignoreCase = true)) return null

        // Strip the leading " find:".
        val afterPrefix = message.removePrefix("You find:").trim()

        // Normalise PowBot formatting:
        // - Replace <br> with spaces
        // - Collapse multiple whitespace into a single space
        val normalised = afterPrefix
            .replace("<br>", " ", ignoreCase = true)
            .replace("\\s+".toRegex(), " ")
            .trim()

        // Try to isolate the first coloured segment: <col=xxxxxx>...</col>
        val colouredMatch = Regex(
            "<col=[0-9a-fA-F]{6}>.*?</col>",
            RegexOption.IGNORE_CASE
        ).find(normalised)

        // If find a coloured segment, that is title chunk. Otherwise, fall back to the whole normalised string.
        val titleSegment = colouredMatch?.value ?: normalised

        // First try: let Books.fromChatTitle handle the coloured segment as-is.
        var book = Books.fromChatTitle(titleSegment)

        // Fallback: strip the colour tags and try again.
        if (book == null) {
            val plainTitle = titleSegment
                .replace(Regex("<col=[0-9a-fA-F]{6}>", RegexOption.IGNORE_CASE), "")
                .replace("</col>", "", ignoreCase = true)
                .trim()

            book = Books.fromChatTitle(plainTitle)
        }

        if (book == null) {
            Logger.error(
                "[Arceuus Library] CHAT | Could not resolve book from chat line (titleSegment='$titleSegment', normalised='$normalised', raw='$message')"
            )
            return null
        }

        // Store the titleSegment as rawTitle so can see exactly what resolved from.
        return LibraryChatEvent.ShelfBookFound(book, titleSegment)
    }

    // -------------------------------------------------------------------------
    // NPC dialogue
    // -------------------------------------------------------------------------

    // NPC request dialogue prefixes (normalised, lower-case, ASCII quotes).
    private val REQUEST_PREFIXES = listOf(
        "i'm looking for:",
        "i think i'd like to read:",
        "have you found my book yet?",
        "have you found my book, human?",
        "you are very kind. please find:",
        "do you have the book i require, traveller?",
        // Just in case of US spelling:
        "do you have the book i require, traveler?"
    )

    // Dialogue of NPC when already have a request from another NPC.
    private val OTHER_CUSTOMER_LINES = listOf(
        "i'll grab you later when you're not busy helping someone else.",
        "i believe you are currently assisting another customer of this library. i shall not trouble you with my needs at this time.",
        "aren't you helping someone else at the moment? don't let me interrupt."
    )

    // Dialogue when ’re about to receive Book of Arcane Knowledge / token.
    private val REWARD_LINES = listOf(
        "well, isn't that handy? thanks, human! you can have this other book that i don't want.",
        "that's handy, thanks, i'll get on with reading it. meanwhile, you can have this other book of mine - i won't be wanting to read it again.",
        "thanks, human. you can have this other book i don't want.",
        "thank you very much. in return, please accept a token of my thanks.",
        "what a marvellous coincidence. thank you very much, traveller. in return, please accept a token of my thanks.",
        "thanks, i'll get on with reading it. meanwhile you can have this other book of mine - i won't be wanting to read it again."
    )

    // Dialogue when just recently fulfilled a request for them.
    private val RECENTLY_HELPED_LINES = listOf(
        "thank you for finding my book. it is most interesting.",
        "thanks for finding the book. i'll have to think up what i need next.",
        "thanks for finding my book. i'm learning such a lot here."
    )

    // NPC request lines with coloured, quoted titles.
    private fun parseCustomerRequest(message: String): LibraryChatEvent? {
        val norm = ChatTextUtils.normaliseNpcText(message)

        val looksLikeRequest = REQUEST_PREFIXES.any { prefix ->
            norm.startsWith(prefix)
        }

        if (!looksLikeRequest) return null

        val rawTitle = extractQuotedTitle(message)
        if (rawTitle == null) {
            Logger.error(
                "[Arceuus Library] CHAT | Could not extract requested book title from NPC line: '$message'"
            )
            return null
        }

        val book = Books.fromRequestedTitle(rawTitle)
        if (book == null) {
            Logger.error(
                "[Arceuus Library] CHAT | Could not resolve requested book from title: '$rawTitle' (message='$message')"
            )
        }

        return LibraryChatEvent.CustomerRequested(
            rawTitle = rawTitle,
            book = book
        )
    }

    // NPC meta dialogue: busy / reward / recently helped.
    private fun parseNpcMetaDialogue(message: String): LibraryChatEvent? {
        val norm = ChatTextUtils.normaliseNpcText(message)

        // Busy / already helping someone else.
        if (OTHER_CUSTOMER_LINES.any { line -> norm.startsWith(line) }) {
            return LibraryChatEvent.NpcMetaDialogue(
                LibraryChatEvent.NpcMetaKind.BUSY_WITH_OTHER_CUSTOMER
            )
        }

        // Reward / token of thanks / BoAK.
        if (REWARD_LINES.any { line -> norm.startsWith(line) }) {
            return LibraryChatEvent.NpcMetaDialogue(
                LibraryChatEvent.NpcMetaKind.REWARD_TOKEN
            )
        }

        // Recently helped / can't help twice in a row.
        if (RECENTLY_HELPED_LINES.any { line -> norm.startsWith(line) }) {
            return LibraryChatEvent.NpcMetaDialogue(
                LibraryChatEvent.NpcMetaKind.RECENTLY_HELPED_ALREADY
            )
        }

        return null
    }

    // -------------------------------------------------------------------------
    // -------------------------------------------------------------------------
// Helpers
// -------------------------------------------------------------------------

/**
 * Extract the book title from NPC dialogue lines that include titles either in
 */
private fun extractQuotedTitle(message: String): String? {
    // 1) Prefer the text inside the first <col=...>...</col> block.
    val colRegex = Regex(
        pattern = """(?i)<\s*col\s*=\s*[^>]+>(.*?)</\s*col\s*>""",
        options = setOf(RegexOption.DOT_MATCHES_ALL)
    )
    val colMatch = colRegex.find(message)
    if (colMatch != null) {
        val inner = colMatch.groupValues.getOrNull(1)?.trim().orEmpty()
        if (inner.isNotEmpty()) {
            // Normalise any <br> tags to spaces so titles match static definitions.
            return ChatTextUtils.replaceBreakTagsWithSpaces(inner).trim()
        }
    }

    // 2) Fallback: use the last quoted segment in the line. deliberately use the LAST
    // Match so that apostrophes in words like "'m" / "'d" don't get mistaken for
    // Quote delimiters around the title.
    val inner = ChatTextUtils.extractLastQuotedSegment(message) ?: return null
    return inner
}

}
