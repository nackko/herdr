package com.ludoscity.herdr.common.data.database

import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.drivers.ios.NativeSqliteDriver

actual class DbArgs(
)

actual fun getSqlDriver(dbArgs: DbArgs): SqlDriver? {
    val driver: SqlDriver = NativeSqliteDriver(HerdrDatabase.Schema, "herdr.db")
    return driver
}