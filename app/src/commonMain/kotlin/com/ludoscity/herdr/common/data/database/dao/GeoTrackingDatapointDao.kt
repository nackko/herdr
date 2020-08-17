package com.ludoscity.herdr.common.data.database.dao

import com.ludoscity.herdr.common.data.GeoTrackingDatapoint
import com.ludoscity.herdr.common.data.database.HerdrDatabase


class GeoTrackingDatapointDao(database: HerdrDatabase) {

    private val db = database.geoTrackingDatapointQueries

    internal fun insert(item: GeoTrackingDatapoint) {
        db.insertOrReplace(item)
    }

    internal fun updateUploadCompleted(id: Long) {
        db.updateUploadCompleted(id)
    }

    internal fun delete(geoTrackingDatapoint: GeoTrackingDatapoint) {
        db.deleteById(geoTrackingDatapoint.id)
    }

    internal fun deleteUploadedAll() {
        db.deleteUploadedAll()
    }

    //TODO: check latest versions of KamMPKit to use Coroutine flow
    internal fun selectReadyForUploadAll(): List<GeoTrackingDatapoint> = db.selectReadyForUploadAll().executeAsList()

    internal fun select(): List<GeoTrackingDatapoint> = db.selectAll().executeAsList()
}