package com.jordigordillo.brainzexplorer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.jordigordillo.brainzexplorer.navigation.BrainzNavHost
import com.jordigordillo.brainzexplorer.ui.theme.BrainzExplorerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BrainzExplorerTheme {
                BrainzNavHost()
            }
        }
    }
}
