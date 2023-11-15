android {
    namespace = "com.example.cleanarchitech_text_0506"
    defaultConfig {
        configurations.all {
            resolutionStrategy {
                force("androidx.emoji2:emoji2-views-helper:1.3.0")
                force("androidx.emoji2:emoji2:1.3.0")
            }
        }
        applicationId = "com.example.cleanarchitech_text_0506"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.4"
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

    implementation(rootProject.libs.bundles.androidx.compose)
    implementation(rootProject.libs.bundles.accompanist)

    implementation(rootProject.libs.android.material)
    implementation(rootProject.libs.rxAndroidBLE)
    implementation(rootProject.libs.f0ris.sweetalert)
    implementation(rootProject.libs.mik3y.usb)
    testImplementation(rootProject.libs.junit)
    debugImplementation(rootProject.libs.test.junit)
    debugImplementation(rootProject.libs.test.espresso)

}