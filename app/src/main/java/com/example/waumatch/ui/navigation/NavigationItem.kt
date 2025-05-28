package com.example.waumatch.ui.navigation

import androidx.annotation.DrawableRes
import com.example.waumatch.R

sealed class NavigationItem(
    val route: String,
    @DrawableRes val icon: Int,
    val label: String
) {
    object Home : NavigationItem("home", R.drawable.home, "Home")
    object Chat : NavigationItem("chat", R.drawable.chat, "Chat")
    object Add : NavigationItem("add", R.drawable.add, "Añadir")
    object Favorites : NavigationItem("favorites", R.drawable.favorite, "Favoritos")
    object Profile : NavigationItem("profile", R.drawable.profile, "Perfil")
    object Login : NavigationItem("login", R.drawable.profile, "Login")
    object Registrar : NavigationItem("registrar", R.drawable.profile, "Registrar")
    object Recuperar : NavigationItem("recuperar", R.drawable.profile, "Recuperar")
    object ForeignProfile : NavigationItem("foreignProfile/{userId}", R.drawable.profile, "ForeignProfile")
    object AnuncioDetallado : NavigationItem("anuncioDetallado/{anuncioId}", R.drawable.perro, "AnuncioDetallado")
    object allReviews : NavigationItem("allReviews/{userId}", R.drawable.perro, "allReviews")
    object anadirMascota : NavigationItem("anadirMascota", R.drawable.add, "Agregar Mascota")
    object AdminMascota : NavigationItem("adminMascota", R.drawable.profile, "Gestionar Mascotas")
    object Ubicacion : NavigationItem("test", R.drawable.profile, "Mapa")

}
