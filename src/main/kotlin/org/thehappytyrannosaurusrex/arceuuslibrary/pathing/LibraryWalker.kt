package org.thehappytyrannosaurusrex.arceuuslibrary.pathing

import org.powbot.api.Condition
import org.powbot.api.Tile
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Players
import org.thehappytyrannosaurusrex.arceuuslibrary.utils.Logger

/**
 * Executes LibraryPathfinder paths in-game using Movement.walkTo.
 *
 * This is deliberately low-level and only used inside the library.
 */
object LibraryWalker {

    /**
     * Follow a path of tiles by stepping on each tile in order.
     *
     * @param path List of world tiles from start to goal (inclusive).
     * @param maxStepTimeoutMs Max time to wait for each step before failing.
     * @return true if we managed to walk the entire path, false on any failure.
     */
    fun followPath(path: List<Tile>, maxStepTimeoutMs: Int = 6000): Boolean {
        if (path.isEmpty()) {
            Logger.warn("[PathWalker] Empty path, nothing to follow.")
            return false
        }

        val lastIndex = path.lastIndex

        for ((index, target) in path.withIndex()) {
            val me = Players.local()
            if (!me.valid()) {
                Logger.warn("[PathWalker] Local player invalid at step $index; aborting.")
                return false
            }

            val current = me.tile()
            val dist = current.distanceTo(target).toDouble()

            // Already close enough: skip this step
            if (dist <= 1.0) {
                Logger.info(
                    "[PathWalker] Step $index/$lastIndex – already near $target " +
                            "(dist=%.2f), skipping.".format(dist)
                )
                continue
            }

            Logger.info("[PathWalker] Step $index/$lastIndex – Movement.walkTo($target)")
            if (!Movement.walkTo(target)) {
                Logger.warn("[PathWalker] Movement.walkTofailed at step $index ($target)")
                return false
            }

            // Wait until we get close enough or timeout
            val attempts = maxStepTimeoutMs / 250
            val arrived = Condition.wait(
                { Players.local().tile().distanceTo(target) <= 1 },
                250,
                attempts
            )

            if (!arrived) {
                Logger.warn("[PathWalker] Timed out walking to step $index ($target)")
                return false
            }
        }

        Logger.info("[PathWalker] Successfully followed path with ${path.size} steps.")
        return true
    }

    private fun compressPath(path: List<Tile>): List<Tile> {
        if (path.size <= 2) return path

        val result = mutableListOf<Tile>()
        result += path.first()

        var lastX = path[0].x
        var lastY = path[0].y
        var lastFloor = path[0].floor

        var lastDx = path[1].x - path[0].x
        var lastDy = path[1].y - path[0].y

        for (i in 1 until path.size - 1) {
            val cur = path[i]
            val next = path[i + 1]

            val dx = next.x - cur.x
            val dy = next.y - cur.y

            // Always keep floor changes as waypoints
            val floorChanged = cur.floor != lastFloor

            // Direction changed: corner
            val directionChanged = dx != lastDx || dy != lastDy

            if (floorChanged || directionChanged) {
                result += cur
                lastDx = dx
                lastDy = dy
                lastFloor = cur.floor
                lastX = cur.x
                lastY = cur.y
            }
        }

        // Always keep final goal
        result += path.last()

        return result
    }

    private fun coarseWaypoints(path: List<Tile>, maxClicks: Int = 4): List<Tile> {
        if (path.isEmpty()) return emptyList()
        if (path.size <= maxClicks) return path  // short paths: click each tile

        val result = mutableListOf<Tile>()
        val lastIndex = path.lastIndex
        val segments = maxClicks - 1 // number of segments between first and last

        // Evenly sample indices from 0..lastIndex inclusive
        for (i in 0..segments) {
            val idx = (i * lastIndex) / segments
            val tile = path[idx]
            if (result.isEmpty() || result.last() != tile) {
                result += tile
            }
        }

        return result
    }

    /**
     * Debug helper:
     *   - Compute A* from the player's current tile to [target]
     *   - Log the path
     *   - Follow it live with followPath(...)
     */
    fun debugFollowTo(target: Tile) {
        val me = Players.local()
        if (!me.valid()) {
            Logger.warn("[PathWalker] Local player not valid; cannot debugFollowTo.")
            return
        }

        val start = me.tile()
        Logger.info("[PathWalker] Debug follow from $start to $target")

        // Still compute the A* path for logging / sanity:
        val rawPath = LibraryPathfinder.findPath(start, target)
        if (rawPath == null) {
            Logger.warn("[PathWalker] No A* path from $start to $target")
        } else {
            Logger.info("[PathWalker] A* path has ${rawPath.size} tiles:")
            rawPath.forEachIndexed { idx, t ->
                Logger.info("[PathWalker]   #$idx -> $t")
            }
        }

        // But let the web walker do the actual moving:
        Logger.info("[PathWalker] Calling Movement.walkTo($target)…")
        val ok = Movement.walkTo(target) // or WebWalking.walkTo(...)
        Logger.info("[PathWalker] Movement.walkTo result: ${if (ok) "SUCCESS" else "FAILURE"}")
    }


}
