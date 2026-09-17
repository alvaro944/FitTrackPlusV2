package com.alvarocervantes.fittrackplus.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.File
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Each test gets its own DataStore file and its own scope. The production store is a
 * process-wide singleton keyed by file name, so deleting the file underneath it would
 * not clear its in-memory cache and the tests would leak state into each other and
 * depend on whatever the device happened to have stored.
 */
@RunWith(AndroidJUnit4::class)
class UserPreferencesRepositoryTest {

    private lateinit var scope: CoroutineScope
    private lateinit var file: File
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repository: UserPreferencesRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        file = File(context.cacheDir, "test-prefs-${UUID.randomUUID()}.preferences_pb")
        dataStore = PreferenceDataStoreFactory.create(scope = scope, produceFile = { file })
        repository = UserPreferencesRepository(dataStore)
    }

    @After
    fun tearDown() {
        scope.cancel()
        file.delete()
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
