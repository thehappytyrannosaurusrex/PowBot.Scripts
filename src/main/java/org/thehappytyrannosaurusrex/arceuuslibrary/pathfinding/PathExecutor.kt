/*
 * Project: Arceuus Library Script (PowBot)
 * File: PathExecutor.kt
 * Purpose: Added review header and standardized logging to Logger.info.
 * Notes: Generated comments + logging normalization on 2025-11-12.
 */

package org.thehappytyrannosaurusrex.arceuuslibrary.pathfinding

import org.thehappytyrannosaurusrex.arceuuslibrary.utils.Logger;
import org.powbot.api.Condition
import org.powbot.api.Tile
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.Players
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Bookshelf
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Bookshelves

/**
 * Executes a planned route of NodeId’s:
 *  - SHELF_STAND: ensure orthogonal standing tile, then Search.
 *  - STAIR_LANDING: detect plane change needed and delegate to StairNavigator.
 *  - Others: step to tile.
 */
/**
 * PathExecutor: Core component of the Arceuus Library script.
 * Auto-generated doc stub (reviewed 2025-11-12).
 */
object PathExecutor {

    private const val SHELF_ANIM = 832   // Arceuus bookshelf search

    fun walkPath(path: List<NodeId>): Boolean {
        if (path.isEmpty()) {
            Logger.info("A* RETURN false (empty path)")
            return false
        }

        var i = 0
        while (i < path.size) {
            val cur = path[i]
            val next = path.getOrNull(i + 1)

            // Handle stair transition only when both nodes are landings and z differs
            if (next != null &&
                cur.kind == NodeId.Kind.STAIR_LANDING &&
                next.kind == NodeId.Kind.STAIR_LANDING &&
                cur.z != next.z
            ) {
                // stand on the current landing first
                if (!stepTo(cur)) {
                    Logger.info("A* RETURN false (couldn't step to current landing)")
                    return false
                }
                if (!StairNavigator.climbToFloor(next.z)) {
                    Logger.info("A* RETURN false (stair climb failed)")
                    return false
                }
                // after plane change, advance to next node
                i += 1
                continue
            }

            // Same-floor hop (or non-stair nodes)
            when (cur.kind) {
                NodeId.Kind.SHELF_STAND -> {
                    val shelf = Bookshelves.BY_INDEX[cur.ref]
                    if (shelf == null) {
                        i += 1
                        continue
                    }
                    if (!stepTo(cur)) {
                        Logger.info("A* RETURN false (to shelf stand)")
                        return false
                    }
                    if (!searchShelf(shelf)) {
                        Logger.info("A* RETURN false (shelf search)")
                        return false
                    }
                }
                else -> {
                    if (!stepTo(cur)) {
                        Logger.info("A* RETURN false (stepTo generic)")
                        return false
                    }
                }
            }

            i += 1
        }

        Logger.info("A* RETURN true")
        return true
    }

    private fun onTile(n: NodeId): Boolean =
        Tile(n.x, n.y, n.z) == Players.local().tile()

    private fun stepTo(n: NodeId): Boolean {
        val t = Tile(n.x, n.y, n.z)
        if (Players.local().tile() == t) return true
        Movement.step(t)
        return waitArrival(n)
    }

    private fun waitArrival(n: NodeId, ticks: Int = 50): Boolean {
        var i = 0
        while (i < ticks) {
            if (onTile(n) && !Players.local().inMotion()) return true
            Condition.sleep(100)
            i++
        }
        return onTile(n)
    }

    private fun clickWalkHere(tile: Tile) {
        Movement.walkTo(tile)
    }

    /** Enforce "Search" only. Retry by snapping to standing tile if needed. */
// PathExecutor.kt (replace the whole method)
    private fun searchShelf(s: Bookshelf): Boolean {
        // enforce orthogonal stand tile first
        if (Players.local().tile() != s.standingTile) {
            Movement.step(s.standingTile)
            Condition.wait({ Players.local().tile() == s.standingTile && !Players.local().inMotion() }, 100, 25)
        }

        repeat(2) {
            val obj = Objects.stream().at(s.objTile).id(s.shelfObjId).nearest().first()
            if (!obj.valid()) {
                Movement.step(s.standingTile)
                Condition.sleep(200)
                return@repeat
            }

            if (!obj.actions().any { it.equals("Search", true) } || !obj.interact("Search")) {
                Condition.sleep(120)
                return@repeat
            }

            // wait animation to start
            val started = Condition.wait({
                Players.local().animation() == SHELF_ANIM
            }, 50, 20)

            // wait for animation to end
            val finished = Condition.wait({
                Players.local().animation() == -1 && !Players.local().inMotion()
            }, 100, 50)

        }
        Logger.info("A* RETURN false"); return false
    }


    fun walkToNpcAndInteract(graph: Graph, npcName: String, action: String = "Help"): Boolean =
        NpcNavigator.walkToNpcAndInteract(graph, npcName, action)

}