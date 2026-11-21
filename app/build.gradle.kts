plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.oylfrutossecos"

    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.oylfrutossecos"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "CATALOGO_HOST", "\"\"")
        buildConfigField("boolean", "GEOCODIFICACION_DEMO", "false")
    }

    val instalarPruebaAislada = providers.gradleProperty("oylPruebaUsb").orNull == "true"
    buildTypes {
        debug {
            val geocodificacionDemo = providers.gradleProperty("oylNominatimDemo").orNull == "true"
            buildConfigField("boolean", "GEOCODIFICACION_DEMO", geocodificacionDemo.toString())
            val host = if (instalarPruebaAislada) "127.0.0.1" else "10.0.2.2"
            buildConfigField("String", "CATALOGO_HOST", "\"$host\"")
            if (instalarPruebaAislada) {
                applicationIdSuffix = ".pruebausb"
            }
        }
        release {
            buildConfigField("String", "CATALOGO_HOST", "\"127.0.0.1\"")
            buildConfigField("boolean", "GEOCODIFICACION_DEMO", "true")

            optimization {
                enable = false
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    testImplementation(libs.junit)
    // Android realista en JVM para JSON, preferencias y procesamiento de imágenes.
    testImplementation("org.robolectric:robolectric:4.16")
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")

    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Escaner QR, compatible con el que ya utilizaba el proyecto.
    implementation("com.google.android.gms:play-services-code-scanner:16.1.0")

    // Room y MVVM.
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")

    // Mapa sencillo con datos OSM y teselas OpenFreeMap.
    // MapLibre OpenGL evita pedir imagenes a tile.openstreetmap.org (403).
    implementation("org.maplibre.gl:android-sdk-opengl:13.7.1")

    // Fotografías de productos con Coil 3
    implementation("io.coil-kt.coil3:coil-compose:3.4.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.4.0")


    // Kotest: aserciones más legibles
    testImplementation(
        "io.kotest:kotest-assertions-core:5.9.1"
    )

    // MockK: simulación de dependencias
    testImplementation(
        "io.mockk:mockk:1.14.5"
    )

    // Pruebas de corrutinas
    testImplementation(
        "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2"
    )

}
