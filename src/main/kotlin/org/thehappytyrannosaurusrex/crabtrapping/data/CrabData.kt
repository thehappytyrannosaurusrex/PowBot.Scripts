package org.thehappytyrannosaurusrex.crabtrapping.data

import org.powbot.api.Area
import org.powbot.api.Tile
import org.powbot.api.rt4.Skills
import org.powbot.api.rt4.Constants
import org.powbot.api.rt4.walking.model.Skill
import org.thehappytyrannosaurusrex.api.data.PlayerStats


// ----------------------------------------
// Basic enums / constants
// ----------------------------------------

enum class CrabType {
    RED,
    BLUE,
    RAINBOW,
}

enum class Bait(val itemName: String) {
    FISH_OFFCUTS("Fish offcuts"),
    FINE_FISH_OFFCUTS("Fine fish offcuts"),
}

enum class TrapState(
    val objectName: String,
    val primaryAction: String?,
    val description: String,
) {
    EMPTY(
        objectName = "Crab trap (empty)",
        primaryAction = "Bait",
        description = "No bait or crab in the trap.",
    ),
    CAUGHT(
        objectName = "Crab trap (full)",
        primaryAction = "Empty",
        description = "Crab caught, ready to be emptied.",
    ),
    BAITED(
        objectName = "Crab trap (baited)",
        primaryAction = "Empty",
        description = "Trap is baited and waiting for a crab.",
    ),
    NOT_BUILT(
        objectName = "Hinged-lid trap",
        primaryAction = "Build",
        description = "Trap not built yet.",
    )
}

// ----------------------------------------
// Trap and setup data
// ----------------------------------------

data class TrapSpot(
    val label: String,   // "Trap 1", "Trap 2", etc.
    val objectId: Int,
    val tile: Tile,
)

/**
 * A particular trap configuration for a Hunter level bracket.
 *
 * Example:
 *  - For levels 20–39, use 2 traps anchored near Trap 5.
 *  - For 40–59, use 3 traps, etc.
 */
data class TrapSetup(
    val minHunterLevel: Int,
    val maxHunterLevel: Int?,      // null = no upper bound
    val maxTrapCount: Int,
    val anchorTile: Tile?,         // tile you stand on for this rotation
    val trapsInOrder: List<TrapSpot>, // order you interact with traps
)

/**
 * Reads Sailing level using the PlayerStats API.
 *
 * Expects PlayerStats.SAILING_LVL.text() to return the level as a String,
 * e.g. "64". If parsing fails, returns null.
 */
fun currentSailingLevelFromPlayerStats(): Int? =
    PlayerStats.SAILING_LVL.text().toIntOrNull()


/**
 * All static data for a crab location (Red/Blue/Rainbow).
 *
 * Sailing requirement is just an Int for now – the API has no Sailing
 * skill enum yet, but you can wire your own Sailing level provider into
 * the helper below.
 */
data class CrabLocation(
    val type: CrabType,
    val locationName: String,
    val area: Area?,
    val hunterLevelRequired: Int,
    val sailingLevelRequired: Int?,
    val bait: Bait,
    val questRequirement: String?,
    val trapSpots: List<TrapSpot>,
    val setups: List<TrapSetup>,
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

    /**
     * Generic Sailing requirement check (you can still pass a value manually).
     */
    fun meetsSailingRequirement(currentSailingLevel: Int?): Boolean =
        sailingLevelRequired?.let { required ->
            (currentSailingLevel ?: 0) >= required
        } ?: true

    /**
     * Sailing requirement check that reads the level from PlayerStats.
     */
    fun meetsSailingRequirementFromPlayerStats(): Boolean =
        meetsSailingRequirement(currentSailingLevelFromPlayerStats())
}


// ----------------------------------------
// Global crab data
// ----------------------------------------

object CrabData {

    const val QUEST_PANDEMONIUM = "Pandemonium"

    // -----------------------------
    // Hunter-level -> trap count
    // Uses Skill.Hunter.realLevel() from the PowBot API.
    // -----------------------------

    /**
     * Max traps allowed for a given Hunter level.
     */
    fun trapsAllowedByHunterLevel(hunterLevel: Int): Int = when {
        hunterLevel >= 80 -> 5
        hunterLevel >= 60 -> 4
        hunterLevel >= 40 -> 3
        hunterLevel >= 20 -> 2
        else -> 1 // or 0 if you want to forbid <20
    }

    /**
     * Max traps allowed right now using the live Hunter level.
     */
    fun trapsAllowedByCurrentHunter(): Int =
        trapsAllowedByHunterLevel(Skill.Hunter.realLevel())

