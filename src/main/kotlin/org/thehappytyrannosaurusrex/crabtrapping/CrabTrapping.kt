package org.thehappytyrannosaurusrex.crabtrapping

import com.google.common.eventbus.Subscribe
import org.powbot.api.Condition
import org.powbot.api.Random
import org.powbot.api.Tile
import org.powbot.api.event.MessageEvent
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.Players
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.AbstractScript
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptManifest
import org.thehappytyrannosaurusrex.api.utils.Logger
import org.thehappytyrannosaurusrex.crabtrapping.data.*

@ScriptManifest(
    name        = "Crab Trapping - Red Loop",
    description = "Red crab trapping loop: normalise traps, bait 5–1, then empty & re-bait in a cycle.",
    author      = "thehappytyrannosaurusrex",
    version     = "0.5.0",
    category    = ScriptCategory.Other
)
class CrabTrapping : AbstractScript() {

    companion object {
        private const val SCRIPT_NAME = "Crab Trapper"
        private const val MAX_REBAIT_TIME_MS = 10_000L
    }

    private enum class Phase {
        INIT,

        // Startup normalisation: get all traps we will use to EMPTY
        NORMALIZE_TRAP5,
        NORMALIZE_TRAP4,
        NORMALIZE_TRAP3,
        NORMALIZE_TRAP2,
        NORMALIZE_TRAP1,

        // Initial baiting
        GO_TO_START,
        BAIT_TRAP5,
        GO_TO_TRAP4_STAND,
        BAIT_TRAP4,
        GO_TO_TRAP3_STAND,
        BAIT_TRAP3,
        GO_TO_TRAP2_STAND,
        BAIT_TRAP2,
        GO_TO_TRAP1_STAND,
        BAIT_TRAP1,
        RETURN_TO_START,

        // Main loop
        WAIT_FOR_CATCH,
        REBAIT_TRAP5,
        GO_TO_TRAP4_STAND_REBAIT,
        REBAIT_TRAP4,
        GO_TO_TRAP3_STAND_REBAIT,
        REBAIT_TRAP3,
        GO_TO_TRAP2_STAND_REBAIT,
        REBAIT_TRAP2,
        GO_TO_TRAP1_STAND_REBAIT,
        REBAIT_TRAP1,

        DONE
    }

    private var phase = Phase.INIT
    private var trapsToUse = 1

    // Location / trap data
    private val redLocation = CrabData.redCrab

    private val START_TILE: Tile = CrabData.redCrabStart
    private val TRAP4_STANDING: Tile = CrabData.redCrabTrap4Standing
    private val TRAP3_STANDING: Tile = CrabData.redCrabTrap3Standing
    private val TRAP2_STANDING: Tile = CrabData.redCrabTrap2Standing
    private val TRAP1_STANDING: Tile = CrabData.redCrabTrap1Standing

    private val trap5Spot: TrapSpot = redLocation.trapSpots.first { it.label == "Trap 5" }
    private val trap4Spot: TrapSpot = redLocation.trapSpots.first { it.label == "Trap 4" }
    private val trap3Spot: TrapSpot = redLocation.trapSpots.first { it.label == "Trap 3" }
    private val trap2Spot: TrapSpot = redLocation.trapSpots.first { it.label == "Trap 2" }
    private val trap1Spot: TrapSpot = redLocation.trapSpots.first { it.label == "Trap 1" }

    // Chat message state (we mostly use state now, but keep these around)
    @Volatile
    private var lastCrabMessage: CrabMessages.Type? = null
    private var waitingForBaitMessage = false // only for initial bait phase

    // Rebait state
    private var currentRebaitTrap: TrapSpot? = null
    private var rebaitStartTime: Long = 0L
    private var rebaitClicksDone: Int = 0
    private var rebaitStartedClicking: Boolean = false
    private var nextClickTime: Long = 0L

    // Startup normalisation click timing
    private val normalizeClickTimes = mutableMapOf<TrapSpot, Long>()

