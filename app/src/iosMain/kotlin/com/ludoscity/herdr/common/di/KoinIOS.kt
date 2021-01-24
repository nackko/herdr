package com.ludoscity.herdr.common.di

import com.ludoscity.herdr.common.data.SecureDataStore
import com.ludoscity.herdr.common.data.database.DbArgs
import com.ludoscity.herdr.common.data.database.HerdrDatabase
import com.ludoscity.herdr.common.data.database.getSqlDriver
import com.squareup.sqldelight.db.SqlDriver
import kotlinx.cinterop.ObjCClass
import kotlinx.cinterop.getOriginalKotlinClass
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.Qualifier
import org.koin.dsl.module

actual val platformModule = module {
    //single<Settings> {
    //    val userDefaults = NSUserDefaults(suiteName = "KAMPSTARTER_SETTINGS")
    //    AppleSettings(userDefaults)
    //}
    single<SqlDriver> { getSqlDriver(DbArgs())!! }
    single { SecureDataStore() }

    //val baseKermit = Kermit(NSLogLogger()).withTag("KampKit")
    //factory { (tag: String?) -> if (tag != null) baseKermit.withTag(tag) else baseKermit }
}

object KoinIOS {
    val koin = initKoin { }

    fun get(objCClass: ObjCClass, qualifier: Qualifier?, parameter: Any): Any {
        val kClazz = getOriginalKotlinClass(objCClass)!!
        return koin.koin.get(kClazz, qualifier) { parametersOf(parameter) }
    }

    fun get(objCClass: ObjCClass, parameter: Any): Any {
        val kClazz = getOriginalKotlinClass(objCClass)!!
        return koin.koin.get(kClazz, null) { parametersOf(parameter) }
    }

    fun get(objCClass: ObjCClass, qualifier: Qualifier?): Any {
        val kClazz = getOriginalKotlinClass(objCClass)!!
        return koin.koin.get(kClazz, qualifier, null)
    }
}
