plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
     id("kotlin-kapt")
}

android {
    namespace = "com.kiosq"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.kiosq"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        viewBinding = true
    }

    // 🔥 FIX JVM ERROR
    compileOptions {
       
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}


dependencies {

    // Material
    implementation("com.google.android.material:material:1.11.0")

    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    // ROOM (WAJIB)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // GSON
    implementation("com.google.code.gson:gson:2.10.1")

}