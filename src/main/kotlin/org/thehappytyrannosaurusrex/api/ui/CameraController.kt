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

class CameraController {

    companion object {
        private const val DEFAULT_YAW = 0
        private const val DEFAULT_ZOOM = 20.0
        private const val DEFAULT_PITCH = 99
        private const val MIN_PITCH = 85
    }

    private var targetPitch: Int = DEFAULT_PITCH

    // -------------------------------------------------------------------------
    // Initialization
    // -------------------------------------------------------------------------

    fun init(profile: CameraInitProfile = CameraInitProfile()) {
        targetPitch = Random.nextInt(profile.minPitch, profile.maxPitch + 1)

        Logger.info("[Camera] Init yaw=${profile.yaw}°, zoom=${profile.zoom.toInt()}%, pitch=$targetPitch")

        setYaw(profile.yaw)
        Camera.pitch(targetPitch)
        Camera.moveZoomSlider(profile.zoom)

        Condition.sleep(1000)
    }

    // -------------------------------------------------------------------------
    // Yaw Control
    // -------------------------------------------------------------------------

    fun faceYaw(targetYaw: Int, toleranceDeg: Int = 0) {
        val current = Camera.yaw()
        val diff = smallestAngleDifference(current, targetYaw)

        if (abs(diff) <= toleranceDeg) return

        // Try compass for cardinal directions first
        if (tryCompassForYaw(targetYaw)) return

        // Fallback: direct yaw set
        setYaw(targetYaw)
    }

    fun faceNorth(toleranceDeg: Int = 0) = faceYaw(0, toleranceDeg)
    fun faceEast(toleranceDeg: Int = 0) = faceYaw(270, toleranceDeg)
    fun faceSouth(toleranceDeg: Int = 0) = faceYaw(180, toleranceDeg)
    fun faceWest(toleranceDeg: Int = 0) = faceYaw(90, toleranceDeg)

    // -------------------------------------------------------------------------
    // Tile/Locatable Facing
    // -------------------------------------------------------------------------

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

    fun faceLocatableCardinal(target: Locatable, toleranceDeg: Int = 5) {
        faceTileCardinal(target.tile(), toleranceDeg)
    }

    // -------------------------------------------------------------------------
    // Internal Helpers
    // -------------------------------------------------------------------------

    private fun setYaw(target: Int) {
        Camera.angle(target)
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

    private fun clickCompass(actionLabel: String): Boolean {
        // Use the function that returns a Component directly
        val compass = WidgetIds.ToplevelButtons.compass()
        if (!compass.valid()) return false

        val actions = compass.actions()
        val hasExplicitAction = actions.any { action -> action.equals(actionLabel, ignoreCase = true) }

        Logger.info("[Camera] Using compass action '$actionLabel'")

        return if (hasExplicitAction) {
            compass.click(actionLabel)
        } else {
            compass.click()
        }
    }
}