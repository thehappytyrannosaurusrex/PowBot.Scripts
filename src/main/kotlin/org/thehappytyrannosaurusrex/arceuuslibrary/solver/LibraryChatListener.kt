package org.thehappytyrannosaurusrex.arceuuslibrary.solver

import org.thehappytyrannosaurusrex.arceuuslibrary.data.Books

// Represents "interesting" chat lines related to the Arceuus Library.
sealed class LibraryChatListener {

    // " find: <col=...>Book Name</col>."
    data class ShelfBookFound(
        val book: Books,
        val rawTitle: String
    ) : LibraryChatEvent()

    // " don't find anything useful here." / " find nothing of interest here."
    object ShelfEmpty : LibraryChatEvent()

    // NPC asks for a book. store:
    // - npcName: if can infer it (optional for now)
    // - rawTitle: the quoted title in the chat
    // - book: resolved Books enum if can map it (can be null)
    data class CustomerRequested(
        val npcName: String?,
        val rawTitle: String,
        val book: Books?
    ) : LibraryChatEvent()
}
