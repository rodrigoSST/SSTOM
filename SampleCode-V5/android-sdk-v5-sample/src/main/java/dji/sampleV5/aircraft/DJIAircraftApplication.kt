package dji.sampleV5.aircraft

import android.content.Context
import dji.sampleV5.aircraft.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin

/**
 * Class Description
 *
 * @author Hoker
 * @date 2022/3/2
 *
 * Copyright (c) 2022, DJI All Rights Reserved.
 */
class DJIAircraftApplication : DJIApplication() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        com.secneo.sdk.Helper.install(this)

        startKoin {
            androidLogger()
            androidContext(this@DJIAircraftApplication)
            koin.loadModules(
                listOf(appModule)
            )
        }
    }
}