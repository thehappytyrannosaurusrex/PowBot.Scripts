/*
 * Project: Arceuus Library Script (PowBot)
 * File: Bookshelf.kt
 * Purpose: Added review header and standardized logging to Logger.info.
 * Notes: Generated comments + logging normalization on 2025-11-12.
 */

package org.thehappytyrannosaurusrex.arceuuslibrary.data
import org.thehappytyrannosaurusrex.api.utils.Logger

import org.powbot.api.Tile
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Locations.Area

enum class Facing { N, E, S, W }

data class Bookshelf(
    val shelfIndex: Int,
    val shelfObjId: Int,
    val objTile: Tile,        // tile of the bookshelf object (clickable)
    val facing: Facing,
    val standingTile: Tile,   // orthogonal standing spot required to Search
    val area: Area,
    val floor: Int
)

object Bookshelves {
    val ALL: List<Bookshelf> = listOf(
        Bookshelf(
            shelfIndex = 0,
            shelfObjId = 28199,
            objTile = Tile(1626, 3795, 0),
            facing = Facing.E,
            standingTile = Tile(1625, 3795, 0),
            area = Area.SOUTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 1,
            shelfObjId = 27991,
            objTile = Tile(1625, 3793, 0),
            facing = Facing.S,
            standingTile = Tile(1625, 3794, 0),
            area = Area.SOUTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 2,
            shelfObjId = 28111,
            objTile = Tile(1623, 3793, 0),
            facing = Facing.S,
            standingTile = Tile(1623, 3794, 0),
            area = Area.SOUTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 3,
            shelfObjId = 28195,
            objTile = Tile(1620, 3792, 0),
            facing = Facing.N,
            standingTile = Tile(1620, 3791, 0),
            area = Area.SOUTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 4,
            shelfObjId = 28216,
            objTile = Tile(1624, 3792, 0),
            facing = Facing.N,
            standingTile = Tile(1624, 3791, 0),
            area = Area.SOUTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 5,
            shelfObjId = 27996,
            objTile = Tile(1626, 3788, 0),
            facing = Facing.E,
            standingTile = Tile(1625, 3788, 0),
            area = Area.SOUTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 6,
            shelfObjId = 28087,
            objTile = Tile(1626, 3787, 0),
            facing = Facing.E,
            standingTile = Tile(1625, 3787, 0),
            area = Area.SOUTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 7,
            shelfObjId = 28004,
            objTile = Tile(1624, 3784, 0),
            facing = Facing.S,
            standingTile = Tile(1624, 3785, 0),
            area = Area.SOUTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 8,
            shelfObjId = 27993,
            objTile = Tile(1623, 3784, 0),
            facing = Facing.S,
            standingTile = Tile(1623, 3785, 0),
            area = Area.SOUTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 9,
            shelfObjId = 28175,
            objTile = Tile(1621, 3784, 0),
            facing = Facing.S,
            standingTile = Tile(1621, 3785, 0),
            area = Area.SOUTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 10,
            shelfObjId = 28191,
            objTile = Tile(1615, 3785, 0),
            facing = Facing.W,
            standingTile = Tile(1616, 3785, 0),
            area = Area.SOUTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 11,
            shelfObjId = 27996,
            objTile = Tile(1615, 3788, 0),
            facing = Facing.W,
            standingTile = Tile(1616, 3788, 0),
            area = Area.SOUTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 12,
            shelfObjId = 28003,
            objTile = Tile(1615, 3790, 0),
            facing = Facing.W,
            standingTile = Tile(1616, 3790, 0),
            area = Area.SOUTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 13,
            shelfObjId = 27996,
            objTile = Tile(1614, 3790, 0),
            facing = Facing.E,
            standingTile = Tile(1613, 3790, 0),
            area = Area.SOUTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 14,
            shelfObjId = 28295,
            objTile = Tile(1614, 3788, 0),
            facing = Facing.E,
            standingTile = Tile(1613, 3788, 0),
            area = Area.SOUTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 15,
            shelfObjId = 28184,
            objTile = Tile(1614, 3786, 0),
            facing = Facing.E,
            standingTile = Tile(1613, 3786, 0),
            area = Area.SOUTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 16,
            shelfObjId = 28139,
            objTile = Tile(1612, 3784, 0),
            facing = Facing.S,
            standingTile = Tile(1612, 3785, 0),
            area = Area.SOUTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 17,
            shelfObjId = 28200,
            objTile = Tile(1610, 3784, 0),
            facing = Facing.S,
            standingTile = Tile(1610, 3785, 0),
            area = Area.SOUTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 18,
            shelfObjId = 28291,
            objTile = Tile(1609, 3784, 0),
            facing = Facing.S,
            standingTile = Tile(1609, 3785, 0),
            area = Area.SOUTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 19,
            shelfObjId = 27991,
            objTile = Tile(1607, 3786, 0),
            facing = Facing.W,
            standingTile = Tile(1608, 3786, 0),
            area = Area.SOUTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 20,
            shelfObjId = 28196,
            objTile = Tile(1607, 3789, 0),
            facing = Facing.W,
            standingTile = Tile(1608, 3789, 0),
            area = Area.SOUTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 21,
            shelfObjId = 28007,
            objTile = Tile(1607, 3795, 0),
            facing = Facing.W,
            standingTile = Tile(1608, 3795, 0),
            area = Area.SOUTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 22,
            shelfObjId = 28147,
            objTile = Tile(1607, 3796, 0),
            facing = Facing.W,
            standingTile = Tile(1608, 3796, 0),
            area = Area.SOUTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 23,
            shelfObjId = 27993,
            objTile = Tile(1607, 3799, 0),
            facing = Facing.W,
            standingTile = Tile(1608, 3799, 0),
            area = Area.SOUTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 24,
            shelfObjId = 28332,
            objTile = Tile(1610, 3801, 0),
            facing = Facing.N,
            standingTile = Tile(1610, 3800, 0),
            area = Area.SOUTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 25,
            shelfObjId = 26168,
            objTile = Tile(1612, 3801, 0),
            facing = Facing.N,
            standingTile = Tile(1612, 3800, 0),
            area = Area.SOUTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 26,
            shelfObjId = 28174,
            objTile = Tile(1618, 3801, 0),
            facing = Facing.N,
            standingTile = Tile(1618, 3800, 0),
            area = Area.SOUTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 27,
            shelfObjId = 28100,
            objTile = Tile(1620, 3801, 0),
            facing = Facing.N,
            standingTile = Tile(1620, 3800, 0),
            area = Area.SOUTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 28,
            shelfObjId = 28199,
            objTile = Tile(1620, 3814, 0),
            facing = Facing.S,
            standingTile = Tile(1620, 3815, 0),
            area = Area.NORTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 29,
            shelfObjId = 28024,
            objTile = Tile(1618, 3814, 0),
            facing = Facing.S,
            standingTile = Tile(1618, 3815, 0),
            area = Area.NORTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 30,
            shelfObjId = 26168,
            objTile = Tile(1617, 3814, 0),
            facing = Facing.S,
            standingTile = Tile(1617, 3815, 0),
            area = Area.NORTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 31,
            shelfObjId = 28020,
            objTile = Tile(1615, 3816, 0),
            facing = Facing.W,
            standingTile = Tile(1616, 3816, 0),
            area = Area.NORTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 32,
            shelfObjId = 28111,
            objTile = Tile(1615, 3817, 0),
            facing = Facing.W,
            standingTile = Tile(1616, 3817, 0),
            area = Area.NORTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 33,
            shelfObjId = 28108,
            objTile = Tile(1615, 3820, 0),
            facing = Facing.W,
            standingTile = Tile(1616, 3820, 0),
            area = Area.NORTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 34,
            shelfObjId = 28195,
            objTile = Tile(1614, 3820, 0),
            facing = Facing.E,
            standingTile = Tile(1613, 3820, 0),
            area = Area.NORTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 35,
            shelfObjId = 27991,
            objTile = Tile(1614, 3817, 0),
            facing = Facing.E,
            standingTile = Tile(1613, 3817, 0),
            area = Area.NORTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 36,
            shelfObjId = 28100,
            objTile = Tile(1614, 3816, 0),
            facing = Facing.E,
            standingTile = Tile(1613, 3816, 0),
            area = Area.NORTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 37,
            shelfObjId = 28003,
            objTile = Tile(1612, 3814, 0),
            facing = Facing.S,
            standingTile = Tile(1612, 3815, 0),
            area = Area.NORTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 38,
            shelfObjId = 27991,
            objTile = Tile(1610, 3814, 0),
            facing = Facing.S,
            standingTile = Tile(1610, 3815, 0),
            area = Area.NORTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 39,
            shelfObjId = 28007,
            objTile = Tile(1607, 3816, 0),
            facing = Facing.W,
            standingTile = Tile(1608, 3816, 0),
            area = Area.NORTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 40,
            shelfObjId = 28147,
            objTile = Tile(1607, 3817, 0),
            facing = Facing.W,
            standingTile = Tile(1608, 3817, 0),
            area = Area.NORTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 41,
            shelfObjId = 27993,
            objTile = Tile(1607, 3820, 0),
            facing = Facing.W,
            standingTile = Tile(1608, 3820, 0),
            area = Area.NORTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 42,
            shelfObjId = 28147,
            objTile = Tile(1607, 3826, 0),
            facing = Facing.W,
            standingTile = Tile(1608, 3826, 0),
            area = Area.NORTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 43,
            shelfObjId = 28004,
            objTile = Tile(1607, 3828, 0),
            facing = Facing.W,
            standingTile = Tile(1608, 3828, 0),
            area = Area.NORTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 44,
            shelfObjId = 28004,
            objTile = Tile(1609, 3831, 0),
            facing = Facing.N,
            standingTile = Tile(1609, 3830, 0),
            area = Area.NORTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 45,
            shelfObjId = 28175,
            objTile = Tile(1612, 3831, 0),
            facing = Facing.N,
            standingTile = Tile(1612, 3830, 0),
            area = Area.NORTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 46,
            shelfObjId = 28336,
            objTile = Tile(1614, 3831, 0),
            facing = Facing.N,
            standingTile = Tile(1614, 3830, 0),
            area = Area.NORTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 47,
            shelfObjId = 28012,
            objTile = Tile(1619, 3831, 0),
            facing = Facing.N,
            standingTile = Tile(1619, 3830, 0),
            area = Area.NORTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 48,
            shelfObjId = 28132,
            objTile = Tile(1621, 3831, 0),
            facing = Facing.N,
            standingTile = Tile(1621, 3830, 0),
            area = Area.NORTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 49,
            shelfObjId = 28291,
            objTile = Tile(1624, 3831, 0),
            facing = Facing.N,
            standingTile = Tile(1624, 3830, 0),
            area = Area.NORTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 50,
            shelfObjId = 28199,
            objTile = Tile(1626, 3829, 0),
            facing = Facing.E,
            standingTile = Tile(1625, 3829, 0),
            area = Area.NORTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 51,
            shelfObjId = 28312,
            objTile = Tile(1626, 3827, 0),
            facing = Facing.E,
            standingTile = Tile(1625, 3827, 0),
            area = Area.NORTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 52,
            shelfObjId = 28336,
            objTile = Tile(1624, 3823, 0),
            facing = Facing.S,
            standingTile = Tile(1624, 3824, 0),
            area = Area.NORTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 53,
            shelfObjId = 27996,
            objTile = Tile(1622, 3823, 0),
            facing = Facing.S,
            standingTile = Tile(1622, 3824, 0),
            area = Area.NORTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 54,
            shelfObjId = 28003,
            objTile = Tile(1620, 3823, 0),
            facing = Facing.S,
            standingTile = Tile(1620, 3824, 0),
            area = Area.NORTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 55,
            shelfObjId = 28047,
            objTile = Tile(1621, 3822, 0),
            facing = Facing.N,
            standingTile = Tile(1621, 3821, 0),
            area = Area.NORTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 56,
            shelfObjId = 28184,
            objTile = Tile(1624, 3822, 0),
            facing = Facing.N,
            standingTile = Tile(1624, 3821, 0),
            area = Area.NORTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 57,
            shelfObjId = 28307,
            objTile = Tile(1626, 3820, 0),
            facing = Facing.E,
            standingTile = Tile(1625, 3820, 0),
            area = Area.NORTHWEST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 58,
            shelfObjId = 28251,
            objTile = Tile(1639, 3821, 0),
            facing = Facing.W,
            standingTile = Tile(1640, 3821, 0),
            area = Area.NORTHEAST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 59,
            shelfObjId = 28312,
            objTile = Tile(1639, 3822, 0),
            facing = Facing.W,
            standingTile = Tile(1640, 3822, 0),
            area = Area.NORTHEAST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 60,
            shelfObjId = 27996,
            objTile = Tile(1639, 3827, 0),
            facing = Facing.W,
            standingTile = Tile(1640, 3827, 0),
            area = Area.NORTHEAST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 61,
            shelfObjId = 28307,
            objTile = Tile(1639, 3829, 0),
            facing = Facing.W,
            standingTile = Tile(1640, 3829, 0),
            area = Area.NORTHEAST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 62,
            shelfObjId = 27993,
            objTile = Tile(1642, 3831, 0),
            facing = Facing.N,
            standingTile = Tile(1642, 3830, 0),
            area = Area.NORTHEAST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 63,
            shelfObjId = 28272,
            objTile = Tile(1645, 3831, 0),
            facing = Facing.N,
            standingTile = Tile(1645, 3830, 0),
            area = Area.NORTHEAST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 64,
            shelfObjId = 28111,
            objTile = Tile(1646, 3829, 0),
            facing = Facing.E,
            standingTile = Tile(1645, 3829, 0),
            area = Area.NORTHEAST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 65,
            shelfObjId = 28008,
            objTile = Tile(1646, 3827, 0),
            facing = Facing.E,
            standingTile = Tile(1645, 3827, 0),
            area = Area.NORTHEAST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 66,
            shelfObjId = 28108,
            objTile = Tile(1646, 3826, 0),
            facing = Facing.E,
            standingTile = Tile(1645, 3826, 0),
            area = Area.NORTHEAST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 67,
            shelfObjId = 28283,
            objTile = Tile(1647, 3827, 0),
            facing = Facing.W,
            standingTile = Tile(1648, 3827, 0),
            area = Area.NORTHEAST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 68,
            shelfObjId = 27991,
            objTile = Tile(1647, 3829, 0),
            facing = Facing.W,
            standingTile = Tile(1648, 3829, 0),
            area = Area.NORTHEAST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 69,
            shelfObjId = 28100,
            objTile = Tile(1647, 3830, 0),
            facing = Facing.W,
            standingTile = Tile(1648, 3830, 0),
            area = Area.NORTHEAST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 70,
            shelfObjId = 28051,
            objTile = Tile(1652, 3831, 0),
            facing = Facing.N,
            standingTile = Tile(1652, 3830, 0),
            area = Area.NORTHEAST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 71,
            shelfObjId = 28132,
            objTile = Tile(1653, 3831, 0),
            facing = Facing.N,
            standingTile = Tile(1653, 3830, 0),
            area = Area.NORTHEAST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 72,
            shelfObjId = 28291,
            objTile = Tile(1656, 3831, 0),
            facing = Facing.N,
            standingTile = Tile(1656, 3830, 0),
            area = Area.NORTHEAST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 73,
            shelfObjId = 27991,
            objTile = Tile(1658, 3829, 0),
            facing = Facing.E,
            standingTile = Tile(1657, 3829, 0),
            area = Area.NORTHEAST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 74,
            shelfObjId = 28184,
            objTile = Tile(1658, 3826, 0),
            facing = Facing.E,
            standingTile = Tile(1657, 3826, 0),
            area = Area.NORTHEAST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 75,
            shelfObjId = 28173,
            objTile = Tile(1658, 3825, 0),
            facing = Facing.E,
            standingTile = Tile(1657, 3825, 0),
            area = Area.NORTHEAST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 76,
            shelfObjId = 28007,
            objTile = Tile(1658, 3820, 0),
            facing = Facing.E,
            standingTile = Tile(1657, 3820, 0),
            area = Area.NORTHEAST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 77,
            shelfObjId = 28147,
            objTile = Tile(1658, 3819, 0),
            facing = Facing.E,
            standingTile = Tile(1657, 3819, 0),
            area = Area.NORTHEAST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 78,
            shelfObjId = 27993,
            objTile = Tile(1658, 3816, 0),
            facing = Facing.E,
            standingTile = Tile(1657, 3816, 0),
            area = Area.NORTHEAST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 79,
            shelfObjId = 28332,
            objTile = Tile(1655, 3814, 0),
            facing = Facing.S,
            standingTile = Tile(1655, 3815, 0),
            area = Area.NORTHEAST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 80,
            shelfObjId = 28024,
            objTile = Tile(1654, 3814, 0),
            facing = Facing.S,
            standingTile = Tile(1654, 3815, 0),
            area = Area.NORTHEAST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 81,
            shelfObjId = 28336,
            objTile = Tile(1651, 3817, 0),
            facing = Facing.W,
            standingTile = Tile(1652, 3817, 0),
            area = Area.NORTHEAST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 82,
            shelfObjId = 27996,
            objTile = Tile(1651, 3819, 0),
            facing = Facing.W,
            standingTile = Tile(1652, 3819, 0),
            area = Area.NORTHEAST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 83,
            shelfObjId = 27992,
            objTile = Tile(1651, 3820, 0),
            facing = Facing.W,
            standingTile = Tile(1652, 3820, 0),
            area = Area.NORTHEAST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 84,
            shelfObjId = 27996,
            objTile = Tile(1650, 3821, 0),
            facing = Facing.E,
            standingTile = Tile(1649, 3821, 0),
            area = Area.NORTHEAST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 85,
            shelfObjId = 28295,
            objTile = Tile(1650, 3819, 0),
            facing = Facing.E,
            standingTile = Tile(1649, 3819, 0),
            area = Area.NORTHEAST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 86,
            shelfObjId = 27993,
            objTile = Tile(1650, 3816, 0),
            facing = Facing.E,
            standingTile = Tile(1649, 3816, 0),
            area = Area.NORTHEAST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 87,
            shelfObjId = 28183,
            objTile = Tile(1648, 3814, 0),
            facing = Facing.S,
            standingTile = Tile(1648, 3815, 0),
            area = Area.NORTHEAST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 88,
            shelfObjId = 27991,
            objTile = Tile(1646, 3814, 0),
            facing = Facing.S,
            standingTile = Tile(1646, 3815, 0),
            area = Area.NORTHEAST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 89,
            shelfObjId = 28100,
            objTile = Tile(1645, 3814, 0),
            facing = Facing.S,
            standingTile = Tile(1645, 3815, 0),
            area = Area.NORTHEAST,
            floor = 0
        ),
        Bookshelf(
            shelfIndex = 90,
            shelfObjId = 28177,
            objTile = Tile(1607, 3820, 1),
            facing = Facing.W,
            standingTile = Tile(1608, 3820, 1),
            area = Area.NORTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 91,
            shelfObjId = 28173,
            objTile = Tile(1607, 3821, 1),
            facing = Facing.W,
            standingTile = Tile(1608, 3821, 1),
            area = Area.NORTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 92,
            shelfObjId = 28263,
            objTile = Tile(1609, 3822, 1),
            facing = Facing.N,
            standingTile = Tile(1609, 3821, 1),
            area = Area.NORTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 93,
            shelfObjId = 27997,
            objTile = Tile(1612, 3823, 1),
            facing = Facing.S,
            standingTile = Tile(1612, 3824, 1),
            area = Area.NORTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 94,
            shelfObjId = 28035,
            objTile = Tile(1611, 3823, 1),
            facing = Facing.S,
            standingTile = Tile(1611, 3824, 1),
            area = Area.NORTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 95,
            shelfObjId = 28193,
            objTile = Tile(1607, 3824, 1),
            facing = Facing.W,
            standingTile = Tile(1608, 3824, 1),
            area = Area.NORTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 96,
            shelfObjId = 28295,
            objTile = Tile(1607, 3825, 1),
            facing = Facing.W,
            standingTile = Tile(1608, 3825, 1),
            area = Area.NORTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 97,
            shelfObjId = 28113,
            objTile = Tile(1607, 3827, 1),
            facing = Facing.W,
            standingTile = Tile(1608, 3827, 1),
            area = Area.NORTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 98,
            shelfObjId = 28171,
            objTile = Tile(1611, 3831, 1),
            facing = Facing.N,
            standingTile = Tile(1611, 3830, 1),
            area = Area.NORTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 99,
            shelfObjId = 28229,
            objTile = Tile(1612, 3831, 1),
            facing = Facing.N,
            standingTile = Tile(1612, 3830, 1),
            area = Area.NORTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 100,
            shelfObjId = 28331,
            objTile = Tile(1613, 3831, 1),
            facing = Facing.N,
            standingTile = Tile(1613, 3830, 1),
            area = Area.NORTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 101,
            shelfObjId = 28203,
            objTile = Tile(1617, 3831, 1),
            facing = Facing.N,
            standingTile = Tile(1617, 3830, 1),
            area = Area.NORTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 102,
            shelfObjId = 28299,
            objTile = Tile(1618, 3831, 1),
            facing = Facing.N,
            standingTile = Tile(1618, 3830, 1),
            area = Area.NORTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 103,
            shelfObjId = 28105,
            objTile = Tile(1620, 3831, 1),
            facing = Facing.N,
            standingTile = Tile(1620, 3830, 1),
            area = Area.NORTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 104,
            shelfObjId = 28177,
            objTile = Tile(1624, 3831, 1),
            facing = Facing.E,
            standingTile = Tile(1623, 3831, 1),
            area = Area.NORTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 105,
            shelfObjId = 28319,
            objTile = Tile(1624, 3829, 1),
            facing = Facing.E,
            standingTile = Tile(1623, 3829, 1),
            area = Area.NORTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 106,
            shelfObjId = 28203,
            objTile = Tile(1624, 3825, 1),
            facing = Facing.E,
            standingTile = Tile(1623, 3825, 1),
            area = Area.NORTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 107,
            shelfObjId = 28263,
            objTile = Tile(1624, 3824, 1),
            facing = Facing.E,
            standingTile = Tile(1623, 3824, 1),
            area = Area.NORTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 108,
            shelfObjId = 28177,
            objTile = Tile(1624, 3819, 1),
            facing = Facing.E,
            standingTile = Tile(1623, 3819, 1),
            area = Area.NORTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 109,
            shelfObjId = 28319,
            objTile = Tile(1624, 3817, 1),
            facing = Facing.E,
            standingTile = Tile(1623, 3817, 1),
            area = Area.NORTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 110,
            shelfObjId = 28001,
            objTile = Tile(1623, 3816, 1),
            facing = Facing.S,
            standingTile = Tile(1623, 3817, 1),
            area = Area.NORTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 111,
            shelfObjId = 28335,
            objTile = Tile(1621, 3816, 1),
            facing = Facing.S,
            standingTile = Tile(1621, 3817, 1),
            area = Area.NORTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 112,
            shelfObjId = 28171,
            objTile = Tile(1617, 3816, 1),
            facing = Facing.S,
            standingTile = Tile(1617, 3817, 1),
            area = Area.NORTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 113,
            shelfObjId = 28217,
            objTile = Tile(1616, 3816, 1),
            facing = Facing.S,
            standingTile = Tile(1616, 3817, 1),
            area = Area.NORTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 114,
            shelfObjId = 28013,
            objTile = Tile(1611, 3816, 1),
            facing = Facing.S,
            standingTile = Tile(1611, 3817, 1),
            area = Area.NORTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 115,
            shelfObjId = 27995,
            objTile = Tile(1609, 3816, 1),
            facing = Facing.S,
            standingTile = Tile(1609, 3817, 1),
            area = Area.NORTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 116,
            shelfObjId = 28099,
            objTile = Tile(1620, 3820, 1),
            facing = Facing.E,
            standingTile = Tile(1619, 3820, 1),
            area = Area.NORTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 117,
            shelfObjId = 28113,
            objTile = Tile(1620, 3822, 1),
            facing = Facing.E,
            standingTile = Tile(1619, 3822, 1),
            area = Area.NORTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 118,
            shelfObjId = 28295,
            objTile = Tile(1620, 3824, 1),
            facing = Facing.E,
            standingTile = Tile(1619, 3824, 1),
            area = Area.NORTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 119,
            shelfObjId = 28193,
            objTile = Tile(1620, 3825, 1),
            facing = Facing.E,
            standingTile = Tile(1619, 3825, 1),
            area = Area.NORTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 120,
            shelfObjId = 28203,
            objTile = Tile(1620, 3827, 1),
            facing = Facing.E,
            standingTile = Tile(1619, 3827, 1),
            area = Area.NORTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 121,
            shelfObjId = 28017,
            objTile = Tile(1621, 3826, 1),
            facing = Facing.W,
            standingTile = Tile(1622, 3826, 1),
            area = Area.NORTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 122,
            shelfObjId = 27991,
            objTile = Tile(1621, 3822, 1),
            facing = Facing.W,
            standingTile = Tile(1622, 3822, 1),
            area = Area.NORTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 123,
            shelfObjId = 28189,
            objTile = Tile(1621, 3820, 1),
            facing = Facing.W,
            standingTile = Tile(1622, 3820, 1),
            area = Area.NORTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 124,
            shelfObjId = 28021,
            objTile = Tile(1607, 3788, 1),
            facing = Facing.W,
            standingTile = Tile(1608, 3788, 1),
            area = Area.SOUTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 125,
            shelfObjId = 28111,
            objTile = Tile(1607, 3789, 1),
            facing = Facing.W,
            standingTile = Tile(1608, 3789, 1),
            area = Area.SOUTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 126,
            shelfObjId = 28289,
            objTile = Tile(1609, 3790, 1),
            facing = Facing.N,
            standingTile = Tile(1609, 3789, 1),
            area = Area.SOUTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 127,
            shelfObjId = 28139,
            objTile = Tile(1611, 3790, 1),
            facing = Facing.N,
            standingTile = Tile(1611, 3789, 1),
            area = Area.SOUTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 128,
            shelfObjId = 28173,
            objTile = Tile(1613, 3790, 1),
            facing = Facing.N,
            standingTile = Tile(1613, 3789, 1),
            area = Area.SOUTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 129,
            shelfObjId = 28193,
            objTile = Tile(1614, 3789, 1),
            facing = Facing.E,
            standingTile = Tile(1613, 3789, 1),
            area = Area.SOUTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 130,
            shelfObjId = 28017,
            objTile = Tile(1615, 3788, 1),
            facing = Facing.W,
            standingTile = Tile(1616, 3788, 1),
            area = Area.SOUTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 131,
            shelfObjId = 28095,
            objTile = Tile(1615, 3790, 1),
            facing = Facing.W,
            standingTile = Tile(1616, 3790, 1),
            area = Area.SOUTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 132,
            shelfObjId = 28197,
            objTile = Tile(1614, 3791, 1),
            facing = Facing.S,
            standingTile = Tile(1614, 3792, 1),
            area = Area.SOUTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 133,
            shelfObjId = 28249,
            objTile = Tile(1613, 3791, 1),
            facing = Facing.S,
            standingTile = Tile(1613, 3792, 1),
            area = Area.SOUTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 134,
            shelfObjId = 28127,
            objTile = Tile(1610, 3791, 1),
            facing = Facing.S,
            standingTile = Tile(1610, 3792, 1),
            area = Area.SOUTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 135,
            shelfObjId = 28189,
            objTile = Tile(1609, 3791, 1),
            facing = Facing.S,
            standingTile = Tile(1609, 3792, 1),
            area = Area.SOUTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 136,
            shelfObjId = 28301,
            objTile = Tile(1608, 3791, 1),
            facing = Facing.S,
            standingTile = Tile(1608, 3792, 1),
            area = Area.SOUTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 137,
            shelfObjId = 28115,
            objTile = Tile(1607, 3793, 1),
            facing = Facing.W,
            standingTile = Tile(1608, 3793, 1),
            area = Area.SOUTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 138,
            shelfObjId = 28025,
            objTile = Tile(1607, 3794, 1),
            facing = Facing.W,
            standingTile = Tile(1608, 3794, 1),
            area = Area.SOUTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 139,
            shelfObjId = 28193,
            objTile = Tile(1608, 3799, 1),
            facing = Facing.N,
            standingTile = Tile(1608, 3798, 1),
            area = Area.SOUTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 140,
            shelfObjId = 28313,
            objTile = Tile(1610, 3799, 1),
            facing = Facing.N,
            standingTile = Tile(1610, 3798, 1),
            area = Area.SOUTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 141,
            shelfObjId = 28283,
            objTile = Tile(1615, 3799, 1),
            facing = Facing.N,
            standingTile = Tile(1615, 3798, 1),
            area = Area.SOUTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 142,
            shelfObjId = 27991,
            objTile = Tile(1616, 3799, 1),
            facing = Facing.N,
            standingTile = Tile(1616, 3798, 1),
            area = Area.SOUTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 143,
            shelfObjId = 28013,
            objTile = Tile(1621, 3799, 1),
            facing = Facing.N,
            standingTile = Tile(1621, 3798, 1),
            area = Area.SOUTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 144,
            shelfObjId = 28131,
            objTile = Tile(1623, 3799, 1),
            facing = Facing.N,
            standingTile = Tile(1623, 3798, 1),
            area = Area.SOUTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 145,
            shelfObjId = 28185,
            objTile = Tile(1624, 3798, 1),
            facing = Facing.E,
            standingTile = Tile(1623, 3798, 1),
            area = Area.SOUTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 146,
            shelfObjId = 28271,
            objTile = Tile(1624, 3796, 1),
            facing = Facing.E,
            standingTile = Tile(1623, 3796, 1),
            area = Area.SOUTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 147,
            shelfObjId = 28009,
            objTile = Tile(1624, 3792, 1),
            facing = Facing.E,
            standingTile = Tile(1623, 3792, 1),
            area = Area.SOUTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 148,
            shelfObjId = 28225,
            objTile = Tile(1624, 3791, 1),
            facing = Facing.E,
            standingTile = Tile(1623, 3791, 1),
            area = Area.SOUTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 149,
            shelfObjId = 28203,
            objTile = Tile(1623, 3789, 1),
            facing = Facing.S,
            standingTile = Tile(1623, 3790, 1),
            area = Area.SOUTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 150,
            shelfObjId = 28313,
            objTile = Tile(1621, 3789, 1),
            facing = Facing.S,
            standingTile = Tile(1621, 3790, 1),
            area = Area.SOUTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 151,
            shelfObjId = 28035,
            objTile = Tile(1620, 3788, 1),
            facing = Facing.N,
            standingTile = Tile(1620, 3787, 1),
            area = Area.SOUTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 152,
            shelfObjId = 27994,
            objTile = Tile(1621, 3788, 1),
            facing = Facing.N,
            standingTile = Tile(1621, 3787, 1),
            area = Area.SOUTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 153,
            shelfObjId = 28199,
            objTile = Tile(1624, 3787, 1),
            facing = Facing.E,
            standingTile = Tile(1623, 3787, 1),
            area = Area.SOUTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 154,
            shelfObjId = 28251,
            objTile = Tile(1624, 3786, 1),
            facing = Facing.E,
            standingTile = Tile(1623, 3786, 1),
            area = Area.SOUTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 155,
            shelfObjId = 28223,
            objTile = Tile(1619, 3784, 1),
            facing = Facing.S,
            standingTile = Tile(1619, 3785, 1),
            area = Area.SOUTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 156,
            shelfObjId = 28317,
            objTile = Tile(1618, 3784, 1),
            facing = Facing.S,
            standingTile = Tile(1618, 3785, 1),
            area = Area.SOUTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 157,
            shelfObjId = 27991,
            objTile = Tile(1616, 3784, 1),
            facing = Facing.S,
            standingTile = Tile(1616, 3785, 1),
            area = Area.SOUTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 158,
            shelfObjId = 28039,
            objTile = Tile(1612, 3784, 1),
            facing = Facing.S,
            standingTile = Tile(1612, 3785, 1),
            area = Area.SOUTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 159,
            shelfObjId = 28107,
            objTile = Tile(1611, 3784, 1),
            facing = Facing.S,
            standingTile = Tile(1611, 3785, 1),
            area = Area.SOUTHWEST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 160,
            shelfObjId = 28006,
            objTile = Tile(1625, 3801, 1),
            facing = Facing.W,
            standingTile = Tile(1626, 3801, 1),
            area = Area.CENTRAL,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 161,
            shelfObjId = 27993,
            objTile = Tile(1625, 3802, 1),
            facing = Facing.W,
            standingTile = Tile(1626, 3802, 1),
            area = Area.CENTRAL,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 162,
            shelfObjId = 28008,
            objTile = Tile(1625, 3803, 1),
            facing = Facing.W,
            standingTile = Tile(1626, 3803, 1),
            area = Area.CENTRAL,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 163,
            shelfObjId = 28120,
            objTile = Tile(1625, 3804, 1),
            facing = Facing.W,
            standingTile = Tile(1626, 3804, 1),
            area = Area.CENTRAL,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 164,
            shelfObjId = 28194,
            objTile = Tile(1625, 3806, 1),
            facing = Facing.W,
            standingTile = Tile(1626, 3806, 1),
            area = Area.CENTRAL,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 165,
            shelfObjId = 28336,
            objTile = Tile(1625, 3807, 1),
            facing = Facing.W,
            standingTile = Tile(1626, 3807, 1),
            area = Area.CENTRAL,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 166,
            shelfObjId = 28026,
            objTile = Tile(1625, 3808, 1),
            facing = Facing.W,
            standingTile = Tile(1626, 3808, 1),
            area = Area.CENTRAL,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 167,
            shelfObjId = 28130,
            objTile = Tile(1625, 3809, 1),
            facing = Facing.W,
            standingTile = Tile(1626, 3809, 1),
            area = Area.CENTRAL,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 168,
            shelfObjId = 28192,
            objTile = Tile(1625, 3811, 1),
            facing = Facing.W,
            standingTile = Tile(1626, 3811, 1),
            area = Area.CENTRAL,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 169,
            shelfObjId = 28298,
            objTile = Tile(1625, 3812, 1),
            facing = Facing.W,
            standingTile = Tile(1626, 3812, 1),
            area = Area.CENTRAL,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 170,
            shelfObjId = 28186,
            objTile = Tile(1625, 3813, 1),
            facing = Facing.W,
            standingTile = Tile(1626, 3813, 1),
            area = Area.CENTRAL,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 171,
            shelfObjId = 28173,
            objTile = Tile(1625, 3814, 1),
            facing = Facing.W,
            standingTile = Tile(1626, 3814, 1),
            area = Area.CENTRAL,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 172,
            shelfObjId = 28022,
            objTile = Tile(1626, 3815, 1),
            facing = Facing.N,
            standingTile = Tile(1626, 3814, 1),
            area = Area.CENTRAL,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 173,
            shelfObjId = 28152,
            objTile = Tile(1627, 3815, 1),
            facing = Facing.N,
            standingTile = Tile(1627, 3814, 1),
            area = Area.CENTRAL,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 174,
            shelfObjId = 28194,
            objTile = Tile(1631, 3815, 1),
            facing = Facing.N,
            standingTile = Tile(1631, 3814, 1),
            area = Area.CENTRAL,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 175,
            shelfObjId = 28296,
            objTile = Tile(1632, 3815, 1),
            facing = Facing.N,
            standingTile = Tile(1632, 3814, 1),
            area = Area.CENTRAL,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 176,
            shelfObjId = 28200,
            objTile = Tile(1633, 3815, 1),
            facing = Facing.N,
            standingTile = Tile(1633, 3814, 1),
            area = Area.CENTRAL,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 177,
            shelfObjId = 28294,
            objTile = Tile(1634, 3815, 1),
            facing = Facing.N,
            standingTile = Tile(1634, 3814, 1),
            area = Area.CENTRAL,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 178,
            shelfObjId = 28188,
            objTile = Tile(1638, 3815, 1),
            facing = Facing.N,
            standingTile = Tile(1638, 3814, 1),
            area = Area.CENTRAL,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 179,
            shelfObjId = 28300,
            objTile = Tile(1639, 3815, 1),
            facing = Facing.N,
            standingTile = Tile(1639, 3814, 1),
            area = Area.CENTRAL,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 180,
            shelfObjId = 28006,
            objTile = Tile(1640, 3814, 1),
            facing = Facing.E,
            standingTile = Tile(1639, 3814, 1),
            area = Area.CENTRAL,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 181,
            shelfObjId = 27993,
            objTile = Tile(1640, 3813, 1),
            facing = Facing.E,
            standingTile = Tile(1639, 3813, 1),
            area = Area.CENTRAL,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 182,
            shelfObjId = 27851,
            objTile = Tile(1640, 3803, 1),
            facing = Facing.E,
            standingTile = Tile(1639, 3803, 1),
            area = Area.CENTRAL,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 183,
            shelfObjId = 28052,
            objTile = Tile(1640, 3802, 1),
            facing = Facing.E,
            standingTile = Tile(1639, 3802, 1),
            area = Area.CENTRAL,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 184,
            shelfObjId = 28138,
            objTile = Tile(1640, 3801, 1),
            facing = Facing.E,
            standingTile = Tile(1639, 3801, 1),
            area = Area.CENTRAL,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 185,
            shelfObjId = 28202,
            objTile = Tile(1639, 3800, 1),
            facing = Facing.S,
            standingTile = Tile(1639, 3801, 1),
            area = Area.CENTRAL,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 186,
            shelfObjId = 28332,
            objTile = Tile(1638, 3800, 1),
            facing = Facing.S,
            standingTile = Tile(1638, 3801, 1),
            area = Area.CENTRAL,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 187,
            shelfObjId = 28010,
            objTile = Tile(1634, 3800, 1),
            facing = Facing.S,
            standingTile = Tile(1634, 3801, 1),
            area = Area.CENTRAL,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 188,
            shelfObjId = 28110,
            objTile = Tile(1633, 3800, 1),
            facing = Facing.S,
            standingTile = Tile(1633, 3801, 1),
            area = Area.CENTRAL,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 189,
            shelfObjId = 28024,
            objTile = Tile(1632, 3800, 1),
            facing = Facing.S,
            standingTile = Tile(1632, 3801, 1),
            area = Area.CENTRAL,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 190,
            shelfObjId = 28168,
            objTile = Tile(1631, 3800, 1),
            facing = Facing.S,
            standingTile = Tile(1631, 3801, 1),
            area = Area.CENTRAL,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 191,
            shelfObjId = 28012,
            objTile = Tile(1627, 3800, 1),
            facing = Facing.S,
            standingTile = Tile(1627, 3801, 1),
            area = Area.CENTRAL,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 192,
            shelfObjId = 28158,
            objTile = Tile(1626, 3800, 1),
            facing = Facing.S,
            standingTile = Tile(1626, 3801, 1),
            area = Area.CENTRAL,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 193,
            shelfObjId = 28017,
            objTile = Tile(1641, 3817, 1),
            facing = Facing.W,
            standingTile = Tile(1642, 3817, 1),
            area = Area.NORTHEAST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 194,
            shelfObjId = 28069,
            objTile = Tile(1641, 3818, 1),
            facing = Facing.W,
            standingTile = Tile(1642, 3818, 1),
            area = Area.NORTHEAST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 195,
            shelfObjId = 28095,
            objTile = Tile(1641, 3819, 1),
            facing = Facing.W,
            standingTile = Tile(1642, 3819, 1),
            area = Area.NORTHEAST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 196,
            shelfObjId = 28289,
            objTile = Tile(1641, 3824, 1),
            facing = Facing.W,
            standingTile = Tile(1642, 3824, 1),
            area = Area.NORTHEAST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 197,
            shelfObjId = 27991,
            objTile = Tile(1641, 3825, 1),
            facing = Facing.W,
            standingTile = Tile(1642, 3825, 1),
            area = Area.NORTHEAST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 198,
            shelfObjId = 28249,
            objTile = Tile(1641, 3829, 1),
            facing = Facing.W,
            standingTile = Tile(1642, 3829, 1),
            area = Area.NORTHEAST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 199,
            shelfObjId = 28015,
            objTile = Tile(1645, 3831, 1),
            facing = Facing.N,
            standingTile = Tile(1645, 3830, 1),
            area = Area.NORTHEAST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 200,
            shelfObjId = 28103,
            objTile = Tile(1646, 3831, 1),
            facing = Facing.N,
            standingTile = Tile(1646, 3830, 1),
            area = Area.NORTHEAST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 201,
            shelfObjId = 28185,
            objTile = Tile(1647, 3831, 1),
            facing = Facing.N,
            standingTile = Tile(1647, 3830, 1),
            area = Area.NORTHEAST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 202,
            shelfObjId = 28173,
            objTile = Tile(1648, 3831, 1),
            facing = Facing.N,
            standingTile = Tile(1648, 3830, 1),
            area = Area.NORTHEAST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 203,
            shelfObjId = 28007,
            objTile = Tile(1649, 3830, 1),
            facing = Facing.E,
            standingTile = Tile(1648, 3830, 1),
            area = Area.NORTHEAST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 204,
            shelfObjId = 28093,
            objTile = Tile(1649, 3828, 1),
            facing = Facing.E,
            standingTile = Tile(1648, 3828, 1),
            area = Area.NORTHEAST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 205,
            shelfObjId = 28172,
            objTile = Tile(1650, 3829, 1),
            facing = Facing.W,
            standingTile = Tile(1651, 3829, 1),
            area = Area.NORTHEAST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 206,
            shelfObjId = 28307,
            objTile = Tile(1652, 3831, 1),
            facing = Facing.N,
            standingTile = Tile(1652, 3830, 1),
            area = Area.NORTHEAST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 207,
            shelfObjId = 28017,
            objTile = Tile(1653, 3831, 1),
            facing = Facing.N,
            standingTile = Tile(1653, 3830, 1),
            area = Area.NORTHEAST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 208,
            shelfObjId = 28185,
            objTile = Tile(1658, 3827, 1),
            facing = Facing.E,
            standingTile = Tile(1657, 3827, 1),
            area = Area.NORTHEAST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 209,
            shelfObjId = 28173,
            objTile = Tile(1658, 3826, 1),
            facing = Facing.E,
            standingTile = Tile(1657, 3826, 1),
            area = Area.NORTHEAST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 210,
            shelfObjId = 28189,
            objTile = Tile(1658, 3823, 1),
            facing = Facing.E,
            standingTile = Tile(1657, 3823, 1),
            area = Area.NORTHEAST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 211,
            shelfObjId = 28301,
            objTile = Tile(1658, 3822, 1),
            facing = Facing.E,
            standingTile = Tile(1657, 3822, 1),
            area = Area.NORTHEAST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 212,
            shelfObjId = 28003,
            objTile = Tile(1658, 3821, 1),
            facing = Facing.E,
            standingTile = Tile(1657, 3821, 1),
            area = Area.NORTHEAST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 213,
            shelfObjId = 27993,
            objTile = Tile(1658, 3820, 1),
            facing = Facing.E,
            standingTile = Tile(1657, 3820, 1),
            area = Area.NORTHEAST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 214,
            shelfObjId = 28229,
            objTile = Tile(1656, 3816, 1),
            facing = Facing.S,
            standingTile = Tile(1656, 3817, 1),
            area = Area.NORTHEAST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 215,
            shelfObjId = 28331,
            objTile = Tile(1655, 3816, 1),
            facing = Facing.S,
            standingTile = Tile(1655, 3817, 1),
            area = Area.NORTHEAST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 216,
            shelfObjId = 28193,
            objTile = Tile(1651, 3816, 1),
            facing = Facing.S,
            standingTile = Tile(1651, 3817, 1),
            area = Area.NORTHEAST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 217,
            shelfObjId = 28019,
            objTile = Tile(1649, 3816, 1),
            facing = Facing.S,
            standingTile = Tile(1649, 3817, 1),
            area = Area.NORTHEAST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 218,
            shelfObjId = 28113,
            objTile = Tile(1648, 3816, 1),
            facing = Facing.S,
            standingTile = Tile(1648, 3817, 1),
            area = Area.NORTHEAST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 219,
            shelfObjId = 28017,
            objTile = Tile(1644, 3816, 1),
            facing = Facing.S,
            standingTile = Tile(1644, 3817, 1),
            area = Area.NORTHEAST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 220,
            shelfObjId = 28045,
            objTile = Tile(1643, 3816, 1),
            facing = Facing.S,
            standingTile = Tile(1643, 3817, 1),
            area = Area.NORTHEAST,
            floor = 1
        ),
        Bookshelf(
            shelfIndex = 221,
            shelfObjId = 28002,
            objTile = Tile(1607, 3785, 2),
            facing = Facing.W,
            standingTile = Tile(1608, 3785, 2),
            area = Area.SOUTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 222,
            shelfObjId = 28050,
            objTile = Tile(1607, 3786, 2),
            facing = Facing.W,
            standingTile = Tile(1608, 3786, 2),
            area = Area.SOUTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 223,
            shelfObjId = 28358,
            objTile = Tile(1607, 3796, 2),
            facing = Facing.W,
            standingTile = Tile(1608, 3796, 2),
            area = Area.SOUTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 224,
            shelfObjId = 28041,
            objTile = Tile(1607, 3797, 2),
            facing = Facing.W,
            standingTile = Tile(1608, 3797, 2),
            area = Area.SOUTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 225,
            shelfObjId = 28010,
            objTile = Tile(1608, 3799, 2),
            facing = Facing.N,
            standingTile = Tile(1608, 3798, 2),
            area = Area.SOUTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 226,
            shelfObjId = 28171,
            objTile = Tile(1610, 3799, 2),
            facing = Facing.N,
            standingTile = Tile(1610, 3798, 2),
            area = Area.SOUTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 227,
            shelfObjId = 28281,
            objTile = Tile(1611, 3799, 2),
            facing = Facing.N,
            standingTile = Tile(1611, 3798, 2),
            area = Area.SOUTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 228,
            shelfObjId = 28010,
            objTile = Tile(1618, 3799, 2),
            facing = Facing.N,
            standingTile = Tile(1618, 3798, 2),
            area = Area.SOUTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 229,
            shelfObjId = 28171,
            objTile = Tile(1621, 3799, 2),
            facing = Facing.N,
            standingTile = Tile(1621, 3798, 2),
            area = Area.SOUTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 230,
            shelfObjId = 28177,
            objTile = Tile(1624, 3797, 2),
            facing = Facing.E,
            standingTile = Tile(1623, 3797, 2),
            area = Area.SOUTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 231,
            shelfObjId = 28350,
            objTile = Tile(1624, 3795, 2),
            facing = Facing.E,
            standingTile = Tile(1623, 3795, 2),
            area = Area.SOUTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 232,
            shelfObjId = 28013,
            objTile = Tile(1624, 3794, 2),
            facing = Facing.E,
            standingTile = Tile(1623, 3794, 2),
            area = Area.SOUTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 233,
            shelfObjId = 27944,
            objTile = Tile(1624, 3792, 2),
            facing = Facing.E,
            standingTile = Tile(1623, 3792, 2),
            area = Area.SOUTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 234,
            shelfObjId = 28206,
            objTile = Tile(1623, 3791, 2),
            facing = Facing.S,
            standingTile = Tile(1623, 3792, 2),
            area = Area.SOUTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 235,
            shelfObjId = 28310,
            objTile = Tile(1622, 3791, 2),
            facing = Facing.S,
            standingTile = Tile(1622, 3792, 2),
            area = Area.SOUTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 236,
            shelfObjId = 28006,
            objTile = Tile(1618, 3792, 2),
            facing = Facing.W,
            standingTile = Tile(1619, 3792, 2),
            area = Area.SOUTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 237,
            shelfObjId = 28062,
            objTile = Tile(1618, 3793, 2),
            facing = Facing.W,
            standingTile = Tile(1619, 3793, 2),
            area = Area.SOUTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 238,
            shelfObjId = 28113,
            objTile = Tile(1618, 3794, 2),
            facing = Facing.W,
            standingTile = Tile(1619, 3794, 2),
            area = Area.SOUTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 239,
            shelfObjId = 28102,
            objTile = Tile(1617, 3793, 2),
            facing = Facing.E,
            standingTile = Tile(1616, 3793, 2),
            area = Area.SOUTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 240,
            shelfObjId = 28013,
            objTile = Tile(1617, 3792, 2),
            facing = Facing.E,
            standingTile = Tile(1616, 3792, 2),
            area = Area.SOUTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 241,
            shelfObjId = 27998,
            objTile = Tile(1618, 3790, 2),
            facing = Facing.N,
            standingTile = Tile(1618, 3789, 2),
            area = Area.SOUTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 242,
            shelfObjId = 28145,
            objTile = Tile(1620, 3790, 2),
            facing = Facing.N,
            standingTile = Tile(1620, 3789, 2),
            area = Area.SOUTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 243,
            shelfObjId = 28010,
            objTile = Tile(1622, 3790, 2),
            facing = Facing.N,
            standingTile = Tile(1622, 3789, 2),
            area = Area.SOUTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 244,
            shelfObjId = 28194,
            objTile = Tile(1624, 3789, 2),
            facing = Facing.E,
            standingTile = Tile(1623, 3789, 2),
            area = Area.SOUTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 245,
            shelfObjId = 28253,
            objTile = Tile(1624, 3788, 2),
            facing = Facing.E,
            standingTile = Tile(1623, 3788, 2),
            area = Area.SOUTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 246,
            shelfObjId = 28171,
            objTile = Tile(1624, 3786, 2),
            facing = Facing.E,
            standingTile = Tile(1623, 3786, 2),
            area = Area.SOUTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 247,
            shelfObjId = 28281,
            objTile = Tile(1624, 3785, 2),
            facing = Facing.E,
            standingTile = Tile(1623, 3785, 2),
            area = Area.SOUTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 248,
            shelfObjId = 27997,
            objTile = Tile(1623, 3784, 2),
            facing = Facing.S,
            standingTile = Tile(1623, 3785, 2),
            area = Area.SOUTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 249,
            shelfObjId = 28170,
            objTile = Tile(1621, 3784, 2),
            facing = Facing.S,
            standingTile = Tile(1621, 3785, 2),
            area = Area.SOUTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 250,
            shelfObjId = 28110,
            objTile = Tile(1611, 3784, 2),
            facing = Facing.S,
            standingTile = Tile(1611, 3785, 2),
            area = Area.SOUTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 251,
            shelfObjId = 28171,
            objTile = Tile(1609, 3784, 2),
            facing = Facing.S,
            standingTile = Tile(1609, 3785, 2),
            area = Area.SOUTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 252,
            shelfObjId = 28113,
            objTile = Tile(1612, 3789, 2),
            facing = Facing.E,
            standingTile = Tile(1611, 3789, 2),
            area = Area.SOUTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 253,
            shelfObjId = 28006,
            objTile = Tile(1612, 3791, 2),
            facing = Facing.E,
            standingTile = Tile(1611, 3791, 2),
            area = Area.SOUTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 254,
            shelfObjId = 28177,
            objTile = Tile(1612, 3794, 2),
            facing = Facing.E,
            standingTile = Tile(1611, 3794, 2),
            area = Area.SOUTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 255,
            shelfObjId = 28073,
            objTile = Tile(1613, 3793, 2),
            facing = Facing.W,
            standingTile = Tile(1614, 3793, 2),
            area = Area.SOUTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 256,
            shelfObjId = 28013,
            objTile = Tile(1613, 3792, 2),
            facing = Facing.W,
            standingTile = Tile(1614, 3792, 2),
            area = Area.SOUTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 257,
            shelfObjId = 28350,
            objTile = Tile(1613, 3791, 2),
            facing = Facing.W,
            standingTile = Tile(1614, 3791, 2),
            area = Area.SOUTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 258,
            shelfObjId = 28158,
            objTile = Tile(1617, 3791, 2),
            facing = Facing.E,
            standingTile = Tile(1616, 3791, 2),
            area = Area.SOUTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 259,
            shelfObjId = 28102,
            objTile = Tile(1617, 3793, 2),
            facing = Facing.E,
            standingTile = Tile(1616, 3793, 2),
            area = Area.SOUTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 260,
            shelfObjId = 28113,
            objTile = Tile(1618, 3794, 2),
            facing = Facing.W,
            standingTile = Tile(1619, 3794, 2),
            area = Area.SOUTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 261,
            shelfObjId = 28006,
            objTile = Tile(1618, 3792, 2),
            facing = Facing.W,
            standingTile = Tile(1619, 3792, 2),
            area = Area.SOUTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 262,
            shelfObjId = 28301,
            objTile = Tile(1619, 3791, 2),
            facing = Facing.S,
            standingTile = Tile(1619, 3791, 2),
            area = Area.SOUTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 263,
            shelfObjId = 28206,
            objTile = Tile(1623, 3791, 2),
            facing = Facing.S,
            standingTile = Tile(1623, 3792, 2),
            area = Area.SOUTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 264,
            shelfObjId = 28110,
            objTile = Tile(1623, 3790, 2),
            facing = Facing.N,
            standingTile = Tile(1623, 3789, 2),
            area = Area.SOUTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 265,
            shelfObjId = 28010,
            objTile = Tile(1622, 3790, 2),
            facing = Facing.N,
            standingTile = Tile(1622, 3789, 2),
            area = Area.SOUTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 266,
            shelfObjId = 28057,
            objTile = Tile(1619, 3790, 2),
            facing = Facing.N,
            standingTile = Tile(1619, 3789, 2),
            area = Area.SOUTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 267,
            shelfObjId = 28010,
            objTile = Tile(1611, 3816, 2),
            facing = Facing.S,
            standingTile = Tile(1611, 3817, 2),
            area = Area.NORTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 268,
            shelfObjId = 28110,
            objTile = Tile(1610, 3816, 2),
            facing = Facing.S,
            standingTile = Tile(1610, 3817, 2),
            area = Area.NORTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 269,
            shelfObjId = 28171,
            objTile = Tile(1609, 3816, 2),
            facing = Facing.S,
            standingTile = Tile(1609, 3817, 2),
            area = Area.NORTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 270,
            shelfObjId = 27997,
            objTile = Tile(1607, 3817, 2),
            facing = Facing.W,
            standingTile = Tile(1608, 3817, 2),
            area = Area.NORTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 271,
            shelfObjId = 28090,
            objTile = Tile(1607, 3819, 2),
            facing = Facing.W,
            standingTile = Tile(1608, 3819, 2),
            area = Area.NORTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 272,
            shelfObjId = 28357,
            objTile = Tile(1607, 3829, 2),
            facing = Facing.W,
            standingTile = Tile(1608, 3829, 2),
            area = Area.NORTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 273,
            shelfObjId = 28013,
            objTile = Tile(1608, 3831, 2),
            facing = Facing.N,
            standingTile = Tile(1608, 3830, 2),
            area = Area.NORTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 274,
            shelfObjId = 27994,
            objTile = Tile(1610, 3831, 2),
            facing = Facing.N,
            standingTile = Tile(1610, 3830, 2),
            area = Area.NORTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 275,
            shelfObjId = 28010,
            objTile = Tile(1611, 3831, 2),
            facing = Facing.N,
            standingTile = Tile(1611, 3830, 2),
            area = Area.NORTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 276,
            shelfObjId = 28050,
            objTile = Tile(1622, 3831, 2),
            facing = Facing.N,
            standingTile = Tile(1622, 3830, 2),
            area = Area.NORTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 277,
            shelfObjId = 28157,
            objTile = Tile(1623, 3831, 2),
            facing = Facing.N,
            standingTile = Tile(1623, 3830, 2),
            area = Area.NORTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 278,
            shelfObjId = 28053,
            objTile = Tile(1624, 3829, 2),
            facing = Facing.E,
            standingTile = Tile(1623, 3829, 2),
            area = Area.NORTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 279,
            shelfObjId = 28134,
            objTile = Tile(1624, 3828, 2),
            facing = Facing.E,
            standingTile = Tile(1623, 3828, 2),
            area = Area.NORTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 280,
            shelfObjId = 28010,
            objTile = Tile(1624, 3821, 2),
            facing = Facing.E,
            standingTile = Tile(1623, 3821, 2),
            area = Area.NORTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 281,
            shelfObjId = 28171,
            objTile = Tile(1624, 3819, 2),
            facing = Facing.E,
            standingTile = Tile(1623, 3819, 2),
            area = Area.NORTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 282,
            shelfObjId = 28310,
            objTile = Tile(1622, 3816, 2),
            facing = Facing.S,
            standingTile = Tile(1622, 3817, 2),
            area = Area.NORTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 283,
            shelfObjId = 28181,
            objTile = Tile(1620, 3816, 2),
            facing = Facing.S,
            standingTile = Tile(1620, 3817, 2),
            area = Area.NORTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 284,
            shelfObjId = 28174,
            objTile = Tile(1618, 3816, 2),
            facing = Facing.S,
            standingTile = Tile(1618, 3817, 2),
            area = Area.NORTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 285,
            shelfObjId = 28309,
            objTile = Tile(1615, 3821, 2),
            facing = Facing.S,
            standingTile = Tile(1615, 3822, 2),
            area = Area.NORTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 286,
            shelfObjId = 28294,
            objTile = Tile(1617, 3821, 2),
            facing = Facing.S,
            standingTile = Tile(1617, 3822, 2),
            area = Area.NORTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 287,
            shelfObjId = 28301,
            objTile = Tile(1619, 3822, 2),
            facing = Facing.E,
            standingTile = Tile(1618, 3822, 2),
            area = Area.NORTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 288,
            shelfObjId = 28310,
            objTile = Tile(1619, 3824, 2),
            facing = Facing.E,
            standingTile = Tile(1618, 3824, 2),
            area = Area.NORTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 289,
            shelfObjId = 28110,
            objTile = Tile(1618, 3826, 2),
            facing = Facing.N,
            standingTile = Tile(1618, 3825, 2),
            area = Area.NORTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 290,
            shelfObjId = 28010,
            objTile = Tile(1617, 3826, 2),
            facing = Facing.N,
            standingTile = Tile(1617, 3825, 2),
            area = Area.NORTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 291,
            shelfObjId = 28158,
            objTile = Tile(1615, 3827, 2),
            facing = Facing.S,
            standingTile = Tile(1615, 3828, 2),
            area = Area.NORTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 292,
            shelfObjId = 28013,
            objTile = Tile(1616, 3827, 2),
            facing = Facing.S,
            standingTile = Tile(1616, 3828, 2),
            area = Area.NORTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 293,
            shelfObjId = 28070,
            objTile = Tile(1618, 3827, 2),
            facing = Facing.S,
            standingTile = Tile(1618, 3828, 2),
            area = Area.NORTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 294,
            shelfObjId = 28113,
            objTile = Tile(1620, 3826, 2),
            facing = Facing.W,
            standingTile = Tile(1621, 3826, 2),
            area = Area.NORTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 295,
            shelfObjId = 28006,
            objTile = Tile(1620, 3824, 2),
            facing = Facing.W,
            standingTile = Tile(1621, 3824, 2),
            area = Area.NORTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 296,
            shelfObjId = 28270,
            objTile = Tile(1620, 3822, 2),
            facing = Facing.W,
            standingTile = Tile(1621, 3822, 2),
            area = Area.NORTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 297,
            shelfObjId = 28177,
            objTile = Tile(1620, 3821, 2),
            facing = Facing.W,
            standingTile = Tile(1621, 3821, 2),
            area = Area.NORTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 298,
            shelfObjId = 28236,
            objTile = Tile(1619, 3820, 2),
            facing = Facing.N,
            standingTile = Tile(1619, 3819, 2),
            area = Area.NORTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 299,
            shelfObjId = 28177,
            objTile = Tile(1617, 3820, 2),
            facing = Facing.N,
            standingTile = Tile(1617, 3819, 2),
            area = Area.NORTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 300,
            shelfObjId = 27991,
            objTile = Tile(1615, 3820, 2),
            facing = Facing.N,
            standingTile = Tile(1615, 3819, 2),
            area = Area.NORTHWEST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 301,
            shelfObjId = 28201,
            objTile = Tile(1641, 3818, 2),
            facing = Facing.W,
            standingTile = Tile(1642, 3818, 2),
            area = Area.NORTHEAST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 302,
            shelfObjId = 28205,
            objTile = Tile(1641, 3820, 2),
            facing = Facing.W,
            standingTile = Tile(1642, 3820, 2),
            area = Area.NORTHEAST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 303,
            shelfObjId = 28309,
            objTile = Tile(1641, 3821, 2),
            facing = Facing.W,
            standingTile = Tile(1642, 3821, 2),
            area = Area.NORTHEAST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 304,
            shelfObjId = 28041,
            objTile = Tile(1641, 3829, 2),
            facing = Facing.W,
            standingTile = Tile(1642, 3829, 2),
            area = Area.NORTHEAST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 305,
            shelfObjId = 28246,
            objTile = Tile(1643, 3831, 2),
            facing = Facing.N,
            standingTile = Tile(1643, 3830, 2),
            area = Area.NORTHEAST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 306,
            shelfObjId = 28174,
            objTile = Tile(1644, 3831, 2),
            facing = Facing.N,
            standingTile = Tile(1644, 3830, 2),
            area = Area.NORTHEAST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 307,
            shelfObjId = 28110,
            objTile = Tile(1654, 3831, 2),
            facing = Facing.N,
            standingTile = Tile(1654, 3830, 2),
            area = Area.NORTHEAST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 308,
            shelfObjId = 28171,
            objTile = Tile(1656, 3831, 2),
            facing = Facing.N,
            standingTile = Tile(1656, 3830, 2),
            area = Area.NORTHEAST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 309,
            shelfObjId = 28002,
            objTile = Tile(1658, 3830, 2),
            facing = Facing.E,
            standingTile = Tile(1657, 3830, 2),
            area = Area.NORTHEAST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 310,
            shelfObjId = 28157,
            objTile = Tile(1658, 3828, 2),
            facing = Facing.E,
            standingTile = Tile(1657, 3828, 2),
            area = Area.NORTHEAST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 311,
            shelfObjId = 28090,
            objTile = Tile(1658, 3818, 2),
            facing = Facing.E,
            standingTile = Tile(1657, 3818, 2),
            area = Area.NORTHEAST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 312,
            shelfObjId = 28170,
            objTile = Tile(1658, 3817, 2),
            facing = Facing.E,
            standingTile = Tile(1657, 3817, 2),
            area = Area.NORTHEAST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 313,
            shelfObjId = 28246,
            objTile = Tile(1656, 3816, 2),
            facing = Facing.S,
            standingTile = Tile(1656, 3817, 2),
            area = Area.NORTHEAST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 314,
            shelfObjId = 28174,
            objTile = Tile(1655, 3816, 2),
            facing = Facing.S,
            standingTile = Tile(1655, 3817, 2),
            area = Area.NORTHEAST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 315,
            shelfObjId = 28229,
            objTile = Tile(1652, 3816, 2),
            facing = Facing.S,
            standingTile = Tile(1652, 3817, 2),
            area = Area.NORTHEAST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 316,
            shelfObjId = 28177,
            objTile = Tile(1648, 3817, 2),
            facing = Facing.W,
            standingTile = Tile(1649, 3817, 2),
            area = Area.NORTHEAST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 317,
            shelfObjId = 28350,
            objTile = Tile(1648, 3819, 2),
            facing = Facing.W,
            standingTile = Tile(1649, 3819, 2),
            area = Area.NORTHEAST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 318,
            shelfObjId = 28073,
            objTile = Tile(1648, 3821, 2),
            facing = Facing.W,
            standingTile = Tile(1649, 3821, 2),
            area = Area.NORTHEAST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 319,
            shelfObjId = 28206,
            objTile = Tile(1649, 3823, 2),
            facing = Facing.N,
            standingTile = Tile(1649, 3822, 2),
            area = Area.NORTHEAST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 320,
            shelfObjId = 28310,
            objTile = Tile(1650, 3823, 2),
            facing = Facing.N,
            standingTile = Tile(1650, 3822, 2),
            area = Area.NORTHEAST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 321,
            shelfObjId = 28189,
            objTile = Tile(1652, 3823, 2),
            facing = Facing.N,
            standingTile = Tile(1652, 3822, 2),
            area = Area.NORTHEAST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 322,
            shelfObjId = 28006,
            objTile = Tile(1654, 3822, 2),
            facing = Facing.E,
            standingTile = Tile(1653, 3822, 2),
            area = Area.NORTHEAST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 323,
            shelfObjId = 28113,
            objTile = Tile(1654, 3820, 2),
            facing = Facing.E,
            standingTile = Tile(1653, 3820, 2),
            area = Area.NORTHEAST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 324,
            shelfObjId = 27991,
            objTile = Tile(1655, 3820, 2),
            facing = Facing.W,
            standingTile = Tile(1656, 3820, 2),
            area = Area.NORTHEAST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 325,
            shelfObjId = 28102,
            objTile = Tile(1655, 3821, 2),
            facing = Facing.W,
            standingTile = Tile(1656, 3821, 2),
            area = Area.NORTHEAST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 326,
            shelfObjId = 28158,
            objTile = Tile(1655, 3823, 2),
            facing = Facing.W,
            standingTile = Tile(1656, 3823, 2),
            area = Area.NORTHEAST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 327,
            shelfObjId = 28057,
            objTile = Tile(1653, 3824, 2),
            facing = Facing.N,
            standingTile = Tile(1653, 3825, 2),
            area = Area.NORTHEAST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 328,
            shelfObjId = 28145,
            objTile = Tile(1652, 3824, 2),
            facing = Facing.N,
            standingTile = Tile(1652, 3825, 2),
            area = Area.NORTHEAST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 329,
            shelfObjId = 28253,
            objTile = Tile(1649, 3824, 2),
            facing = Facing.N,
            standingTile = Tile(1649, 3825, 2),
            area = Area.NORTHEAST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 330,
            shelfObjId = 28173,
            objTile = Tile(1648, 3824, 2),
            facing = Facing.N,
            standingTile = Tile(1648, 3825, 2),
            area = Area.NORTHEAST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 331,
            shelfObjId = 28070,
            objTile = Tile(1647, 3822, 2),
            facing = Facing.E,
            standingTile = Tile(1646, 3822, 2),
            area = Area.NORTHEAST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 332,
            shelfObjId = 28177,
            objTile = Tile(1647, 3820, 2),
            facing = Facing.E,
            standingTile = Tile(1646, 3820, 2),
            area = Area.NORTHEAST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 333,
            shelfObjId = 28326,
            objTile = Tile(1647, 3818, 2),
            facing = Facing.E,
            standingTile = Tile(1646, 3818, 2),
            area = Area.NORTHEAST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 334,
            shelfObjId = 28013,
            objTile = Tile(1645, 3816, 2),
            facing = Facing.S,
            standingTile = Tile(1645, 3817, 2),
            area = Area.NORTHEAST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 335,
            shelfObjId = 28053,
            objTile = Tile(1644, 3816, 2),
            facing = Facing.S,
            standingTile = Tile(1644, 3817, 2),
            area = Area.NORTHEAST,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 336,
            shelfObjId = 28010,
            objTile = Tile(1625, 3802, 2),
            facing = Facing.W,
            standingTile = Tile(1626, 3802, 2),
            area = Area.CENTRAL,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 337,
            shelfObjId = 28024,
            objTile = Tile(1625, 3804, 2),
            facing = Facing.W,
            standingTile = Tile(1626, 3804, 2),
            area = Area.CENTRAL,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 338,
            shelfObjId = 28298,
            objTile = Tile(1625, 3811, 2),
            facing = Facing.W,
            standingTile = Tile(1626, 3811, 2),
            area = Area.CENTRAL,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 339,
            shelfObjId = 28186,
            objTile = Tile(1625, 3812, 2),
            facing = Facing.W,
            standingTile = Tile(1626, 3812, 2),
            area = Area.CENTRAL,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 340,
            shelfObjId = 28010,
            objTile = Tile(1627, 3815, 2),
            facing = Facing.N,
            standingTile = Tile(1627, 3814, 2),
            area = Area.CENTRAL,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 341,
            shelfObjId = 28110,
            objTile = Tile(1628, 3815, 2),
            facing = Facing.N,
            standingTile = Tile(1628, 3814, 2),
            area = Area.CENTRAL,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 342,
            shelfObjId = 28194,
            objTile = Tile(1635, 3815, 2),
            facing = Facing.N,
            standingTile = Tile(1635, 3814, 2),
            area = Area.CENTRAL,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 343,
            shelfObjId = 28200,
            objTile = Tile(1637, 3815, 2),
            facing = Facing.N,
            standingTile = Tile(1637, 3814, 2),
            area = Area.CENTRAL,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 344,
            shelfObjId = 28294,
            objTile = Tile(1638, 3815, 2),
            facing = Facing.N,
            standingTile = Tile(1638, 3814, 2),
            area = Area.CENTRAL,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 345,
            shelfObjId = 28006,
            objTile = Tile(1640, 3813, 2),
            facing = Facing.E,
            standingTile = Tile(1639, 3813, 2),
            area = Area.CENTRAL,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 346,
            shelfObjId = 28008,
            objTile = Tile(1640, 3811, 2),
            facing = Facing.E,
            standingTile = Tile(1639, 3811, 2),
            area = Area.CENTRAL,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 347,
            shelfObjId = 28120,
            objTile = Tile(1640, 3810, 2),
            facing = Facing.E,
            standingTile = Tile(1639, 3810, 2),
            area = Area.CENTRAL,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 348,
            shelfObjId = 28202,
            objTile = Tile(1638, 3800, 2),
            facing = Facing.S,
            standingTile = Tile(1638, 3801, 2),
            area = Area.CENTRAL,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 349,
            shelfObjId = 28190,
            objTile = Tile(1632, 3800, 2),
            facing = Facing.S,
            standingTile = Tile(1632, 3801, 2),
            area = Area.CENTRAL,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 350,
            shelfObjId = 28312,
            objTile = Tile(1630, 3800, 2),
            facing = Facing.S,
            standingTile = Tile(1630, 3801, 2),
            area = Area.CENTRAL,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 351,
            shelfObjId = 28014,
            objTile = Tile(1629, 3800, 2),
            facing = Facing.S,
            standingTile = Tile(1629, 3801, 2),
            area = Area.CENTRAL,
            floor = 2
        ),
        Bookshelf(
            shelfIndex = 352,
            shelfObjId = 28138,
            objTile = Tile(1627, 3800, 2),
            facing = Facing.S,
            standingTile = Tile(1627, 3801, 2),
            area = Area.CENTRAL,
            floor = 2
        )
    )
    // Convenience index by shelfIndex
    val BY_INDEX: Map<Int, Bookshelf> = ALL.associateBy { it.shelfIndex }
}