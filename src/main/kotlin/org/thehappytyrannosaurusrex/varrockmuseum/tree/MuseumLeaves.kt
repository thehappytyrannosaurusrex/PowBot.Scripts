package org.thehappytyrannosaurusrex.varrockmuseum.tree

import org.powbot.api.Condition
import org.powbot.api.script.tree.SimpleLeaf
import org.thehappytyrannosaurusrex.varrockmuseum.VarrockMuseumCleaner

/**
 * Leaf factories for the Varrock Museum Cleaner behaviour tree.
 */

// ---------------------- Stage leaves ----------------------

fun leafPreBank(script: VarrockMuseumCleaner): SimpleLeaf<VarrockMuseumCleaner> =
    SimpleLeaf(script, "Startup: pre-bank cleanup") {
        script.handlePreBankCleanup()
    }

fun leafTravel(script: VarrockMuseumCleaner): SimpleLeaf<VarrockMuseumCleaner> =
    SimpleLeaf(script, "Travel to museum") {
        script.handleTravelToMuseum()
    }

fun leafEnsureKit(script: VarrockMuseumCleaner): SimpleLeaf<VarrockMuseumCleaner> =
    SimpleLeaf(script, "Ensure cleaning kit") {
        script.handleEnsureKit()
    }

fun leafInsideNormalise(script: VarrockMuseumCleaner): SimpleLeaf<VarrockMuseumCleaner> =
    SimpleLeaf(script, "Normalise inventory inside museum") {
        script.handleInsideNormalise()
    }

fun leafMainLoop(script: VarrockMuseumCleaner): SimpleLeaf<VarrockMuseumCleaner> =
    SimpleLeaf(script, "Main cleaning loop") {
        script.handleMainLoop()
    }

// ---------------------- Fallback / idle ----------------------

fun leafIdle(script: VarrockMuseumCleaner): SimpleLeaf<VarrockMuseumCleaner> =
    SimpleLeaf(script, "Idle") {
        // Shouldn't be hit often – only if stage isn't one of the known values.
        Condition.sleep(300)
    }
