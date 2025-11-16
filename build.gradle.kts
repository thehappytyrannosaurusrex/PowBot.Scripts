plugins {
    id("org.jetbrains.kotlin.jvm") version "2.2.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.0"

    application
}

group = "org.thehappytyrannosaurusrex"

repositories {
    mavenCentral()
    google()
    maven ("https://repo.powbot.org/releases")
}

dependencies {
    // Kotlin base
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    // PowBot SDK
    implementation("org.powbot:client-sdk-loader:3.+")
    implementation("org.powbot:client-sdk:3.+")

    // 🌀 Async/concurrency (lightweight threads)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // JSON handling & configuration (Jackson suite)
    implementation("com.fasterxml.jackson.core:jackson-core:2.13.3")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.13.3")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.3")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.3")

    // Optional Kotlin serialization (lightweight alternative to Jackson)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

    // Logging (simple + slf4j)
    implementation("org.slf4j:slf4j-simple:2.0.17")

    // Testing framework (JUnit 5 + Kotlin test)
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")

    // Utility libraries for math, timing, etc. (optional, helps with advanced logic)
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("com.google.guava:guava:32.1.2-jre")
}