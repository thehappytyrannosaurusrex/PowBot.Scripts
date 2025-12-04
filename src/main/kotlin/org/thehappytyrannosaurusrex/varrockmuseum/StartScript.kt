package org.thehappytyrannosaurusrex.varrockmuseum

import org.powbot.mobile.service.ScriptUploader

//Device name is Pixel 9 46251FDAS0082H or BlueStacks 127.0.0.1:5555

fun main() {
    ScriptUploader().uploadAndStart(
        "Varrock Museum Cleaner",
        "main",
        "46251FDAS0082H",
        true,
        false
    )
}
