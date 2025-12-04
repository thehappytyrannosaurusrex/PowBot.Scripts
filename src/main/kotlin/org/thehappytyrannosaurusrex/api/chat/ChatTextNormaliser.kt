package org.thehappytyrannosaurusrex.api.chat

/**
 * Shared text helpers for parsing NPC / chat dialogue.
 */
object ChatTextNormaliser {

    /**
 * Strip <col=...> and corresponding </col> tags.
 */
    fun stripColorTags(text: String): String {
        return text
            .replace(Regex("""(?i)<\s*col\s*=\s*[^>]+>"""), "")
            .replace(Regex("""(?i)</\s*col\s*>"""), "")
    }

    /**
 * Replace <br> / <br/> tags with a single space.
 */
    fun replaceBreakTagsWithSpaces(text: String): String {
        return text.replace(Regex("""(?i)<br\s*/?>"""), " ")
    }

    /**
 * Convert “curly” quotes to plain ASCII quotes.
 */
    fun normaliseQuotesToAscii(text: String): String {
        return text
            .replace("‘", "'")
            .replace("’", "'")
            .replace("“", "\"")
            .replace("”", "\"")
    }

    /**
 * Collapse runs of whitespace to a single space and trim ends.
 */
    fun normaliseWhitespace(text: String): String {
        return text
            .replace("\\s+".toRegex(), " ")
            .trim()
    }

    /**
 * Normalise NPC dialogue for pattern matching:
 */
    fun normaliseNpcText(message: String): String {
        val noColors = stripColorTags(message)
        val noBreaks = replaceBreakTagsWithSpaces(noColors)
        val asciiQuotes = normaliseQuotesToAscii(noBreaks)

        return normaliseWhitespace(asciiQuotes.lowercase())
    }

    /**
 * Last thing between quotes ("..." or '...') in a line of dialogue.
 */
    fun extractLastQuotedSegment(message: String): String? {
        val quotePattern = Regex("[\"“']([^\"”']+)[\"”']")
        val matches = quotePattern.findAll(message).toList()
        if (matches.isEmpty()) return null

        val last = matches.last()
        val inner = last.groupValues.getOrNull(1)?.trim().orEmpty()
        if (inner.isEmpty()) return null

        return replaceBreakTagsWithSpaces(inner).trim()
    }
}
