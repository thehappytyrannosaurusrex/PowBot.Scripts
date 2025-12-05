package org.thehappytyrannosaurusrex.api.utils

import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.api.rt4.walking.model.Skill

/**
 * Reusable Paint builder utility for creating consistent script overlays.
 *
 * Usage:
 * ```kotlin
 * val paint = PaintUtil.create {
 *     title("My Script")
 *     trackSkill(Skill.Slayer)
 *     stat("Stage") { currentStage.name }
 *     stat("Items Collected") { itemCount.toString() }
 *     stat("Runtime") { formatRuntime() }
 * }
 * addPaint(paint)
 * ```
 */
object PaintUtil {

    // Default positioning
    private const val DEFAULT_X = 40
    private const val DEFAULT_Y = 45

    /**
     * Create a Paint using the DSL builder
     */
    fun create(block: PaintDsl.() -> Unit): Paint {
        val dsl = PaintDsl()
        dsl.block()
        return dsl.build()
    }

    /**
     * Simple paint with just a tracked skill and custom stats
     */
    fun simple(
        trackedSkill: Skill? = null,
        stats: Map<String, () -> String> = emptyMap(),
        x: Int = DEFAULT_X,
        y: Int = DEFAULT_Y
    ): Paint {
        return create {
            position(x, y)
            trackedSkill?.let { trackSkill(it) }
            stats.forEach { (label, provider) -> stat(label, provider) }
        }
    }

    /**
     * DSL class for building paints fluently
     */
    class PaintDsl {
        private var x: Int = DEFAULT_X
        private var y: Int = DEFAULT_Y
        private var trackedSkill: Skill? = null
        private val stats = mutableListOf<Pair<String, () -> String>>()

        /**
         * Set paint position
         */
        fun position(x: Int, y: Int) {
            this.x = x
            this.y = y
        }

        /**
         * Track XP gains for a skill
         */
        fun trackSkill(skill: Skill) {
            this.trackedSkill = skill
        }

        /**
         * Add a dynamic stat line
         */
        fun stat(label: String, valueProvider: () -> String) {
            stats.add(label to valueProvider)
        }

        /**
         * Add a static stat line
         */
        fun staticStat(label: String, value: String) {
            stats.add(label to { value })
        }

        internal fun build(): Paint {
            val builder = PaintBuilder.newBuilder()
                .x(x)
                .y(y)

            trackedSkill?.let { builder.trackSkill(it) }

            stats.forEach { (label, provider) ->
                builder.addString(label, provider)
            }

            return builder.build()
        }
    }

    // -------------------------------------------------------------------------
    // Formatting Helpers
    // -------------------------------------------------------------------------

    /**
     * Format milliseconds as HH:MM:SS
     */
    fun formatRuntime(startTimeMs: Long): String {
        val elapsed = System.currentTimeMillis() - startTimeMs
        val seconds = (elapsed / 1000) % 60
        val minutes = (elapsed / (1000 * 60)) % 60
        val hours = elapsed / (1000 * 60 * 60)
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    /**
     * Format a number with commas (e.g., 1,234,567)
     */
    fun formatNumber(number: Long): String {
        return String.format("%,d", number)
    }

    /**
     * Format a number with commas (Int version)
     */
    fun formatNumber(number: Int): String = formatNumber(number.toLong())

    /**
     * Format rate per hour
     */
    fun formatPerHour(count: Int, startTimeMs: Long): String {
        val elapsed = System.currentTimeMillis() - startTimeMs
        if (elapsed < 1000) return "0/hr"
        val perHour = (count * 3600000.0 / elapsed).toLong()
        return "${formatNumber(perHour)}/hr"
    }
}