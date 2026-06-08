package com.alvarocervantes.fittrackplus

import android.app.Application
import android.content.pm.ApplicationInfo
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.alvarocervantes.fittrackplus.core.notification.ActiveSessionObserver
import com.alvarocervantes.fittrackplus.data.local.seed.DebugDemoDataSeeder
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class FitTrackPlusApp : Application() {

    @Inject lateinit var debugDemoDataSeeder: DebugDemoDataSeeder
    @Inject lateinit var activeSessionObserver: ActiveSessionObserver

    override fun onCreate() {
        super.onCreate()
        if ((applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            debugDemoDataSeeder.seedIfEmpty()
        }
        activeSessionObserver.start(ProcessLifecycleOwner.get().lifecycleScope)
    }
}
