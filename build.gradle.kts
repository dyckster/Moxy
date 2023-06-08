// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.2.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}")
        classpath("org.jetbrains.kotlin:kotlin-serialization:${Versions.kotlin}")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.43.2")
    }
}

plugins {
    id("com.github.ben-manes.versions") version "0.31.0"
    id("org.jetbrains.dokka") version "1.4.32"
    id("com.vanniktech.maven.publish") version "0.15.1" apply false
}

tasks.withType(Sign::class.java).all {
    required(false)
}

subprojects {
    repositories {
        google()
        mavenCentral()
        maven {
            setUrl("https://kotlin.bintray.com/kotlinx")
            content {
                includeGroup("org.jetbrains.kotlinx")
            }
        }

        val repo = maven {
            url = rootProject.file("build/localMavenPublish").toURI()
            content {
                includeGroup("com.github.moxy-community")
            }
        }
        // The only way to put the repository to the top using public api
        remove(repo)
        addFirst(repo)
    }
}

subprojects {
    apply(plugin = "checkstyle")

    tasks.register<Checkstyle>("checkstyle") {
        description = "Runs Checkstyle inspection"
        group = "moxy"
        configFile = rootProject.file("checkstyle.xml")
        ignoreFailures = false
        isShowViolations = true
        classpath = files()
        exclude("**/*.kt")
        source("src/main/java")
    }

    // check code style after project evaluation
    afterEvaluate {
        tasks.named("check").configure { dependsOn("checkstyle") }
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

tasks.named<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask>("dependencyUpdates")
    .configure {
        rejectVersionIf { isNonStable(candidate.version) }
    }