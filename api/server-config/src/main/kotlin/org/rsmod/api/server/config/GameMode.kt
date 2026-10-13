package org.rsmod.api.server.config

import com.fasterxml.jackson.annotation.JsonProperty

public enum class GameMode {
    @JsonProperty("normal") Normal,

    @JsonProperty("dodian") Dodian,
}
