plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.assignmentplanner"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.assignmentplanner"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.googleid)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    
    // External Libraries & SDKs for Rubric
    // implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // implementation("androidx.room:room-runtime:2.6.1")
    // implementation("androidx.room:room-ktx:2.6.1")
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}