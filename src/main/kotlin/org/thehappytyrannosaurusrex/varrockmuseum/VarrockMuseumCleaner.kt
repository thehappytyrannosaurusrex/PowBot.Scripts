package org.thehappytyrannosaurusrex.varrockmuseum

import org.powbot.api.script.AbstractScript
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.Condition
import org.powbot.api.Notifications
import org.powbot.mobile.script.ScriptManager
import org.thehappytyrannosaurusrex.api.inventory.DropUtils
import org.thehappytyrannosaurusrex.api.pathing.DaxUtils
import org.thehappytyrannosaurusrex.api.ui.CameraController
import org.thehappytyrannosaurusrex.api.ui.CameraInitProfile
import org.thehappytyrannosaurusrex.api.ui.ViewportUi
import org.thehappytyrannosaurusrex.api.utils.Logger
import org.thehappytyrannosaurusrex.api.utils.PaintUtil
import org.thehappytyrannosaurusrex.api.utils.InventoryUtils.fullInv
import org.thehappytyrannosaurusrex.api.utils.InventoryUtils.invContains
import org.thehappytyrannosaurusrex.varrockmuseum.config.Config
import org.thehappytyrannosaurusrex.varrockmuseum.config.LampSkill
import org.thehappytyrannosaurusrex.varrockmuseum.config.Options
import org.thehappytyrannosaurusrex.varrockmuseum.config.buildConfig
import org.thehappytyrannosaurusrex.varrockmuseum.data.MuseumConstants
import org.thehappytyrannosaurusrex.varrockmuseum.utils.MuseumUtils
import kotlin.random.Random

@ScriptManifest(
    name = "Varrock Museum Cleaner",
    description = "Cleans Varrock Museum specimens and uses Antique lamps on a chosen skill.",
    version = "1.1.2",
    author = "thehappytyrannosaurusrex",
    category = ScriptCategory.Other
)
@ScriptConfiguration.List(
    [
        ScriptConfiguration(
            name = Options.Keys.LAMP_SKILL,
            description = "Which skill should Antique lamps be used on?",
            optionType = OptionType.STRING,
            allowedValues = [
                Options.Values.ATTACK, Options.Values.STRENGTH, Options.Values.DEFENCE,
                Options.Values.RANGED, Options.Values.MAGIC, Options.Values.HITPOINTS,
                Options.Values.PRAYER, Options.Values.AGILITY, Options.Values.HERBLORE,
                Options.Values.THIEVING, Options.Values.CRAFTING, Options.Values.FLETCHING,
                Options.Values.SLAYER, Options.Values.HUNTER, Options.Values.MINING,
                Options.Values.SMITHING, Options.Values.FISHING, Options.Values.COOKING,
                Options.Values.FIREMAKING, Options.Values.WOODCUTTING, Options.Values.FARMING,
                Options.Values.RUNECRAFTING, Options.Values.CONSTRUCTION, Options.Values.SAILING
            ],
            defaultValue = Options.Values.SLAYER
        ),
        ScriptConfiguration(
            name = Options.Keys.KEEP_ITEMS,
            description = "Comma-separated item names to NEVER drop. They can still be banked.",
            optionType = OptionType.STRING,
            defaultValue = "Coins,"
        ),
        ScriptConfiguration(
            name = Options.Keys.STOP_AT_LEVEL,
            description = "Stop at target (0 = never stop).",
            optionType = OptionType.INTEGER,
            defaultValue = "0"
        ),
        ScriptConfiguration(
            name = Options.Keys.ONE_TICK_CLICK,
            description = "1T Click: Click rocks every 400-600ms .",
            optionType = OptionType.BOOLEAN,
            defaultValue = "false"
        )
    ]
)
class VarrockMuseumCleaner : AbstractScript() {

    companion object {
        private const val SCRIPT_NAME = "Varrock Museum Cleaner"
    }

