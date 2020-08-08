/*
 *     Copyright (c) 2020. f8full https://github.com/f8full
 *     Herdr is a privacy conscious multiplatform mobile data collector
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
object Versions {
    val minSdk = 23
    val targetSdk = 28
    val compileSdk = 28

    val androidxLifecycle = "2.2.0"
    val appCompat = "1.1.0"
    val appAuth = "0.7.1"
    val cocoapodsExt = "0.9"
    val constraintLayout = "1.1.3"
    val coroutines = "1.3.5"
    val fragment = "1.2.4"
    val androidGradlePlugin = "3.6.2"
    val kermit = "0.1.7"
    val koin = "3.0.0-alpha-2"
    val kotlin = "1.3.72"
    val ktor = "1.3.2"
    val materialDesign = "1.1.0"
    val mokoMvvm = "0.7.1"
    val navigation = "2.2.2"
    val securityCrypto = "1.0.0-rc01"
    val serializer = "0.20.0"
    val sqlDelight = "1.4.0"
}

object Deps {

    val androidGradlePlugin = "com.android.tools.build:gradle:${Versions.androidGradlePlugin}"
    val appCompatX = "androidx.appcompat:appcompat:${Versions.appCompat}"
    val appAuth = "net.openid:appauth:${Versions.appAuth}"
    val cocoapodsExt = "co.touchlab:kotlinnativecocoapods:${Versions.cocoapodsExt}"
    val constraintlayoutX = "androidx.constraintlayout:constraintlayout:${Versions.constraintLayout}"
    val fragmentX = "androidx.fragment:fragment:${Versions.fragment}"
    val kermit = "co.touchlab:kermit:${Versions.kermit}"
    val koinCore = "org.koin:koin-core:${Versions.koin}"
    val navigationFragmentX = "androidx.navigation:navigation-fragment-ktx:${Versions.navigation}"
    val navigationUiX = "androidx.navigation:navigation-ui-ktx:${Versions.navigation}"
    val securityCrypto = "androidx.security:security-crypto:${Versions.securityCrypto}"

    object Coroutines {
        val gradle = "com.squareup.sqldelight:gradle-plugin:${Versions.sqlDelight}"
        val common = "org.jetbrains.kotlinx:kotlinx-coroutines-core-common:${Versions.coroutines}"
        val native = "org.jetbrains.kotlinx:kotlinx-coroutines-core-native:${Versions.coroutines}"
        val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}"
        //val test = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.coroutines}"
    }

    object Ktor {
        val commonCore = "io.ktor:ktor-client-core:${Versions.ktor}"
        val commonJson = "io.ktor:ktor-client-json:${Versions.ktor}"
        val commonLogging = "io.ktor:ktor-client-logging:${Versions.ktor}"
        val android = "io.ktor:ktor-client-android:${Versions.ktor}"

        //val androidCore = "io.ktor:ktor-client-okhttp:${Versions.ktor}"
        val androidJson = "io.ktor:ktor-client-json-jvm:${Versions.ktor}"
        val androidLogging = "io.ktor:ktor-client-logging-jvm:${Versions.ktor}"
        val ios = "io.ktor:ktor-client-ios:${Versions.ktor}"
        val iosCore = "io.ktor:ktor-client-core-native:${Versions.ktor}"
        val iosJson = "io.ktor:ktor-client-json-native:${Versions.ktor}"
        val iosLogging = "io.ktor:ktor-client-logging-native:${Versions.ktor}"
        val commonSerialization = "io.ktor:ktor-client-serialization:${Versions.ktor}"
        val androidSerialization = "io.ktor:ktor-client-serialization-jvm:${Versions.ktor}"
        val iosSerialization = "io.ktor:ktor-client-serialization-native:${Versions.ktor}"
    }

    object MokoMvvn {
        val common = "dev.icerock.moko:mvvm:${Versions.mokoMvvm}"
        val android = "androidx.lifecycle:lifecycle-extensions:${Versions.androidxLifecycle}"
        val iosX64 = "dev.icerock.moko:mvvm-iosx64:${Versions.mokoMvvm}"
        val iodArm64 = "dev.icerock.moko:mvvm-iosarm64:${Versions.mokoMvvm}"
    }

    object Serialization {
        val common =
            "org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:${Versions.serializer}"
        val ios = "org.jetbrains.kotlinx:kotlinx-serialization-runtime-native:${Versions.serializer}"
    }

    object SqlDelight {
        val gradle = "com.squareup.sqldelight:gradle-plugin:${Versions.sqlDelight}"
        val runtime = "com.squareup.sqldelight:runtime:${Versions.sqlDelight}"
        val driverIos = "com.squareup.sqldelight:native-driver:${Versions.sqlDelight}"
        val driverAndroid = "com.squareup.sqldelight:android-driver:${Versions.sqlDelight}"
    }

}

