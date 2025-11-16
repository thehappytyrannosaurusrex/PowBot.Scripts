package org.thehappytyrannosaurusrex.arceuuslibrary

import org.powbot.api.rt4.Skills
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.tree.TreeComponent
import org.powbot.api.script.tree.TreeScript
import org.powbot.dax.api.DaxWalker
import org.powbot.mobile.service.ScriptUploader
import org.thehappytyrannosaurusrex.arceuuslibrary.branches.RootBranch
import org.thehappytyrannosaurusrex.arceuuslibrary.camera.CameraController
import org.thehappytyrannosaurusrex.arceuuslibrary.config.Config
import org.thehappytyrannosaurusrex.arceuuslibrary.config.Options
import org.thehappytyrannosaurusrex.arceuuslibrary.config.XpType
import org.thehappytyrannosaurusrex.arceuuslibrary.config.DebugMode
import org.thehappytyrannosaurusrex.arceuuslibrary.config.buildConfig
import org.thehappytyrannosaurusrex.arceuuslibrary.debug.ComprehensivePathDebug
import org.thehappytyrannosaurusrex.arceuuslibrary.debug.PathStressTest
import org.thehappytyrannosaurusrex.arceuuslibrary.travel.DaxConfig
import org.thehappytyrannosaurusrex.arceuuslibrary.ui.ViewportUi
import org.thehappytyrannosaurusrex.arceuuslibrary.utils.Logger

@ScriptManifest(
    name = "Arceuus Library",
    description = "Learns the Arceuus Library layout and fetches NPC-requested books for XP.",
    version = "0.3.0",
    author = "thehappytyrannosaurusrex",
    category = ScriptCategory.Magic
)
@ScriptConfiguration.List(
    [
        ScriptConfiguration(
            name = Options.Keys.XP_TYPE,
            description = "Which skill to train using the Book of Arcane Knowledge.",
            optionType = OptionType.STRING,
            allowedValues = [Options.Values.MAGIC, Options.Values.RUNECRAFTING],
            defaultValue = Options.Values.MAGIC
        ),
        ScriptConfiguration(
            name = Options.Keys.USE_GRACEFUL,
            description = "Should we use full Graceful? (Will fetch)",
            optionType = OptionType.BOOLEAN,
            defaultValue = "false"
        ),
        ScriptConfiguration(
            name = Options.Keys.USE_STAMINA,
            description = "Use and restock stamina potions",
            optionType = OptionType.BOOLEAN,
            defaultValue = "false"
        ),
        ScriptConfiguration(
            name = Options.Keys.ALLOW_TRAVEL_ITEMS,
            description = "Allow teleport runes/tabs (& coins for charter) to remain in inventory",
            optionType = OptionType.BOOLEAN,
            defaultValue = "true"
        ),
        ScriptConfiguration(
            name = Options.Keys.STOP_AT_LEVEL,
            description = "Stop automatically when reaching this level in the chosen XP type (0 = ignore).",
            optionType = OptionType.INTEGER,
            defaultValue = "0"
        ),
        ScriptConfiguration(
            name = Options.Keys.DEBUG_MODE,
            description = "Debug behaviour when running in a dev environment. Disable on live accounts.",
            optionType = OptionType.STRING,
            allowedValues = [
                Options.Values.DEBUG_NONE,
                Options.Values.DEBUG_PATH_STRESS,
                Options.Values.DEBUG_COMPREHENSIVE_PATH
            ],
            defaultValue = Options.Values.DEBUG_NONE
        )
    ]
)
class ArceuusLibrary : TreeScript() {

    // Live configuration built on start
    private lateinit var config: Config

    private val cameraController = CameraController()
    private val viewportUi = ViewportUi()

    // --- Root of behavior tree --- //
    override val rootComponent: TreeComponent<*>
        get() = RootBranch(this)

    // --- Accessors used by branches/leaves later ---
    fun selectedXpType(): XpType = config.xpType
    fun shouldUseGraceful(): Boolean = config.useGraceful
    fun shouldUseStamina(): Boolean = config.useStamina
    fun shouldAllowTravelItems(): Boolean = config.allowTravelItems
    fun targetLevel(): Int = config.stopAtLevel
    fun trackedSkillId(): Int = config.trackedSkillId
    fun debugMode(): DebugMode = config.debugMode

    override fun onStart() {
        super.onStart()

        Logger.info("[Arceuus Library] MAIN | Arceuus Library script starting…")

        // Build config snapshot from script options
        config = buildConfig()
        Logger.info("[Arceuus Library] MAIN | User options parsed and validated.")

        applyDaxBlacklist()

        cameraController.init()
        Logger.info("[Arceuus Library] MAIN | Camera has been initialised.")

        viewportUi.tidyOnStart()

        runSelectedDebugMode()
    }

    private fun applyDaxBlacklist() {
        try {
            DaxWalker.blacklistTeleports(*DaxConfig.BLACKLISTED_TELEPORTS)
            Logger.info("[Arceuus Library] MAIN | Applying DaxWalker blacklists...")
        } catch (e: Exception) {
            Logger.error("[Arceuus Library] MAIN | Failed to apply teleport blacklist: ${e.message}")
        }
    }

    private fun runSelectedDebugMode() {
        when (config.debugMode) {
            DebugMode.NONE -> {
                // No debug behaviour; normal run.
                Logger.info("[Arceuus Library] MAIN | Debug mode disabled.")
            }
            DebugMode.PATH_STRESS_TEST -> {
                Logger.info("[Arceuus Library] MAIN | Running path stress test debug mode…")
                // Requires  start inside the library (any floor).
                PathStressTest.runLiveStress(hops = 30)
            }
            DebugMode.COMPREHENSIVE_PATH_DEBUG -> {
                Logger.info("[Arceuus Library] MAIN | Running comprehensive path debug mode…")
                ComprehensivePathDebug.runLive()
            }
        }
    }

    private fun stopIfReachedTargetLevel(): Boolean {
        val target = config.stopAtLevel
        if (target <= 0) return false

        val current = Skills.realLevel(config.trackedSkillId)
        if (current >= target) {
            Logger.info("[Arceuus Library] MAIN | ${config.xpType.label.uppercase()} reached target level $target.")
            controller.stop()
            return true
        }
        return false
    }

    override fun poll() {
        if (stopIfReachedTargetLevel()) return
        // Future: cameraController.tick() if maintenance needed
        super.poll()
    }

    override fun onStop() {
        Logger.info("[Arceuus Library] MAIN | Arceuus Library script has stopped.")
    }
}

fun main() {
    ScriptUploader().uploadAndStart(
        "Arceuus Library",
        "main",
        "127.0.0.1:5555",
        true,
        false
    )
}