    // Script Stages
    enum class Stage {
        STARTUP_USE_LAMPS,        // Use any lamps at startup
        STARTUP_DROP_JUNK,        // Drop junk items
        STARTUP_BANK_EQUIPMENT,   // Bank wrong equipment
        STARTUP_BANK_ITEMS,       // Bank unwanted items
        TRAVEL_TO_MUSEUM,         // Travel to cleaning area
        ENSURE_TOOLS,             // Get tools and equip leather
        NORMALISE_INVENTORY,      // Process semi-clean inventory
        MAIN_LOOP                 // Main cleaning loop
    }

    // Script State
    private var stage = Stage.STARTUP_USE_LAMPS
    private lateinit var config: Config
    private var startTime: Long = 0L
    var lampsUsed: Int = 0
        private set
    var currentTask: String = "Starting..."
        private set

    // Helpers (lazy init)
    private val cameraController by lazy { CameraController() }
    private val viewportUi by lazy { ViewportUi() }

    // Config accessors
    private val lampSkill: LampSkill get() = config.lampSkill
    private val keepItemNames: Set<String> get() = config.keepItemNames
    private val stopAtLevel: Int get() = config.stopAtLevel
    private val oneTickClick: Boolean get() = config.oneTickClick

    // =========================================================================
    // Lifecycle - onStart
    // =========================================================================

    override fun onStart() {
        try {
            log("STARTUP", "Varrock Museum Cleaner starting...")
            startTime = System.currentTimeMillis()
            currentTask = "Initializing"

            // 1. Parse config
            config = buildConfig(this)
            log("STARTUP", "Lamp skill: ${lampSkill.displayName}")
            log("STARTUP", "Keep items: ${if (keepItemNames.isEmpty()) "(none)" else keepItemNames.joinToString()}")
            if (stopAtLevel > 0) log("STARTUP", "Stop at level: $stopAtLevel")
            if (oneTickClick) log("STARTUP", "1T Click mode: ENABLED")

            cameraController.init(CameraInitProfile(yaw = 190, zoom = 10.0, minPitch = 85, maxPitch = 95))

            DropUtils.enableTapToDrop()
            DropUtils.ensureInventoryTab()
            initPaint()

            if (!MuseumUtils.isInsideCleaningArea()) {
                log("STARTUP", "Not in cleaning area - will travel there after startup checks")
            }
            if (!MuseumUtils.isDigSiteCompleted()) {
                log("STARTUP", "The Dig Site quest is NOT completed. Stopping script.")
                Notifications.showNotification("The Dig Site quest is NOT completed. Stopping script.")
                ScriptManager.stop()
                return
            }

            // 8. Requirements check - skill level
            if (!MuseumUtils.isSkillLevelSufficient(lampSkill)) {
                log("STARTUP", "${lampSkill.displayName} level is < 10. Cannot use lamps. Stopping.")
                ScriptManager.stop()
                return
            }

            // 9. Apply DaxWalker blacklist
            DaxUtils.applyDefaultBlacklist()

            log("STARTUP", "Initialization complete. Starting startup inventory checks.")

        } catch (t: Throwable) {
            log("STARTUP", "Exception during onStart: ${t.message}")
            ScriptManager.stop()
        }
    }

    override fun onStop() {
        log("STOP", "Script stopped. Lamps used: $lampsUsed, XP gained: ${lampsUsed * 500}")
    }

    override fun poll() {
        if (checkStopAtLevel()) return

        when (stage) {
            Stage.STARTUP_USE_LAMPS -> handleStartupUseLamps()
            Stage.STARTUP_DROP_JUNK -> handleStartupDropJunk()
            Stage.STARTUP_BANK_EQUIPMENT -> handleStartupBankEquipment()
            Stage.STARTUP_BANK_ITEMS -> handleStartupBankItems()
            Stage.TRAVEL_TO_MUSEUM -> handleTravelToMuseum()
            Stage.ENSURE_TOOLS -> handleEnsureTools()
            Stage.NORMALISE_INVENTORY -> handleNormaliseInventory()
            Stage.MAIN_LOOP -> handleMainLoop()
        }
    }

