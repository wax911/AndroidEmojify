package io.wax911.emoji.buildSrc.plugin.components

import io.wax911.emoji.buildSrc.plugin.extensions.baseExtension
import io.wax911.emoji.buildSrc.common.Versions
import io.wax911.emoji.buildSrc.common.isLibraryModule
import io.wax911.emoji.buildSrc.common.isSampleModule
import io.wax911.emoji.buildSrc.plugin.extensions.baseAppExtension
import io.wax911.emoji.buildSrc.plugin.extensions.libraryExtension
import com.android.build.gradle.internal.dsl.DefaultConfig
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile
import java.io.File

@Suppress("UnstableApiUsage")
private fun DefaultConfig.applyAdditionalConfiguration(project: Project) {
    if (project.isSampleModule()) {
        applicationId = "io.wax911.emoji.sample"
        project.baseAppExtension().buildFeatures {
            viewBinding = true
        }
    }
    else
        consumerProguardFiles.add(File("consumer-rules.pro"))

    if (project.isLibraryModule()) {
        project.libraryExtension().buildFeatures {
            viewBinding = true
        }
    }

    println("Applying vector drawables configuration for module -> ${project.path}")
    vectorDrawables.useSupportLibrary = true
}

internal fun Project.configureAndroid(): Unit = baseExtension().run {
    compileSdkVersion(Versions.compileSdk)
    defaultConfig {
        minSdkVersion(Versions.minSdk)
        targetSdkVersion(Versions.targetSdk)
        versionCode = Versions.versionCode
        versionName = Versions.versionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        applyAdditionalConfiguration(project)
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            isTestCoverageEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }

        getByName("debug") {
            isMinifyEnabled = false
            isTestCoverageEnabled = true
        }
    }

    packagingOptions {
        exclude("META-INF/NOTICE.txt")
        exclude("META-INF/LICENSE")
        exclude("META-INF/LICENSE.txt")
    }

    sourceSets {
        map { androidSourceSet ->
            androidSourceSet.java.srcDir(
                "src/${androidSourceSet.name}/kotlin"
            )
        }
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.isReturnDefaultValues = true
    }

    lintOptions {
        isAbortOnError = false
        isIgnoreWarnings = false
        isIgnoreTestSources = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    tasks.withType(KotlinCompile::class.java) {
        kotlinOptions {
            allWarningsAsErrors = false
            kotlinOptions {
                allWarningsAsErrors = false
                // Filter out modules that won't be using coroutines
                freeCompilerArgs = listOf(
                    "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                    "-Xopt-in=kotlinx.coroutines.FlowPreview",
                    "-Xopt-in=kotlinx.coroutines.FlowPreview",
                    "-Xopt-in=kotlin.Experimental"
                )
            }
        }
    }

    tasks.withType(KotlinJvmCompile::class.java) {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}