package org.thehappytyrannosaurusrex.arceuuslibrary.tree

import org.powbot.api.Condition
import org.powbot.api.rt4.Chat
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Npcs
import org.powbot.api.script.tree.Leaf
import org.thehappytyrannosaurusrex.arceuuslibrary.ArceuusLibrary
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Locations
import org.thehappytyrannosaurusrex.arceuuslibrary.solver.LibraryChatEvent
import org.thehappytyrannosaurusrex.arceuuslibrary.solver.LibraryChatParser
import org.thehappytyrannosaurusrex.arceuuslibrary.state.LibraryNpcs
import org.thehappytyrannosaurusrex.api.chat.ChatSource
import org.thehappytyrannosaurusrex.api.utils.Logger

class GetNewRequestLeaf(script: ArceuusLibrary) :
    Leaf<ArceuusLibrary>(script, "Get new request") {

    private enum class Outcome { NONE, GOT_REQUEST, RECENTLY_HELPED, BUSY_WITH_OTHER }

    override fun execute() {
        val state = script.state()

        if (state.activeRequest != null) {
            Logger.info("[Arceuus Library] REQUEST | Already have request; skipping.")
            return
        }

        val targetNpcName = state.currentRequestNpc ?: LibraryNpcs.PROFESSOR
        Logger.info("[Arceuus Library] REQUEST | Talking to '$targetNpcName'.")

        val npc = Npcs.stream().name(targetNpcName).nearest().first()
        if (!npc.valid()) {
            val anchor = when (targetNpcName) {
                LibraryNpcs.PROFESSOR -> Locations.NPC_PROFESSOR_ANCHOR
                LibraryNpcs.VILLIA -> Locations.NPC_VILLIA_ANCHOR
                LibraryNpcs.SAM -> Locations.NPC_SAM_ANCHOR
                else -> null
            }

            if (anchor != null) {
                Logger.info("[Arceuus Library] REQUEST | Walking to NPC anchor $anchor.")
                Movement.walkTo(anchor)
            }
            return
        }

        script.camera().faceLocatableCardinal(npc)

        if (!npc.inViewport()) {
            Movement.moveTo(npc)
            Condition.wait({ npc.inViewport() }, 250, 12)
        }
        if (!npc.inViewport()) return

        if (Chat.chatting()) {
            Chat.clickContinue()
            Condition.wait({ !Chat.chatting() }, 200, 10)
        }

        val interacted = npc.interact("Help") || npc.interact("Talk-to")
        if (!interacted) {
            Logger.info("[Arceuus Library] REQUEST | Failed to interact with '$targetNpcName'.")
            return
        }

        Condition.wait({ Chat.chatting() }, 200, 15)

        var outcome = Outcome.NONE

        Condition.wait({
            val msg = Chat.getChatMessage()
            if (msg.isNullOrBlank()) return@wait false

            val event = LibraryChatParser.parse(msg, ChatSource.CHAT) ?: return@wait false

            when (event) {
                is LibraryChatEvent.CustomerRequested -> {
                    state.onCustomerRequested(targetNpcName, event.rawTitle, event.book)
                    Logger.info("[Arceuus Library] REQUEST | Got request: ${event.book?.name ?: event.rawTitle}")
                    outcome = Outcome.GOT_REQUEST
                    true
                }
                is LibraryChatEvent.NpcMetaDialogue -> {
                    outcome = Outcome.RECENTLY_HELPED
                    true
                }
                else -> false
            }
        }, 300, 30)

        // Spam continue to close dialogue
        repeat(10) {
            if (!Chat.chatting()) return@repeat
            Chat.clickContinue()
            Condition.sleep(400)
        }
    }
}