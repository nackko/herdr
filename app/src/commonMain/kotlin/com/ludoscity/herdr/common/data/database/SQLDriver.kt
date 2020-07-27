package com.ludoscity.herdr.common.data.database

import com.squareup.sqldelight.db.SqlDriver

expect class DbArgs

expect fun getSqlDriver(dbArgs: DbArgs): SqlDriver?

object DatabaseCreator {
    fun getDataBase(dbArgs: DbArgs): HerdrDatabase? {
        val sqlDriver = getSqlDriver(dbArgs)
        return if (sqlDriver != null) {
            HerdrDatabase(sqlDriver)
        } else {
            null
        }
    }
}