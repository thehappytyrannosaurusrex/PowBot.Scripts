package org.thehappytyrannosaurusrex.arceuuslibrary.data

import org.powbot.api.Tile

/**
 * Stair landing definitions for Arceuus Library.
 *
 * Each link encodes where the player ends up after clicking "Climb"
 * on a specific stair tile (up or down). All links are bidirectional:
 * if you can go A -> B, we also encode B -> A.
 */
data class StairLink(val from: Tile, val to: Tile, val cost: Int = 10)

object StairLandings {

    /**
     * All known stair landing links.
     *
     * These are taken directly from "Stair Landing Tiles.txt":
     * "Tiles correspond to where the player ends up when clicking climb on
     * stairs in either up or down direction".
     */
    val LINKS: List<StairLink> = listOf(
        // SW GROUND ↔ SW FIRST
        StairLink(Tile(1617, 3794, 0), Tile(1610, 3794, 1)),
        StairLink(Tile(1610, 3794, 1), Tile(1617, 3794, 0)),
        StairLink(Tile(1617, 3795, 0), Tile(1610, 3795, 1)),
        StairLink(Tile(1610, 3795, 1), Tile(1617, 3795, 0)),
        StairLink(Tile(1617, 3796, 0), Tile(1610, 3796, 1)),
        StairLink(Tile(1610, 3796, 1), Tile(1617, 3796, 0)),
        StairLink(Tile(1617, 3797, 0), Tile(1610, 3797, 1)),
        StairLink(Tile(1610, 3797, 1), Tile(1617, 3797, 0)),

        // SW FIRST ↔ SW SECOND
        StairLink(Tile(1621, 3791, 1), Tile(1621, 3798, 2)),
        StairLink(Tile(1621, 3798, 2), Tile(1621, 3791, 1)),
        StairLink(Tile(1622, 3791, 1), Tile(1622, 3798, 2)),
        StairLink(Tile(1622, 3798, 2), Tile(1622, 3791, 1)),

        // NW GROUND ↔ NW FIRST
        StairLink(Tile(1617, 3825, 0), Tile(1610, 3825, 1)),
        StairLink(Tile(1610, 3825, 1), Tile(1617, 3825, 0)),
        StairLink(Tile(1617, 3826, 0), Tile(1610, 3826, 1)),
        StairLink(Tile(1610, 3826, 1), Tile(1617, 3826, 0)),
        StairLink(Tile(1617, 3827, 0), Tile(1610, 3827, 1)),
        StairLink(Tile(1610, 3827, 1), Tile(1617, 3827, 0)),
        StairLink(Tile(1617, 3828, 0), Tile(1610, 3828, 1)),
        StairLink(Tile(1610, 3828, 1), Tile(1617, 3828, 0)),

        // NW FIRST ↔ NW SECOND
        StairLink(Tile(1615, 3819, 1), Tile(1608, 3819, 2)),
        StairLink(Tile(1608, 3819, 2), Tile(1615, 3819, 1)),
        StairLink(Tile(1615, 3818, 1), Tile(1608, 3818, 2)),
        StairLink(Tile(1608, 3818, 2), Tile(1615, 3818, 1)),

        // NE GROUND ↔ NE FIRST
        StairLink(Tile(1643, 3818, 0), Tile(1643, 3825, 1)),
        StairLink(Tile(1643, 3825, 1), Tile(1643, 3818, 0)),
        StairLink(Tile(1644, 3818, 0), Tile(1644, 3825, 1)),
        StairLink(Tile(1644, 3825, 1), Tile(1644, 3818, 0)),
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
        StairLink(Tile(1639, 3803, 2), Tile(1639, 3810, 1)),
    )

    /** All landing links starting from [tile]. */
    fun from(tile: Tile): List<StairLink> =
        LINKS.filter { it.from == tile }

    /** Just the tiles you can reach from [tile] by climbing stairs. */
    fun targetsFrom(tile: Tile): List<Tile> =
        LINKS.asSequence().filter { it.from == tile }.map { it.to }.toList()
}
