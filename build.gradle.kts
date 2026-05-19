plugins {
    java
    alias(libs.plugins.shadow)

    id("module-java-versions") apply false
    id("obfuscation")
}

allprojects {
    apply(plugin = "java")

    repositories {
        mavenCentral()
        mavenLocal()

        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://jitpack.io")

        maven("https://repo.tcoded.com/releases") // folialib
    }

    dependencies {
        compileOnly(rootProject.libs.paper.api)

        compileOnly(rootProject.libs.lombok)
        annotationProcessor(rootProject.libs.lombok)

        implementation(rootProject.libs.nbtapi)
        implementation(rootProject.libs.folialib)
    }
}

subprojects {
    apply(plugin = "module-java-versions")
}

val commons = setOf("commons")
configure(subprojects.filter { it.name !in commons}) {
    dependencies {
        compileOnly(project(":commons"))
    }
}

val api = setOf("commons", "api")
configure(subprojects.filter { it.name !in api }) {
    dependencies {
        compileOnly(project(":api"))
    }
}

val bukkit = setOf("commons", "api", "bukkit")
configure(subprojects.filter { it.name !in bukkit }) {
    dependencies {
        compileOnly(project(":bukkit"))
    }
}

dependencies {
    implementation(project(":api"))
    implementation(project(":commons"))
    implementation(project(":hooks_java17"))
    implementation(project(":bukkit"))
}

tasks.shadowJar {
    relocate("com.tcoded.folialib", "me.serbob.mythictools.libs.folialib")
    relocate("de.tr7zw.changeme.nbtapi", "me.serbob.mythictools.libs.nbt")

    archiveClassifier.set("unobf")

    outputs.upToDateWhen { false } // to remake the -unobf every time
}

tasks.named("shadowJar") {
    finalizedBy("obfuscate")
}