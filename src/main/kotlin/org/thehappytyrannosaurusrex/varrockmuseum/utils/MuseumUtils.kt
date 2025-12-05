package org.thehappytyrannosaurusrex.varrockmuseum.utils

import org.powbot.api.Condition
import org.powbot.api.Random
import org.powbot.api.rt4.*
import org.thehappytyrannosaurusrex.api.inventory.DropUtils
import org.thehappytyrannosaurusrex.api.utils.BankUtils
import org.thehappytyrannosaurusrex.api.utils.Logger
import org.thehappytyrannosaurusrex.api.utils.InventoryUtils
import org.thehappytyrannosaurusrex.varrockmuseum.config.LampSkill
import org.thehappytyrannosaurusrex.varrockmuseum.data.MuseumConstants as C

/**
 * Stateless utility functions for Varrock Museum script.
 * All game logic and interactions are contained here.
 */
object MuseumUtils {

    private const val SCRIPT_NAME = "Varrock Museum"

    private fun log(tag: String, message: String) = Logger.info(SCRIPT_NAME, tag, message)

    // Pre-computed lowercase sets for performance
    private val uniqueArtefactNamesLc = C.UNIQUE_ARTEFACT_NAMES.map { it.lowercase() }.toSet()
    private val commonArtefactNamesLc = C.COMMON_ARTEFACT_NAMES.map { it.lowercase() }.toSet()
    private val keepWhenDroppingLc = C.KEEP_WHEN_DROPPING.map { it.lowercase() }.toSet()

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
        val trackedSkill = lampSkill.trackedSkill ?: return true
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

    // Location Checks
    fun isInsideCleaningArea(): Boolean {
        val me = Players.local()
        return me.valid() && C.CLEANING_AREA.contains(me.tile())
    }

    fun needsToTravelToMuseum(): Boolean {
        val me = Players.local()
        return me.valid() && !C.CLEANING_AREA.contains(me.tile())
    }

    // Inventory State Checks
    fun hasUncleanedFinds(): Boolean =
        InventoryUtils.invContains(C.UNCLEANED_FIND)

    fun hasAntiqueLamps(): Boolean =
        InventoryUtils.invContains(C.ANTIQUE_LAMP)

    fun hasFindsToStore(): Boolean =
        Inventory.stream().any { item ->
            val name = item.name().lowercase()
            name in uniqueArtefactNamesLc || name in commonArtefactNamesLc
        }

    fun hasAllTools(): Boolean {
        val hasTrowel = InventoryUtils.invContains(C.TROWEL)
        val hasRockPick = InventoryUtils.invContains(C.ROCK_PICK)
        val hasBrush = InventoryUtils.invContains(C.SPECIMEN_BRUSH)
        return hasTrowel && hasRockPick && hasBrush
    }

    fun hasJunkToDrop(userKeepNames: Set<String>): Boolean =
        computeJunkItems(userKeepNames).isNotEmpty()

    fun shouldBankKeepItems(userKeepNames: Set<String>): Boolean {
        if (!Inventory.isFull()) return false
        if (hasUncleanedFinds()) return false
        if (hasFindsToStore()) return false
        if (hasJunkToDrop(userKeepNames)) return false
        if (userKeepNames.isEmpty()) return false
        return Inventory.stream().any { it.valid() && it.name().lowercase().trim() in userKeepNames }
    }

    fun computeJunkItems(userKeepNames: Set<String>): List<Item> {
        val junk = mutableListOf<Item>()

        Inventory.stream().forEach { item ->
            if (!item.valid() || item.id() == -1) return@forEach

            val name = item.name()
            val normName = name.lowercase().trim()

            // Skip user-kept items
            if (normName in userKeepNames) return@forEach
            // Skip script-essential items
            if (normName in keepWhenDroppingLc) return@forEach
            // Skip artefacts (will be stored)
            if (normName in uniqueArtefactNamesLc) return@forEach
            if (normName in commonArtefactNamesLc) return@forEach

            junk.add(item)
        }
        return junk
    }

    fun hasNonEssentialItems(): Boolean {
        val allowedLc = C.INITIAL_KEEP_FOR_BANK.map { it.lowercase() }.toSet()
        return Inventory.stream().any { item ->
            item.valid() && item.name().lowercase().trim() !in allowedLc
        }
    }

    // Actions - Travel & Navigation

    fun travelToMuseum(): Boolean {
        val me = Players.local()
        if (!me.valid()) return false

        if (C.CLEANING_AREA.contains(me.tile())) {
            log("TRAVEL", "Already inside cleaning area")
            return true
        }

        if (me.tile().distanceTo(C.MUSEUM_TARGET_TILE) > 5) {
            log("TRAVEL", "Walking to museum")
            Movement.walkTo(C.MUSEUM_TARGET_TILE)
            Condition.wait({ C.CLEANING_AREA.contains(Players.local().tile()) }, 300, 20)
        }

        handleDoorAndGate()
        return C.CLEANING_AREA.contains(Players.local().tile())
    }

