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
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.parameter.parametersOf
import org.koin.core.scope.Scope
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

fun initKoin(appDeclaration: KoinAppDeclaration = {}) = startKoin {
    appDeclaration()
    modules(platformModule, coreModule)
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
}

internal inline fun <reified T> Scope.getWith(vararg params: Any?): T {
    return get(parameters = { parametersOf(*params) })
}

expect val platformModule: Module