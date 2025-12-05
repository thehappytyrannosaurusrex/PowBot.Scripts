package org.thehappytyrannosaurusrex.crabtrapping.data

import org.powbot.api.Area
import org.powbot.api.Tile
import org.thehappytyrannosaurusrex.api.data.PlayerStats

fun currentSailingLevelFromPlayerStats(): Int? =
    PlayerStats.sailingLvl().text().toIntOrNull()

enum class CrabType(val itemName: String) {
    RED("Red crab"),
    BLUE("Blue crab"),
    RAINBOW("Rainbow crab")
}

enum class Bait(val itemName: String) {
    FISH_OFFCUTS("Fish offcuts"),
    FINE_FISH_OFFCUTS("Fine fish offcuts")
}

enum class TrapState(val objectName: String, val primaryAction: String?) {
    EMPTY("Crab trap (empty)", "Bait"),
    CAUGHT("Crab trap (full)", "Empty"),
    BAITED("Crab trap (baited)", "Empty"),
    NOT_BUILT("Hole", "Build-trap");

    companion object {
        fun fromObjectName(name: String): TrapState? =
            values().firstOrNull { it.objectName.equals(name, ignoreCase = true) }
    }
}

data class TrapSpot(
    val label: String,
    val objectId: Int,
    val tile: Tile,
    val standingTile: Tile? = null
)

fun TrapSpot.preferredStandingTile(): Tile = standingTile ?: tile

data class TrapSetup(
    val minHunterLevel: Int,
    val maxHunterLevel: Int?,
    val maxTrapCount: Int,
    val trapsInOrder: List<TrapSpot>
)

data class CrabLocation(
    val crabType: CrabType,
    val requiredBait: Bait,
    val area: Area,
    val trapSpots: List<TrapSpot>,
    val setups: List<TrapSetup>
)

object CrabData {

    // Red crab standing tiles
    val redCrabStart = Tile(1764, 3061, 0)
    val redCrabTrap4Standing = Tile(1763, 3059, 0)
    val redCrabTrap3Standing = Tile(1764, 3057, 0)
    val redCrabTrap2Standing = Tile(1766, 3056, 0)
    val redCrabTrap1Standing = Tile(1768, 3056, 0)

    // Red crab location data
    val redCrab = CrabLocation(
        crabType = CrabType.RED,
        requiredBait = Bait.FISH_OFFCUTS,
        area = Area(Tile(1760, 3054, 0), Tile(1772, 3066, 0)),
        trapSpots = listOf(
            TrapSpot("Trap 5", 56295, Tile(1765, 3061, 0), redCrabStart),
            TrapSpot("Trap 4", 56295, Tile(1763, 3060, 0), redCrabTrap4Standing),
            TrapSpot("Trap 3", 56295, Tile(1764, 3058, 0), redCrabTrap3Standing),
            TrapSpot("Trap 2", 56295, Tile(1766, 3057, 0), redCrabTrap2Standing),
            TrapSpot("Trap 1", 56295, Tile(1768, 3057, 0), redCrabTrap1Standing)
        ),
        setups = listOf(
            TrapSetup(1, 19, 1, listOf()),
            TrapSetup(20, 39, 2, listOf()),
            TrapSetup(40, 59, 3, listOf()),
            TrapSetup(60, 79, 4, listOf()),
            TrapSetup(80, null, 5, listOf())
        )
    )

    fun trapsAllowedByHunter(hunterLevel: Int): Int = when {
        hunterLevel >= 80 -> 5
        hunterLevel >= 60 -> 4
        hunterLevel >= 40 -> 3
        hunterLevel >= 20 -> 2
        else -> 1
    }
}