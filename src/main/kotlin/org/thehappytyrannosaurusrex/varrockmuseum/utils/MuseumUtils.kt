package org.thehappytyrannosaurusrex.varrockmuseum.utils

import org.powbot.api.Condition
import org.powbot.api.Random
import org.powbot.api.rt4.*
import org.powbot.mobile.script.ScriptManager
import org.thehappytyrannosaurusrex.api.chat.DialogueHandler.handleDialogue
import org.thehappytyrannosaurusrex.api.chat.DialogueHandler.dialogueInput
import org.thehappytyrannosaurusrex.api.inventory.DropUtils
import org.thehappytyrannosaurusrex.api.utils.InventoryUtils.invContains
import org.thehappytyrannosaurusrex.api.utils.InventoryUtils.anyInInv
import org.thehappytyrannosaurusrex.api.utils.InventoryUtils.findInvItem
import org.thehappytyrannosaurusrex.api.utils.InventoryUtils.countInInv
import org.thehappytyrannosaurusrex.api.utils.InventoryUtils.fullInv
import org.thehappytyrannosaurusrex.api.utils.InventoryUtils.freeSlots
import org.thehappytyrannosaurusrex.api.utils.InventoryUtils.setContainsLower
import org.thehappytyrannosaurusrex.api.utils.InteractionUtils.findObject
import org.thehappytyrannosaurusrex.api.utils.InteractionUtils.interactObject
import org.thehappytyrannosaurusrex.api.utils.Logger
import org.thehappytyrannosaurusrex.varrockmuseum.config.LampSkill
import org.thehappytyrannosaurusrex.varrockmuseum.data.MuseumConstants as C


object MuseumUtils {

    private const val SCRIPT_NAME = "Varrock Museum Cleaner"

    private fun log(tag: String, message: String) = Logger.info(SCRIPT_NAME, tag, message)

    // Lowercase sets for performance
    private val cleaningToolsLc = C.CLEANING_TOOLS.map { it.lowercase() }.toSet()
    private val leatherEquipmentLc = C.LEATHER_EQUIPMENT.map { it.lowercase() }.toSet()
    private val commonArtefactsLc = C.COMMON_ARTEFACT_NAMES.map { it.lowercase() }.toSet()
    private val uniqueArtefactsLc = C.UNIQUE_ARTEFACT_NAMES.map { it.lowercase() }.toSet()
    private val allJunkLc = C.ALL_JUNK_ITEMS.map { it.lowercase() }.toSet()
    private val scriptKeepLc = C.SCRIPT_KEEP_ITEMS.map { it.lowercase() }.toSet()
    private val allowedInvLc = C.ALLOWED_INVENTORY_ITEMS.map { it.lowercase() }.toSet()

    // Script Running Check to stop inf loop
    private fun isScriptRunning(): Boolean {
        return try {
            !ScriptManager.isStopping() && !ScriptManager.isPaused()
        } catch (_: Throwable) {
            true
        }
    }

    private fun safeWait(condition: () -> Boolean, intervalMs: Int = 300, maxAttempts: Int = 30): Boolean {
        var attempts = 0
        while (attempts < maxAttempts && isScriptRunning()) {
            if (condition()) return true
            Condition.sleep(intervalMs)
            attempts++
        }
        return condition() // Final check
    }


    // Quest / Requirements Checks
    fun isDigSiteCompleted(): Boolean {
        return try {
            Quests.Quest.THE_DIG_SITE.completed()
        } catch (t: Throwable) {
            log("QUEST", "Error checking quest: ${t.message}. Assuming completed.")
            true
        }
    }

    fun isSkillLevelSufficient(lampSkill: LampSkill): Boolean {
        val trackedSkill = lampSkill.trackedSkill ?: return true // Sailing etc - skip check
        return try {
            val lvl = Skills.realLevel(trackedSkill.index)
            if (lvl < 10) {
                log("CHECK", "${lampSkill.displayName} level is $lvl (<10). Too low for lamps.")
                false
            } else true
        } catch (t: Throwable) {
            log("CHECK", "Failed to read level: ${t.message}")
            true
        }
    }

