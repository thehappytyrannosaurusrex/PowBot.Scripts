package org.thehappytyrannosaurusrex.arceuuslibrary.leaves

import org.powbot.api.rt4.Players
import org.powbot.api.script.tree.Leaf
import org.thehappytyrannosaurusrex.arceuuslibrary.ArceuusLibrary
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Locations
import org.thehappytyrannosaurusrex.arceuuslibrary.utils.Logger

/**
 * InsideLibraryLeaf
 *
 * Runs only while the behaviour tree thinks we are inside the Arceuus Library.
 * Before doing any library-specific work, we reuse InventoryReadyLeaf to handle:
 *  - Graceful setup / re-equip
 *  - Stamina setup / restock
 *  - Runtime stamina usage
 *
 * That way, enabling "Use Graceful" or "Use Stamina" while already in the
 * library still causes the script to fetch the gear/potions.
 */
class InsideLibraryLeaf(script: ArceuusLibrary) :
    Leaf<ArceuusLibrary>(script, "Inside Library Behaviour") {

    // Reuse all the setup + stamina logic that already exists here.
    private val setupLeaf = InventoryReadyLeaf(script)

    private var loggedOnce = false

    override fun execute() {
        // 1) Always run setup first: graceful, staminas, possible bank trip.
        setupLeaf.execute()

        val me = Players.local()
        if (!me.valid()) {
            // We can't meaningfully do library logic without a valid player.
            loggedOnce = false
            return
        }

        val here = me.tile()

        // If the setup leaf moved us out of the library (e.g. to bank),
        // skip library logic this tick. TravelOrLibraryBranch will route
        // us appropriately (likely back to InventoryReadyLeaf) next poll.
        if (!Locations.isInsideLibrary(here)) {
            if (loggedOnce) {
                Logger.info("[Library] Left library area (probably to bank); pausing library behaviour.")
                loggedOnce = false
            }
            return
        }

        // We're inside the library *and* setup didn't pull us away this tick.
        if (!loggedOnce) {
            Logger.info("[Library] Inside Arceuus Library – core behaviour not implemented yet (placeholder).")
            loggedOnce = true
        }

        // 2) TODO: Real library logic goes here:
        //    - Ensure we have/assign a customer
        //    - Parse requested book from chat
        //    - Pick a bookshelf based on your book table / mapping
        //    - Path to the shelf & search it
        //    - Hand in the book, repeat...
    }
}
