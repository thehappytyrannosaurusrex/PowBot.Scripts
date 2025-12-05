package org.thehappytyrannosaurusrex.api.utils

import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Item

object InventoryUtils {

    // Basic inventory state
    fun fullInv(): Boolean = Inventory.isFull()
    fun emptyInv(): Boolean = Inventory.isEmpty()
    fun notEmptyInv(): Boolean = Inventory.isNotEmpty()
    fun freeSlots(): Int = Inventory.emptySlotCount()

    // String-based item checks
    fun invContains(item: String): Boolean = Inventory.stream().name(item).isNotEmpty()
    fun anyInInv(items: List<String>): Boolean = items.any { invContains(it) }
    fun allInInv(items: List<String>): Boolean = items.all { invContains(it) }
    fun findInvItem(item: String): Item = Inventory.stream().name(item).first()
    fun countInInv(item: String): Int = Inventory.stream().name(item).count().toInt()

    // ID-based item checks
    fun invContainsId(id: Int): Boolean = Inventory.stream().id(id).isNotEmpty()
    fun anyInInvId(items: List<Int>): Boolean = items.any { invContainsId(it) }
    fun allInInvId(items: List<Int>): Boolean = items.all { invContainsId(it) }
    fun findInvItemId(id: Int): Item = Inventory.stream().id(id).first()
    fun countInvItem(id: Int): Int = Inventory.stream().id(id).count().toInt()

    // Check for non-allowed items using name predicates
    fun hasNonAllowedItems(allowedByName: List<(String) -> Boolean> = emptyList()): Boolean {
        if (emptyInv()) return false
        return Inventory.stream().anyMatch { item ->
            val name = item.name()
            !allowedByName.any { predicate -> predicate(name) }
        }
    }

    // Check for non-allowed items using explicit name set + optional predicates
    fun hasNonAllowedItemsByNames(
        allowedNames: Set<String>,
        additionalPredicates: List<(String) -> Boolean> = emptyList()
    ): Boolean {
        if (emptyInv()) return false
        val normalizedAllowed = allowedNames.map { it.lowercase().trim() }.toSet()

        return Inventory.stream().anyMatch { item ->
            if (!item.valid()) return@anyMatch false
            val name = item.name().lowercase().trim()
            val inAllowedNames = name in normalizedAllowed
            val matchesPredicate = additionalPredicates.any { it(item.name()) }
            !(inAllowedNames || matchesPredicate)
        }
    }

    // Get all item names in inventory
    fun allItemNames(): List<String> =
        Inventory.stream().filter { it.valid() }.map { it.name() }.toList()

    // Explicit ID-based methods (use only when IDs are required)
    object ById {
        fun invContains(id: Int): Boolean = Inventory.stream().id(id).isNotEmpty()
        fun countInInv(id: Int): Int = Inventory.stream().id(id).count().toInt()

        fun hasNonAllowedItems(
            allowedItemIds: Set<Int> = emptySet(),
            allowedByName: List<(String) -> Boolean> = emptyList()
        ): Boolean {
            if (Inventory.isEmpty()) return false
            return Inventory.stream().anyMatch { item ->
                if (!item.valid()) return@anyMatch false
                val id = item.id()
                val name = item.name()
                val allowedById = id in allowedItemIds
                val allowedByNameMatch = allowedByName.any { predicate -> predicate(name) }
                !(allowedById || allowedByNameMatch)
            }
        }
    }
}