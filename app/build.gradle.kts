plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "app.barapause.mobile"
    compileSdk = 35

    defaultConfig {
        applicationId = "app.barapause.mobile"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "1.1.0-beta"
    }

    buildTypes {
        debug {
            // Separate testing app so existing V1 settings are never destroyed by debug signing changes.
            applicationIdSuffix = ".beta"
            versionNameSuffix = "-beta-debug"
        }
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}
