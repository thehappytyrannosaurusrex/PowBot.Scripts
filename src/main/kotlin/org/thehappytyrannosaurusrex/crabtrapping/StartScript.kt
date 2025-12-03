package org.thehappytyrannosaurusrex.crabtrapping

import org.powbot.mobile.service.ScriptUploader

//Pixel 9 46251FDAS0082H or BlueStacks 127.0.0.1:5555

fun main() {
    ScriptUploader().uploadAndStart(
        "Crab Trapping - Red Loop",
        "main",
        "127.0.0.1:5555",
        true,
        false
    )
}