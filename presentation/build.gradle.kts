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
        sourceCompatibility = JavaVersion.VERSION_20
        targetCompatibility = JavaVersion.VERSION_20
    }
}

dependencies {
    implementation(project(":data"))
    implementation(project(":domain"))

    implementation(platform(rootProject.libs.compose.bom))
    androidTestImplementation(platform(rootProject.libs.compose.bom))
    implementation(rootProject.libs.kizitonwose.calendar.compose)
    androidTestImplementation(rootProject.libs.compose.ui.test.jnit4)
    debugImplementation(rootProject.libs.compose.ui.tooling)
    debugImplementation(rootProject.libs.compose.ui.test.manifest)
    runtimeOnly(rootProject.libs.compose.runtime)

    implementation(rootProject.libs.bundles.androidx.compose)
    implementation(rootProject.libs.bundles.accompanist)

    implementation(rootProject.libs.android.material)
    implementation(rootProject.libs.rxAndroidBLE)
    implementation(rootProject.libs.f0ris.sweetalert)
    implementation(rootProject.libs.mik3y.usb)
    testImplementation(rootProject.libs.junit)
    testImplementation(rootProject.libs.mockk)
    testImplementation(rootProject.libs.turbine)
    testImplementation(rootProject.libs.coroutines.test)
    debugImplementation(rootProject.libs.test.junit)
    debugImplementation(rootProject.libs.test.espresso)

    debugImplementation(rootProject.libs.hilt.test)
    testImplementation(rootProject.libs.hilt.test)

    implementation(rootProject.libs.gson)
    implementation(rootProject.libs.mapstruct)
    ksp(rootProject.libs.hilt.compiler)
    implementation(rootProject.libs.hilt)

    implementation(rootProject.libs.kotlinx.serialization.json)
    implementation(rootProject.libs.errorprone)
}