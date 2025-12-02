package org.thehappytyrannosaurusrex.varrockmuseum.tree

import org.powbot.api.script.tree.SimpleBranch
import org.powbot.api.script.tree.TreeComponent
import org.thehappytyrannosaurusrex.varrockmuseum.VarrockMuseumCleaner

/**
 * Stage-based branch wiring for the Varrock Museum Cleaner behaviour tree.
 */
object MuseumBranches {

    fun buildRoot(script: VarrockMuseumCleaner): TreeComponent<*> =
        buildPreBankStage(script)

    // ---------------------- Stage: PRE_BANK_CLEAN ----------------------

    private fun buildPreBankStage(script: VarrockMuseumCleaner): TreeComponent<VarrockMuseumCleaner> =
        SimpleBranch(
            script,
            "Stage: PRE_BANK_CLEAN?",
            successComponent = leafPreBank(script),
            failedComponent = buildTravelStage(script),
            validator = { script.stage == VarrockMuseumCleaner.Stage.PRE_BANK_CLEAN }
        )

    // ---------------------- Stage: TRAVEL_TO_MUSEUM --------------------

    private fun buildTravelStage(script: VarrockMuseumCleaner): TreeComponent<VarrockMuseumCleaner> =
        SimpleBranch(
            script,
            "Stage: TRAVEL_TO_MUSEUM?",
            successComponent = leafTravel(script),
            failedComponent = buildEnsureKitStage(script),
            validator = { script.stage == VarrockMuseumCleaner.Stage.TRAVEL_TO_MUSEUM }
        )

    // ---------------------- Stage: ENSURE_KIT --------------------------

    private fun buildEnsureKitStage(script: VarrockMuseumCleaner): TreeComponent<VarrockMuseumCleaner> =
        SimpleBranch(
            script,
            "Stage: ENSURE_KIT?",
            successComponent = leafEnsureKit(script),
            failedComponent = buildInsideNormaliseStage(script),
            validator = { script.stage == VarrockMuseumCleaner.Stage.ENSURE_KIT }
        )

    // ---------------------- Stage: INSIDE_NORMALISE --------------------

    private fun buildInsideNormaliseStage(script: VarrockMuseumCleaner): TreeComponent<VarrockMuseumCleaner> =
        SimpleBranch(
            script,
            "Stage: INSIDE_NORMALISE?",
            successComponent = leafInsideNormalise(script),
            failedComponent = buildMainLoopStage(script),
            validator = { script.stage == VarrockMuseumCleaner.Stage.INSIDE_NORMALISE }
        )

    // ---------------------- Stage: MAIN_LOOP ---------------------------

    private fun buildMainLoopStage(script: VarrockMuseumCleaner): TreeComponent<VarrockMuseumCleaner> =
        SimpleBranch(
            script,
            "Stage: MAIN_LOOP?",
            successComponent = leafMainLoop(script),
            failedComponent = leafIdle(script),
            validator = { script.stage == VarrockMuseumCleaner.Stage.MAIN_LOOP }
        )
}
