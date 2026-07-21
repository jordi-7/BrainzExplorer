package com.jordigordillo.brainzexplorer.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jordigordillo.brainzexplorer.ui.detail.ArtistDetailScreen
import com.jordigordillo.brainzexplorer.ui.home.HomeScreen

@Composable
fun BrainzNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Home) {
        composable<Home> {
            HomeScreen(
                onArtistClick = { artistId, artistName ->
                    navController.navigate(ArtistDetailRoute(artistId, artistName))
                },
            )
        }
        composable<ArtistDetailRoute> {
            
        }
    }
}
