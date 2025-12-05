package org.thehappytyrannosaurusrex.arceuuslibrary.solver

import org.powbot.api.Tile
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Bookshelf
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Books
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Bookshelves
import org.thehappytyrannosaurusrex.api.utils.Logger

class LibrarySolver {

    // Maps shelf index to known book (null = unknown, or empty)
    private val shelfContents = mutableMapOf<Int, Books?>()

    // Track which shelves we've searched this layout
    private val searchedShelves = mutableSetOf<Int>()

    // Current sequence hypothesis (null = unknown)
    private var currentSequence: List<Books>? = null

    // Reset on layout change
    fun onLayoutReset() {
        Logger.info("[Solver] Layout reset detected; clearing state.")
        shelfContents.clear()
        searchedShelves.clear()
        currentSequence = null
    }

    // Record a book found at a shelf
    fun onBookFound(shelfIndex: Int, book: Books) {
        shelfContents[shelfIndex] = book
        searchedShelves.add(shelfIndex)
        Logger.info("[Solver] Found ${book.displayName} at shelf $shelfIndex")
        tryDetermineSequence()
    }

    // Record an empty shelf
    fun onShelfEmpty(shelfIndex: Int) {
        shelfContents[shelfIndex] = null
        searchedShelves.add(shelfIndex)
        Logger.info("[Solver] Shelf $shelfIndex is empty")
    }

    // Try to determine which sequence we're in based on found books
    private fun tryDetermineSequence() {
        val knownBooks = shelfContents.filterValues { it != null }.values.filterNotNull().toSet()
        if (knownBooks.size < 2) return

        val candidates = LibrarySequences.sequences.filter { seq ->
            knownBooks.all { it in seq }
        }

        when {
            candidates.isEmpty() -> {
                Logger.warn("[Solver] No sequence matches found books; layout may have changed.")
                currentSequence = null
            }
            candidates.size == 1 -> {
                currentSequence = candidates.first()
                Logger.info("[Solver] Sequence determined!")
            }
            else -> {
                Logger.info("[Solver] ${candidates.size} possible sequences remain.")
            }
        }
    }

    // Find which shelf likely contains the requested book
    fun findShelfForBook(book: Books): Bookshelf? {
        // First check if we already know where this book is
        shelfContents.entries.firstOrNull { it.value == book }?.let { entry ->
            return Bookshelves.BY_INDEX[entry.key]
        }

        // If sequence is known, we could predict location (future enhancement)
        // For now, return null to indicate we need to search
        return null
    }

    // Get next shelf to search (nearest unsearched shelf)
    fun nextShelfToSearch(from: Tile): Bookshelf? {
        return Bookshelves.ALL
            .filter { it.shelfIndex !in searchedShelves }
            .minByOrNull { it.standingTile.distanceTo(from) }
    }

    // Check if we have enough info to locate a book
    fun canLocateBook(book: Books): Boolean {
        return shelfContents.values.contains(book)
    }

    // Get solver status for logging
    fun status(): String {
        val searched = searchedShelves.size
        val total = Bookshelves.ALL.size
        val known = shelfContents.count { it.value != null }
        val seqStatus = if (currentSequence != null) "known" else "unknown"
        return "Solver: $searched/$total searched, $known books found, sequence $seqStatus"
    }
}