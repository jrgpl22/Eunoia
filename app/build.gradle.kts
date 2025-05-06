plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.moodtracker"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.moodtracker"
        minSdk = 26
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.room:room-runtime:2.6.0")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    annotationProcessor("androidx.room:room-compiler:2.6.0")
    implementation("com.github.bumptech.glide:glide:4.15.1")


    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}