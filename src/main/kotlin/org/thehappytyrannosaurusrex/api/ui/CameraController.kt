package org.thehappytyrannosaurusrex.api.ui

import org.powbot.api.Condition
import org.powbot.api.Locatable
import org.powbot.api.Tile
import org.powbot.api.rt4.Camera
import org.powbot.api.rt4.Players
import org.powbot.api.rt4.Widgets
import org.thehappytyrannosaurusrex.api.data.WidgetIds
import org.thehappytyrannosaurusrex.api.utils.Logger
import kotlin.math.abs
import kotlin.random.Random

/**
 * Simple, reusable camera controller for scripts.
 */
data class CameraInitProfile(
    val yaw: Int = 0,
    val zoom: Double = 20.0,
    val minPitch: Int = 85,
    val maxPitch: Int = 99
)

class CameraController {

    companion object {
        internal const val CAMERA_TARGET_YAW = 0           // exact north, degrees [0..359]
        internal const val CAMERA_TARGET_ZOOM = 20.0       // percent [0..100]
        internal const val CAMERA_TARGET_PITCH = 99        // percent [0..99]
        internal const val CAMERA_MIN_PITCH = 85

        // Reserved for future “maintenance” logic
        private const val CAMERA_MAINTENANCE_MS = 60_000L
        private const val CAMERA_START_GRACE_MS = 6_000L
        private const val CAMERA_START_RETRY_MS = 1200L

        @Suppress("unused")
        private const val YAW_TOL = 2
        @Suppress("unused")
        private const val ZOOM_TOL = 5.0
        @Suppress("unused")
        private const val PITCH_TOL = 10
    }

    private var lastCameraMaintainAt: Long = 0L
    private var cameraStartupUntil: Long = 0L
    private var lastCameraStartupTryAt: Long = 0L
    private var cameraLockedOnce: Boolean = false
    private var cameraTargetPitch: Int = CAMERA_TARGET_PITCH
    private var nextCamMaintainAt: Long = 0L

    // --- Basic yaw helpers ---------------------------------------------------

    private fun yaw(): Int = Camera.yaw()

    private fun setYaw(target: Int) {
        // PowBot Camera.angle(Int) sets absolute yaw in degrees
        Camera.angle(target)
    }

    // --- Compass helpers (widget 601, component 1) ---------------------------

    private fun clickCompass(actionLabel: String): Boolean {
        val compass = WidgetIds.ToplevelButtons.COMPASS.component()
        if (!compass.valid()) {
            return false
        }

        val actions = compass.actions()
        val hasExplicitAction = actions.any { it.equals(actionLabel, ignoreCase = true) }

        Logger.info(
            "[Camera] Using compass action '$actionLabel' via " +
                    "widget(${WidgetIds.ToplevelButtons.COMPASS.group})." +
                    "component(${WidgetIds.ToplevelButtons.COMPASS.path})."
        )

        if (hasExplicitAction) {
            compass.click(actionLabel)
        } else {
            compass.click()
        }

        return true
    }


    /**
 * Try to satisfy a target yaw using the compass, if it matches an
 */
    private fun tryCompassForYaw(targetYaw: Int): Boolean {
        val normalized = normalizeYaw(targetYaw)
        val action = when (normalized) {
            0   -> "Look North"
            90  -> "Look West"
            180 -> "Look South"
            270 -> "Look East"
            else -> return false
        }

        return clickCompass(action)
    }

    // --- Angle helpers -------------------------------------------------------

    private fun normalizeYaw(yaw: Int): Int {
        var y = yaw % 360
        if (y < 0) y += 360
        return y
    }

    private fun smallestAngleDifference(from: Int, to: Int): Int {
        val fromNorm = normalizeYaw(from)
        val toNorm = normalizeYaw(to)
        var diff = toNorm - fromNorm
        if (diff > 180) diff -= 360
        if (diff < -180) diff += 360
        return diff
    }

    // --- Initial profile helpers --------------------------------------------

    /**
 * Pick a random pitch for run between [profile.minPitch] and [profile.maxPitch] (inclusive).
 */
    private fun randomizeInitialPitch(profile: CameraInitProfile) {
        cameraTargetPitch = Random.nextInt(profile.minPitch, profile.maxPitch + 1)
    }

