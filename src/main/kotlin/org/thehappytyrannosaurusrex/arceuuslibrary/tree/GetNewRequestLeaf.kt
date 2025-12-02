package org.thehappytyrannosaurusrex.arceuuslibrary.tree

import org.powbot.api.Condition
import org.powbot.api.rt4.Chat
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Npcs
import org.powbot.api.script.tree.Leaf
import org.thehappytyrannosaurusrex.arceuuslibrary.ArceuusLibrary
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Locations
import org.thehappytyrannosaurusrex.api.chat.ChatSource
import org.thehappytyrannosaurusrex.arceuuslibrary.solver.LibraryChatEvent
import org.thehappytyrannosaurusrex.arceuuslibrary.solver.LibraryChatParser
import org.thehappytyrannosaurusrex.arceuuslibrary.state.LibraryNpcs
import org.thehappytyrannosaurusrex.api.utils.Logger

/**
 * Walks to preferred librarian, talks to them, and waits for either:
 */
class GetNewRequestLeaf(script: ArceuusLibrary) :
    Leaf<ArceuusLibrary>(script, "Get new request") {

    private enum class Outcome {
        NONE,
        GOT_REQUEST,
        RECENTLY_HELPED,
        BUSY_WITH_OTHER
    }

    override fun execute() {
        val state = script.state()

        // If already have a request, nothing to do.
        if (state.activeRequest != null) {
            Logger.info("[Arceuus Library] REQUEST | Active request already present; skipping.")
            return
        }

        // Decide who to talk to:
        // - Prefer whatever LibraryState says
        // - Default to Professor on fresh start
        val targetNpcName = state.currentRequestNpc ?: LibraryNpcs.PROFESSOR

        Logger.info(
            "[Arceuus Library] REQUEST | Trying to obtain a request from '$targetNpcName' (state=$state)"
        )

        val npc = Npcs.stream().name(targetNpcName).nearest().first()
        if (!npc.valid()) {
            // If can't see the NPC at all, walk to their known anchor tile on the ground floor.
            val anchor = when (targetNpcName) {
                LibraryNpcs.PROFESSOR -> Locations.NPC_PROFESSOR_ANCHOR
                LibraryNpcs.VILLIA    -> Locations.NPC_VILLIA_ANCHOR
                LibraryNpcs.SAM       -> Locations.NPC_SAM_ANCHOR
                else                  -> null
            }

            if (anchor == null) {
                Logger.info(
                    "[Arceuus Library] REQUEST | Could not find NPC '$targetNpcName' nearby " +
                            "and no anchor is configured for them."
                )
                return
            }

            Logger.info(
                "[Arceuus Library] REQUEST | Could not find NPC '$targetNpcName' nearby. " +
                        "Walking to NPC anchor at $anchor."
            )

            // Let webwalking/pathfinding handle actually getting there.
            Movement.walkTo(anchor)

            // On the next tick, leaf will run again; by then the NPC
            // Should usually be discoverable by Npcs.stream().
            return
        }

        // NEW: nudge camera so the target NPC is roughly in front of .
        script.camera().faceLocatableCardinal(npc)

        // Make sure can actually see the NPC.
        if (!npc.inViewport()) {
            Logger.info(
                "[Arceuus Library] REQUEST | Walking towards '$targetNpcName' to get in viewport."
            )
            Movement.moveTo(npc)
            Condition.wait({ npc.inViewport() }, 250, 12)
        }

        if (!npc.inViewport()) {
            Logger.info(
                "[Arceuus Library] REQUEST | '$targetNpcName' still not in viewport after stepping."
            )
            return
        }

        // Clean up any hanging dialogue before starting a new convo.
        if (Chat.chatting()) {
            Chat.clickContinue()
            Condition.wait({ !Chat.chatting() }, 200, 10)
        }

        Logger.info(
            "[Arceuus Library] REQUEST | Interacting with '$targetNpcName' for new request."
        )

        // Prefer Talk-to, but fall back to Help if that exists on the menu.
        val interacted = npc.interact("Help") || npc.interact("Talk-to")
        if (!interacted) {
            Logger.info(
                "[Arceuus Library] REQUEST | Failed to interact with '$targetNpcName'."
            )
            return
        }

        // Wait for chat to open.
        Condition.wait({ Chat.chatting() }, 200, 15)

        Logger.info(
            "[Arceuus Library] REQUEST | Waiting for request or meta dialogue in chat."
        )

        var outcome = Outcome.NONE

        Condition.wait({
            val msg = Chat.getChatMessage()
            if (msg.isNullOrBlank()) {
                return@wait false
            }

            val event = LibraryChatParser.parse(msg, ChatSource.CHAT) ?: return@wait false

            when (event) {
                is LibraryChatEvent.CustomerRequested -> {
                    // Normal happy path: NPC asked for a book.
                    state.onCustomerRequested(
                        npcName = targetNpcName,
                        rawTitle = event.rawTitle,
                        book = event.book
                    )
                    Logger.info(
                        "[Arceuus Library] REQUEST | Parsed new request from '$targetNpcName': " +
                                "book=${event.book}, raw='${event.rawTitle}'"
                    )
                    outcome = Outcome.GOT_REQUEST
                    true
                }

                is LibraryChatEvent.NpcMetaDialogue -> {
                    when (event.kind) {
                        LibraryChatEvent.NpcMetaKind.RECENTLY_HELPED_ALREADY -> {
                            // Happens when talk to Professor/Villia after
                            // Just finishing their request. Use it to switch prefs.
                            Logger.info(
                                "[Arceuus Library] REQUEST | '$targetNpcName' says we've " +
                                        "recently helped already; marking as recently helped."
                            )
                            state.markRecentlyHelped(targetNpcName)
                            outcome = Outcome.RECENTLY_HELPED
                            true
                        }

                        LibraryChatEvent.NpcMetaKind.BUSY_WITH_OTHER_CUSTOMER -> {
                            Logger.info(
                                "[Arceuus Library] REQUEST | '$targetNpcName' says we're " +
                                        "busy helping someone else."
                            )

                            // If happens when *think* have no active request
                            // (typical on script start), and it's from Professor, assume
                            // The outstanding request is from Villia or Sam.
                            // Prefer to check Villia first so can re-enter the
                            // Prof/Villia ping-pong.
                            if (targetNpcName == LibraryNpcs.PROFESSOR &&
                                state.activeRequest == null
                            ) {
                                state.currentRequestNpc = LibraryNpcs.VILLIA
                                state.nextRequestNpc = LibraryNpcs.PROFESSOR
                                Logger.info(
                                    "[Arceuus Library] REQUEST | BUSY_WITH_OTHER from Professor on clean state; " +
                                            "switching preference to Villia (next=Professor) to re-enter ping-pong."
                                )
                            }

                            outcome = Outcome.BUSY_WITH_OTHER
                            true
                        }

                        else -> false
                    }
                }

                else -> false
            }
        }, 250, 24)

        when (outcome) {
            Outcome.GOT_REQUEST -> {
                Logger.info(
                    "[Arceuus Library] REQUEST | New activeRequest is now: ${state.activeRequest}"
                )
            }

            Outcome.RECENTLY_HELPED -> {
                Logger.info(
                    "[Arceuus Library] REQUEST | Marked '$targetNpcName' as recently helped. " +
                            "Next preferred NPC is now: ${state.currentRequestNpc}"
                )
            }

            Outcome.BUSY_WITH_OTHER -> {
                // Nothing more to do tick; on next tick, InsideLibraryLeaf
                // Will call again, and if switched to Villia above, 'll
                // Now try Villia first.
            }

            Outcome.NONE -> {
                Logger.info(
                    "[Arceuus Library] REQUEST | Timed out waiting for a CustomerRequested " +
                            "or meta dialogue line."
                )
            }
        }
    }
}
