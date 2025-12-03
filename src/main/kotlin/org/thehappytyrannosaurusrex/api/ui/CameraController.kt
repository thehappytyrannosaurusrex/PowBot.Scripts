package org.thehappytyrannosaurusrex.api.ui

import org.powbot.api.Condition
import org.powbot.api.Locatable
import org.powbot.api.Tile
import org.powbot.api.rt4.Camera
import org.powbot.api.rt4.Players
import org.thehappytyrannosaurusrex.api.data.WidgetIds
import org.thehappytyrannosaurusrex.api.utils.Logger
import kotlin.math.abs
import kotlin.random.Random

data class CameraInitProfile(
    val yaw: Int = 0,
    val zoom: Double = 20.0,
    val minPitch: Int = 85,
    val maxPitch: Int = 99
)

// Simple, reusable camera controller
class CameraController {

    companion object {
        internal const val CAMERA_TARGET_YAW = 0
        internal const val CAMERA_TARGET_ZOOM = 20.0
        internal const val CAMERA_TARGET_PITCH = 99
        internal const val CAMERA_MIN_PITCH = 85
        private const val CAMERA_START_GRACE_MS = 6_000L
    }

    private var cameraStartupUntil: Long = 0L
    private var lastCameraStartupTryAt: Long = 0L
    private var cameraLockedOnce: Boolean = false
    private var cameraTargetPitch: Int = CAMERA_TARGET_PITCH

    private fun yaw(): Int = Camera.yaw()
    private fun setYaw(target: Int) = Camera.angle(target)

    // Click compass for cardinal direction shortcuts
    private fun clickCompass(actionLabel: String): Boolean {
        val compass = WidgetIds.ToplevelButtons.COMPASS.component()
        if (!compass.valid()) return false

        val actions = compass.actions()
        val hasExplicitAction = actions.any { it.equals(actionLabel, ignoreCase = true) }

        Logger.info("[Camera] Using compass action '$actionLabel'")

        if (hasExplicitAction) compass.click(actionLabel) else compass.click()
        return true
    }

    private fun tryCompassForYaw(targetYaw: Int): Boolean {
        val normalized = normalizeYaw(targetYaw)
        val action = when (normalized) {
            0 -> "Look North"
            90 -> "Look West"
            180 -> "Look South"
            270 -> "Look East"
            else -> return false
        }
        return clickCompass(action)
    }

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

    private fun randomizeInitialPitch(profile: CameraInitProfile) {
        cameraTargetPitch = Random.nextInt(profile.minPitch, profile.maxPitch + 1)
    }

    private fun applyBaselineCamera(profile: CameraInitProfile) {
        setYaw(profile.yaw)
        Camera.pitch(cameraTargetPitch)
        Camera.moveZoomSlider(profile.zoom)
    }

    private fun resetCameraState(now: Long) {
        cameraStartupUntil = now + CAMERA_START_GRACE_MS
        lastCameraStartupTryAt = 0L
        cameraLockedOnce = false
    }

    // Face a specific yaw, only if not already within tolerance
    fun faceYaw(targetYaw: Int, toleranceDeg: Int = 0) {
        val current = yaw()
        val diff = smallestAngleDifference(current, targetYaw)
        if (abs(diff) <= toleranceDeg) return
        if (tryCompassForYaw(targetYaw)) return
        setYaw(targetYaw)
    }

    fun faceNorth(toleranceDeg: Int = 0) = faceYaw(0, toleranceDeg)
    fun faceEast(toleranceDeg: Int = 0) = faceYaw(270, toleranceDeg)
    fun faceSouth(toleranceDeg: Int = 0) = faceYaw(180, toleranceDeg)
    fun faceWest(toleranceDeg: Int = 0) = faceYaw(90, toleranceDeg)

    // Face a tile in one of the 4 cardinal directions based on relative position
    fun faceTileCardinal(targetTile: Tile, toleranceDeg: Int = 5) {
        val me = Players.local()
        if (!me.valid()) return

        val myTile = me.tile()
        val dx = targetTile.x - myTile.x
        val dy = targetTile.y - myTile.y

        if (dx == 0 && dy == 0) return

        val targetYaw = when {
            abs(dx) > abs(dy) && dx > 0 -> 270  // east
            abs(dx) > abs(dy) && dx < 0 -> 90   // west
            dy < 0 -> 180                        // south
            else -> 0                            // north
        }
        faceYaw(targetYaw, toleranceDeg)
    }

    fun faceLocatableCardinal(target: Locatable, toleranceDeg: Int = 5) =
        faceTileCardinal(target.tile(), toleranceDeg)

    // Initialize camera with profile
    fun init(profile: CameraInitProfile) {
        val now = System.currentTimeMillis()
        randomizeInitialPitch(profile)
        Logger.info("[Camera] Init yaw=${profile.yaw}°, zoom=${profile.zoom.toInt()}%, pitch=$cameraTargetPitch")
        applyBaselineCamera(profile)
        resetCameraState(now)
        Condition.sleep(1000)
    }

    // Default initialization
    fun init() = init(CameraInitProfile(
        yaw = CAMERA_TARGET_YAW,
        zoom = CAMERA_TARGET_ZOOM,
        minPitch = CAMERA_MIN_PITCH,
        maxPitch = CAMERA_TARGET_PITCH
    ))
}