    fun getCurrentLevel(lampSkill: LampSkill): Int {
        val trackedSkill = lampSkill.trackedSkill ?: return 0
        return try {
            Skills.realLevel(trackedSkill.index)
        } catch (_: Throwable) {
            0
        }
    }

    fun hasReachedTargetLevel(lampSkill: LampSkill, targetLevel: Int): Boolean {
        if (targetLevel <= 0) return false
        return getCurrentLevel(lampSkill) >= targetLevel
    }

    // Location Checks
    fun isInsideCleaningArea(): Boolean {
        val me = Players.local()
        return me.valid() && C.CLEANING_AREA.contains(me.tile())
    }

    // Inventory State Checks
    fun hasLamps(): Boolean =
        anyInInv(C.ANTIQUE_LAMP, C.LAMP)

    fun hasUncleanedFinds(): Boolean =
        invContains(C.UNCLEANED_FIND)

    fun hasCommonArtefacts(): Boolean =
        setContainsLower(commonArtefactsLc)

    fun hasAllTools(): Boolean =
        invContains(C.TROWEL) && invContains(C.ROCK_PICK) && invContains(C.SPECIMEN_BRUSH)

    fun hasLeatherBootsEquipped(): Boolean {
        val feet = Equipment.itemAt(C.FEET_SLOT)
        return feet.valid() && feet.name().equals(C.LEATHER_BOOTS, ignoreCase = true)
    }

    fun hasLeatherGlovesEquipped(): Boolean {
        val hands = Equipment.itemAt(C.HAND_SLOT)
        return hands.valid() && hands.name().equals(C.LEATHER_GLOVES, ignoreCase = true)
    }

    fun hasWrongEquipment(): Boolean {
        val hands = Equipment.itemAt(C.HAND_SLOT)
        val feet = Equipment.itemAt(C.FEET_SLOT)

        // Check hands slot - must be empty or leather gloves
        if (hands.valid() && !hands.name().equals(C.LEATHER_GLOVES, ignoreCase = true)) {
            return true
        }

        // Check feet slot - must be empty or leather boots
        if (feet.valid() && !feet.name().equals(C.LEATHER_BOOTS, ignoreCase = true)) {
            return true
        }

        return false
    }

    fun hasJunkToDrop(userKeepItems: Set<String>): Boolean {
        val userKeepLc = userKeepItems.map { it.lowercase() }.toSet()
        return Inventory.stream().any { item ->
            val nameLc = item.name().lowercase()
            allJunkLc.contains(nameLc) && !scriptKeepLc.contains(nameLc) && !userKeepLc.contains(nameLc)
        }
    }

    fun hasItemsToBank(userKeepItems: Set<String>): Boolean {
        val userKeepLc = userKeepItems.map { it.lowercase() }.toSet()
        return Inventory.stream().any { item ->
            val nameLc = item.name().lowercase()
            !allowedInvLc.contains(nameLc) &&
                    !cleaningToolsLc.contains(nameLc) &&
                    !leatherEquipmentLc.contains(nameLc) &&
                    !userKeepLc.contains(nameLc)
        }
    }

    // A 'clean' inventory = max 1 of each tool + no items that need banking
    fun hasCleanInventory(userKeepItems: Set<String>): Boolean {
        // Check for duplicate tools
        if (countInInv(C.TROWEL) > 1) return false
        if (countInInv(C.ROCK_PICK) > 1) return false
        if (countInInv(C.SPECIMEN_BRUSH) > 1) return false

        // Check for items that need banking
        if (hasItemsToBank(userKeepItems)) return false

        // Check for wrong equipment
        if (hasWrongEquipment()) return false

        return true
    }


    // A 'semi-clean' inventory = clean but may have uncleaned finds or common artefacts
    fun hasSemiCleanInventory(userKeepItems: Set<String>): Boolean {
        if (!hasCleanInventory(userKeepItems)) return false
        return hasUncleanedFinds() || hasCommonArtefacts()
    }




