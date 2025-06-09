package com.example.waumatch.ui.screens

import android.app.Application
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.waumatch.data.local.AnuncioEntity
import com.example.waumatch.ui.components.AnuncioCard
import com.example.waumatch.ui.components.Mascota
import com.example.waumatch.ui.components.SearchBar
import com.example.waumatch.viewmodel.AnuncioViewModel
import com.example.waumatch.viewmodel.AnuncioViewModelFactory
import com.example.waumatch.viewmodel.MascotaViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Colores del tema
val NightBlue = Color(0xFF111826)
val DeepNavy = Color(0xFF022859)
val OceanBlue = Color(0xFF024873)
val SkyBlue = Color(0xFF1D7A93)
val AquaLight = Color(0xFF2EDFF2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: AnuncioViewModel = viewModel(factory = AnuncioViewModelFactory(application))
    val mascotasViewModel: MascotaViewModel = viewModel()

    val anuncios by viewModel.anuncios.collectAsState()
    val mascotas by mascotasViewModel.mascotas.collectAsState()

    // Cargar mascotas cuando cambian los anuncios
    LaunchedEffect(anuncios) {
        //mascotasViewModel.cargarMascotasParaAnuncios(anuncios)
    }

    var searchQuery by remember { mutableStateOf("") }
    var filtrarPorComunidad by remember { mutableStateOf(false) }
    var showFilters by remember { mutableStateOf(false) }
    var selectedTipoMascota by remember { mutableStateOf("Todos") }
    var selectedDistancia by remember { mutableStateOf(300f) }
    var selectedTipoAnuncio by remember { mutableStateOf("Todos") }
    var ignorarDistancia by remember { mutableStateOf(false) }

    val userLocation by produceState<List<Double>?>(initialValue = null) {
        val idUsuario = viewModel.obtenerIdUsuarioActual() ?: ""
        try {
            val db = FirebaseFirestore.getInstance()
            val doc = db.collection("usuarios").document(idUsuario).get().await()
            if (doc.exists()) {
                val lat = doc.getDouble("latitud")
                val lng = doc.getDouble("longitud")
                val rad = doc.getDouble("radio_km") ?: 300.0
                if (lat != null && lng != null) {
                    value = listOf(lat, lng, rad)
                }
            }
        } catch (e: Exception) {
            Log.e("HomeScreen", "Error al obtener ubicación del usuario", e)
            value = null
        }
    }

    // Lista estática de tipos de mascotas
    val tiposDisponibles = listOf("Todos", "Perro", "Gato", "Conejo", "Ave", "Hámster")

    val anunciosFiltrados by produceState(
        initialValue = emptyList<AnuncioEntity>(),
        userLocation,
        searchQuery,
        filtrarPorComunidad,
        anuncios,
        mascotas,
        selectedTipoMascota,
        selectedDistancia,
        selectedTipoAnuncio,
        ignorarDistancia
    ) {
        if (userLocation == null && !ignorarDistancia) {
            value = emptyList()
            return@produceState
        }

        val fechaActual = System.currentTimeMillis()
        val startOfDayMillis = getStartOfDayMillis(fechaActual) // Get start of current day
        val mascotaIdToEspecie = mascotas.associate { it.id to it.especie }
        val idUsuario = viewModel.obtenerIdUsuarioActual() ?: ""

        val lista = anuncios.filter { anuncio ->
            val noEsDelUsuario = anuncio.idCreador != idUsuario
            val coincideBusqueda = searchQuery.isBlank() ||
                    anuncio.titulo.contains(searchQuery, ignoreCase = true) ||
                    anuncio.descripcion.contains(searchQuery, ignoreCase = true)
            val coincideComunidad = !filtrarPorComunidad ||
                    viewModel.perteneceALaComunidadDelUsuario(anuncio.idCreador)
            val dentroDelRadio = ignorarDistancia ||
                    (userLocation != null && viewModel.calculateDistance(
                        lat1 = userLocation!![0],
                        lon1 = userLocation!![1],
                        lat2 = anuncio.latitud,
                        lon2 = anuncio.longitud
                    ) <= selectedDistancia)
            val coincideTipoAnuncio = selectedTipoAnuncio == "Todos" ||
                    anuncio.tipos == selectedTipoAnuncio
            val coincideTipoMascota = selectedTipoMascota == "Todos" ||
                    anuncio.mascotasIds.any { mascotaId ->
                        mascotaIdToEspecie[mascotaId] == selectedTipoMascota
                    }

            val fechaFinMillis = parseFechaAInMillis(anuncio.fechaFin)
            val anuncioActivo = fechaFinMillis >= startOfDayMillis // Include announcements ending today

            noEsDelUsuario && coincideBusqueda && coincideComunidad &&
                    dentroDelRadio && coincideTipoAnuncio && coincideTipoMascota &&
                    anuncioActivo
        }

        value = lista
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    SearchBar(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        onFilterClick = { showFilters = !showFilters },
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .padding(end = 8.dp)
                    )
                },
                actions = {
                    IconButton(
                        onClick = { showFilters = !showFilters },
                        modifier = Modifier
                            .background(
                                color = if (showFilters) AquaLight else Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filtros",
                            tint = if (showFilters) NightBlue else Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OceanBlue,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color.Transparent,
        modifier = Modifier.background(
            brush = Brush.verticalGradient(colors = listOf(OceanBlue, SkyBlue))
        )
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            if (showFilters) {
                FiltersPanel(
                    selectedTipoMascota = selectedTipoMascota,
                    onTipoMascotaChange = { selectedTipoMascota = it },
                    tiposDisponibles = tiposDisponibles,
                    selectedDistancia = selectedDistancia,
                    onDistanciaChange = { selectedDistancia = it },
                    selectedTipoAnuncio = selectedTipoAnuncio,
                    onTipoAnuncioChange = { selectedTipoAnuncio = it },
                    filtrarPorComunidad = filtrarPorComunidad,
                    onFiltrarComunidadChange = { filtrarPorComunidad = it },
                    ignorarDistancia = ignorarDistancia,
                    onIgnorarDistanciaChange = { ignorarDistancia = it },
                    onClose = { showFilters = false }
                )
            }

            if (selectedTipoMascota != "Todos" || selectedDistancia != 300f ||
                selectedTipoAnuncio != "Todos" || filtrarPorComunidad || ignorarDistancia
            ) {
                FiltersIndicator(
                    selectedTipoMascota = selectedTipoMascota,
                    selectedDistancia = selectedDistancia,
                    selectedTipoAnuncio = selectedTipoAnuncio,
                    filtrarPorComunidad = filtrarPorComunidad,
                    ignorarDistancia = ignorarDistancia,
                    onClearFilters = {
                        selectedTipoMascota = "Todos"
                        selectedDistancia = 300f
                        selectedTipoAnuncio = "Todos"
                        filtrarPorComunidad = false
                        ignorarDistancia = false
                    }
                )
            }

            if (anunciosFiltrados.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (anuncios.isEmpty()) "No hay anuncios disponibles."
                        else "No se encontraron anuncios que coincidan con los filtros.",
                        style = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp, top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(anunciosFiltrados) { anuncio ->
                        AnuncioCard(
                            anuncio = anuncio,
                            onClick = { navController.navigate("anuncioDetallado/${anuncio.id}") },
                            onToggleFavorito = { viewModel.toggleFavorito(it) }
                        )
                    }
                }
            }
        }
    }
}

