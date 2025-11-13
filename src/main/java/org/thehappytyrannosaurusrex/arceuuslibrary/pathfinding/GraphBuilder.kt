/*
 * Project: Arceuus Library Script (PowBot)
 * File: GraphBuilder.kt
 * Purpose: Added review header and standardized logging to Logger.info.
 * Notes: Generated comments + logging normalization on 2025-11-12.
 */

package org.thehappytyrannosaurusrex.arceuuslibrary.pathfinding

import org.thehappytyrannosaurusrex.arceuuslibrary.utils.Logger;
import org.powbot.api.Tile
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Bookshelves
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Bookshelf
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Locations.Area

/**
 * Builds a navigation graph for the Arceuus Library.
 * - Nodes: shelf standing spots, stair landings (one-way), NPC tiles (you add), optional pivots.
 * - Edges: same-floor walkable connections; stair one-way edges across floors.
 *
 * The graph respects the connectivity rules described for GF/F1/F2 areas.
 */
/**
 * GraphBuilder: Core component of the Arceuus Library script.
 * Auto-generated doc stub (reviewed 2025-11-12).
 */
class GraphBuilder(
    private val stairs: List<Stair>,                // one-way stairs
    private val npcs: List<Npc> = emptyList(),      // optional NPC anchors
    private val connectRadius: Int = 8,             // connect same-floor nodes within this Manhattan distance
    private val stairPenalty: Int = 10
) {
    data class Npc(val name: String, val tile: Tile)
    data class Stair(val from: Tile, val to: Tile) // one-way

    fun build(): Graph {
        val g = Graph()

        // 1) Shelf standing nodes
        val shelfNodes = Bookshelves.ALL.map { shelf ->
            NodeId(shelf.standingTile.x, shelf.standingTile.y, shelf.standingTile.floor, NodeId.Kind.SHELF_STAND, shelf.shelfIndex)
                .also { g.addNode(it) }
        }

        // 2) Stairs (one-way)
        val stairNodes = mutableSetOf<NodeId>()
        for (s in stairs) {
            val a = NodeId(s.from.x, s.from.y, s.from.floor, NodeId.Kind.STAIR_LANDING)
            val b = NodeId(s.to.x, s.to.y, s.to.floor, NodeId.Kind.STAIR_LANDING)
            g.addDirected(a, b, stairPenalty)
            stairNodes += a; stairNodes += b
        }

        // 3) NPCs
        val npcNodes = npcs.map { n ->
            NodeId(n.tile.x, n.tile.y, n.tile.floor, NodeId.Kind.NPC).also { g.addNode(it) }
        }

        // 4) Same-floor connections in local neighbourhood
        // Library is small; O(n^2) is fine.
        val all = (shelfNodes + stairNodes + npcNodes).toList()
        val byZ = all.groupBy { it.z }
        for ((z, nodes) in byZ) {
            for (i in 0 until nodes.size) {
                val a = nodes[i]
                for (j in i + 1 until nodes.size) {
                    val b = nodes[j]
                    val md = manhattan(a, b)
                    if (md <= connectRadius && allowedSameFloorEdge(a, b)) {
                        g.addUndirected(a, b, md)
                    }
                }
            }
        }
        return g
    }

    private fun manhattan(a: NodeId, b: NodeId) = kotlin.math.abs(a.x - b.x) + kotlin.math.abs(a.y - b.y)

    /**
     * Apply area connectivity heuristics by floor:
     * - On ground floor (z=0): all areas connected (no gating).
     * - On first floor (z=1): areas are isolated except via stairs (so we allow only edges inside same area).
     * - On second floor (z=2): areas/corridors connect SW<->NW, NW<->NE, and to CENTRAL via corridors.
     *   We approximate by allowing edges as long as they are within connectRadius (local), since corridors physically connect them.
     *
     * This function can be tightened later by checking exact corridor polygons.
     */
    private fun allowedSameFloorEdge(a: NodeId, b: NodeId): Boolean {
        if (a.z != b.z) return false
        // find matching shelves (if any) to infer area; otherwise allow.
        val sa = Bookshelves.BY_INDEX[a.ref]
        val sb = Bookshelves.BY_INDEX[b.ref]
        val z = a.z
        return when (z) {
            0 -> true // ground floor all connected
            1 -> {
                // first floor: isolate by area
                if (sa != null && sb != null) sa.area == sb.area else true
            }
            2 -> true // second floor corridors interconnect; local edges are fine
            else -> true
        }
    }
}