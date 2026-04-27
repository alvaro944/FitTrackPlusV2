package com.alvarocervantes.fittrackplus.data.preferences

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class UserPreferencesRepositoryTest {

    private lateinit var repository: UserPreferencesRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        // Delete the DataStore file before each test to ensure a clean state
        File(context.filesDir, "datastore/fittrackplus_user_preferences.preferences_pb").delete()
        repository = UserPreferencesRepository(context)
    }

    @Test
    fun hasSeenOnboarding_defaultFalse() = runTest {
        val result = repository.hasSeenOnboarding.first()
        assertFalse(result)
    }

    @Test
    fun setHasSeenOnboarding_persistsTrue() = runTest {
        repository.setHasSeenOnboarding(true)
        val result = repository.hasSeenOnboarding.first()
        assertTrue(result)
    }
}
