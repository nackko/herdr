package com.ludoscity.herdr.common.di

import com.ludoscity.herdr.common.ApplicationDispatcher
import com.ludoscity.herdr.common.data.database.HerdrDatabase
import com.ludoscity.herdr.common.data.network.INetworkDataPipe
import com.ludoscity.herdr.common.data.network.cozy.CozyCloupApi
import com.ludoscity.herdr.common.data.repository.AnalTrackingRepository
import com.ludoscity.herdr.common.data.repository.GeoTrackingRepository
import com.ludoscity.herdr.common.data.repository.LoginRepository
import com.ludoscity.herdr.common.domain.usecase.analytics.SaveAnalyticsDatapointUseCaseAsync
import com.ludoscity.herdr.common.domain.usecase.geotracking.SaveGeotrackingDatapointUseCaseAsync
import com.ludoscity.herdr.common.domain.usecase.login.*
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
    single { GeoTrackingRepository() }
    single { AnalTrackingRepository() }

    //use case
    single { RegisterAuthClientUseCaseAsync() }
    single { UnregisterAuthClientUseCaseAsync() }
    single { RetrieveAccessAndRefreshTokenUseCaseAsync() }
    single { RefreshAccessAndRefreshTokenUseCaseAsync() }
    single { CreateDirectoryUseCaseAsync() }
    single { SaveGeotrackingDatapointUseCaseAsync() }
    single { SaveAnalyticsDatapointUseCaseAsync() }
}

internal inline fun <reified T> Scope.getWith(vararg params: Any?): T {
    return get(parameters = { parametersOf(*params) })
}

expect val platformModule: Module