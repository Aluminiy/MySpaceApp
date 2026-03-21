package ru.omc.myspaceapp

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
import ru.omc.myspaceapp.di.appModule

class MySpaceApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initAndroidContext(this)
        startKoin {
            androidContext(this@MySpaceApplication)
            modules(appModule)
        }
    }
}