    // -----------------------------
    // RED CRAB: The Pandemonium
    // -----------------------------

    // Trap spots (IDs + tiles) – from your crab data notes.
    private val redTrap1 = TrapSpot(
        label = "Trap 1",
        objectId = 58884,
        tile = Tile(3033, 2978, 0),
    )

    private val redTrap2 = TrapSpot(
        label = "Trap 2",
        objectId = 58885,
        tile = Tile(3035, 2975, 0),
    )

    private val redTrap3 = TrapSpot(
        label = "Trap 3",
        objectId = 58886,
        tile = Tile(3037, 2974, 0),
    )

    private val redTrap4 = TrapSpot(
        label = "Trap 4",
        objectId = 58887,
        tile = Tile(3036, 2972, 0),
    )

    private val redTrap5 = TrapSpot(
        label = "Trap 5",
        objectId = 58888,
        tile = Tile(3037, 2970, 0),
    )

    /**
     * Main Red Crab location in The Pandemonium.
     *
     * Area is defined as a rectangle between two diagonal tiles using
     * PowBot's Area class.
     */
    val redCrab: CrabLocation = CrabLocation(
        type = CrabType.RED,
        locationName = "The Pandemonium",
        area = Area(
            Tile(3022, 3006, 0),     // south-west
            Tile(3071, 2958, 0),     // north-east
        ),
        hunterLevelRequired = 21,
        sailingLevelRequired = null,
        bait = Bait.FISH_OFFCUTS,
        questRequirement = QUEST_PANDEMONIUM,
        trapSpots = listOf(redTrap1, redTrap2, redTrap3, redTrap4, redTrap5),
        setups = listOf(
            // 2 traps: Hunter 20–39 – anchor orthogonal to Trap 5.
            TrapSetup(
                minHunterLevel = 20,
                maxHunterLevel = 39,
                maxTrapCount = 2,
                anchorTile = Tile(3037, 2971, 0),
                trapsInOrder = listOf(redTrap5, redTrap4),
            ),
            // 3 traps: Hunter 40–59 – anchor orthogonal to Trap 4.
            TrapSetup(
                minHunterLevel = 40,
                maxHunterLevel = 59,
                maxTrapCount = 3,
                anchorTile = Tile(3037, 2972, 0),
                trapsInOrder = listOf(redTrap5, redTrap4, redTrap3),
            ),
            // 4 traps: Hunter 60–79 – TODO anchor; using a null for now.
            TrapSetup(
                minHunterLevel = 60,
                maxHunterLevel = 79,
                maxTrapCount = 4,
                anchorTile = Tile(3037, 2971, 0),
                trapsInOrder = listOf(redTrap5, redTrap4, redTrap3, redTrap2),
            ),
            // 5 traps: Hunter 80+ – full rotation 5 → 1.
            TrapSetup(
                minHunterLevel = 80,
                maxHunterLevel = null,
                maxTrapCount = 5,
                anchorTile = Tile(3037, 2972, 0),
                trapsInOrder = listOf(redTrap5, redTrap4, redTrap3, redTrap2, redTrap1),
            ),
        ),
    )

    // -----------------------------
    // BLUE CRAB: The Great Conch
    // -----------------------------

    /**
     * TODO:
     *  - Fill in Area once you’ve measured it with tiles.
     *  - Add TrapSpot list with object IDs + tiles for each trap.
     *  - Add TrapSetup list for level brackets, like redCrab.
     */
    val blueCrab: CrabLocation = CrabLocation(
        type = CrabType.BLUE,
        locationName = "The Great Conch",
        area = null,                 // TODO: Area(Tile(...), Tile(...))
        hunterLevelRequired = 48,
        sailingLevelRequired = 45,
        bait = Bait.FISH_OFFCUTS,
        questRequirement = null,
        trapSpots = emptyList(),     // TODO
        setups = emptyList(),        // TODO
    )

    // -----------------------------
    // RAINBOW CRAB: The Crown Jewel
    // -----------------------------

    val rainbowCrab: CrabLocation = CrabLocation(
        type = CrabType.RAINBOW,
        locationName = "The Crown Jewel",
        area = null,                 // TODO: Area(Tile(...), Tile(...))
        hunterLevelRequired = 77,
        sailingLevelRequired = 64,
        bait = Bait.FINE_FISH_OFFCUTS,
        questRequirement = null,
        trapSpots = emptyList(),     // TODO
        setups = emptyList(),        // TODO
    )

    // -----------------------------
    // Convenience collection
    // -----------------------------

    val allCrabLocations: List<CrabLocation> = listOf(
        redCrab,
        blueCrab,
        rainbowCrab,
    )
}
