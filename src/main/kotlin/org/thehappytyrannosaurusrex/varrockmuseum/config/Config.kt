package org.thehappytyrannosaurusrex.varrockmuseum.config

import org.powbot.api.script.AbstractScript

data class Config(
    val lampSkill: LampSkill,
    val keepItemNames: Set<String>,
    val stopAtLevel: Int,
    val oneTickClick: Boolean
)

fun buildConfig(script: AbstractScript): Config {
    val selectedLamp = try { script.getOption<String>(Options.Keys.LAMP_SKILL) } catch (_: Exception) { null }
    val lampSkill = LampSkill.fromDisplayNameOrDefault(selectedLamp, LampSkill.SLAYER)

    val keepRaw = try { script.getOption<String>(Options.Keys.KEEP_ITEMS) } catch (_: Exception) { null }
    val keepNames = keepRaw
        ?.split(',')
        ?.map { it.trim().lowercase() }
        ?.filter { it.isNotEmpty() }
        ?.toSet()
        ?: emptySet()

    val stopAtLevel = try {
        script.getOption<Int>(Options.Keys.STOP_AT_LEVEL).coerceIn(0, 99)
    } catch (_: Exception) { 0 }

    val oneTickClick = try {
        script.getOption<Boolean>(Options.Keys.ONE_TICK_CLICK)
    } catch (_: Exception) { false }

    return Config(
        lampSkill = lampSkill,
        keepItemNames = keepNames,
        stopAtLevel = stopAtLevel,
        oneTickClick = oneTickClick
    )
}