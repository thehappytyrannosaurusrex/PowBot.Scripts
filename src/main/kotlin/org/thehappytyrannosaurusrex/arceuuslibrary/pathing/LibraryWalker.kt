package org.thehappytyrannosaurusrex.arceuuslibrary.pathing

import org.powbot.api.Condition
import org.powbot.api.Tile
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Players
import org.thehappytyrannosaurusrex.api.utils.Logger

object LibraryWalker {

    private const val TAG = "PathWalker"

    // Follow a computed path tile by tile
    fun followPath(path: List<Tile>, maxStepTimeoutMs: Int = 6000): Boolean {
        if (path.isEmpty()) {
            Logger.warn("[$TAG] Empty path, nothing to follow.")
            return false
        }

        val lastIndex = path.lastIndex

        for ((index, target) in path.withIndex()) {
            val me = Players.local()
            if (!me.valid()) {
                Logger.warn("[$TAG] Local player invalid at step $index; aborting.")
                return false
            }

            val current = me.tile()
            val dist = current.distanceTo(target).toDouble()

            // Already close enough: skip step
            if (dist <= 1.0) {
                Logger.info("[$TAG] Step $index/$lastIndex – already near $target (dist=${"%.2f".format(dist)}), skipping.")
                continue
            }

            Logger.info("[$TAG] Step $index/$lastIndex – Walking to $target")

            if (!Movement.walkTo(target)) {
                Logger.warn("[$TAG] Movement.walkTo failed at step $index ($target)")
                return false
            }

            // Wait until close enough or timeout
            val attempts = maxStepTimeoutMs / 250
            val arrived = Condition.wait(
                { Players.local().tile().distanceTo(target) <= 1 },
                250,
                attempts
            )

            if (!arrived) {
                Logger.warn("[$TAG] Timed out walking to step $index ($target)")
                return false
            }
        }

        Logger.info("[$TAG] Successfully followed path with ${path.size} steps.")
        return true
    }

    // Compress path by removing redundant intermediate tiles
    fun compressPath(path: List<Tile>): List<Tile> {
        if (path.size <= 2) return path

        val result = mutableListOf<Tile>()
        result += path.first()

        var lastFloor = path[0].floor
        var lastDx = path[1].x - path[0].x
        var lastDy = path[1].y - path[0].y

        for (i in 1 until path.size - 1) {
            val cur = path[i]
            val next = path[i + 1]

            val dx = next.x - cur.x
            val dy = next.y - cur.y

            val floorChanged = cur.floor != lastFloor
            val directionChanged = dx != lastDx || dy != lastDy

            if (floorChanged || directionChanged) {
                result += cur
                lastDx = dx
                lastDy = dy
                lastFloor = cur.floor
            }
        }

        result += path.last()
        return result
    }

    // Reduce path to coarse waypoints for fewer clicks
    fun coarseWaypoints(path: List<Tile>, maxClicks: Int = 4): List<Tile> {
        if (path.isEmpty()) return emptyList()
        if (path.size <= maxClicks) return path

        val result = mutableListOf<Tile>()
        val lastIndex = path.lastIndex
        val segments = maxClicks - 1

        for (i in 0..segments) {
            val idx = (i * lastIndex) / segments
            val tile = path[idx]
            if (result.isEmpty() || result.last() != tile) {
                result += tile
            }
        }

        return result
    }

    // Debug helper: log path and attempt to walk
    fun debugFollowTo(target: Tile) {
        val me = Players.local()
        if (!me.valid()) {
            Logger.warn("[$TAG] Local player not valid; cannot debugFollowTo.")
            return
        }

        val start = me.tile()
        Logger.info("[$TAG] Debug follow from $start to $target")

        val rawPath = LibraryPathfinder.findPath(start, target)
        if (rawPath == null) {
            Logger.warn("[$TAG] No A* path from $start to $target")
        } else {
            Logger.info("[$TAG] A* path has ${rawPath.size} tiles:")
            rawPath.forEachIndexed { idx, t ->
                Logger.info("[$TAG] #$idx -> $t")
            }
        }

        Logger.info("[$TAG] Calling Movement.walkTo($target)...")
        val ok = Movement.walkTo(target)
        Logger.info("[$TAG] Movement.walkTo result: ${if (ok) "SUCCESS" else "FAILURE"}")
    }
}