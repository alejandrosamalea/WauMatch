package com.example.waumatch

import RecuperarScreen
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.waumatch.auth.LoginScreen
import com.example.waumatch.auth.RegisterScreen
import com.example.waumatch.ui.navigation.MainNavigationBar
import com.example.waumatch.ui.navigation.NavigationItem
import com.example.waumatch.ui.screens.*
import com.example.waumatch.ui.screens.Profiles.ForeignProfileScreen
import com.example.waumatch.ui.screens.Profiles.ProfileScreen
import com.example.waumatch.ui.screens.Profiles.Ubicacion
import com.example.waumatch.ui.screens.mascotas.AdminMascota
import com.example.waumatch.ui.screens.mascotas.AnadirMascota
import com.example.waumatch.ui.screens.mascotas.EditarMascota
import com.example.waumatch.ui.screens.mascotas.MascotaDetailsScreen
import com.example.waumatch.ui.theme.WauMatchTheme
import com.example.waumatch.viewmodel.CloudinaryManager
import com.google.firebase.auth.FirebaseAuth
import com.example.waumatch.viewmodel.ChatViewModel



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
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
                        if (selectedDestination != NavigationItem.Login.route && selectedDestination != NavigationItem.Registrar.route && selectedDestination != NavigationItem.Recuperar.route
                            && selectedDestination != NavigationItem.Add.route && selectedDestination != NavigationItem.ForeignProfile.route && selectedDestination != NavigationItem.AnuncioDetallado.route
                            && selectedDestination != NavigationItem.allReviews.route && selectedDestination != NavigationItem.anadirMascota.route && selectedDestination != NavigationItem.AdminMascota.route
                            && selectedDestination != NavigationItem.Ubicacion.route)
                        {
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
                        composable(NavigationItem.Chat.route) {
                            val chatViewModel: ChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                            ChatScreen(navController, chatViewModel)
                        }
                        composable(
                            route = "chatDetail/{userId}",
                            arguments = listOf(navArgument("userId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val userId = backStackEntry.arguments?.getString("userId") ?: ""
                            val chatViewModel: ChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                            ChatDetailScreen(
                                navController = navController,
                                userId = userId,
                                viewModel = chatViewModel
                            )
                        }
                        composable(NavigationItem.Add.route) { AddScreen(navController) }
                        composable(NavigationItem.Favorites.route) { FavoritesScreen(navController) }
                        composable(NavigationItem.Profile.route) { ProfileScreen(navController) }
                        composable(NavigationItem.Login.route) { LoginScreen(navController) }
                        composable(NavigationItem.Registrar.route) { RegisterScreen(navController) }
                        composable(NavigationItem.Recuperar.route) { RecuperarScreen(navController) }
                        composable(
                            route = NavigationItem.ForeignProfile.route,
                            arguments = listOf(navArgument("userId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val userId = backStackEntry.arguments?.getString("userId")
                            val currentUser = FirebaseAuth.getInstance().currentUser

                            if (currentUser == null) {
                                // Si no hay usuario autenticado, redirigir al login
                                LaunchedEffect(Unit) {
                                    navController.navigate(NavigationItem.Login.route) {
                                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                    }
                                }
                            } else if (userId == null || userId == "{userId}" || userId.isEmpty() || userId == currentUser.uid) {
                                // Redirigir a ProfileScreen si userId es inválido o es el del usuario autenticado
                                LaunchedEffect(Unit) {
                                    navController.navigate(NavigationItem.Profile.route) {
                                        popUpTo(NavigationItem.ForeignProfile.route) { inclusive = true }
                                        // Evitar múltiples instancias de ProfileScreen
                                        launchSingleTop = true
                                    }
                                }
                            } else {
                                // Mostrar ForeignProfileScreen para otro usuario
                                ForeignProfileScreen(
                                    userId = userId,
                                    onBackClick = { navController.popBackStack() },
                                    navController = navController
                                )
                            }
                        }
                        composable(
                            NavigationItem.AnuncioDetallado.route,

                        ) { backStackEntry ->
                            val anuncioId = backStackEntry.arguments?.getString("anuncioId") ?: ""
                            AnuncioDetalladoScreen(navController = navController, anuncioId = anuncioId, onBackClick = { navController.popBackStack() })
                        }

                        composable(
                            route = "allReviews/{userId}",
                            arguments = listOf(navArgument("userId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val userId = backStackEntry.arguments?.getString("userId") ?: ""
                            AllReviewsScreen(
                                userId = userId,
                                navController = navController,
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                        composable(NavigationItem.anadirMascota.route) {
                            AnadirMascota(
                                navController = navController
                            )
                        }
                        composable(NavigationItem.AdminMascota.route) {
                            AdminMascota(
                                navController = navController
                            )
                        }
                        composable(
                            route = "editarmascota/{mascotaId}",
                            arguments = listOf(navArgument("mascotaId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val mascotaId = backStackEntry.arguments?.getString("mascotaId") ?: ""

                            EditarMascota(
                                navController = navController,
                                mascotaId = mascotaId
                            )
                        }


                        composable(NavigationItem.Ubicacion.route) {
                            Ubicacion(
                                navController = navController
                            )
                        }
                        composable(
                            route = "mascotaDetailsScreen/{UIdUsuario}/{mascotaId}",
                            arguments = listOf(
                                navArgument("UIdUsuario") { type = NavType.StringType },
                                navArgument("mascotaId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val userId = backStackEntry.arguments?.getString("UIdUsuario") ?: ""
                            val mascotaId = backStackEntry.arguments?.getString("mascotaId") ?: ""

                            MascotaDetailsScreen(
                                navController = navController,
                                mascotaId = mascotaId,
                                userId = userId
                            )
                        }

                    }
                }
            }
        }
    }
}
