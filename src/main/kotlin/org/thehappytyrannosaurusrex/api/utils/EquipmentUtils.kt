package org.thehappytyrannosaurusrex.api.utils

import org.powbot.api.rt4.Equipment

object EquipmentUtils {

    // -------------------------------------------------------------------------
    // Equipment Checks by Name
    // -------------------------------------------------------------------------

    fun isEquipped(name: String): Boolean =
        Equipment.stream().name(name).isNotEmpty()

    fun isEquippedAny(names: List<String>): Boolean =
        names.any { isEquipped(it) }

    fun isEquippedAll(names: List<String>): Boolean =
        names.all { isEquipped(it) }

    // -------------------------------------------------------------------------
    // Equipment Checks by ID
    // -------------------------------------------------------------------------

    fun isEquippedId(id: Int): Boolean =
        Equipment.stream().id(id).isNotEmpty()

    fun isEquippedAnyId(ids: List<Int>): Boolean =
        ids.any { isEquippedId(it) }

    fun isEquippedAnyId(vararg ids: Int): Boolean =
        ids.any { isEquippedId(it) }

    // -------------------------------------------------------------------------
    // Slot Checks
    // -------------------------------------------------------------------------

    fun getEquippedInSlot(slot: Equipment.Slot): String? {
        val item = Equipment.itemAt(slot)
        return if (item.valid()) item.name() else null
    }

    fun isSlotEmpty(slot: Equipment.Slot): Boolean =
        !Equipment.itemAt(slot).valid()

    fun isSlotOccupied(slot: Equipment.Slot): Boolean =
        Equipment.itemAt(slot).valid()
}