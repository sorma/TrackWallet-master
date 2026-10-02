import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.jm.trackwallet"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.jm.trackwallet"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        resValue("string", "CURRENCY_API_KEY", gradleLocalProperties(rootDir, providers).getProperty("CURRENCY_API_KEY"))
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
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.play.services.auth)
    implementation (libs.material.v150)
    implementation(libs.google.firebase.auth)
    implementation(libs.play.services.auth.api.phone)
    implementation(libs.firebase.ui.auth)
    implementation(libs.google.firebase.firestore)
    implementation(libs.firebase.analytics)
    implementation(libs.retrofit.v290)
    implementation(libs.converter.gson)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.retrofit)
    implementation(libs.commons.validator)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.philjay.mpandroidchart)
    implementation(libs.recyclerview.v121)
    implementation(libs.recyclerview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

}

