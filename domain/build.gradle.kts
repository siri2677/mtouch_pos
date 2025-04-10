plugins {
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_20
    targetCompatibility = JavaVersion.VERSION_20
}

dependencies {
    implementation(rootProject.libs.kotlin.stdlib)
    implementation(rootProject.libs.coroutines)
    implementation(rootProject.libs.gson)
    implementation(rootProject.libs.javax.inject)
}