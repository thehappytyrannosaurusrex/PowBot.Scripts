/*
 * Project: Arceuus Library Script (PowBot)
 * File: PathSmoke.kt
 * Purpose: Added review header and standardized logging to Logger.info.
 * Notes: Generated comments + logging normalization on 2025-11-12.
 */

package org.thehappytyrannosaurusrex.arceuuslibrary.test

import org.powbot.api.Condition
import org.powbot.api.Tile
import org.powbot.api.rt4.Players
import kotlin.random.Random
import org.thehappytyrannosaurusrex.arceuuslibrary.pathfinding.*
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Bookshelf
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Bookshelves
import org.thehappytyrannosaurusrex.arceuuslibrary.data.StairConfig
import org.thehappytyrannosaurusrex.arceuuslibrary.pathfinding.NpcNavigator
import org.thehappytyrannosaurusrex.arceuuslibrary.pathfinding.PathExecutor
import org.thehappytyrannosaurusrex.arceuuslibrary.utils.Logger

/**
 * Ready-made smoke runner:
 *  1) Ground→random Second floor shelf (multi-hop stairs)
 *  2) A shelf near stairs (tests tight stand + “walk-here snap” behavior)
 *  3) First-floor inter-area (NE → NW) must use stairs
 *  4) Dynamic NPC goal + interact (drift-aware replan)
 *
 * Usage: PathSmoke.runAll(npcName = "Sam")
 */
/**
 * PathSmoke: Core component of the Arceuus Library script.
 * Auto-generated doc stub (reviewed 2025-11-12).
 */
object PathSmoke {

    private const val CLOSE_OK_TILES = 0   // we expect exact stand tile for shelves
    private const val MAX_MILLIS_BETWEEN_STEPS = 150

    fun runAll(npcName: String = "Sam", npcAction: String = "Talk-to"): Boolean {
        Logger.info("PathSmoke: building navigation graph...")
        val graph = PathPlanner.buildGraph()

        val me = Players.local().tile()
        Logger.info("PathSmoke: you are at $me")

        // ---- 1) Ground → random Second floor shelf (multi-hop) ----
        val gfShelf = randomShelf(0) ?: return fail("No Ground floor shelves found")
        val f2Shelf = randomShelf(2) ?: return fail("No Second floor shelves found")

        Logger.info("1) GF → F2  | from=${gfShelf.shelfIndex}@${t(gfShelf.standingTile)}  to=${f2Shelf.shelfIndex}@${t(f2Shelf.standingTile)}")
        if (!walkToShelf(graph, gfShelf)) return fail("Failed walking to GF shelf ${gfShelf.shelfIndex}")
        if (!pathAndWalkToShelf(graph, f2Shelf, requireStairs = true)) return fail("Failed GF→F2 route: stairs not used or execution failed")
        Logger.info("✓ 1) OK")

        // ---- 2) Shelf near a staircase (tight stand snap + Search-only) ----
        val nearStairShelf = pickShelfNearAnyLanding(maxDist = 3) ?: randomShelf(1) ?: randomShelf(0) ?: return fail("No shelves for near-stairs test")
        Logger.info("2) Near-stair shelf | index=${nearStairShelf.shelfIndex} stand=${t(nearStairShelf.standingTile)}")
        if (!pathAndWalkToShelf(graph, nearStairShelf, requireStairs = false)) return fail("Near-stair shelf route failed")
        Logger.info("✓ 2) OK")

        // ---- 3) First-floor NE → NW (must use stairs due to F1 isolation) ----
        val f1NE = randomShelf(1, areaName = "NORTHEAST") ?: return fail("No F1 NE shelves")
        val f1NW = randomShelf(1, areaName = "NORTHWEST") ?: return fail("No F1 NW shelves")

        Logger.info("3) F1 NE → NW | NE=${f1NE.shelfIndex}@${t(f1NE.standingTile)}  NW=${f1NW.shelfIndex}@${t(f1NW.standingTile)}")
        if (!walkToShelf(graph, f1NE)) return fail("Failed to reach F1 NE shelf ${f1NE.shelfIndex}")
        if (!pathAndWalkToShelf(graph, f1NW, requireStairs = true)) return fail("F1 NE→NW did not use stairs or failed")
        Logger.info("✓ 3) OK")

        // ---- 4) Dynamic NPC goal: replan + interact ----
        Logger.info("4) NPC dynamic | npc=$npcName action=$npcAction")
        val npcOk = NpcNavigator.walkToNpcAndInteract(graph, npcName, npcAction)
        if (!npcOk) return fail("Failed to reach or interact with NPC '$npcName'")
        Logger.info("✓ 4) OK")

        Logger.info("PathSmoke: ALL CHECKS PASSED 🎉")
        Logger.info("A* RETURN true"); return true
    }

