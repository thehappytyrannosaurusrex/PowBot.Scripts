package org.thehappytyrannosaurusrex.arceuuslibrary.data

import org.powbot.api.Area
import org.powbot.api.Tile

object Locations {

    // --- High level anchors ---
    val LIBRARY_TILE = Tile(1632, 3804, 0)
    val ARCEUUS_BANK_TILE = Tile(1629, 3746, 0)

    // --- Logical areas ---
    enum class Area { NORTHWEST, NORTHEAST, SOUTHWEST, CENTRAL, CORRIDOR, OUTSIDE }

    // --- Floor 0 (Ground floor) ---
    private val SW_GROUND = Area(Tile(1607, 3784, 0), Tile(1626, 3801, 0))
    private val NW_GROUND = Area(Tile(1607, 3814, 0), Tile(1626, 3831, 0))
    private val NE_GROUND = Area(Tile(1639, 3814, 0), Tile(1658, 3831, 0))
    private val CENTRAL_GROUND = Area(Tile(1622, 3797, 0), Tile(1643, 3818, 0))

    // --- Floor 1 (First floor) ---
    private val SW_FIRST = Area(Tile(1607, 3784, 1), Tile(1624, 3799, 1))
    private val NW_FIRST = Area(Tile(1607, 3816, 1), Tile(1624, 3831, 1))
    private val NE_FIRST = Area(Tile(1641, 3814, 1), Tile(1658, 3831, 1))
    private val CENTRAL_FIRST = Area(Tile(1625, 3800, 1), Tile(1640, 3815, 1))

    // --- Floor 2 (Second floor) ---
    private val SW_SECOND = Area(Tile(1607, 3784, 2), Tile(1624, 3799, 2))
    private val NW_SECOND = Area(Tile(1607, 3816, 2), Tile(1624, 3831, 2))
    private val NE_SECOND = Area(Tile(1641, 3816, 2), Tile(1658, 3831, 2))
    private val CENTRAL_SECOND = Area(Tile(1625, 3800, 2), Tile(1640, 3815, 2))

    // --- Corridors (Second floor only) ---
    private val CORRIDOR_NWSW = Area(Tile(1613, 3800, 2), Tile(1616, 3815, 2))
    private val CORRIDOR_NWSW_CENTRAL = Area(Tile(1617, 3807, 2), Tile(1625, 3808, 2))
    private val CORRIDOR_NWNE = Area(Tile(1625, 3823, 2), Tile(1640, 3826, 2))
    private val CORRIDOR_NWNE_CENTRAL = Area(Tile(1632, 3816, 2), Tile(1633, 3822, 2))

    // --- Anchor tiles by floor ---
    val SW_GROUND_ANCHOR = Tile(1619, 3796, 0)
    val NW_GROUND_ANCHOR = Tile(1620, 3818, 0)
    val NE_GROUND_ANCHOR = Tile(1645, 3817, 0)
    val CENTRAL_GROUND_ANCHOR = LIBRARY_TILE

    val SW_FIRST_ANCHOR = Tile(1617, 3792, 1)
    val NW_FIRST_ANCHOR = Tile(1617, 3828, 1)
    val NE_FIRST_ANCHOR = Tile(1650, 3825, 1)
    val CENTRAL_FIRST_ANCHOR = Tile(1638, 3813, 1)

    val SW_SECOND_ANCHOR = Tile(1615, 3796, 2)
    val NW_SECOND_ANCHOR = Tile(1613, 3824, 2)
    val NE_SECOND_ANCHOR = Tile(1645, 3826, 2)
    val CENTRAL_SECOND_ANCHOR = Tile(1634, 3802, 2)

    // --- NPC anchors (ground floor) ---
    val NPC_SAM_ANCHOR = Tile(1639, 3801, 0)
    val NPC_PROFESSOR_ANCHOR = Tile(1625, 3801, 0)
    val NPC_VILLIA_ANCHOR = Tile(1626, 3814, 0)

    // --- Area lookup by floor ---
    private data class FloorArea(val area: Area, val bounds: org.powbot.api.Area)

