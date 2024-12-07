plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.securitymerch"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.securitymerch"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.gridlayout)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation("com.google.firebase:firebase-messaging:23.2.0") // Para notificaciones Firebase

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Glide para manejo de imágenes
    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")

    // Lottie para animaciones
    implementation("com.airbnb.android:lottie:3.7.0")

    // Material Components
    implementation("com.google.android.material:material:1.9.0")

    // Play Services Tasks
    implementation("com.google.android.gms:play-services-tasks:18.0.2")

    // Firebase BoM para gestionar versiones
    implementation(platform("com.google.firebase:firebase-bom:32.0.0"))

    // ML Kit para escaneo de código de barras
    implementation("com.google.mlkit:barcode-scanning:17.0.2")
    implementation("com.google.android.gms:play-services-mlkit-barcode-scanning:18.0.0")

    // iText7 para generación de PDFs
    implementation("com.itextpdf:itext7-core:7.2.3")
}
