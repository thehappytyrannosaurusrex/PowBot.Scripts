package org.thehappytyrannosaurusrex.varrockmuseum.utils

import org.powbot.api.Condition
import org.powbot.api.Random
import org.powbot.api.rt4.*
import org.thehappytyrannosaurusrex.api.inventory.DropUtils
import org.thehappytyrannosaurusrex.api.utils.BankUtils
import org.thehappytyrannosaurusrex.api.utils.InteractionUtils
import org.thehappytyrannosaurusrex.api.utils.InventoryUtils
import org.thehappytyrannosaurusrex.api.utils.Logger
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
    private val allArtefactNamesLc = uniqueArtefactNamesLc + commonArtefactNamesLc
    private val keepWhenDroppingLc = C.KEEP_WHEN_DROPPING.map { it.lowercase() }.toSet()

    // =========================================================================
    // Quest / Requirements Checks
    // =========================================================================

    /**
     * Check if The Dig Site quest is completed (required for this activity)
     */
    fun isDigSiteCompleted(): Boolean {
        return try {
            Quests.Quest.THE_DIG_SITE.completed()
        } catch (t: Throwable) {
            log("QUEST", "Error checking quest: ${t.message}. Assuming completed.")
            true
        }
    }

    /**
     * Verify skill level is >= 10 for lamp usage. Returns false if too low.
     */
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

    fun getCurrentLevel(lampSkill: LampSkill): Int {
        val trackedSkill = lampSkill.trackedSkill ?: return 0
        return try {
            Skills.realLevel(trackedSkill.index)
        } catch (t: Throwable) {
            0
        }
    }

    fun hasReachedTargetLevel(lampSkill: LampSkill, targetLevel: Int): Boolean {
        if (targetLevel <= 0) return false
        return getCurrentLevel(lampSkill) >= targetLevel
    }

    // =========================================================================
    // Location Checks
    // =========================================================================

    fun isInsideCleaningArea(): Boolean {
        val me = Players.local()
        return me.valid() && C.CLEANING_AREA.contains(me.tile())
    }

    fun needsToTravelToMuseum(): Boolean {
        val me = Players.local()
        return me.valid() && !C.CLEANING_AREA.contains(me.tile())
    }

    // Inventory State Checks

    fun hasUncleanedFinds(): Boolean = InventoryUtils.invContains(C.UNCLEANED_FIND)
    fun hasAntiqueLamps(): Boolean = InventoryUtils.invContains(C.ANTIQUE_LAMP)
    fun hasFindsToStore(): Boolean = InventoryUtils.hasAnyOfNames(allArtefactNamesLc)
    fun hasAllTools(): Boolean = InventoryUtils.allInInv(C.TROWEL, C.ROCK_PICK, C.SPECIMEN_BRUSH)
    fun hasJunkToDrop(userKeepNames: Set<String>): Boolean =
        computeJunkItems(userKeepNames).isNotEmpty()

    fun shouldBankKeepItems(userKeepNames: Set<String>): Boolean {
        if (!InventoryUtils.fullInv()) return false
        if (hasUncleanedFinds()) return false
        if (hasFindsToStore()) return false
        if (hasJunkToDrop(userKeepNames)) return false
        if (userKeepNames.isEmpty()) return false
        return Inventory.stream().any { it.valid() && it.name().lowercase().trim() in userKeepNames }
    }

    /**
     * Compute list of junk items to drop
     */
    fun computeJunkItems(userKeepNames: Set<String>): List<Item> {
        val junk = mutableListOf<Item>()

        Inventory.stream().forEach { item ->
            if (!item.valid() || item.id() == -1) return@forEach

            val normName = item.name().lowercase().trim()

            // Skip user-kept items
            if (normName in userKeepNames) return@forEach
            // Skip script-essential items
            if (normName in keepWhenDroppingLc) return@forEach
            // Skip artefacts (will be stored)
            if (normName in allArtefactNamesLc) return@forEach

            junk.add(item)
        }
        return junk
    }

    /**
     * Check if inventory has non-essential items for banking
     */
    fun hasNonEssentialItems(): Boolean =
        InventoryUtils.hasNonAllowedItemsByNames(C.INITIAL_KEEP_FOR_BANK)

    // =========================================================================
    // Actions - Travel & Navigation (using InteractionUtils)
    // =========================================================================

    /**
     * Walk to the museum cleaning area
     */
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
            InteractionUtils.waitUntilInArea(C.CLEANING_AREA, 6000)
        }

        handleDoorAndGate()
        return C.CLEANING_AREA.contains(Players.local().tile())
    }

    /**
     * Open doors and gates if needed
     */
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
        val interacted = InteractionUtils.interactObject(C.TOOL_OBJECT, "Take")
        if (!interacted) {
            log("KIT", "Tools object not found")
            return false
        }

        return Condition.wait({ hasAllTools() }, 300, 10)
    }

    /**
     * Clean uncleaned finds at the specimen table
     */
    fun cleanSpecimen(): Boolean {
        if (!hasUncleanedFinds()) return false

        val success = InteractionUtils.interactObject(C.SPECIMEN_TABLE)
        if (!success) {
            log("CLEAN", "Failed to use uncleaned find on specimen table")
            return false
        }

        return Condition.wait({ Players.local().animation() == -1 && !hasUncleanedFinds()  }, 2400, 30)
    }

    /**
     * Store artefacts in the storage crate.
     * Interacts ONCE and waits until all artefacts are deposited.
     */
    fun storeFinds(): Boolean {
        if (!hasFindsToStore()) return false

        val crate = InteractionUtils.findObject(C.STORAGE_CRATE)
        if (!crate.valid()) {
            log("STORE", "No storage crate found")
            return false
        }

        // Only interact once
        log("STORE", "Storing finds in crate")
        crate.click()

        // Wait until NO artefacts remain in inventory
        return Condition.wait({ Players.local().animation() == -1 && !hasFindsToStore() }, 2400, 30)
    }

    /**
     * Collect uncleaned finds from specimen rocks (standard mode).
     * Clicks once and waits for inventory to fill.
     */
    fun collectUncleanedFinds(): Boolean {
        val interacted = InteractionUtils.interactObject(C.SPECIMEN_ROCK)
        if (!interacted) {
            log("COLLECT", "No specimen rocks found")
            return false
        }

        return Condition.wait(
            { hasUncleanedFinds() || InventoryUtils.fullInv() },
            300, 30
        )
    }

    /**
     * Collect uncleaned finds using 1-tick clicking (spam click mode).
     * Clicks the rocks every 400-700ms until inventory is full of uncleaned finds.
     *
     * @return true when inventory is full
     */
    fun collectUncleanedFinds1T(): Boolean {
        val rocks = InteractionUtils.findObject(C.SPECIMEN_ROCK)
        if (!rocks.valid()) {
            log("COLLECT", "No specimen rocks found")
            return false
        }

        log("COLLECT", "1T clicking specimen rocks")

        while (!InventoryUtils.fullInv()) {
            // Check if rocks still valid
            val currentRocks = InteractionUtils.findObject(C.SPECIMEN_ROCK)
            if (!currentRocks.valid()) {
                log("COLLECT", "Rocks no longer valid")
                break
            }

            // Click the rocks
            currentRocks.click()

            // Wait 400-700ms before next click
            Condition.sleep(Random.nextInt(400, 700))
        }

        return InventoryUtils.fullInv()
    }

    /**
     * Use antique lamp on selected skill
     * Uses Components.stream() to find skill by text for robustness.
     *
     * @return true if lamp was used successfully
     */
    fun useAntiqueLamp(lampSkill: LampSkill): Boolean {
        val lamp = InventoryUtils.findInvItem(C.ANTIQUE_LAMP)
        if (!lamp.valid()) return false

        log("LAMP", "Using Antique lamp on ${lampSkill.displayName}")
        lamp.interact("Rub")

        // Wait for lamp widget to appear
        if (!Condition.wait({ Widgets.widget(240).valid() }, 300, 10)) {
            log("LAMP", "Lamp widget did not appear")
            return false
        }

        // Try to find skill component by widget index first (faster)
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
}