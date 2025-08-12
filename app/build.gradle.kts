plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.wpk.modul1"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.wpk.modul1"
        minSdk = 24
        targetSdk = 34  // Zachowujemy 34 dla stability w production
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // AppCompat dla themes compatibility
    implementation("androidx.appcompat:appcompat:1.6.1")

    // Core Android
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

    // Activity Compose
    implementation("androidx.activity:activity-compose:1.9.3")

    // Compose BOM - zarządza wersjami wszystkich Compose libraries
    implementation(platform("androidx.compose:compose-bom:2024.10.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material:material")

    // DODANE: Material Icons Extended dla Icons.Filled.Send
    implementation("androidx.compose.material:material-icons-extended")

    // ViewModel Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    // ML Kit Text Recognition (OCR)
    implementation("com.google.mlkit:text-recognition:16.0.1")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

    // Core Splashscreen
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Test dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.10.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // CameraX dependencies
    implementation("androidx.camera:camera-core:1.3.0")
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.3.0")
// Permissions (Accompanist)
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")

    implementation ("androidx.navigation:navigation-compose:2.7.5")
    dependencies { implementation ("androidx.navigation:navigation-runtime-ktx:2.7.5")
    }

}


// Dependency resolution strategy - PRZENIESIONE NA ZEWNĄTRZ dependencies block
configurations.all {
    resolutionStrategy {
        // Wymuś konkretne wersje dla konfliktujących dependencies
        force("androidx.core:core:1.15.0")
        force("androidx.core:core-ktx:1.15.0")
        force("androidx.activity:activity:1.9.3")
        force("androidx.activity:activity-ktx:1.9.3")
        force("androidx.activity:activity-compose:1.9.3")
        force("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
        force("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    }
}
