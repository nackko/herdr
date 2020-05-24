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

package com.ludoscity.herdr.common.di

import com.ludoscity.herdr.common.ApplicationDispatcher
import com.ludoscity.herdr.common.data.network.INetworkDataPipe
import com.ludoscity.herdr.common.data.network.cozy.CozyCloupApi
import com.ludoscity.herdr.common.data.network.cozy.CozyDataPipe
import com.ludoscity.herdr.common.data.repository.GeoTrackingRepository
import com.ludoscity.herdr.common.data.repository.LoginRepository
import com.ludoscity.herdr.common.domain.usecase.injection.InjectDataStoreUseCaseSync
import com.ludoscity.herdr.common.domain.usecase.injection.InjectDatabaseUseCaseSync
import com.ludoscity.herdr.common.domain.usecase.login.RegisterAuthClientUseCaseAsync
import com.ludoscity.herdr.common.domain.usecase.login.RetrieveAccessAndRefreshTokenUseCaseAsync
import com.ludoscity.herdr.common.domain.usecase.login.UnregisterAuthClientUseCaseAsync
import org.kodein.di.Kodein
import org.kodein.di.erased.bind
import org.kodein.di.erased.instance
import org.kodein.di.erased.provider
import org.kodein.di.erased.singleton
import kotlin.coroutines.CoroutineContext
import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
val KodeinInjector = Kodein {

    //coroutine
    bind<CoroutineContext>() with provider { ApplicationDispatcher }

    //use case
    bind<RegisterAuthClientUseCaseAsync>() with singleton {
        RegisterAuthClientUseCaseAsync(
            instance()
        )
    }

    bind<UnregisterAuthClientUseCaseAsync>() with singleton {
        UnregisterAuthClientUseCaseAsync(
            instance()
        )
    }

    bind<RetrieveAccessAndRefreshTokenUseCaseAsync>() with singleton {
        RetrieveAccessAndRefreshTokenUseCaseAsync(
            instance()
        )
    }

    bind<InjectDataStoreUseCaseSync>() with singleton {
        InjectDataStoreUseCaseSync(
            instance()
        )
    }

    bind<InjectDatabaseUseCaseSync>() with singleton {
        InjectDatabaseUseCaseSync(
            instance()
        )
    }

    //repo
    bind<LoginRepository>() with singleton { LoginRepository(instance()) }
    bind<GeoTrackingRepository>() with singleton { GeoTrackingRepository() }

    //data pipe
    bind<INetworkDataPipe>() with provider { CozyDataPipe(instance()) }

    //api
    bind<CozyCloupApi>() with provider { CozyCloupApi() }
}