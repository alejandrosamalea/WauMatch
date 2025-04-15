package com.example.waumatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.waumatch.ui.navigation.MainNavigationBar
import com.example.waumatch.ui.navigation.NavigationItem
import com.example.waumatch.ui.screens.*
import com.example.waumatch.ui.theme.WauMatchTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WauMatchTheme {
                val navController = rememberNavController()

                Scaffold(
                    bottomBar = {
                        MainNavigationBar(navController)
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = NavigationItem.Home.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(NavigationItem.Home.route) { HomeScreen() }
                        composable(NavigationItem.Chat.route) { ChatScreen() }
                        composable(NavigationItem.Add.route) { AddScreen() }
                        composable(NavigationItem.Favorites.route) { FavoritesScreen() }
                        composable(NavigationItem.Profile.route) { ProfileScreen() }
                    }
                }
            }
        }
    }
}
