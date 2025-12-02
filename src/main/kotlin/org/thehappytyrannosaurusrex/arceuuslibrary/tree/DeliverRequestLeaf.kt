package org.thehappytyrannosaurusrex.arceuuslibrary.tree

import org.powbot.api.Condition
import org.powbot.api.rt4.Chat
import org.powbot.api.rt4.Inventory
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
 * Deliver the requested book to the active request NPC if already hold it.
 */
class DeliverRequestLeaf(script: ArceuusLibrary) :
    Leaf<ArceuusLibrary>(script, "Deliver requested book") {

    override fun execute() {
        val state = script.state()
        val active = state.activeRequest

        if (active == null) {
            Logger.info("[Arceuus Library] DELIVER | No active request; nothing to deliver.")
            return
        }

        val requestedBook = active.book
        if (requestedBook == null) {
            Logger.info(
                "[Arceuus Library] DELIVER | Active request has null book enum (raw='${active.rawTitle}'); cannot deliver."
            )
            return
        }

        val bookItem = Inventory.stream().id(requestedBook.itemId).first()
        if (!bookItem.valid()) {
            Logger.info(
                "[Arceuus Library] DELIVER | Requested book ${requestedBook.name} not in inventory; cannot deliver."
            )
            return
        }

        // Prefer the NPC from the request, then currentRequestNpc.
        val targetNpcName = active.npcName
            ?: state.currentRequestNpc
            ?: run {
                Logger.info(
                    "[Arceuus Library] DELIVER | Unknown NPC for active request; " +
                            "cannot safely deliver. (book=${requestedBook.name})"
                )
                return
            }

        Logger.info(
            "[Arceuus Library] DELIVER | Attempting to deliver ${requestedBook.name} to '$targetNpcName'."
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
                    "[Arceuus Library] DELIVER | Could not find NPC '$targetNpcName' nearby " +
                            "and no anchor is configured for them."
                )
                return
            }

            Logger.info(
                "[Arceuus Library] DELIVER NPC anchor at $anchor."
            )

            // Let webwalking/pathfinding handle actually getting there.
            Movement.walkTo(anchor)

            // On the next tick, leaf will run again; by then the NPC
            // Should usually be discoverable by Npcs.stream().
            return
        }


        // NEW: nudge camera so the target NPC is roughly in front of .
        script.camera().faceLocatableCardinal(npc)

        // Move into viewport if needed.
        if (!npc.inViewport()) {
            Logger.info(
                "[Arceuus Library] DELIVER | Stepping towards '$targetNpcName' to get them in viewport."
            )
            Movement.moveTo(npc)
            Condition.wait({ npc.inViewport() }, 250, 12)
        }

        if (!npc.inViewport()) {
            Logger.info(
                "[Arceuus Library] DELIVER | '$targetNpcName' still not in viewport after stepping."
            )
            return
        }

        // Close any existing chat to avoid conflicts.
        if (Chat.chatting()) {
            Chat.clickContinue()
            Condition.wait({ !Chat.chatting() }, 200, 10)
        }

        // For Arceuus Library, simply talking to the NPC while holding the correct
        // Book is enough to turn it in and receive the reward dialogue.
        Logger.info(
            "[Arceuus Library] DELIVER | Talking to '$targetNpcName' to hand in ${requestedBook.name}."
        )

        val interacted = npc.interact("Help") || npc.interact("Talk-to")
        if (!interacted) {
            Logger.info(
                "[Arceuus Library] DELIVER | Failed to interact with '$targetNpcName'."
            )
            return
        }

        // Wait for chat to open.
        Condition.wait({ Chat.chatting() }, 200, 15)

        Logger.info(
            "[Arceuus Library] DELIVER | Waiting for reward/token dialogue to confirm delivery."
        )

        var delivered = false

        Condition.wait({
            val msg = Chat.getChatMessage()
            if (msg.isNullOrBlank()) {
                return@wait false
            }

            val event = LibraryChatParser.parse(msg, ChatSource.CHAT) ?: return@wait false

            if (event is LibraryChatEvent.NpcMetaDialogue) {
                when (event.kind) {
                    LibraryChatEvent.NpcMetaKind.REWARD_TOKEN -> {
                        Logger.info(
                            "[Arceuus Library] DELIVER | Reward/token dialogue detected; " +
                                    "marking '$targetNpcName' as recently helped and clearing active request."
                        )
                        state.markRecentlyHelped(targetNpcName)
                        state.clearActiveRequest()
                        delivered = true
                        true
                    }

                    LibraryChatEvent.NpcMetaKind.RECENTLY_HELPED_ALREADY -> {
                        Logger.info(
                            "[Arceuus Library] DELIVER | '$targetNpcName' says we've recently helped already."
                        )
                        // Still mark as recently helped so ping-pong logic can move on.
                        state.markRecentlyHelped(targetNpcName)
                        state.clearActiveRequest()
                        delivered = true
                        true
                    }

                    else -> false
                }
            } else {
                false
            }
        }, 250, 24)

        if (delivered) {
            Logger.info(
                "[Arceuus Library] DELIVER | Delivery complete; activeRequest cleared. " +
                        "State is now: ${script.state()}"
            )
        } else {
            Logger.info(
                "[Arceuus Library] DELIVER | Timed out waiting for reward / recently-helped dialogue."
            )
        }
    }
}
