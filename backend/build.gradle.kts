plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

group = "com.mycodecalendar"
version = "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("com.mycodecalendar.backend.ApplicationKt")
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-netty-jvm:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-status-pages-jvm:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-cors-jvm:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-client-core-jvm:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-client-cio-jvm:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-client-content-negotiation-jvm:${libs.versions.ktor.get()}")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.4.14")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    testImplementation("io.ktor:ktor-server-tests-jvm:${libs.versions.ktor.get()}")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}
