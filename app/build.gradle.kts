plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.copymanga.downloader"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.copymanga.downloader"
        minSdk = 26
        targetSdk = 34
        versionCode = 11
        versionName = "0.1.10"
        vectorDrawables { useSupportLibrary = true }
    }

    signingConfigs {
        create("release") {
            val props = rootProject.file("local.properties").let { file ->
                java.util.Properties().apply { if (file.exists()) load(file.inputStream()) }
            }
            storeFile = file(
                System.getenv("KEYSTORE_FILE") ?: props.getProperty("signing.storeFile", "release.jks")
            )
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: props.getProperty("signing.storePassword", "")
            keyAlias = System.getenv("KEY_ALIAS") ?: props.getProperty("signing.keyAlias", "")
            keyPassword = System.getenv("KEY_PASSWORD") ?: props.getProperty("signing.keyPassword", "")
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
            pickFirsts += "META-INF/INDEX.LIST"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    debugImplementation(libs.androidx.ui.tooling)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization.converter)

    implementation(libs.coil.compose)

    implementation(libs.pdfbox.android)
}
