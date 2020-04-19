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

package com.ludoscity.herdr.common.data

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

actual class SecureDataStore actual constructor() {
    private lateinit var encryptedPreferences: SharedPreferences
    private val sharedPrefFilename = "herdr_secure_prefs"

    @SuppressLint("ApplySharedPref")
    actual suspend fun storeString(key: String, data: String) {
        encryptedPreferences.edit()
            .putString(key, data)
            .commit()
    }

    actual suspend fun retrieveString(key: String): String? {
        return encryptedPreferences.getString(key, null)
    }

    @SuppressLint("ApplySharedPref")
    actual suspend fun deleteKey(key: String) {
        encryptedPreferences.edit()
            .remove(key)
            .commit()
    }

    constructor(ctx: Context) : this() {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        encryptedPreferences = EncryptedSharedPreferences
            .create(
                sharedPrefFilename,
                masterKeyAlias,
                ctx,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
    }
}