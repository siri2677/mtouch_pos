plugins {
    id("org.jetbrains.kotlin.jvm")
}

dependencies {
    implementation(rootProject.libs.coroutines)
    implementation(rootProject.libs.gson)
    implementation(rootProject.libs.javax.inject)
    implementation(rootProject.libs.errorprone)
}
