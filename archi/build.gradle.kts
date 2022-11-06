plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
//    id "kotlin-kapt"
//    id "dagger.hilt.android.plugin"
}

android {
    compileSdk = AndroidSdk.compile

    defaultConfig {
        minSdk = AndroidSdk.min
        targetSdk = AndroidSdk.target

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = Versions.Java.java
        targetCompatibility = Versions.Java.java
    }
    kotlinOptions {
        jvmTarget = Versions.Java.jvmTarget
    }
    namespace = "com.uvisual.archi"
}

dependencies {

    compose()
    android()
    androidTest()
}