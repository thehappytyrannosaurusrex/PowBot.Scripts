package org.thehappytyrannosaurusrex.varrockmuseum.data

import org.powbot.api.Area
import org.powbot.api.Tile

object MuseumConstants {

    // Item names
    const val UNCLEANED_FIND = "Uncleaned find"
    const val ANTIQUE_LAMP = "Antique lamp"
    const val TROWEL = "Trowel"
    const val ROCK_PICK = "Rock pick"
    const val SPECIMEN_BRUSH = "Specimen brush"
    const val LEATHER_GLOVES = "Leather gloves"
    const val LEATHER_BOOTS = "Leather boots"

    // Object names (for interactions)
    const val TOOL_OBJECT = "Tools"
    const val SPECIMEN_TABLE = "Specimen table"
    const val STORAGE_CRATE = "Storage crate"
    const val SPECIMEN_ROCK = "Dig Site specimen rocks"
    const val MUSEUM_DOOR = "Door"
    const val MUSEUM_GATE = "Gate"

    // Unique artefacts (display case items)
    val UNIQUE_ARTEFACT_NAMES = setOf(
        "Old symbol",
        "Ancient symbol",
        "Old coin",
        "Ancient coin",
        "Clean necklace"
    )

    // Common artefacts
    val COMMON_ARTEFACT_NAMES = setOf(
        "Pottery",
        "Jewellery",
        "Old chipped vase",
        "Arrowheads"
    )

    // Unremarkable finds (junk)
    val UNREMARKABLE_FIND_NAMES = setOf(
        "Broken arrow",
        "Broken glass",
        "Iron dagger",
        "Uncut jade",
        "Bones",
        "Coins"
    )

    // Storage crate rewards
    val STORAGE_CRATE_REWARD_NAMES = setOf(
        "Coins", "Bowl", "Pot", "Iron bolts", "Iron dagger",
        "Bronze limbs", "Wooden stock", "Tin ore", "Copper ore",
        "Iron ore", "Coal", "Mithril ore", "Iron knife", "Iron dart",
        "Iron arrowtips", "Uncut jade", "Uncut opal", "Big bones", "Antique lamp"
    )

    // Quest varp
    const val QUEST_DIGSITE_ID = 29

    // Locations
    val CLEANING_AREA = Area(Tile(3253, 3440, 0), Tile(3268, 3455, 0))
    val MUSEUM_TARGET_TILE = Tile(3257, 3448, 0)
    val VARROCK_EAST_BANK_TILE = Tile(3254, 3420, 0)
    val TOOLS_TILE = Tile(3259, 3441, 0)
    val DOOR_TILES = arrayOf(Tile(3264, 3441, 0), Tile(3265, 3441, 0))
    val GATE_TILES = arrayOf(Tile(3261, 3447, 0), Tile(3260, 3447, 0))

    // Items to keep when banking at startup
    val INITIAL_KEEP_FOR_BANK = setOf(
        TROWEL, ROCK_PICK, SPECIMEN_BRUSH, LEATHER_GLOVES,
        LEATHER_BOOTS, UNCLEANED_FIND, ANTIQUE_LAMP
    )

    // Items to never drop
    val KEEP_WHEN_DROPPING = setOf(
        TROWEL, ROCK_PICK, SPECIMEN_BRUSH, UNCLEANED_FIND, ANTIQUE_LAMP
    )
}