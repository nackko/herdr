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
    val targetSdk = 29
    val compileSdk = 29
    val buildTools = "29.0.3"

    val androidxLifecycle = "2.2.0"
    val anko = "0.10.3"
    val appCompat = "1.2.0"
    val appAuth = "0.7.1"
    //val cocoapodsext = "0.9" //??
    val constraintLayout = "2.0.1"
    val coroutines = "1.3.9-native-mt"
    val dateTime = "0.1.0"
    val deviceNames = "1.1.7"
    val fragment = "1.2.5"
    val androidGradlePlugin = "4.0.1"
    val kermit = "0.1.8"
    val koin = "3.0.0-alpha-4"
    val kotlin = "1.4.0"
    val kPermissions = "1.0.0"
    val ktor = "1.4.0"
    val materialDesign = "1.2.1"
    val mokoMvvm = "0.8.0"
    val navigation = "2.3.0"
    val playServices = "17.0.0"
    val securityCrypto = "1.0.0-rc02"
    val serializer = "1.0.0-RC"
    val sqlDelight = "1.4.1"
    val work = "2.4.0"
}

object Deps {

    //TODO: FIXME -- anko is deprecated
    val anko = "org.jetbrains.anko:anko-commons:${Versions.anko}"
    val androidGradlePlugin = "com.android.tools.build:gradle:${Versions.androidGradlePlugin}"
    val appCompatX = "androidx.appcompat:appcompat:${Versions.appCompat}"
    val appAuth = "net.openid:appauth:${Versions.appAuth}"
    val constraintlayoutX = "androidx.constraintlayout:constraintlayout:${Versions.constraintLayout}"
    val devicesName = "com.jaredrummler:android-device-names:${Versions.deviceNames}"
    val fragmentX = "androidx.fragment:fragment:${Versions.fragment}"
    val googleMobileService_GooglePlayServicesLocation = "com.google.android.gms:play-services-location:${Versions.playServices}"
    val kPermissions = "com.github.fondesa:kpermissions:${Versions.kPermissions}"
    val materialDesign = "com.google.android.material:material:${Versions.materialDesign}"
    val navigationFragmentX = "androidx.navigation:navigation-fragment-ktx:${Versions.navigation}"
    val navigationUiX = "androidx.navigation:navigation-ui-ktx:${Versions.navigation}"
    val securityCrypto = "androidx.security:security-crypto:${Versions.securityCrypto}"
    val work = "androidx.work:work-runtime-ktx:${Versions.work}"

    object Coroutines {
        //val gradle = "com.squareup.sqldelight:gradle-plugin:${Versions.sqlDelight}"
        val common = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
        val native = "org.jetbrains.kotlinx:kotlinx-coroutines-core-native:${Versions.coroutines}"
        val android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}"
        //val test = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.coroutines}"
    }

    object Ktor {
        val commonCore = "io.ktor:ktor-client-core:${Versions.ktor}"
        val commonJson = "io.ktor:ktor-client-json:${Versions.ktor}"
        val commonLogging = "io.ktor:ktor-client-logging:${Versions.ktor}"
        val commonSerialization = "io.ktor:ktor-client-serialization:${Versions.ktor}"

        //implementation "io.ktor:ktor-client-core-jvm:$ktorVersion"

        val androidCore = "io.ktor:ktor-client-core-jvm:${Versions.ktor}"
        val android = "io.ktor:ktor-client-android:${Versions.ktor}"
        val androidJson = "io.ktor:ktor-client-json-jvm:${Versions.ktor}"
        val androidLogging = "io.ktor:ktor-client-logging-jvm:${Versions.ktor}"
        val androidSerialization = "io.ktor:ktor-client-serialization-jvm:${Versions.ktor}"

        val ios = "io.ktor:ktor-client-ios:${Versions.ktor}"
        val iosCore = "io.ktor:ktor-client-core-native:${Versions.ktor}"
        val iosJson = "io.ktor:ktor-client-json-native:${Versions.ktor}"
        val iosLogging = "io.ktor:ktor-client-logging-native:${Versions.ktor}"
        val iosSerialization = "io.ktor:ktor-client-serialization-native:${Versions.ktor}"
    }

    //object Koin {
    //    val common = "org.koin:koin-core:${Versions.koin}"
    //}

    object MokoMvvm {
        val common = "dev.icerock.moko:mvvm:${Versions.mokoMvvm}"
        val android = "androidx.lifecycle:lifecycle-extensions:${Versions.androidxLifecycle}"
    }

    object Serialization {
        val commonCore =
                "org.jetbrains.kotlinx:kotlinx-serialization-core:${Versions.serializer}"
        val commonProtobuf =
                "org.jetbrains.kotlinx:kotlinx-serialization-protobuf:${Versions.serializer}"
        val ios = "org.jetbrains.kotlinx:kotlinx-serialization-runtime-native:${Versions.serializer}"
    }

    object DateTime {
        val common = "org.jetbrains.kotlinx:kotlinx-datetime:${Versions.dateTime}"
    }

    object Kermit {
        val commonApi = "co.touchlab:kermit:${Versions.kermit}"
    }

    object Koin {
        val common = "org.koin:koin-core:${Versions.koin}"
    }

    object SqlDelight {
        val gradle = "com.squareup.sqldelight:gradle-plugin:${Versions.sqlDelight}"
        val common = "com.squareup.sqldelight:runtime:${Versions.sqlDelight}"
        val android = "com.squareup.sqldelight:android-driver:${Versions.sqlDelight}"
        val ios = "com.squareup.sqldelight:native-driver:${Versions.sqlDelight}"
    }

}
