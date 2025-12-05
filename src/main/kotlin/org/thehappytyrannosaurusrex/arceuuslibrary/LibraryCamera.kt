package org.thehappytyrannosaurusrex.arceuuslibrary

import org.thehappytyrannosaurusrex.api.ui.CameraController
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Bookshelf

// Extension: face camera toward a bookshelf based on its facing direction
fun CameraController.faceShelfByFacing(shelf: Bookshelf, toleranceDeg: Int = 10) {
    faceYaw(shelf.facing.toYaw(), toleranceDeg)
}