/*
 * Project: Arceuus Library Script (PowBot)
 * File: NpcNavigator.kt
 * Purpose: Added review header and standardized logging to Logger.info.
 * Notes: Generated comments + logging normalization on 2025-11-12.
 */

package org.thehappytyrannosaurusrex.arceuuslibrary.pathfinding

import org.powbot.api.Condition
import org.powbot.api.rt4.Npcs
import org.powbot.api.rt4.Players


/**
 * NpcNavigator: Core component of the Arceuus Library script.
 * Auto-generated doc stub (reviewed 2025-11-12).
 */
object NpcNavigator {

    private const val REPLAN_DRIFT_TILES = 5   // replan if NPC moved this far from last goal
    private const val CLOSE_ENOUGH_TILES = 2   // when within this range, interact
    private const val MAX_REPLAN_LOOPS = 8     // safety bound
    private const val INTERACT_ACTION = "Help"

    /**
     * Walk to [npcName] using A* and PathExecutor (stairs handled identically to shelf paths),
     * dynamically replanning when the NPC wanders. Returns true if we managed to interact.
     */
    fun walkToNpcAndInteract(graph: Graph, npcName: String, action: String = INTERACT_ACTION): Boolean {
        var loops = 0
        var lastGoalTile = Players.local().tile() // init

        while (loops++ < MAX_REPLAN_LOOPS) {
            val npc = Npcs.stream().name(npcName).nearest().first()
            if (!npc.valid()) return false

            val goal = npc.tile()
            val me = Players.local().tile()

            // Close enough? Just interact.
            if (me.floor == goal.floor && me.distanceTo(goal) <= CLOSE_ENOUGH_TILES) {
                return Npcs.stream().name(npcName).nearest().first().valid()
                        && Npcs.stream().name(npcName).nearest().first().interact(action)
            }

            // If the NPC drifted far from our previous target, we’ll plan to the new position
            val needReplan = PathPlanner.tileManhattan(lastGoalTile, goal) >= REPLAN_DRIFT_TILES
            if (needReplan || loops == 1) {
                val path = PathPlanner.shortestToNpcNow(graph, me, npcName)
                if (path.isEmpty()) return false

                // Execute via PathExecutor so we reuse robust stairs & arrival logic
                val ok = PathExecutor.walkPath(path)
                if (!ok) return false

                // After walking that path, either we're close enough or the NPC moved; loop will handle both.
                lastGoalTile = goal
                // A tiny pause helps the stream update in busier areas
                Condition.sleep(120)
                continue
            }

            // No replan requested, but we’re still not close: brief wait then retry (gives the client time to settle)
            Condition.sleep(120)
        }

        // One last attempt to interact if we ended nearby
        val npc = Npcs.stream().name(npcName).nearest().first()
        return npc.valid() && npc.interact(action)
    }
}