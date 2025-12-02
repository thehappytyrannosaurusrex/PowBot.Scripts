package org.thehappytyrannosaurusrex.arceuuslibrary.tree

import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Players
import org.powbot.api.script.tree.Leaf
import org.thehappytyrannosaurusrex.arceuuslibrary.ArceuusLibrary
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Locations
import org.thehappytyrannosaurusrex.api.utils.Logger

/**
 * Core behaviour while the player is inside the Arceuus Library.
 */
class InsideLibraryLeaf(script: ArceuusLibrary) :
    Leaf<ArceuusLibrary>(script, "Inside Library Behaviour") {

    // Setup (graceful, stamina, travel items) when 're inside.
    private val setupLeaf = InventoryReadyLeaf(script)

    // Request handling leaves.
    private val getNewRequestLeaf = GetNewRequestLeaf(script)
    private val deliverRequestLeaf = DeliverRequestLeaf(script)

    private var loggedOnce = false

    override fun execute() {
        val me = Players.local()
        if (!me.valid()) {
            return
        }

        val here = me.tile()
        if (!Locations.isInsideLibrary(here)) {
            // Shouldn't normally happen because TravelOrLibraryBranch
            // Should only delegate here if 're inside, but guard anyway.
            if (!loggedOnce) {
                Logger.info("[Arceuus Library] LOGIC | InsideLibraryLeaf called while outside library; doing nothing.")
                loggedOnce = true
            }
            return
        }

        // 1) Ensure inventory + stamina/graceful setup is correct.
        // Mirrors the top of InventoryReadyLeaf.execute().
        val gracefulOk = setupLeaf.ensureGraceful()
        val staminaOk = setupLeaf.ensureStamina()
        if (!gracefulOk || !staminaOk) {
            // EnsureGraceful / ensureStamina will move to bank / open bank
            // As needed, so just wait for them to finish.
            return
        }

        // Runtime stamina policy (sipping + restocking when low).
        if (setupLeaf.maybeUseStaminaAndRestock()) {
            // If returns true, it either drank or started moving to bank.
            return
        }

        val state = script.state()
        val active = state.activeRequest

        // 2) If don't yet have an active request, obtain one.
        if (active == null) {
            getNewRequestLeaf.execute()
            return
        }

        // 3) If have an active request and a resolved book, check inventory.
        val requestedBook = active.book
        if (requestedBook == null) {
            Logger.info(
                "[Arceuus Library] LOGIC | Have active request but book enum is null; " +
                        "likely unknown title mapping for '${active.rawTitle}'. Waiting / logging only for now."
            )
            return
        }

        val hasBook = Inventory.stream().id(requestedBook.itemId).first().valid()
        if (hasBook) {
            // Can fulfil the request immediately.
            deliverRequestLeaf.execute()
            return
        }

        // 4) have an active request but do NOT have the book yet.
        // In a future phase, a FetchRequestedBookLeaf will use the solver
        // To path to the correct shelf and obtain the book. For now, just log.
        if (!loggedOnce) {
            Logger.info(
                "[Arceuus Library] LOGIC | Active request ${requestedBook.name} but not in inventory; " +
                        "fetch behaviour not implemented yet."
            )
            loggedOnce = true
        }
    }
}
