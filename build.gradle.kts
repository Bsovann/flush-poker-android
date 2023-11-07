buildscript {
    val agp_version by extra("7.0.0")
    val agp_version1 by extra("8.0.0")
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.2" apply false
}
val compileSdkVersion by extra(34)