    override fun onStart() {
        val hunterLevel = Skill.Hunter.realLevel()
        val allowedTraps = trapsAllowedByHunter(hunterLevel)

        // We support up to 5 traps (5,4,3,2,1)
        trapsToUse = minOf(allowedTraps, redLocation.trapSpots.size)

        Logger.info(
            SCRIPT_NAME,
            "START",
            "Hunter=$hunterLevel, trapsAllowed=$allowedTraps, trapsToUse=$trapsToUse"
        )

        phase = if (trapsToUse >= 1) Phase.NORMALIZE_TRAP5 else Phase.DONE
    }

    override fun onStop() {
        Logger.info(SCRIPT_NAME, "STOP", "Script stopped at phase=$phase")
    }

    // -------------------------------------------------------------------------
    // Game messages -> CrabMessages.Type
    // -------------------------------------------------------------------------

    @Subscribe
    fun onServerMessage(event: MessageEvent) {
        val msg = event.message ?: return
        val type = CrabMessages.classify(msg) ?: return

        // We no longer use TRAP_CATCH for sequencing, but we still care about
        // TRAP_BAITED_CLICK / TRAP_WAIT / TRAP_BAITED_AUTO for debugging/paint.
        when (type) {
            CrabMessages.Type.TRAP_BAITED_CLICK,
            CrabMessages.Type.TRAP_WAIT,
            CrabMessages.Type.TRAP_BAITED_AUTO -> {
                lastCrabMessage = type
            }
            else -> {
                // ignore for now
            }
        }
    }

    // -------------------------------------------------------------------------
    // Main loop
    // -------------------------------------------------------------------------

