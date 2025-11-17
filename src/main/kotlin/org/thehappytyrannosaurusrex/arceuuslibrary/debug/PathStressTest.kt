package org.thehappytyrannosaurusrex.arceuuslibrary.debug

import kotlin.random.Random
import org.powbot.api.Condition
import org.powbot.api.Tile
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Players
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Bookshelves
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Locations
import org.thehappytyrannosaurusrex.arceuuslibrary.pathing.LibraryPathfinder
import org.thehappytyrannosaurusrex.api.utils.Logger

object PathStressTest {

    // How many hops to attempt in one run.
    private const val DEFAULT_HOPS = 30

    // Max milliseconds to wait per hop after calling Movement.walkTo.
    private const val HOP_TIMEOUT_MS = 20_000

    fun runLiveStress(hops: Int = DEFAULT_HOPS) {
        val me = Players.local()
        if (!me.valid()) {
            Logger.warn("[StressPath] Local player invalid; cannot run stress test.")
            return
        }

        if (!Locations.isInsideLibrary(me.tile())) {
            Logger.warn("[StressPath] Player is not inside library; stress test expects to start in library.")
        }

        val rng = Random(System.currentTimeMillis())

        // Pre-build a pool of interesting targets:
        //  - all shelf standing tiles
        //  - central first floor
        //  - NPC anchors
        val shelfTargets = Bookshelves.ALL.map { it.standingTile }

        val centralFirstFloor = Tile(1638, 3813, 1)
        val npcAnchors = listOf(
            Tile(1639, 3801, 0), // Sam
            Tile(1625, 3801, 0), // Professor
            Tile(1626, 3814, 0)  // Villia
        )

        val fixedTargets = listOf(centralFirstFloor) + npcAnchors
        val allTargets = shelfTargets + fixedTargets

        if (allTargets.isEmpty()) {
            Logger.warn("[StressPath] No targets configured; aborting.")
            return
        }

        Logger.info("[Arceuus Library] DEBUG | Starting live stress test with $hops hops…")

        var hopsAttempted = 0
        var hopsSucceeded = 0
        var hopsFailed = 0
        var hopsNoPath = 0

        var totalPathTiles = 0
        var pathCount = 0

        repeat(hops) { hopIndex ->
            val meNow = Players.local()
            if (!meNow.valid()) {
                Logger.warn("[StressPath] Local player invalid mid-test; aborting.")
                return
            }

            val start = meNow.tile()

            // Try to pick a target with some distance; avoid tiny hops right next to us.
            val target = pickReasonableTarget(start, allTargets, rng)

            hopsAttempted++
            val humanIndex = hopIndex + 1
            Logger.info("[Arceuus Library] DEBUG | Hop $humanIndex/$hops: $start -> $target")

            // A* path for this hop (for diagnostics & stats)
            val path = LibraryPathfinder.findPath(start, target)
            if (path == null) {
                Logger.warn("[StressPath] Hop $humanIndex: A* found no path from $start to $target")
                hopsNoPath++
            } else {
                Logger.info("[Arceuus Library] DEBUG | Hop $humanIndex: A* path length=${path.size}")
                totalPathTiles += path.size
                pathCount++
            }

            // Actually walk using Movement.walkTo
            Logger.info("[Arceuus Library] DEBUG | Hop $humanIndex: Movement.walkTo($target)")
            val walkOk = Movement.walkTo(target)

            // Wait until we are near the target or timeout
            val attempts = HOP_TIMEOUT_MS / 500
            val arrived = Condition.wait(
                {
                    val cur = Players.local().tile()
                    cur.floor == target.floor && cur.distanceTo(target) <= 2
                },
                500,
                attempts
            )

            if (walkOk && arrived) {
                hopsSucceeded++
                Logger.info("[Arceuus Library] DEBUG | Hop $humanIndex: SUCCESS (arrived near $target)")
            } else {
                hopsFailed++
                Logger.warn(
                    "[StressPath] Hop $humanIndex: FAILURE, " +
                            "walkOk=$walkOk, arrived=$arrived"
                )
            }
        }

        val avgPathLen = if (pathCount > 0) totalPathTiles.toDouble() / pathCount else 0.0

        Logger.info("[Arceuus Library] DEBUG | Summary: " +
                    "hopsAttempted=$hopsAttempted, " +
                    "succeeded=$hopsSucceeded, failed=$hopsFailed, noPath=$hopsNoPath, " +
                    "avgAStarLength=%.2f (over $pathCount paths)"
                        .format(avgPathLen)
        )
    }

    private fun pickReasonableTarget(start: Tile, candidates: List<Tile>, rng: Random): Tile {
        val shuffled = candidates.shuffled(rng)

        // First try to find something with distance >= 5 tiles.
        val farEnough = shuffled.firstOrNull { it.distanceTo(start) >= 5 }
        if (farEnough != null) {
            return farEnough
        }

        // If everything is close (e.g. cramped area), just pick any random candidate.
        return shuffled.first()
    }
}
