import org.gradle.api.JavaVersion

object Versions {
    object Kotlin {
        const val lang = "1.6.10"
        const val coroutines = "1.6.4"
        const val serialization = "1.3.0"
    }

    object Java {
        const val jvmTarget = "11"
        val java = JavaVersion.VERSION_11
    }

    const val ksp = "${Kotlin.lang}-1.0.0"
    const val kotlinPoet = "1.10.1"
    const val ktlint = "0.41.0"

    const val androidx_test = "1.4.0"
    const val extJUnitVersion = "1.1.3-rc01"
    const val espressoVersion = "3.4.0"

    const val compose = "1.1.1"
    const val activity = "1.4.0"
    const val lifecycle = "2.4.0"
    const val coreKtx = "1.8.0"
    const val navigation = "2.5.0"

    const val hilt = "2.42"
}