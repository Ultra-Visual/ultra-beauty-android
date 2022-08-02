// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    extra.apply {
        set("compose_version", "1.1.0-beta01")
    }
}
plugins {
    id ("com.android.application") version "7.2.1" apply false
    id ("com.android.library") version "7.2.1" apply false
    id ("org.jetbrains.kotlin.android") version "1.5.31" apply false
//    id "kotlin-kapt" version "1.4.20" apply false
}

tasks.create<Delete>("clean") {
    delete(rootProject.buildDir)
}
