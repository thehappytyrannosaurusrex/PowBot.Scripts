package org.thehappytyrannosaurusrex.arceuuslibrary.solver

import org.thehappytyrannosaurusrex.arceuuslibrary.data.Books

sealed class LibraryChatEvent {

    // Found a book on a shelf
    data class ShelfBookFound(
        val book: Books,
        val rawTitle: String
    ) : LibraryChatEvent()

    // Shelf was empty
    object ShelfEmpty : LibraryChatEvent()

    // Layout reset (books shuffled)
    object LayoutReset : LibraryChatEvent()

    // NPC requested a book
    data class CustomerRequested(
        val rawTitle: String,
        val book: Books?
    ) : LibraryChatEvent()

    // NPC meta-dialogue types
    enum class NpcMetaKind {
        BUSY_WITH_OTHER_CUSTOMER,
        REWARD_TOKEN,
        RECENTLY_HELPED_ALREADY
    }

    data class NpcMetaDialogue(val kind: NpcMetaKind) : LibraryChatEvent()
}