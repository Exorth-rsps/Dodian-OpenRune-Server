package dev.openrune.dodian

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.dataformat.toml.TomlMapper
import java.io.File

public class DodianWorldAreas(public val surfaceName: String?, public val underground: Area?) {
    public data class Area(
        val id: Int,
        val name: String,
        val display: String,
        val originX: Int,
        val originY: Int,
        val originLevel: Int,
        val zoom: Int,
        val moves: List<Move>,
    ) {
        public fun destination(square: Int): Int {
            val x = square shr 8
            val y = square and 0xFF
            val move = moves.firstOrNull { it.contains(x, y) } ?: return square
            return ((move.toX + x - move.fromX) shl 8) or (move.toY + y - move.fromY)
        }
    }

    public data class Move(
        val fromX: Int,
        val fromY: Int,
        val toX: Int,
        val toY: Int,
        val width: Int,
        val height: Int,
    ) {
        public fun contains(x: Int, y: Int): Boolean =
            x in fromX until fromX + width && y in fromY until fromY + height
    }

    public companion object {
        public fun parse(file: File): DodianWorldAreas {
            if (!file.exists()) {
                return DodianWorldAreas(null, null)
            }
            val root = TomlMapper().readTree(file)
            val surface = root.get("surface_name")?.asText()
            val underground = root.get("underground")?.toArea(file)
            return DodianWorldAreas(surface, underground)
        }

        private fun JsonNode.toArea(file: File): Area {
            val origin = require("origin", file)
            val id = require("id", file).asInt()
            require(id != 0) { "$file: area id 0 is the surface" }
            return Area(
                id = id,
                name = require("name", file).asText(),
                display = require("display", file).asText(),
                originX = origin[0].asInt(),
                originY = origin[1].asInt(),
                originLevel = origin.get(2)?.asInt() ?: 0,
                zoom = path("zoom").asInt(100),
                moves = path("move").map { it.toMove(file) },
            )
        }

        private fun JsonNode.toMove(file: File): Move {
            val from = require("from", file)
            val to = require("to", file)
            val size = path("size")
            return Move(
                fromX = from[0].asInt(),
                fromY = from[1].asInt(),
                toX = to[0].asInt(),
                toY = to[1].asInt(),
                width = size.get(0)?.asInt() ?: 1,
                height = size.get(1)?.asInt() ?: 1,
            )
        }

        private fun JsonNode.require(field: String, file: File): JsonNode =
            get(field) ?: error("$file: [underground] needs '$field'")
    }
}
