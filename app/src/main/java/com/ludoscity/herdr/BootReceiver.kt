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

package com.ludoscity.herdr

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ludoscity.herdr.common.base.Response
import com.ludoscity.herdr.common.domain.usecase.login.CheckLoginStatusUseCaseAsync
import com.ludoscity.herdr.data.transrecognition.TransitionRecognitionService
import com.ludoscity.herdr.utils.startServiceForeground
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.intentFor
import org.koin.core.KoinComponent
import org.koin.core.inject

class BootReceiver : BroadcastReceiver(), KoinComponent {

    private val checkLoginStatusUseCaseAsync: CheckLoginStatusUseCaseAsync by inject()

    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            GlobalScope.launch(Dispatchers.IO) {
                val response = checkLoginStatusUseCaseAsync.execute()

                if (response is Response.Success && response.data) {
                    context.startServiceForeground(context.intentFor<TransitionRecognitionService>())
                }
            }
        }
    }
}