package org.thehappytyrannosaurusrex.varrockmuseum.data

import org.powbot.api.Area
import org.powbot.api.Tile

object MuseumConstants {
    // Items
    const val UNCLEANED_FIND = 11175
    const val ANTIQUE_LAMP = 11189
    const val TROWEL = 676
    const val ROCK_PICK = 675
    const val SPECIMEN_BRUSH = 670
    const val LEATHER_GLOVES = 1059
    const val LEATHER_BOOTS = 1061

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

    // 2) Common artefacts (crate-eligible finds want to store)
    val COMMON_ARTEFACT_NAMES = setOf(
        "Pottery",
        "Jewellery",
        "Old chipped vase",
        "Arrowheads"
    )

    // 3) Unremarkable finds (fine to drop unless user asks to keep them)
    val UNREMARKABLE_FIND_NAMES = setOf(
        "Broken arrow",
        "Broken glass",
        "Iron dagger",
        "Uncut jade",
        "Bones",
        "Coins"
    )

    // 4) Storage crate rewards (what get back after "Add finds")
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
    const val SPECIMEN_TABLE_ID = 24556
    const val STORAGE_CRATE_ID = 24534
    val SPECIMEN_ROCK_IDS = intArrayOf(24557, 24558, 24559)

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
    val INITIAL_KEEP_FOR_BANK = intArrayOf(
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
    val KEEP_WHEN_DROPPING = intArrayOf(
        TROWEL,
        ROCK_PICK,
        SPECIMEN_BRUSH,
        UNCLEANED_FIND,
        ANTIQUE_LAMP
    )
}
