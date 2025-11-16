package org.thehappytyrannosaurusrex.arceuuslibrary.branches

import org.powbot.api.rt4.Players
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.thehappytyrannosaurusrex.arceuuslibrary.ArceuusLibrary
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Locations
import org.thehappytyrannosaurusrex.arceuuslibrary.leaves.InsideLibraryLeaf
import org.thehappytyrannosaurusrex.arceuuslibrary.leaves.InventoryReadyLeaf
import org.thehappytyrannosaurusrex.arceuuslibrary.utils.Logger

class TravelOrLibraryBranch(script: ArceuusLibrary) :
    Branch<ArceuusLibrary>(script, "Travel vs Library") {

    private val insideLibraryLeaf = InsideLibraryLeaf(script)
    private val inventoryReadyLeaf = InventoryReadyLeaf(script)

    override fun validate(): Boolean {
        val me = Players.local()
        if (!me.valid()) {
            Logger.info("[Arceuus Library] LOGIC | Local player invalid; treating as outside library.")
            return false
        }
        val here = me.tile()
        val inside = Locations.isInsideLibrary(here)
        if (inside) {
            Logger.info("[Arceuus Library] LOGIC | Player inside library area; delegating to InsideLibraryLeaf.")
        }
        return inside
    }

    override val successComponent: TreeComponent<ArceuusLibrary>
        get() = insideLibraryLeaf         // validate() == true → inside library

    override val failedComponent: TreeComponent<ArceuusLibrary>
        get() = inventoryReadyLeaf        // validate() == false → need setup/travel
}
