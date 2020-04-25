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

package com.ludoscity.herdr.ui.drivesetup

import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.ludoscity.herdr.BR
import com.ludoscity.herdr.R
import com.ludoscity.herdr.common.ui.drivesetup.DriveSetupViewModel
import com.ludoscity.herdr.databinding.FragmentDriveSetupBinding
import dev.icerock.moko.mvvm.MvvmEventsFragment
import dev.icerock.moko.mvvm.createViewModelFactory
import dev.icerock.moko.mvvm.dispatcher.eventsDispatcherOnMain

class DriveSetupFragment : MvvmEventsFragment<FragmentDriveSetupBinding, DriveSetupViewModel,
        DriveSetupViewModel.DriveSetupFragmentEventListener>(), DriveSetupViewModel.DriveSetupFragmentEventListener {
    override val layoutId: Int = R.layout.fragment_drive_setup
    override val viewModelVariableId: Int = BR.driveSetupViewModel
    override val viewModelClass: Class<DriveSetupViewModel> = DriveSetupViewModel::class.java

    override fun viewModelFactory(): ViewModelProvider.Factory {
        return createViewModelFactory { DriveSetupViewModel(eventsDispatcherOnMain()) }
    }

    override fun routeToDriveLogin() {
        this.findNavController().navigate(R.id.action_driveSetupFragment_to_driveLoginFragment)
    }
}
