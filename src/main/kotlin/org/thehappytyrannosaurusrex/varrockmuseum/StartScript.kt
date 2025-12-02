package org.thehappytyrannosaurusrex.varrockmuseum

import org.powbot.mobile.service.ScriptUploader

/**
 * Local launcher for the Varrock Museum Cleaner script.
 */
fun main() {
    ScriptUploader().uploadAndStart(
        "Varrock Museum Cleaner",
        "main",
        "127.0.0.1:5555",
        true,
        false
    )
}
