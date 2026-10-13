plugins {
    id("base-conventions")
}

dependencies {
    implementation(libs.jackson.dataformat.toml)
    implementation(projects.api.pluginCommons)
    implementation(projects.content.generic.genericLocs)
}
