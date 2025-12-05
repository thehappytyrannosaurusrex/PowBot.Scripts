package org.thehappytyrannosaurusrex.arceuuslibrary.tree

import org.powbot.api.rt4.Players
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.thehappytyrannosaurusrex.arceuuslibrary.ArceuusLibrary
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Locations
import org.thehappytyrannosaurusrex.api.utils.Logger

class TravelOrLibraryBranch(script: ArceuusLibrary) :
    Branch<ArceuusLibrary>(script, "Travel vs Library") {

    private val insideLibraryLeaf = InsideLibraryLeaf(script)
    private val inventoryReadyLeaf = InventoryReadyLeaf(script)

    override fun validate(): Boolean {
        val me = Players.local()
        if (!me.valid()) {
            Logger.info("[Arceuus Library] LOGIC | Local player invalid.")
            return false
        }
        return Locations.isInsideLibrary(me.tile())
    }

    override val successComponent: TreeComponent<ArceuusLibrary>
        get() = insideLibraryLeaf

    override val failedComponent: TreeComponent<ArceuusLibrary>
        get() = inventoryReadyLeaf
}