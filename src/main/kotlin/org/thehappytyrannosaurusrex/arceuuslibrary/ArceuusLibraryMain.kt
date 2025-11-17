package org.thehappytyrannosaurusrex.arceuuslibrary

import org.powbot.mobile.service.ScriptUploader

fun main() {
    ScriptUploader().uploadAndStart(
        "Arceuus Library",
        "main",
        "127.0.0.1:5555",
        true,
        false
    )
}
