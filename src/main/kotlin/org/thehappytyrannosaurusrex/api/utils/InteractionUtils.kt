package org.thehappytyrannosaurusrex.api.utils

import org.powbot.api.Area
import org.powbot.api.Condition
import org.powbot.api.Tile
import org.powbot.api.rt4.*
import org.thehappytyrannosaurusrex.api.utils.InteractionUtils

object InteractionUtils {

    // -------------------------------------------------------------------------
    // Game Objects
    // -------------------------------------------------------------------------

    fun findObject(name: String): GameObject =
        Objects.stream().name(name).nearest().first()

    fun findObject(name: String, within: Area): GameObject =
        Objects.stream().name(name).within(within).nearest().first()

    fun findObjectAt(name: String, tile: Tile): GameObject =
        Objects.stream().name(name).at(tile).first()

    fun interactObject(name: String, action: String? = null, ensureViewport: Boolean = true): Boolean {
        val obj = findObject(name)
        return doInteractObject(obj, action, ensureViewport)
    }

    fun interactObjectAt(name: String, tile: Tile, action: String? = null, ensureViewport: Boolean = true): Boolean {
        val obj = findObjectAt(name, tile)
        return doInteractObject(obj, action, ensureViewport)
    }

    fun interactObjectWithin(name: String, area: Area, action: String? = null, ensureViewport: Boolean = true): Boolean {
        val obj = findObject(name, area)
        return doInteractObject(obj, action, ensureViewport)
    }

    private fun doInteractObject(obj: GameObject, action: String?, ensureViewport: Boolean): Boolean {
        if (!obj.valid()) return false
        if (ensureViewport && !obj.inViewport()) {
            Camera.turnTo(obj)
            Condition.sleep(300)
        }
        return if (action != null) obj.interact(action) || obj.click() else obj.click()
    }

    // -------------------------------------------------------------------------
    // NPCs
    // -------------------------------------------------------------------------

    fun findNpc(name: String): Npc =
        Npcs.stream().name(name).nearest().first()

    fun findNpcWithin(name: String, distance: Int): Npc =
        Npcs.stream().name(name).within(distance).nearest().first()

    fun interactNpc(name: String, action: String? = null, ensureViewport: Boolean = true): Boolean {
        val npc = findNpc(name)
        return doInteractNpc(npc, action, ensureViewport)
    }

    fun talkToNpc(name: String, ensureViewport: Boolean = true): Boolean =
        interactNpc(name, "Talk-to", ensureViewport)

    private fun doInteractNpc(npc: Npc, action: String?, ensureViewport: Boolean): Boolean {
        if (!npc.valid()) return false
        if (ensureViewport && !npc.inViewport()) {
            Movement.moveTo(npc)
            Condition.wait({ npc.inViewport() }, 250, 12)
        }
        if (!npc.inViewport()) return false
        return if (action != null) npc.interact(action) || npc.click() else npc.click()
    }

    // -------------------------------------------------------------------------
    // Ground Items
    // -------------------------------------------------------------------------

    fun findGroundItem(name: String): GroundItem =
        GroundItems.stream().name(name).nearest().first()

    fun findGroundItemAt(name: String, tile: Tile): GroundItem =
        GroundItems.stream().name(name).at(tile).first()

    fun interactGroundItem(name: String, action: String? = null, ensureViewport: Boolean = true): Boolean {
        val item = findGroundItem(name)
        return doInteractGroundItem(item, action, ensureViewport)
    }

    fun takeGroundItem(name: String, ensureViewport: Boolean = true): Boolean =
        interactGroundItem(name, "Take", ensureViewport)

    private fun doInteractGroundItem(item: GroundItem, action: String?, ensureViewport: Boolean): Boolean {
        if (!item.valid()) return false
        if (ensureViewport && !item.inViewport()) {
            Camera.turnTo(item)
            Condition.sleep(300)
        }
        return if (action != null) item.interact(action) || item.click() else item.click()
    }

    // -------------------------------------------------------------------------
    // Inventory Items
    // -------------------------------------------------------------------------

    fun interactInvItem(name: String, action: String? = null): Boolean {
        val item = findInvItem(name)
        if (!item.valid()) return false
        return if (action != null) item.interact(action) || item.click() else item.click()
    }

    fun useInvItem(name: String): Boolean = interactInvItem(name, "Use")
    fun dropInvItem(name: String): Boolean = interactInvItem(name, "Drop")
    fun eatInvItem(name: String): Boolean = interactInvItem(name, "Eat")
    fun drinkInvItem(name: String): Boolean = interactInvItem(name, "Drink")

    fun equipInvItem(name: String): Boolean {
        val item = findInvItem(name)
        if (!item.valid()) return false
        val equipAction = item.actions().firstOrNull {
            it.equals("Wear", true) || it.equals("Wield", true) || it.equals("Equip", true)
        }
        return if (equipAction != null) item.interact(equipAction) else item.click()
    }

    // -------------------------------------------------------------------------
    // Equipment
    // -------------------------------------------------------------------------

    fun hasEquipped(name: String): Boolean = Equipment.stream().name(name).isNotEmpty()
    fun hasAllEquipped(vararg names: String): Boolean = names.all { hasEquipped(it) }

    fun unequipItem(name: String): Boolean {
        val item = Equipment.stream().name(name).first()
        if (!item.valid()) return false
        return item.interact("Remove") || item.click()
    }

    // -------------------------------------------------------------------------
    // Wait Helpers
    // -------------------------------------------------------------------------

    fun waitUntilIdle(timeoutMs: Int = 5000): Boolean {
        val attempts = timeoutMs / 150
        return Condition.wait({ Players.local().animation() == -1 && !Players.local().inMotion() }, 150, attempts)
    }

    fun waitUntilAtTile(tile: Tile, tolerance: Int = 1, timeoutMs: Int = 5000): Boolean {
        val attempts = timeoutMs / 150
        return Condition.wait({ Players.local().tile().distanceTo(tile) <= tolerance }, 150, attempts)
    }

    fun waitUntilInArea(area: Area, timeoutMs: Int = 5000): Boolean {
        val attempts = timeoutMs / 150
        return Condition.wait({ area.contains(Players.local()) }, 150, attempts)
    }

    // -------------------------------------------------------------------------
    // Explicit ID-based methods (use only when IDs are required)
    // -------------------------------------------------------------------------

    object ById {
        fun findObject(id: Int): GameObject = Objects.stream().id(id).nearest().first()
        fun findNpc(id: Int): Npc = Npcs.stream().id(id).nearest().first()


        fun interactObject(id: Int, action: String? = null): Boolean {
            val obj = findObject(id)
            if (!obj.valid()) return false
            return if (action != null) obj.interact(action) || obj.click() else obj.click()
        }

        fun interactNpc(id: Int, action: String? = null): Boolean {
            val npc = findNpc(id)
            if (!npc.valid()) return false
            return if (action != null) npc.interact(action) || npc.click() else npc.click()
        }

        fun interactInvItem(id: Int, action: String? = null): Boolean {
            val item = findInvItem(id)
            if (!item.valid()) return false
            return if (action != null) item.interact(action) || item.click() else item.click()
        }
    }
}