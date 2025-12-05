package org.thehappytyrannosaurusrex.arceuuslibrary.tree

import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Players
import org.powbot.api.script.tree.Leaf
import org.thehappytyrannosaurusrex.arceuuslibrary.ArceuusLibrary
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Locations
import org.thehappytyrannosaurusrex.api.utils.Logger

class InsideLibraryLeaf(script: ArceuusLibrary) :
    Leaf<ArceuusLibrary>(script, "Inside Library Behaviour") {

    private val setupLeaf = InventoryReadyLeaf(script)
    private val getNewRequestLeaf = GetNewRequestLeaf(script)
    private val deliverRequestLeaf = DeliverRequestLeaf(script)

    private var loggedFetchNotImplemented = false

    override fun execute() {
        val me = Players.local()
        if (!me.valid()) return

        val here = me.tile()
        if (!Locations.isInsideLibrary(here)) return

        // Ensure graceful/stamina setup
        if (!setupLeaf.ensureGraceful()) return
        if (!setupLeaf.ensureStamina()) return
        if (setupLeaf.maybeUseStaminaAndRestock()) return

        val state = script.state()
        val active = state.activeRequest

        // Get new request if none active
        if (active == null) {
            getNewRequestLeaf.execute()
            return
        }

        val requestedBook = active.book
        if (requestedBook == null) {
            Logger.info("[Arceuus Library] LOGIC | Active request has null book; raw='${active.rawTitle}'.")
            return
        }

        // Check if we have the book
        val hasBook = Inventory.stream().id(requestedBook.itemId).first().valid()
        if (hasBook) {
            deliverRequestLeaf.execute()
            return
        }

        // TODO: FetchRequestedBookLeaf using A* pathfinder and solver
        if (!loggedFetchNotImplemented) {
            Logger.info("[Arceuus Library] LOGIC | Need to fetch ${requestedBook.name}; solver not implemented yet.")
            loggedFetchNotImplemented = true
        }
    }
}