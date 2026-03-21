package ru.omc.myspaceapp

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import ru.omc.myspaceapp.db.AppDatabase

actual fun createDatabase(): AppDatabase {
    val driver: SqlDriver = NativeSqliteDriver(
        AppDatabase.Schema,
        "AppDatabase.db"
    )
    return AppDatabase(driver)
}