package org.thehappytyrannosaurusrex.api.inventory

import org.powbot.api.rt4.Inventory


object InventoryManagement {

    fun fullInv(): Boolean {
        return Inventory.isFull()
    }

    fun emptyInv(): Boolean {
        return Inventory.isEmpty()
    }

    fun notEmptyInv(): Boolean{
        return Inventory.isNotEmpty()
    }

    fun invContains (item: String): Boolean {
        return Inventory.stream().name(item).isNotEmpty()
    }

    fun anyInInv (items: List<String>): Boolean {
        return items.any { invContains(it) }
    }
    
    /**
 * Returns true if the inventory contains any item that is not allowed.
 */
    fun hasNonAllowedItems(allowedByName: List<(String) -> Boolean> = emptyList()): Boolean {
        if (emptyInv()) {
            return false
        }

        return Inventory.stream().anyMatch { item ->
            val name = item.name()
            val allowedByNameMatch = allowedByName.any { predicate -> predicate(name) }

            !(allowedByNameMatch)
        }
    }
}
