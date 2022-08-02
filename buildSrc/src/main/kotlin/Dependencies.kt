import org.gradle.api.Project
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.repositories

fun Project.configRepository() {
    repositories {
        google()
        mavenCentral()
        jcenter()
        maven("https://jitpack.io")
    }
}

fun DependencyHandlerScope.koin() {

}

fun DependencyHandlerScope.compose() {
    implementation("androidx.compose.ui:ui", Versions.compose)
    implementation("androidx.compose.ui:ui-tooling", Versions.compose)
    implementation("androidx.compose.foundation:foundation", Versions.compose)
    implementation("androidx.compose.animation:animation", Versions.compose)
    implementation("androidx.compose.material:material", Versions.compose)
    implementation("androidx.compose.material:material-icons-core", Versions.compose)
    implementation("androidx.compose.material:material-icons-extended", Versions.compose)
    implementation("androidx.navigation:navigation-compose", Versions.navigation)
}

fun DependencyHandlerScope.android() {
    lifecycle()
    api("androidx.core:core-ktx", Versions.coreKtx)
    implementation("androidx.activity:activity-ktx", Versions.activity)
    implementation("androidx.activity:activity-compose", Versions.activity)
//    implementation("io.coil-kt:coil-compose", Versions.coil)
}

fun DependencyHandlerScope.lifecycle() {
    implementation("androidx.lifecycle:lifecycle-runtime-ktx", Versions.lifecycle)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx", Versions.lifecycle)
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate", Versions.lifecycle)
    implementation("androidx.lifecycle:lifecycle-common-java8", Versions.lifecycle)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose", Versions.lifecycle)
}

fun DependencyHandlerScope.kotlinCoroutines() {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core", Versions.Kotlin.coroutines)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android", Versions.Kotlin.coroutines)
}

