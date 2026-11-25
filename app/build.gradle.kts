
import java.util.Properties

plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

// Read Google Maps API key from local.properties
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

android {
    namespace = "ke.ac.strathmore.campusexplorer"
    compileSdk = 36

    defaultConfig {
        applicationId = "ke.ac.strathmore.campusexplorer"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        manifestPlaceholders["MAPS_API_KEY"] =
            localProperties.getProperty("MAPS_API_KEY", "")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // AndroidX and Material
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.material)

    // Lifecycle and coroutines
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.kotlinx.coroutines.android)

    // Navigation
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)

    // RecyclerView and Swipe Refresh
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.swiperefreshlayout)

    // Firebase - use only ONE Firebase BOM
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)

    // Google Maps
    implementation(libs.play.services.maps)

    // Retrofit REST API
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
}
