package com.example.waumatch.ui.screens.Profiles

import android.annotation.SuppressLint
import android.location.Geocoder
import android.preference.PreferenceManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.util.*

@SuppressLint("ClickableViewAccessibility")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Ubicacion(navController: NavController) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var userGeoPoint by remember { mutableStateOf<GeoPoint?>(null) }
    var selectedLocation by remember { mutableStateOf("") }
    var touchDownX by remember { mutableStateOf(0f) }
    var touchDownY by remember { mutableStateOf(0f) }

    LaunchedEffect(userId) {
        if (userId != null) {
            val userDoc = firestore.collection("usuarios").document(userId).get().await()
            val lat = userDoc.getDouble("latitud")
            val lon = userDoc.getDouble("longitud")
            if (lat != null && lon != null) {
                val geo = GeoPoint(lat, lon)
                userGeoPoint = geo
                selectedLocation = userDoc.getString("location") ?: "Ubicación desconocida"

                mapView?.let { map ->
                    val marker = Marker(map).apply {
                        position = geo
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                    map.overlays.clear()
                    map.overlays.add(marker)
                    map.controller.setCenter(geo)
                    map.invalidate()
                }
            }
        }
    }

    fun guardarDatos() {
        if (userId == null || userGeoPoint == null) return
        val data = hashMapOf(
            "latitud" to userGeoPoint!!.latitude,
            "longitud" to userGeoPoint!!.longitude,
            "location" to selectedLocation
        )
        firestore.collection("usuarios")
            .document(userId)
            .set(data, SetOptions.merge())
        navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seleccionar ubicación") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    if (userId == null) {
                        MapView(ctx)
                    } else {
                        Configuration.getInstance().load(
                            ctx,
                            PreferenceManager.getDefaultSharedPreferences(ctx)
                        )
                        val map = MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            controller.setZoom(13.0)
                            if (userGeoPoint == null) {
                                controller.setCenter(GeoPoint(40.416775, -3.703790))
                            } else {
                                controller.setCenter(userGeoPoint)
                            }
                        }
                        map.setOnTouchListener { _, event ->
                            when (event.action) {
                                android.view.MotionEvent.ACTION_DOWN -> {
                                    touchDownX = event.x
                                    touchDownY = event.y
                                    false
                                }

                                android.view.MotionEvent.ACTION_UP -> {
                                    val deltaX = Math.abs(event.x - touchDownX)
                                    val deltaY = Math.abs(event.y - touchDownY)
                                    if (deltaX < 10 && deltaY < 10) {
                                        val projection = map.projection
                                        val geoPoint = projection.fromPixels(
                                            event.x.toInt(),
                                            event.y.toInt()
                                        ) as GeoPoint
                                        userGeoPoint = geoPoint
                                        map.overlays.clear()
                                        val marker = Marker(map).apply {
                                            position = geoPoint
                                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                        }
                                        map.overlays.add(marker)
                                        map.controller.setCenter(geoPoint)
                                        val geocoder = Geocoder(ctx, Locale.getDefault())
                                        val addresses = geocoder.getFromLocation(
                                            geoPoint.latitude,
                                            geoPoint.longitude,
                                            1
                                        )
                                        selectedLocation = if (!addresses.isNullOrEmpty()) {
                                            addresses[0].getAddressLine(0)
                                                ?: "Ubicación desconocida"
                                        } else {
                                            "Ubicación desconocida"
                                        }
                                        map.invalidate()
                                    }
                                    true
                                }

                                else -> false
                            }
                        }
                        mapView = map
                        map
                    }
                }
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.8f))
                    .padding(16.dp)
            ) {
                Text(text = "Ubicación: $selectedLocation", color = Color.Black)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { guardarDatos() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = userGeoPoint != null
                ) {
                    Text(text = "Guardar ubicación", color = Color.Black)
                }
            }
        }
    }
}