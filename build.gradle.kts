
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}


android {
    namespace = "com.example.imagesteganography"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.imagesteganography"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    android {
        buildFeatures {
            viewBinding = true
        }
    }

}


dependencies {
//    implementation("org.jetbrains:annotations:15.0") {
//        // Exclude the older version of the annotations library from this dependency
//        exclude(group = "org.jetbrains", module = "annotations-java5")
//    }
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.firebase.auth)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation (libs.picasso)
    implementation (libs.butterknife.v1000)
    annotationProcessor (libs.butterknife.compiler.v1000)
//    implementation (libs.annotations.java5)
    implementation (libs.gridlayout)
    implementation(libs.guava)
}

configurations {
    all {
        resolutionStrategy {
            // Force all dependencies to use version 15.0 of the annotations library
            force("org.jetbrains:annotations:15.0")
        }
    }
}

