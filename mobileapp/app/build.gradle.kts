
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.iothome"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.iothome"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"



    }
// GİZLİ BİLGİLERİ BURAYA TAŞIYORUZ - YENİ YAKLAŞIM
    // local.properties dosyasındaki değerleri String olarak alıyoruz.
    // 'getProperty()' yerine 'findProperty()' kullanmak, değer yoksa hata vermez.
    val mqttUsername = project.findProperty("MQTT_USERNAME") as String? ?: "DEFAULT_USERNAME"
    val mqttPassword = project.findProperty("MQTT_PASSWORD") as String? ?: "DEFAULT_PASSWORD"





    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // BuildConfig'e değerleri atıyoruz.
            buildConfigField("String", "MQTT_USERNAME", "\"$mqttUsername\"")
            buildConfigField("String", "MQTT_PASSWORD", "\"$mqttPassword\"")
        }

    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true

    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.ui.graphics)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.sceneform.base)
    implementation(libs.androidx.foundation.layout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    // Ikonlar (Lightbulb, vb.) için
    implementation(libs.androidx.material.icons.extended)
    // ViewModel'ı Compose'da kullanabilmek için (viewModel() fonksiyonu)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    // Eclipse Paho MQTT Client (Endüstri standardı MQTT kütüphanesi)
    implementation(libs.org.eclipse.paho.client.mqttv3)

    // Paho'nun Android servisini kullanmak için
    implementation(libs.org.eclipse.paho.android.service)

    // Coroutine desteği için (Asenkron veri akışı için kritik)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Coil - Resim Yükleme Kütüphanesi
    implementation("io.coil-kt:coil-compose:2.6.0") // Güncel sürümü kontrol edin
}