    fun handleDoorAndGate() {
        C.DOOR_TILES.forEach { doorTile ->
            val door = Objects.stream().at(doorTile).name(C.MUSEUM_DOOR).first()
            if (door.valid() && door.actions().contains("Open")) {
                door.interact("Open")
                Condition.sleep(Random.nextInt(400, 700))
            }
        }

        C.GATE_TILES.forEach { gateTile ->
            val gate = Objects.stream().at(gateTile).name(C.MUSEUM_GATE).first()
            if (gate.valid() && gate.actions().contains("Open")) {
                gate.interact("Open")
                Condition.sleep(Random.nextInt(400, 700))
            }
        }
    }

    // =========================================================================
    // Actions - Inventory Management
    // =========================================================================

    /**
     * Drop junk items from inventory
     */
    fun dropJunkItems(userKeepNames: Set<String>, reason: String = "cleanup"): Boolean {
        val toDrop = computeJunkItems(userKeepNames)
        if (toDrop.isEmpty()) {
            log("DROP", "No junk to drop for $reason")
            return false
        }

        log("DROP", "Dropping ${toDrop.size} junk items for $reason")
        DropUtils.ensureInventoryTab()
        DropUtils.dropItems(toDrop)
        return true
    }

    /**
     * Bank all non-essential items (pre-bank cleanup)
     */
    fun bankNonEssentialItems(): Boolean {
        if (!hasNonEssentialItems()) {
            log("BANK", "Inventory already clean")
            return true
        }

        log("BANK", "Banking non-essential items at Varrock East")
        return BankUtils.depositAllExceptByName(
            keepItemNames = C.INITIAL_KEEP_FOR_BANK,
            preferredBankTile = C.VARROCK_EAST_BANK_TILE
        )
    }

    /**
     * Bank user-kept items when inventory is full
     */
    fun bankKeepItems(): Boolean {
        val done = BankUtils.depositAllExceptByName(
            keepItemNames = C.INITIAL_KEEP_FOR_BANK,
            preferredBankTile = C.VARROCK_EAST_BANK_TILE
        )
        if (done) log("BANK", "Deposited keep items")
        return done
    }

    // =========================================================================
    // Actions - Museum Activities
    // =========================================================================

    /**
     * Take cleaning tools from the tools object
     */
    fun takeTools(): Boolean {
        if (hasAllTools()) {
            log("KIT", "All tools present")
            return true
        }

        log("KIT", "Missing tools, searching tools object")
        val tools = Objects.stream().name(C.TOOL_OBJECT).nearest().first()
        if (!tools.valid()) {
            log("KIT", "Tools object not found")
            return false
        }

        tools.interact("Take")
        return Condition.wait({
            Inventory.stream().name(C.TROWEL).isNotEmpty() &&
                    Inventory.stream().name(C.ROCK_PICK).isNotEmpty() &&
                    Inventory.stream().name(C.SPECIMEN_BRUSH).isNotEmpty()
        }, 300, 10)
    }

    /**
     * Clean uncleaned finds at the specimen table
     */
    fun cleanSpecimen(): Boolean {
        val table = Objects.stream().name(C.SPECIMEN_TABLE).nearest().first()
        if (!table.valid()) {
            log("CLEAN", "No specimen table found")
            return false
        }

        val uncleaned = Inventory.stream().name(C.UNCLEANED_FIND).first()
        if (!uncleaned.valid()) return false

        uncleaned.useOn(table)
        return Condition.wait(
            { !Inventory.stream().name(C.UNCLEANED_FIND).isNotEmpty() || Inventory.isFull() },
            300, 30
        )
    }

    /**
     * Store artefacts in the storage crate
     */
    fun storeFinds(): Boolean {
        val crate = Objects.stream().name(C.STORAGE_CRATE).nearest().first()
        if (!crate.valid()) {
            log("STORE", "No storage crate found")
            return false
        }

        crate.interact("Search")
        return Condition.wait({ !hasFindsToStore() }, 300, 20)
    }

    /**
     * Collect uncleaned finds from specimen rocks
     */
    fun collectUncleanedFinds(): Boolean {
        val rocks = Objects.stream().name(C.SPECIMEN_ROCK).nearest().first()
        if (!rocks.valid()) {
            log("COLLECT", "No specimen rocks found")
            return false
        }

        rocks.interact("Take")
        return Condition.wait(
            { Inventory.stream().name(C.UNCLEANED_FIND).isNotEmpty() || Inventory.isFull() },
            300, 30
        )
    }

    /**
     * Use antique lamp on selected skill
     * @return true if lamp was used successfully
     */
    fun useAntiqueLamp(lampSkill: LampSkill): Boolean {
        val lamp = Inventory.stream().name(C.ANTIQUE_LAMP).first()
        if (!lamp.valid()) return false

        log("LAMP", "Using Antique lamp on ${lampSkill.displayName}")
        lamp.interact("Rub")

        if (!Condition.wait({ Widgets.widget(240).valid() }, 300, 10)) {
            log("LAMP", "Lamp widget did not appear")
            return false
        }

        val skillComponent = Widgets.widget(240).component(lampSkill.widgetIndex)
        if (!skillComponent.valid()) {
            log("LAMP", "Skill component not found at index ${lampSkill.widgetIndex}")
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
}