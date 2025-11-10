package org.thehappytyrannosaurusrex.arceuuslibrary

import kotlin.math.abs
import org.powbot.api.rt4.Constants
import org.powbot.api.rt4.Skills
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.tree.TreeComponent
import org.powbot.api.script.tree.TreeScript
import org.powbot.dax.api.DaxWalker
import org.powbot.dax.teleports.Teleport
import org.powbot.api.rt4.Camera
import org.powbot.mobile.service.ScriptUploader
import org.thehappytyrannosaurusrex.arceuuslibrary.branches.InventorySanityBranch
import org.thehappytyrannosaurusrex.arceuuslibrary.utils.Logger

// --- Options model ---
object Options {
    object Keys {
        const val XP_TYPE = "XP Type"
        const val USE_GRACEFUL = "Use Graceful Set"
        const val USE_STAMINA = "Use Stamina Potions"
        const val ALLOW_TRAVEL_ITEMS = "Allow Travel Items"
        const val STOP_AT_LEVEL = "Stop At Target Level"
    }

    object Values {
        const val MAGIC = "Magic"
        const val RUNECRAFTING = "Runecrafting"
    }
}

/** High-level user choice for what XP the script should focus on. */
enum class XpType(val label: String, val skillId: Int) {
    MAGIC(Options.Values.MAGIC, Constants.SKILLS_MAGIC),
    RUNECRAFTING(Options.Values.RUNECRAFTING, Constants.SKILLS_RUNECRAFTING);

    companion object {
        fun fromLabel(label: String?): XpType =
            entries.firstOrNull { it.label.equals(label, ignoreCase = true) } ?: MAGIC
    }
}

/** Immutable configuration snapshot built from script options at startup. */
data class Config(
    val xpType: XpType,
    val useGraceful: Boolean,
    val useStamina: Boolean,
    val allowTravelItems: Boolean,
    /** 0 disables stop condition */
    val stopAtLevel: Int
) {
    val trackedSkillId: Int get() = xpType.skillId

    fun summary(): String =
        "XP=${xpType.label} (skillId=$trackedSkillId) | " +
                "Graceful=$useGraceful | " +
                "Stamina=$useStamina | " +
                "AllowTravelItems=$allowTravelItems | " +
                "StopAt=${if (stopAtLevel == 0) "disabled" else stopAtLevel}"
}

@ScriptManifest(
    name = "Arceuus Library",
    description = "Learns the Arceuus Library layout and fetches NPC-requested books for XP.",
    version = "0.2.0",
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
            description = "Allow teleport runes/tabs (e.g. House tab) to remain in inventory",
            optionType = OptionType.BOOLEAN,
            defaultValue = "true"
        ),
        ScriptConfiguration(
            name = Options.Keys.STOP_AT_LEVEL,
            description = "Stop automatically when reaching this level in the chosen XP type (0 = ignore).",
            optionType = OptionType.INTEGER,
            defaultValue = "0"
        )
    ]
)
class ArceuusLibrary : TreeScript() {

    // Live configuration built on start
    private lateinit var config: Config

    // --- Root of our behavior tree ---
    override val rootComponent: TreeComponent<*>
        get() = InventorySanityBranch(this)

    // --- Accessors used by branches/leaves later ---
    fun selectedXpType(): XpType = config.xpType
    fun shouldUseGraceful(): Boolean = config.useGraceful
    fun shouldUseStamina(): Boolean = config.useStamina
    fun shouldAllowTravelItems(): Boolean = config.allowTravelItems
    fun targetLevel(): Int = config.stopAtLevel
    fun trackedSkillId(): Int = config.trackedSkillId

    // Dax teleport blacklist
    object DaxConfig {
        val BLACKLISTED_TELEPORTS = arrayOf(
            Teleport.SOUL_WARS_MINIGAME,
            Teleport.LAST_MAN_STANDING_MINIGAME,
        )
    }

    // Camera settings & cadence
    companion object {
        private const val CAMERA_TARGET_YAW = 0           // exact north, degrees [0..359]
        private const val CAMERA_TARGET_ZOOM = 1.0        // percent [0..100]
        private const val CAMERA_TARGET_PITCH = 99        // percent [0..99]
        private const val CAMERA_MIN_PITCH = 85

        // run maintenance every 12s once we’ve locked in
        private const val CAMERA_MAINTENANCE_MS = 12_000L

        // during startup we’ll try to lock every 300ms until stable
        private const val CAMERA_START_GRACE_MS = 6_000L
        private const val CAMERA_START_RETRY_MS = 300L

        // how close is “good enough”
        private const val YAW_TOL = 2           // degrees
        private const val ZOOM_TOL = 1.0        // percent
        private const val PITCH_TOL = 1         // percent
    }
    private var lastCameraMaintainAt: Long = 0L
    private var cameraStartupUntil: Long = 0L
    private var lastCameraStartupTryAt: Long = 0L
    private var cameraLockedOnce: Boolean = false

    private fun yaw(): Int = Camera.yaw()
    private fun setYaw(target: Int) {
        // PowBot Camera.angle(Int) sets absolute yaw in degrees
        Camera.angle(target)
    }

    private var nextCamMaintainAt = 0L

