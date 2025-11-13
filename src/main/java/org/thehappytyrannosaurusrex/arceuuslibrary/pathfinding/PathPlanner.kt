/*
 * Project: Arceuus Library Script (PowBot)
 * File: PathPlanner.kt
 * Purpose: Added review header and standardized logging to Logger.info.
 * Notes: Generated comments + logging normalization on 2025-11-12.
 */

package org.thehappytyrannosaurusrex.arceuuslibrary.pathfinding

import org.thehappytyrannosaurusrex.arceuuslibrary.utils.Logger;
import org.powbot.api.rt4.Npcs
import org.powbot.api.Tile
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Bookshelves
import org.thehappytyrannosaurusrex.arceuuslibrary.data.StairConfig
import kotlin.math.abs

/**
 * PathPlanner: Core component of the Arceuus Library script.
 * Auto-generated doc stub (reviewed 2025-11-12).
 */
object PathPlanner {
    private val aStar = AStar(stairPenalty = 12)

    fun buildGraph(npcs: List<GraphBuilder.Npc> = emptyList()): Graph {
        val gb = GraphBuilder(stairs = StairConfig.STAIRS, npcs = npcs, connectRadius = 10, stairPenalty = 12)
        return gb.build()
    }

    // removed "same floor only" filter — we want the truly nearest anchor (could be a landing on another z)
    fun nearestNode(graph: Graph, tile: Tile): NodeId {
        var best: NodeId? = null
        var bestMd = Int.MAX_VALUE
        for (n in graph.nodes) {
            val md = abs(n.x - tile.x) + abs(n.y - tile.y) + 10 * abs(n.z - tile.floor)
            if (md < bestMd) { bestMd = md; best = n }
        }
        return best ?: createTempNode(graph, tile)
    }

    private fun createTempNode(graph: Graph, tile: Tile, connectRadius: Int = 8): NodeId {
        val temp = NodeId(tile.x, tile.y, tile.floor, NodeId.Kind.TEMP)
        for (n in graph.nodes) {
            if (n.z != tile.floor) continue
            val md = abs(n.x - tile.x) + abs(n.y - tile.y)
            if (md in 1..connectRadius) graph.addUndirected(temp, n, md)
        }
        graph.addNode(temp)
        return temp
    }

    fun nodeForShelfIndex(graph: Graph, shelfIndex: Int): NodeId? {
        val s = Bookshelves.BY_INDEX[shelfIndex] ?: return null
        val z = s.standingTile.floor
        return graph.nodes.firstOrNull { it.kind == NodeId.Kind.SHELF_STAND && it.ref == shelfIndex && it.z == z }
    }

    fun shortestToShelf(graph: Graph, fromTile: Tile, shelfIndex: Int): List<NodeId> {
        val start = nearestNode(graph, fromTile)
        val goal = nodeForShelfIndex(graph, shelfIndex) ?: return emptyList()
        return aStar.shortestPath(graph, start, goal)
    }

    fun shortestToNpcNow(graph: Graph, fromTile: Tile, npcName: String): List<NodeId> {
        val start = nearestNode(graph, fromTile)
        val npc = Npcs.stream().name(npcName).nearest().first()
        if (!npc.valid()) return emptyList()
        val npcTile = npc.tile()
        val goal = createTempNode(graph, npcTile)
        return aStar.shortestPath(graph, start, goal)
    }


    // admissible: never > true cost (treat each floor delta as ~10)
    fun tileManhattan(a: Tile, b: Tile): Int =
        abs(a.x - b.x) + abs(a.y - b.y) + 10 * abs(a.floor - b.floor)
}
