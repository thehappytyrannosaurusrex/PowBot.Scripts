package org.thehappytyrannosaurusrex.crabtrapping

import com.google.common.eventbus.Subscribe
import org.powbot.api.Tile
import org.powbot.api.event.MessageEvent
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.AbstractScript
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptManifest
import org.thehappytyrannosaurusrex.api.utils.Logger
import org.thehappytyrannosaurusrex.crabtrapping.data.*
import org.thehappytyrannosaurusrex.crabtrapping.utils.CrabUtils

@ScriptManifest(
    name = "Crab Trapping - Red Loop",
    description = "Red crab trapping: normalise traps, bait 5-1, then empty & re-bait in a cycle.",
    author = "thehappytyrannosaurusrex",
    version = "0.6.0",
    category = ScriptCategory.Other
)
class CrabTrapping : AbstractScript() {

    companion object {
        private const val SCRIPT_NAME = "Crab Trapper"
    }

    private enum class Phase {
        INIT,
        NORMALIZE_TRAP5, NORMALIZE_TRAP4, NORMALIZE_TRAP3, NORMALIZE_TRAP2, NORMALIZE_TRAP1,
        GO_TO_START,
        BAIT_TRAP5, GO_TO_TRAP4_STAND, BAIT_TRAP4, GO_TO_TRAP3_STAND, BAIT_TRAP3,
        GO_TO_TRAP2_STAND, BAIT_TRAP2, GO_TO_TRAP1_STAND, BAIT_TRAP1,
        RETURN_TO_START,
        WAIT_FOR_CATCH,
        REBAIT_TRAP5, GO_TO_TRAP4_STAND_REBAIT, REBAIT_TRAP4, GO_TO_TRAP3_STAND_REBAIT, REBAIT_TRAP3,
        GO_TO_TRAP2_STAND_REBAIT, REBAIT_TRAP2, GO_TO_TRAP1_STAND_REBAIT, REBAIT_TRAP1,
        DONE
    }

    private var phase = Phase.INIT
    private var trapsToUse = 1

    // Location data - easily swappable for blue/rainbow crabs
    private val location = CrabData.redCrab
    private val startTile = CrabData.redCrabStart
    private val standingTiles = mapOf(
        "Trap 5" to CrabData.redCrabStart,
        "Trap 4" to CrabData.redCrabTrap4Standing,
        "Trap 3" to CrabData.redCrabTrap3Standing,
        "Trap 2" to CrabData.redCrabTrap2Standing,
        "Trap 1" to CrabData.redCrabTrap1Standing
    )

    // Trap spots
    private val trap5 by lazy { location.trapSpots.first { it.label == "Trap 5" } }
    private val trap4 by lazy { location.trapSpots.first { it.label == "Trap 4" } }
    private val trap3 by lazy { location.trapSpots.first { it.label == "Trap 3" } }
    private val trap2 by lazy { location.trapSpots.first { it.label == "Trap 2" } }
    private val trap1 by lazy { location.trapSpots.first { it.label == "Trap 1" } }

    // Message state
    @Volatile private var lastCrabMessage: CrabMessages.Type? = null
    private var waitingForBaitMessage = false

    // Normalisation and rebait state
    private val normalizeClickTimes = mutableMapOf<TrapSpot, Long>()
    private val rebaitState = CrabUtils.RebaitState()

    private fun log(tag: String, message: String) = Logger.info(SCRIPT_NAME, tag, message)

    override fun onStart() {
        val hunterLevel = Skill.Hunter.realLevel()
        trapsToUse = minOf(CrabUtils.trapsAllowedByHunter(hunterLevel), location.trapSpots.size)

        log("START", "Hunter=$hunterLevel, trapsToUse=$trapsToUse, location=${location.crabType}")
        phase = if (trapsToUse >= 1) Phase.NORMALIZE_TRAP5 else Phase.DONE
    }

    override fun onStop() {
        log("STOP", "Script stopped at phase=$phase")
    }