    override fun poll() {
        try {
            when (phase) {
                Phase.INIT -> phase =
                    if (trapsToUse >= 1) Phase.NORMALIZE_TRAP5 else Phase.DONE

                // --- STARTUP NORMALISATION ---------------------------------

                Phase.NORMALIZE_TRAP5 -> {
                    if (normalizeTrap(trap5Spot, START_TILE)) {
                        Logger.info(SCRIPT_NAME, "NORMALIZE", "Trap 5 normalised to EMPTY")
                        phase =
                            if (trapsToUse >= 2) Phase.NORMALIZE_TRAP4
                            else Phase.GO_TO_START
                    }
                }

                Phase.NORMALIZE_TRAP4 -> {
                    if (trapsToUse < 2) {
                        phase = Phase.GO_TO_START
                        return
                    }
                    if (normalizeTrap(trap4Spot, TRAP4_STANDING)) {
                        Logger.info(SCRIPT_NAME, "NORMALIZE", "Trap 4 normalised to EMPTY")
                        phase =
                            if (trapsToUse >= 3) Phase.NORMALIZE_TRAP3
                            else Phase.GO_TO_START
                    }
                }

                Phase.NORMALIZE_TRAP3 -> {
                    if (trapsToUse < 3) {
                        phase = Phase.GO_TO_START
                        return
                    }
                    if (normalizeTrap(trap3Spot, TRAP3_STANDING)) {
                        Logger.info(SCRIPT_NAME, "NORMALIZE", "Trap 3 normalised to EMPTY")
                        phase =
                            if (trapsToUse >= 4) Phase.NORMALIZE_TRAP2
                            else Phase.GO_TO_START
                    }
                }

                Phase.NORMALIZE_TRAP2 -> {
                    if (trapsToUse < 4) {
                        phase = Phase.GO_TO_START
                        return
                    }
                    if (normalizeTrap(trap2Spot, TRAP2_STANDING)) {
                        Logger.info(SCRIPT_NAME, "NORMALIZE", "Trap 2 normalised to EMPTY")
                        phase =
                            if (trapsToUse >= 5) Phase.NORMALIZE_TRAP1
                            else Phase.GO_TO_START
                    }
                }

                Phase.NORMALIZE_TRAP1 -> {
                    if (trapsToUse < 5) {
                        phase = Phase.GO_TO_START
                        return
                    }
                    if (normalizeTrap(trap1Spot, TRAP1_STANDING)) {
                        Logger.info(SCRIPT_NAME, "NORMALIZE", "Trap 1 normalised to EMPTY")
                        phase = Phase.GO_TO_START
                    }
                }

                // --- Initial baiting ----------------------------------------

                Phase.GO_TO_START -> {
                    if (walkAndWait(START_TILE)) {
                        Logger.info(SCRIPT_NAME, "STATE", "At redCrabStart, baiting Trap 5")
                        resetMessageState()
                        phase = Phase.BAIT_TRAP5
                    }
                }

                Phase.BAIT_TRAP5 -> handleInitialBaitPhase(
                    spot = trap5Spot,
                    standingTile = START_TILE
                ) {
                    phase =
                        if (trapsToUse >= 2) Phase.GO_TO_TRAP4_STAND
                        else Phase.RETURN_TO_START
                }

                Phase.GO_TO_TRAP4_STAND -> {
                    if (trapsToUse < 2) {
                        phase = Phase.RETURN_TO_START
                        return
                    }
                    if (walkAndWait(TRAP4_STANDING)) {
                        Logger.info(SCRIPT_NAME, "STATE", "At Trap 4 standing tile, baiting")
                        resetMessageState()
                        phase = Phase.BAIT_TRAP4
                    }
                }

                Phase.BAIT_TRAP4 -> handleInitialBaitPhase(
                    spot = trap4Spot,
                    standingTile = TRAP4_STANDING
                ) {
                    phase =
                        if (trapsToUse >= 3) Phase.GO_TO_TRAP3_STAND
                        else Phase.RETURN_TO_START
                }

                Phase.GO_TO_TRAP3_STAND -> {
                    if (trapsToUse < 3) {
                        phase = Phase.RETURN_TO_START
                        return
                    }
                    if (walkAndWait(TRAP3_STANDING)) {
                        Logger.info(SCRIPT_NAME, "STATE", "At Trap 3 standing tile, baiting")
                        resetMessageState()
                        phase = Phase.BAIT_TRAP3
                    }
                }

                Phase.BAIT_TRAP3 -> handleInitialBaitPhase(
                    spot = trap3Spot,
                    standingTile = TRAP3_STANDING
                ) {
                    phase =
                        if (trapsToUse >= 4) Phase.GO_TO_TRAP2_STAND
                        else Phase.RETURN_TO_START
                }

                Phase.GO_TO_TRAP2_STAND -> {
                    if (trapsToUse < 4) {
                        phase = Phase.RETURN_TO_START
                        return
                    }
                    if (walkAndWait(TRAP2_STANDING)) {
                        Logger.info(SCRIPT_NAME, "STATE", "At Trap 2 standing tile, baiting")
                        resetMessageState()
                        phase = Phase.BAIT_TRAP2
                    }
                }

                Phase.BAIT_TRAP2 -> handleInitialBaitPhase(
                    spot = trap2Spot,
                    standingTile = TRAP2_STANDING
                ) {
                    phase =
                        if (trapsToUse >= 5) Phase.GO_TO_TRAP1_STAND
                        else Phase.RETURN_TO_START
                }

                Phase.GO_TO_TRAP1_STAND -> {
                    if (trapsToUse < 5) {
                        phase = Phase.RETURN_TO_START
                        return
                    }
                    if (walkAndWait(TRAP1_STANDING)) {
                        Logger.info(SCRIPT_NAME, "STATE", "At Trap 1 standing tile, baiting")
                        resetMessageState()
                        phase = Phase.BAIT_TRAP1
                    }
                }

                Phase.BAIT_TRAP1 -> handleInitialBaitPhase(
                    spot = trap1Spot,
                    standingTile = TRAP1_STANDING
                ) {
                    phase = Phase.RETURN_TO_START
                }

                Phase.RETURN_TO_START -> {
                    if (walkAndWait(START_TILE)) {
                        Logger.info(SCRIPT_NAME, "STATE", "Back at redCrabStart, entering main loop")
                        resetMessageState()
                        phase = Phase.WAIT_FOR_CATCH
                    }
                }

                // --- Main loop: wait for Trap 5 CAUGHT, then re-bait sequence

                Phase.WAIT_FOR_CATCH -> {
                    val me = Players.local()
                    if (me.valid() && me.tile() != START_TILE) {
                        walkAndWait(START_TILE)
                        return
                    }

                    val trap5State = getTrapState(trap5Spot)
                    if (trap5State == TrapState.CAUGHT) {
                        Logger.info(
                            SCRIPT_NAME,
                            "LOOP",
                            "Trap 5 is CAUGHT – starting rebait sequence 5 -> 4 -> 3 -> 2 -> 1"
                        )

                        resetRebaitState()
                        startRebaitFor(trap5Spot)
                        phase = Phase.REBAIT_TRAP5
                    }
                }

                Phase.REBAIT_TRAP5 -> handleRebaitPhase(trap5Spot) {
                    phase =
                        if (trapsToUse >= 2) Phase.GO_TO_TRAP4_STAND_REBAIT
                        else Phase.RETURN_TO_START
                }

                Phase.GO_TO_TRAP4_STAND_REBAIT -> {
                    if (trapsToUse < 2) {
                        phase = Phase.RETURN_TO_START
                        return
                    }
                    if (walkAndWait(TRAP4_STANDING)) {
                        resetRebaitState()
                        startRebaitFor(trap4Spot)
                        phase = Phase.REBAIT_TRAP4
                    }
                }

                Phase.REBAIT_TRAP4 -> handleRebaitPhase(trap4Spot) {
                    phase =
                        if (trapsToUse >= 3) Phase.GO_TO_TRAP3_STAND_REBAIT
                        else Phase.RETURN_TO_START
                }

                Phase.GO_TO_TRAP3_STAND_REBAIT -> {
                    if (trapsToUse < 3) {
                        phase = Phase.RETURN_TO_START
                        return
                    }
                    if (walkAndWait(TRAP3_STANDING)) {
                        resetRebaitState()
                        startRebaitFor(trap3Spot)
                        phase = Phase.REBAIT_TRAP3
                    }
                }

                Phase.REBAIT_TRAP3 -> handleRebaitPhase(trap3Spot) {
                    phase =
                        if (trapsToUse >= 4) Phase.GO_TO_TRAP2_STAND_REBAIT
                        else Phase.RETURN_TO_START
                }

                Phase.GO_TO_TRAP2_STAND_REBAIT -> {
                    if (trapsToUse < 4) {
                        phase = Phase.RETURN_TO_START
                        return
                    }
                    if (walkAndWait(TRAP2_STANDING)) {
                        resetRebaitState()
                        startRebaitFor(trap2Spot)
                        phase = Phase.REBAIT_TRAP2
                    }
                }

                Phase.REBAIT_TRAP2 -> handleRebaitPhase(trap2Spot) {
                    phase =
                        if (trapsToUse >= 5) Phase.GO_TO_TRAP1_STAND_REBAIT
                        else Phase.RETURN_TO_START
                }

                Phase.GO_TO_TRAP1_STAND_REBAIT -> {
                    if (trapsToUse < 5) {
                        phase = Phase.RETURN_TO_START
                        return
                    }
                    if (walkAndWait(TRAP1_STANDING)) {
                        resetRebaitState()
                        startRebaitFor(trap1Spot)
                        phase = Phase.REBAIT_TRAP1
                    }
                }

                Phase.REBAIT_TRAP1 -> handleRebaitPhase(trap1Spot) {
                    phase = Phase.RETURN_TO_START
                }

                Phase.DONE -> {
                    // no-op
                }
            }
        } catch (t: Throwable) {
            Logger.error(SCRIPT_NAME, "ERROR", "Exception in poll(): ${t.message}", t)
            controller.stop()
        }
    }

