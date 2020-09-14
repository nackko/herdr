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

actual class Timer actual constructor(
    actual val periodMilliSeconds: Long,
    private val block: () -> Boolean
) {
    private val runQueue = dispatch_get_main_queue()
    private var currentTimerRun: Long = Long.MIN_VALUE

    actual fun start() {
        scheduleDispatch(currentTimerRun)
    }

    actual fun stop() {
        // up run & current ran block not execute
        currentTimerRun++
        if (currentTimerRun == Long.MAX_VALUE) {
            currentTimerRun = Long.MIN_VALUE
        }
    }

    private fun scheduleDispatch(run: Long) {
        dispatch_after(
            dispatch_time(DISPATCH_TIME_NOW, periodMilliSeconds * NANOSEC_PER_MILLISEC),
            runQueue
        ) {
            if (currentTimerRun != run) return@dispatch_after

            if (block()) {
                scheduleDispatch(run)
            }
        }
    }

    private companion object {
        const val NANOSEC_PER_MILLISEC = 1000000
    }
}