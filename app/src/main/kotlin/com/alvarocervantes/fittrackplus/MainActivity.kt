package com.alvarocervantes.fittrackplus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.alvarocervantes.fittrackplus.core.design.AppThemeMode
import com.alvarocervantes.fittrackplus.core.design.FitTrackPlusTheme
import com.alvarocervantes.fittrackplus.data.preferences.UserPreferencesRepository
import com.alvarocervantes.fittrackplus.feature.launch.FitTrackPlusAppRoot
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by userPreferencesRepository.themeMode.collectAsStateWithLifecycle(
                initialValue = AppThemeMode.System
            )
            val hasSeenOnboarding by userPreferencesRepository.hasSeenOnboarding
                .collectAsStateWithLifecycle(initialValue = false)
            FitTrackPlusTheme(themeMode = themeMode) {
                FitTrackPlusAppRoot(
                    hasSeenOnboarding = hasSeenOnboarding,
                    onOnboardingComplete = {
                        lifecycleScope.launch {
                            userPreferencesRepository.setHasSeenOnboarding(true)
                        }
                    }
                )
            }
        }
    }
}
