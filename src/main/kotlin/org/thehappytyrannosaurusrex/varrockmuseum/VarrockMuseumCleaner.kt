package org.thehappytyrannosaurusrex.varrockmuseum

import org.powbot.api.Condition
import org.powbot.api.script.AbstractScript
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.thehappytyrannosaurusrex.api.inventory.DropUtils
import org.thehappytyrannosaurusrex.api.ui.CameraController
import org.thehappytyrannosaurusrex.api.ui.CameraInitProfile
import org.thehappytyrannosaurusrex.api.ui.ViewportUi
import org.thehappytyrannosaurusrex.api.utils.Logger
import org.thehappytyrannosaurusrex.api.utils.PaintUtil
import org.thehappytyrannosaurusrex.varrockmuseum.config.Config
import org.thehappytyrannosaurusrex.varrockmuseum.config.LampSkill
import org.thehappytyrannosaurusrex.varrockmuseum.config.Options
import org.thehappytyrannosaurusrex.varrockmuseum.config.buildConfig
import org.thehappytyrannosaurusrex.varrockmuseum.utils.MuseumUtils

@ScriptManifest(
    name = "Varrock Museum Cleaner",
    description = "Cleans Varrock Museum specimens and uses Antique lamps on a chosen skill.",
    version = "3.0.0",
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
            description = "Comma-separated item names to NEVER drop (they can still be banked).",
            optionType = OptionType.STRING,
            defaultValue = "Coins,"
        )
    ]
)
class VarrockMuseumCleaner : AbstractScript() {

    companion object {
        private const val SCRIPT_NAME = "Varrock Museum"
    }

    // =========================================================================
    // Script State
    // =========================================================================

    enum class Stage {
        PRE_BANK_CLEAN,
        TRAVEL_TO_MUSEUM,
        ENSURE_KIT,
        INSIDE_NORMALISE,
        MAIN_LOOP
    }

    private var stage = Stage.PRE_BANK_CLEAN
    private lateinit var config: Config
    private var startTime: Long = 0L
    var lampsUsed: Int = 0
        private set

    // Helpers (lazy init)
    private val cameraController by lazy { CameraController() }
    private val viewportUi by lazy { ViewportUi() }

    // Config accessors
    private val lampSkill: LampSkill get() = config.lampSkill
    private val keepItemNames: Set<String> get() = config.keepItemNames

    // =========================================================================
    // Lifecycle
    // =========================================================================

    override fun onStart() {
        try {
            log("STARTUP", "Varrock Museum Cleaner starting...")
            startTime = System.currentTimeMillis()

            // Parse config
            config = buildConfig(this)
            log("STARTUP", "Using Antique lamps on: ${lampSkill.displayName}")
            if (keepItemNames.isNotEmpty()) {
                log("STARTUP", "Keep list: ${keepItemNames.joinToString()}")
            }

            // Setup camera and UI
            cameraController.init(CameraInitProfile(yaw = 190, zoom = 10.0, minPitch = 85, maxPitch = 95))
            viewportUi.tidyOnStart()

            // Setup input mode
            DropUtils.enableTapToDrop()
            DropUtils.ensureInventoryTab()

            // Setup paint overlay
            initPaint()

            // Validate requirements
            if (!MuseumUtils.isDigSiteCompleted()) {
                log("STARTUP", "The Dig Site is NOT completed. Stopping script.")
                controller.stop()
                return
            }

            if (!MuseumUtils.isSkillLevelSufficient(lampSkill)) {
                log("STARTUP", "${lampSkill.displayName} level is < 10. Cannot use lamps. Stopping.")
                controller.stop()
                return
            }

            log("STARTUP", "Initialization complete. Starting main loop.")

        } catch (t: Throwable) {
            log("STARTUP", "Exception during onStart: ${t.message}")
            controller.stop()
        }
    }

    override fun onStop() {
        log("STOP", "Script stopped. Lamps used: $lampsUsed")
    }

    // =========================================================================
    // Main Loop (poll)
    // =========================================================================

    override fun poll() {
        when (stage) {
            Stage.PRE_BANK_CLEAN -> handlePreBankClean()
            Stage.TRAVEL_TO_MUSEUM -> handleTravel()
            Stage.ENSURE_KIT -> handleEnsureKit()
            Stage.INSIDE_NORMALISE -> handleNormalise()
            Stage.MAIN_LOOP -> handleMainLoop()
        }
    }

    // =========================================================================
    // Stage Handlers
    // =========================================================================

    private fun handlePreBankClean() {
        log("STAGE", "PRE_BANK_CLEAN")

        if (!MuseumUtils.hasNonEssentialItems()) {
            log("STAGE", "Inventory clean, advancing to TRAVEL")
            stage = Stage.TRAVEL_TO_MUSEUM
            return
        }

        if (MuseumUtils.bankNonEssentialItems()) {
            stage = Stage.TRAVEL_TO_MUSEUM
        }
    }

    private fun handleTravel() {
        log("STAGE", "TRAVEL_TO_MUSEUM")

        if (MuseumUtils.isInsideCleaningArea()) {
            log("TRAVEL", "Already in cleaning area")
            stage = Stage.ENSURE_KIT
            return
        }

        if (MuseumUtils.travelToMuseum()) {
            stage = Stage.ENSURE_KIT
        }
    }

    private fun handleEnsureKit() {
        log("STAGE", "ENSURE_KIT")

        if (MuseumUtils.hasAllTools()) {
            log("KIT", "All tools present")
            stage = Stage.INSIDE_NORMALISE
            return
        }

        if (MuseumUtils.takeTools()) {
            stage = Stage.INSIDE_NORMALISE
        }
    }

    private fun handleNormalise() {
        log("STAGE", "INSIDE_NORMALISE")
        MuseumUtils.dropJunkItems(keepItemNames, "normalisation")
        stage = Stage.MAIN_LOOP
    }

    private fun handleMainLoop() {
        // Priority 1: Use antique lamps immediately
        if (MuseumUtils.hasAntiqueLamps()) {
            if (MuseumUtils.useAntiqueLamp(lampSkill)) {
                lampsUsed++
            }
            return
        }

        // Priority 2: Clean uncleaned finds
        if (MuseumUtils.hasUncleanedFinds()) {
            MuseumUtils.cleanSpecimen()
            return
        }

        // Priority 3: Store artefacts
        if (MuseumUtils.hasFindsToStore()) {
            MuseumUtils.storeFinds()
            return
        }

        // Priority 4: Drop junk
        if (MuseumUtils.hasJunkToDrop(keepItemNames)) {
            MuseumUtils.dropJunkItems(keepItemNames, "main loop")
            return
        }

        // Priority 5: Bank kept items if inventory full and nothing else to do
        if (MuseumUtils.shouldBankKeepItems(keepItemNames)) {
            MuseumUtils.bankKeepItems()
            return
        }

        // Default: Collect more uncleaned finds
        MuseumUtils.collectUncleanedFinds()
    }

    // Paint Setup
    private fun initPaint() {
        val paint = PaintUtil.create {
            trackSkill(lampSkill.trackedSkill ?: org.powbot.api.rt4.walking.model.Skill.Overall)
            stat("Stage") { stage.name }
            stat("Lamps Used") { lampsUsed.toString() }
        }
        addPaint(paint)
    }

    // Logging
    private fun log(tag: String, message: String) {
        Logger.info(SCRIPT_NAME, tag, message)
    }
}