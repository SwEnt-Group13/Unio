
import java.io.FileInputStream
import java.util.Properties

plugins {
    jacoco
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.ktfmt)
    alias(libs.plugins.gms)
    alias(libs.plugins.sonar)
    id("jacoco")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    kotlin("kapt")
}

android {
    namespace = "com.android.unio"
    compileSdk = 34

    // Load the API key from local.properties
    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localProperties.load(FileInputStream(localPropertiesFile))
    }

    val mapsApiKey: String = localProperties.getProperty("MAPS_API_KEY") ?: ""


    defaultConfig {
        manifestPlaceholders += mapOf()
        applicationId = "com.android.unio"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "com.android.unio.HiltApplication"
        vectorDrawables {
            useSupportLibrary = true
        }
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),"proguard-rules.pro"
            )
        }
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.7"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            merges += "META-INF/LICENSE.md"
            merges += "META-INF/LICENSE-notice.md"
            excludes += "META-INF/LICENSE-notice.md"
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true

            isReturnDefaultValues = true
        }
        packagingOptions {
            jniLibs {
                useLegacyPackaging = true
            }
        }
    }


    buildFeatures {
        compose = true
        buildConfig = true
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    // Robolectric needs to be run only in debug. But its tests are placed in the shared source set (test)
    // The next lines transfers the src/test/* from shared to the testDebug one
    //
    // This prevent errors from occurring during unit tests
    sourceSets.getByName("testDebug") {
        val test = sourceSets.getByName("test")

        java.setSrcDirs(test.java.srcDirs)
        res.setSrcDirs(test.res.srcDirs)
        resources.setSrcDirs(test.resources.srcDirs)
    }

    sourceSets.getByName("test") {
        java.setSrcDirs(emptyList<File>())
        res.setSrcDirs(emptyList<File>())
        resources.setSrcDirs(emptyList<File>())
    }

  bundle {
    language {
      enableSplit = false
    }
  }
}

sonar {
    properties {
        property("sonar.projectKey", "SwEnt-Group13_unio")
        property("sonar.projectName", "unio")
        property("sonar.organization", "swent-group13")
        property("sonar.host.url", "https://sonarcloud.io")
        // Comma-separated paths to the various directories containing the *.xml JUnit report files. Each path may be absolute or relative to the project base directory.
        property("sonar.junit.reportPaths", "${project.layout.buildDirectory.get()}/test-results/testDebugunitTest/")
        // Paths to xml files with Android Lint issues. If the main flavor is changed, this file will have to be changed too.
        property("sonar.androidLint.reportPaths", "${project.layout.buildDirectory.get()}/reports/lint-results-debug.xml")
        // Paths to JaCoCo XML coverage report files.
        property("sonar.coverage.jacoco.xmlReportPaths", "${project.layout.buildDirectory.get()}/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
    }
}

// When a library is used both by robolectric and connected tests, use this function
fun DependencyHandlerScope.globalTestImplementation(dep: Any) {
    androidTestImplementation(dep)
    testImplementation(dep)
}

dependencies {

    implementation(platform(libs.firebase.bom))
    implementation(libs.google.firebase.auth.ktx)
    implementation(libs.firebase.analytics)

    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.work.runtime.ktx)

    // Core
    implementation(libs.core.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.kotlinx.serialization.json)

    // Jetpack Compose UI
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.firebase.storage.ktx)
    implementation(libs.androidx.ui.text.google.fonts)
    implementation(libs.androidx.compose.material)
    implementation(libs.core)
    implementation(libs.firebase.functions.ktx)
    implementation(libs.play.services.location)
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.androidx.espresso.contrib)

    testImplementation(libs.test.core.ktx)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.material)

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(platform(libs.compose.bom))

    globalTestImplementation(libs.androidx.junit)
    globalTestImplementation(libs.androidx.espresso.core)

    // ------------- Jetpack Compose ------------------
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    globalTestImplementation(composeBom)

    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    // Material Design 3
    implementation(libs.compose.material3)
    // Integration with activities
    implementation(libs.compose.activity)
    // Integration with ViewModels
    implementation(libs.compose.viewmodel)
    // Android Studio Preview support
    implementation(libs.compose.preview)
    debugImplementation(libs.compose.tooling)
    // UI Tests
    globalTestImplementation(libs.compose.test.junit)
    debugImplementation(libs.compose.test.manifest)

    // --------- Kaspresso test framework ----------
    globalTestImplementation(libs.kaspresso)
    globalTestImplementation(libs.kaspresso.compose)

    // ----------       Robolectric     ------------


    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Google Service and Maps
    implementation(libs.play.services.maps)
    implementation(libs.maps.compose)
    implementation(libs.maps.compose.utils)
    implementation(libs.play.services.auth)

    // Firebase
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.ui.auth)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.auth)

    // Networking with OkHttp
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson.converter)
    testImplementation(libs.retrofit.mock)
    testImplementation(libs.mockwebserver)
    androidTestImplementation(libs.mockwebserver)
    androidTestImplementation(libs.retrofit.mock)


    // Testing Unit
    testImplementation(libs.junit)
    androidTestImplementation(libs.mockk)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.mockk.agent)
    testImplementation(libs.json)

    // Test UI
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.espresso.intents)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.inline)
    testImplementation(libs.mockito.kotlin)
    androidTestImplementation(libs.mockito.android)
    androidTestImplementation(libs.mockito.kotlin)
    testImplementation(libs.robolectric)
    androidTestImplementation(libs.kaspresso)
    androidTestImplementation(libs.kaspresso.allure.support)
    androidTestImplementation(libs.kaspresso.compose.support)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk.v193)

    implementation(libs.androidx.material.icons.extended)
    // For AsyncImage
    implementation(libs.coil.compose)

    // Hilt : Dependency Injection
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.navigation.fragment)
    kapt(libs.hilt.android.compiler)

    testImplementation(libs.hilt.android.testing)
    kaptTest(libs.hilt.android.compiler.v2511)

    androidTestImplementation(libs.hilt.android.testing)
    kaptAndroidTest(libs.hilt.android.compiler.v2511)
    implementation(libs.androidx.appsearch)
    implementation(libs.androidx.appsearch.local.storage)
    kapt(libs.androidx.appsearch.compiler)

    //App search
    implementation(libs.guava)
    implementation(libs.kotlinx.coroutines.guava)

    // Kotlin Reflect
    implementation(kotlin("reflect"))

    // Preferences
    implementation(libs.androidx.preference.ktx)
    implementation(libs.library)
    implementation(libs.accompanist.permissions)
}

