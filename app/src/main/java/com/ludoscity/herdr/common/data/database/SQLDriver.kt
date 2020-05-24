package com.ludoscity.herdr.common.data.database

import android.content.Context
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver


actual class DbArgs(
    var context: Context
)

actual fun getSqlDriver(dbArgs: DbArgs): SqlDriver? {
    val driver: SqlDriver = AndroidSqliteDriver(HerdrDatabase.Schema, dbArgs.context, "herdr.db")
    return driver
}