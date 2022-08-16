plugins {
    id("com.android.library")
    id("com.vanniktech.maven.publish")
}

android {
    compileSdkVersion(31)

    defaultConfig {
        minSdkVersion(14)
        targetSdkVersion(31)

        consumerProguardFiles("../moxy/src/main/resources/META-INF/proguard/moxy.pro")
    }

    buildFeatures {
        buildConfig = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    api(project(":moxy"))

    compileOnly(Deps.android)
    compileOnly(project(":stub-appcompat"))
}
