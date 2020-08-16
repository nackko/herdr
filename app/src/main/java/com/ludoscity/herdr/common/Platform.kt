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

import android.os.Build
import android.util.Base64
import com.jaredrummler.android.device.DeviceName
import com.ludoscity.herdr.BuildConfig
import com.ludoscity.herdr.utils.Utils
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

actual object Platform {
    actual val now: Long
        get() = System.currentTimeMillis()
    actual val nowString: String
        get() = SimpleDateFormat(Utils.getSimpleDateFormatPattern(), Locale.US)
            .format(Date(System.currentTimeMillis()))
    //TODO: timestamping of datapoints might show discrepancy between Long epoch and String one?

    actual val app_version: String = "Android:${BuildConfig.VERSION_NAME}"
    actual val api_level: Long = Build.VERSION.SDK_INT.toLong()
    actual val device_model: String = DeviceName.getDeviceName()
    actual val language: String
        get() = Locale.getDefault().language
    actual val country: String
        get() = Locale.getDefault().country

    actual fun toISO8601UTC(timestampString: String): String {
        val createdAtOriginal = SimpleDateFormat(Utils.getSimpleDateFormatPattern(), Locale.US)
            .parse(timestampString)
        return convertToISO8601UTC(createdAtOriginal) ?: ""
    }


    actual fun hashBase64MD5(toHash: ByteArray): String {
        return Base64.encodeToString(
            MessageDigest.getInstance("MD5")
                .digest(toHash),
            Base64.DEFAULT
        )
    }

    private fun convertToISO8601UTC(date: Date?): String? {
        val tz = TimeZone.getTimeZone("UTC")
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.US)
        df.timeZone = tz
        return if (date != null) df.format(date) else null
    }
}