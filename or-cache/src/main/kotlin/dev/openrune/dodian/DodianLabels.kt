package dev.openrune.dodian

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.dataformat.toml.TomlMapper
import java.io.File

public class DodianLabels(public val keepOsrsLabels: Boolean, public val labels: List<Label>) {
    public data class Label(
        val text: String,
        val x: Int,
        val y: Int,
        val level: Int,
        val style: Style,
        val area: String,
    )

    public enum class Style(public val colour: Int, public val textSize: Int) {
        Region(0xFF981F, 2),
        Town(0xFFFFFF, 1),
        Place(0xFFFFFF, 0),
    }

    public companion object {
        public fun parse(file: File): DodianLabels {
            if (!file.exists()) {
                return DodianLabels(keepOsrsLabels = true, labels = emptyList())
            }
            val root = TomlMapper().readTree(file)
            val labels = root.path("label").map { node -> node.toLabel(file) }
            return DodianLabels(root.path("osrs_labels").asBoolean(true), labels)
        }

        private fun JsonNode.toLabel(file: File): Label {
            val text = require("text", file).asText()
            val styleName = path("style").asText("place")
            val style =
                Style.entries.firstOrNull { it.name.equals(styleName, ignoreCase = true) }
                    ?: error("$file: label '$text' has unknown style '$styleName' (region/town/place)")
            return Label(
                text = text,
                x = require("x", file).asInt(),
                y = require("y", file).asInt(),
                level = path("level").asInt(0),
                style = style,
                area = path("area").asText("main"),
            )
        }

        private fun JsonNode.require(field: String, file: File): JsonNode =
            get(field) ?: error("$file: every [[label]] needs '$field'")
    }
}
