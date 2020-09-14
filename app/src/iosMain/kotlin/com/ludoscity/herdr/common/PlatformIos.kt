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

actual object Platform {
    actual val app_version: String = "iOS:"
    actual val api_level: Long
        get() = TODO("Not yet implemented")
    actual val device_model: String
        get() = TODO("Not yet implemented")
    actual val language: String
        get() = TODO("Not yet implemented")
    actual val country: String
        get() = TODO("Not yet implemented")
    actual val now: Long
        get() = TODO("Not yet implemented")
    actual val nowString: String
        get() = TODO("Not yet implemented")

    actual fun toISO8601UTC(timestampString: String): String {
        TODO("Not yet implemented")
    }

    actual fun hashBase64MD5(toHash: ByteArray): String {
        TODO("Not yet implemented")
    }
}