    @Subscribe
    fun onServerMessage(event: MessageEvent) {
        val msg = event.message ?: return
        val type = CrabMessages.classify(msg) ?: return

        when (type) {
            CrabMessages.Type.TRAP_BAITED_CLICK,
            CrabMessages.Type.TRAP_WAIT,
            CrabMessages.Type.TRAP_BAITED_AUTO -> lastCrabMessage = type
            else -> {}
        }
    }

    override fun poll() {
        try {
            when (phase) {
                Phase.INIT -> phase = if (trapsToUse >= 1) Phase.NORMALIZE_TRAP5 else Phase.DONE

                // Normalisation
                Phase.NORMALIZE_TRAP5 -> handleNormalize(trap5, standingTiles["Trap 5"]!!, 2, Phase.NORMALIZE_TRAP4)
                Phase.NORMALIZE_TRAP4 -> handleNormalize(trap4, standingTiles["Trap 4"]!!, 3, Phase.NORMALIZE_TRAP3)
                Phase.NORMALIZE_TRAP3 -> handleNormalize(trap3, standingTiles["Trap 3"]!!, 4, Phase.NORMALIZE_TRAP2)
                Phase.NORMALIZE_TRAP2 -> handleNormalize(trap2, standingTiles["Trap 2"]!!, 5, Phase.NORMALIZE_TRAP1)
                Phase.NORMALIZE_TRAP1 -> handleNormalize(trap1, standingTiles["Trap 1"]!!, 6, Phase.GO_TO_START)

                // Initial baiting
                Phase.GO_TO_START -> handleGoTo(startTile, Phase.BAIT_TRAP5)
                Phase.BAIT_TRAP5 -> handleBait(trap5, standingTiles["Trap 5"]!!, 2, Phase.GO_TO_TRAP4_STAND)
                Phase.GO_TO_TRAP4_STAND -> handleGoTo(standingTiles["Trap 4"]!!, Phase.BAIT_TRAP4, 2)
                Phase.BAIT_TRAP4 -> handleBait(trap4, standingTiles["Trap 4"]!!, 3, Phase.GO_TO_TRAP3_STAND)
                Phase.GO_TO_TRAP3_STAND -> handleGoTo(standingTiles["Trap 3"]!!, Phase.BAIT_TRAP3, 3)
                Phase.BAIT_TRAP3 -> handleBait(trap3, standingTiles["Trap 3"]!!, 4, Phase.GO_TO_TRAP2_STAND)
                Phase.GO_TO_TRAP2_STAND -> handleGoTo(standingTiles["Trap 2"]!!, Phase.BAIT_TRAP2, 4)
                Phase.BAIT_TRAP2 -> handleBait(trap2, standingTiles["Trap 2"]!!, 5, Phase.GO_TO_TRAP1_STAND)
                Phase.GO_TO_TRAP1_STAND -> handleGoTo(standingTiles["Trap 1"]!!, Phase.BAIT_TRAP1, 5)
                Phase.BAIT_TRAP1 -> handleBait(trap1, standingTiles["Trap 1"]!!, 6, Phase.RETURN_TO_START)

                Phase.RETURN_TO_START -> handleReturnToStart()
                Phase.WAIT_FOR_CATCH -> handleWaitForCatch()

                // Rebait
                Phase.REBAIT_TRAP5 -> handleRebaitPhase(trap5, 2, Phase.GO_TO_TRAP4_STAND_REBAIT)
                Phase.GO_TO_TRAP4_STAND_REBAIT -> handleGoToRebait(standingTiles["Trap 4"]!!, trap4, 2, Phase.REBAIT_TRAP4)
                Phase.REBAIT_TRAP4 -> handleRebaitPhase(trap4, 3, Phase.GO_TO_TRAP3_STAND_REBAIT)
                Phase.GO_TO_TRAP3_STAND_REBAIT -> handleGoToRebait(standingTiles["Trap 3"]!!, trap3, 3, Phase.REBAIT_TRAP3)
                Phase.REBAIT_TRAP3 -> handleRebaitPhase(trap3, 4, Phase.GO_TO_TRAP2_STAND_REBAIT)
                Phase.GO_TO_TRAP2_STAND_REBAIT -> handleGoToRebait(standingTiles["Trap 2"]!!, trap2, 4, Phase.REBAIT_TRAP2)
                Phase.REBAIT_TRAP2 -> handleRebaitPhase(trap2, 5, Phase.GO_TO_TRAP1_STAND_REBAIT)
                Phase.GO_TO_TRAP1_STAND_REBAIT -> handleGoToRebait(standingTiles["Trap 1"]!!, trap1, 5, Phase.REBAIT_TRAP1)
                Phase.REBAIT_TRAP1 -> handleRebaitPhase(trap1, 6, Phase.RETURN_TO_START)

                Phase.DONE -> {}
            }
        } catch (t: Throwable) {
            Logger.error(SCRIPT_NAME, "ERROR", "Exception: ${t.message}")
        }
    }

