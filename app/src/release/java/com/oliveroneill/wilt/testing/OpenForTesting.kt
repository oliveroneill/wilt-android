package com.android.example.github.testing

/**
 * The release build does not use [OpenClass] annotation, so any attempts to inherit from
 * those classes will fail the build in release mode.
 *
 * Taken from https://github.com/googlesamples/android-architecture-components/blob/master/GithubBrowserSample/app/src/release/java/com/android/example/github/testing/OpenForTesting.kt
 */
@Target(AnnotationTarget.CLASS)
annotation class OpenForTesting
