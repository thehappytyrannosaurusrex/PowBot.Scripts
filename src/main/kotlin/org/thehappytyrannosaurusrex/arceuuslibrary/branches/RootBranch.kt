package org.thehappytyrannosaurusrex.arceuuslibrary.branches

import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.thehappytyrannosaurusrex.arceuuslibrary.ArceuusLibrary

/**
 * RootBranch: top-level selector for the Arceuus Library script.
 *
 * For now it always delegates to [InventorySanityBranch], which in turn
 * chooses between banking and travel/library behaviour. As the script
 * grows, this is the place to add higher-level modes (recover, debug, etc).
 */
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

    // We don't use failedComponent in this pattern; mirror success.
    override val failedComponent: TreeComponent<ArceuusLibrary>
        get() = successComponent
}
