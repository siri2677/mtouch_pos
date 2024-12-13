plugins {
    alias(libs.plugins.kotlin.jvm)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(20)
    }
}

dependencies {
    implementation(rootProject.libs.kotlin.stdlib)
    implementation(rootProject.libs.coroutines)
    implementation(rootProject.libs.gson)
    implementation(rootProject.libs.javax.inject)
}
