plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services") version "4.5.0" //apply false
}

android {
    namespace = "com.example.cambiazoapp"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.cambiazoapp"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:34.19.0"))

    implementation("com.google.firebase:firebase-auth") //registrarse e iniciar sesión, así que agregaremos Firebase Authentication

    implementation("com.google.firebase:firebase-firestore") //agregar Cloud Firestore

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7") //viewModel() desde Jetpack Compose
    implementation("androidx.navigation:navigation-compose:2.8.9")//
    implementation("androidx.compose.material:material-icons-extended")//Usaremos los íconos de Material
    implementation("com.google.android.gms:play-services-auth:21.4.0")//librería que permitirá iniciar sesión con Google
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}