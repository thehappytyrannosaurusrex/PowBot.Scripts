package org.thehappytyrannosaurusrex.api.pathing

import org.powbot.dax.teleports.Teleport

object DaxConfig {
    val BLACKLISTED_TELEPORTS = arrayOf(
        Teleport.SOUL_WARS_MINIGAME,
        Teleport.LAST_MAN_STANDING_MINIGAME,
        Teleport.BARBARIAN_ASSAULT_MINIGAME,
        Teleport.TZHAAR_FIGHT_PIT_MINIGAME,
        Teleport.BURTHROPE_GAMES_ROOM_MINIGAME
    )
}
