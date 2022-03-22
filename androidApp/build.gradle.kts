import java.io.ByteArrayOutputStream

plugins {
    id("com.android.application")
    kotlin("android")
}

fun Project.execWithOutput(cmd: String): String {
    val byteOut: ByteArrayOutputStream = ByteArrayOutputStream()
    project.exec {
        commandLine = cmd.split(" ")
        standardOutput = byteOut
    }
    return byteOut.toByteArray().toString()
}

fun gitVersion(): String {
    var process = ""
    val maybeTagOfCurrentCommit = execWithOutput("git describe --contains HEAD")
    process = if (maybeTagOfCurrentCommit.isEmpty()) {
        println("No tag on current commit. Will take the latest one.")
        execWithOutput("git for-each-ref refs/tags --sort=-authordate --format='%(refname:short)' --count=1")
    } else {
        println("Tag found on current commit")
        execWithOutput("git describe --contains HEAD")
    }
    return process.replace("'", "").substring(1).replace("\\.", "").trim()
}

fun versionCodeFromGit(): Int {
    println("version code " + gitVersion())
    return gitVersion().toInt()
}

fun versionNameFromGit(): String {
    println("version name " + gitVersion())
    return gitVersion()
}

android {
    compileOptions {
        // Flag to enable support for the new language APIs
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    compileSdk = 31
    buildToolsVersion = "31.0.0"
    buildFeatures {
        viewBinding = true
    }
    defaultConfig {
        applicationId = "bou.amine.apps.readerforselfossv2.android"
        minSdk = 21
        targetSdk = 31
        versionCode = versionCodeFromGit()
        versionName = versionNameFromGit()

        multiDexEnabled = true
        lint {
            abortOnError = true
        }
        vectorDrawables.useSupportLibrary = true

        // tests
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf("room.schemaLocation" to "$projectDir/schemas")
            }
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
        getByName("debug") {
            buildConfigField("String", "LOGIN_URL", properties["appLoginUrl"] as String)
            buildConfigField("String", "LOGIN_PASSWORD", properties["appLoginPassword"] as String)
            buildConfigField("String", "LOGIN_USERNAME", properties["appLoginUsername"] as String)
        }
    }
    flavorDimensions.add("build")
    productFlavors {
        create("github") {
            versionNameSuffix = "-github"
            dimension = "build"
        }
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

}

dependencies {
    implementation(project(":shared"))
    implementation("com.google.android.material:material:1.4.0")
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0")
}