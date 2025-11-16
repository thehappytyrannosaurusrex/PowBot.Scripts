package org.thehappytyrannosaurusrex.arceuuslibrary.pathing

import org.powbot.api.Tile
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.CollisionMap
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Locations
import org.thehappytyrannosaurusrex.arceuuslibrary.data.StairLandings

/**
 * Normalised tile representation for our pathfinder.
 *
 * Wraps (x, y, floor) so we can safely use it as a map/set key.
 */
data class NavTile(val x: Int, val y: Int, val floor: Int) {
    fun toTile(): Tile = Tile(x, y, floor)

    companion object {
        fun from(tile: Tile): NavTile = NavTile(tile.x, tile.y, tile.floor)
    }
}

/**
 * Step costs used by the pathfinder.
 *
 * Using 10 instead of 1 lets us tweak weights later without going into fractions.
 */
object StepCost {
    const val WALK: Int = 10         // cost of a normal ground step
    const val STAIR: Int = 20        // cost of a stair transition (per StairLink)
}

/**
 * Low-level navigation helpers for the Arceuus Library.
 *
 * This knows ONLY about tiles, bounds, collisions and stairs.
 * No bookshelf / NPC / request logic should live here.
 */
object LibraryNav {

    /**
     * Collision flag bits that make a tile unwalkable.
     *
     * These are standard OSRS clipping bits (blocked object / full block / floor dec).
     * If this feels too strict in practice, we can relax the mask later.
     */
    private const val UNWALKABLE_MASK: Int = 0x100 or 0x200000 or 0x40000

    /**
     * Is this tile in any of the library areas (on any floor)?
     */
    fun inLibrary(tile: Tile): Boolean = Locations.isInsideLibrary(tile)
    fun inLibrary(nav: NavTile): Boolean = inLibrary(nav.toTile())

    /**
     * Optional override for tests: if set, isWalkable(tile) will return this instead
     * of using Movement.collisionMap.
     *
     * In production this should remain null.
     */
    var testWalkableOverride: ((Tile) -> Boolean)? = null

    /**
     * Is the given tile walkable according to our bounds and the collision map?
     */
    fun isWalkable(tile: Tile): Boolean {
        // Test/debug hook: lets us bypass collision in tests or while debugging.
        testWalkableOverride?.let { override ->
            return override(tile)
        }

        // Hard bound: only ever walk inside our library definition
        if (!inLibrary(tile)) return false

        val cm: CollisionMap = Movement.collisionMap(tile.floor)

        // In your build this returns a 2D accessor you can index as flags2D[x][y].
        val flags2D = cm.flags()

        val localX = tile.localX()
        val localY = tile.localY()

        val flag: Int = try {
            flags2D[localX][localY]
        } catch (e: Exception) {
            // Outside loaded region or some other weirdness: treat as blocked.
            return false
        }

        // A tile is walkable if NONE of the "unwalkable" bits are set
        return (flag and UNWALKABLE_MASK) == 0
    }

    fun isWalkable(nav: NavTile): Boolean = isWalkable(nav.toTile())

    /**
     * 4-way (N/E/S/W) neighbours on the same floor.
     *
     * Returns a concrete List instead of a Sequence to avoid coroutine dependencies.
     */
    private fun orthogonalNeighbours(nav: NavTile): List<Pair<NavTile, Int>> {
        val result = mutableListOf<Pair<NavTile, Int>>()

        val floor = nav.floor
        val x = nav.x
        val y = nav.y

        val deltas = arrayOf(
            1 to 0,
            -1 to 0,
            0 to 1,
            0 to -1
        )

        for ((dx, dy) in deltas) {
            val neighbour = NavTile(x + dx, y + dy, floor)
            if (isWalkable(neighbour)) {
                result += neighbour to StepCost.WALK
            }
        }

        return result
    }

    /**
     * Stair neighbours: if this tile is a stair landing, we can also go
     * to the linked landing tile(s) on another floor.
     *
     * Returns a concrete List instead of a Sequence.
     */
    private fun stairNeighbours(nav: NavTile): List<Pair<NavTile, Int>> {
        val result = mutableListOf<Pair<NavTile, Int>>()
        val fromTile = nav.toTile()

        for (link in StairLandings.from(fromTile)) {
            val targetNav = NavTile.from(link.to)
            // Sanity check: keep stair transitions inside library.
            if (inLibrary(targetNav)) {
                result += targetNav to link.cost
            }
        }

        return result
    }

    /**
     * All neighbours reachable in one logical step (ground walk or stair climb).
     *
     * Returned as (neighbour, cost) pairs to support weighted A*.
     */
    fun neighboursOf(nav: NavTile): List<Pair<NavTile, Int>> {
        val result = mutableListOf<Pair<NavTile, Int>>()
        result += orthogonalNeighbours(nav)
        result += stairNeighbours(nav)
        return result
    }
}

/**
 * Heuristic for A* in the library.
 *
 * Uses Manhattan distance in tiles plus a penalty per floor difference,
 * all in the same cost units as g (multiples of StepCost.WALK).
 */
object LibraryHeuristic {

    fun estimate(from: NavTile, to: NavTile): Int {
        val dx = kotlin.math.abs(from.x - to.x)
        val dy = kotlin.math.abs(from.y - to.y)
        val dz = kotlin.math.abs(from.floor - to.floor)

        val tileCost = (dx + dy) * StepCost.WALK
        val floorPenalty = dz * (StepCost.STAIR * 2) // treat each floor change as expensive
        return tileCost + floorPenalty
    }
}
