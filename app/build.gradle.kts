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
    //kotlin("native.cocoapods")
    kotlin("kapt")
    //kotlin("android")
    //kotlin("android.extensions")
    kotlin("plugin.serialization") version (Versions.kotlin)
    id("com.android.application")
    id("com.squareup.sqldelight")
    id("dev.icerock.mobile.multiplatform") apply (false)
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
    buildToolsVersion(Versions.buildTools)
    defaultConfig {
        applicationId = "com.ludoscity.herdr"
        minSdkVersion(Versions.minSdk)
        targetSdkVersion(Versions.targetSdk)
        versionCode = 1
        versionName = "herdr-1.0"
        manifestPlaceholders["appAuthRedirectScheme"] = "com.ludoscity.herdr"
    }

    buildFeatures {
        dataBinding = true
    }

    buildTypes {
        /*getByName("alpha") {
            versionNameSuffix = "-alpha0"
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            buildConfigField("String", "DATABASE_NAME", "\"herdr-database.db\"")
            //FIXME: use technique from there (declare extensions, tried in the buildSrc folder structure to none avail.
            // https://medium.com/back-market-engineering/power-up-your-gradle-build-files-with-kts-7b77fbec3251
        }

        getByName("beta") {
            versionNameSuffix = "-beta0"
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            buildConfigField("String", "DATABASE_NAME", "\"herdr-database.db\"")
            //FIXME: use technique from there (declare extensions, tried in the buildSrc folder structure to none avail.
            // https://medium.com/back-market-engineering/power-up-your-gradle-build-files-with-kts-7b77fbec3251
        }*/

        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            buildConfigField("String", "DATABASE_NAME", "\"herdr-database.db\"")
            //buildConfigField "String", "DATABASE_NAME", "\"findmybikes-database\""
        }
        getByName("debug") {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
            //signingConfig signingConfigs.debug
            isDebuggable = true
            buildConfigField("String", "DATABASE_NAME", "\"herdr-database-debug.db\"")
            //buildConfigField "String", "DATABASE_NAME", "\"findmybikes-database-debug\""
        }
    }

    //dataBinding {
    //    isEnabled = true
    //}

    packagingOptions {
        exclude("META-INF/proguard/coroutines.pro")
        exclude("META-INF/*.kotlin_module")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    applicationVariants.all {
        this.resValue("string", "app_version_name", this.versionName ?: "missing_app_version_name")
    }

    /*buildFeatures {
        dataBinding = true
    }*/
}

kotlin {
    android()
    ios()

    //targets.getByName<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>("ios").compilations["main"].kotlinOptions.freeCompilerArgs +=
    //    listOf("-Xobjc-generics", "-Xg0")

    // CocoaPods requires the podspec to have a version.
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
        api(Deps.Kermit.commonApi)
        implementation(Deps.Coroutines.common) {
            version {
                strictly(Versions.coroutines)
            }
        }
        implementation(Deps.Serialization.commonCore)
        implementation(Deps.Serialization.commonProtobuf)
        api(Deps.MokoMvvm.core.common)
        api(Deps.MokoMvvm.liveData.common)
        api(Deps.mokoResources.common)
        implementation(Deps.Koin.common)
        implementation(Deps.DateTime.common)

        implementation(Deps.Ktor.commonCore)
        implementation(Deps.Ktor.commonLogging)
        implementation(Deps.Ktor.commonJson)
        implementation(Deps.Ktor.commonSerialization)

        implementation(Deps.SqlDelight.common)

    }

    sourceSets["commonTest"].dependencies {
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
    }
    sourceSets["androidMain"].dependencies {
        implementation(Deps.appCompatX)
        implementation(Deps.fragmentX)
        implementation(Deps.constraintlayoutX)
        implementation(Deps.navigationFragmentX)
        implementation(Deps.navigationUiX)
        implementation(Deps.securityCrypto)
        implementation(Deps.materialDesign)
        implementation(Deps.googleMobileService_GooglePlayServicesLocation)
        implementation(Deps.work)
        implementation(Deps.anko)
        implementation(Deps.devicesName)
        implementation(Deps.kPermissions)
        //https://github.com/openid/AppAuth-Android
        implementation(Deps.appAuth)
        implementation(Deps.Coroutines.android)
        api(Deps.MokoMvvm.dataBinding)

        implementation(Deps.Ktor.android)
        implementation(Deps.Ktor.androidCore)
        implementation(Deps.Ktor.androidJson)
        implementation(Deps.Ktor.androidLogging)
        implementation(Deps.Ktor.androidSerialization)

        implementation(Deps.SqlDelight.android)

        // fix of package javax.annotation does not exist import javax.annotation.Generated in DataBinding code
        compileOnly("javax.annotation:jsr250-api:1.0")
    }

    sourceSets["androidTest"].dependencies {
        implementation(kotlin("test"))
        implementation(kotlin("test-junit"))
    }

    sourceSets["iosMain"].dependencies {
        implementation(Deps.SqlDelight.ios)
        implementation(Deps.Ktor.ios)
    }

    sourceSets["iosArm64Main"].dependsOn(sourceSets["iosX64Main"])

    sourceSets["iosTest"].dependencies { }

    /*cocoapods {
        summary = "Common libary for herdr"
        homepage = "https://github.com/f8full/herdr"
        frameworkName = "MultiPlatformLibrary"
    }*/
}

sqldelight {
    database("HerdrDatabase") {
        packageName = "com.ludoscity.herdr.common.data.database"
    }
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> { kotlinOptions.jvmTarget = "1.8" }

// apply plugin only after android configuration because required android gradle plugin variants setup
afterEvaluate {
    apply(plugin = "dev.icerock.mobile.multiplatform")
    apply(plugin = "dev.icerock.mobile.multiplatform.ios-framework")

    configure<dev.icerock.gradle.FrameworkConfig> {
        export(Deps.MokoMvvm.core)
        export(Deps.MokoMvvm.liveData)
        export(Deps.mokoResources)
    }
}