    // ---------- Helpers ----------

    private fun randomShelf(floor: Int, areaName: String? = null): Bookshelf? {
        val pool = Bookshelves.ALL.filter { it.floor == floor && (areaName == null || it.area.name == areaName) }
        if (pool.isEmpty()) return null
        return pool[Random.nextInt(pool.size)]
    }

    private fun pickShelfNearAnyLanding(maxDist: Int = 3): Bookshelf? {
        // Build a set of landing tiles from StairConfig
        val landings = StairConfig.STAIRS.flatMap { listOf(it.from, it.to) }
        var best: Bookshelf? = null
        var bestD = Int.MAX_VALUE
        for (s in Bookshelves.ALL) {
            val d = landings.minOfOrNull { md(s.standingTile, it) } ?: continue
            if (d <= maxDist && d < bestD) { best = s; bestD = d }
        }
        return best
    }

    private fun pathAndWalkToShelf(graph: Graph, shelf: Bookshelf, requireStairs: Boolean): Boolean {
        val me = Players.local().tile()
        val path = PathPlanner.shortestToShelf(graph, me, shelf.shelfIndex)
        if (path.isEmpty()) {
            Logger.info("  → empty path to shelf ${shelf.shelfIndex}")
            Logger.info("A* RETURN false"); return false
        }
        val usedStairs = path.any { it.kind == NodeId.Kind.STAIR_LANDING }
        if (requireStairs && !usedStairs) {
            Logger.info("  → expected stairs in route but none found")
            Logger.info("A* RETURN false"); return false
        }
        Logger.info("  → path nodes=${path.size}  stairsUsed=$usedStairs")
        val ok = PathExecutor.walkPath(path)
        if (!ok) Logger.info("A* RETURN false"); return false
        return ensureAt(shelf.standingTile, CLOSE_OK_TILES)
    }

    private fun walkToShelf(graph: Graph, shelf: Bookshelf): Boolean =
        pathAndWalkToShelf(graph, shelf, requireStairs = false)

    private fun ensureAt(tile: Tile, maxDistance: Int): Boolean {
        return Condition.wait({
            val me = Players.local().tile()
            me.floor == tile.floor && me.distanceTo(tile) <= maxDistance
        }, MAX_MILLIS_BETWEEN_STEPS, 25).also { ok ->
            val here = Players.local().tile()
            Logger.info("  → at=${t(here)}  target=${t(tile)}  ok=$ok")
        }
    }

    private fun t(tile: Tile) = "(${tile.x},${tile.y},${tile.floor})"

    private fun md(a: Tile, b: Tile) = kotlin.math.abs(a.x - b.x) + kotlin.math.abs(a.y - b.y) + if (a.floor == b.floor) 0 else 999

    private fun fail(msg: String): Boolean {
        Logger.info("PathSmoke: FAILED — $msg")
        Logger.info("A* RETURN false"); return false
    }
}


// Added to ease local manual runs without poll():
fun main() {
    try {
        Logger.info("PathSmoke manual start")
        try {
            PathSmoke.runAll("Sam")
        } catch (e: Throwable) {
            Logger.error("PathSmoke.runAll failed: " + e.toString())
        }
        Logger.info("PathSmoke manual end")
    } catch (t: Throwable) {
        Logger.error("Fatal in PathSmoke main: " + t.toString())
    }
}
