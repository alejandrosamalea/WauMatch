package com.example.waumatch.ui.screens.Profiles

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FieldPath
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.waumatch.ui.theme.OceanBlue
import com.example.waumatch.ui.theme.SkyBlue
import com.example.waumatch.viewmodel.ProfileManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ForeignProfileScreen(userId: String, onBackClick: () -> Unit, navController: NavController) {
    val context = LocalContext.current
    var nombre by remember { mutableStateOf("Sin nombre") }
    var fechaRegistro by remember { mutableStateOf("01/2025") }
    var subtitle by remember { mutableStateOf("") }
    var about by remember { mutableStateOf("Añade una descripción") }
    var availability by remember {
        mutableStateOf(
            mapOf(
                "Lunes" to mapOf("start" to "00:00", "end" to "24:00"),
                "Martes" to mapOf("start" to "00:00", "end" to "24:00"),
                "Miércoles" to mapOf("start" to "00:00", "end" to "24:00"),
                "Jueves" to mapOf("start" to "00:00", "end" to "24:00"),
                "Viernes" to mapOf("start" to "00:00", "end" to "24:00"),
                "Sábado" to mapOf("start" to "00:00", "end" to "24:00"),
                "Domingo" to mapOf("start" to "00:00", "end" to "24:00")
            )
        )
    }
    var profileImage by remember { mutableStateOf("https://via.placeholder.com/150") }
    var tags by remember { mutableStateOf(listOf("♥️ Amante de los animales")) }
    var rating by remember { mutableStateOf(0) }
    var reviewText by remember { mutableStateOf("") }
    var reviews by remember { mutableStateOf(listOf<ReviewData>()) }
    var userReview by remember { mutableStateOf<ReviewData?>(null) }
    var isEditingReview by remember { mutableStateOf(false) }

    val averageRating = if (reviews.isNotEmpty()) {
        String.format("%.1f", reviews.map { it.rating }.average())
    } else {
        "0.0"
    }
    val reviewCount = reviews.size

    val db = FirebaseFirestore.getInstance()
    val authUser = FirebaseAuth.getInstance().currentUser

    val profileManager: ProfileManager = viewModel(factory = ProfileManagerFactory(context))

    LaunchedEffect(userId) {
        val usuario = db.collection("usuarios").document(userId)
        usuario.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    nombre = documentSnapshot.getString("nombre") ?: "Sin nombre"
                    fechaRegistro = documentSnapshot.getString("fechaRegistro") ?: "01/2025"
                    subtitle = documentSnapshot.getString("subtitle") ?: ""
                    about = documentSnapshot.getString("about") ?: "Añade una descripción"
                    tags = documentSnapshot.get("tags") as? List<String> ?: tags
                    profileImage = documentSnapshot.getString("profileImage") ?: "https://via.placeholder.com/150"

                    val firebaseAvailability = documentSnapshot.get("availability")
                    if (firebaseAvailability != null) {
                        when (firebaseAvailability) {
                            is Map<*, *> -> {
                                try {
                                    val availabilityMap = firebaseAvailability as Map<String, Map<String, String>>
                                    availability = availability.toMutableMap().apply {
                                        availabilityMap.forEach { (day, times) ->
                                            if (this.containsKey(day)) {
                                                this[day] = times
                                            }
                                        }
                                    }
                                } catch (e: ClassCastException) {
                                    val oldAvailability = firebaseAvailability as Map<String, String>
                                    availability = availability.toMutableMap().apply {
                                        oldAvailability.forEach { (day, timeRange) ->
                                            if (this.containsKey(day)) {
                                                val times = timeRange.split(" - ").let {
                                                    if (it.size == 2) mapOf("start" to it[0], "end" to it[1])
                                                    else mapOf("start" to "00:00", "end" to "24:00")
                                                }
                                                this[day] = times
                                            }
                                        }
                                    }
                                }
                            }
                            else -> {
                                println("Formato de availability no soportado: $firebaseAvailability")
                            }
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                println("Error al cargar datos: ${exception.message}")
            }
    }

    LaunchedEffect(userId) {
        loadTopReviews(userId, { fetchedReviews ->
            reviews = fetchedReviews
        })
    }


    LaunchedEffect(userId, authUser?.uid) {
        if (authUser != null) {
            db.collection("reseñas")
                .whereEqualTo("idEmisor", authUser.uid)
                .whereEqualTo("idReceptor", userId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val reviewDoc = querySnapshot.documents.first()
                        userReview = ReviewData(
                            rating = reviewDoc.getLong("rating")?.toInt() ?: 0,
                            comment = reviewDoc.getString("comment") ?: "",
                            idEmisor = reviewDoc.getString("idEmisor") ?: "",
                            fechaCreacion = reviewDoc.getString("fechaCreacion") ?: "",
                            idReceptor = reviewDoc.getString("idReceptor") ?: ""
                        )
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error al verificar reseña del usuario: ${e.message}")
                }
        }
    }

    LaunchedEffect(isEditingReview, userReview) {
        if (isEditingReview && userReview != null) {
            rating = userReview!!.rating
            reviewText = userReview!!.comment
        }
    }

    fun handleSubmitReview(bd: FirebaseFirestore, userId: String, context: Context) {
        val emisorId = authUser?.uid ?: return

        if (rating == 0) {
            Toast.makeText(context, "Por favor selecciona una calificación", Toast.LENGTH_SHORT).show()
            return
        }

        if (reviewText.trim().isEmpty()) {
            Toast.makeText(context, "Por favor escribe un comentario", Toast.LENGTH_SHORT).show()
            return
        }

        bd.collection("reseñas")
            .whereEqualTo("idEmisor", emisorId)
            .whereEqualTo("idReceptor", userId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    Toast.makeText(context, "Ya has dejado una reseña para este usuario", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                bd.collection("usuarios").document(emisorId).get()
                    .addOnSuccessListener { document ->
                        val nombre = document.getString("nombre") ?: "Anónimo"
                        val fotoPerfil = document.getString("profileImage") ?: ""

                        val newReview = ReviewData(
                            rating = rating,
                            comment = reviewText,
                            idEmisor = emisorId,
                            fechaCreacion = SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(Date()),
                            idReceptor = userId
                        )

                        bd.collection("reseñas").add(newReview)
                            .addOnSuccessListener {
                                Toast.makeText(context, "¡Reseña enviada con éxito!", Toast.LENGTH_SHORT).show()
                                userReview = newReview
                                reviews = listOf(newReview) + reviews
                                rating = 0
                                reviewText = ""
                            }
                            .addOnFailureListener { e ->
                                Log.e("Firestore", "Error al guardar la reseña: ${e.message}")
                                Toast.makeText(context, "Error al enviar la reseña", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "No se pudo obtener el usuario", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al verificar existencia de reseña: ${e.message}")
                Toast.makeText(context, "Error al verificar reseña existente", Toast.LENGTH_SHORT).show()
            }
    }

    fun handleUpdateReview(bd: FirebaseFirestore, userId: String, context: Context) {
        val emisorId = authUser?.uid ?: return

        if (rating == 0) {
            Toast.makeText(context, "Por favor selecciona una calificación", Toast.LENGTH_SHORT).show()
            return
        }

        if (reviewText.trim().isEmpty()) {
            Toast.makeText(context, "Por favor escribe un comentario", Toast.LENGTH_SHORT).show()
            return
        }

        bd.collection("reseñas")
            .whereEqualTo("idEmisor", emisorId)
            .whereEqualTo("idReceptor", userId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    Toast.makeText(context, "No se encontró la reseña", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val reviewDoc = querySnapshot.documents.first()
                val updatedReview = ReviewData(
                    rating = rating,
                    comment = reviewText,
                    idEmisor = emisorId,
                    fechaCreacion = userReview?.fechaCreacion ?: SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(Date()),
                    idReceptor = userId
                )

                reviewDoc.reference.set(updatedReview)
                    .addOnSuccessListener {
                        Toast.makeText(context, "¡Reseña actualizada con éxito!", Toast.LENGTH_SHORT).show()
                        userReview = updatedReview
                        reviews = reviews.map { if (it.idEmisor == emisorId) updatedReview else it }
                        isEditingReview = false
                        rating = 0
                        reviewText = ""
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error al actualizar la reseña: ${e.message}")
                        Toast.makeText(context, "Error al actualizar la reseña", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al buscar la reseña: ${e.message}")
                Toast.makeText(context, "Error al buscar la reseña", Toast.LENGTH_SHORT).show()
            }
    }

    fun handleDeleteReview(bd: FirebaseFirestore, userId: String, context: Context) {
        val emisorId = authUser?.uid ?: return

        bd.collection("reseñas")
            .whereEqualTo("idEmisor", emisorId)
            .whereEqualTo("idReceptor", userId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    Toast.makeText(context, "No se encontró la reseña", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val reviewDoc = querySnapshot.documents.first()
                reviewDoc.reference.delete()
                    .addOnSuccessListener {
                        Toast.makeText(context, "¡Reseña eliminada con éxito!", Toast.LENGTH_SHORT).show()
                        userReview = null
                        reviews = reviews.filter { it.idEmisor != emisorId }
                        isEditingReview = false
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error al eliminar la reseña: ${e.message}")
                        Toast.makeText(context, "Error al eliminar la reseña", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al buscar la reseña: ${e.message}")
                Toast.makeText(context, "Error al buscar la reseña", Toast.LENGTH_SHORT).show()
            }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(OceanBlue, SkyBlue)
                )
            )
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 30.dp)
                    .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 16.dp, end = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = ComposeColor.White
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(profileImage),
                        contentDescription = "Imagen de perfil",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(3.dp, ComposeColor(0xFF2EDFF2), CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(15.dp))
                    Text(
                        text = nombre,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = ComposeColor.White
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = subtitle,
                        fontSize = 16.sp,
                        color = ComposeColor(0xFF1EB7D9)
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                            .background(
                                color = ComposeColor(0x1A1EB7D9),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = averageRating,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ComposeColor(0xFF2EDFF2)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Average Rating Star",
                                        tint = ComposeColor(0xFFFFD700),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Text(
                                    text = if (reviewCount == 1) "$reviewCount reseña" else "$reviewCount reseñas",
                                    fontSize = 14.sp,
                                    color = ComposeColor.White,
                                    modifier = Modifier.padding(top = 5.dp)
                                )
                            }
                            StatItem(number = "127", label = "Cuidados")
                            val (mesReg, anioReg) = fechaRegistro.split("/").map { it.toInt() }
                            val totalMeses = (Calendar.getInstance().get(Calendar.YEAR) - anioReg) * 12 + (Calendar.getInstance().get(Calendar.MONTH) + 1 - mesReg)
                            StatItem(
                                number = if (totalMeses >= 12) (totalMeses / 12).toString() else totalMeses.toString(),
                                label = if (totalMeses >= 12) if (totalMeses / 12 == 1) "Año" else "Años" else if (totalMeses == 1) "Mes" else "Meses"
                            )
                        }
                    }
                }
            }
        }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Sobre Mí",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ComposeColor.White,
                    modifier = Modifier.padding(bottom = 15.dp)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ComposeColor(0x1A1EB7D9), RoundedCornerShape(12.dp))
                        .padding(20.dp)
                ) {
                    Text(
                        text = about,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = ComposeColor.White,
                        modifier = Modifier.padding(bottom = 15.dp)
                    )
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(tags) { tag ->
                            Box(
                                modifier = Modifier
                                    .background(ComposeColor(0x262EDFF2), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = tag,
                                    fontSize = 14.sp,
                                    color = ComposeColor(0xFF2EDFF2)
                                )
                            }
                        }
                    }
                }
            }
        }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Disponibilidad",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ComposeColor.White,
                    modifier = Modifier.padding(bottom = 15.dp)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ComposeColor(0x1A1EB7D9), RoundedCornerShape(12.dp))
                        .padding(15.dp)
                ) {
                    availability.forEach { (day, times) ->
                        AvailabilityItem(
                            day = day,
                            startTime = times["start"] ?: "00:00",
                            endTime = times["end"] ?: "24:00",
                            isEditing = false,
                            onTimeChange = { _, _ -> }
                        )
                    }
                }
            }
        }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = if (userReview != null && !isEditingReview) "Tu Reseña" else if (isEditingReview) "Editar Reseña" else "Dejar una Reseña",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ComposeColor.White,
                    modifier = Modifier.padding(bottom = 15.dp)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ComposeColor(0x1A1EB7D9), RoundedCornerShape(12.dp))
                        .padding(15.dp)
                ) {
                    if (userReview != null && !isEditingReview) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(2.dp, ComposeColor(0xFF2EDFF2), RoundedCornerShape(8.dp))
                                .padding(15.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    (1..5).forEach { star ->
                                        Icon(
                                            imageVector = if (star <= userReview!!.rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                            contentDescription = "Estrella $star",
                                            tint = if (star <= userReview!!.rating) ComposeColor(0xFFFFD700) else ComposeColor(0xFFCCCCCC),
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    IconButton(
                                        onClick = { isEditingReview = true },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Editar reseña",
                                            tint = ComposeColor(0xFF2EDFF2),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { handleDeleteReview(db, userId, context) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Eliminar reseña",
                                            tint = ComposeColor(0xFFFF4444),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = userReview!!.comment,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = ComposeColor.White,
                                lineHeight = 26.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Enviado: ${userReview!!.fechaCreacion}",
                                fontSize = 14.sp,
                                color = ComposeColor(0xFFCCCCCC)
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            (1..5).forEach { star ->
                                IconButton(
                                    onClick = { rating = star },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (star <= rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                        contentDescription = "Estrella $star",
                                        tint = if (star <= rating) ComposeColor(0xFFFFD700) else ComposeColor(0xFFCCCCCC),
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                        }
                        TextField(
                            value = reviewText,
                            onValueChange = { reviewText = it },
                            placeholder = { Text("Escribe tu reseña aquí...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .border(1.dp, ComposeColor(0xFFDDDDDD), RoundedCornerShape(8.dp))
                                .background(ComposeColor.White, RoundedCornerShape(8.dp)),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = ComposeColor.White,
                                unfocusedContainerColor = ComposeColor.White,
                                focusedIndicatorColor = ComposeColor.Transparent,
                                unfocusedIndicatorColor = ComposeColor.Transparent
                            ),
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 15.sp,
                                color = ComposeColor.Black
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = {
                                    if (isEditingReview) {
                                        handleUpdateReview(db, userId, context)
                                    } else {
                                        handleSubmitReview(db, userId, context)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ComposeColor(0xFF2EDFF2),
                                    contentColor = ComposeColor(0xFF111826)
                                )
                            ) {
                                Text(
                                    text = if (isEditingReview) "Guardar Cambios" else "Enviar Reseña",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (isEditingReview) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        isEditingReview = false
                                        rating = 0
                                        reviewText = ""
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ComposeColor(0xFFCCCCCC),
                                        contentColor = ComposeColor(0xFF111826)
                                    )
                                ) {
                                    Text(
                                        text = "Cancelar",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Últimas Reseñas",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ComposeColor.White,
                    modifier = Modifier.padding(bottom = 15.dp)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ComposeColor(0x1A1EB7D9), RoundedCornerShape(12.dp))
                        .padding(15.dp)
                ) {
                    if (reviews.isEmpty()) {
                        Text(
                            text = "Sin reseñas",
                            fontSize = 14.sp,
                            color = ComposeColor.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        reviews.forEach { review ->
                            Review(
                                reviewerImageUrl = review.reviewerImageUrl,
                                reviewerName = review.reviewerName,
                                rating = review.rating,
                                reviewText = review.comment,
                                onClick = { navController.navigate("foreignProfile/${review.idEmisor}") }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
                if (reviews.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            navController.navigate("allReviews/$userId")
                        },
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(top = 10.dp)
                    ) {
                        Text(
                            text = "Ver todas las reseñas",
                            color = ComposeColor(0xFF2EDFF2),
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
        item {
            Button(
                onClick = { navController.navigate("chatDetail/${userId}") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 30.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ComposeColor(0xFF2EDFF2),
                    contentColor = ComposeColor(0xFF111826)
                )
            ) {
                Text(
                    text = "Contactar",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


data class ReviewData(
    val rating: Int = 0,
    val comment: String = "",
    val idEmisor: String = "",
    val fechaCreacion: String = "",
    val idReceptor: String = "",
    val reviewerName: String = "Anónimo", // Nuevo campo
    val reviewerImageUrl: String = "https://via.placeholder.com/150" // Nuevo campo
) {
    constructor() : this(0, "", "", "", "", "Anónimo", "https://via.placeholder.com/150")
}

fun loadTopReviews(userId: String, onResult: (List<ReviewData>) -> Unit) {
    if (userId.isBlank()) {
        Log.e("FirestoreError", "userId es null o vacío")
        onResult(emptyList())
        return
    }

    val db = FirebaseFirestore.getInstance()
    db.collection("reseñas")
        .whereEqualTo("idReceptor", userId.trim())
        .get()
        .addOnSuccessListener { result ->
            val fetchedReviews = mutableListOf<ReviewData>()
            val userIds = result.mapNotNull { it.getString("idEmisor") }.distinct()

            if (userIds.isEmpty()) {
                Log.d("FirestoreResult", "No se encontraron reseñas")
                onResult(emptyList())
                return@addOnSuccessListener
            }

            db.collection("usuarios")
                .whereIn(FieldPath.documentId(), userIds)
                .get()
                .addOnSuccessListener { userResult ->
                    val userMap = userResult.documents.associate { doc ->
                        doc.id to Pair(
                            doc.getString("nombre") ?: "Anónimo",
                            doc.getString("profileImage") ?: "https://via.placeholder.com/150"
                        )
                    }

                    fetchedReviews.addAll(result.mapNotNull { doc ->
                        try {
                            val idEmisor = doc.getString("idEmisor") ?: return@mapNotNull null
                            val (name, imageUrl) = userMap[idEmisor] ?: Pair("Anónimo", "https://via.placeholder.com/150")
                            ReviewData(
                                comment = doc.getString("comment") ?: "",
                                rating = doc.getLong("rating")?.toInt() ?: 0,
                                fechaCreacion = doc.getString("fechaCreacion") ?: "",
                                idReceptor = doc.getString("idReceptor") ?: "",
                                idEmisor = idEmisor,
                                reviewerName = name,
                                reviewerImageUrl = imageUrl
                            )
                        } catch (e: Exception) {
                            Log.e("FirestoreParse", "Error al convertir documento: ${doc.id}", e)
                            null
                        }
                    })

                    Log.d("FirestoreResult", "Se obtuvieron ${fetchedReviews.size} reseñas")
                    onResult(fetchedReviews)
                }

        }
        .addOnFailureListener { exception ->
            Log.e("FirestoreError", "Error al obtener reseñas", exception)
            onResult(emptyList())
        }
}