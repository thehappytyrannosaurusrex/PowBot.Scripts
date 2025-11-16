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
import org.powbot.api.event.MessageEvent
import org.powbot.api.rt4.Chat
import com.google.common.eventbus.Subscribe
import org.thehappytyrannosaurusrex.arceuuslibrary.solver.ChatSource
import org.thehappytyrannosaurusrex.arceuuslibrary.solver.LibraryChatEvent
import org.thehappytyrannosaurusrex.arceuuslibrary.solver.LibraryChatParser

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
                Options.Values.DEBUG_COMPREHENSIVE_PATH,
                Options.Values.DEBUG_CHAT_PARSER
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

    // Nice, book-aware logging for parser events.
    private fun logChatParserEvent(event: LibraryChatEvent, source: ChatSource) {
        val sourceTag = when (source) {
            ChatSource.SERVER -> "SERVER"
            ChatSource.CHAT -> "CHAT"
        }

        when (event) {
            is LibraryChatEvent.ShelfBookFound -> {
                Logger.info(
                    "[Arceuus Library] DEBUG | $sourceTag: ShelfBookFound = ${event.book.name}"
                )
            }

            LibraryChatEvent.ShelfEmpty -> {
                Logger.info(
                    "[Arceuus Library] DEBUG | $sourceTag: ShelfEmpty"
                )
            }

            LibraryChatEvent.LayoutReset -> {
                Logger.info(
                    "[Arceuus Library] DEBUG | $sourceTag: LayoutReset"
                )
            }

            is LibraryChatEvent.CustomerRequested -> {
                val bookEnum = event.book?.name ?: "UNKNOWN"
                Logger.info(
                    "[Arceuus Library] DEBUG | $sourceTag: CustomerRequested = $bookEnum"
                )
            }

            is LibraryChatEvent.PlayerReplyToRequest -> {
                Logger.info(
                    "[Arceuus Library] DEBUG | $sourceTag: PlayerReply kind=${event.kind}"
                )
            }

            is LibraryChatEvent.NpcMetaDialogue -> {
                Logger.info(
                    "[Arceuus Library] DEBUG | $sourceTag: NpcMetaDialogue kind=${event.kind}"
                )
            }

            else -> {
                // Safety net in case we ever add more subclasses later.
                Logger.info(
                    "[Arceuus Library] DEBUG | $sourceTag: Unhandled event type=${event::class.simpleName}"
                )
            }
        }
    }


    // Used only in CHAT_PARSER_DEBUG mode to avoid spamming the same chat line.
    private var lastParsedChatMessage: String? = null

    // In CHAT_PARSER_DEBUG mode, this polls the Chat API and feeds messages into the parser.
    private fun chatParserDebugTick() {
        if (!Chat.chatting()) {
            lastParsedChatMessage = null
            return
        }

        val msg = Chat.getChatMessage()
        if (msg.isNullOrBlank() || msg == lastParsedChatMessage) {
            return
        }

        lastParsedChatMessage = msg

        val event = LibraryChatParser.parse(msg, ChatSource.CHAT) ?: return
        logChatParserEvent(event, ChatSource.CHAT)
    }

    // Server messages (e.g. empty shelf / layout reset / "You find: ...")
    @Subscribe
    fun onServerMessage(event: MessageEvent) {
        val msg = event.message ?: return
        val parsed = LibraryChatParser.parse(msg, ChatSource.SERVER) ?: return

        logChatParserEvent(parsed, ChatSource.SERVER)
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
        // In chat parser debug mode we don't run the behaviour tree at all.
        // This ensures there is no movement/clicking; you can manually play.
        if (config.debugMode == DebugMode.CHAT_PARSER_DEBUG) {
            chatParserDebugTick()
            return
        }

        if (stopIfReachedTargetLevel()) return
        // Future: cameraController.tick() if you want maintenance
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
