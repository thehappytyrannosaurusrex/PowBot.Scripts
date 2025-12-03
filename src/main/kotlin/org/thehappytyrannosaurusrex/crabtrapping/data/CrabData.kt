package org.thehappytyrannosaurusrex.crabtrapping.data

import org.powbot.api.Area
import org.powbot.api.Tile
import org.powbot.api.rt4.Constants
import org.powbot.api.rt4.Skills
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Item
import org.thehappytyrannosaurusrex.api.data.PlayerStats

// ----------------------------------------
// Sailing helper via PlayerStats
// ----------------------------------------

/**
 * Reads Sailing level using the PlayerStats API.
 *
 * Expects PlayerStats.SAILING_LVL.text() to return the level as a String,
 * e.g. "64". If parsing fails, returns null.
 */
fun currentSailingLevelFromPlayerStats(): Int? =
    PlayerStats.SAILING_LVL.text().toIntOrNull()

// ----------------------------------------
// Basic enums / constants
// ----------------------------------------

enum class CrabType(val itemName: String) {
    RED("Red crab"),
    BLUE("Blue crab"),
    RAINBOW("Rainbow crab"),
}

enum class Bait(val itemName: String) {
    FISH_OFFCUTS("Fish offcuts"),
    FINE_FISH_OFFCUTS("Fine fish offcuts"),
}

enum class TrapState(
    val objectName: String,
    val primaryAction: String?,
) {
    EMPTY(
        objectName = "Crab trap (empty)",
        primaryAction = "Bait",
    ),
    CAUGHT(
        objectName = "Crab trap (full)",
        primaryAction = "Empty",
    ),
    BAITED(
        objectName = "Crab trap (baited)",
        primaryAction = "Empty",
    ),
    NOT_BUILT(
        objectName = "Hole",
        primaryAction = "Build-trap",
    );

    companion object {
        fun fromObjectName(name: String): TrapState? =
            values().firstOrNull { it.objectName.equals(name, ignoreCase = true) }
    }
}


// ----------------------------------------
// Trap and setup data
// ----------------------------------------

data class TrapSpot(
    val label: String,      // "Trap 1", "Trap 2", etc.
    val objectId: Int,
    val tile: Tile,         // trap object tile
    val standingTile: Tile? = null, // tile to stand on BEFORE interacting
)

/**
 * Convenience: get the tile we *prefer* to stand on for this trap.
 * Falls back to the trap tile itself if no explicit standing tile.
 */
fun TrapSpot.preferredStandingTile(): Tile = standingTile ?: tile

/**
 * A particular trap configuration for a Hunter level bracket.
 */
data class TrapSetup(
    val minHunterLevel: Int,
    val maxHunterLevel: Int?,          // null = no upper bound
    val maxTrapCount: Int,
    val trapsInOrder: List<TrapSpot>,  // order you interact with traps
)

/**
 * All static data for a crab location (Red/Blue/Rainbow).
 */
data class CrabInformation(
    val rawName: CrabType,
    val rawId: Int?,
    val pasteName: String,
    val pasteId: Int?,
    val area: Area?,
    val hunterLevelRequired: Int,
    val sailingLevelRequired: Int?,
    val bait: Bait,
    val trapSpots: List<TrapSpot>,
    val setups: List<TrapSetup>,
    val bankLocation: Tile?,
) {
    fun bestSetupForHunterLevel(hunterLevel: Int): TrapSetup? =
        setups
            .filter {
                hunterLevel >= it.minHunterLevel &&
                        (it.maxHunterLevel == null || hunterLevel <= it.maxHunterLevel)
            }
            .maxByOrNull { it.maxTrapCount }

    fun bestSetupForCurrentHunter(): TrapSetup? =
        bestSetupForHunterLevel(Skill.Hunter.realLevel())

    fun meetsHunterRequirementNow(): Boolean =
        Skill.Hunter.realLevel() >= hunterLevelRequired

    fun meetsSailingRequirement(currentSailingLevel: Int?): Boolean =
        sailingLevelRequired?.let { required ->
            (currentSailingLevel ?: 0) >= required
        } ?: true

    fun meetsSailingRequirementFromPlayerStats(): Boolean =
        meetsSailingRequirement(currentSailingLevelFromPlayerStats())
}

// ----------------------------------------
// Global crab data
// ----------------------------------------

object CrabData {

    // -----------------------------
    // Hunter-level -> trap count
    // -----------------------------

    fun trapsAllowedByHunterLevel(hunterLevel: Int): Int = when {
        hunterLevel >= 80 -> 5
        hunterLevel >= 60 -> 4
        hunterLevel >= 40 -> 3
        hunterLevel >= 20 -> 2
        else -> 1
    }

    fun trapsAllowedByCurrentHunter(): Int =
        trapsAllowedByHunterLevel(Skill.Hunter.realLevel())

    // -----------------------------
    // Hunter XP helpers using SKILLS_HUNTER
    // -----------------------------

    private const val HUNTER_INDEX = Constants.SKILLS_HUNTER

    val currentHunterExperience: Int
        get() = Skills.experience(HUNTER_INDEX)

    fun hunterExperienceGained(since: Int): Int =
        currentHunterExperience - since

