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
}