val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

fun parseFechaAInMillis(fechaStr: String): Long {
    return try {
        val date = sdf.parse(fechaStr)
        date?.time ?: 0L
    } catch (e: Exception) {
        0L
    }
}

fun getStartOfDayMillis(currentTimeMillis: Long): Long {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = currentTimeMillis
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

@Composable
fun FiltersPanel(
    selectedTipoMascota: String,
    onTipoMascotaChange: (String) -> Unit,
    tiposDisponibles: List<String>,
    selectedDistancia: Float,
    onDistanciaChange: (Float) -> Unit,
    selectedTipoAnuncio: String,
    onTipoAnuncioChange: (String) -> Unit,
    filtrarPorComunidad: Boolean,
    onFiltrarComunidadChange: (Boolean) -> Unit,
    ignorarDistancia: Boolean,
    onIgnorarDistanciaChange: (Boolean) -> Unit,
    onClose: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        color = Color.White.copy(alpha = 0.95f),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filtros",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = NightBlue
                    )
                )
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = NightBlue
                    )
                }
            }
            Text(
                text = "Distancia máxima: ${selectedDistancia.toInt()} km",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = DeepNavy,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Slider(
                value = selectedDistancia,
                onValueChange = onDistanciaChange,
                valueRange = 1f..300f,
                steps = 19,
                colors = SliderDefaults.colors(
                    thumbColor = AquaLight,
                    activeTrackColor = SkyBlue,
                    inactiveTrackColor = SkyBlue.copy(alpha = 0.3f)
                ),
                modifier = Modifier.padding(bottom = 12.dp),
                enabled = !ignorarDistancia
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Switch(
                    checked = ignorarDistancia,
                    onCheckedChange = onIgnorarDistanciaChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AquaLight,
                        checkedTrackColor = SkyBlue,
                        uncheckedThumbColor = DeepNavy,
                        uncheckedTrackColor = SkyBlue.copy(alpha = 0.3f)
                    )
                )
                Text(
                    text = "Mostrar todos los anuncios",
                    style = MaterialTheme.typography.bodyMedium.copy(color = DeepNavy),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Tipo de Anuncio",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = DeepNavy,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                listOf("Todos", "Dueño", "Cuidador").forEach { tipo ->
                    FilterChip(
                        text = tipo,
                        isSelected = selectedTipoAnuncio == tipo,
                        onClick = { onTipoAnuncioChange(tipo) }
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = filtrarPorComunidad,
                    onCheckedChange = onFiltrarComunidadChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = SkyBlue,
                        uncheckedColor = DeepNavy
                    )
                )
                Text(
                    text = "Solo de mi comunidad",
                    style = MaterialTheme.typography.bodyMedium.copy(color = DeepNavy),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
fun FilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        color = if (isSelected) AquaLight else Color.White,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, if (isSelected) AquaLight else SkyBlue)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                color = if (isSelected) NightBlue else SkyBlue,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
            ),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun FiltersIndicator(
    selectedTipoMascota: String,
    selectedDistancia: Float,
    selectedTipoAnuncio: String,
    filtrarPorComunidad: Boolean,
    ignorarDistancia: Boolean,
    onClearFilters: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = AquaLight.copy(alpha = 0.3f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                val activeFilters = mutableListOf<String>()
                if (selectedTipoMascota != "Todos") activeFilters.add(selectedTipoMascota)
                if (!ignorarDistancia && selectedDistancia != 50f) activeFilters.add("${selectedDistancia.toInt()}km")
                if (ignorarDistancia) activeFilters.add("Todas las distancias")
                if (selectedTipoAnuncio != "Todos") activeFilters.add(selectedTipoAnuncio)
                if (filtrarPorComunidad) activeFilters.add("Comunidad")

                Text(
                    text = "Filtros: ${activeFilters.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                    maxLines = 1
                )
            }

            TextButton(
                onClick = onClearFilters,
                colors = ButtonDefaults.textButtonColors(contentColor = AquaLight)
            ) {
                Text("Limpiar", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}