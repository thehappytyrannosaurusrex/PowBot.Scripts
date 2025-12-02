package org.thehappytyrannosaurusrex.arceuuslibrary.solver

import org.thehappytyrannosaurusrex.arceuuslibrary.data.Books
import org.thehappytyrannosaurusrex.api.utils.Logger

/**
 * Port of the core Kourend Library sequence solver from RuneLite:
 */
enum class SolverState {
    NO_DATA,
    INCOMPLETE,
    COMPLETE
}

class LibrarySolver(
    /**
 * Total number of “slots” (indices) in the library ring.
 */
    private val slotCount: Int,

    /**
 * All sequences the game can choose between.
 */
    private val sequences: List<List<Books>> = LibrarySequences.sequences,

    /**
 * Optional predicate for “double index” bookcases (the 6 weird top-floor cases).
 */
    private val isDoubleIndex: (Int) -> Boolean = { false }
) {
    private val step: Int

    // Known book at each slot index (null = unknown / filler / not a layout book)
    private val knownBooks: Array<Books?> = arrayOfNulls(slotCount)

    // For each slot index, what layout books *could* be here given current best sequences
    private val possibleBooks: Array<MutableSet<Books>> =
        Array(slotCount) { mutableSetOf() }

    private var _state: SolverState = SolverState.NO_DATA
    val state: SolverState get() = _state

    init {
        require(slotCount > 0) { "slotCount must be > 0" }
        require(sequences.isNotEmpty()) { "At least one sequence is required" }

        val seqSize = sequences.first().size
        require(sequences.all { it.size == seqSize }) {
            "All sequences must have the same length"
        }

        // RuneLite: step = byIndex.size() / values().length;
        step = slotCount / seqSize
        require(step > 0) {
            "Computed step must be > 0 (slotCount=$slotCount, seqSize=$seqSize)"
        }

        reset("initial")
    }

    /**
 * Clear all known and possible books.
 */
    fun reset(reason: String = "manual reset") {
        _state = SolverState.NO_DATA
        for (i in 0 until slotCount) {
            knownBooks[i] = null
            possibleBooks[i].clear()
        }
        Logger.info("[Arceuus Library] SOLVER | Reset: $reason")
    }

    /**
 * Record an observation:
 */
    fun mark(index: Int, book: Books?) {
        if (index !in 0 until slotCount) {
            Logger.error("[Arceuus Library] SOLVER | mark() called with out-of-range index=$index")
            return
        }

        var currentIndex = index
        val currentBook = book

        // While loop mirrors the for (;;){...reset();continue;} structure
        // In the RuneLite solver.
        while (true) {
            val existingBook = knownBooks[currentIndex]

            if (existingBook != null) {
                // Slot already had a book from previous mark.
                // Reset if it mismatches, except for the special "null vs VARLAMORE_ENVOY" case.
                if (currentBook != existingBook &&
                    !(currentBook == null && existingBook == Books.VARLAMORE_ENVOY)
                ) {
                    reset("mismatch at index=$currentIndex existing=$existingBook new=$currentBook")
                }
            } else if (_state != SolverState.NO_DATA) {
                // Had expectations; if book isn't one of the expected possible books, reset.
                if (currentBook != null && !possibleBooks[currentIndex].contains(currentBook)) {
                    reset("unexpected book at index=$currentIndex book=$currentBook")
                }
            }

            if (_state == SolverState.COMPLETE) {
                // Layout previously solved. If now see nothing where were
                // Expecting *some* book that isn't VARLAMORE_ENVOY, assume the layout changed.
                if (currentBook == null &&
                    possibleBooks[currentIndex].isNotEmpty() &&
                    possibleBooks[currentIndex].none { it == Books.VARLAMORE_ENVOY }
                ) {
                    reset("layout changed at index=$currentIndex (expected non-null)")
                } else {
                    // Everything is consistent with the solved layout; nothing to do.
                    return
                }
            }

            // From here, (re)set the observation and do the sequence scoring.
            Logger.info("[Arceuus Library] SOLVER | Setting index=$currentIndex book=$currentBook")
            knownBooks[currentIndex] = currentBook

            // RuneLite: “Basing the sequences on null is not supported, though possible”
            if (currentBook == null) {
                return
            }

            // RuneLite: double-index bookcases are “not fully supported”; they bail early.
            if (isDoubleIndex(currentIndex)) {
                return
            }

            _state = SolverState.INCOMPLETE

            // Map each sequence to the number of bookcases that match it; 0 if mismatch.
            val certainty = IntArray(sequences.size)

            for (seqIdx in sequences.indices) {
                val sequence = sequences[seqIdx]
                val zero = zeroIndexForSequence(sequence, currentIndex, currentBook)
                if (zero == null) {
                    certainty[seqIdx] = 0
                    continue
                }

                var found = 0
                var inconsistent = false

                for (i in 0 until slotCount) {
                    val ai = (i + zero) % slotCount
                    val slotBook = knownBooks[ai]

                    if (i % step == 0) {
                        val seqI = i / step
                        if (slotBook != null && seqI < sequence.size) {
                            val expected = sequence[seqI]
                            if (slotBook != expected) {
                                // Sequence is incompatible with observations.
                                inconsistent = true
                                found = 0
                                break
                            }
                            found++
                        }
                    } else {
                        // Non-sequence positions: should NOT contain a known book, unless it's a double-index case.
                        if (slotBook != null && !isDoubleIndex(ai)) {
                            inconsistent = true
                            found = 0
                            break
                        }
                    }
                }

                certainty[seqIdx] = if (inconsistent) 0 else found
            }

            Logger.info(
                "[Arceuus Library] SOLVER | Certainty=" +
                        certainty.joinToString(prefix = "[", postfix = "]")
            )

            // Clear all possibleBooks – will recompute them from best-fitting sequences.
            for (i in 0 until slotCount) {
                possibleBooks[i].clear()
            }

            val max = certainty.maxOrNull() ?: 0

            // RuneLite: if have books set but 0 sequences match, something is wrong → reset + retry.
            if (max == 0) {
                reset("no sequences match current observations")
                // Retry once with the fresh state, like RuneLite's `continue;` in the for(;;) loop.
                continue
            }

            // For all sequences with certainty == max, write their predictions into possibleBooks.
            for (seqIdx in sequences.indices) {
                if (certainty[seqIdx] != max) continue

                val sequence = sequences[seqIdx]
                val zero = zeroIndexForSequence(sequence, currentIndex, currentBook) ?: continue

                for (i in 0 until slotCount) {
                    val ai = (i + zero) % slotCount

                    if (knownBooks[ai] == null) {
                        val seqI = i / step
                        if (i % step == 0 && seqI < sequence.size) {
                            val predictedBook = sequence[seqI]
                            possibleBooks[ai].add(predictedBook)
                        }
                    }
                }
            }

            val numBest = certainty.count { it == max }
            if (numBest == 1) {
                _state = SolverState.COMPLETE
            }

            return
        }
    }

    /**
 * Find the “zero” index for a sequence, assuming that [book] at [bookcaseIndex]
 */
    private fun zeroIndexForSequence(
        sequence: List<Books>,
        bookcaseIndex: Int,
        book: Books
    ): Int? {
        val seqPos = sequence.indexOf(book)
        if (seqPos < 0) {
            return null
        }

        var zero = bookcaseIndex - step * seqPos
        while (zero < 0) {
            zero += slotCount
        }
        return zero
    }

    // --- Public query helpers for debug / future integration ---

    fun getKnownBook(index: Int): Books? =
        if (index in 0 until slotCount) knownBooks[index] else null

    fun getPossibleBooks(index: Int): Set<Books> =
        if (index in 0 until slotCount) possibleBooks[index] else emptySet()

    fun debugSummary(): String {
        val knownCount = knownBooks.count { it != null }
        val possibleSlots = possibleBooks.count { it.isNotEmpty() }
        return "state=$_state known=$knownCount/$slotCount possibleSlots=$possibleSlots"
    }
}
