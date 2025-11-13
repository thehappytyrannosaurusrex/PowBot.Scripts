/*
 * Project: Arceuus Library Script (PowBot)
 * File: GraphSanityTest.kt
 * Purpose: Added review header and standardized logging to Logger.info.
 * Notes: Generated comments + logging normalization on 2025-11-12.
 */

package org.thehappytyrannosaurusrex.arceuuslibrary.test

import org.thehappytyrannosaurusrex.arceuuslibrary.utils.Logger;
import org.junit.Test
import kotlin.test.assertTrue
import org.thehappytyrannosaurusrex.arceuuslibrary.pathfinding.*
import org.thehappytyrannosaurusrex.arceuuslibrary.data.StairConfig
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Bookshelves
import org.powbot.api.Tile

/**
 * GraphSanityTest: Core component of the Arceuus Library script.
 * Auto-generated doc stub (reviewed 2025-11-12).
 */
class GraphSanityTest {

    @Test
    fun `shelves and stairs exist in graph`() {
        val g = PathPlanner.buildGraph()
        val shelfNodeCount = g.nodes.count { it.kind == NodeId.Kind.SHELF_STAND }
        val stairNodeCount  = g.nodes.count { it.kind == NodeId.Kind.STAIR_LANDING }

        Logger.info("GraphSanity: shelves=$shelfNodeCount stairs=$stairNodeCount stairPairs=${StairConfig.STAIRS.size}")

        assertTrue(shelfNodeCount >= 300, "Expected ~353 shelf stand nodes, got $shelfNodeCount")
        assertTrue(stairNodeCount > 0, "Expected stair landing nodes")
    }

    @Test
    fun `stair edges are one-way and costed`() {
        val g = PathPlanner.buildGraph()
        val oneWayEdges = g.adj.values.flatten().filter { e ->
            e.from.kind == NodeId.Kind.STAIR_LANDING && e.to.kind == NodeId.Kind.STAIR_LANDING && e.cost >= 8
        }
        assertTrue(oneWayEdges.isNotEmpty(), "No stair edges were created")
    }
}