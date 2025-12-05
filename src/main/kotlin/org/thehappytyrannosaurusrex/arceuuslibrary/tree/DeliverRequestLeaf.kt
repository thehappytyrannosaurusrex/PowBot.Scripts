package org.thehappytyrannosaurusrex.arceuuslibrary.tree

import org.powbot.api.Condition
import org.powbot.api.rt4.Chat
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Npcs
import org.powbot.api.script.tree.Leaf
import org.thehappytyrannosaurusrex.arceuuslibrary.ArceuusLibrary
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Locations
import org.thehappytyrannosaurusrex.arceuuslibrary.state.LibraryNpcs
import org.thehappytyrannosaurusrex.api.utils.Logger

class DeliverRequestLeaf(script: ArceuusLibrary) :
    Leaf<ArceuusLibrary>(script, "Deliver requested book") {

    override fun execute() {
        val state = script.state()
        val active = state.activeRequest

        if (active == null) {
            Logger.info("[Arceuus Library] DELIVER | No active request.")
            return
        }

        val book = active.book
        if (book == null) {
            Logger.info("[Arceuus Library] DELIVER | Active request has null book; raw='${active.rawTitle}'.")
            return
        }

        val bookItem = Inventory.stream().id(book.itemId).first()
        if (!bookItem.valid()) {
            Logger.info("[Arceuus Library] DELIVER | Book ${book.name} not in inventory.")
            return
        }

        val targetNpcName = active.npcName ?: state.currentRequestNpc
        if (targetNpcName == null) {
            Logger.info("[Arceuus Library] DELIVER | Unknown NPC for delivery.")
            return
        }

        Logger.info("[Arceuus Library] DELIVER | Delivering ${book.name} to '$targetNpcName'.")

        val npc = Npcs.stream().name(targetNpcName).nearest().first()
        if (!npc.valid()) {
            val anchor = when (targetNpcName) {
                LibraryNpcs.PROFESSOR -> Locations.NPC_PROFESSOR_ANCHOR
                LibraryNpcs.VILLIA -> Locations.NPC_VILLIA_ANCHOR
                LibraryNpcs.SAM -> Locations.NPC_SAM_ANCHOR
                else -> null
            }
            if (anchor != null) {
                Logger.info("[Arceuus Library] DELIVER | Walking to NPC anchor $anchor.")
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
            Logger.info("[Arceuus Library] DELIVER | Failed to interact.")
            return
        }

        Condition.wait({ Chat.chatting() }, 200, 15)

        // Spam continue through reward dialogue
        repeat(15) {
            if (!Chat.chatting()) return@repeat
            Chat.clickContinue()
            Condition.sleep(500)
        }

        // Clear active request after delivery
        state.clearActiveRequest()
        state.rotateToNextNpc()
        Logger.info("[Arceuus Library] DELIVER | Delivered; rotating to next NPC.")
    }
}