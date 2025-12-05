package org.thehappytyrannosaurusrex.varrockmuseum.utils

import org.powbot.api.Condition
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.Players
import InventoryUtils
import org.powbot.api.Random
import org.powbot.api.rt4.Item
import org.powbot.api.rt4.Quests
import org.powbot.api.rt4.Skills
import org.powbot.api.rt4.Widgets
import org.thehappytyrannosaurusrex.api.inventory.DropUtils
import org.thehappytyrannosaurusrex.api.utils.BankUtils
import org.thehappytyrannosaurusrex.api.utils.Logger
import org.thehappytyrannosaurusrex.varrockmuseum.config.Config
import org.thehappytyrannosaurusrex.varrockmuseum.config.LampSkill
import org.powbot.api.rt4.*
import org.thehappytyrannosaurusrex.varrockmuseum.data.MuseumConstants as C


object MuseumUtils {

    private const val SCRIPT_NAME = "Varrock Museum"

    private fun log(tag: String, message: String) {
        Logger.info(SCRIPT_NAME, tag, message)
    }

    enum class Stage {
        PRE_BANK_CLEAN,
        TRAVEL_TO_MUSEUM,
        ENSURE_KIT,
        INSIDE_NORMALISE,
        MAIN_LOOP
    }

    var stage: Stage = Stage.PRE_BANK_CLEAN

    lateinit var config: Config
        private set

    val lampSkill: LampSkill
        get() = config.lampSkill

    private val uniqueArtefactNamesLc = C.UNIQUE_ARTEFACT_NAMES.map { it.lowercase() }.toSet()
    private val commonArtefactNamesLc = C.COMMON_ARTEFACT_NAMES.map { it.lowercase() }.toSet()

    fun handlePreBankCleanup() {
        log("STAGE", "PRE_BANK_CLEAN: checking inventory")

        val hasJunk = InventoryUtils.hasNonAllowedItemsByNames(
            allowedNames = C.INITIAL_KEEP_FOR_BANK,
            additionalPredicates = emptyList()
        )

        if (hasJunk) {
            log("BANK", "Banking non-essential items at Varrock East")
            val done = BankUtils.depositAllExceptByName(
                keepItemNames = C.INITIAL_KEEP_FOR_BANK,
                preferredBankTile = C.VARROCK_EAST_BANK_TILE
            )
            if (done) {
                log("BANK", "Pre-bank cleanup complete")
                stage = Stage.TRAVEL_TO_MUSEUM
            }
        } else {
            log("STAGE", "Inventory already clean")
            stage = Stage.TRAVEL_TO_MUSEUM
        }
    }

    fun handleTravelToMuseum() {
        log("STAGE", "TRAVEL_TO_MUSEUM")

        val me = Players.local()
        if (!me.valid()) return

        if (C.CLEANING_AREA.contains(me.tile())) {
            log("TRAVEL", "Already inside cleaning area")
            stage = Stage.ENSURE_KIT
            return
        }

        if (me.tile().distanceTo(C.MUSEUM_TARGET_TILE) > 5) {
            log("TRAVEL", "Walking to museum")
            Movement.walkTo(C.MUSEUM_TARGET_TILE)
            Condition.wait({ C.CLEANING_AREA.contains(Players.local().tile()) }, 300, 20)
        }

        handleDoorAndGate()

        if (C.CLEANING_AREA.contains(Players.local().tile())) {
            stage = Stage.ENSURE_KIT
        }
    }

    fun handleEnsureKit() {
        log("STAGE", "ENSURE_KIT")

        val hasTrowel = Inventory.stream().name(C.TROWEL).isNotEmpty()
        val hasRockPick = Inventory.stream().name(C.ROCK_PICK).isNotEmpty()
        val hasBrush = Inventory.stream().name(C.SPECIMEN_BRUSH).isNotEmpty()

        if (hasTrowel && hasRockPick && hasBrush) {
            log("KIT", "All tools present")
            stage = Stage.INSIDE_NORMALISE
            return
        }

        log("KIT", "Missing tools, searching tools object")
        val tools = Objects.stream().name(C.TOOL_OBJECT).nearest().first()
        if (tools.valid()) {
            tools.interact("Take")
            Condition.wait({
                Inventory.stream().name(C.TROWEL).isNotEmpty() &&
                        Inventory.stream().name(C.ROCK_PICK).isNotEmpty() &&
                        Inventory.stream().name(C.SPECIMEN_BRUSH).isNotEmpty()
            }, 300, 10)
        }
    }

    fun handleInsideNormalise() {
        log("STAGE", "INSIDE_NORMALISE")
        dropJunkItems("normalisation")
        stage = Stage.MAIN_LOOP
    }

    private fun hasUncleanedFinds(): Boolean =
        Inventory.stream().name(C.UNCLEANED_FIND).isNotEmpty()

    private fun hasAntiqueLamps(): Boolean =
        Inventory.stream().name(C.ANTIQUE_LAMP).isNotEmpty()

