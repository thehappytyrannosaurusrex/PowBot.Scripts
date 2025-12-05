package org.thehappytyrannosaurusrex.arceuuslibrary.pathing

import org.powbot.api.Tile
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Locations
import org.thehappytyrannosaurusrex.arceuuslibrary.data.StairLanding
import org.thehappytyrannosaurusrex.api.utils.Logger
import java.util.PriorityQueue
import kotlin.math.abs

object LibraryPathfinder {

    private const val TAG = "Pathfinder"
    private const val MAX_ITERATIONS = 5000

    // A* node for priority queue
    private data class Node(
        val tile: Tile,
        val gCost: Int,      // Cost from start
        val hCost: Int,      // Heuristic to goal
        val parent: Node?
    ) : Comparable<Node> {
        val fCost: Int get() = gCost + hCost
        override fun compareTo(other: Node): Int = fCost.compareTo(other.fCost)
    }

    // Find path using A* algorithm
    fun findPath(start: Tile, goal: Tile): List<Tile>? {
        if (start == goal) return listOf(start)

        // Different floors require stair traversal
        if (start.floor != goal.floor) {
            return findPathAcrossFloors(start, goal)
        }

        return findPathSameFloor(start, goal)
    }

    // A* on a single floor
    private fun findPathSameFloor(start: Tile, goal: Tile): List<Tile>? {
        val openSet = PriorityQueue<Node>()
        val closedSet = mutableSetOf<Tile>()
        val gScores = mutableMapOf<Tile, Int>()

        val startNode = Node(start, 0, heuristic(start, goal), null)
        openSet.add(startNode)
        gScores[start] = 0

        var iterations = 0

        while (openSet.isNotEmpty() && iterations < MAX_ITERATIONS) {
            iterations++
            val current = openSet.poll()

            if (current.tile == goal) {
                return reconstructPath(current)
            }

            if (current.tile in closedSet) continue
            closedSet.add(current.tile)

            for (neighbor in getNeighbors(current.tile)) {
                if (neighbor in closedSet) continue
                if (!isWalkable(neighbor)) continue

                val tentativeG = current.gCost + moveCost(current.tile, neighbor)

                if (tentativeG < (gScores[neighbor] ?: Int.MAX_VALUE)) {
                    gScores[neighbor] = tentativeG
                    val neighborNode = Node(neighbor, tentativeG, heuristic(neighbor, goal), current)
                    openSet.add(neighborNode)
                }
            }
        }

        Logger.warn("[$TAG] No path found from $start to $goal after $iterations iterations")
        return null
    }

    // Path across multiple floors using stairs
    private fun findPathAcrossFloors(start: Tile, goal: Tile): List<Tile>? {
        // Find stairs on start floor that go toward goal floor
        val startFloorStairs = StairLanding.stairTilesOnFloor(start.floor)
        val goalFloorStairs = StairLanding.stairTilesOnFloor(goal.floor)

        if (startFloorStairs.isEmpty() || goalFloorStairs.isEmpty()) {
            Logger.warn("[$TAG] No stair connections between floors ${start.floor} and ${goal.floor}")
            return null
        }

        // Simple approach: find best stair on each floor
        val bestStartStair = startFloorStairs.minByOrNull { heuristic(start, it) + heuristic(it, goal) }
        val stairLink = bestStartStair?.let { StairLanding.from(it).firstOrNull { link -> link.to.floor == goal.floor || link.changesFloor } }

        if (bestStartStair == null || stairLink == null) {
            Logger.warn("[$TAG] Cannot find valid stair route from floor ${start.floor} to ${goal.floor}")
            return null
        }

        // Build path: start -> stair entry -> stair exit -> goal
        val pathToStair = findPathSameFloor(start, bestStartStair) ?: return null
        val pathFromStair = findPathSameFloor(stairLink.to, goal) ?: findPathAcrossFloors(stairLink.to, goal) ?: return null

        return pathToStair + pathFromStair
    }

    // Get walkable neighbors (4-directional + stairs)
    private fun getNeighbors(tile: Tile): List<Tile> {
        val neighbors = mutableListOf<Tile>()

        // Cardinal directions
        neighbors.add(Tile(tile.x + 1, tile.y, tile.floor))
        neighbors.add(Tile(tile.x - 1, tile.y, tile.floor))
        neighbors.add(Tile(tile.x, tile.y + 1, tile.floor))
        neighbors.add(Tile(tile.x, tile.y - 1, tile.floor))

        // Stair connections
        neighbors.addAll(StairLanding.targetsFrom(tile))

        return neighbors
    }

    // Check if tile is walkable (within library bounds)
    private fun isWalkable(tile: Tile): Boolean {
        return Locations.isInsideLibrary(tile)
    }

    // Movement cost between adjacent tiles
    private fun moveCost(from: Tile, to: Tile): Int {
        // Stair transitions cost more
        if (from.floor != to.floor) return 5
        return 1
    }

    // Manhattan distance heuristic
    private fun heuristic(a: Tile, b: Tile): Int {
        val dx = abs(a.x - b.x)
        val dy = abs(a.y - b.y)
        val dFloor = abs(a.floor - b.floor) * 10 // Floor changes are expensive
        return dx + dy + dFloor
    }

    // Reconstruct path from goal node
    private fun reconstructPath(goalNode: Node): List<Tile> {
        val path = mutableListOf<Tile>()
        var current: Node? = goalNode

        while (current != null) {
            path.add(0, current.tile)
            current = current.parent
        }

        return path
    }

    // Check if path exists between two tiles
    fun hasPath(start: Tile, goal: Tile): Boolean = findPath(start, goal) != null

    // Get path length or -1 if no path
    fun pathLength(start: Tile, goal: Tile): Int = findPath(start, goal)?.size ?: -1
}