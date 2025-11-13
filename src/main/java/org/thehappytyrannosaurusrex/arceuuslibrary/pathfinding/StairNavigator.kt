/*
 * Project: Arceuus Library Script (PowBot)
 * File: StairNavigator.kt
 * Purpose: Added review header and standardized logging to Logger.info.
 * Notes: Generated comments + logging normalization on 2025-11-12.
 */

package org.thehappytyrannosaurusrex.arceuuslibrary.pathfinding

import org.thehappytyrannosaurusrex.arceuuslibrary.utils.Logger;
import org.powbot.api.Condition
import org.powbot.api.Filter
import org.powbot.api.MenuCommand
import org.powbot.api.Tile
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Menu
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.Players
import org.thehappytyrannosaurusrex.arceuuslibrary.data.CLIMB_ACTIONS
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Rect
import org.thehappytyrannosaurusrex.arceuuslibrary.data.StairSpec
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Stairs
import org.thehappytyrannosaurusrex.arceuuslibrary.data.StairConfig
import kotlin.math.abs
import kotlin.random.Random

/**
 * StairNavigator: Core component of the Arceuus Library script.
 * Auto-generated doc stub (reviewed 2025-11-12).
 */
object StairNavigator {

    private const val FLOOR_CHANGE_WAIT = 3_000
    private const val RETRY_OTHER_STAIRS = 2

    // StairNavigator.kt (replace the body of climbToFloor with this version)
    fun climbToFloor(targetFloor: Int): Boolean {
        val startFloor = Players.local().tile().floor
        if (startFloor == targetFloor) return true

        var current = startFloor
        var attempts = 0

        while (current != targetFloor && attempts < 6) {
            val hopTarget = nextHopTarget(current, targetFloor)
            val spec = Stairs.match(Players.local().tile(), hopTarget) ?: return false

            // Approach best candidate stairs for current→hopTarget
            val candidates = candidatesFor(spec.fromFloor, spec.toFloor).sortedBy {
                manhattan(Players.local().tile(), it.rect.center())
            }

            var climbed = false
            for (cand in candidates) {
                if (!approach(cand.rect.center(), cand.rect)) continue
                if (!clickClimb(cand)) continue

                val prevFloor = Players.local().tile().floor

                // HARD WAIT: plane must change
                val planeChanged = Condition.wait({
                    Players.local().tile().floor != prevFloor
                }, 100, FLOOR_CHANGE_WAIT / 100)

                if (!planeChanged) {
                    // didn’t change floors → try next candidate stair block
                    continue
                }

                // Optional landing sanity: we expect to be close to a known landing on the new floor
                val here = Players.local().tile()
                val nearLanding = StairConfig.STAIRS.any { st ->
                    st.to.floor == here.floor && here.distanceTo(st.to) <= 2
                }
                if (!nearLanding) {
                    // Give it a tick to settle camera/movement, then recheck
                    Condition.sleep(150)
                }

                current = Players.local().tile().floor
                climbed = true
                break
            }

            if (!climbed) return false
            attempts++
        }

        return Players.local().tile().floor == targetFloor
    }


    private fun candidatesFor(fromFloor: Int, toFloor: Int) =
        Stairs.SPECS.filter { it.fromFloor == fromFloor && it.toFloor == toFloor }

    private fun approach(target: Tile, rect: Rect): Boolean {
        ensureRunEnabled()
        if (!rect.contains(Players.local().tile())) {
            if (!stepAndWait(target, withinDist = 2)) return false
        }

        val obj = findStairObjectIn(rect)
        if (obj != null && !obj.inViewport()) {
            if (Movement.walkTo(obj.tile())) {
                Condition.wait({ obj.inViewport() }, 150, 12)
            }
        }
        return true
    }

    private fun clickClimb(spec: StairSpec): Boolean {
        val byIdInRect = Objects.stream()
            .id(*spec.stairIds) // spec.stairIds is IntArray (non-null)
            .filter { it.valid() && spec.rect.contains(it.tile()) }
            .nearest()
            .first()

        val targetObj = byIdInRect ?: Objects.stream()
            .name("Staircase", "Stairs")
            .filter { it.valid() && spec.rect.contains(it.tile()) }
            .nearest()
            .first()

        val obj = targetObj ?: Objects.stream().id(*spec.stairIds).nearest().first()
        obj ?: return false

        if (Menu.opened()) Menu.close()

        val filter = object : Filter<MenuCommand> {
            override fun accept(cmd: MenuCommand?): Boolean {
                val a = (cmd?.action ?: "").lowercase()
                return CLIMB_ACTIONS.any { it.lowercase() == a }
            }
        }

        val ok = obj.click(filter, true)
        Condition.sleep(Random.nextInt(90, 180))
        return ok
    }

    private fun findStairObjectIn(rect: Rect): GameObject? {
        val byId = Objects.stream()
            .id(*Stairs.ALL_IDS)
            .filter { it.valid() && rect.contains(it.tile()) }
            .nearest()
            .first()
        if (byId != null) return byId

        return Objects.stream()
            .name("Staircase", "Stairs")
            .filter { it.valid() && rect.contains(it.tile()) }
            .nearest()
            .first()
    }

    private fun waitFloorChange(prevFloor: Int, timeoutMs: Int): Boolean =
        Condition.wait({ Players.local().tile().floor != prevFloor }, 150, timeoutMs / 150)

    private fun stepAndWait(tile: Tile, withinDist: Int): Boolean {
        if (!Movement.walkTo(tile)) {
            Condition.wait({ !Players.local().inMotion() }, 100, 3)
            return false
        }
        Condition.wait({
            val me = Players.local().tile()
            me.floor == tile.floor && me.distanceTo(tile) <= withinDist
        }, 200, 25)
        return true
    }

    private fun ensureRunEnabled(threshold: Int = 20) {
        try {
            if (!Movement.running() && Movement.energyLevel() >= threshold) {
                Movement.running(true)
            }
        } catch (_: Throwable) { }
    }

    private fun manhattan(a: Tile, b: Tile): Int =
        abs(a.x - b.x) + abs(a.y - b.y) + if (a.floor == b.floor) 0 else 1000

    private fun nextHopTarget(current: Int, target: Int): Int =
        if (current < target) current + 1 else current - 1
}