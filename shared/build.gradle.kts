plugins {
    kotlin("jvm")
    alias(libs.plugins.serialization)
}

group = "tech.nimbus"
version = "1.0-SNAPSHOT"

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
    sourceSets {
        // Добавляем commonMain как основной источник для JVM модуля
        val main by getting {
            kotlin.srcDir("src/commonMain/kotlin")
        }
        val test by getting {
            kotlin.srcDir("src/commonTest/kotlin")
        }
    }
}

tasks.test {
    useJUnit()
}
