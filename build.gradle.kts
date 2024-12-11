import org.apache.tools.ant.filters.ReplaceTokens
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("fabric-loom") version "1.9-SNAPSHOT"
    id("maven-publish")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("co.uzzu.dotenv.gradle") version "4.0.0"
    java
}

group = property("maven_group")!!
version = property("mod_version")!!

val mcVersions = property("supported_versions")!!
val targetVersion = mcVersions.toString().split(";")[0]

repositories {
    // Add repositories to retrieve artifacts from in here.
    // You should only use this when depending on other mods because
    // Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
    // See https://docs.gradle.org/current/userguide/declaring_repositories.html
    // for more information about repositories.
    maven { url = uri("https://maven.gurkz.me/releases") }
}

dependencies {
    minecraft("com.mojang:minecraft:$targetVersion")
    mappings("net.fabricmc:yarn:${property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")

    modImplementation("net.fabricmc:fabric-language-kotlin:${property("fabric_kotlin_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")

    include("me.lucko:fabric-permissions-api:${property("fabric_permissions_api_version")}")?.let { modImplementation(it) }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks {
    processResources {

        filesMatching("fabric.mod.json") {
            expand(getProperties())
            filter<ReplaceTokens>("tokens" to mapOf("supported_versions" to mcVersions.toString().split(";").joinToString("\",\""), "version" to project.version))
        }
    }

    jar {
        from("LICENSE")
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                artifactId = property("archives_base_name")!!.toString()
                groupId = property("maven_group")!!.toString()
                version = version
                artifact(remapJar) {
                    builtBy(remapJar)
                }
                artifact(kotlinSourcesJar) {
                    builtBy(remapSourcesJar)
                }
            }
        }

        // select the repositories you want to publish to
        repositories {
            // uncomment to publish to the local maven
            maven {
                name = "gurkanMaven"
                val repoHost = "https://maven.gurkz.me"
                url = uri(if (version.toString().endsWith("SNAPSHOT")) "$repoHost/snapshots" else "$repoHost/releases")
                credentials {
                    username = env.fetch("MVN_USERNAME")
                    password = env.fetch("MVN_TOKEN")
                }
            }
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

java {
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()
}