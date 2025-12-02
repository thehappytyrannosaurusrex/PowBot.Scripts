package org.thehappytyrannosaurusrex.arceuuslibrary.solver

import org.thehappytyrannosaurusrex.arceuuslibrary.data.Books
import org.thehappytyrannosaurusrex.api.chat.ChatSource


// All interesting chat / server events related to the Arceuus Library.
sealed class LibraryChatEvent {

    // Searched a shelf and found a specific book.
    data class ShelfBookFound(
        val book: Books,
        val rawTitle: String
    ) : LibraryChatEvent()

    // Searched a shelf and found nothing useful.
    object ShelfEmpty : LibraryChatEvent()

    // Layout reset (books shuffled).
    object LayoutReset : LibraryChatEvent()

    // NPC asks / reminds about a specific book.
    data class CustomerRequested(
        val rawTitle: String,
        val book: Books?      // null when we couldn't resolve the title.
    ) : LibraryChatEvent()

    // Player reply kinds kept for potential future use, but no longer emit
    // PlayerReplyToRequest events from the parser.
    enum class PlayerReplyKind {
        ACCEPTED_REQUEST,   // "I'll see what I can do."
        NO_BOOK_YET,        // "Not yet, sorry."
        HAS_BOOK_NOW        // "I just happen to have a copy with me."
    }

    // NPC meta-dialogue around requests / rewards.
    enum class NpcMetaKind {
        BUSY_WITH_OTHER_CUSTOMER,   // "I'll grab you later..." / already helping someone else.
        REWARD_TOKEN,               // Book of Arcane Knowledge / token of thanks dialogue.
        RECENTLY_HELPED_ALREADY     // "Thanks for finding my book..." (cooldown / can't help twice).
    }

    data class NpcMetaDialogue(
        val kind: NpcMetaKind
    ) : LibraryChatEvent()
}