    private fun checkStopAtLevel(): Boolean {
        if (stopAtLevel <= 0) return false

        if (MuseumUtils.hasReachedTargetLevel(lampSkill, stopAtLevel)) {
            val current = MuseumUtils.getCurrentLevel(lampSkill)
            log("STOP", "Reached target level $stopAtLevel (current: $current). Stopping.")
            currentTask = "Target level reached!"
            ScriptManager.stop()
            return true
        }
        return false
    }

    // Stage 1: Use any antique lamps we have at startup
    private fun handleStartupUseLamps() {
        currentTask = "Startup: Using lamps"
        log("STAGE", "STARTUP_USE_LAMPS")

        if (MuseumUtils.hasLamps()) {
            if (MuseumUtils.useAntiqueLamp(lampSkill)) {
                lampsUsed++
            }
            return // Stay in this stage until all lamps used
        }
        // No more lamps, advance to next stage
        stage = Stage.STARTUP_DROP_JUNK
    }

    // Stage 2: Drop junk items (unique artefacts, storage rewards)
    private fun handleStartupDropJunk() {
        currentTask = "Startup: Dropping junk"
        log("STAGE", "STARTUP_DROP_JUNK")

        if (MuseumUtils.hasJunkToDrop(keepItemNames)) {
            MuseumUtils.dropJunkItems(keepItemNames)
            return // Check again
        }

        // No more junk, advance
        stage = Stage.STARTUP_BANK_EQUIPMENT
    }

    // Stage 3: Check equipment, bank wrong items in hands/feet slots
    private fun handleStartupBankEquipment() {
        currentTask = "Startup: Checking equipment"
        log("STAGE", "STARTUP_BANK_EQUIPMENT")

        if (MuseumUtils.hasWrongEquipment()) {
            log("STAGE", "Found wrong equipment, banking")
            if (MuseumUtils.bankWrongEquipment()) {
                stage = Stage.STARTUP_BANK_ITEMS
            }
            return
        }

        // Equipment is fine, advance
        stage = Stage.STARTUP_BANK_ITEMS
    }

    // Stage 4: Bank any items that shouldn't be in inventory
    private fun handleStartupBankItems() {
        currentTask = "Startup: Checking inventory"
        log("STAGE", "STARTUP_BANK_ITEMS")

        if (MuseumUtils.hasItemsToBank(keepItemNames)) {
            log("STAGE", "Found items to bank")
            if (MuseumUtils.bankUnwantedItems(keepItemNames)) {
                stage = Stage.TRAVEL_TO_MUSEUM
            }
            return
        }

        // Inventory is clean enough, advance
        stage = Stage.TRAVEL_TO_MUSEUM
    }

    // Stage 5: Travel to the museum cleaning area
    private fun handleTravelToMuseum() {
        currentTask = "Traveling to museum"
        log("STAGE", "TRAVEL_TO_MUSEUM")

        if (MuseumUtils.isInsideCleaningArea()) {
            log("TRAVEL", "Inside cleaning area")
            stage = Stage.ENSURE_TOOLS
            return
        }

        MuseumUtils.travelToMuseum()
    }


