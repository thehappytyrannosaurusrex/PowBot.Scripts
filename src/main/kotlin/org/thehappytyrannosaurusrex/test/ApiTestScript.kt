package org.thehappytyrannosaurusrex.test

import org.powbot.api.Condition
import org.powbot.api.rt4.Bank
import org.powbot.api.script.AbstractScript
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptManifest
import org.thehappytyrannosaurusrex.api.inventory.RunePouch
import org.thehappytyrannosaurusrex.api.utils.Logger

@ScriptManifest(
    name        = "API Test Script",
    description = "Tests Logger and RunePouch utilities",
    author      = "thehappytyrannosaurusrex",
    version     = "1.0.0",
    category    = ScriptCategory.Magic
)
class ApiTestScript : AbstractScript() {

    companion object {
        private const val SCRIPT_NAME = "ApiTest"

        // Basic reanimation-style runes (just for testing)
        private const val BODY_RUNE  = 559
        private const val NATURE_RUNE = 561
        private const val SOUL_RUNE   = 566
    }

    private enum class Stage {
        STARTUP,
        OPEN_BANK,
        TEST_EMPTY,
        TEST_FILL,
        DONE
    }

    private var stage = Stage.STARTUP

    override fun onStart() {
        Logger.info(SCRIPT_NAME, "STARTUP", "API test script starting")
    }

    override fun onStop() {
        Logger.info(SCRIPT_NAME, "SHUTDOWN", "API test script stopped")
    }

    override fun poll() {
        when (stage) {
            Stage.STARTUP   -> handleStartup()
            Stage.OPEN_BANK -> handleOpenBank()
            Stage.TEST_EMPTY -> handleTestEmpty()
            Stage.TEST_FILL -> handleTestFill()
            Stage.DONE      -> handleDone()
        }
    }

    // -------------------- Stage handlers --------------------

    private fun handleStartup() {
        Logger.debug(SCRIPT_NAME, "STARTUP", "Moving to OPEN_BANK stage")
        stage = Stage.OPEN_BANK
    }

    private fun handleOpenBank() {
        if (Bank.opened()) {
            Logger.info(SCRIPT_NAME, "BANK", "Bank already open, proceeding to TEST_EMPTY")
            stage = Stage.TEST_EMPTY
            return
        }

        Logger.debug(SCRIPT_NAME, "BANK", "Attempting to open nearest bank")
        if (Bank.open()) {
            Condition.wait({ Bank.opened() }, 200, 20)
        }

        if (Bank.opened()) {
            Logger.info(SCRIPT_NAME, "BANK", "Bank successfully opened")
            stage = Stage.TEST_EMPTY
        } else {
            Logger.warn(SCRIPT_NAME, "BANK", "Unable to open bank yet, will retry")
        }
    }

    private fun handleTestEmpty() {
        Logger.info(SCRIPT_NAME, "RUNE_POUCH", "Ensuring pouch is in inventory and emptying at bank")

        if (!RunePouch.ensurePouchInInventory()) {
            Logger.warn(
                SCRIPT_NAME,
                "RUNE_POUCH",
                "No rune pouch found in inventory or bank; aborting tests"
            )
            stage = Stage.DONE
            return
        }

        val emptied = RunePouch.emptyAtBank()
        if (emptied) {
            Logger.info(SCRIPT_NAME, "RUNE_POUCH", "Rune pouch successfully emptied at bank")
        } else {
            Logger.warn(
                SCRIPT_NAME,
                "RUNE_POUCH",
                "Did not see \"The pouch is empty.\" message; continuing anyway"
            )
        }

        stage = Stage.TEST_FILL
    }

    private fun handleTestFill() {
        Logger.info(
            SCRIPT_NAME,
            "RUNE_POUCH",
            "Testing clearAndFillFromBankWithResult with Body + Nature + Soul runes"
        )

        val runeIds = listOf(BODY_RUNE, NATURE_RUNE, SOUL_RUNE)
        val result = RunePouch.clearAndFillFromBankWithResult(runeIds)

        if (result == null) {
            Logger.error(
                SCRIPT_NAME,
                "RUNE_POUCH",
                "clearAndFillFromBankWithResult returned null (too many rune types? bank closed? no pouch?)"
            )
            stage = Stage.DONE
            return
        }

        Logger.info(
            SCRIPT_NAME,
            "RUNE_POUCH",
            "Loaded runes into ${result.pouchType.name} pouch:"
        )

        result.runeAmounts.forEach { (id, amount) ->
            Logger.info(
                SCRIPT_NAME,
                "RUNE_POUCH",
                " - runeId=$id -> $amount runes"
            )
        }

        Logger.info(
            SCRIPT_NAME,
            "RUNE_POUCH",
            "Total loaded runes: ${result.totalRunes}"
        )

        stage = Stage.DONE
    }

    private fun handleDone() {
        Logger.info(SCRIPT_NAME, "DONE", "All tests complete, stopping script")
        controller.stop()
    }
}
