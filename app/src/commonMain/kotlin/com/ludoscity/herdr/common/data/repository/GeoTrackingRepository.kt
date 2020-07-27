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

package com.ludoscity.herdr.common.data.repository

import com.ludoscity.herdr.common.base.Response
import com.ludoscity.herdr.common.data.GeoTrackingDatapoint
import com.ludoscity.herdr.common.data.database.HerdrDatabase
import com.ludoscity.herdr.common.data.database.dao.GeoTrackingDatapointDao
import org.koin.core.KoinComponent
import org.koin.core.inject

class GeoTrackingRepository : KoinComponent {

    private val herdrDb: HerdrDatabase by inject()

    suspend fun insertGeoTrackingDatapoint(record: GeoTrackingDatapoint): Response<List<GeoTrackingDatapoint>> {
        val geoTrackingDao = GeoTrackingDatapointDao(herdrDb)
        geoTrackingDao.insert(record)
        return Response.Success(geoTrackingDao.select())
    }

//    @Update
//    fun update(record: GeoTrackingDatapoint)
//
//    @Query("DELETE FROM geotrackingdatapoint")
//    fun deleteAll()
//
//    @Query("DELETE FROM geotrackingdatapoint WHERE upload_completed='1'")
//    fun deleteUploadedAll()
//
//    @Query("SELECT * from geotrackingdatapoint ORDER BY id ASC")
//    fun getAllList(): LiveData<List<GeoTrackingDatapoint>>
//
//    @Query("SELECT * from geotrackingdatapoint WHERE upload_completed='0'")
//    fun getNonUploadedList(): List<GeoTrackingDatapoint>

//    private lateinit var herdrDatabase: HerdrDatabase
//
//    fun setDatabase(databaseToSet: HerdrDatabase)
}