    // Drop all junk items (unique artefacts, storage rewards, unremarkable finds).
    // Respects user's keep items and script keep items.
    fun dropJunkItems(userKeepItems: Set<String>): Boolean {
        val userKeepLc = userKeepItems.map { it.lowercase() }.toSet()
        val combinedKeep = scriptKeepLc + userKeepLc

        val toDrop = Inventory.stream().filter { item ->
            val nameLc = item.name().lowercase()
            allJunkLc.contains(nameLc) && !combinedKeep.contains(nameLc)
        }.toList()

        if (toDrop.isEmpty()) {
            log("DROP", "No junk to drop")
            return false
        }

        log("DROP", "Dropping ${toDrop.size} junk items")
        DropUtils.enableTapToDrop()
        DropUtils.ensureInventoryTab()
        DropUtils.dropItems(toDrop)
        return true
    }

    // Drop duplicate tools (keep only 1 of each)
    fun dropDuplicateTools(): Boolean {
        var dropped = false

        C.CLEANING_TOOLS.forEach { toolName ->
            val tools = Inventory.stream().name(toolName).toList()
            if (tools.size > 1) {
                // Drop all but the first one
                tools.drop(1).forEach { tool ->
                    if (tool.valid() && isScriptRunning()) {
                        DropUtils.dropItem(tool)
                        Condition.sleep(Random.nextInt(200, 400))
                        dropped = true
                    }
                }
            }
        }

        return dropped
    }



    // Equip leather gloves if in inventory and not equipped
    fun equipLeatherGloves(): Boolean {
        if (hasLeatherGlovesEquipped()) return true

        val gloves = findInvItem(C.LEATHER_GLOVES)
        if (!gloves.valid()) return false

        log("EQUIP", "Equipping leather gloves")
        val equipped = gloves.interact("Wear") || gloves.click()
        if (equipped) {
            safeWait({ hasLeatherGlovesEquipped() }, 300, 10)
        }
        return hasLeatherGlovesEquipped()
    }

    // Equip leather boots if in inventory and not equipped
    fun equipLeatherBoots(): Boolean {
        if (hasLeatherBootsEquipped()) return true

        val boots = findInvItem(C.LEATHER_BOOTS)
        if (!boots.valid()) return false

        log("EQUIP", "Equipping leather boots")
        val equipped = boots.interact("Wear") || boots.click()
        if (equipped) {
            safeWait({ hasLeatherBootsEquipped() }, 300, 10)
        }
        return hasLeatherBootsEquipped()
    }

    // Unequip item from a slot
    fun unequipSlot(slot: Equipment.Slot): Boolean {
        val item = Equipment.itemAt(slot)
        if (!item.valid()) return true

        log("UNEQUIP", "Unequipping ${item.name()} from ${slot.name}")
        return item.interact("Remove") || item.click()
    }


    // Bank items that shouldn't be in inventory at Varrock East Bank (not tools/leather/finds/keep items)
    fun bankUnwantedItems(userKeepItems: Set<String>): Boolean {
        val userKeepLc = userKeepItems.map { it.lowercase() }.toSet()
        val keepSet = allowedInvLc + cleaningToolsLc + leatherEquipmentLc + userKeepLc

        // Walk to bank if needed
        if (!Bank.present()) {
            log("BANK", "Walking to Varrock East Bank")
            Movement.moveTo(C.VARROCK_EAST_BANK_TILE)
            return false
        }

        if (!Bank.opened()) {
            log("BANK", "Opening bank")
            Bank.open()
            safeWait({ Bank.opened() }, 300, 10)
            return false
        }

        // Deposit unwanted items
        var deposited = false
        Inventory.stream().forEach { item ->
            if (!isScriptRunning()) return false
            val nameLc = item.name().lowercase()
            if (!keepSet.contains(nameLc) && item.valid()) {
                Bank.deposit(item.id(), Bank.Amount.ALL)
                Condition.sleep(Random.nextInt(200, 400))
                deposited = true
            }
        }

        if (deposited) {
            log("BANK", "Deposited unwanted items")
        }

        Bank.close()
        return !hasItemsToBank(userKeepItems)
    }