    // Stage 6: Get tools and equip leather
    private fun handleEnsureTools() {
        currentTask = "Getting tools"
        log("STAGE", "ENSURE_TOOLS")

        // Check if we have all tools and proper equipment
        val hasTools = MuseumUtils.hasAllTools()
        val hasGloves = MuseumUtils.hasLeatherGlovesEquipped()
        val hasBoots = MuseumUtils.hasLeatherBootsEquipped()

        if (!hasTools || !hasGloves || !hasBoots) {
            log("KIT", "Getting tools (hasTools=$hasTools, gloves=$hasGloves, boots=$hasBoots)")
            MuseumUtils.takeTools() // This also equips leather and drops duplicates
            return
        }

        // All ready, check if we have a semi-clean inventory
        if (MuseumUtils.hasSemiCleanInventory(keepItemNames)) {
            log("STAGE", "Semi-clean inventory detected, normalizing")
            stage = Stage.NORMALISE_INVENTORY
        } else {
            log("STAGE", "Clean inventory, starting main loop")
            stage = Stage.MAIN_LOOP
        }
    }

    // Stage 7: Process semi-clean inventory (uncleaned finds / common artefacts)
    private fun handleNormaliseInventory() {
        currentTask = "Normalizing inventory"
        log("STAGE", "NORMALISE_INVENTORY")

        // Priority 1: Clean uncleaned finds
        if (MuseumUtils.hasUncleanedFinds()) {
            currentTask = "Cleaning existing finds"
            MuseumUtils.cleanSpecimen()
            return
        }

        // Priority 2: Store common artefacts
        if (MuseumUtils.hasCommonArtefacts()) {
            currentTask = "Storing existing artefacts"
            MuseumUtils.storeFinds()
            return
        }

        // Priority 3: Drop any junk from cleaning/storing
        if (MuseumUtils.hasJunkToDrop(keepItemNames)) {
            currentTask = "Dropping junk"
            MuseumUtils.dropJunkItems(keepItemNames)
            return
        }

        // All normalized, start main loop
        log("STAGE", "Inventory normalized, starting main loop")
        stage = Stage.MAIN_LOOP
    }

    // Main Loop
    private fun handleMainLoop() {

        // Priority 1: Use antique lamps
        if (MuseumUtils.hasLamps()) {
            currentTask = "Using lamp"
            if (MuseumUtils.useAntiqueLamp(lampSkill)) {
                lampsUsed++
            }
            return
        }

        // Priority 2: Drop junk items (do this before collecting/cleaning/storing)
        if (MuseumUtils.hasJunkToDrop(keepItemNames)) {
            currentTask = "Dropping junk"
            MuseumUtils.dropJunkItems(keepItemNames)
            return
        }

        // Priority 3: Clean uncleaned finds
        if (MuseumUtils.hasUncleanedFinds()) {
            currentTask = "Cleaning finds"
            MuseumUtils.cleanSpecimen()
            return
        }

        // Priority 4: Store common artefacts
        if (MuseumUtils.hasCommonArtefacts() && fullInv()) {
            currentTask = "Storing artefacts"
            MuseumUtils.storeFinds()
            return
        }

        // Priority 5: Collect uncleaned finds (only when inventory is ready)
        // This should only trigger when we have a clean inventory with just tools
        if (!fullInv()) {
            currentTask = if (oneTickClick) "1T Collecting finds" else "Collecting finds"
            if (oneTickClick) {
                MuseumUtils.collectUncleanedFinds1T()
            } else {
                MuseumUtils.collectUncleanedFinds()
            }
            return
        }

        // Default: Idle (should rarely reach here)
        currentTask = "Idle - waiting"
        Condition.sleep(Random.nextInt(600, 1200))
    }

    // Paint Setup
    private fun initPaint() {
        val paint = PaintUtil.create {
            trackSkill(lampSkill.trackedSkill ?: org.powbot.api.rt4.walking.model.Skill.Overall)
            currentTask { currentTask }
            stat("Lamps Used") { lampsUsed.toString() }
            stat("XP Gained") { (lampsUsed * 500).toString() }
            if (stopAtLevel > 0) {
                stat("Target Lvl") { "$stopAtLevel (current: ${MuseumUtils.getCurrentLevel(lampSkill)})" }
            }
        }
        addPaint(paint)
    }

    private fun log(tag: String, message: String) {
        Logger.info(SCRIPT_NAME, tag, message)
    }
}