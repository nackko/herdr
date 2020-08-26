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

package com.ludoscity.herdr.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

object Utils {
    fun getSimpleDateFormatPattern(): String {
        //see: https://developer.android.com/reference/java/text/SimpleDateFormat
        //https://stackoverflow.com/questions/28373610/android-parse-string-to-date-unknown-pattern-character-x
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
        else
            "yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ"
    }
}
/**
 * Extension function to start foreground services
 *
 * @param service   the intent of service to be started
 */
fun Context.startServiceForeground(service: Intent) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(service)
    } else {
        startService(service)
    }
}