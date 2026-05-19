dependencies {
    compileOnly(libs.vaultapi)
    compileOnly(libs.shopgui.api)

    compileOnly(fileTree("libs") { include("*.jar") })
}

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    filesMatching("plugin.yml") {
        expand("version" to project.version)
    }
}