import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    kotlin("plugin.serialization") version "2.0.21"
    alias(libs.plugins.google.gms.google.services)
    id("com.google.firebase.crashlytics")
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
}

android {
    namespace = "com.anviam.fragmentapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.anviam.fragmentapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        getByName("release") {
            configure<CrashlyticsExtension> {
                nativeSymbolUploadEnabled = true
            }
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
        dataBinding = true
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.firebase.database)
    implementation(libs.firebase.auth.ktx.v2310)
    implementation(libs.play.services.location)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    val nav_version = "2.8.9"
    implementation("androidx.navigation:navigation-fragment:$nav_version")
    implementation("androidx.navigation:navigation-ui:$nav_version")
    implementation("androidx.navigation:navigation-dynamic-features-fragment:$nav_version")

    // Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:32.7.3"))

    // Firebase Crashlytics & Analytics (versionless due to BoM)
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")

    // Firebase Crashlytics NDK (must specify version explicitly)
    implementation("com.google.firebase:firebase-crashlytics-ndk:18.6.2")

    //google map dependency
    implementation ("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.maps.android:android-maps-utils:3.10.0")
    implementation ("com.google.android.gms:play-services-location:21.2.0")

    //Coroutine Dependency
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

// Adding Banner/ Image Slider into App
//    implementation ("com.github.smarteist:autoimageslider:1.4.0")
//    implementation("com.github.bumptech.glide:glide:4.11.0")

    implementation ("com.google.android.material:material:1.11.0")


}
