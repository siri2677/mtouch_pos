plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.example.data"
    compileSdkVersion = rootProject.libs.versions.androidCompile.get()
    defaultConfig {
        minSdk = rootProject.libs.versions.minSdk.get().toInt()
        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(rootProject.libs.bundles.retrofit)
    implementation(rootProject.libs.gson)
    implementation(rootProject.libs.hilt)

    api(rootProject.libs.room)

    ksp(rootProject.libs.room.compiler)
    ksp(rootProject.libs.hilt.compiler)
}
