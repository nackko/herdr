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

package com.ludoscity.herdr.common

import android.os.Handler
import android.os.Looper

actual class Timer actual constructor(
    actual val periodMilliSeconds: Long,
    block: () -> Boolean
) {

    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {
            if (block()) {
                handler.postDelayed(this, periodMilliSeconds)
            }
        }
    }

    actual fun start() {
        handler.postDelayed(runnable, periodMilliSeconds)
    }

    actual fun stop() {
        handler.removeCallbacks(runnable)
    }
}