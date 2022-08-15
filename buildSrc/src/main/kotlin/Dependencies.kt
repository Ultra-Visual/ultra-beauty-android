import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
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

fun DependencyHandler.koin() {

}

fun DependencyHandler.compose() {
    implementation("androidx.compose.ui:ui", Versions.compose)
    implementation("androidx.compose.ui:ui-tooling", Versions.compose)
    implementation("androidx.compose.foundation:foundation", Versions.compose)
    implementation("androidx.compose.animation:animation", Versions.compose)
    implementation("androidx.compose.material:material", Versions.compose)
    implementation("androidx.compose.material:material-icons-core", Versions.compose)
    implementation("androidx.compose.material:material-icons-extended", Versions.compose)
    implementation("androidx.navigation:navigation-compose", Versions.navigation)
}

fun DependencyHandler.android() {
    lifecycle()
    api("androidx.core:core-ktx", Versions.coreKtx)
    implementation("androidx.activity:activity-ktx", Versions.activity)
    implementation("androidx.activity:activity-compose", Versions.activity)
    implementation("androidx.appcompat:appcompat", Versions.activity)
    implementation("com.google.android.material:material", "1.6.1")
//    implementation("io.coil-kt:coil-compose", Versions.coil)
}

fun DependencyHandler.lifecycle() {
    implementation("androidx.lifecycle:lifecycle-runtime-ktx", Versions.lifecycle)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx", Versions.lifecycle)
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate", Versions.lifecycle)
    implementation("androidx.lifecycle:lifecycle-common-java8", Versions.lifecycle)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose", Versions.lifecycle)
}

fun DependencyHandler.kotlinCoroutines() {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core", Versions.Kotlin.coroutines)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android", Versions.Kotlin.coroutines)
}

fun DependencyHandler.hilt() {
    api("com.google.dagger:hilt-android", Versions.hilt)
    annotationProcessor("com.google.dagger:hilt-compiler", Versions.hilt)
    kapt("com.google.dagger:hilt-android-compiler", Versions.hilt)
}

fun DependencyHandler.androidTest() {
    testImplementation("junit:junit:4.+")
    androidTestImplementation("androidx.arch.core:core-testing:2.1.0")
    androidTestImplementation("androidx.test:core", Versions.androidx_test)
    androidTestImplementation("androidx.test:runner", Versions.androidx_test)
    androidTestImplementation("androidx.test.ext:junit", Versions.extJUnitVersion)
    androidTestImplementation("androidx.test.espresso:espresso-core", Versions.espressoVersion)
    androidTestImplementation("androidx.compose.ui:ui-test", Versions.compose)
    debugImplementation("androidx.compose.ui:ui-tooling", Versions.compose)
}