package org.thehappytyrannosaurusrex.arceuuslibrary.branches

import org.powbot.api.rt4.Players
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.thehappytyrannosaurusrex.arceuuslibrary.ArceuusLibrary
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Locations
import org.thehappytyrannosaurusrex.arceuuslibrary.leaves.InsideLibraryLeaf
import org.thehappytyrannosaurusrex.arceuuslibrary.leaves.InventoryReadyLeaf
import org.thehappytyrannosaurusrex.arceuuslibrary.utils.Logger

/**
 * TravelOrLibraryBranch
 *
 * Assumes inventory is already in a "clean" state.
 * - If the player is inside any library area (see Locations), run the
 *   core in-library behaviour.
 * - Otherwise, run the setup/travel leaf to get us there.
 */
class TravelOrLibraryBranch(script: ArceuusLibrary) :
    Branch<ArceuusLibrary>(script, "Travel vs Library") {

    private val insideLibraryLeaf = InsideLibraryLeaf(script)
    private val inventoryReadyLeaf = InventoryReadyLeaf(script)

    override fun validate(): Boolean {
        val me = Players.local()
        if (!me.valid()) {
            Logger.info("[Root] [TravelOrLibrary] Local player invalid; treating as outside library.")
            return false
        }
        val here = me.tile()
        val inside = Locations.isInsideLibrary(here)
        if (inside) {
            Logger.info("[Root] [TravelOrLibrary] Player inside library area; delegating to InsideLibraryLeaf.")
        }
        return inside
    }

    override val successComponent: TreeComponent<ArceuusLibrary>
        get() = insideLibraryLeaf         // validate() == true → inside library

    override val failedComponent: TreeComponent<ArceuusLibrary>
        get() = inventoryReadyLeaf        // validate() == false → need setup/travel
}
