package com.example.waumatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.lint.Names.Runtime.LaunchedEffect
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.waumatch.auth.LoginScreen
import com.example.waumatch.auth.RegisterScreen
import com.example.waumatch.ui.navigation.MainNavigationBar
import com.example.waumatch.ui.navigation.NavigationItem
import com.example.waumatch.ui.screens.*
import com.example.waumatch.ui.theme.WauMatchTheme
import com.example.waumatch.viewmodel.CloudinaryManager
import com.google.firebase.auth.FirebaseAuth


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CloudinaryManager.init(this)
        setContent {
            WauMatchTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val selectedDestination = navBackStackEntry?.destination?.route ?: NavigationItem.Home
                val auth = FirebaseAuth.getInstance()

                LaunchedEffect(Unit) {
                    if (auth.currentUser != null) {
                        navController.navigate(NavigationItem.Home.route) {
                            popUpTo(NavigationItem.Login.route) { inclusive = true }
                        }
                    }
                }
                Scaffold(
                    bottomBar = {
                        if (selectedDestination != NavigationItem.Login.route && selectedDestination != NavigationItem.Registrar.route) {
                            MainNavigationBar(navController)
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = NavigationItem.Login.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(NavigationItem.Home.route) { HomeScreen(navController) }
                        composable(NavigationItem.Chat.route) { ChatScreen() }
                        composable(NavigationItem.Add.route) { AddScreen(navController) }
                        composable(NavigationItem.Favorites.route) { FavoritesScreen(navController) }
                        composable(NavigationItem.Profile.route) { ProfileScreen() }
                        composable(NavigationItem.Login.route) { LoginScreen(navController) }
                        composable(NavigationItem.Registrar.route) { RegisterScreen(navController)
                        }
                    }
                }
            }
        }
    }
}
