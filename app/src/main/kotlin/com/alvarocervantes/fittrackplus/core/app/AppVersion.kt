package com.alvarocervantes.fittrackplus.core.app

import com.alvarocervantes.fittrackplus.BuildConfig

object AppVersion {
    val displayName: String
        get() = "v${BuildConfig.VERSION_NAME} · ${BuildConfig.APP_CHANNEL}"
}
