package org.thehappytyrannosaurusrex.arceuuslibrary.pathing

import kotlin.random.Random
import org.powbot.api.Tile
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Bookshelves
import org.thehappytyrannosaurusrex.api.utils.Logger

object LibraryPathfinder {

    fun findPath(start: Tile, goal: Tile, maxNodes: Int = 20_000): List<Tile>? {
        val startNav = NavTile.from(start)
        val goalNav = NavTile.from(goal)

        // For safety: only path inside the library.
        if (!LibraryNav.inLibrary(startNav)) {
            Logger.warn("[Pathfinder] Start tile $start is outside library; aborting A*.")
            return null
        }
        if (!LibraryNav.inLibrary(goalNav)) {
            Logger.warn("[Pathfinder] Goal tile $goal is outside library; aborting A*.")
            return null
        }

        // If the goal is not walkable, don't even try.
        if (!LibraryNav.isWalkable(goalNav)) {
            Logger.warn("[Pathfinder] Goal tile $goal is not walkable; aborting A*.")
            return null
        }

        if (startNav == goalNav) {
            return listOf(start)
        }

        val navPath = findPath(startNav, goalNav, maxNodes) ?: return null
        return navPath.map { it.toTile() }
    }

    private fun findPath(start: NavTile, goal: NavTile, maxNodes: Int): List<NavTile>? {
        // Node for the priority queue
        data class Node(val nav: NavTile, val f: Int, val h: Int)

        // Open set: frontier of nodes to explore, ordered by f = g + h
        val open = java.util.PriorityQueue<Node>(compareBy<Node> { it.f }.thenBy { it.h })

        // Closed set: nodes 've fully explored
        val closed = mutableSetOf<NavTile>()

        // Best known cost from start to each node
        val gScore = mutableMapOf<NavTile, Int>().apply {
            this[start] = 0
        }

        // Parent pointers for path reconstruction
        val cameFrom = mutableMapOf<NavTile, NavTile>()

        val h0 = LibraryHeuristic.estimate(start, goal)
        open.add(Node(start, f = h0, h = h0))

        var expanded = 0

        while (open.isNotEmpty()) {
            if (expanded++ > maxNodes) {
                Logger.warn("[Pathfinder] Aborting A* after expanding $expanded nodes (limit $maxNodes).")
                return null
            }

            val currentNode = open.poll()
            val current = currentNode.nav

            if (current == goal) {
                return reconstructPath(cameFrom, current)
            }

            if (!closed.add(current)) {
                // Already processed with an equal or better gScore.
                continue
            }

            val currentG = gScore[current] ?: Int.MAX_VALUE

            for ((neighbour, stepCost) in LibraryNav.neighboursOf(current)) {
                if (neighbour in closed) continue

                val tentativeG = currentG + stepCost
                val oldG = gScore[neighbour] ?: Int.MAX_VALUE
                if (tentativeG >= oldG) {
                    continue
                }

                // Path to neighbour is better than any previous one.
                cameFrom[neighbour] = current
                gScore[neighbour] = tentativeG

                val h = LibraryHeuristic.estimate(neighbour, goal)
                val f = tentativeG + h
                open.add(Node(neighbour, f = f, h = h))
            }
        }

        // No path found
        Logger.info("[Arceuus Library] PATHING | No path found from $start to $goal.")
        return null
    }

    private fun reconstructPath(
        cameFrom: Map<NavTile, NavTile>,
        goal: NavTile
    ): List<NavTile> {
        val path = mutableListOf<NavTile>()
        var current: NavTile? = goal

        while (current != null) {
            path += current
            current = cameFrom[current]
        }

        path.reverse()
        return path
    }
}
