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

    companion object {
        private const val MAX_DISTANCE = 200.0
    }

    private val bookItemIds: Set<Int> = Books.allItemIds().toSet()

    private fun hasNonBookItems(): Boolean =
        Inventory.stream().anyMatch { it.id() != -1 && it.id() !in bookItemIds }

    override fun execute() {
        if (!hasNonBookItems()) {
            Logger.info("[Arceuus Library] BANK | No non-book items; skipping.")
            return
        }

        val done = BankUtils.depositInventoryToNearestBank(
            preferredBankTile = Locations.ARCEUUS_BANK_TILE,
            maxPreferredDistance = MAX_DISTANCE,
            logPrefix = "[Arceuus Library] BANK |"
        )

        if (done) {
            Logger.info("[Arceuus Library] BANK | Inventory clean.")
        }
    }
}