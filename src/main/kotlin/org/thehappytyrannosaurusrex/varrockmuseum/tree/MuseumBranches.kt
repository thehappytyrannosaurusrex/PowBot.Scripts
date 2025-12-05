package org.thehappytyrannosaurusrex.varrockmuseum.tree

import org.powbot.api.script.tree.SimpleBranch
import org.powbot.api.script.tree.TreeComponent
import org.thehappytyrannosaurusrex.varrockmuseum.VarrockMuseumCleaner
import org.thehappytyrannosaurusrex.varrockmuseum.VarrockMuseumCleaner.Stage

object MuseumBranches {

    fun buildRoot(script: VarrockMuseumCleaner): TreeComponent<*> =
        buildPreBankStage(script)

    private fun buildPreBankStage(script: VarrockMuseumCleaner): TreeComponent<VarrockMuseumCleaner> =
        SimpleBranch(
            script,
            "Stage: PRE_BANK_CLEAN?",
            successComponent = leafPreBank(script),
            failedComponent = buildTravelStage(script),
            validator = { script.stage == Stage.PRE_BANK_CLEAN }
        )

    private fun buildTravelStage(script: VarrockMuseumCleaner): TreeComponent<VarrockMuseumCleaner> =
        SimpleBranch(
            script,
            "Stage: TRAVEL_TO_MUSEUM?",
            successComponent = leafTravel(script),
            failedComponent = buildEnsureKitStage(script),
            validator = { script.stage == Stage.TRAVEL_TO_MUSEUM }
        )

    private fun buildEnsureKitStage(script: VarrockMuseumCleaner): TreeComponent<VarrockMuseumCleaner> =
        SimpleBranch(
            script,
            "Stage: ENSURE_KIT?",
            successComponent = leafEnsureKit(script),
            failedComponent = buildInsideNormaliseStage(script),
            validator = { script.stage == Stage.ENSURE_KIT }
        )

    private fun buildInsideNormaliseStage(script: VarrockMuseumCleaner): TreeComponent<VarrockMuseumCleaner> =
        SimpleBranch(
            script,
            "Stage: INSIDE_NORMALISE?",
            successComponent = leafInsideNormalise(script),
            failedComponent = buildMainLoopStage(script),
            validator = { script.stage == Stage.INSIDE_NORMALISE }
        )

    private fun buildMainLoopStage(script: VarrockMuseumCleaner): TreeComponent<VarrockMuseumCleaner> =
        SimpleBranch(
            script,
            "Stage: MAIN_LOOP?",
            successComponent = leafMainLoop(script),
            failedComponent = leafIdle(script),
            validator = { script.stage == Stage.MAIN_LOOP }
        )
}