/*
 * Project: Arceuus Library Script (PowBot)
 * File: AStarBehaviourTest.kt
 * Purpose: Added review header and standardized logging to Logger.info.
 * Notes: Generated comments + logging normalization on 2025-11-12.
 */

package org.thehappytyrannosaurusrex.arceuuslibrary.test

import org.thehappytyrannosaurusrex.arceuuslibrary.utils.Logger;
import org.junit.Test
import kotlin.test.assertTrue
import org.powbot.api.Tile
import org.thehappytyrannosaurusrex.arceuuslibrary.pathfinding.*
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Bookshelves

/**
 * AStarBehaviorTest: Core component of the Arceuus Library script.
 * Auto-generated doc stub (reviewed 2025-11-12).
 */
class AStarBehaviorTest {

    @Test
    fun `path across floors uses stairs`() {
        val g = PathPlanner.buildGraph()
        // Pick any shelf on GF and any on F2
        val gf = Bookshelves.ALL.first { it.floor == 0 }
        val f2 = Bookshelves.ALL.first { it.floor == 2 }

        val path = PathPlanner.shortestToShelf(g, gf.standingTile, f2.shelfIndex)
        assertTrue(path.isNotEmpty(), "A* returned empty path")
        assertTrue(path.any { it.kind == NodeId.Kind.STAIR_LANDING }, "No stair node in cross-floor path")
    }

    @Test
    fun `first floor areas are isolated on same floor`() {
        val g = PathPlanner.buildGraph()
        val f1Shelves = Bookshelves.ALL.filter { it.floor == 1 }
        val ne = f1Shelves.first { it.area.name == "NORTHEAST" }
        val nw = f1Shelves.first { it.area.name == "NORTHWEST" }

        // If goal & start are both on F1 and in different areas, expect the path to include stairs
        val path = PathPlanner.shortestToShelf(g, ne.standingTile, nw.shelfIndex)
        assertTrue(path.any { it.kind == NodeId.Kind.STAIR_LANDING }, "F1 inter-area route must use stairs")
    }
}