    // --- Phase Handlers ---

    private fun handleNormalize(spot: TrapSpot, standing: Tile, minTraps: Int, nextPhase: Phase) {
        if (trapsToUse < minTraps) { phase = Phase.GO_TO_START; return }

        val result = CrabUtils.normalizeTrap(spot, standing, normalizeClickTimes)
        if (result.shouldStop) {
            controller.stop()
            return
        }
        if (result.done) {
            log("NORMALIZE", "${spot.label} done")
            phase = nextPhase
        }
    }

    private fun handleGoTo(tile: Tile, nextPhase: Phase, minTraps: Int = 1) {
        if (trapsToUse < minTraps) { phase = Phase.RETURN_TO_START; return }
        if (CrabUtils.walkToTile(tile)) {
            resetMessageState()
            phase = nextPhase
        }
    }

    private fun handleBait(spot: TrapSpot, standing: Tile, minTraps: Int, nextPhase: Phase) {
        if (trapsToUse < minTraps) { phase = Phase.RETURN_TO_START; return }

        if (!CrabUtils.isAtTile(standing)) {
            CrabUtils.walkToTile(standing)
            return
        }

        if (lastCrabMessage == CrabMessages.Type.TRAP_BAITED_CLICK) {
            log("BAIT", "${spot.label} baited")
            resetMessageState()
            phase = nextPhase
            return
        }

        if (!waitingForBaitMessage) {
            if (CrabUtils.baitEmptyTrap(spot)) {
                waitingForBaitMessage = true
            }
        }
    }

    private fun handleReturnToStart() {
        if (CrabUtils.walkToTile(startTile)) {
            log("STATE", "Back at start, entering main loop")
            resetMessageState()
            phase = Phase.WAIT_FOR_CATCH
        }
    }

    private fun handleWaitForCatch() {
        if (!CrabUtils.isAtTile(startTile)) {
            CrabUtils.walkToTile(startTile)
            return
        }

        if (CrabUtils.isTrapInState(trap5, TrapState.CAUGHT)) {
            log("LOOP", "Trap 5 CAUGHT - starting rebait sequence")
            rebaitState.reset()
            rebaitState.startFor(trap5)
            phase = Phase.REBAIT_TRAP5
        }
    }

    private fun handleGoToRebait(standing: Tile, spot: TrapSpot, minTraps: Int, nextPhase: Phase) {
        if (trapsToUse < minTraps) { phase = Phase.RETURN_TO_START; return }
        if (CrabUtils.walkToTile(standing)) {
            rebaitState.reset()
            rebaitState.startFor(spot)
            phase = nextPhase
        }
    }

    private fun handleRebaitPhase(spot: TrapSpot, minTraps: Int, nextPhase: Phase) {
        if (trapsToUse < minTraps) { phase = Phase.RETURN_TO_START; return }

        val result = CrabUtils.handleRebait(spot, rebaitState)
        if (result.complete) {
            phase = nextPhase
        }
    }

    private fun resetMessageState() {
        lastCrabMessage = null
        waitingForBaitMessage = false
    }
}