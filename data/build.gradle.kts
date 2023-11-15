android {
    namespace = "com.example.data"
}

dependencies {
    implementation(project(":domain"))
    implementation(files("libs/com.ksnet.library_v1.1.4.jar"))
    implementation(rootProject.libs.skydoeves.sandwich)
    implementation(rootProject.libs.bundles.retrofit)

    api(rootProject.libs.room)
    ksp(rootProject.libs.room.compiler)
}