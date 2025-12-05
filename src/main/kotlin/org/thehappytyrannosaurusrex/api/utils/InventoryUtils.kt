package org.thehappytyrannosaurusrex.api.utils

import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Item

object InventoryUtils {

    // =========================================================================
    // Basic Inventory State
    // =========================================================================

    fun fullInv(): Boolean = Inventory.isFull()
    fun emptyInv(): Boolean = Inventory.isEmpty()
    fun notEmptyInv(): Boolean = Inventory.isNotEmpty()
    fun freeSlots(): Int = Inventory.emptySlotCount()
    fun usedSlots(): Int = 28 - freeSlots()

    // =========================================================================
    // String-based Item Checks
    // =========================================================================

    fun invContains(item: String): Boolean = Inventory.stream().name(item).isNotEmpty()
    fun anyInInv(items: List<String>): Boolean = items.any { invContains(it) }
    fun anyInInv(vararg items: String): Boolean = items.any { invContains(it) }
    fun allInInv(items: List<String>): Boolean = items.all { invContains(it) }
    fun allInInv(vararg items: String): Boolean = items.all { invContains(it) }
    fun noneInInv(items: List<String>): Boolean = items.none { invContains(it) }
    fun noneInInv(vararg items: String): Boolean = items.none { invContains(it) }
    fun inContainsLower(item: String): Boolean = Inventory.stream().any { item.contains(it.name().lowercase()) }
    fun setContainsLower(item: Set<String>): Boolean = Inventory.stream().any { item.contains(it.name().lowercase()) }

    fun findInvItem(item: String): Item = Inventory.stream().name(item).first()
    fun countInInv(item: String): Int = Inventory.stream().name(item).count().toInt()

    /**
     * Get total stack size of an item (useful for stackable items)
     */
    fun stackSizeInInv(item: String): Int {
        val invItem = findInvItem(item)
        return if (invItem.valid()) invItem.stackSize() else 0
    }

    /**
     * Check if inventory contains any item matching a predicate
     */
    fun anyMatching(predicate: (Item) -> Boolean): Boolean =
        Inventory.stream().any { it.valid() && predicate(it) }

    /**
     * Check if inventory contains any item whose name contains the given substring
     */
    fun anyNameContains(substring: String, ignoreCase: Boolean = true): Boolean =
        Inventory.stream().any { it.valid() && it.name().contains(substring, ignoreCase) }

    /**
     * Find first item whose name contains the given substring
     */
    fun findByNameContains(substring: String, ignoreCase: Boolean = true): Item =
        Inventory.stream().filter { it.valid() && it.name().contains(substring, ignoreCase) }.first()

    // =========================================================================
    // ID-based Item Checks
    // =========================================================================

    fun invContainsId(id: Int): Boolean = Inventory.stream().id(id).isNotEmpty()
    fun anyInInvId(ids: List<Int>): Boolean = ids.any { invContainsId(it) }
    fun anyInInvId(vararg ids: Int): Boolean = ids.any { invContainsId(it) }
    fun allInInvId(ids: List<Int>): Boolean = ids.all { invContainsId(it) }
    fun allInInvId(vararg ids: Int): Boolean = ids.all { invContainsId(it) }

    fun findInvItemId(id: Int): Item = Inventory.stream().id(id).first()
    fun countInvItem(id: Int): Int = Inventory.stream().id(id).count().toInt()

    // =========================================================================
    // Item Filtering / Listing
    // =========================================================================

    /**
     * Get all item names in inventory
     */
    fun allItemNames(): List<String> =
        Inventory.stream().filter { it.valid() }.map { it.name() }.toList()

    /**
     * Get all unique item names in inventory
     */
    fun uniqueItemNames(): Set<String> =
        Inventory.stream().filter { it.valid() }.map { it.name() }.toSet()

    /**
     * Get all items matching a predicate
     */
    fun findAll(predicate: (Item) -> Boolean): List<Item> =
        Inventory.stream().filter { it.valid() && predicate(it) }.toList()

    /**
     * Get all items with names in the given set
     */
    fun findAllByNames(names: Set<String>): List<Item> {
        val normalized = names.map { it.lowercase().trim() }.toSet()
        return Inventory.stream()
            .filter { it.valid() && it.name().lowercase().trim() in normalized }
            .toList()
    }

    /**
     * Get all items NOT in the given names set
     */
    fun findAllExceptNames(keepNames: Set<String>): List<Item> {
        val normalized = keepNames.map { it.lowercase().trim() }.toSet()
        return Inventory.stream()
            .filter { it.valid() && it.name().lowercase().trim() !in normalized }
            .toList()
    }

    // =========================================================================
    // Non-Allowed Items Checks (for banking/dropping logic)
    // =========================================================================

    /**
     * Check for non-allowed items using name predicates
     */
    fun hasNonAllowedItems(allowedByName: List<(String) -> Boolean> = emptyList()): Boolean {
        if (emptyInv()) return false
        return Inventory.stream().anyMatch { item ->
            val name = item.name()
            !allowedByName.any { predicate -> predicate(name) }
        }
    }

    /**
     * Check for non-allowed items using explicit name set + optional predicates
     */
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

    /**
     * Check if inventory has any items whose names are in the given set
     */
    fun hasAnyOfNames(names: Set<String>): Boolean {
        val normalized = names.map { it.lowercase().trim() }.toSet()
        return Inventory.stream().any { it.valid() && it.name().lowercase().trim() in normalized }
    }

    // =========================================================================
    // Explicit ID-based Methods (use only when IDs are required)
    // =========================================================================

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