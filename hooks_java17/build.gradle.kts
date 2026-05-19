repositories {
    maven("https://maven.enginehub.org/repo/")
}

dependencies {
    compileOnly(libs.worldguard)
    compileOnly(libs.landsapi)

    compileOnly(fileTree("libs") { include("*.jar") })
}
