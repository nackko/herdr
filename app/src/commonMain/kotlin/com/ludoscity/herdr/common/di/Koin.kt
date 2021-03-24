package com.ludoscity.herdr.common.di

import com.ludoscity.herdr.common.ApplicationDispatcher
import com.ludoscity.herdr.common.data.database.HerdrDatabase
import com.ludoscity.herdr.common.data.network.INetworkDataPipe
import com.ludoscity.herdr.common.data.network.cozy.CozyCloupApi
import com.ludoscity.herdr.common.data.repository.*
import com.ludoscity.herdr.common.domain.usecase.analytics.*
import com.ludoscity.herdr.common.domain.usecase.geotracking.*
import com.ludoscity.herdr.common.domain.usecase.login.*
import com.ludoscity.herdr.common.domain.usecase.useractivity.*
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.parameter.parametersOf
import org.koin.core.scope.Scope
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

fun initKoin(appModule: Module): KoinApplication {
    @Suppress("UnnecessaryVariable") val koinApp = startKoin {
        modules(
            appModule,
            platformModule,
            coreModule
        )
    }

    //TODO: additional koin app configuration can happen here
    //see: https://github.com/touchlab/KaMPKit/blob/0b1a956b3a0c1ee417916e3835062f55eaba78b3/shared/src/commonMain/kotlin/co/touchlab/kampkit/Koin.kt

    return koinApp
}

private val coreModule = module {
    single { HerdrDatabase(get()) }
    single<INetworkDataPipe> { CozyCloupApi(getWith("CozyCloupApi")) }

    factory { ApplicationDispatcher }

    //repo
    single { LoginRepository() }
    single { UserActivityTrackingRepository() }
    single { GeoTrackingRepository() }
    single { AnalTrackingRepository() }
    single { HeadlessRepository() }

    //use case
    single { RegisterAuthClientUseCaseAsync() }
    single { UnregisterAuthClientUseCaseAsync() }
    single { RetrieveAccessAndRefreshTokenUseCaseAsync() }
    single { RefreshAccessAndRefreshTokenUseCaseAsync() }
    single { SetupDirectoryUseCaseAsync() }
    single { SaveGeoTrackingDatapointUseCaseAsync() }
    single { SaveAnalyticsDatapointUseCaseAsync() }
    single { UploadAllAnalyticsDatapointUseCaseAsync() }
    single { PurgeAllAnalyticsDatapointUseCaseAsync() }
    single { PurgeAllGeoTrackingDatapointUseCaseAsync() }
    single { UploadAllGeoTrackingDatapointUseCaseAsync() }
    single { UpdateUserLocGeoTrackingDatapointUseCaseSync() }
    single { ObserveGeoTrackingUseCaseSync() }
    single { ObserveLoggedInUseCaseSync() }
    single { UpdatePermissionGrantedUseCaseSync() }
    single { GetPermissionGrantedUseCaseSync() }
    single { UpdateGeoTrackingUseCaseSync() }
    single { UpdateUserActivityUseCaseSync() }
    single { ObserveUserActivityUseCaseSync() }
    single { ObserveGeoTrackUserActivityUseCaseSync() }
    single { UpdateWillGeoTrackUserActivityUseCaseAsync() }
    single { RetrieveWillGeoTrackUserActivityUseCaseAsync() }
    single { RetrieveLastUserActivityTimestampUseCaseAsync() }
    single { RetrieveUserActivityUseCaseSync() }
    single { CheckLoginStatusUseCaseAsync() }
}

internal inline fun <reified T> Scope.getWith(vararg params: Any?): T {
    return get(parameters = { parametersOf(*params) })
}

expect val platformModule: Module