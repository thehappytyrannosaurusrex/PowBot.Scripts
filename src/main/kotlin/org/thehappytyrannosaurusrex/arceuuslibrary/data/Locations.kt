package org.thehappytyrannosaurusrex.arceuuslibrary.data

import org.powbot.api.Tile

/**
 * Centralised tile, area and anchor definitions for the Arceuus Library.
 */
object Locations {

    // --- High level anchors / important tiles ---

    /**
 * Rough centre of the Arceuus Library on the ground floor.
 */
    val LIBRARY_TILE: Tile = Tile(1632, 3804, 0)

    /**
 * Preferred Arceuus bank tile when banking from the library.
 */
    val ARCEUUS_BANK_TILE: Tile = Tile(1629, 3746, 0)

    // --- Logical areas inside the library ---

    enum class Area { NORTHWEST, NORTHEAST, SOUTHWEST, CENTRAL, CORRIDOR, OUTSIDE }

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

    /**
 * High-level metadata for each WorkArea:
 */
    data class WorkAreaInfo(
        val workArea: WorkArea,
        val anchor: Tile,
        val shelves: List<Bookshelf>
    )

    // -----------------------------
    // Floor 0 (Ground floor)
    // -----------------------------
    private val SW_GROUND = RectArea(1607, 3784, 1626, 3801, 0)
    private val NW_GROUND = RectArea(1607, 3814, 1626, 3831, 0)
    private val NE_GROUND = RectArea(1639, 3814, 1658, 3831, 0)
    // Central Building (Overlaps)
    private val CENTRAL_GROUND = RectArea(1622, 3797, 1643, 3818, 0)

    // -----------------------------
    // Floor 1 (First floor)
    // -----------------------------
    private val SW_FIRST = RectArea(1607, 3784, 1624, 3799, 1)
    private val NW_FIRST = RectArea(1607, 3816, 1624, 3831, 1)
    private val NE_FIRST = RectArea(1641, 3814, 1658, 3831, 1)
    // Custom central for 1st floor
    private val CENTRAL_FIRST = RectArea(1625, 3800, 1640, 3815, 1)

    // -----------------------------
    // Floor 2 (Second floor)
    // -----------------------------
    private val SW_SECOND = RectArea(1607, 3784, 1624, 3799, 2)
    private val NW_SECOND = RectArea(1607, 3816, 1624, 3831, 2)
    private val NE_SECOND = RectArea(1641, 3816, 1658, 3831, 2)
    // Custom central for 2nd floor
    private val CENTRAL_SECOND = RectArea(1625, 3800, 1640, 3815, 2)

    // ----------------------------
    // Connecting corridors (Second floor only)
    // ----------------------------

    // Corridor that connects the Northwest and Southwest areas of the second floor
    private val CORRIDOR_NWSW = RectArea(1613, 3800, 1616, 3815, 2)
    // Corridor that connects that corridor to the central room
    private val CORRIDOR_NWSW_CENTRAL = RectArea(1617, 3807, 1625, 3808, 2)

    // Corridor that connects the Northwest and Northeast areas of the second floor
    private val CORRIDOR_NWNE = RectArea(1625, 3823, 1640, 3826, 2)
    // Corridor that connects that corridor to the central room
    private val CORRIDOR_NWNE_CENTRAL = RectArea(1632, 3816, 1633, 3822, 2)

    // -----------------------------
    // Floor 0 (Ground floor) Anchor tiles
    // -----------------------------
    val SW_GROUND_ANCHOR = Tile(1619, 3796, 0)
    val NW_GROUND_ANCHOR = Tile(1620, 3818, 0)
    val NE_GROUND_ANCHOR = Tile(1645, 3817, 0)
    val CENTRAL_GROUND_ANCHOR = LIBRARY_TILE

    // -----------------------------
    // Floor 1 (First floor) Anchor tiles
    // -----------------------------
    val SW_FIRST_ANCHOR = Tile(1617, 3792, 1)
    val NW_FIRST_ANCHOR = Tile(1617, 3828, 1)
    val NE_FIRST_ANCHOR = Tile(1650, 3825, 1)
    val CENTRAL_FIRST_ANCHOR = Tile(1638, 3813, 1)

    // -----------------------------
    // Floor 2 (Second floor) Anchor tiles
    // -----------------------------
    val SW_SECOND_ANCHOR = Tile(1615, 3796, 2)
    val NW_SECOND_ANCHOR = Tile(1613, 3824, 2)
    val NE_SECOND_ANCHOR = Tile(1645, 3826, 2)
    val CENTRAL_SECOND_ANCHOR = Tile(1634, 3802, 2)

