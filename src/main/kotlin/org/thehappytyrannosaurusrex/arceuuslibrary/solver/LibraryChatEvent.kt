package org.thehappytyrannosaurusrex.arceuuslibrary.solver

import org.thehappytyrannosaurusrex.arceuuslibrary.data.Books

sealed class LibraryChatEvent {

    data class ShelfBookFound(
        val book: Books,
        val rawTitle: String
    ) : LibraryChatEvent()

    object ShelfEmpty : LibraryChatEvent()

    object LayoutReset : LibraryChatEvent()

    data class CustomerRequested(
        val rawTitle: String,
        val book: Books?  // null when title couldn't be resolved
    ) : LibraryChatEvent()

    enum class PlayerReplyKind {
        ACCEPTED_REQUEST,
        NO_BOOK_YET,
        HAS_BOOK_NOW
    }

    enum class NpcMetaKind {
        BUSY_WITH_OTHER_CUSTOMER,
        REWARD_TOKEN,
        RECENTLY_HELPED_ALREADY
    }

    data class NpcMetaDialogue(
        val kind: NpcMetaKind
    ) : LibraryChatEvent()
}