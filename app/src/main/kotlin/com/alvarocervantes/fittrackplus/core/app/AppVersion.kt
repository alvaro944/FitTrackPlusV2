package com.alvarocervantes.fittrackplus.core.app

import com.alvarocervantes.fittrackplus.BuildConfig

object AppVersion {
    val displayName: String
        get() = buildString {
            append("v${BuildConfig.VERSION_NAME} · ${BuildConfig.GIT_BRANCH}")
            if (BuildConfig.GIT_SHA.isNotBlank()) {
                append(" · ${BuildConfig.GIT_SHA}")
            }
        }
}
