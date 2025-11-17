package org.thehappytyrannosaurusrex.arceuuslibrary

import org.powbot.api.rt4.Skills
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.tree.TreeComponent
import org.powbot.api.script.tree.TreeScript
import org.powbot.dax.api.DaxWalker
import org.thehappytyrannosaurusrex.arceuuslibrary.tree.RootBranch
import org.thehappytyrannosaurusrex.api.ui.CameraController
import org.thehappytyrannosaurusrex.arceuuslibrary.solver.LibraryDebugController
import org.thehappytyrannosaurusrex.arceuuslibrary.config.Config
import org.thehappytyrannosaurusrex.arceuuslibrary.config.Options
import org.thehappytyrannosaurusrex.arceuuslibrary.config.XpType
import org.thehappytyrannosaurusrex.arceuuslibrary.config.DebugMode
import org.thehappytyrannosaurusrex.arceuuslibrary.config.buildConfig
import org.thehappytyrannosaurusrex.arceuuslibrary.debug.ComprehensivePathDebug
import org.thehappytyrannosaurusrex.arceuuslibrary.debug.PathStressTest
import org.thehappytyrannosaurusrex.api.pathing.DaxConfig
import org.thehappytyrannosaurusrex.api.ui.ViewportUi
import org.thehappytyrannosaurusrex.api.utils.Logger
import org.powbot.api.event.MessageEvent
import com.google.common.eventbus.Subscribe

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
            description = "DEBUG MODE",
            optionType = OptionType.STRING,
            allowedValues = [
                Options.Values.DEBUG_NONE,
                Options.Values.DEBUG_COMPREHENSIVE_PATH,
                Options.Values.DEBUG_PATH_STRESS,
                Options.Values.DEBUG_CHAT_PARSER,
                Options.Values.DEBUG_MANUAL_SOLVER
            ],
            defaultValue = Options.Values.DEBUG_NONE
        )
    ]
)
class ArceuusLibrary : TreeScript() {

    // Live configuration built on start
    private lateinit var config: Config

    // Owns chat parsing + manual solver debug behavior.
    private lateinit var debugController: LibraryDebugController

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

        // NEW: set up debug controller now that config is ready
        debugController = LibraryDebugController { config.debugMode }

        applyDaxBlacklist()

        cameraController.init()
        Logger.info("[Arceuus Library] MAIN | Camera has been initialised.")

        runSelectedDebugMode()

        viewportUi.tidyOnStart()
    }

    private fun applyDaxBlacklist() {
        try {
            DaxWalker.blacklistTeleports(*DaxConfig.BLACKLISTED_TELEPORTS)
            Logger.info("[Arceuus Library] MAIN | Applying DaxWalker blacklists...")
        } catch (e: Exception) {
            Logger.error("[Arceuus Library] MAIN | Failed to apply teleport blacklist: ${e.message}")
        }
    }

    // Server messages (e.g. empty shelf / layout reset / "You find: ...")
    @Subscribe
    fun onServerMessage(event: MessageEvent) {
        val msg = event.message ?: return

        // Guard against very early events, just in case.
        if (!::debugController.isInitialized) {
            return
        }

        debugController.onServerMessage(msg)
    }


    private fun runSelectedDebugMode() {
        when (config.debugMode) {
            DebugMode.NONE -> {
                Logger.info("[Arceuus Library] MAIN | Debug mode disabled.")
            }
            DebugMode.PATH_STRESS_TEST -> {
                Logger.info("[Arceuus Library] DEBUG | Running path stress test debug mode…")
                // Requires you to start inside the library (any floor).
                PathStressTest.runLiveStress(hops = 30)
            }
            DebugMode.COMPREHENSIVE_PATH_DEBUG -> {
                Logger.info("[Arceuus Library] DEBUG | Running comprehensive path debug mode…")
                ComprehensivePathDebug.runLive()
            }
            DebugMode.CHAT_PARSER_DEBUG -> {
                Logger.info("[Arceuus Library] DEBUG | Chat parser debug mode active. Script will not move or click; only logs parsed chat/server events.")
            }
            DebugMode.MANUAL_SOLVER_DEBUG -> {
                Logger.info("[Arceuus Library] DEBUG | Manual solver debug mode active. No movement/clicking; solver updates from your actions.")
                debugController.resetManualSolver("entered manual solver debug")
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
        if (config.debugMode == DebugMode.CHAT_PARSER_DEBUG ||
            config.debugMode == DebugMode.MANUAL_SOLVER_DEBUG
        ) {
            // Only watch chat & server messages, no clicks or movement.
            debugController.tickChatDebug()
            return
        }

        if (stopIfReachedTargetLevel()) return
        super.poll()
    }

    override fun onStop() {
        Logger.info("[Arceuus Library] MAIN | Arceuus Library script has stopped.")
    }
}
