package org.thehappytyrannosaurusrex.varrockmuseum.data

import org.powbot.api.Area
import org.powbot.api.Tile
import org.powbot.api.rt4.Equipment

object MuseumConstants {

    // =========================================================================
    // Item Names
    // =========================================================================

    const val UNCLEANED_FIND = "Uncleaned find"
    const val ANTIQUE_LAMP = "Antique lamp"
    const val LAMP = "Lamp"
    const val TROWEL = "Trowel"
    const val ROCK_PICK = "Rock pick"
    const val SPECIMEN_BRUSH = "Specimen brush"
    const val LEATHER_GLOVES = "Leather gloves"
    const val LEATHER_BOOTS = "Leather boots"

    // =========================================================================
    // Object Names
    // =========================================================================

    const val TOOL_OBJECT = "Tools"
    const val SPECIMEN_TABLE = "Specimen table"
    const val STORAGE_CRATE = "Storage crate"
    const val SPECIMEN_ROCK = "Dig Site specimen rocks"
    const val MUSEUM_DOOR = "Door"
    const val MUSEUM_GATE = "Gate"

    // =========================================================================
    // Equipment Slots
    // =========================================================================

    val HAND_SLOT = Equipment.Slot.HANDS
    val FEET_SLOT = Equipment.Slot.FEET

    // =========================================================================
    // Cleaning Tools (max 1 of each in inventory)
    // =========================================================================

    val CLEANING_TOOLS = setOf(TROWEL, ROCK_PICK, SPECIMEN_BRUSH)
    val LEATHER_EQUIPMENT = setOf(LEATHER_GLOVES, LEATHER_BOOTS)

    // =========================================================================
    // Artefact Categories
    // =========================================================================

    /**
     * Unique artefacts - display case items that should be dropped (not stored).
     * These are junk items.
     */
    val UNIQUE_ARTEFACT_NAMES = setOf(
        "Old symbol",
        "Ancient symbol",
        "Old coin",
        "Ancient coin",
        "Clean necklace"
    )

    /**
     * Common artefacts - can be stored in the storage crate for rewards.
     */
    val COMMON_ARTEFACT_NAMES = setOf(
        "Pottery",
        "Jewellery",
        "Old chipped vase",
        "Arrowheads"
    )

    /**
     * All artefact names (common + unique)
     */
    val ALL_ARTEFACT_NAMES = COMMON_ARTEFACT_NAMES + UNIQUE_ARTEFACT_NAMES

    // =========================================================================
    // Storage Crate Rewards (junk items to drop)
    // =========================================================================

    /**
     * Items received from storage crate - these are junk and should be dropped.
     * Note: Coins and Antique lamp are special cases handled separately.
     */
    val STORAGE_CRATE_REWARD_NAMES = setOf(
        "Bowl", "Pot", "Iron bolts", "Iron dagger",
        "Bronze limbs", "Wooden stock", "Tin ore", "Copper ore",
        "Iron ore", "Coal", "Mithril ore", "Iron knife", "Iron dart",
        "Iron arrowtips", "Uncut jade", "Uncut opal", "Big bones"
    )

    /**
     * Unremarkable finds from cleaning (junk to drop)
     */
    val UNREMARKABLE_FIND_NAMES = setOf(
        "Broken arrow",
        "Broken glass",
        "Iron dagger",
        "Uncut jade",
        "Bones"
        // Note: "Coins" removed - always kept by default
    )

    /**
     * All junk items (unique artefacts + storage rewards + unremarkable finds)
     */
    val ALL_JUNK_ITEMS = UNIQUE_ARTEFACT_NAMES + STORAGE_CRATE_REWARD_NAMES + UNREMARKABLE_FIND_NAMES

    // =========================================================================
    // Items to NEVER Drop (script internal)
    // =========================================================================

    /**
     * Items that should never be dropped by the script.
     * User's keep items are added to this at runtime.
     */
    val SCRIPT_KEEP_ITEMS = setOf(
        TROWEL, ROCK_PICK, SPECIMEN_BRUSH,
        UNCLEANED_FIND, ANTIQUE_LAMP,
        "Coins" // Always keep coins by default
    )

    // =========================================================================
    // Items for Bank Filtering
    // =========================================================================

    /**
     * Items that are allowed in inventory (won't trigger banking).
     * Everything else should be banked before starting.
     */
    val ALLOWED_INVENTORY_ITEMS = setOf(
        TROWEL, ROCK_PICK, SPECIMEN_BRUSH,
        UNCLEANED_FIND, ANTIQUE_LAMP,
        // Common artefacts are semi-clean - will be processed
        "Pottery", "Jewellery", "Old chipped vase", "Arrowheads"
    )

    // =========================================================================
    // Locations
    // =========================================================================

    val CLEANING_AREA = Area(Tile(3258, 3442, 0), Tile(3267, 3456, 0))
    val MUSEUM_TARGET_TILE = Tile(3257, 3448, 0)
    val VARROCK_EAST_BANK_TILE = Tile(3254, 3420, 0)
    val TOOLS_TILE = Tile(3259, 3441, 0)

    // Door and gate positions
    val BACK_DOOR_TILE = Tile(3265, 3441, 0)
    val GATE_TILE = Tile(3261, 3447, 0)
    val DOOR_TILES = arrayOf(Tile(3264, 3441, 0), Tile(3265, 3441, 0))
    val GATE_TILES = arrayOf(Tile(3261, 3447, 0), Tile(3260, 3447, 0))

}