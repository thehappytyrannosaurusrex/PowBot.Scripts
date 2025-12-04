package org.thehappytyrannosaurusrex.varrockmuseum.data

import org.powbot.api.Area
import org.powbot.api.Tile

object MuseumConstants {
    // Items
    const val UNCLEANED_FIND = "Uncleaned find"
    const val ANTIQUE_LAMP = "Antique Lamp"
    const val TROWEL = "Trowel"
    const val ROCK_PICK = "Rock pick"
    const val SPECIMEN_BRUSH = "Specimen brush"
    const val LEATHER_GLOVES = "Leather gloves"
    const val LEATHER_BOOTS = "Leather boots"

    /**
 * Name-based groupings for cleaned finds / rewards.
 */

    // 1) Unique artefacts (display case items + clean necklace)
    val UNIQUE_ARTEFACT_NAMES = setOf(
        "Old symbol",
        "Ancient symbol",
        "Old coin",
        "Ancient coin",
        "Clean necklace"
    )

    val COMMON_ARTEFACT_NAMES = setOf(
        "Pottery",
        "Jewellery",
        "Old chipped vase",
        "Arrowheads"
    )

    val UNREMARKABLE_FIND_NAMES = setOf(
        "Broken arrow",
        "Broken glass",
        "Iron dagger",
        "Uncut jade",
        "Bones",
        "Coins"
    )
    val STORAGE_CRATE_REWARD_NAMES = setOf(
        "Coins",
        "Bowl",
        "Pot",
        "Iron bolts",
        "Iron dagger",
        "Bronze limbs",
        "Wooden stock",
        "Tin ore",
        "Copper ore",
        "Iron ore",
        "Coal",
        "Mithril ore",
        "Iron knife",
        "Iron dart",
        "Iron arrowtips",
        "Uncut jade",
        "Uncut opal",
        "Big bones",
        "Antique lamp"
    )

    // (Existing ID-based lists are kept for backwards compatibility if use them elsewhere.)

    // Quest (only used for logging now; main check is via Quests API)
    const val QUEST_DIGSITE_ID = 29

    // Objects
    const val SPECIMEN_TABLE = "Specimen table"
    const val STORAGE_CRATE = "Storage crate"
    val SPECIMEN_ROCK = "Dig Site specimen rocks"

    // Cleaning area & navigation
    val CLEANING_AREA = Area(Tile(3253, 3440, 0), Tile(3268, 3455, 0))
    val MUSEUM_TARGET_TILE = Tile(3257, 3448, 0)

    // Varrock East bank (used for both initial cleanup and keep-item banking)
    val VARROCK_EAST_BANK_TILE = Tile(3254, 3420, 0)

    val TOOLS_TILE = Tile(3259, 3441, 0)

    val DOOR_TILES = arrayOf(Tile(3264, 3441, 0), Tile(3265, 3441, 0))
    val GATE_TILES = arrayOf(Tile(3261, 3447, 0), Tile(3260, 3447, 0))

    /**
 * Items keep in the INVENTORY when doing an initial bank clean.
 */
    val INITIAL_KEEP_FOR_BANK = setOf(
        TROWEL,
        ROCK_PICK,
        SPECIMEN_BRUSH,
        LEATHER_GLOVES,
        LEATHER_BOOTS,
        UNCLEANED_FIND,
        ANTIQUE_LAMP
    )

    /**
 * Items never drop via the generic "drop junk" helper.
 */
    val KEEP_WHEN_DROPPING = setOf(
        TROWEL,
        ROCK_PICK,
        SPECIMEN_BRUSH,
        UNCLEANED_FIND,
        ANTIQUE_LAMP
    )
}
