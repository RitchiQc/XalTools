val java16 = setOf(
    "v1_16_5",
)

val java17 = setOf(
    "hooks_java17",
)

plugins {
    `java-library`
}

java {
    toolchain {
        val version = when (project.name) {
            in java16 -> 16
            in java17 -> 17
            else -> 8
        }
        languageVersion.set(JavaLanguageVersion.of(version))
    }
}