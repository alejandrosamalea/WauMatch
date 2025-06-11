package com.example.waumatch

import RecuperarScreen
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.example.waumatch.viewmodel.AnuncioViewModel
import com.example.waumatch.viewmodel.CloudinaryManager
import com.google.firebase.auth.FirebaseAuth
import com.example.waumatch.viewmodel.ChatViewModel
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val ONESIGNAL_APP_ID = "038e24e7-eca7-426a-868c-f513079bb67c"
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        OneSignal.Debug.logLevel = LogLevel.VERBOSE
        OneSignal.initWithContext(this, "038e24e7-eca7-426a-868c-f513079bb67c")
        CoroutineScope(Dispatchers.IO).launch {
            OneSignal.Notifications.requestPermission(true)
        }


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
                        fun setUserId(userId: String) {
                            CoroutineScope(Dispatchers.Main).launch {
                                OneSignal.login(userId)
                            }
                        }
                        val auth = FirebaseAuth.getInstance()

                        auth.currentUser?.let { setUserId(it.uid) }

                        navController.navigate(NavigationItem.Home.route) {
                            popUpTo(NavigationItem.Login.route) { inclusive = true }
                        }
                    }
                }

                RequestNotificationPermission();

                Scaffold(
                    bottomBar = {
                        if (selectedDestination != NavigationItem.Login.route &&
                            selectedDestination != NavigationItem.Registrar.route &&
                            selectedDestination != NavigationItem.Recuperar.route &&
                            selectedDestination != NavigationItem.Add.route &&
                            selectedDestination != NavigationItem.ForeignProfile.route &&
                            selectedDestination != NavigationItem.AnuncioDetallado.route &&
                            selectedDestination != NavigationItem.allReviews.route &&
                            selectedDestination != NavigationItem.anadirMascota.route &&
                            selectedDestination != NavigationItem.AdminMascota.route &&
                            selectedDestination != NavigationItem.Ubicacion.route   &&
                            selectedDestination != NavigationItem.ChatDetallado.route &&
                            selectedDestination != NavigationItem.MisAnuncios.route
                            )
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
                            val chatViewModel: ChatViewModel = viewModel()
                            ChatScreen(navController, chatViewModel)
                        }
                        composable(
                            NavigationItem.ChatDetallado.route,
                            arguments = listOf(navArgument("userId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val userId = backStackEntry.arguments?.getString("userId") ?: ""
                            val chatViewModel: ChatViewModel = viewModel()
                            ChatDetailScreen(
                                navController = navController,
                                userId = userId,
                                viewModel = chatViewModel
                            )
                        }
                        composable(NavigationItem.Add.route) { AddScreen(navController) }
                        composable(NavigationItem.Favorites.route) { FavoritesScreen(navController) }
                        composable(NavigationItem.Profile.route) { ProfileScreen(navController, anuncioViewModel = AnuncioViewModel(application)) }
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
                                LaunchedEffect(Unit) {
                                    navController.navigate(NavigationItem.Login.route) {
                                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                    }
                                }
                            } else if (userId == null || userId == "{userId}" || userId.isEmpty() || userId == currentUser.uid) {
                                LaunchedEffect(Unit) {
                                    navController.navigate(NavigationItem.Profile.route) {
                                        popUpTo(NavigationItem.ForeignProfile.route) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            } else {
                                ForeignProfileScreen(
                                    userId = userId,
                                    onBackClick = { navController.popBackStack() },
                                    navController = navController
                                )
                            }
                        }
                        composable(
                            route = NavigationItem.AnuncioDetallado.route,
                            arguments = listOf(navArgument("anuncioId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val anuncioId = backStackEntry.arguments?.getString("anuncioId") ?: ""
                            AnuncioDetalladoScreen(
                                navController = navController,
                                anuncioId = anuncioId,
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                        composable(
                            route = NavigationItem.allReviews.route,
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
                            AnadirMascota(navController = navController)
                        }
                        composable(NavigationItem.AdminMascota.route) {
                            AdminMascota(navController = navController)
                        }
                        composable(
                            route = NavigationItem.EditarMascota.route,
                            arguments = listOf(navArgument("mascotaId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val mascotaId = backStackEntry.arguments?.getString("mascotaId") ?: ""
                            EditarMascota(
                                navController = navController,
                                mascotaId = mascotaId
                            )
                        }
                        composable(NavigationItem.Ubicacion.route) {
                            Ubicacion(navController = navController)
                        }
                        composable(
                            route = NavigationItem.MascotaDetails.route,
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
                        composable(NavigationItem.MisAnuncios.route) {
                            MisAnunciosScreen(navController = navController)
                        }
                        composable(
                            route = NavigationItem.ForeignAds.route,
                            arguments = listOf(navArgument("userId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val userId = backStackEntry.arguments?.getString("userId") ?: ""
                            ForeignAdsScreen(
                                userId = userId,
                                navController = navController
                            )
                        }
                    }
                }
            }
        }
    }
}