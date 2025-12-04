package org.thehappytyrannosaurusrex.arceuuslibrary.tree

import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Leaf
import org.thehappytyrannosaurusrex.arceuuslibrary.ArceuusLibrary
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Books
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Locations
import org.thehappytyrannosaurusrex.api.utils.BankUtils
import org.thehappytyrannosaurusrex.api.utils.Logger

class DepositInventoryAtBankLeaf(script: ArceuusLibrary) :
    Leaf<ArceuusLibrary>(script, "Deposit inventory at bank") {

    private val bookItemIds: Set<Int> = Books.allItemIds().toSet()

    companion object {
        private const val ARCEUUS_MAX_DISTANCE = 200.0
    }

    private fun hasNonBookItems(): Boolean =
        Inventory.stream().anyMatch { item ->
            val id = item.id()
            id != -1 && id !in bookItemIds
        }

    override fun execute() {
        if (!hasNonBookItems()) {
            Logger.info("[Arceuus Library] LOGIC | No non-book items left; skipping bank.")
            return
        }

        val done = BankUtils.depositInventoryToNearestBank(
            preferredBankTile = Locations.ARCEUUS_BANK_TILE,
            maxPreferredDistance = ARCEUUS_MAX_DISTANCE,
            logPrefix = "[Arceuus Library] BANK |"
        )

        if (done) {
            Logger.info("[Arceuus Library] LOGIC | Banking complete; inventory clean.")
        }
    }
}
