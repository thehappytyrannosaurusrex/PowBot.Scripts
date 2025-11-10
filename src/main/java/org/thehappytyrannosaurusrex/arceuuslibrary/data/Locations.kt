package org.thehappytyrannosaurusrex.arceuuslibrary.data

import org.powbot.api.Tile

/**
 * Classifies a Tile inside the Arceuus Library by **bounded rectangles** per floor.
 *
 * Areas: Northwest, Northeast, Southwest, Central (plus Outside)
 * Floors: Ground floor (0), First floor (1), Second floor (2)
 *
 * Any tile outside these bounds is considered OUTSIDE.
 */
object Locations {

    private val LIBRARY_TILE = Tile(1632, 3804, 0)

    enum class Area { NORTHWEST, NORTHEAST, SOUTHWEST, CENTRAL, OUTSIDE }

    /** Inclusive rectangle bound on a specific floor. */
    private data class RectArea(
        val x1: Int, val y1: Int,
        val x2: Int, val y2: Int,
        val floor: Int
    ) {
        private val minX = minOf(x1, x2)
        private val maxX = maxOf(x1, x2)
        private val minY = minOf(y1, y2)
        private val maxY = maxOf(y1, y2)

        fun contains(t: Tile): Boolean =
            t.floor == floor && t.x in minX..maxX && t.y in minY..maxY
    }

    // -----------------------------
    // Floor 0 (Ground floor)
    // -----------------------------
    private val SW_GROUND = RectArea(1606, 3783, 1627, 3802, 0)
    private val NW_GROUND = RectArea(1606, 3813, 1627, 3832, 0)
    private val NE_GROUND = RectArea(1638, 3813, 1659, 3832, 0)
    // Central Building (Overlaps)
    private val CENTRAL_GROUND = RectArea(1622, 3797, 1643, 3818, 0)

    // -----------------------------
    // Floor 1 (First floor)
    // -----------------------------
    private val SW_FIRST = RectArea(1606, 3783, 1627, 3802, 1)
    private val NW_FIRST = RectArea(1606, 3813, 1627, 3832, 1)
    private val NE_FIRST = RectArea(1638, 3813, 1659, 3832, 1)
    // Custom central for 1st floor
    private val CENTRAL_FIRST = RectArea(1624, 3799, 1641, 3816, 1)

    // -----------------------------
    // Floor 2 (Second floor)
    // -----------------------------
    private val SW_SECOND = RectArea(1606, 3783, 1627, 3802, 2)
    private val NW_SECOND = RectArea(1606, 3813, 1627, 3832, 2)
    private val NE_SECOND = RectArea(1638, 3813, 1659, 3832, 2)
    // Custom central for 2nd floor
    private val CENTRAL_SECOND = RectArea(1624, 3799, 1641, 3816, 2)

    //NPC General locations
    private val NPC_SAM_LOCATION = Tile(1639, 3801, 0)
    private val NPC_PROFESSOR_LOCATION = Tile(1625, 3801, 0)
    private val NPC_VILLIA_LOCATION = Tile(1626, 3814, 0)

    /**
     * Areas are checked **CENTRAL first** (to avoid ambiguity with overlapping edges),
     * then the three buildings.
     */
    private val areasByFloor: Map<Int, List<Pair<Area, RectArea>>> = mapOf(
        0 to listOf(
            Area.CENTRAL   to CENTRAL_GROUND,
            Area.SOUTHWEST to SW_GROUND,
            Area.NORTHWEST to NW_GROUND,
            Area.NORTHEAST to NE_GROUND,
        ),
        1 to listOf(
            Area.CENTRAL   to CENTRAL_FIRST,
            Area.SOUTHWEST to SW_FIRST,
            Area.NORTHWEST to NW_FIRST,
            Area.NORTHEAST to NE_FIRST,
        ),
        2 to listOf(
            Area.CENTRAL   to CENTRAL_SECOND,
            Area.SOUTHWEST to SW_SECOND,
            Area.NORTHWEST to NW_SECOND,
            Area.NORTHEAST to NE_SECOND,
        )
    )

    // --- Public library anchor tiles ---

    /**
     * Ground-floor "anchor" tile for the Arceuus Library.
     * Used for long-range travel and arrival distance checks.
     */
    val libraryTile: Tile
        get() = LIBRARY_TILE

    /**
     * Backwards-compat alias; treat the library "center" as the same anchor tile.
     */
    val libraryCenter: Tile
        get() = LIBRARY_TILE

    /** True if the tile is inside any of the defined rectangles for its floor. */
    fun isInsideLibrary(tile: Tile): Boolean = area(tile) != Area.OUTSIDE

    /**
     * Resolve the bounded area for a tile; returns OUTSIDE if not inside the library.
     * (Central is checked first on each floor.)
     */
    fun area(tile: Tile): Area {
        val floorAreas = areasByFloor[tile.floor] ?: return Area.OUTSIDE
        return floorAreas.firstOrNull { it.second.contains(tile) }?.first ?: Area.OUTSIDE
    }

    fun areaName(tile: Tile): String = when (area(tile)) {
        Area.NORTHWEST -> "Northwest"
        Area.NORTHEAST -> "Northeast"
        Area.SOUTHWEST -> "Southwest"
        Area.CENTRAL   -> "Central"
        Area.OUTSIDE   -> "Outside"
    }

    fun floorName(floor: Int): String = when (floor) {
        0 -> "Ground floor"
        1 -> "First floor"
        2 -> "Second floor"
        else -> "Floor $floor"
    }

    /** e.g., "Northwest Library, First floor" or "Outside library" */
    fun describe(tile: Tile): String {
        val a = area(tile)
        return if (a == Area.OUTSIDE) "Outside library"
        else "${areaName(tile)} Library, ${floorName(tile.floor)}"
    }

    /** Useful for gating logs/paint updates so we only announce when crossing areas/floors. */
    fun isDifferentAreaOrFloor(from: Tile?, to: Tile): Boolean {
        if (from == null) return true
        return area(from) != area(to) || from.floor != to.floor
    }

    // Optional helper if you need the central rectangle directly
    private fun centralArea(floor: Int): RectArea? = when (floor) {
        0 -> CENTRAL_GROUND
        1 -> CENTRAL_FIRST
        2 -> CENTRAL_SECOND
        else -> null
    }

    fun isAtLibrary(tile: Tile, radius: Int = 3): Boolean =
        tile.distanceTo(LIBRARY_TILE) <= radius
}