    private fun initCamera() {
        Logger.info("[Camera] Initializing to yaw=$CAMERA_TARGET_YAW, zoom=${CAMERA_TARGET_ZOOM.toInt()}%, pitch=$CAMERA_TARGET_PITCH.")

        // Snap once
        setYaw(CAMERA_TARGET_YAW)
        Camera.pitch(CAMERA_TARGET_PITCH)
        Camera.moveZoomSlider(CAMERA_TARGET_ZOOM)

        // Start a short “startup window” of aggressive retries
        val now = System.currentTimeMillis()
        cameraStartupUntil = now + CAMERA_START_GRACE_MS
        lastCameraStartupTryAt = 0L
        lastCameraMaintainAt = now // so we don’t immediately do the 12s cadence
        cameraLockedOnce = false
    }

    private fun cameraWithinTolerance(): Boolean {
        val yOk = abs(yaw() - CAMERA_TARGET_YAW) <= YAW_TOL ||
                abs((yaw() - 360) - CAMERA_TARGET_YAW) <= YAW_TOL
        val zOk = abs(Camera.zoom - CAMERA_TARGET_ZOOM.toInt()) <= ZOOM_TOL
        val pOk = abs(Camera.pitch() - CAMERA_TARGET_PITCH) <= PITCH_TOL
        return yOk && zOk && pOk
    }

    private fun maintainCamera() {
        val now = System.currentTimeMillis()

        // Aggressive startup retries
        if (now < cameraStartupUntil) {
            if (now - lastCameraStartupTryAt >= CAMERA_START_RETRY_MS) {
                if (!cameraWithinTolerance()) {
                    setYaw(CAMERA_TARGET_YAW)
                    Camera.pitch(CAMERA_TARGET_PITCH)
                    Camera.moveZoomSlider(CAMERA_TARGET_ZOOM)
                    Logger.info("[Camera] Startup snap → yaw=${yaw()}, zoom=${org.powbot.api.rt4.Camera.zoom}%, pitch=${org.powbot.api.rt4.Camera.pitch()}%")
                } else {
                    cameraLockedOnce = true
                }
                lastCameraStartupTryAt = now
            }
            return
        }

        // Regular cadence (every ~12s)
        if (now - lastCameraMaintainAt < CAMERA_MAINTENANCE_MS) return
        lastCameraMaintainAt = now

        // If we drifted, gently correct. Keep corrections idempotent.
        if (!cameraWithinTolerance()) {
            Logger.info("[Camera] Maintenance correction.")
            if (Camera.pitch() < CAMERA_MIN_PITCH) {
                Camera.pitch(CAMERA_MIN_PITCH)
            }
            setYaw(CAMERA_TARGET_YAW)
            Camera.moveZoomSlider(CAMERA_TARGET_ZOOM)
            Camera.pitch(CAMERA_TARGET_PITCH)
        }
    }



    // --- Option parsing & validation ---
    private inline fun <reified T> optionOrDefault(key: String, default: T): T = try {
        getOption<T>(key)
    } catch (_: IllegalArgumentException) {
        Logger.error("[Config] Option '$key' not found; using default '$default'.")
        default
    } catch (e: Exception) {
        Logger.error("[Config] Failed to read option '$key': ${e.message}. Using default '$default'.")
        default
    }

    private fun reloadConfig(): Config {
        val xpTypeLabel = optionOrDefault(Options.Keys.XP_TYPE, Options.Values.MAGIC)
        val xpType = XpType.fromLabel(xpTypeLabel)
        val useGraceful = optionOrDefault(Options.Keys.USE_GRACEFUL, false)
        val useStamina = optionOrDefault(Options.Keys.USE_STAMINA, false)
        val allowTravel = optionOrDefault(Options.Keys.ALLOW_TRAVEL_ITEMS, true)
        val rawStop = optionOrDefault(Options.Keys.STOP_AT_LEVEL, 0)
        val stopAtLevel = if (rawStop == 0) 0 else rawStop.coerceIn(1, 99)

        return Config(
            xpType = xpType,
            useGraceful = useGraceful,
            useStamina = useStamina,
            allowTravelItems = allowTravel,
            stopAtLevel = stopAtLevel
        ).also {
            Logger.info("[Config] ${it.summary()}")
        }
    }

    // --- Script lifecycle ---
    override fun onStart() {
        super.onStart()
        Logger.info("[Startup] Arceuus Library script starting…")
        config = reloadConfig()
        Logger.info("[Startup] User options parsed and validated.")

        try {
            DaxWalker.blacklistTeleports(*DaxConfig.BLACKLISTED_TELEPORTS)
            Logger.info("[DaxWalker] Applying DaxWalker blacklists...")
        } catch (e: Exception) {
            Logger.error("[DaxWalker] Failed to apply teleport blacklist: ${e.message}")
        }

        initCamera()
        Logger.info("[Startup] Camera has been initialised.")
    }

    /** Stop the script if a target level is reached. */
    private fun stopIfReachedTargetLevel(): Boolean {
        val target = config.stopAtLevel
        if (target <= 0) return false
        val current = Skills.realLevel(config.trackedSkillId)
        if (current >= target) {
            Logger.info("[Shutdown] ${config.xpType.label.uppercase()} reached target level $target.")
            controller.stop()
            return true
        }
        return false
    }

    override fun poll() {
        maintainCamera()
        if (stopIfReachedTargetLevel()) return
        super.poll()
    }

    override fun onStop() {
        Logger.info("[Shutdown] Arceuus Library script has stopped.")
    }
}

/** Local test launcher. */
fun main() {
    ScriptUploader().uploadAndStart(
        "Arceuus Library",
        "main",
        "127.0.0.1:5555",
        true,
        false
    )
}
