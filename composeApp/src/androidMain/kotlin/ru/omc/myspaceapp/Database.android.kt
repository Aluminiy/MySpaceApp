package ru.omc.myspaceapp

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import ru.omc.myspaceapp.db.AppDatabase

lateinit var androidContext: Context
    private set

fun initAndroidContext(context: Context) {
    androidContext = context
}

private val databaseLock = Any()
private var _database: AppDatabase? = null

actual fun createDatabase(): AppDatabase {
    return _database ?: synchronized(databaseLock) {
        _database ?: AppDatabase(
            AndroidSqliteDriver(
                AppDatabase.Schema,
                androidContext,
                "AppDatabase.db"
            )
        ).also { _database = it }
    }
}