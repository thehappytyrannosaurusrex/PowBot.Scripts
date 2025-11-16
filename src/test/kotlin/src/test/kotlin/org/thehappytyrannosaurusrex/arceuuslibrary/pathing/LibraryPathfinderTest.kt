package org.thehappytyrannosaurusrex.arceuuslibrary.pathing

import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.powbot.api.Tile
import org.thehappytyrannosaurusrex.arceuuslibrary.data.Locations

class LibraryPathfinderTest {

    @Before
    fun setup() {
        // In tests, treat every tile that's "inside library" as walkable.
        LibraryNav.testWalkableOverride = { tile ->
            Locations.isInsideLibrary(tile)
        }
    }

    @After
    fun tearDown() {
        LibraryNav.testWalkableOverride = null
    }

    @Test
    fun `straight-line path on same floor`() {
        val start = Tile(1630, 3800, 0)
        val goal = Tile(1635, 3800, 0)

        val path = LibraryPathfinder.findPath(start, goal)

        assertNotNull("Path should not be null", path)
        val tiles = path!!

        assertEquals("Path should start at start tile", start, tiles.first())
        assertEquals("Path should end at goal tile", goal, tiles.last())

        // All steps should be 4-way neighbours
        for (i in 0 until tiles.size - 1) {
            val a = tiles[i]
            val b = tiles[i + 1]
            assertEquals("Floor must stay the same", a.floor, b.floor)
            val dx = kotlin.math.abs(a.x - b.x)
            val dy = kotlin.math.abs(a.y - b.y)
            assertTrue("Steps must be 4-way neighbours", dx + dy == 1)
        }
    }

    @Test
    fun `blocked tile forces detour`() {
        val start = Tile(1630, 3800, 0)
        val goal = Tile(1632, 3800, 0)
        val blocked = Tile(1631, 3800, 0)

        // Override walkability to simulate a single blocked tile
        LibraryNav.testWalkableOverride = { tile ->
            Locations.isInsideLibrary(tile) && tile != blocked
        }

        val path = LibraryPathfinder.findPath(start, goal)
        assertNotNull("Path should not be null", path)
        val tiles = path!!

        // There should be no step that passes through the blocked tile
        assertFalse("Path must not contain blocked tile", tiles.contains(blocked))

        // And the path should still start and end correctly
        assertEquals(start, tiles.first())
        assertEquals(goal, tiles.last())
    }
}

