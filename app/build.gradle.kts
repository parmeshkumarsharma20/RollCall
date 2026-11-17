plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

// Redirect build directory outside OneDrive to prevent file locking by OneDrive Sync
layout.buildDirectory.set(file("C:/tmp/RollCall_build/app"))

android {
    namespace = "com.example.rollcall"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.rollcall"
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
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.firebase.database)
    implementation(libs.googleid)
    implementation(libs.material)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.messaging)

    // Location & QR & Animation & Auth
    implementation(libs.play.services.location)
    implementation(libs.play.services.auth)
    implementation(libs.zxing.core)
    implementation(libs.zxing.embedded)
    implementation(libs.lottie)

    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}