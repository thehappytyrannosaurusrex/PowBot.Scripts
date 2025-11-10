package org.thehappytyrannosaurusrex.arceuuslibrary.utils

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow

/**
 * Timer and XP tracking helper.
 * Provides runtime duration, XP/hour, and time-to-level calculations.
 */
class Timer(var time: Number = 0L) {

    companion object {
        fun formatTime(time: Long, addMillis: Boolean = false): String {
            if (time <= 0L)
                return "--:--:--"
            val totalSec = time / 1000
            val totalMin = totalSec / 60
            val totalHour = totalMin / 60
            val totalDay = totalHour / 24
            val second = (totalSec % 60).toInt()
            val minute = (totalMin % 60).toInt()
            val hour = (totalHour % 24).toInt()
            val day = totalDay.toInt()

            val t = StringBuilder()
            if (day > 0) t.append("$day:")
            if (hour > 0) {
                if (hour < 10) t.append("0")
                t.append("$hour:")
            }
            if (minute < 10) t.append("0")
            t.append("$minute:")
            if (second < 10) t.append("0")
            t.append(second)

            if (addMillis && time < 1000) t.append(":$time")
            return t.toString()
        }
    }

    private var start = System.currentTimeMillis()
    private var end = 0L

    init {
        if (time.toLong() > 0) {
            end = start + time.toLong()
        }
    }

    fun getElapsedTime(): Long = System.currentTimeMillis() - start
    fun getElapsedString(): String = formatTime(getElapsedTime())
    fun getRemainder(): Long = end - System.currentTimeMillis()
    fun getRemainderString(): String = formatTime(getRemainder())
    fun isFinished(): Boolean = System.currentTimeMillis() > end

    fun reset(newTime: Number = time) {
        time = newTime
        start = System.currentTimeMillis()
        end = start + time.toLong()
    }

    fun stop() {
        end = System.currentTimeMillis()
    }

    fun getXPForLevel(level: Int): Int {
        var points = 0
        var output = 0
        for (lvl in 1..level) {
            points += floor(lvl + 300.0 * 2.0.pow(lvl / 7.0)).toInt()
            if (lvl >= level) return output
            output = floor(points / 4.0).toInt()
        }
        return 0
    }

    fun getTimeToNextLevel(xpPerHour: Int, level: Int, xp: Long): String {
        val elapsed = if (xpPerHour < 1) 0 else ((getXPForLevel(level + 1) - xp) * 3600000.0 / xpPerHour).toLong()
        return formatTime(elapsed)
    }

    fun getPerHour(gained: Int): Int {
        if (gained == 0) return 0
        return ceil(gained * 3600000.0 / (System.currentTimeMillis() - start)).toInt()
    }

    override fun toString(): String = getElapsedString()
}