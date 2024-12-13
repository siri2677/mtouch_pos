plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.example.mtouchpos"
    compileSdkVersion = rootProject.libs.versions.androidCompile.get()
    defaultConfig {
        minSdk = rootProject.libs.versions.minSdk.get().toInt()
        targetSdk = rootProject.libs.versions.targetSdk.get().toInt()
        versionCode = rootProject.libs.versions.versionCode.get().toInt()
        versionName = rootProject.libs.versions.versionName.get()

        configurations.all {
            resolutionStrategy {
                force("androidx.emoji2:emoji2-views-helper:1.3.0")
                force("androidx.emoji2:emoji2:1.3.0")
            }
        }
        applicationId = "com.example.mtouchpos"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        compose = true
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    packaging {
        resources {
            excludes.add("META-INF/com.google.dagger_dagger.version")
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    implementation(project(":data"))
    implementation(project(":domain"))

    implementation(platform(rootProject.libs.compose.bom))
    androidTestImplementation(platform(rootProject.libs.compose.bom))
    debugImplementation(rootProject.libs.compose.ui.tooling)
    androidTestImplementation(rootProject.libs.compose.ui.test.jnit4)
    debugImplementation(rootProject.libs.compose.ui.test.manifest)
    runtimeOnly(rootProject.libs.compose.runtime)

    implementation(rootProject.libs.bundles.compose)

    implementation(rootProject.libs.android.material)
    implementation(rootProject.libs.rxAndroidBLE)
    implementation(rootProject.libs.mik3y.usb)
    testImplementation(rootProject.libs.junit)
    testImplementation(rootProject.libs.mockk)
    testImplementation(rootProject.libs.turbine)
    testImplementation(rootProject.libs.coroutines.test)
    debugImplementation(rootProject.libs.test.junit)
    debugImplementation(rootProject.libs.test.espresso)

    implementation(rootProject.libs.gson)
    ksp(rootProject.libs.hilt.compiler)
    implementation(rootProject.libs.hilt)

    implementation(rootProject.libs.kotlinx.serialization.json)
    implementation(rootProject.libs.errorprone)
}