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

repositories {
    google()
    jcenter()
    mavenCentral()
}

plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    kotlin("kapt")
    kotlin("plugin.serialization") version (Versions.kotlin)
    id("com.android.application")
    id("com.squareup.sqldelight")
}


/*
dependencies {
    androidTestImplementation 'androidx.test.ext:junit:1.1.1'
    //Eurk! See: https://github.com/icerockdev/moko-mvvm/commit/bc34f2f019b55689df1bf411e13c5272151f139d#diff-7fcaab89343780d7e4c6e3ed99266fcd
    // fix of package javax.annotation does not exist import javax.annotation.Generated in DataBinding code
    compileOnly("javax.annotation:jsr250-api:1.0")
}
* */

android {
    compileSdkVersion(Versions.compileSdk)
    defaultConfig {
        applicationId = "com.ludoscity.herdr"
        minSdkVersion(Versions.minSdk)
        targetSdkVersion(Versions.targetSdk)
        versionCode = 1
        versionName = "1.0"
        manifestPlaceholders["appAuthRedirectScheme"] = "com.ludoscity.herdr"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            //buildConfigField "String", "DATABASE_NAME", "\"findmybikes-database\""
        }
        getByName("debug") {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
            //signingConfig signingConfigs.debug
            isDebuggable = true
            //buildConfigField "String", "DATABASE_NAME", "\"findmybikes-database-debug\""
        }
    }

    dataBinding {
        isEnabled = true
    }

    packagingOptions {
        exclude("META-INF/proguard/coroutines.pro")
        exclude("META-INF/*.kotlin_module")
    }
}

kotlin {
    android()
    //Revert to just ios() when gradle plugin can properly resolve it
    val onPhone = System.getenv("SDK_NAME")?.startsWith("iphoneos") ?: false
    if (onPhone) {
        iosArm64("ios")
    } else {
        iosX64("ios")
    }

    //targets.getByName<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>("ios").compilations["main"].kotlinOptions.freeCompilerArgs +=
    //    listOf("-Xobjc-generics", "-Xg0")

    version = "1.0"

    /*sourceSets {
        all {
            languageSettings.apply {
                useExperimentalAnnotation("kotlin.RequiresOptIn")
                useExperimentalAnnotation("kotlinx.coroutines.ExperimentalCoroutinesApi")
            }
        }
    }*/

    sourceSets["commonMain"].dependencies {
        api(kotlin("stdlib-common", Versions.kotlin))
        implementation(Deps.Coroutines.common)
        implementation(Deps.Serialization.common)
        implementation(Deps.MokoMvvn.common)
        implementation(Deps.koinCore)
        implementation(Deps.Ktor.commonCore)
        implementation(Deps.Ktor.commonLogging)
        implementation(Deps.Ktor.commonJson)
        implementation(Deps.Ktor.commonSerialization)
        implementation(Deps.SqlDelight.runtime)
        api(Deps.kermit)
    }

    sourceSets["commonTest"].dependencies {
        /*implementation kotlin('test-common')
                implementation kotlin('test-annotations-common')*/

    }
    sourceSets["androidMain"].dependencies {
        implementation(kotlin("stdlib", Versions.kotlin))
        implementation(kotlin("stdlib-common", Versions.kotlin))
        implementation(Deps.appCompatX)
        implementation(Deps.fragmentX)
        implementation(Deps.constraintlayoutX)
        implementation(Deps.navigationFragmentX)
        implementation(Deps.navigationUiX)
        implementation(Deps.securityCrypto)
        //https://github.com/openid/AppAuth-Android
        implementation(Deps.appAuth)
        implementation(Deps.Coroutines.android)
        implementation(Deps.MokoMvvn.android)
        implementation(Deps.Ktor.androidJson)
        implementation(Deps.Ktor.android)
        implementation(Deps.Ktor.androidLogging)
        implementation(Deps.Ktor.androidSerialization)
        implementation(Deps.SqlDelight.driverAndroid)

        // fix of package javax.annotation does not exist import javax.annotation.Generated in DataBinding code
        compileOnly("javax.annotation:jsr250-api:1.0")
    }

    sourceSets["androidTest"].dependencies {
        //implementation kotlin('test')
        //implementation kotlin('test-junit')
    }

    sourceSets["iosMain"].dependencies {
        implementation(Deps.Serialization.ios)
        implementation(Deps.Coroutines.native) {
            version {
                strictly("1.3.5-native-mt")
            }
        }
        implementation(Deps.Ktor.ios)
        implementation(Deps.Ktor.iosCore)
        implementation(Deps.Ktor.iosLogging)
        implementation(Deps.Ktor.iosJson)
        implementation(Deps.Ktor.iosSerialization)
        implementation(Deps.SqlDelight.driverIos)
    }

    sourceSets["iosTest"].dependencies { }

    cocoapods {
        summary = "Common libary for herdr"
        homepage = "https://github.com/f8full/herdr"
        frameworkName = "MultiPlatformLibrary"
    }
}

sqldelight {
    database("HerdrDatabase") {
        packageName = "com.ludoscity.herdr.common.data.database"
    }
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> { kotlinOptions.jvmTarget = "1.8" }
