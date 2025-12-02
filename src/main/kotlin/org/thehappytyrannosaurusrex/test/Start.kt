package org.thehappytyrannosaurusrex.test
import org.powbot.mobile.service.ScriptUploader

fun main() {
    ScriptUploader().uploadAndStart(
        "API Test Script",
        "main",
        "127.0.0.1:5555",
        true,
        false
    )
}