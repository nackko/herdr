package com.ludoscity.herdr.common.di

import co.touchlab.kermit.Kermit
import co.touchlab.kermit.NSLogLogger
import com.ludoscity.herdr.common.data.SecureDataStore
import com.ludoscity.herdr.common.data.database.HerdrDatabase
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.drivers.native.NativeSqliteDriver
import kotlinx.cinterop.ObjCClass
import kotlinx.cinterop.getOriginalKotlinClass
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.Qualifier
import org.koin.dsl.module

fun initKoinIos(
    secureDataStore: SecureDataStore
): KoinApplication = initKoin {
    module {
        single { secureDataStore }
    }
}

actual val platformModule = module {
    //single<Settings> {
    //    val userDefaults = NSUserDefaults(suiteName = "KAMPSTARTER_SETTINGS")
    //    AppleSettings(userDefaults)
    //}
    single<SqlDriver> { NativeSqliteDriver(HerdrDatabase.Schema, "herdr.db") }
    //TODO: fix secure data store implementation
    //single { SecureDataStore() }

    val baseKermit = Kermit(NSLogLogger()).withTag("Herdr")
    factory { (tag: String?) -> if (tag != null) baseKermit.withTag(tag) else baseKermit }
}

fun Koin.get(objCClass: ObjCClass, qualifier: Qualifier?, parameter: Any): Any {
    val kClazz = getOriginalKotlinClass(objCClass)!!
    return get(kClazz, qualifier) { parametersOf(parameter) }
}

fun Koin.get(objCClass: ObjCClass, parameter: Any): Any {
    val kClazz = getOriginalKotlinClass(objCClass)!!
    return get(kClazz, null) { parametersOf(parameter) }
}

fun Koin.get(objCClass: ObjCClass, qualifier: Qualifier?): Any {
    val kClazz = getOriginalKotlinClass(objCClass)!!
    return get(kClazz, qualifier, null)
}

//object KoinIOS {
//    val koin = initKoin { }
//
//    fun get(objCClass: ObjCClass, qualifier: Qualifier?, parameter: Any): Any {
//        val kClazz = getOriginalKotlinClass(objCClass)!!
//        return koin.koin.get(kClazz, qualifier) { parametersOf(parameter) }
//    }
//
//    fun get(objCClass: ObjCClass, parameter: Any): Any {
//        val kClazz = getOriginalKotlinClass(objCClass)!!
//        return koin.koin.get(kClazz, null) { parametersOf(parameter) }
//    }
//
//    fun get(objCClass: ObjCClass, qualifier: Qualifier?): Any {
//        val kClazz = getOriginalKotlinClass(objCClass)!!
//        return koin.koin.get(kClazz, qualifier, null)
//    }
//}
