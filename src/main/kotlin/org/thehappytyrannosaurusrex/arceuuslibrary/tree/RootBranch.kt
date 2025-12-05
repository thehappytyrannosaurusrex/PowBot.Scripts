package org.thehappytyrannosaurusrex.arceuuslibrary.tree

import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.thehappytyrannosaurusrex.arceuuslibrary.ArceuusLibrary

class RootBranch(script: ArceuusLibrary) :
    Branch<ArceuusLibrary>(script, "Root selector") {

    private val inventorySanityBranch = InventorySanityBranch(script)

    override fun validate(): Boolean = true

    override val successComponent: TreeComponent<ArceuusLibrary>
        get() = inventorySanityBranch

    override val failedComponent: TreeComponent<ArceuusLibrary>
        get() = successComponent
}