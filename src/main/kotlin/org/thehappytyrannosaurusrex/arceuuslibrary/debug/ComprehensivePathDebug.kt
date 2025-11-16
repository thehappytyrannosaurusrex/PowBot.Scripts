package org.thehappytyrannosaurusrex.arceuuslibrary.debug

import kotlin.random.Random
import org.powbot.api.Tile
import org.powbot.api.rt4.Players
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Bookshelves
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Locations
import org.thehappytyrannosaurusrex.arceuuslibrary.pathing.LibraryPathfinder
import org.thehappytyrannosaurusrex.arceuuslibrary.pathing.LibraryPathfinder.findPath
import org.thehappytyrannosaurusrex.arceuuslibrary.utils.Logger
import kotlin.collections.forEachIndexed

/**
 * Multi-hop debug:
 *   start -> random shelf standing tiles on each floor -> central 1st floor -> NPC anchor
 *
 * Logging-only. NO movement.
 */
object ComprehensivePathDebug {

    private const val SHELVES_PER_FLOOR = 2

    fun debugLogPath(start: Tile, goal: Tile, maxNodes: Int = 20_000) {
        val path = findPath(start, goal, maxNodes)
        if (path == null) {
            Logger.warn("[Pathfinder] debugLogPath: No path from $start to $goal")
            return
        }

        Logger.info("[Pathfinder] debugLogPath: path length=${path.size} from $start to $goal")
        path.forEachIndexed { index, tile ->
            Logger.info("[Pathfinder]   #$index -> $tile")
        }
    }

    fun run() {
        val me = Players.local()
        if (!me.valid()) {
            Logger.warn("[DebugPath] Local player is not valid; skipping comprehensive debug.")
            return
        }

        val rng = Random(System.currentTimeMillis())
        val playerTile = me.tile()

        // Start either at the player's tile (if inside library) or the library anchor.
        val startTile: Tile =
            if (Locations.isInsideLibrary(playerTile)) {
                playerTile
            } else {
                Logger.info(
                    "[DebugPath] Player is outside the library; " +
                            "using library center ${Locations.libraryTile} as start."
                )
                Locations.libraryTile
            }

        val waypoints = mutableListOf<Tile>()
        waypoints += startTile

        // ---- 1) Random shelf standing tiles per floor ----
        fun addRandomShelfWaypoints(floor: Int, count: Int) {
            val shelvesOnFloor = Bookshelves.ALL.filter { it.floor == floor }
            if (shelvesOnFloor.isEmpty()) {
                Logger.warn("[DebugPath] No shelves configured for floor $floor; skipping.")
                return
            }

            val chosen = shelvesOnFloor.shuffled(rng).take(count)
            for (shelf in chosen) {
                waypoints += shelf.standingTile
                Logger.info(
                    "[DebugPath] Added shelf waypoint: " +
                            "floor=$floor, index=${shelf.shelfIndex}, standing=${shelf.standingTile}"
                )
            }
        }

        addRandomShelfWaypoints(0, SHELVES_PER_FLOOR) // ground floor
        addRandomShelfWaypoints(1, SHELVES_PER_FLOOR) // first floor
        addRandomShelfWaypoints(2, SHELVES_PER_FLOOR) // second floor

        // ---- 2) Central first-floor area ----
        val centralFirstFloor = Tile(1638, 3813, 1) // keep in sync with Locations.kt
        waypoints += centralFirstFloor
        Logger.info("[DebugPath] Added central first-floor waypoint: $centralFirstFloor")

        // ---- 3) One of the NPC anchor tiles on ground floor ----
        val npcAnchors = listOf(
            Tile(1639, 3801, 0), // Sam
            Tile(1625, 3801, 0), // Professor
            Tile(1626, 3814, 0)  // Villia
        )
        val npcTarget = npcAnchors.shuffled(rng).first()
        waypoints += npcTarget
        Logger.info("[DebugPath] Added NPC anchor waypoint: $npcTarget")

        val totalHops = waypoints.size - 1
        Logger.info("[DebugPath] Comprehensive path debug will compute $totalHops hops.")

        var successfulHops = 0
        var failedHops = 0
        var totalSteps = 0

        // ---- 4) Run A* between each consecutive pair and log the results ----
        for (i in 0 until totalHops) {
            val from = waypoints[i]
            val to = waypoints[i + 1]

            Logger.info("[DebugPath] Hop ${i + 1}/$totalHops: $from -> $to")

            val path = LibraryPathfinder.findPath(from, to)
            if (path == null) {
                failedHops++
                Logger.warn("[Pathfinder] Hop ${i + 1}: no path from $from to $to")
                continue
            }

            successfulHops++
            totalSteps += path.size

            Logger.info("[Pathfinder] Hop ${i + 1}: path length=${path.size}")
            path.forEachIndexed { idx, tile ->
                Logger.info("[Pathfinder]   Hop ${i + 1} step #$idx -> $tile")
            }
        }

        // ---- 5) Summary ----
        Logger.info(
            "[DebugPath] Summary: hops=$totalHops, " +
                    "successful=$successfulHops, failed=$failedHops, totalSteps=$totalSteps"
        )
    }
}
