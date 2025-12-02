package org.thehappytyrannosaurusrex.varrockmuseum.config

import org.powbot.api.script.tree.TreeScript

data class Config(
    val lampSkill: LampSkill,
    /**
 * Normalised item names (lowercased, trimmed) that the user wants to KEEP
 */
    val keepItemNames: Set<String>
)

/**
 * Build a typed config from script options.
 */
fun buildConfig(script: TreeScript): Config {
    val selectedLamp = try {
        script.getOption<String>(Options.Keys.LAMP_SKILL)
    } catch (e: Exception) {
        null
    }

    val lampSkill = LampSkill.fromDisplayNameOrDefault(selectedLamp, LampSkill.SLAYER)

    val keepRaw = try {
        script.getOption<String>(Options.Keys.KEEP_ITEMS)
    } catch (e: Exception) {
        null
    }

    val keepNames = keepRaw
        ?.split(',')
        ?.map { it.trim() }
        ?.filter { it.isNotEmpty() }
        ?.map { it.lowercase() }
        ?.toSet()
        ?: emptySet()

    return Config(
        lampSkill = lampSkill,
        keepItemNames = keepNames
    )
}