    fun snapshotHunterExperience(): Int = currentHunterExperience

    // -----------------------------
    // RED CRAB: The Pandemonium
    // -----------------------------

    /**
     * Standing tiles for efficient pathing:
     */
    val redCrabStart: Tile = Tile(3037, 2971, 0)
    val redCrabTrap4Standing: Tile = Tile(3037, 2972, 0)
    val redCrabTrap3Standing: Tile = Tile(3037, 2973, 0)
    val redCrabTrap2Standing: Tile = Tile(3036, 2975, 0)
    val redCrabTrap1Standing: Tile = Tile(3034, 2978, 0)

    // Trap spots (IDs + tiles + (optional) standing tiles).
    private val redTrap1 = TrapSpot(
        label = "Trap 1",
        objectId = 58884,
        tile = Tile(3033, 2978, 0),
        standingTile = redCrabTrap1Standing, // no special pre-stand
    )

    private val redTrap2 = TrapSpot(
        label = "Trap 2",
        objectId = 58885,
        tile = Tile(3035, 2975, 0),
        standingTile = redCrabTrap2Standing, // no special pre-stand
    )

    private val redTrap3 = TrapSpot(
        label = "Trap 3",
        objectId = 58886,
        tile = Tile(3037, 2974, 0),
        standingTile = redCrabTrap3Standing,
    )

    private val redTrap4 = TrapSpot(
        label = "Trap 4",
        objectId = 58887,
        tile = Tile(3036, 2972, 0),
        standingTile = redCrabTrap4Standing,
    )

    private val redTrap5 = TrapSpot(
        label = "Trap 5",
        objectId = 58888,
        tile = Tile(3037, 2970, 0),
        standingTile = redCrabStart,
    )

    /**
     * Main Red Crab location in The Pandemonium.
     */
    val redCrab: CrabInformation = CrabInformation(
        rawName = CrabType.RED,
        rawId = 31671,
        pasteName = "Crab paste",
        pasteId = 31708,
        area = Area(
            Tile(3022, 3006, 0),     // south-west
            Tile(3071, 2958, 0),     // north-east
        ),
        hunterLevelRequired = 21,
        sailingLevelRequired = null,
        bait = Bait.FISH_OFFCUTS,
        trapSpots = listOf(redTrap1, redTrap2, redTrap3, redTrap4, redTrap5),
        setups = listOf(
            // 2 traps: Hunter 20–39 – 5 → 4.
            TrapSetup(
                minHunterLevel = 20,
                maxHunterLevel = 39,
                maxTrapCount = 2,
                trapsInOrder = listOf(redTrap5, redTrap4),
            ),
            // 3 traps: Hunter 40–59 – 5 → 4 → 3.
            TrapSetup(
                minHunterLevel = 40,
                maxHunterLevel = 59,
                maxTrapCount = 3,
                trapsInOrder = listOf(redTrap5, redTrap4, redTrap3),
            ),
            // 4 traps: Hunter 60–79 – 5 → 4 → 3 → 2.
            TrapSetup(
                minHunterLevel = 60,
                maxHunterLevel = 79,
                maxTrapCount = 4,
                trapsInOrder = listOf(redTrap5, redTrap4, redTrap3, redTrap2),
            ),
            // 5 traps: Hunter 80+ – 5 → 4 → 3 → 2 → 1.
            TrapSetup(
                minHunterLevel = 80,
                maxHunterLevel = null,
                maxTrapCount = 5,
                trapsInOrder = listOf(redTrap5, redTrap4, redTrap3, redTrap2, redTrap1),
            ),
        ),
        bankLocation = Tile(3039, 3000, 0),
    )

    // -----------------------------
    // BLUE CRAB: The Great Conch (skeleton to fill)
    // -----------------------------

    val blueCrab: CrabInformation = CrabInformation(
        rawName = CrabType.BLUE,
        rawId = null,
        pasteName = "Crab paste",
        pasteId = 31708,
        area = null,
        hunterLevelRequired = 48,
        sailingLevelRequired = 45,
        bait = Bait.FISH_OFFCUTS,
        trapSpots = emptyList(),
        setups = emptyList(),
        bankLocation = null,
    )

    // -----------------------------
    // RAINBOW CRAB: The Crown Jewel (skeleton to fill)
    // -----------------------------

    val rainbowCrab: CrabInformation = CrabInformation(
        rawName = CrabType.RAINBOW,
        rawId = null,
        pasteName = "Rainbow crab paste",
        pasteId = null,
        area = null,
        hunterLevelRequired = 77,
        sailingLevelRequired = 64,
        bait = Bait.FINE_FISH_OFFCUTS,
        trapSpots = emptyList(),
        setups = emptyList(),
        bankLocation = null,
    )

    // -----------------------------
    // Convenience collection / filters
    // -----------------------------

    val allCrabLocations: List<CrabInformation> = listOf(
        redCrab,
        blueCrab,
        rainbowCrab,
    )

    fun unlockedLocations(): List<CrabInformation> =
        allCrabLocations.filter {
            it.meetsHunterRequirementNow() &&
                    it.meetsSailingRequirementFromPlayerStats()
        }

    object CrabItems {
        const val PESTLE_MORTAR = 233
    }

}