    // -------------------------------------------------------------------------
    // Movement helper (step, exact tile)
    // -------------------------------------------------------------------------

    private fun walkAndWait(target: Tile, attempts: Int = 20): Boolean {
        val me = Players.local()
        if (!me.valid()) return false

        if (me.tile() == target) return true

        if (!Movement.step(target)) {
            Logger.warn(SCRIPT_NAME, "WALK", "Movement.step($target) returned false")
            return false
        }

        val arrived = Condition.wait(
            {
                val p = Players.local()
                if (!p.valid()) return@wait false

                if (p.tile() == target) {
                    true
                } else {
                    if (!p.inMotion()) {
                        Movement.step(target)
                    }
                    false
                }
            },
            250,
            attempts
        )

        if (!arrived) {
            Logger.warn(SCRIPT_NAME, "WALK", "Timed out stepping to $target (never landed exactly on tile)")
        }
        return arrived
    }

    private fun trapsAllowedByHunter(hunterLevel: Int): Int =
        when {
            hunterLevel >= 80 -> 5
            hunterLevel >= 60 -> 4
            hunterLevel >= 40 -> 3
            hunterLevel >= 20 -> 2
            else -> 1
        }

    private fun resetMessageState() {
        lastCrabMessage = null
        waitingForBaitMessage = false
    }

