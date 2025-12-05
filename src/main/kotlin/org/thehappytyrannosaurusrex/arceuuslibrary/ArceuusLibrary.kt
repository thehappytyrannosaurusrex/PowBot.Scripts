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
import org.thehappytyrannosaurusrex.api.utils.ScriptUtils
import org.thehappytyrannosaurusrex.arceuuslibrary.state.LibraryState
import org.thehappytyrannosaurusrex.arceuuslibrary.state.LibraryNpcs
import org.thehappytyrannosaurusrex.arceuuslibrary.solver.LibraryChatEvent
import org.thehappytyrannosaurusrex.arceuuslibrary.solver.LibraryDebugController


@ScriptManifest(
    name = "Arceuus Library",
    description = "Learns the Arceuus Library layout and fetches NPC-requested books for XP.",
    version = "0.0.9",
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
                Options.Values.DEBUG_MANUAL_SOLVER,
                Options.Values.DEBUG_CHAT_AND_MANUAL_SOLVER
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
    private val libraryState = LibraryState()

    private fun handleLibraryChatEvent(event: LibraryChatEvent) {
        when (event) {
            is LibraryChatEvent.CustomerRequested -> {
                // Don't know the NPC name yet, so pass null for now.
                // Once can identify which NPC 're talking to, 'll pass that here.
                state().onCustomerRequested(
                    npcName = null,
                    rawTitle = event.rawTitle,
                    book = event.book
                )

                val bookName = event.book?.name ?: "UNKNOWN"
                Logger.info(
                    "[Arceuus Library] STATE | Active request set from chat: " +
                            "npc=<unknown>, book=$bookName, raw='${event.rawTitle}'"
                )
            }

            LibraryChatEvent.LayoutReset -> {
                state().onLayoutReset()
                Logger.info("[Arceuus Library] STATE | Layout reset detected; LibraryState cleared.")
            }

            // 'll use these later for ping-pong / cooldown logic.
            is LibraryChatEvent.NpcMetaDialogue -> {
                // Example future usage:
                // If (event.kind == NpcMetaKind.RECENTLY_HELPED_ALREADY && lastNpcName != null) {
                // State().markRecentlyHelped(lastNpcName)
                // }
            }

            else -> {
                // Other events (ShelfBookFound, ShelfEmpty, etc.) are handled by the solver.
            }
        }
    }

    // --- Root of behavior tree --- //
    override val rootComponent: TreeComponent<*>
        get() = RootBranch(this)

    // --- Accessors used by branches/leaves later ---
    fun selectedXpType(): XpType = config.xpType
    fun shouldUseGraceful(): Boolean = config.useGraceful
    fun shouldUseStamina(): Boolean = config.useStamina
    fun shouldAllowTravelItems(): Boolean = config.allowTravelItems
    fun targetLevel(): Int = config.stopAtLevel
    fun state(): LibraryState = libraryState

    // NEW: let leaves access the camera helper
    fun camera(): CameraController = cameraController

    fun trackedSkillId(): Int = config.trackedSkillId
    fun debugMode(): DebugMode = config.debugMode

    override fun onStart() {
        super.onStart()

        Logger.info("[Arceuus Library] MAIN | Arceuus Library script starting…")

        // Build config snapshot from script options
        config = buildConfig()
        Logger.info("[Arceuus Library] MAIN | User options parsed and validated.")

        // NEW: set up debug controller now that config is ready
        debugController = LibraryDebugController(
            debugModeProvider = { config.debugMode },
            onEventFromChat = { event -> handleLibraryChatEvent(event) }
        )

        // Reset manual solver + high level library state on script start
        debugController.resetManualSolver("script start")
        libraryState.resetAll()
        Logger.info("[Arceuus Library] MAIN | LibraryState after reset: ${state()}")


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

    // Server messages (e.g. empty shelf / layout reset / " find: ...")
    @Subscribe
    fun onServerMessage(event: MessageEvent) {
        val msg = event.message ?: return

        // Guard against very early events, just in case.
        if (!::debugController.isInitialized) {
            return
        }

        debugController.onServerMessage(msg)
    }


    fun runSelectedDebugMode() {
        when (config.debugMode) {
            DebugMode.NONE -> {
                Logger.info("[Arceuus Library] MAIN | Debug mode disabled.")
            }
            DebugMode.COMPREHENSIVE_PATH_DEBUG -> {
                Logger.info("[Arceuus Library] DEBUG | Running comprehensive path debug mode…")
                ComprehensivePathDebug.runLive()
            }
            DebugMode.PATH_STRESS_TEST -> {
                Logger.info("[Arceuus Library] DEBUG | Running path stress test debug mode…")
                // Requires to start inside the library (any floor).
                PathStressTest.runLiveStress(hops = 30)
            }
            DebugMode.CHAT_PARSER_DEBUG -> {
                Logger.info(
                    "[Arceuus Library] DEBUG | Chat parser debug active. " +
                            "Script will not move or click; only logs parsed chat/server events."
                )
            }
            DebugMode.MANUAL_SOLVER_DEBUG -> {
                Logger.info(
                    "[Arceuus Library] DEBUG | Manual solver debug active. " +
                            "No movement/clicking; solver updates from your actions."
                )
                debugController.resetManualSolver("entered manual solver debug")
            }
            DebugMode.CHAT_AND_MANUAL_SOLVER_DEBUG -> {
                Logger.info(
                    "[Arceuus Library] DEBUG | Chat parser + manual solver debug active. " +
                            "Script will not move or click; logs parsed chat/server events " +
                            "and updates the solver based on your manual clicks."
                )
                debugController.resetManualSolver("entered chat+manual solver debug")
            }
        }
    }


    private fun stopIfReachedTargetLevel(): Boolean {
        val target = config.stopAtLevel
        if (target <= 0) return false

        val current = Skills.realLevel(config.trackedSkillId)
        if (current >= target) {
            ScriptUtils.stopWithInfo(this,
                "[Arceuus Library] MAIN | ${config.xpType.label.uppercase()} reached target level $target."
            )
            return true
        }
        return false
    }

    override fun poll() {
        // Always let the debug controller sniff chat so that parser logs and
        // LibraryState updates work in all modes, not just explicit debug modes.
        if (::debugController.isInitialized) {
            debugController.tickChatDebug()
        }

        if (config.debugMode == DebugMode.CHAT_PARSER_DEBUG ||
            config.debugMode == DebugMode.MANUAL_SOLVER_DEBUG ||
            config.debugMode == DebugMode.CHAT_AND_MANUAL_SOLVER_DEBUG
        ) {
            // In these modes *only* watch chat & server messages; no clicks or movement.
            return
        }

        if (stopIfReachedTargetLevel()) return
        super.poll()
    }


    override fun onStop() {
        PathStressTest.cancel()
        Logger.info("[Arceuus Library] MAIN | Arceuus Library script has stopped.")
        super.onStop()

    }

}