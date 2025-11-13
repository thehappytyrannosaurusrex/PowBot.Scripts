/*
 * Project: Arceuus Library Script (PowBot)
 * File: StairConfig.kt
 * Purpose: Added review header and standardized logging to Logger.info.
 * Notes: Generated comments + logging normalization on 2025-11-12.
 */

package org.thehappytyrannosaurusrex.arceuuslibrary.data

import org.thehappytyrannosaurusrex.arceuuslibrary.utils.Logger;
import org.powbot.api.Tile
import org.thehappytyrannosaurusrex.arceuuslibrary.pathfinding.GraphBuilder  // <- IMPORTANT: pathfinding, not core

/**
 * StairConfig: Core component of the Arceuus Library script.
 * Auto-generated doc stub (reviewed 2025-11-12).
 */
object StairConfig {
    val STAIRS: List<GraphBuilder.Stair> = listOf(
        // SW GROUND ↔ SW FIRST
        GraphBuilder.Stair(Tile(1617, 3794, 0), Tile(1610, 3794, 1)),
        GraphBuilder.Stair(Tile(1610, 3794, 1), Tile(1617, 3794, 0)),

        GraphBuilder.Stair(Tile(1617, 3795, 0), Tile(1610, 3795, 1)),
        GraphBuilder.Stair(Tile(1610, 3795, 1), Tile(1617, 3795, 0)),

        GraphBuilder.Stair(Tile(1617, 3796, 0), Tile(1610, 3796, 1)),
        GraphBuilder.Stair(Tile(1610, 3796, 1), Tile(1617, 3796, 0)),

        GraphBuilder.Stair(Tile(1617, 3797, 0), Tile(1610, 3797, 1)),
        GraphBuilder.Stair(Tile(1610, 3797, 1), Tile(1617, 3797, 0)),

        // SW FIRST ↔ SW SECOND
        GraphBuilder.Stair(Tile(1621, 3791, 1), Tile(1621, 3798, 2)),
        GraphBuilder.Stair(Tile(1621, 3798, 2), Tile(1621, 3791, 1)),

        GraphBuilder.Stair(Tile(1622, 3791, 1), Tile(1622, 3798, 2)),
        GraphBuilder.Stair(Tile(1622, 3798, 2), Tile(1622, 3791, 1)),

        // NW GROUND ↔ NW FIRST
        GraphBuilder.Stair(Tile(1617, 3825, 0), Tile(1610, 3825, 1)),
        GraphBuilder.Stair(Tile(1610, 3825, 1), Tile(1617, 3825, 0)),

        GraphBuilder.Stair(Tile(1617, 3826, 0), Tile(1610, 3826, 1)),
        GraphBuilder.Stair(Tile(1610, 3826, 1), Tile(1617, 3826, 0)),

        GraphBuilder.Stair(Tile(1617, 3827, 0), Tile(1610, 3827, 1)),
        GraphBuilder.Stair(Tile(1610, 3827, 1), Tile(1617, 3827, 0)),

        GraphBuilder.Stair(Tile(1617, 3828, 0), Tile(1610, 3828, 1)),
        GraphBuilder.Stair(Tile(1610, 3828, 1), Tile(1617, 3828, 0)),

        // NW FIRST ↔ NW SECOND
        GraphBuilder.Stair(Tile(1615, 3819, 1), Tile(1608, 3819, 2)),
        GraphBuilder.Stair(Tile(1608, 3819, 2), Tile(1615, 3819, 1)),

        GraphBuilder.Stair(Tile(1615, 3818, 1), Tile(1608, 3818, 2)),
        GraphBuilder.Stair(Tile(1608, 3818, 2), Tile(1615, 3818, 1)),

        // NE GROUND ↔ NE FIRST
        GraphBuilder.Stair(Tile(1643, 3818, 0), Tile(1643, 3825, 1)),
        GraphBuilder.Stair(Tile(1643, 3825, 1), Tile(1643, 3818, 0)),

        GraphBuilder.Stair(Tile(1644, 3818, 0), Tile(1644, 3825, 1)),
        GraphBuilder.Stair(Tile(1644, 3825, 1), Tile(1644, 3818, 0)),

        GraphBuilder.Stair(Tile(1645, 3818, 0), Tile(1645, 3825, 1)),
        GraphBuilder.Stair(Tile(1645, 3825, 1), Tile(1645, 3818, 0)),

        GraphBuilder.Stair(Tile(1646, 3818, 0), Tile(1645, 3825, 1)),
        GraphBuilder.Stair(Tile(1645, 3825, 1), Tile(1646, 3818, 0)),

        // NE FIRST ↔ NE SECOND
        GraphBuilder.Stair(Tile(1643, 3828, 1), Tile(1650, 3828, 2)),
        GraphBuilder.Stair(Tile(1650, 3828, 2), Tile(1643, 3828, 1)),

        GraphBuilder.Stair(Tile(1643, 3829, 1), Tile(1650, 3829, 2)),
        GraphBuilder.Stair(Tile(1650, 3829, 2), Tile(1643, 3829, 1)),

        // CENTRAL FIRST ↔ CENTRAL SECOND
        GraphBuilder.Stair(Tile(1638, 3810, 1), Tile(1638, 3803, 2)),
        GraphBuilder.Stair(Tile(1638, 3803, 2), Tile(1638, 3810, 1)),

        GraphBuilder.Stair(Tile(1639, 3810, 1), Tile(1639, 3803, 2)),
        GraphBuilder.Stair(Tile(1639, 3803, 2), Tile(1639, 3810, 1))
    )
}