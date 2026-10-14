plugins {
    id("base-conventions")
}

dependencies {
    implementation(libs.jackson.dataformat.toml)
    implementation(projects.api.pluginCommons)
    implementation(projects.api.death)
    implementation(projects.content.generic.genericLocs)
    implementation(projects.content.quest)
    implementation(projects.content.skills.slayer)
}
