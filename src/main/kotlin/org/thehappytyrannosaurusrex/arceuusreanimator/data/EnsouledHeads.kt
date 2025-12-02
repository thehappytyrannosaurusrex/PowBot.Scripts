package org.thehappytyrannosaurusrex.arceuusreanimator.data

import org.powbot.api.rt4.Magic
import org.powbot.api.rt4.magic.Rune

data class RuneRequirement(val rune: Rune, val amount: Int)

val REANIMATION_RUNES: Map<Magic.ArceuusSpell, List<RuneRequirement>> = mapOf(
    Magic.ArceuusSpell.BASIC_REANIMATION to listOf(
        RuneRequirement(Rune.BODY, 4),
        RuneRequirement(Rune.NATURE, 2),
    ),
    Magic.ArceuusSpell.ADEPT_REANIMATION to listOf(
        RuneRequirement(Rune.BODY, 4),
        RuneRequirement(Rune.NATURE, 3),
        RuneRequirement(Rune.SOUL, 1),
    ),
    Magic.ArceuusSpell.EXPERT_REANIMATION to listOf(
        RuneRequirement(Rune.BLOOD, 1),
        RuneRequirement(Rune.NATURE, 3),
        RuneRequirement(Rune.SOUL, 2),
    ),
    Magic.ArceuusSpell.MASTER_REANIMATION to listOf(
        RuneRequirement(Rune.BLOOD, 2),
        RuneRequirement(Rune.NATURE, 4),
        RuneRequirement(Rune.SOUL, 4),
    )
)

enum class EnsouledHead(
    val displayName: String,
    val objIdItem: Int,
    val objIdDrop: Int,
    val spell: Magic.ArceuusSpell,
    val NpcName: String,
    val NpcId: Int
) {
    GOBLIN(
        "Ensouled goblin head",
        13448,
        13447,
        Magic.ArceuusSpell.BASIC_REANIMATION,
        "Reanimated goblin",
        7018
    ),
    MONKEY(
        "Ensouled monkey head",
        13451,
        13450,
        Magic.ArceuusSpell.BASIC_REANIMATION,
        "Reanimated monkey",
        7019
    ),
    IMP(
        "Ensouled imp head",
        13454,
        13453,
        Magic.ArceuusSpell.BASIC_REANIMATION,
        "Reanimated imp",
        7020
    ),
    MINOTAUR(
        "Ensouled minotaur head",
        13457,
        13456,
        Magic.ArceuusSpell.BASIC_REANIMATION,
        "Reanimated minotaur",
        7021
    ),
    SCORPION(
        "Ensouled scorpion head",
        13460,
        13459,
        Magic.ArceuusSpell.BASIC_REANIMATION,
        "Reanimated scorpion",
        7022
    ),
    BEAR(
        "Ensouled bear head",
        13463,
        13462,
        Magic.ArceuusSpell.BASIC_REANIMATION,
        "Reanimated bear",
        7023
    ),
    UNICORN(
        "Ensouled unicorn head",
        13466,
        13465,
        Magic.ArceuusSpell.BASIC_REANIMATION,
        "Reanimated unicorn",
        7024
    ),
    DOG(
        "Ensouled dog head",
        13469,
        13468,
        Magic.ArceuusSpell.ADEPT_REANIMATION,
        "Reanimated dog",
        7025
    ),
    CHAOS_DRUID(
        "Ensouled chaos druid head",
        13472,
        13471,
        Magic.ArceuusSpell.ADEPT_REANIMATION,
        "Reanimated chaos druid",
        7026
    ),
    GIANT(
        "Ensouled giant head",
        13475,
        13474,
        Magic.ArceuusSpell.ADEPT_REANIMATION,
        "Reanimated giant",
        7027
    ),
    OGRE(
        "Ensouled ogre head",
        13478,
        13477,
        Magic.ArceuusSpell.ADEPT_REANIMATION,
        "Reanimated ogre",
        7028
    ),
    ELF(
        "Ensouled elf head",
        13481,
        13480,
        Magic.ArceuusSpell.ADEPT_REANIMATION,
        "Reanimated elf",
        7029
    ),
    TROLL(
        "Ensouled troll head",
        13484,
        13483,
        Magic.ArceuusSpell.ADEPT_REANIMATION,
        "Reanimated troll",
        7030
    ),
    HORROR(
        "Ensouled horror head",
        13487,
        13486,
        Magic.ArceuusSpell.ADEPT_REANIMATION,
        "Reanimated horror",
        7031
    ),
    KALPHITE(
        "Ensouled kalphite head",
        13490,
        13489,
        Magic.ArceuusSpell.EXPERT_REANIMATION,
        "Reanimated kalphite",
        7032
    ),
    DAGANNOTH(
        "Ensouled dagannoth head",
        13493,
        13492,
        Magic.ArceuusSpell.EXPERT_REANIMATION,
        "Reanimated dagannoth",
        7033
    ),
    BLOODVELD(
        "Ensouled bloodveld head",
        13496,
        13495,
        Magic.ArceuusSpell.EXPERT_REANIMATION,
        "Reanimated bloodveld",
        7034
    ),
    TZHAAR(
        "Ensouled tzhaar head",
        13499,
        13498,
        Magic.ArceuusSpell.EXPERT_REANIMATION,
        "Reanimated tzhaar",
        7035
    ),
    DEMON(
        "Ensouled demon head",
        13502,
        13501,
        Magic.ArceuusSpell.EXPERT_REANIMATION,
        "Reanimated demon",
        7036
    ),
    HELLHOUND(
        "Ensouled hellhound head",
        26997,
        26996,
        Magic.ArceuusSpell.EXPERT_REANIMATION,
        "Reanimated hellhound",
        11463
    ),
    AVIANSIE(
        "Ensouled aviansie head",
        13505,
        13504,
        Magic.ArceuusSpell.MASTER_REANIMATION,
        "Reanimated aviansie",
        7037
    ),
    ABYSSAL(
        "Ensouled abyssal head",
        13508,
        13507,
        Magic.ArceuusSpell.MASTER_REANIMATION,
        "Reanimated abyssal",
        7038
    ),
    DRAGON(
        "Ensouled dragon head",
        13511,
        13510,
        Magic.ArceuusSpell.MASTER_REANIMATION,
        "Reanimated dragon",
        7039
    )
}
