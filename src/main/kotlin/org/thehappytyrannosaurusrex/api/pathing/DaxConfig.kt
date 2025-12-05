package org.thehappytyrannosaurusrex.api.pathing

import org.powbot.dax.teleports.Teleport

object DaxConfig {

    val ALL_MINIGAME_TELEPORTS = arrayOf(
        Teleport.SOUL_WARS_MINIGAME,
        Teleport.LAST_MAN_STANDING_MINIGAME,
        Teleport.BARBARIAN_ASSAULT_MINIGAME,
        Teleport.TZHAAR_FIGHT_PIT_MINIGAME,
        Teleport.BURTHROPE_GAMES_ROOM_MINIGAME,
        Teleport.CLAN_WARS_MINIGAME,
        Teleport.CASTLE_WARS_MINIGAME,
        Teleport.PEST_CONTROL_MINIGAME,
        Teleport.FISHING_TRAWLER_MINIGAME,
        Teleport.TROUBLE_BREWING_MINIGAME,
        Teleport.GIANTS_FOUNDRY_MINIGAME,
    )

    /**
     * Default blacklist - all minigame teleports.
     */
    val BLACKLISTED_TELEPORTS: Array<Teleport> = ALL_MINIGAME_TELEPORTS
}