/*
 * Project: Arceuus Library Script (PowBot)
 * File: DebugPath.kt
 * Purpose: Added review header and standardized logging to Logger.info.
 * Notes: Generated comments + logging normalization on 2025-11-12.
 */

package org.thehappytyrannosaurusrex.arceuuslibrary.debug

import org.thehappytyrannosaurusrex.arceuuslibrary.pathfinding.*
import org.thehappytyrannosaurusrex.arceuuslibrary.utils.Logger

/**
 * DebugPath: Core component of the Arceuus Library script.
 * Auto-generated doc stub (reviewed 2025-11-12).
 */
object DebugPath {
    fun describe(path: List<NodeId>): String =
        path.joinToString(" -> ") { "${it.kind.name.take(1)}(${it.x},${it.y},${it.z})#${it.ref}" }

    fun verifyAdjacency(graph: Graph, path: List<NodeId>): Boolean {
        for (i in 0 until path.size - 1) {
            val a = path[i]; val b = path[i + 1]
            val ok = graph.adj[a]?.any { it.to == b } == true
            if (!ok) return false
        }
        return true
    }
}