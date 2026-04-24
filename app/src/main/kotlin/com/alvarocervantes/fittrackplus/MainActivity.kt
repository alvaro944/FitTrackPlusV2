package com.alvarocervantes.fittrackplus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.alvarocervantes.fittrackplus.core.design.FitTrackPlusTheme
import com.alvarocervantes.fittrackplus.feature.launch.FitTrackPlusAppRoot
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitTrackPlusTheme {
                FitTrackPlusAppRoot()
            }
        }
    }
}