    // Bank wrong equipment from hands/feet slots
    fun bankWrongEquipment(): Boolean {
        if (!hasWrongEquipment()) return true

        // Walk to bank if needed
        if (!Bank.present()) {
            log("BANK", "Walking to Varrock East Bank")
            Movement.moveTo(C.VARROCK_EAST_BANK_TILE)
            return false
        }

        if (!Bank.opened()) {
            log("BANK", "Opening bank")
            Bank.open()
            safeWait({ Bank.opened() }, 300, 10)
            return false
        }

        // Unequip and deposit wrong items
        val hands = Equipment.itemAt(C.HAND_SLOT)
        if (hands.valid() && !hands.name().equals(C.LEATHER_GLOVES, ignoreCase = true)) {
            log("BANK", "Unequipping ${hands.name()} from hands")
            unequipSlot(C.HAND_SLOT)
            Condition.sleep(Random.nextInt(300, 500))
            val invItem = Inventory.stream().name(hands.name()).first()
            if (invItem.valid()) {
                Bank.deposit(invItem.id(), Bank.Amount.ALL)
            }
        }

        val feet = Equipment.itemAt(C.FEET_SLOT)
        if (feet.valid() && !feet.name().equals(C.LEATHER_BOOTS, ignoreCase = true)) {
            log("BANK", "Unequipping ${feet.name()} from feet")
            unequipSlot(C.FEET_SLOT)
            Condition.sleep(Random.nextInt(300, 500))
            val invItem = Inventory.stream().name(feet.name()).first()
            if (invItem.valid()) {
                Bank.deposit(invItem.id(), Bank.Amount.ALL)
            }
        }

        Bank.close()
        return !hasWrongEquipment()
    }


    // Travel to museum cleaning area via door or gate (whichever is closest)
    fun travelToMuseum(): Boolean {
        if (isInsideCleaningArea()) {
            log("TRAVEL", "Already inside cleaning area")
            return true
        }

        val me = Players.local()
        if (!me.valid()) return false

        val myTile = me.tile()
        val distToDoor = myTile.distanceTo(C.BACK_DOOR_TILE)

        // If far from the door, walk closer first
        if (distToDoor > 5) {
            log("TRAVEL", "Walking to museum door area")
            Movement.walkTo(C.BACK_DOOR_TILE)
            // Wait until close enough to attempt the door
            safeWait({ Players.local().tile().distanceTo(C.BACK_DOOR_TILE) < 15 }, 600, 20)
            return false
        }

        // Try entering via the door
        return enterViaDoor()
    }


    private fun enterViaDoor(): Boolean {
        val door = Objects.stream().within(10).name(C.MUSEUM_DOOR).action("Open").nearest().first()
        if (!door.valid()) {
            log("TRAVEL", "No door found, walking closer")
            Movement.walkTo(C.BACK_DOOR_TILE)
            return false
        }

        log("TRAVEL", "Opening door")
        door.interact("Open") || door.click()
        return safeWait({ isInsideCleaningArea() }, 300, 15)
    }


    // Take tools from the tool rack. Handles dialogue popup.
    fun takeTools(): Boolean {
        if (hasAllTools()) {
            log("KIT", "All tools already present")
            return true
        }

        val toolsObj = findObject(C.TOOL_OBJECT)
        if (!toolsObj.valid()) {
            log("KIT", "No tools object found")
            return false
        }

        log("KIT", "Taking tools")
        interactObject(C.TOOL_OBJECT, "Take") // assume this returns immediately; safeWait below checks result

        // Wait for any dialogue that might appear (bounded)
        Condition.sleep(Random.nextInt(400, 700))
        if (Chat.chatting() || Chat.canContinue()) {
            Chat.continueChat("Yes")
        }

        // Wait for the tools to appear in inventory (bounded)
        val gotTools = safeWait({ hasAllTools() }, 300, 30) // ~9s

        if (gotTools) {
            Condition.sleep(Random.nextInt(300, 500))
            equipLeatherBoots()
            equipLeatherGloves()
            dropDuplicateTools()
        } else {
            log("KIT", "Failed to obtain tools after interaction")
        }

        return hasAllTools()
    }



