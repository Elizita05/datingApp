plugins {
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.serialization") version "1.9.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))


    implementation("io.ktor:ktor-server-core:2.3.0")
    implementation("io.ktor:ktor-server-netty:2.3.0")
    implementation("io.ktor:ktor-server-auth-jwt:2.3.0")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.0")
    implementation("io.ktor:ktor-server-cors-jvm:2.3.0")


    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("org.jetbrains.exposed:exposed-core:0.45.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.45.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.45.0")


    implementation("ch.qos.logback:logback-classic:1.4.0")
    implementation("com.google.guava:guava:30.1-android")
    implementation("io.ktor:ktor-server-websockets:2.3.0")


}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(11)
}