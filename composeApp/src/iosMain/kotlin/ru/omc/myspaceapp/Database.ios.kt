package ru.omc.myspaceapp

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import ru.omc.myspaceapp.db.AppDatabase

private val databaseLock = SynchronizedObject()
private var _database: AppDatabase? = null

actual fun createDatabase(): AppDatabase {
    return _database ?: synchronized(databaseLock) {
        _database ?: AppDatabase(
            NativeSqliteDriver(AppDatabase.Schema, "AppDatabase.db")
        ).also { _database = it }
    }
}