    // Use antique lamp on the selected skill
    fun useAntiqueLamp(lampSkill: LampSkill): Boolean {
        val lamp = findObject(C.ANTIQUE_LAMP); findObject(C.LAMP)
        if (!lamp.valid()) return false

        log("LAMP", "Using Antique lamp on ${lampSkill.displayName}")
        lamp.interact("Rub")

        // Wait for lamp widget to appear
        if (!safeWait({ Widgets.widget(240).valid() }, 300, 10)) {
            log("LAMP", "Lamp widget did not appear")
            return false
        }

        // Try to find skill component by widget index first
        var skillComponent = Widgets.widget(240).component(lampSkill.widgetIndex)

        // Fallback: search by text using Components.stream()
        if (!skillComponent.valid()) {
            log("LAMP", "Widget index ${lampSkill.widgetIndex} invalid, searching by text")
            skillComponent = Components.stream()
                .widget(240)
                .textContains(lampSkill.displayName)
                .viewable()
                .first()
        }

        if (!skillComponent.valid()) {
            log("LAMP", "Skill component not found for ${lampSkill.displayName}")
            return false
        }

        skillComponent.click()
        Condition.sleep(Random.nextInt(300, 500))

        val confirmBtn = Widgets.widget(240).component(27)
        if (confirmBtn.valid()) {
            confirmBtn.click()
            Condition.sleep(Random.nextInt(400, 600))
            return true
        }

        return false
    }

    // Clean uncleaned finds at the specimen table.
    // Clicks once and waits until cleaning is complete.
    fun cleanSpecimen(): Boolean {
        if (!hasUncleanedFinds()) return false

        val table = findObject(C.SPECIMEN_TABLE)
        if (!table.valid()) {
            log("CLEAN", "No specimen table found")
            return false
        }

        log("CLEAN", "Cleaning finds at specimen table")
        if (table.click() || table.interact("Add")) {
            Condition.wait({ Players.local().animation() == -1 && !hasUncleanedFinds()}, 1800, 28)
        }
        return true
    }


    // Store common artefacts in the storage crate.
    // Clicks once and waits until all artefacts are deposited.
    fun storeFinds(): Boolean {
        if (!hasCommonArtefacts()) return false
        if (!isInsideCleaningArea()) return travelToMuseum()

        val crate = findObject(C.STORAGE_CRATE)
        if (!crate.valid()) {
            log("STORE", "No storage crate found")
            return false
        }
        log("STORE", "Storing finds in crate")

        if (crate.click() || crate.interact("Add")) {
            Condition.wait({ Players.local().animation() == -1 && (!hasCommonArtefacts()) }, 1800, 28)
            Condition.sleep(Random.nextInt(1200, 1800))
        }
        return true
    }

    // Collect uncleaned finds from specimen rocks (standard mode).
    // Clicks once and waits for inventory to fill.
    fun collectUncleanedFinds(): Boolean {
        val rocks = findObject(C.SPECIMEN_ROCK)
        if (!rocks.valid()) {
            log("COLLECT", "No specimen rocks found")
            return false
        }

        log("COLLECT", "Collecting uncleaned finds")
        if (rocks.click() || rocks.interact("Use")) {
            return safeWait({
                fullInv() || !isScriptRunning()
            }, 300, 100)
        }

        return false
    }

    fun collectUncleanedFinds1T(): Boolean {
        if (fullInv()) {
            log("COLLECT 1T", "Inventory full")
            return false
        }

        log("COLLECT 1T", "1T clicking specimen rocks")

        var attempts = 0
        val maxAttempts = 200

        while (!fullInv() && freeSlots() != 0 && isScriptRunning() && attempts < maxAttempts) {
            val rock = findObject(C.SPECIMEN_ROCK)
            if (!rock.valid()) {
                log("COLLECT 1T", "No specimen rocks found (iteration $attempts)")
                return false
            }

            val clicked = try {
                rock.click() || rock.interact("Take")
            } catch (t: Throwable) {
                false
            }

            Condition.sleep(Random.nextInt(200, 400))

            if (clicked) {
                Condition.sleep(Random.nextInt(150, 300))
            }
            attempts++
        }
        return !fullInv() && freeSlots() != 0
    }
}