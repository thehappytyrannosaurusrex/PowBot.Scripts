package org.thehappytyrannosaurusrex.arceuuslibrary.pathing

import org.powbot.api.Tile
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.CollisionMap
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Locations
import org.thehappytyrannosaurusrex.arceuuslibrary.data.StairLandings

data class NavTile(val x: Int, val y: Int, val floor: Int) {
    fun toTile(): Tile = Tile(x, y, floor)

    companion object {
        fun from(tile: Tile): NavTile = NavTile(tile.x, tile.y, tile.floor)
    }
}

object StepCost {
    const val WALK: Int = 10         // cost of a normal ground step
    const val STAIR: Int = 20        // cost of a stair transition (per StairLink)
}

object LibraryNav {

    private const val UNWALKABLE_MASK: Int = 0x100 or 0x200000 or 0x40000

    fun inLibrary(tile: Tile): Boolean = Locations.isInsideLibrary(tile)
    fun inLibrary(nav: NavTile): Boolean = inLibrary(nav.toTile())

    var testWalkableOverride: ((Tile) -> Boolean)? = null

    fun isWalkable(tile: Tile): Boolean {
        // Test/debug hook: lets bypass collision in tests or while debugging.
        testWalkableOverride?.let { override ->
            return override(tile)
        }

        // Hard bound: only ever walk inside library definition
        if (!inLibrary(tile)) return false

        val cm: CollisionMap = Movement.collisionMap(tile.floor)

        // In build returns a 2D accessor can index as flags2D[x][y].
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

    fun neighboursOf(nav: NavTile): List<Pair<NavTile, Int>> {
        val result = mutableListOf<Pair<NavTile, Int>>()
        result += orthogonalNeighbours(nav)
        result += stairNeighbours(nav)
        return result
    }
}

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
