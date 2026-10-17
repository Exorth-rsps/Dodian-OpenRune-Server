plugins {
    id("base-conventions")
}

dependencies {
    implementation(libs.jackson.dataformat.toml)
    implementation(projects.api.pluginCommons)
    implementation(projects.api.death)
    implementation(projects.api.instances)
    implementation(projects.api.npc)
    implementation(projects.content.generic.genericLocs)
    implementation(projects.content.quest)
    implementation(projects.content.skills.herblore)
    implementation(projects.content.skills.runecrafting)
    implementation(projects.content.skills.slayer)
}
