package org.thehappytyrannosaurusrex.crabtrapping.utils

import org.powbot.api.Condition
import org.powbot.api.Random
import org.powbot.api.Tile
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.Players
import org.thehappytyrannosaurusrex.api.utils.Logger
import org.thehappytyrannosaurusrex.crabtrapping.data.CrabLocation
import org.thehappytyrannosaurusrex.crabtrapping.data.TrapSpot
import org.thehappytyrannosaurusrex.crabtrapping.data.TrapState
import org.thehappytyrannosaurusrex.crabtrapping.data.preferredStandingTile

object CrabUtils {

    private const val TAG = "CrabUtils"

    // --- Movement ---

    fun walkToTile(target: Tile, attempts: Int = 10): Boolean {
        val me = Players.local()
        if (!me.valid()) return false
        if (me.tile() == target) return true

        Movement.step(target)
        return Condition.wait({
            val p = Players.local()
            if (!p.valid()) return@wait false
            if (p.tile() == target) {
                true
            } else {
                if (!p.inMotion()) Movement.step(target)
                false
            }
        }, 250, attempts)
    }

    fun isAtTile(tile: Tile): Boolean {
        val me = Players.local()
        return me.valid() && me.tile() == tile
    }

    fun ensureAtTile(tile: Tile, attempts: Int = 10): Boolean {
        return if (isAtTile(tile)) true else walkToTile(tile, attempts)
    }

    // --- Trap Finding ---

    fun findTrapAt(spot: TrapSpot): GameObject? =
        Objects.stream().at(spot.tile).id(spot.objectId).first().takeIf { it.valid() }

    fun findTrapByLabel(location: CrabLocation, label: String): TrapSpot? =
        location.trapSpots.firstOrNull { it.label == label }

    fun getTrapState(spot: TrapSpot): TrapState? {
        val trap = findTrapAt(spot) ?: return null
        return TrapState.fromObjectName(trap.name)
    }

    fun isTrapInState(spot: TrapSpot, state: TrapState): Boolean =
        getTrapState(spot) == state

    // --- Trap Interactions ---

    fun clickTrap(spot: TrapSpot): Boolean {
        val trap = findTrapAt(spot) ?: return false
        return trap.click()
    }

    fun clickTrapWithAction(spot: TrapSpot, action: String): Boolean {
        val trap = findTrapAt(spot) ?: return false
        return trap.interact(action)
    }

    fun baitEmptyTrap(spot: TrapSpot, logPrefix: String = "BAIT"): Boolean {
        val trap = findTrapAt(spot)
        if (trap == null) {
            Logger.warn(TAG, logPrefix, "${spot.label} - no trap found at ${spot.tile}")
            return false
        }

        val state = TrapState.fromObjectName(trap.name)
        if (state != TrapState.EMPTY) {
            Logger.info(TAG, logPrefix, "${spot.label} is $state, not EMPTY")
            return false
        }

        val clicked = trap.click()
        if (clicked) {
            Logger.info(TAG, logPrefix, "${spot.label} - clicked EMPTY trap")
        }
        return clicked
    }

    // --- Normalisation ---

    data class NormalizeResult(
        val done: Boolean,
        val shouldStop: Boolean = false,
        val message: String? = null
    )

    fun normalizeTrap(
        spot: TrapSpot,
        standingTile: Tile,
        clickTimes: MutableMap<TrapSpot, Long>,
        clickCooldownMs: Long = 3_000L
    ): NormalizeResult {
        val me = Players.local()
        if (!me.valid()) return NormalizeResult(false)

        if (me.tile() != standingTile) {
            walkToTile(standingTile)
            return NormalizeResult(false)
        }

        val trap = findTrapAt(spot)
        if (trap == null) {
            Logger.warn(TAG, "NORMALIZE", "${spot.label} - no trap found at ${spot.tile}")
            return NormalizeResult(false)
        }

        val state = TrapState.fromObjectName(trap.name)

        return when (state) {
            TrapState.EMPTY -> {
                clickTimes.remove(spot)
                Logger.info(TAG, "NORMALIZE", "${spot.label} - already EMPTY")
                NormalizeResult(done = true)
            }

            TrapState.NOT_BUILT -> {
                Logger.error(TAG, "NORMALIZE", "${spot.label} is NOT_BUILT. Build manually first.")
                NormalizeResult(done = true, shouldStop = true, message = "Trap not built")
            }

            TrapState.BAITED -> {
                clickTimes.remove(spot)
                Logger.info(TAG, "NORMALIZE", "${spot.label} - BAITED, waiting for CAUGHT")
                NormalizeResult(false)
            }

            TrapState.CAUGHT -> {
                val now = System.currentTimeMillis()
                val lastClick = clickTimes[spot] ?: 0L

                if (lastClick == 0L || now - lastClick > clickCooldownMs) {
                    Logger.info(TAG, "NORMALIZE", "${spot.label} - CAUGHT, clicking to empty")
                    trap.click()
                    standingTile.matrix().click()
                    clickTimes[spot] = now
                }
                NormalizeResult(false)
            }

            null -> {
                Logger.warn(TAG, "NORMALIZE", "${spot.label} - unknown state for '${trap.name}'")
                NormalizeResult(false)
            }
        }
    }

