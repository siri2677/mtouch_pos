android {
    namespace = "com.example.data"
}

dependencies {
    implementation(project(":domain"))
    implementation(rootProject.libs.skydoeves.sandwich)
    implementation(rootProject.libs.bundles.retrofit)

    api(rootProject.libs.room)
    ksp(rootProject.libs.room.compiler)
}