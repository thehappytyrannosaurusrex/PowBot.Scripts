package org.thehappytyrannosaurusrex.arceuuslibrary.solver

import org.thehappytyrannosaurusrex.arceuuslibrary.data.Books

// Where the message came from so we can treat server vs chat differently.
enum class ChatSource {
    SERVER,
    CHAT
}

// All interesting chat / server events related to the Arceuus Library.
sealed class LibraryChatEvent {

    // You searched a shelf and found a specific book.
    data class ShelfBookFound(
        val book: Books,
        val rawTitle: String
    ) : LibraryChatEvent()

    // You searched a shelf and found nothing useful.
    object ShelfEmpty : LibraryChatEvent()

    // Layout reset (books shuffled).
    object LayoutReset : LibraryChatEvent()

    // NPC asks / reminds you about a specific book.
    data class CustomerRequested(
        val rawTitle: String,
        val book: Books?
    ) : LibraryChatEvent()

    // Your player replies after a request.
    enum class PlayerReplyKind {
        ACCEPTED_REQUEST,   // "I'll see what I can do."
        NO_BOOK_YET,        // "Not yet, sorry."
        HAS_BOOK_NOW        // "I just happen to have a copy with me."
    }

    data class PlayerReplyToRequest(
        val kind: PlayerReplyKind
    ) : LibraryChatEvent()

    // NPC meta-dialogue around requests / rewards.
    enum class NpcMetaKind {
        BUSY_WITH_OTHER_CUSTOMER,   // "I'll grab you later when you're not busy..."
        REWARD_TOKEN,               // Book of Arcane Knowledge / token of thanks.
        RECENTLY_HELPED_ALREADY     // "Thanks for finding my book..." (cooldown).
    }

    data class NpcMetaDialogue(
        val kind: NpcMetaKind
    ) : LibraryChatEvent()
}
