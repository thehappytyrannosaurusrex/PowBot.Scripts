package org.thehappytyrannosaurusrex.varrockmuseum.config

import org.powbot.api.script.tree.TreeScript

data class Config(
    val lampSkill: LampSkill,
    val keepItemNames: Set<String>
)

fun buildConfig(script: TreeScript): Config {
    val selectedLamp = try { script.getOption<String>(Options.Keys.LAMP_SKILL) } catch (_: Exception) { null }
    val lampSkill = LampSkill.fromDisplayNameOrDefault(selectedLamp, LampSkill.SLAYER)

    val keepRaw = try { script.getOption<String>(Options.Keys.KEEP_ITEMS) } catch (_: Exception) { null }
    val keepNames = keepRaw
        ?.split(',')
        ?.map { it.trim().lowercase() }
        ?.filter { it.isNotEmpty() }
        ?.toSet()
        ?: emptySet()

    return Config(lampSkill = lampSkill, keepItemNames = keepNames)
}