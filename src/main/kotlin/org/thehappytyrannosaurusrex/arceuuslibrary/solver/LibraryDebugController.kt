package org.thehappytyrannosaurusrex.arceuuslibrary.solver

import org.powbot.api.rt4.Chat
import org.powbot.api.rt4.Players
import org.thehappytyrannosaurusrex.arceuuslibrary.config.DebugMode
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Books
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Bookshelves
import org.thehappytyrannosaurusrex.api.utils.Logger
import org.thehappytyrannosaurusrex.api.chat.ChatSource
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Locations

/**
 * Owns all Arceuus Library chat parsing + manual solver debug behavior.
 *
 * - CHAT_PARSER_DEBUG: logs parsed events only.
 * - MANUAL_SOLVER_DEBUG: logs parsed events AND feeds them into LibrarySolver.
 * - CHAT_AND_MANUAL_SOLVER_DEBUG: same as MANUAL_SOLVER_DEBUG, but exposed as a combined mode option.
 */

class LibraryDebugController(
    /** Current debug mode (we ask the script each tick so it can change at runtime). */
    private val debugModeProvider: () -> DebugMode
) {

    // Sequence-based layout solver (used only in MANUAL_SOLVER_DEBUG).
    private val librarySolver = LibrarySolver(
        slotCount = Bookshelves.ALL.maxOf { it.shelfIndex } + 1
        // using default sequences & isDoubleIndex = { false } for now
    )

    // Used only in parser/solver debug modes to avoid spamming the same chat line.
    private var lastParsedChatMessage: String? = null

    /**
     * Called when the script enters MANUAL_SOLVER_DEBUG, or whenever you want to wipe state.
     */
    fun resetManualSolver(reason: String) {
        librarySolver.reset(reason)
    }

    /**
     * Called from the script's poll() in parser / manual solver debug modes.
     * Polls the Chat API, parses the most recent line, and handles it.
     */
    fun tickChatDebug() {
        val mode = debugModeProvider()
        if (mode != DebugMode.CHAT_PARSER_DEBUG &&
            mode != DebugMode.MANUAL_SOLVER_DEBUG &&
            mode != DebugMode.CHAT_AND_MANUAL_SOLVER_DEBUG
        ) {
            return
        }

        if (!Chat.chatting()) {
            lastParsedChatMessage = null
            return
        }

        val msg = Chat.getChatMessage()
        if (msg.isNullOrBlank() || msg == lastParsedChatMessage) {
            return
        }

        lastParsedChatMessage = msg

        val event = LibraryChatParser.parse(msg, ChatSource.CHAT) ?: return
        handleParsedEvent(event, ChatSource.CHAT, mode)
    }

    /**
     * Called from the script's MessageEvent @Subscribe handler.
     */
    fun onServerMessage(message: String) {
        val mode = debugModeProvider()
        if (mode != DebugMode.CHAT_PARSER_DEBUG &&
            mode != DebugMode.MANUAL_SOLVER_DEBUG &&
            mode != DebugMode.CHAT_AND_MANUAL_SOLVER_DEBUG
        ) {
            return
        }

        val parsed = LibraryChatParser.parse(message, ChatSource.SERVER) ?: return
        handleParsedEvent(parsed, ChatSource.SERVER, mode)
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private fun handleParsedEvent(
        event: LibraryChatEvent,
        source: ChatSource,
        mode: DebugMode
    ) {
        logChatParserEvent(event, source)

        // In CHAT_PARSER_DEBUG we stop here: only logging.
        if (mode != DebugMode.MANUAL_SOLVER_DEBUG &&
            mode != DebugMode.CHAT_AND_MANUAL_SOLVER_DEBUG
        ) {
            return
        }

        // In MANUAL_SOLVER_DEBUG, we also feed shelves into the solver.
        when (event) {
            is LibraryChatEvent.ShelfBookFound -> {
                val idx = currentShelfIndex()
                if (idx == null) {
                    Logger.error(
                        "[Arceuus Library] SOLVER | ShelfBookFound but no matching standing tile for player."
                    )
                    return
                }

                librarySolver.mark(idx, event.book)
                logSolverStateAfterObservation(idx, event.book)
            }

            LibraryChatEvent.LayoutReset -> {
                librarySolver.reset("layout reset chat message")
            }

            else -> {
                // We'll use other events (requests, rewards, etc.) later,
                // but for now they don't affect the layout solver.
            }
        }
    }

    // Nice, book-aware logging for parser events.
    private fun logChatParserEvent(event: LibraryChatEvent, source: ChatSource) {
        val sourceTag = when (source) {
            ChatSource.SERVER -> "SERVER"
            ChatSource.CHAT -> "CHAT"
        }

        when (event) {
            is LibraryChatEvent.ShelfBookFound -> {
                Logger.info(
                    "[Arceuus Library] DEBUG | $sourceTag: ShelfBookFound = ${event.book.name}"
                )
            }

            LibraryChatEvent.ShelfEmpty -> {
                Logger.info(
                    "[Arceuus Library] DEBUG | $sourceTag: ShelfEmpty"
                )
            }

            LibraryChatEvent.LayoutReset -> {
                Logger.info(
                    "[Arceuus Library] DEBUG | $sourceTag: LayoutReset"
                )
            }

            is LibraryChatEvent.CustomerRequested -> {
                val bookEnum = event.book?.name ?: "UNKNOWN"
                Logger.info(
                    "[Arceuus Library] DEBUG | $sourceTag: CustomerRequested = $bookEnum"
                )
            }

            is LibraryChatEvent.NpcMetaDialogue -> {
                Logger.info(
                    "[Arceuus Library] DEBUG | $sourceTag: NpcMetaDialogue kind=${event.kind}"
                )
            }

            else -> {
                // Safety net in case we ever add more subclasses later.
                Logger.info(
                    "[Arceuus Library] DEBUG | $sourceTag: Unhandled event type=${event::class.simpleName}"
                )
            }
        }
    }

    /**
     * Returns the solver shelf index for the bookshelf we’re currently standing at,
     * or null if we’re not standing on any bookshelf’s standing tile.
     */
    private fun currentShelfIndex(): Int? {
        val me = Players.local()
        val tile = me.tile()

        val shelf = Bookshelves.ALL.firstOrNull { it.standingTile == tile } ?: return null
        return shelf.shelfIndex
    }

    /**
     * Pretty, per-shelf dump of solver state after each observation.
     */
    private fun logSolverStateAfterObservation(index: Int, book: Books) {
        // High-level summary (raw debug string from solver)
        Logger.info(
            "[Arceuus Library] SOLVER | After obs index=$index book=${book.name}: ${librarySolver.debugSummary()}"
        )

        var observedCount = 0      // shelves we actually clicked/confirmed
        var predictedCount = 0     // shelves solver has inferred from the pattern

        // One line per shelf with either:
        // - a known layout book, or
        // - one or more possible layout books
        for (shelf in Bookshelves.ALL) {
            val idx = shelf.shelfIndex
            val known = librarySolver.getKnownBook(idx)
            val possible = librarySolver.getPossibleBooks(idx)

            if (known == null && possible.isEmpty()) {
                continue
            }

            val tile = shelf.objTile
            val coord = "${tile.x}, ${tile.y}, ${tile.floor}"
            val area = shelf.area
            val floor = Locations.floorName(shelf.floor).uppercase()

            val bookLabel = if (known != null) {
                observedCount++
                "[${known.name}]"
            } else {
                predictedCount++
                val possStr = possible.joinToString(",") { it.name }
                "[$possStr]"
            }

            Logger.info(
                "[Arceuus Library] SOLVER | $bookLabel at shelf $idx ($coord) in $area $floor"
            )
        }

        val totalSlots = observedCount + predictedCount
        val stateLabel = librarySolver.state.name

        Logger.info(
            "[Arceuus Library] SOLVER | Layout $stateLabel: " +
                    "known=$observedCount, predicted=$predictedCount, totalKnownSlots=$totalSlots"
        )
    }
}