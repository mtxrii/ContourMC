plugins {
    id("idea")
    id("java-library")

    alias(libs.plugins.shadow)
    alias(libs.plugins.paperweight.userdev)
    alias(libs.plugins.run.paper)
    alias(libs.plugins.resource.factory)
}


fun RepositoryHandler.github(path: String, action: MavenArtifactRepository.() -> Unit = {}) = maven {
    url = uri("https://maven.pkg.github.com/$path")

    credentials {
        username = if (project.hasProperty("githubPackagesUser")) project.properties["githubPackagesUser"]?.toString() else System.getenv("GITHUB_ACTOR");
        password = if (project.hasProperty("githubPackagesAuth")) project.properties["githubPackagesAuth"]?.toString() else System.getenv("GITHUB_TOKEN");
    }

    this.action()
}

fun xyz.jpenilla.runtask.pluginsapi.DownloadPluginsSpec.modrinth(provider: Provider<PluginDependency>) =
    modrinth(provider.get().pluginId, provider.get().version.toString())


repositories {
    mavenCentral()

    github("Sxtanna/platform") {
        content {
            includeGroup("com.sxtanna.platform")
        }
    }

    maven("https://oss.sonatype.org/content/repositories/snapshots/") {
        name = "sonatype-snapshots"
        mavenContent {
            snapshotsOnly()
        }
    }
}

dependencies {
    paperweight.paperDevBundle(libs.versions.paper.api)

    implementation(libs.platform.core)
    implementation(libs.platform.paper)

    implementation(libs.configurate.yaml)
    implementation(libs.configurate.json)
    implementation(libs.configurate.hocon)

    implementation(libs.cloud.annotations)
    implementation(libs.cloud.paper)
    implementation(libs.cloud.extras)

    implementation(libs.classgraph)

    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    implementation(libs.google.guice)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.launcher)

    testImplementation(libs.assertj)

    components.all {
        isChanging = id.group == "com.sxtanna.platform"
    }
}

configurations.configureEach {
    resolutionStrategy.cacheChangingModulesFor(0, TimeUnit.SECONDS)
}


// no longer needed as of 26.1
// paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

idea {
    module.isDownloadJavadoc = true
    module.isDownloadSources = true
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get()))
}

tasks {
    assemble {
        dependsOn += rootProject.tasks.shadowJar
    }

    javadoc {
        options.encoding = Charsets.UTF_8.name()

        (options as? StandardJavadocDocletOptions)?.apply {
            addBooleanOption("html5", true)
            addStringOption("Xdoclint:none", "-quiet")
        }
    }

    test {
        useJUnitPlatform()
    }

    shadowJar {
        exclude("META-INF/**")

        archiveClassifier.set("")
    }

    compileJava {
        options.isFork = true
        options.encoding = Charsets.UTF_8.name()
        options.compilerArgs.add("-parameters")
        options.compilerArgs.add("-Xlint:unchecked")

        sourceCompatibility = JavaVersion.toVersion(libs.versions.java.get()).majorVersion
        targetCompatibility = JavaVersion.toVersion(libs.versions.java.get()).majorVersion
    }

    runServer {
        jvmArgs("-Xms2G", "-Xmx2G")
        minecraftVersion(libs.versions.minecraft.get())

        if (providers.gradleProperty("logging.debug").getOrElse("false").toBoolean()) {
            systemProperty("log4j.configurationFile", file("run/log4j2.xml").absolutePath)
        }

        downloadPlugins {
            modrinth(libs.plugins.modrinth.worldedit)
            modrinth(libs.plugins.modrinth.worldguard)
            modrinth(libs.plugins.modrinth.multiverse)
        }
    }
}

paperPluginYaml {
    main = "com.mtxrii.contourmc.ContourMCPlugin"
    bootstrapper = "com.mtxrii.contourmc.ContourMCPluginBootstrap"

    name = "ContourMC"
    prefix = "CTR"

    apiVersion = libs.versions.minecraft
}