plugins {
    alias(libs.plugins.android.application)
}

val releaseStoreFilePath = providers.gradleProperty("YIRAE_RELEASE_STORE_FILE")
    .orElse(providers.environmentVariable("YIRAE_RELEASE_STORE_FILE"))
val releaseStorePassword = providers.gradleProperty("YIRAE_RELEASE_STORE_PASSWORD")
    .orElse(providers.environmentVariable("YIRAE_RELEASE_STORE_PASSWORD"))
val releaseKeyAlias = providers.gradleProperty("YIRAE_RELEASE_KEY_ALIAS")
    .orElse(providers.environmentVariable("YIRAE_RELEASE_KEY_ALIAS"))
val releaseKeyPassword = providers.gradleProperty("YIRAE_RELEASE_KEY_PASSWORD")
    .orElse(providers.environmentVariable("YIRAE_RELEASE_KEY_PASSWORD"))
val hasReleaseSigning = releaseStoreFilePath.isPresent
    && releaseStorePassword.isPresent
    && releaseKeyAlias.isPresent
    && releaseKeyPassword.isPresent

android {
    namespace = "com.example.yirae"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.yirae"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = rootProject.file(releaseStoreFilePath.get())
                storePassword = releaseStorePassword.get()
                keyAlias = releaseKeyAlias.get()
                keyPassword = releaseKeyPassword.get()
            }
        }
    }

    buildTypes {
        release {
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.okhttp)
    implementation(libs.gson)
    implementation(libs.glide)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