    // -------------------------------------------------------------------------
    // STARTUP NORMALISATION (same logic as before, just shared for 5–1)
    // -------------------------------------------------------------------------

    private fun normalizeTrap(spot: TrapSpot, standingTile: Tile): Boolean {
        val me = Players.local()
        if (!me.valid()) return false

        if (me.tile() != standingTile) {
            walkAndWait(standingTile)
            return false
        }

        val trap = findTrapAt(spot)
        if (trap == null || !trap.valid()) {
            Logger.warn(SCRIPT_NAME, "NORMALIZE", "${spot.label} – no trap found at ${spot.tile}")
            return false
        }

        val state = TrapState.fromObjectName(trap.name)
        Logger.info(
            SCRIPT_NAME,
            "NORMALIZE",
            "${spot.label} – tile=${trap.tile()} obj='${trap.name}' state=$state"
        )

        return when (state) {
            TrapState.EMPTY -> {
                normalizeClickTimes.remove(spot)
                Logger.info(SCRIPT_NAME, "NORMALIZE", "${spot.label} – already EMPTY, cleared")
                true
            }

            TrapState.NOT_BUILT -> {
                Logger.error(
                    SCRIPT_NAME,
                    "NORMALIZE",
                    "${spot.label} – trap is NOT_BUILT. Build your crab traps once manually, then restart the script."
                )
                controller.stop()
                true
            }

            TrapState.BAITED -> {
                normalizeClickTimes.remove(spot)
                Logger.info(
                    SCRIPT_NAME,
                    "NORMALIZE",
                    "${spot.label} – Baited at startup, waiting for it to become CAUGHT."
                )
                false
            }

            TrapState.CAUGHT -> {
                val now = System.currentTimeMillis()
                val lastClick = normalizeClickTimes[spot] ?: 0L

                if (lastClick == 0L || now - lastClick > 3_000L) {
                    Logger.info(
                        SCRIPT_NAME,
                        "NORMALIZE",
                        "${spot.label} – CAUGHT, performing 'click trap + click tile' once."
                    )

                    val clickedTrap = trap.click()
                    if (!clickedTrap) {
                        Logger.warn(
                            SCRIPT_NAME,
                            "NORMALIZE",
                            "${spot.label} – click() on CAUGHT trap failed at ${trap.tile()}"
                        )
                        return false
                    }

                    // Click our standing tile once so we don't auto-rebait
                    standingTile.matrix().click()

                    normalizeClickTimes[spot] = now
                }

                false
            }

            null -> {
                Logger.warn(
                    SCRIPT_NAME,
                    "NORMALIZE",
                    "${spot.label} – could not map object '${trap.name}' to a TrapState"
                )
                false
            }
        }
    }

    // -------------------------------------------------------------------------
    // Initial baiting: click once on EMPTY, wait for TRAP_BAITED_CLICK
    // -------------------------------------------------------------------------

    private fun handleInitialBaitPhase(
        spot: TrapSpot,
        standingTile: Tile,
        onFinished: () -> Unit
    ) {
        val me = Players.local()
        if (!me.valid()) return

        if (me.tile() != standingTile) {
            walkAndWait(standingTile)
            return
        }

        if (lastCrabMessage == CrabMessages.Type.TRAP_BAITED_CLICK) {
            Logger.info(SCRIPT_NAME, "BAIT", "${spot.label} baited (TRAP_BAITED_CLICK)")
            resetMessageState()
            onFinished()
            return
        }

        if (!waitingForBaitMessage) {
            if (baitEmptyTrapAt(spot)) {
                waitingForBaitMessage = true
            }
        }
    }

    private fun baitEmptyTrapAt(spot: TrapSpot): Boolean {
        val trap = findTrapAt(spot)
        if (trap == null || !trap.valid()) {
            Logger.warn(SCRIPT_NAME, "BAIT", "${spot.label} – no trap object found at ${spot.tile}.")
            return false
        }

        val state = TrapState.fromObjectName(trap.name)
        if (state != TrapState.EMPTY) {
            Logger.info(
                SCRIPT_NAME,
                "BAIT",
                "${spot.label} – trap at ${trap.tile()} is $state, not EMPTY, skipping."
            )
            return false
        }

        val clicked = trap.click()
        if (!clicked) {
            Logger.warn(SCRIPT_NAME, "BAIT", "${spot.label} – click() on EMPTY trap failed at ${trap.tile()}.")
        } else {
            Logger.info(SCRIPT_NAME, "BAIT", "${spot.label} – clicked EMPTY trap at ${trap.tile()}")
        }
        return clicked
    }

