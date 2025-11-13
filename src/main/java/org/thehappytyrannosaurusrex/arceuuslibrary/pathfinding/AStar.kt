/*
 * Project: Arceuus Library Script (PowBot)
 * File: AStar.kt
 * Purpose: Added review header and standardized logging to Logger.info.
 * Notes: Generated comments + logging normalization on 2025-11-12.
 */

package org.thehappytyrannosaurusrex.arceuuslibrary.pathfinding

import org.thehappytyrannosaurusrex.arceuuslibrary.utils.Logger;
import java.util.PriorityQueue
import kotlin.math.abs

data class NodeId(val x: Int, val y: Int, val z: Int, val kind: Kind, val ref: Int = -1) {
    enum class Kind { SHELF_STAND, STAIR_LANDING, NPC, PIVOT, TEMP }
}

data class Edge(val from: NodeId, val to: NodeId, val cost: Int)

/**
 * Graph: Core component of the Arceuus Library script.
 * Auto-generated doc stub (reviewed 2025-11-12).
 */
class Graph {
    val nodes: MutableSet<NodeId> = mutableSetOf()
    val adj: MutableMap<NodeId, MutableList<Edge>> = mutableMapOf()

    fun addNode(n: NodeId) { if (nodes.add(n)) adj.putIfAbsent(n, mutableListOf()) }
    fun addUndirected(a: NodeId, b: NodeId, cost: Int) {
        addNode(a); addNode(b)
        adj.getValue(a).add(Edge(a, b, cost))
        adj.getValue(b).add(Edge(b, a, cost))
    }
    fun addDirected(a: NodeId, b: NodeId, cost: Int) {
        addNode(a); addNode(b)
        adj.getValue(a).add(Edge(a, b, cost))
    }
}

class AStar(private val stairPenalty: Int = 10) {
    init { Logger.info("A* ENTER AStar") }
    private fun h(a: NodeId, b: NodeId): Int {
        val dx = abs(a.x - b.x)
        val dy = abs(a.y - b.y)
        val dz = abs(a.z - b.z)
        return dx + dy + dz * stairPenalty
    }

    fun shortestPath(graph: Graph, start: NodeId, goal: NodeId): List<NodeId> {
        if (start == goal) return listOf(start)
        data class Q(val n: NodeId, val f: Int)
        val open = PriorityQueue<Q>(compareBy { it.f })
        val gScore = HashMap<NodeId, Int>()
        val came = HashMap<NodeId, NodeId?>()

        gScore[start] = 0
        came[start] = null
        Logger.info("A* PUSH to open"); open.add(Q(start, h(start, goal)))

        val closed = HashSet<NodeId>()
        while (open.isNotEmpty()) {
            val cur = run { Logger.info("A* POP from open"); open.poll().n }
            if (cur == goal) break
            if (!closed.add(cur)) continue

            val edges = graph.adj[cur] ?: continue
            for (e in edges) {
                val tentative = gScore.getOrDefault(cur, Int.MAX_VALUE) + e.cost
                if (tentative < gScore.getOrDefault(e.to, Int.MAX_VALUE)) {
                    gScore[e.to] = tentative
                    came[e.to] = cur
                    val f = tentative + h(e.to, goal)
                    Logger.info("A* PUSH to open"); open.add(Q(e.to, f))
                }
            }
        }
        if (!came.containsKey(goal)) return emptyList()
        // reconstruct
        val out = ArrayList<NodeId>()
        var n: NodeId? = goal
        while (n != null) { out.add(n); n = came[n] }
        out.reverse()
        return out
    }
}