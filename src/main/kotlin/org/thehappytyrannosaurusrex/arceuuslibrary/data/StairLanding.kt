package org.thehappytyrannosaurusrex.arceuuslibrary.data

import org.powbot.api.Tile

// Represents a traversable stair connection for A* pathfinding
data class StairLink(
    val from: Tile,
    val to: Tile,
    val cost: Int = 1
) {
    val changesFloor: Boolean get() = from.floor != to.floor
}

object StairLanding {

    // All stair connections in the library (bidirectional pairs)
    val LINKS: List<StairLink> = listOf(
        // SW GROUND ↔ SW FIRST
        StairLink(Tile(1616, 3808, 0), Tile(1616, 3815, 1)),
        StairLink(Tile(1616, 3815, 1), Tile(1616, 3808, 0)),
        StairLink(Tile(1617, 3808, 0), Tile(1616, 3815, 1)),
        StairLink(Tile(1616, 3815, 1), Tile(1617, 3808, 0)),

        // SW FIRST ↔ SW SECOND
        StairLink(Tile(1623, 3815, 1), Tile(1623, 3808, 2)),
        StairLink(Tile(1623, 3808, 2), Tile(1623, 3815, 1)),
        StairLink(Tile(1624, 3815, 1), Tile(1624, 3808, 2)),
        StairLink(Tile(1624, 3808, 2), Tile(1624, 3815, 1)),

        // NE GROUND ↔ NE FIRST
        StairLink(Tile(1645, 3818, 0), Tile(1645, 3825, 1)),
        StairLink(Tile(1645, 3825, 1), Tile(1645, 3818, 0)),
        StairLink(Tile(1646, 3818, 0), Tile(1645, 3825, 1)),
        StairLink(Tile(1645, 3825, 1), Tile(1646, 3818, 0)),

        // NE FIRST ↔ NE SECOND
        StairLink(Tile(1643, 3828, 1), Tile(1650, 3828, 2)),
        StairLink(Tile(1650, 3828, 2), Tile(1643, 3828, 1)),
        StairLink(Tile(1643, 3829, 1), Tile(1650, 3829, 2)),
        StairLink(Tile(1650, 3829, 2), Tile(1643, 3829, 1)),

        // CENTRAL FIRST ↔ CENTRAL SECOND
        StairLink(Tile(1638, 3810, 1), Tile(1638, 3803, 2)),
        StairLink(Tile(1638, 3803, 2), Tile(1638, 3810, 1)),
        StairLink(Tile(1639, 3810, 1), Tile(1639, 3803, 2)),
        StairLink(Tile(1639, 3803, 2), Tile(1639, 3810, 1))
    )

    // Get all stair links from a specific tile
    fun from(tile: Tile): List<StairLink> = LINKS.filter { it.from == tile }

    // Get all destination tiles reachable from a specific tile via stairs
    fun targetsFrom(tile: Tile): List<Tile> = LINKS.filter { it.from == tile }.map { it.to }

    // Check if a tile is a stair entry point
    fun isStairTile(tile: Tile): Boolean = LINKS.any { it.from == tile }

    // Get the link between two tiles if it exists
    fun linkBetween(from: Tile, to: Tile): StairLink? = LINKS.firstOrNull { it.from == from && it.to == to }

    // Get all stair tiles on a specific floor
    fun stairTilesOnFloor(floor: Int): List<Tile> = LINKS.filter { it.from.floor == floor }.map { it.from }.distinct()
}

// Alias for backwards compatibility with LibraryNav
typealias StairLandings = StairLanding