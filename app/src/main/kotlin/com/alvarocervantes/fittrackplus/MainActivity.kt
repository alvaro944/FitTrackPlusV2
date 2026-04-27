package com.alvarocervantes.fittrackplus

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.alvarocervantes.fittrackplus.core.design.AppThemeMode
import com.alvarocervantes.fittrackplus.core.design.FitTrackPlusTheme
import com.alvarocervantes.fittrackplus.core.navigation.AppRoute
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

        val initialTab: AppRoute? = when (intent.getStringExtra("open_tab")) {
            "workout" -> AppRoute.Workout
            "stats" -> AppRoute.Stats
            else -> null
        }

        setContent {
            val themeMode by userPreferencesRepository.themeMode.collectAsStateWithLifecycle(
                initialValue = AppThemeMode.System
            )
            val hasSeenOnboarding by userPreferencesRepository.hasSeenOnboarding
                .collectAsStateWithLifecycle(initialValue = false)

            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { /* granted or not — app continues either way */ }

            LaunchedEffect(hasSeenOnboarding) {
                if (hasSeenOnboarding && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            FitTrackPlusTheme(themeMode = themeMode) {
                FitTrackPlusAppRoot(
                    hasSeenOnboarding = hasSeenOnboarding,
                    onOnboardingComplete = {
                        lifecycleScope.launch {
                            userPreferencesRepository.setHasSeenOnboarding(true)
                        }
                    },
                    initialTab = initialTab
                )
            }
        }
    }
}
