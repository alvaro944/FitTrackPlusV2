package com.alvarocervantes.fittrackplus.core.app

import com.alvarocervantes.fittrackplus.BuildConfig
import org.junit.Assert.assertEquals
import org.junit.Test

class AppVersionTest {
    @Test
    fun displayNameIncludesVersionBranchAndCommit() {
        val expected = buildString {
            append("v${BuildConfig.VERSION_NAME} · ${BuildConfig.GIT_BRANCH}")
            if (BuildConfig.GIT_SHA.isNotBlank()) {
                append(" · ${BuildConfig.GIT_SHA}")
            }
        }
        assertEquals(expected, AppVersion.displayName)
    }
}
