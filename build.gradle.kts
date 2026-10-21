plugins {
    kotlin("multiplatform") version "2.1.0"
    id("com.android.application") version "8.7.3"
    id("org.jetbrains.compose") version "1.7.3"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0"
}

kotlin {
    jvm("desktop")
    androidTarget()

    sourceSets {
        //IGNORAMOS LA ANTIGUA CARPETA DE VIEWS SIN ELIMINARLA, PARA MANTENER COMO RESPALDO
        commonMain {
            kotlin.exclude("**/views/**")
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
        }

        getByName("desktopMain").dependencies {
            implementation(compose.desktop.currentOs)
            implementation(compose.uiTooling)
        }

        getByName("androidMain").dependencies {
            implementation("androidx.activity:activity-compose:1.9.3")
            implementation(compose.uiTooling)
            implementation("androidx.compose.ui:ui-tooling-preview:1.7.3")
        }
    }
}

android {
    namespace = "com.tuusuario.superapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.tuusuario.superapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        jvmArgs += listOf("--enable-native-access=ALL-UNNAMED")
    }
}