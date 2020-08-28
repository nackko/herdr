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

package com.ludoscity.herdr.common.ui.driveedit

import dev.icerock.moko.mvvm.dispatcher.EventsDispatcher
import dev.icerock.moko.mvvm.dispatcher.EventsDispatcherOwner
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import org.koin.core.KoinComponent

class DriveEditFragmentViewModel(override val eventsDispatcher: EventsDispatcher<DriveEditFragmentEventListener>) :
    KoinComponent,
    ViewModel(),
    EventsDispatcherOwner<DriveEditFragmentViewModel.DriveEditFragmentEventListener> {

    fun onLogoutButtonPressed() {
        eventsDispatcher.dispatchEvent { routeToDriveLogin() }
    }

    interface DriveEditFragmentEventListener {
        fun routeToDriveLogin()
    }
}
