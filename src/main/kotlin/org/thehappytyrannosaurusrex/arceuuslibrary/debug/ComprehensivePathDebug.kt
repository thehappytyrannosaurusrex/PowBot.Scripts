package org.thehappytyrannosaurusrex.arceuuslibrary.debug

import kotlin.random.Random
import org.powbot.api.Tile
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Players
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Bookshelves
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Locations
import org.thehappytyrannosaurusrex.arceuuslibrary.pathing.LibraryPathfinder
import org.thehappytyrannosaurusrex.arceuuslibrary.utils.Logger

/**
 * Comprehensive debug:
 *   start -> random shelves on each floor -> central 1st floor -> NPC anchor
 *
 * - runOffline(): A* only, logging paths (no movement).
 * - runLive():    A* logging + Movement.walkTo each hop.
 */
object ComprehensivePathDebug {

    private const val SHELVES_PER_FLOOR = 2

    /**
     * Build a list of waypoints:
     *   [0] start (player tile or library anchor)
     *   [1..] random shelves on each floor
     *   [X] central first floor
     *   [last] random NPC anchor
     */
    private fun buildWaypoints(rng: Random): List<Tile> {
        val me = Players.local()
        if (!me.valid()) {
            Logger.warn("[DebugPath] Local player is not valid; cannot build waypoints.")
            return emptyList()
        }

        val playerTile = me.tile()

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
        Logger.info("[DebugPath] Start waypoint: $startTile")

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

        // Random shelves per floor
        addRandomShelfWaypoints(0, SHELVES_PER_FLOOR) // ground floor
        addRandomShelfWaypoints(1, SHELVES_PER_FLOOR) // first floor
        addRandomShelfWaypoints(2, SHELVES_PER_FLOOR) // second floor

        // Central first-floor area (keep in sync with Locations)
        val centralFirstFloor = Tile(1638, 3813, 1)
        waypoints += centralFirstFloor
        Logger.info("[DebugPath] Added central first-floor waypoint: $centralFirstFloor")

        // NPC anchors on ground floor
        val npcAnchors = listOf(
            Tile(1639, 3801, 0), // Sam
            Tile(1625, 3801, 0), // Professor
            Tile(1626, 3814, 0)  // Villia
        )
        val npcTarget = npcAnchors.shuffled(rng).first()
        waypoints += npcTarget
        Logger.info("[DebugPath] Added NPC anchor waypoint: $npcTarget")

        return waypoints
    }

    /**
     * Original offline test: A* only, log paths, no movement.
     */
    fun runOffline() {
        val rng = Random(System.currentTimeMillis())
        val waypoints = buildWaypoints(rng)
        if (waypoints.size < 2) {
            Logger.warn("[DebugPath] Not enough waypoints for offline test; aborting.")
            return
        }

        val totalHops = waypoints.size - 1
        Logger.info("[DebugPath] (Offline) Will compute $totalHops hops.")

        var successfulHops = 0
        var failedHops = 0
        var totalSteps = 0

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

        Logger.info(
            "[DebugPath] (Offline) Summary: hops=$totalHops, " +
                    "successful=$successfulHops, failed=$failedHops, totalSteps=$totalSteps"
        )
    }

    /**
     * New multi-hop live test:
     *  - same waypoints as runOffline
     *  - still calls A* for logging / sanity
     *  - but actually walks each hop with Movement.walkTo(...)
     */
    fun runLive() {
        val rng = Random(System.currentTimeMillis())
        val waypoints = buildWaypoints(rng)
        if (waypoints.size < 2) {
            Logger.warn("[LivePath] Not enough waypoints for live test; aborting.")
            return
        }

        val totalHops = waypoints.size - 1
        Logger.info("[LivePath] Multi-hop live test will perform $totalHops hops.")

        var hopsSucceeded = 0
        var hopsFailed = 0

        for (i in 0 until totalHops) {
            val from = waypoints[i]
            val to = waypoints[i + 1]

            Logger.info("[LivePath] Hop ${i + 1}/$totalHops: $from -> $to")

            // Optional: still check our own A* connectivity for this hop
            val path = LibraryPathfinder.findPath(from, to)
            if (path == null) {
                Logger.warn("[LivePath] Hop ${i + 1}: A* found no path $from -> $to")
            } else {
                Logger.info("[LivePath] Hop ${i + 1}: A* path length=${path.size}")
            }

            Logger.info("[LivePath] Hop ${i + 1}: Movement.walkTo($to)")
            val ok = Movement.walkTo(to)

            if (!ok) {
                hopsFailed++
                Logger.warn("[LivePath] Hop ${i + 1}: Movement.walkTo failed for $to")
                continue
            }

            hopsSucceeded++
            Logger.info("[LivePath] Hop ${i + 1}: Movement.walkTo succeeded.")
        }

        Logger.info(
            "[LivePath] Summary: hops=$totalHops, " +
                    "successful=$hopsSucceeded, failed=$hopsFailed"
        )
    }
}
