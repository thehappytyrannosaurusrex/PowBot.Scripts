package org.thehappytyrannosaurusrex.arceuuslibrary.solver

import org.thehappytyrannosaurusrex.arceuuslibrary.data.Books
import org.thehappytyrannosaurusrex.api.utils.Logger

object LibraryChatParser {

    // Public entry point. You pass the raw message and where it came from.
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

        if ("you don’t find anything useful here" in lower ||
            "you don't find anything useful here" in lower
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
        parseShelfBookFound(message)?.let { return it }
        parseCustomerRequest(message)?.let { return it }
        parsePlayerReply(message)?.let { return it }
        parseNpcMetaDialogue(message)?.let { return it }

        return null
    }

    // "You find: <col=00007f>BOOK NAME AND AUTHOR.</col> You take a copy."
    // "You find: <col=00007f>BOOK NAME AND AUTHOR.</col> You already have a copy."
    private fun parseShelfBookFound(message: String): LibraryChatEvent? {
        if (!message.startsWith("You find:", ignoreCase = true)) return null

        // Strip the leading "You find:".
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

        // If we find a coloured segment, that is our title chunk. Otherwise, fall back to the whole normalised string.
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

        // We store the titleSegment as rawTitle so we can see exactly what we resolved from.
        return LibraryChatEvent.ShelfBookFound(book, titleSegment)
    }



    // NPC request lines with coloured, quoted titles.
    private fun parseCustomerRequest(message: String): LibraryChatEvent? {
        val lower = message.lowercase()

        val looksLikeRequest = listOf(
            "i’m looking for:",
            "i'm looking for:",
            "i think i’d like to read:",
            "i think i'd like to read:",
            "have you found my book yet?",
            "have you found my book, human?",
            "you are very kind. please find:",
            "do you have the book i require, traveller?",
            "do you have the book i require, traveler?"
        ).any { lower.contains(it) }

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

    // Player replies after a request.
    private fun parsePlayerReply(message: String): LibraryChatEvent? {
        val lower = message.lowercase()

        // Already have the requested book.
        if (lower.contains("i just happen to have a copy with me")) {
            return LibraryChatEvent.PlayerReplyToRequest(
                LibraryChatEvent.PlayerReplyKind.HAS_BOOK_NOW
            )
        }

        // Accepted request but doesn't have it yet.
        if (lower.contains("i’ll see what i can do") ||
            lower.contains("i'll see what i can do")
        ) {
            return LibraryChatEvent.PlayerReplyToRequest(
                LibraryChatEvent.PlayerReplyKind.ACCEPTED_REQUEST
            )
        }

        // Explicitly saying we don't have it yet.
        if (lower.contains("not yet, sorry")) {
            return LibraryChatEvent.PlayerReplyToRequest(
                LibraryChatEvent.PlayerReplyKind.NO_BOOK_YET
            )
        }

        return null
    }

    // NPC meta dialogue: busy / reward / recently helped.
    private fun parseNpcMetaDialogue(message: String): LibraryChatEvent? {
        val lower = message.lowercase()

        // Busy / already helping someone else.
        if (lower.contains("i’ll grab you later when you’re not busy helping someone else") ||
            lower.contains("i'll grab you later when you're not busy helping someone else") ||
            lower.contains("aren’t you helping someone else at the moment") ||
            lower.contains("aren't you helping someone else at the moment") ||
            lower.contains("i believe you are currently assisting") ||
            lower.contains("i believe you are currently assisting")
        ) {
            return LibraryChatEvent.NpcMetaDialogue(
                LibraryChatEvent.NpcMetaKind.BUSY_WITH_OTHER_CUSTOMER
            )
        }

        // Reward / token of thanks / BoAK.
        if (lower.contains("you can have this other book i don’t want") ||
            lower.contains("you can have this other book i don't want") ||
            lower.contains("you can have this other book of mine - i won’t be wanting to read it again") ||
            lower.contains("you can have this other book of mine - i won't be wanting to read it again") ||
            lower.contains("please accept a token of my thanks")
        ) {
            return LibraryChatEvent.NpcMetaDialogue(
                LibraryChatEvent.NpcMetaKind.REWARD_TOKEN
            )
        }

        // Recently helped / can't help twice in a row.
        if (lower.contains("thank you for finding my book. it is most interesting") ||
            lower.contains("thanks for finding the book. i’ll have to think up what i need next") ||
            lower.contains("thanks for finding the book. i'll have to think up what i need next") ||
            lower.contains("thanks for finding my book. i’m learning such a lot here") ||
            lower.contains("thanks for finding my book. i'm learning such a lot here")
        ) {
            return LibraryChatEvent.NpcMetaDialogue(
                LibraryChatEvent.NpcMetaKind.RECENTLY_HELPED_ALREADY
            )
        }

        return null
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    // First thing between quotes ("..." or '...').
    // Works with colour tags inside, e.g.
    // "I'm looking for:'<col=0000ff>The Journey of Rada, by Griselle</col>'."
    private fun extractQuotedTitle(message: String): String? {
        val pattern = Regex("[\"“']([^\"”']+)[\"”']")
        val match = pattern.find(message) ?: return null
        return match.groupValues.getOrNull(1)?.trim()
    }
}