    private val areasByFloor: Map<Int, List<FloorArea>> = mapOf(
        0 to listOf(
            FloorArea(Area.CENTRAL, CENTRAL_GROUND),
            FloorArea(Area.SOUTHWEST, SW_GROUND),
            FloorArea(Area.NORTHWEST, NW_GROUND),
            FloorArea(Area.NORTHEAST, NE_GROUND)
        ),
        1 to listOf(
            FloorArea(Area.CENTRAL, CENTRAL_FIRST),
            FloorArea(Area.SOUTHWEST, SW_FIRST),
            FloorArea(Area.NORTHWEST, NW_FIRST),
            FloorArea(Area.NORTHEAST, NE_FIRST)
        ),
        2 to listOf(
            FloorArea(Area.CENTRAL, CENTRAL_SECOND),
            FloorArea(Area.SOUTHWEST, SW_SECOND),
            FloorArea(Area.NORTHWEST, NW_SECOND),
            FloorArea(Area.NORTHEAST, NE_SECOND),
            FloorArea(Area.CORRIDOR, CORRIDOR_NWSW),
            FloorArea(Area.CORRIDOR, CORRIDOR_NWSW_CENTRAL),
            FloorArea(Area.CORRIDOR, CORRIDOR_NWNE),
            FloorArea(Area.CORRIDOR, CORRIDOR_NWNE_CENTRAL)
        )
    )

    // --- Helpers ---

    val libraryTile: Tile get() = LIBRARY_TILE
    val libraryCenter: Tile get() = LIBRARY_TILE

    fun isInsideLibrary(tile: Tile): Boolean = area(tile) != Area.OUTSIDE

    fun area(tile: Tile): Area {
        val floorAreas = areasByFloor[tile.floor] ?: return Area.OUTSIDE
        return floorAreas.firstOrNull { it.bounds.contains(tile) }?.area ?: Area.OUTSIDE
    }

    fun areaName(tile: Tile): String = when (area(tile)) {
        Area.NORTHWEST -> "Northwest"
        Area.NORTHEAST -> "Northeast"
        Area.SOUTHWEST -> "Southwest"
        Area.CENTRAL -> "Central"
        Area.CORRIDOR -> "Corridor"
        Area.OUTSIDE -> "Outside"
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

    fun isDifferentAreaOrFloor(from: Tile?, to: Tile): Boolean {
        if (from == null) return true
        return area(from) != area(to) || from.floor != to.floor
    }

    fun isAtLibrary(tile: Tile, radius: Int = 3): Boolean =
        tile.distanceTo(LIBRARY_TILE) <= radius

    fun isAtBank(tile: Tile, radius: Int = 8): Boolean =
        tile.distanceTo(ARCEUUS_BANK_TILE) <= radius

    fun anchorFor(area: Area, floor: Int): Tile? = when {
        area == Area.SOUTHWEST && floor == 0 -> SW_GROUND_ANCHOR
        area == Area.NORTHWEST && floor == 0 -> NW_GROUND_ANCHOR
        area == Area.NORTHEAST && floor == 0 -> NE_GROUND_ANCHOR
        area == Area.CENTRAL && floor == 0 -> CENTRAL_GROUND_ANCHOR
        area == Area.SOUTHWEST && floor == 1 -> SW_FIRST_ANCHOR
        area == Area.NORTHWEST && floor == 1 -> NW_FIRST_ANCHOR
        area == Area.NORTHEAST && floor == 1 -> NE_FIRST_ANCHOR
        area == Area.CENTRAL && floor == 1 -> CENTRAL_FIRST_ANCHOR
        area == Area.SOUTHWEST && floor == 2 -> SW_SECOND_ANCHOR
        area == Area.NORTHWEST && floor == 2 -> NW_SECOND_ANCHOR
        area == Area.NORTHEAST && floor == 2 -> NE_SECOND_ANCHOR
        area == Area.CENTRAL && floor == 2 -> CENTRAL_SECOND_ANCHOR
        else -> null
    }

    fun shelvesInArea(area: Area, floor: Int): List<Bookshelf> =
        Bookshelves.ALL.filter { it.area == area && it.floor == floor }

    fun orderedShelvesForArea(area: Area, floor: Int, from: Tile): List<Bookshelf> =
        shelvesInArea(area, floor).sortedBy { it.standingTile.distanceTo(from) }

    // Get PowBot Area bounds for a logical area on a floor
    fun boundsFor(area: Area, floor: Int): org.powbot.api.Area? {
        val floorAreas = areasByFloor[floor] ?: return null
        return floorAreas.firstOrNull { it.area == area }?.bounds
    }
}