    private fun hasFindsToStore(): Boolean =
        Inventory.stream().any { item ->
            val name = item.name().lowercase()
            name in uniqueArtefactNamesLc || name in commonArtefactNamesLc
        }

    private fun hasJunkToDrop(): Boolean = computeJunkItems().isNotEmpty()

    private fun computeJunkItems(): List<Item> {
        val junk = mutableListOf<Item>()
        val keepNames = config.keepItemNames

        Inventory.stream().forEach { item ->
            if (!item.valid() || item.id() == -1) return@forEach
            val name = item.name()
            val normName = name.lowercase().trim()

            if (normName in keepNames) return@forEach
            if (name in C.KEEP_WHEN_DROPPING) return@forEach
            if (normName in uniqueArtefactNamesLc) return@forEach
            if (normName in commonArtefactNamesLc) return@forEach

            junk.add(item)
        }
        return junk
    }

    private fun dropJunkItems(reason: String) {
        val toDrop = computeJunkItems()
        if (toDrop.isEmpty()) {
            log("DROP", "No junk to drop for $reason")
            return
        }

        log("DROP", "Dropping ${toDrop.size} junk items for $reason")
        DropUtils.ensureInventoryTab()

        toDrop.shuffled().forEach { item ->
            if (!item.valid()) return@forEach
            if (DropUtils.dropItem(item)) {
                Condition.sleep(Random.nextInt(350, 650))
            }
        }
    }

    private fun cleanSpecimen() {
        val table = Objects.stream().name(C.SPECIMEN_TABLE).nearest().first()
        if (!table.valid()) {
            log("CLEAN", "No specimen table found")
            return
        }

        val uncleaned = Inventory.stream().name(C.UNCLEANED_FIND).first()
        if (!uncleaned.valid()) return

        uncleaned.useOn(table)
        Condition.wait({ !Inventory.stream().name(C.UNCLEANED_FIND).isNotEmpty() || Inventory.isFull() }, 300, 30)
    }

    private fun storeFinds() {
        val crate = Objects.stream().name(C.STORAGE_CRATE).nearest().first()
        if (!crate.valid()) {
            log("STORE", "No storage crate found")
            return
        }

        crate.interact("Search")
        Condition.wait({ !hasFindsToStore() }, 300, 20)
    }

    private fun collectUncleanedFinds() {
        val rocks = Objects.stream().name(C.SPECIMEN_ROCK).nearest().first()
        if (!rocks.valid()) {
            log("COLLECT", "No specimen rocks found")
            return
        }

        rocks.interact("Take")
        Condition.wait({ Inventory.stream().name(C.UNCLEANED_FIND).isNotEmpty() || Inventory.isFull() }, 300, 30)
    }

    private fun useAntiqueLamp() {
        val lamp = Inventory.stream().name(C.ANTIQUE_LAMP).first()
        if (!lamp.valid()) return

        log("LAMP", "Using Antique lamp on ${lampSkill.displayName}")
        lamp.interact("Rub")

        Condition.wait({ Widgets.widget(240).valid() }, 300, 10)

        val skillComponent = Widgets.widget(240).component(lampSkill.widgetIndex)
        if (skillComponent.valid()) {
            skillComponent.click()
            Condition.sleep(Random.nextInt(300, 500))

            val confirmBtn = Widgets.widget(240).component(27)
            if (confirmBtn.valid()) {
                confirmBtn.click()
                lampsUsed++
                Condition.sleep(Random.nextInt(400, 600))
            }
        }
    }

    private fun shouldBankKeepItems(): Boolean {
        if (!Inventory.isFull()) return false
        if (hasUncleanedFinds()) return false
        if (hasFindsToStore()) return false
        if (hasJunkToDrop()) return false

        val keepNames = config.keepItemNames
        if (keepNames.isEmpty()) return false

        return Inventory.stream().any { it.valid() && it.name().lowercase().trim() in keepNames }
    }

    private fun handleDoorAndGate() {
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

    fun isDigSiteCompleted(): Boolean {
        return try {
            Quests.Quest.THE_DIG_SITE.completed()
        } catch (t: Throwable) {
            log("QUEST", "Error checking quest: ${t.message}. Assuming completed.")
            true
        }
    }

    private fun checkSelectedSkillLevelOrStop(): Boolean {
        val trackedSkill = lampSkill.trackedSkill ?: return true

        return try {
            val lvl = Skills.realLevel(trackedSkill.index)
            if (lvl < 10) {
                log("STARTUP", "${lampSkill.displayName} level is $lvl (<10). Stopping.")
                controller.stop()
                false
            } else true
        } catch (t: Throwable) {
            log("STARTUP", "Failed to read level: ${t.message}")
            true
        }
    }

    private fun checkTapToDrop() {
        val enabled = DropUtils.isTapToDropEnabled()
        if (!enabled) {
            log("SETUP", "Tap-to-drop is disabled. Attempting to enable.")
            DropUtils.enableTapToDrop()
        }
    }

}