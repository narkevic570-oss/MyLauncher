plugins {
    id("com.android.application")
}

android {
    namespace = "app.mylauncher"
    compileSdk = 36

    defaultConfig {
        applicationId = "app.mylauncher"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
}
