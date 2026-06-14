val serverPluginDirectory = file(findProperty("serverPluginDirectory") ?: "path/to/serverPluginsFolder...")

plugins {
    java
    id("org.springframework.boot") version "4.1.0"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.mtxrii.contourmc"
version = "0.0.1-SNAPSHOT"
description = "ContourMC"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(26)
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.postgresql:postgresql:42.7.7")
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.+")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
}

tasks.register<Copy>("copyToServerPluginFolder") {
    from(tasks.jar)
    into("$serverPluginDirectory")
    dependsOn(tasks.jar)
}
