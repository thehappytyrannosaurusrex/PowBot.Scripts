package org.thehappytyrannosaurusrex.api.chat

import org.powbot.api.Condition
import org.powbot.api.rt4.Chat

// Simple helpers for navigating NPC dialogues
object DialogueUtils {

    // Click through "click to continue" dialogue up to maxAttempts times
    fun spamClickContinue(maxAttempts: Int = 10, delayMs: Int = 800) {
        var attempts = 0
        while (Chat.canContinue() && attempts < maxAttempts) {
            Chat.clickContinue()
            Condition.sleep(delayMs)
            attempts++
        }
    }
}