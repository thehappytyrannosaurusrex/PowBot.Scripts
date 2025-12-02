package org.thehappytyrannosaurusrex.arceuuslibrary

import org.thehappytyrannosaurusrex.api.ui.CameraController
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Bookshelf
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Facing

/**
 * Helpers for camera behaviour specific to the Arceuus Library.
 */
fun CameraController.faceShelfByFacing(
    shelf: Bookshelf,
    toleranceDeg: Int = 10
) {
    // PowBot yaw convention:
    // 0 = north
    // 90 = west
    // 180 = south
    // 270 = east
    val yaw = when (shelf.facing) {
        Facing.N -> 0
        Facing.E -> 270
        Facing.S -> 180
        Facing.W -> 90
    }

    faceYaw(yaw, toleranceDeg)
}