kapt {
    correctErrorTypes = true
}


tasks.withType<Test> {
    // Configure Jacoco for each tests
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

tasks.register("jacocoTestReport",JacocoReport::class) {
    mustRunAfter("testDebugUnitTest","connectedDebugAndroidTest")

    reports {
        xml.required = true
        html.required = true
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
    )
    val debugTree = fileTree("${project.buildDir}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }
    val mainSrc = "${project.projectDir}/src/main/java"

    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree))
    executionData.setFrom(fileTree(project.buildDir) {
        include("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
        include("outputs/code_coverage/debugAndroidTest/connected/*/coverage.ec")
    })
}

configurations.forEach { configuration ->
  // Exclude protobuf-lite from all configurations
  // This fixes the "Internal error in Cloud Firestore" issue
  configuration.exclude("com.google.protobuf", "protobuf-lite")
}

fun runCommand(command: String) {
    val os = System.getProperty("os.name").toLowerCase()
    if (os.contains("win")) {
        val adjustedCommand = command
            .replace("./gradlew", "gradlew") // Windows does not support the ./ prefix
            .replace("'", "\"") // Windows does not support single quotes
        ProcessBuilder("cmd", "/c", "start", "cmd", "/k", adjustedCommand).start()
    } else if (os.contains("nix") || os.contains("nux") || os.contains("bsd")) {
        ProcessBuilder(
            "sh",
            "-c",
            """
                x-terminal-emulator -e '$command' ||
                gnome-terminal -- bash -c '$command; exec bash' ||
                konsole -e '$command'
                """.trimIndent()
        ).start()
    } else if (os.contains("mac")) {
        ProcessBuilder(
            "osascript",
            "-e",
            """
                tell application "Terminal"
                    do script "cd ${project.rootDir} && $command"
                    activate
                end tell
                """.trimIndent()
        ).start()
    } else {
        throw GradleException("Unsupported operating system: $os")
    }
}

tasks.register("startFirebaseEmulators") {
    doLast {
        runCommand("firebase emulators:start --import=./firebase/emulator-data")
    }
}

tasks.register("connectedCheckWithFirebaseEmulators") {
    doLast {
        // For Linux and MacOS, you need to grant execute permissions to gradlew e.g. chmod +x gradlew
        runCommand("firebase emulators:exec --import=./firebase/emulator-data './gradlew connectedCheck'")
    }
}