    /**
 * Log the camera baseline ’re going to apply.
 */
    private fun logInitialCameraState(profile: CameraInitProfile) {
        Logger.info(
            "[Camera] Init yaw=${profile.yaw}°, " +
                    "zoom=${profile.zoom.toInt()}%, pitch=$cameraTargetPitch."
        )
    }

    /**
 * Snap camera once to baseline yaw / pitch / zoom.
 */
    private fun applyBaselineCamera(profile: CameraInitProfile) {
        setYaw(profile.yaw)
        Camera.pitch(cameraTargetPitch)
        Camera.moveZoomSlider(profile.zoom)
    }

    /**
 * Initialise timing / state for any future “keep camera in shape” logic.
 */
    private fun resetCameraState(now: Long) {
        cameraStartupUntil = now + CAMERA_START_GRACE_MS
        lastCameraStartupTryAt = 0L
        lastCameraMaintainAt = now
        cameraLockedOnce = false
        nextCamMaintainAt = now + CAMERA_MAINTENANCE_MS
    }

    // --- Public facing helpers ----------------------------------------------

    /**
 * Face a specific yaw, only if 're not already within [toleranceDeg].
 */
    fun faceYaw(targetYaw: Int, toleranceDeg: Int = 0) {
        val current = yaw()
        val diff = smallestAngleDifference(current, targetYaw)
        if (abs(diff) <= toleranceDeg) {
            // Already roughly facing the desired direction.
            return
        }

        // Try to use the compass for exact cardinals first.
        if (tryCompassForYaw(targetYaw)) {
            return
        }

        // Fallback: direct yaw set.
        setYaw(targetYaw)
    }

    // --- Cardinal convenience helpers ---------------------------------------

    /**
 * Face due north (yaw = 0°).
 */
    fun faceNorth(toleranceDeg: Int = 0) {
        faceYaw(0, toleranceDeg)
    }

    /**
 * Face due east (yaw = 270° in convention).
 */
    fun faceEast(toleranceDeg: Int = 0) {
        faceYaw(270, toleranceDeg)
    }

    /**
 * Face due south (yaw = 180°).
 */
    fun faceSouth(toleranceDeg: Int = 0) {
        faceYaw(180, toleranceDeg)
    }

    /**
 * Face due west (yaw = 90° in convention).
 */
    fun faceWest(toleranceDeg: Int = 0) {
        faceYaw(90, toleranceDeg)
    }

    /**
 * Face a [Tile] in one of the 4 cardinal directions based on where
 */
    fun faceTileCardinal(targetTile: Tile, toleranceDeg: Int = 5) {
        val me = Players.local()
        if (!me.valid()) return

        val myTile = me.tile()
        val dx = targetTile.x - myTile.x
        val dy = targetTile.y - myTile.y

        // Standing on the same tile – no useful facing to choose.
        if (dx == 0 && dy == 0) return

        val targetYaw = when {
            // Mostly east / west
            abs(dx) > abs(dy) && dx > 0 -> 270   // east
            abs(dx) > abs(dy) && dx < 0 -> 90    // west

            // Mostly south / north (y decreases as go south)
            dy < 0 -> 180                         // south
            else -> 0                             // north
        }

        faceYaw(targetYaw, toleranceDeg)
    }

    /**
 * Convenience wrapper for any [Locatable] (Npc, GameObject, etc.).
 */
    fun faceLocatableCardinal(target: Locatable, toleranceDeg: Int = 5) {
        val tile = target.tile()
        faceTileCardinal(tile, toleranceDeg)
    }

    // --- Initial camera lock -------------------------------------------------

    /**
 * Initialise the camera for script using the provided [profile].
 */
    fun init(profile: CameraInitProfile) {
        val now = System.currentTimeMillis()

        randomizeInitialPitch(profile)
        logInitialCameraState(profile)

        applyBaselineCamera(profile)
        resetCameraState(now)

        // Small settle delay
        Condition.sleep(1000)
    }

    /**
 * Backwards-compatible init using a default [CameraInitProfile] that
 */
    fun init() {
        init(
            CameraInitProfile(
                yaw = CAMERA_TARGET_YAW,
                zoom = CAMERA_TARGET_ZOOM,
                minPitch = CAMERA_MIN_PITCH,
                maxPitch = CAMERA_TARGET_PITCH
            )
        )
    }
}
