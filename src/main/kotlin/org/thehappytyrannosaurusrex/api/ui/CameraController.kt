package org.thehappytyrannosaurusrex.api.ui

import org.powbot.api.Condition
import org.powbot.api.rt4.Camera
import org.thehappytyrannosaurusrex.api.utils.Logger
import kotlin.random.Random

class CameraController {

    companion object {
        private const val CAMERA_TARGET_YAW = 0           // exact north, degrees [0..359]
        private const val CAMERA_TARGET_ZOOM = 0.0        // percent [0..100]
        private const val CAMERA_TARGET_PITCH = 99        // percent [0..99]
        private const val CAMERA_MIN_PITCH = 85

        // run maintenance every 60s once we’ve locked in
        private const val CAMERA_MAINTENANCE_MS = 60_000L

        // during startup we’ll try to lock every ~2 ticks until stable
        private const val CAMERA_START_GRACE_MS = 6_000L
        private const val CAMERA_START_RETRY_MS = 1200L

        // how close is “good enough” – reserved for future maintenance
        @Suppress("unused")
        private const val YAW_TOL = 2           // degrees
        @Suppress("unused")
        private const val ZOOM_TOL = 5.0        // percent
        @Suppress("unused")
        private const val PITCH_TOL = 10        // percent
    }

    private var lastCameraMaintainAt: Long = 0L
    private var cameraStartupUntil: Long = 0L
    private var lastCameraStartupTryAt: Long = 0L
    private var cameraLockedOnce: Boolean = false
    private var cameraTargetPitch: Int = CAMERA_TARGET_PITCH
    private var nextCamMaintainAt: Long = 0L

    private fun yaw(): Int = Camera.yaw()

    private fun setYaw(target: Int) {
        // PowBot Camera.angle(Int) sets absolute yaw in degrees
        Camera.angle(target)
    }

    fun init() {
        // Pick a random pitch for this run between 85 and 99 (inclusive)
        cameraTargetPitch = Random.nextInt(CAMERA_MIN_PITCH, CAMERA_TARGET_PITCH + 1)

        Logger.info("[Arceuus Library] CAMERA | Initializing to yaw=$CAMERA_TARGET_YAW°, " +
                    "zoom=${CAMERA_TARGET_ZOOM.toInt()}%, pitch=$cameraTargetPitch."
        )

        // Snap once
        setYaw(CAMERA_TARGET_YAW)
        Camera.pitch(cameraTargetPitch)
        Camera.moveZoomSlider(CAMERA_TARGET_ZOOM)

        // Start a short “startup window” of aggressive retries (future use)
        val now = System.currentTimeMillis()
        cameraStartupUntil = now + CAMERA_START_GRACE_MS
        lastCameraStartupTryAt = 0L
        lastCameraMaintainAt = now // so we don’t immediately do the maintenance cadence
        cameraLockedOnce = false
        nextCamMaintainAt = now + CAMERA_MAINTENANCE_MS

        Condition.sleep(1000)
    }

    fun tick() {
        val now = System.currentTimeMillis()
        if (now < nextCamMaintainAt) return

        Logger.info("[Arceuus Library] CAMERA | Maintenance tick (placeholder).")
        nextCamMaintainAt = now + CAMERA_MAINTENANCE_MS
    }
}
