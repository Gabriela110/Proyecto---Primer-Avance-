pluginManagement {
    repositories {
        google()        // <-- AQUÍ ES DONDE GRADLE BUSCA EL PLUGIN DE ANDROID
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Proyecto-Primer-Avance"