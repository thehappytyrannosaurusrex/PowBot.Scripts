package org.thehappytyrannosaurusrex.arceuuslibrary.tree

import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.thehappytyrannosaurusrex.arceuuslibrary.ArceuusLibrary

class RootBranch(script: ArceuusLibrary) :
    Branch<ArceuusLibrary>(script, "Root selector") {

    // Current "main" behaviour chain:
    // RootBranch -> InventorySanityBranch -> {DepositInventoryAtBank | TravelOrLibraryBranch}
    private val inventorySanityBranch = InventorySanityBranch(script)

    override fun validate(): Boolean {
        // Root is always active; successComponent decides what to run.
        return true
    }

    override val successComponent: TreeComponent<ArceuusLibrary>
        get() = inventorySanityBranch

    // Don't use failedComponent in pattern; mirror success.
    override val failedComponent: TreeComponent<ArceuusLibrary>
        get() = successComponent
}
