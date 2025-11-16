/*
 * Project: Arceuus Library Script (PowBot)
 * File: Stairs.kt
 * Purpose: Added review header and standardized logging to Logger.info.
 * Notes: Generated comments + logging normalization on 2025-11-12.
 */

package org.thehappytyrannosaurusrex.arceuuslibrary.data

import org.thehappytyrannosaurusrex.arceuuslibrary.utils.Logger;
import org.powbot.api.Tile
import kotlin.math.max
import kotlin.math.min
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Locations.Area

// ===== Directional stair IDs (keep your existing sets) =====
private val UP_GROUND_TO_FIRST    = intArrayOf(27853, 27854)
private val DOWN_FIRST_TO_GROUND  = intArrayOf(27855, 27856)
private val UP_FIRST_TO_SECOND    = intArrayOf(27851)
private val DOWN_SECOND_TO_FIRST  = intArrayOf(27852)

// Accept any of these verbs; clients differ
val CLIMB_ACTIONS = arrayOf("Climb", "Climb-up", "Climb-down", "Climb Stairs")

// ---------- Geometry ----------
data class Rect(val x1: Int, val y1: Int, val x2: Int, val y2: Int, val z: Int) {
    val minX = min(x1, x2)
    val maxX = max(x1, x2)
    val minY = min(y1, y2)
    val maxY = max(y1, y2)
    fun contains(t: Tile): Boolean = t.floor == z && t.x in minX..maxX && t.y in minY..maxY
    fun center(): Tile = Tile((minX + maxX) / 2, (minY + maxY) / 2, z)
}

data class StairSpec(
    val label: String,
    val area: Area,
    val fromFloor: Int,
    val toFloor: Int,
    val rect: Rect,
    val stairIds: IntArray
)

// ---------- Your rectangles wired into specs (unchanged semantics) ----------
private val INTERNAL_SPECS: List<StairSpec> = listOf(
    // NW
    StairSpec("NW Ground→First", Area.NORTHWEST, 0, 1, Rect(1614, 3828, 1616, 3825, 0), UP_GROUND_TO_FIRST),
    StairSpec("NW First→Ground", Area.NORTHWEST, 1, 0, Rect(1611, 3828, 1614, 3825, 1), DOWN_FIRST_TO_GROUND),
    StairSpec("NW First→Second", Area.NORTHWEST, 1, 2, Rect(1612, 3819, 1614, 3818, 1), UP_FIRST_TO_SECOND),
    StairSpec("NW Second→First", Area.NORTHWEST, 2, 1, Rect(1609, 3819, 1612, 3818, 2), DOWN_SECOND_TO_FIRST),

    // SW
    StairSpec("SW Ground→First", Area.SOUTHWEST, 0, 1, Rect(1614, 3797, 1616, 3794, 0), UP_GROUND_TO_FIRST),
    StairSpec("SW First→Ground", Area.SOUTHWEST, 1, 0, Rect(1611, 3797, 1614, 3794, 1), DOWN_FIRST_TO_GROUND),
    StairSpec("SW First→Second", Area.SOUTHWEST, 1, 2, Rect(1621, 3794, 1622, 3792, 1), UP_FIRST_TO_SECOND),
    StairSpec("SW Second→First", Area.SOUTHWEST, 2, 1, Rect(1621, 3797, 1622, 3794, 2), DOWN_SECOND_TO_FIRST),

    // NE
    StairSpec("NE Ground→First", Area.NORTHEAST, 0, 1, Rect(1643, 3821, 1646, 3819, 0), UP_GROUND_TO_FIRST),
    StairSpec("NE First→Ground", Area.NORTHEAST, 1, 0, Rect(1643, 3824, 1646, 3821, 1), DOWN_FIRST_TO_GROUND),
    StairSpec("NE First→Second", Area.NORTHEAST, 1, 2, Rect(1644, 3829, 1646, 3828, 1), UP_FIRST_TO_SECOND),
    StairSpec("NE Second→First", Area.NORTHEAST, 2, 1, Rect(1646, 3829, 1649, 3828, 2), DOWN_SECOND_TO_FIRST),

    // CENTRAL (First ↔ Second only)
    StairSpec("Central First→Second", Area.CENTRAL, 1, 2, Rect(1638, 3809, 1639, 3807, 1), UP_FIRST_TO_SECOND),
    StairSpec("Central Second→First", Area.CENTRAL, 2, 1, Rect(1638, 3807, 1639, 3804, 2), DOWN_SECOND_TO_FIRST),
)

object Stairs {
    val SPECS: List<StairSpec> = INTERNAL_SPECS

    val ALL_IDS: IntArray by lazy {
        SPECS.flatMap { it.stairIds.asList() }.distinct().toIntArray()
    }

    fun match(fromTile: Tile, toFloor: Int): StairSpec? {
        val fromFloor = fromTile.floor
        // 1) Exact rect containment
        SPECS.firstOrNull { it.fromFloor == fromFloor && it.toFloor == toFloor && it.rect.contains(fromTile) }?.let { return it }
        // 2) Fallback: nearest rect among same from→to floors
        return SPECS
            .filter { it.fromFloor == fromFloor && it.toFloor == toFloor }
            .minByOrNull { manhattan(fromTile, it.rect.center()) }
    }

    private fun manhattan(a: Tile, b: Tile) = kotlin.math.abs(a.x - b.x) + kotlin.math.abs(a.y - b.y)
}