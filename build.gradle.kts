import com.android.build.gradle.BaseExtension
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.kotlin.kapt) apply false
}

subprojects {
    fun subprojectSetting(androidGradle: String) {
        apply {
            plugin(androidGradle)
            plugin(rootProject.libs.plugins.kotlin.android.get().pluginId)
            plugin(rootProject.libs.plugins.kotlin.kapt.get().pluginId)
            plugin(rootProject.libs.plugins.ksp.get().pluginId)
            plugin(rootProject.libs.plugins.hilt.get().pluginId)
        }

        extensions.configure(BaseExtension::class.java) {
            compileSdkVersion = rootProject.libs.versions.androidCompile.get()
            defaultConfig {
                minSdk = rootProject.libs.versions.minSdk.get().toInt()
                targetSdk = rootProject.libs.versions.targetSdk.get().toInt()
                versionCode = rootProject.libs.versions.versionCode.get().toInt()
                versionName = rootProject.libs.versions.versionName.get()
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                vectorDrawables.useSupportLibrary = true
                javaCompileOptions {
                    annotationProcessorOptions {
                        arguments["room.schemaLocation"] = "$projectDir/schemas"
                    }
                }
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
            packagingOptions {
                resources {
                    excludes.add("META-INF/com.google.dagger_dagger.version")
                    excludes.add("/META-INF/{AL2.0,LGPL2.1}")
                }
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
        }
        dependencies {
            val implementation by configurations
            fun kotlinAnnotation(type: String, group: String, name: String, version: String)  {
                configurations[type].dependencies.add(DefaultExternalModuleDependency(group, name, version))
            }
            implementation(rootProject.libs.gson)
            implementation(rootProject.libs.mapstruct)
            kotlinAnnotation("kapt","org.mapstruct", "mapstruct-processor", rootProject.libs.versions.mapstruct.get())
            implementation(rootProject.libs.hilt)
            kotlinAnnotation("ksp","com.google.dagger", "hilt-android-compiler", rootProject.libs.versions.hilt.get())
        }
    }

    when(name) {
        "presentation" -> {
            subprojectSetting(rootProject.libs.plugins.android.application.get().pluginId)
        }
        "data" -> {
            subprojectSetting(rootProject.libs.plugins.android.library.get().pluginId)
        }
    }
}

