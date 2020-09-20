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

package com.ludoscity.herdr.ui.drivesettings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.ludoscity.herdr.BR
import com.ludoscity.herdr.R
import com.ludoscity.herdr.common.ui.drivesettings.DriveSettingsFragmentViewModel
import com.ludoscity.herdr.databinding.FragmentDriveSettingsBinding
import com.ludoscity.herdr.ui.CustomTabsNavigator
import dev.icerock.moko.mvvm.MvvmEventsFragment
import dev.icerock.moko.mvvm.createViewModelFactory
import dev.icerock.moko.mvvm.dispatcher.eventsDispatcherOnMain

class DriveSettingsFragment : MvvmEventsFragment<FragmentDriveSettingsBinding, DriveSettingsFragmentViewModel,
        DriveSettingsFragmentViewModel.DriveSettingsFragmentEventListener>(), DriveSettingsFragmentViewModel.DriveSettingsFragmentEventListener {
    override val layoutId: Int = R.layout.fragment_drive_settings
    override val viewModelVariableId: Int = BR.driveSettingsViewModel
    override val viewModelClass: Class<DriveSettingsFragmentViewModel> =
        DriveSettingsFragmentViewModel::class.java

    override fun viewModelFactory(): ViewModelProvider.Factory {
        return createViewModelFactory {
            DriveSettingsFragmentViewModel(
                eventsDispatcherOnMain()
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        super.onCreateView(inflater, container, savedInstanceState)
        binding.folderNameText.text = arguments?.getString("folderName")
        binding.driveUrlText.text = arguments?.getString("stackUrl")

        return binding.root
    }

    override fun routeToStart() {
        this.findNavController().navigate(R.id.action_driveEditFragment_to_startFragment)
    }

    override fun routeToSeeCloudFolder() {
        val bundle = bundleOf(
            CustomTabsNavigator.URL_BUNDLE_KEY to computeCloudFolderUrl()
        )

        this.findNavController().navigate(
            R.id.action_driveEditFragment_to_view_cloud_folder,
            bundle
        )
    }

    private fun computeCloudFolderUrl(): String {
        val baseUrl = binding.driveUrlText.text.toString()

        val prefix = baseUrl.substringBefore(".")
        val drivePrefix = "$prefix-drive"

        return "${
            baseUrl.replace(
                prefix,
                drivePrefix
            )
        }/#/folder/${arguments?.getString("folderId")}"

    }
}
