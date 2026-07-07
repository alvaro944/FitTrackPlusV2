package com.alvarocervantes.fittrackplus.core.app

import com.alvarocervantes.fittrackplus.BuildConfig
import org.junit.Assert.assertEquals
import org.junit.Test

class AppVersionTest {
    @Test
    fun displayNameIncludesVersionAndChannel() {
        assertEquals("v${BuildConfig.VERSION_NAME} · ${BuildConfig.APP_CHANNEL}", AppVersion.displayName)
    }
}