    // --- Rebait Logic ---

    class RebaitState {
        var currentTrap: TrapSpot? = null
        var startTime: Long = 0L
        var clicksDone: Int = 0
        var startedClicking: Boolean = false
        var nextClickTime: Long = 0L

        fun reset() {
            currentTrap = null
            startTime = 0L
            clicksDone = 0
            startedClicking = false
            nextClickTime = 0L
        }

        fun startFor(spot: TrapSpot) {
            reset()
            currentTrap = spot
            Logger.info(TAG, "REBAIT", "Preparing to rebait ${spot.label}")
        }
    }

    data class RebaitResult(
        val complete: Boolean,
        val timedOut: Boolean = false
    )

    fun handleRebait(
        spot: TrapSpot,
        state: RebaitState,
        maxTimeMs: Long = 10_000L,
        clickIntervalMin: Int = 400,
        clickIntervalMax: Int = 600
    ): RebaitResult {
        val me = Players.local()
        if (!me.valid()) return RebaitResult(false)

        val standingTile = spot.preferredStandingTile()
        if (me.tile() != standingTile) {
            walkToTile(standingTile)
            return RebaitResult(false)
        }

        if (state.currentTrap != spot) {
            state.startFor(spot)
        }

        val trap = findTrapAt(spot)
        if (trap == null) {
            Logger.warn(TAG, "REBAIT", "${spot.label} - no trap found")
            return RebaitResult(false)
        }

        val trapState = TrapState.fromObjectName(trap.name)
        if (trapState == null) {
            Logger.warn(TAG, "REBAIT", "${spot.label} - unknown state '${trap.name}'")
            return RebaitResult(false)
        }

        val now = System.currentTimeMillis()

        // Start clicking when trap is CAUGHT
        if (!state.startedClicking) {
            if (trapState != TrapState.CAUGHT) return RebaitResult(false)
            state.startedClicking = true
            state.startTime = now
            Logger.info(TAG, "REBAIT", "${spot.label} - CAUGHT, starting click spam")
        }

        // Success: trap is now BAITED
        if (trapState == TrapState.BAITED) {
            Logger.info(TAG, "REBAIT", "${spot.label} - re-bait complete (${state.clicksDone} clicks)")
            state.reset()
            return RebaitResult(complete = true)
        }

        // Timeout check
        if (now - state.startTime > maxTimeMs) {
            Logger.warn(TAG, "REBAIT", "${spot.label} - timeout after ${state.clicksDone} clicks")
            state.reset()
            return RebaitResult(complete = true, timedOut = true)
        }

        // Click spam
        if (now >= state.nextClickTime) {
            trap.click()
            state.clicksDone++
            state.nextClickTime = now + Random.nextInt(clickIntervalMin, clickIntervalMax)
        }

        return RebaitResult(false)
    }

    // --- Utility ---

    fun trapsAllowedByHunter(hunterLevel: Int): Int = when {
        hunterLevel >= 80 -> 5
        hunterLevel >= 60 -> 4
        hunterLevel >= 40 -> 3
        hunterLevel >= 20 -> 2
        else -> 1
    }

    fun getTrapsForLevel(location: CrabLocation, hunterLevel: Int): List<TrapSpot> {
        val maxTraps = trapsAllowedByHunter(hunterLevel)
        return location.trapSpots.take(maxTraps)
    }

    fun getStandingTileForTrap(location: CrabLocation, trapLabel: String): Tile? {
        val spot = findTrapByLabel(location, trapLabel) ?: return null
        return spot.preferredStandingTile()
    }
}