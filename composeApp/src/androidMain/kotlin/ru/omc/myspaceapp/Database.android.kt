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

actual fun createDatabase(): AppDatabase {
    val driver: SqlDriver = AndroidSqliteDriver(
        AppDatabase.Schema,
        androidContext,
        "AppDatabase.db"
    )
    return AppDatabase(driver)
}