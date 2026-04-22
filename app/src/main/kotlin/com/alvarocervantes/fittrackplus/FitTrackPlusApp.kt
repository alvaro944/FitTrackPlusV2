package com.alvarocervantes.fittrackplus

import android.app.Application
import android.content.pm.ApplicationInfo
import com.alvarocervantes.fittrackplus.data.local.seed.DebugDemoDataSeeder
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class FitTrackPlusApp : Application() {

    @Inject lateinit var debugDemoDataSeeder: DebugDemoDataSeeder

    override fun onCreate() {
        super.onCreate()
        if ((applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            debugDemoDataSeeder.seedIfEmpty()
        }
    }
}