    // NPC Anchor tiles (ground floor)
    val NPC_SAM_ANCHOR = Tile(1639, 3801, 0)
    val NPC_PROFESSOR_ANCHOR = Tile(1625, 3801, 0)
    val NPC_VILLIA_ANCHOR = Tile(1626, 3814, 0)

    // -----------------------------
    // Area lookup by floor
    // -----------------------------
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
            Area.CORRIDOR  to CORRIDOR_NWSW,
            Area.CORRIDOR  to CORRIDOR_NWSW_CENTRAL,
            Area.CORRIDOR  to CORRIDOR_NWNE,
            Area.CORRIDOR  to CORRIDOR_NWNE_CENTRAL,
        )
    )

    // --- Convenience accessors / helpers ---

    val libraryTile: Tile
        get() = LIBRARY_TILE

    val libraryCenter: Tile
        get() = LIBRARY_TILE

    fun isInsideLibrary(tile: Tile): Boolean = area(tile) != Area.OUTSIDE

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
        Area.CORRIDOR  -> "Corridor"
    }

    fun floorName(floor: Int): String = when (floor) {
        0 -> "Ground floor"
        1 -> "First floor"
        2 -> "Second floor"
        else -> "Floor $floor"
    }

    fun describe(tile: Tile): String {
        val a = area(tile)
        return if (a == Area.OUTSIDE) "Outside library"
        else "${areaName(tile)} Library, ${floorName(tile.floor)}"
    }

    /**
 * Returns true if the two tiles are in different logical areas
 */
    fun isDifferentAreaOrFloor(from: Tile?, to: Tile): Boolean {
        if (from == null) return true
        return area(from) != area(to) || from.floor != to.floor
    }

    // Optional helper if need the central rectangle directly
    private fun centralArea(floor: Int): RectArea? = when (floor) {
        0 -> CENTRAL_GROUND
        1 -> CENTRAL_FIRST
        2 -> CENTRAL_SECOND
        else -> null
    }

    fun isAtLibrary(tile: Tile, radius: Int = 3): Boolean =
        tile.distanceTo(LIBRARY_TILE) <= radius

    fun isAtBank(tile: Tile, radius: Int = 8): Boolean =
        tile.distanceTo(ARCEUUS_BANK_TILE) <= radius


    /**
 * Mapping from high-level WorkArea enum to the underlying anchor tile and
 */
    val workAreas: Map<WorkArea, WorkAreaInfo> by lazy {
        mapOf(
            WorkArea.SW_GROUND to WorkAreaInfo(
                workArea = WorkArea.SW_GROUND,
                anchor = SW_GROUND_ANCHOR,
                shelves = Bookshelves.ALL.filter {
                    it.floor == 0 && it.area == Area.SOUTHWEST
                }
            ),
            WorkArea.CENTRAL_GROUND to WorkAreaInfo(
                workArea = WorkArea.CENTRAL_GROUND,
                anchor = LIBRARY_TILE,
                shelves = Bookshelves.ALL.filter {
                    it.floor == 0 && it.area == Area.CENTRAL
                }
            ),
            WorkArea.NW_GROUND to WorkAreaInfo(
                workArea = WorkArea.NW_GROUND,
                anchor = NW_GROUND_ANCHOR,
                shelves = Bookshelves.ALL.filter {
                    it.floor == 0 && it.area == Area.NORTHWEST
                }
            ),
            WorkArea.NE_GROUND to WorkAreaInfo(
                workArea = WorkArea.NE_GROUND,
                anchor = NE_GROUND_ANCHOR,
                shelves = Bookshelves.ALL.filter {
                    it.floor == 0 && it.area == Area.NORTHEAST
                }
            )
        )
    }

    fun workAreaInfo(area: WorkArea): WorkAreaInfo? =
        workAreas[area]

    fun shelvesForWorkArea(area: WorkArea): List<Bookshelf> =
        workAreas[area]?.shelves ?: emptyList()

    /**
 * Simple helper for future SolveAreaLeaf: given a WorkArea and a
 */
    fun orderedShelvesForArea(area: WorkArea, from: Tile): List<Bookshelf> =
        shelvesForWorkArea(area)
            .sortedBy { shelf -> shelf.standingTile.distanceTo(from) }

}
