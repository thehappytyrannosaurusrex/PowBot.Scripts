package org.thehappytyrannosaurusrex.api.chat

import org.powbot.api.Condition
import org.powbot.api.rt4.Chat

object DialogueHandler {

    // Clicks through "click to continue" dialogue up to maxAttempts times
    fun spamClickContinue(maxAttempts: Int = 10, delayMs: Int = 800) {
        var attempts = 0
        while (Chat.canContinue() && attempts < maxAttempts) {
            Chat.clickContinue()
            Condition.sleep(delayMs)
            attempts++
        }
    }

    // Handles full dialogue with optional responses
    fun handleDialogue(maxAttempts: Int = 10, delayMs: Int = 600) {
        var attempts = 0
        while ((Chat.chatting() || Chat.canContinue()) && attempts < maxAttempts) {
            if (Chat.canContinue()) {
                Chat.clickContinue()
            }
            Condition.sleep(delayMs)
            attempts++
        }
    }

    // Waits for chat to open
    fun waitForChat(timeoutMs: Int = 3000): Boolean {
        val attempts = timeoutMs / 200
        return Condition.wait({ Chat.chatting() }, 200, attempts)
    }

    // Closes any open chat
    fun closeChat() {
        if (Chat.chatting()) {
            Chat.clickContinue()
            Condition.wait({ !Chat.chatting() }, 200, 10)
        }
    }
}