    // -------------------------------------------------------------------------
    // Re-bait logic: CAUGHT -> click spam (~2/s) until BAITED
    // -------------------------------------------------------------------------

    private fun startRebaitFor(spot: TrapSpot) {
        currentRebaitTrap = spot
        rebaitStartTime = 0L
        rebaitClicksDone = 0
        rebaitStartedClicking = false
        nextClickTime = 0L
        lastCrabMessage = null

        Logger.info(SCRIPT_NAME, "REBAIT", "Preparing to rebait ${spot.label}")
    }

    private fun resetRebaitState() {
        currentRebaitTrap = null
        rebaitStartTime = 0L
        rebaitClicksDone = 0
        rebaitStartedClicking = false
        nextClickTime = 0L
        lastCrabMessage = null
    }

    private fun handleRebaitPhase(
        spot: TrapSpot,
        onFinished: () -> Unit
    ) {
        val me = Players.local()
        if (!me.valid()) return

        val standingTile = spot.preferredStandingTile()
        if (me.tile() != standingTile) {
            walkAndWait(standingTile)
            return
        }

        if (currentRebaitTrap != spot) {
            startRebaitFor(spot)
        }

        val trap = findTrapAt(spot)
        if (trap == null || !trap.valid()) {
            Logger.warn(SCRIPT_NAME, "REBAIT", "${spot.label} – no trap found at ${spot.tile} during re-bait.")
            return
        }

        val state = TrapState.fromObjectName(trap.name)
        if (state == null) {
            Logger.warn(SCRIPT_NAME, "REBAIT", "${spot.label} – unknown trap state for object '${trap.name}'")
            return
        }

        val now = System.currentTimeMillis()

        if (!rebaitStartedClicking) {
            if (state != TrapState.CAUGHT) {
                return
            }
            rebaitStartedClicking = true
            rebaitStartTime = now
            Logger.info(SCRIPT_NAME, "REBAIT", "${spot.label} – state=CAUGHT, starting click spam")
        }

        if (state == TrapState.BAITED) {
            Logger.info(
                SCRIPT_NAME,
                "REBAIT",
                "${spot.label} – re-bait complete. state=$state, clicks=$rebaitClicksDone"
            )
            resetRebaitState()
            onFinished()
            return
        }

        if (rebaitStartTime > 0L && now - rebaitStartTime > MAX_REBAIT_TIME_MS) {
            Logger.warn(
                SCRIPT_NAME,
                "REBAIT",
                "${spot.label} – taking longer than ${MAX_REBAIT_TIME_MS}ms (state=$state), still trying."
            )
        }

        if (now >= nextClickTime) {
            val clicked = trap.click()
            if (clicked) {
                rebaitClicksDone++
                Logger.info(
                    SCRIPT_NAME,
                    "REBAIT",
                    "${spot.label} – re-bait click #$rebaitClicksDone (state before click=$state)"
                )
            } else {
                Logger.warn(
                    SCRIPT_NAME,
                    "REBAIT",
                    "${spot.label} – failed to click trap during re-bait at ${trap.tile()}"
                )
            }
            nextClickTime = now + Random.nextInt(500, 1000)
        }
    }

    // -------------------------------------------------------------------------
    // Object / state helpers
    // -------------------------------------------------------------------------

    private fun findTrapAt(spot: TrapSpot) =
        Objects.stream()
            .within(8.0)
            .name(
                TrapState.EMPTY.objectName,
                TrapState.BAITED.objectName,
                TrapState.CAUGHT.objectName,
                TrapState.NOT_BUILT.objectName
            )
            .filter { it.tile() == spot.tile }
            .firstOrNull()

    private fun getTrapState(spot: TrapSpot): TrapState? {
        val trap = findTrapAt(spot) ?: return null
        return TrapState.fromObjectName(trap.name)
    }


}
