import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

// ---------------------------------------------------------------------------
// Read local.properties (never committed) to obtain the football-data.org
// API token and base URL. These are exposed to the app through BuildConfig
// fields. If the file or a key is missing we fall back to safe placeholders
// so the project always builds and the app runs with demo data.
// ---------------------------------------------------------------------------
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    FileInputStream(localPropertiesFile).use { localProperties.load(it) }
}

fun readProp(key: String, default: String): String {
    // Priority: local.properties -> environment variable -> default placeholder.
    return localProperties.getProperty(key)
        ?: System.getenv(key)
        ?: default
}

val footballApiToken: String = readProp("FOOTBALL_DATA_API_TOKEN", "your_api_token_here")
val footballApiBaseUrl: String = readProp("FOOTBALL_API_BASE_URL", "https://api.football-data.org/v4")

// ---------------------------------------------------------------------------
// Release signing config. Values come from environment variables so the
// keystore and passwords are never stored in the repository. In CI these are
// provided by GitHub Secrets (decoded to a file first). Locally you may set
// the same environment variables or edit this block for your own keystore.
// ---------------------------------------------------------------------------
val keystoreFilePath: String? = System.getenv("ANDROID_KEYSTORE_FILE")
val keystorePassword: String? = System.getenv("ANDROID_KEYSTORE_PASSWORD")
val keyAlias: String? = System.getenv("ANDROID_KEY_ALIAS")
val keyPasswordEnv: String? = System.getenv("ANDROID_KEY_PASSWORD")
val hasReleaseSigning: Boolean =
    keystoreFilePath != null && file(keystoreFilePath).exists() &&
        keystorePassword != null && keyAlias != null && keyPasswordEnv != null

android {
    namespace = "com.cornerkick.planner"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.cornerkick.planner"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        // Only INTERNET permission is declared in the manifest; no test runner
        // that needs extra permissions is required for release.
        vectorDrawables { useSupportLibrary = true }

        // Expose API configuration to the app via BuildConfig.
        buildConfigField("String", "FOOTBALL_DATA_API_TOKEN", "\"$footballApiToken\"")
        buildConfigField("String", "FOOTBALL_API_BASE_URL", "\"$footballApiBaseUrl\"")
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(keystoreFilePath!!)
                storePassword = keystorePassword
                this.keyAlias = keyAlias
                keyPassword = keyPasswordEnv
                // PKCS12 keystore.
                storeType = "PKCS12"
                enableV1Signing = true
                enableV2Signing = true
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
        }
        release {
            // NOTE: minify/shrink are enabled here. If you need to verify a
            // non-minified release first, temporarily set both to false, build
            // and launch-test, then re-enable. See README "Release optimization".
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Use the real release keystore only when it is configured; this lets
            // the project be imported and built locally without signing secrets.
            signingConfig = if (hasReleaseSigning) {
                signingConfigs.getByName("release")
            } else {
                null
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        // Ensure 16 KB page-size friendly packaging: we ship no native libraries,
        // so there is nothing to align, but keep uncompressed libs setting safe.
        jniLibs {
            useLegacyPackaging = false
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.09.03")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.core:core-splashscreen:1.0.1")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.8.1")

    implementation("androidx.datastore:datastore-preferences:1.1.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    debugImplementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
