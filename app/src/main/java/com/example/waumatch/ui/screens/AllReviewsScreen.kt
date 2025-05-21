package com.example.waumatch.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.waumatch.ui.theme.OceanBlue
import com.example.waumatch.ui.theme.SkyBlue
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.collections.set
import com.google.firebase.firestore.FieldPath


@Composable
fun AllReviewsScreen(userId: String, onBackClick: () -> Unit, navController: NavController) {
    val reviews = remember { mutableStateOf<List<ReviewData>>(emptyList()) }
    var filter by remember { mutableStateOf("Todas") }
    val filterOptions = listOf("Todas", "Positivas", "Críticas", "5", "4", "3", "2", "1")
    val votedReviews = remember { mutableStateMapOf<String, Boolean>() } // Clave: idEmisor+comment, Valor: true (Sí) o false (No)

    // Load reviews from Firestore
    LaunchedEffect(userId) {
        loadReviews(userId) { fetchedReviews ->
            reviews.value = fetchedReviews
        }
    }

    // Filter reviews based on the selected filter
    val filteredReviews = when (filter) {
        "Positivas" -> reviews.value.filter { it.rating >= 4 }
        "Críticas" -> reviews.value.filter { it.rating <= 3 }
        "5", "4", "3", "2", "1" -> reviews.value.filter { it.rating == filter.toInt() }
        else -> reviews.value // "Todas"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(colors = listOf(OceanBlue, SkyBlue))
            )
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 20.dp)
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = ComposeColor.White
                    )
                }
                Text(
                    text = "Reseñas",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = ComposeColor.White,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                )
            }
        }

        // Filter buttons in a LazyRow for horizontal scrolling
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filterOptions) { option ->
                    FilterButton(
                        text = option,
                        isSelected = filter == option,
                        onClick = { filter = option }
                    )
                }
            }
        }

        if (filteredReviews.isEmpty()) {
            item {
                Text(
                    text = "No hay reseñas para mostrar",
                    fontSize = 14.sp,
                    color = ComposeColor.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            items(filteredReviews) { review ->
                ReviewItem(
                    review = review,
                    onClick = { navController.navigate("foreignProfile/${review.idEmisor}") },
                    votedReviews = votedReviews,
                    onVoteChanged = { isUseful ->
                        val key = "${review.idEmisor}${review.comment}"
                        updateUsefulCount(review.idEmisor, review.idReceptor, review.comment, isUseful, votedReviews[key] ?: false)
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun FilterButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) ComposeColor(0xFF2EDFF2) else ComposeColor(0x1A1EB7D9)
    val textColor = if (isSelected) ComposeColor(0xFF111826) else ComposeColor.White

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = when (text) {
                "Positivas" -> "Positivas"
                "Críticas" -> "Críticas"
                else -> text
            },
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        )
        if (text in listOf("5", "4", "3", "2", "1")) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Star",
                tint = if (isSelected) ComposeColor(0xFF111826) else ComposeColor.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun ReviewItem(
    review: ReviewData,
    onClick: () -> Unit,
    votedReviews: MutableMap<String, Boolean>,
    onVoteChanged: (Boolean) -> Unit
) {
    var usefulCount by remember { mutableStateOf(review.usefulCount) }
    val voteKey = "${review.idEmisor}${review.comment}"
    val hasVoted = votedReviews.containsKey(voteKey)
    val isUseful = votedReviews[voteKey] ?: false

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(ComposeColor(0x1A1EB7D9), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            verticalAlignment = Alignment.Top
        ) {
            Image(
                painter = rememberAsyncImagePainter(review.emisorFoto),
                contentDescription = "Reviewer Image",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .border(1.dp, ComposeColor.Gray, CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = review.nombre,
                    fontWeight = FontWeight.Bold,
                    color = ComposeColor.White,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Row(
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    repeat(5) { index ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Star",
                            tint = if (index < review.rating) ComposeColor(0xFFFFD700) else ComposeColor(0xFFCCCCCC),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = review.fechaCreacion,
                        fontSize = 12.sp,
                        color = ComposeColor(0xFFCCCCCC)
                    )
                }
                Text(
                    text = review.comment,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = ComposeColor.White
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "¿Te ha resultado útil esta opinión?",
                fontSize = 12.sp,
                color = ComposeColor.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Sí",
                fontSize = 12.sp,
                color = ComposeColor(0xFF2EDFF2),
                fontWeight = if (hasVoted && isUseful) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier
                    .clickable {
                        if (!hasVoted || (hasVoted && !isUseful)) {
                            onVoteChanged(true)
                            votedReviews[voteKey] = true
                            if (!hasVoted) usefulCount++
                        }
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
            Text(
                text = "No",
                fontSize = 12.sp,
                color = ComposeColor(0xFF2EDFF2),
                fontWeight = if (hasVoted && !isUseful) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier
                    .clickable {
                        if (!hasVoted || (hasVoted && isUseful)) {
                            onVoteChanged(false)
                            votedReviews[voteKey] = false
                            if (!hasVoted) usefulCount++ else if (isUseful) usefulCount--
                        }
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        Text(
            text = "Útil para $usefulCount personas",
            fontSize = 12.sp,
            color = ComposeColor(0xFFCCCCCC),
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 4.dp)
        )
    }
}

data class ReviewData(
    val rating: Int = 0,
    val comment: String = "",
    val idEmisor: String = "",
    val nombre: String = "",
    val emisorFoto: String = "",
    val fechaCreacion: String = "",
    val idReceptor: String = "",
    val usefulCount: Int = 0
) {
    constructor() : this(0, "", "", "", "", "", "", 0)
}

fun loadReviews(userId: String, onResult: (List<ReviewData>) -> Unit) {
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
                                nombre = name,
                                comment = doc.getString("comment") ?: "",
                                emisorFoto = imageUrl,
                                rating = doc.getLong("rating")?.toInt() ?: 0,
                                fechaCreacion = doc.getString("fechaCreacion") ?: "",
                                idReceptor = doc.getString("idReceptor") ?: "",
                                idEmisor = doc.getString("idEmisor") ?: "",
                                usefulCount = doc.getLong("usefulCount")?.toInt() ?: 0
                            )
                        } catch (e: Exception) {
                            Log.e("FirestoreParse", "Error al convertir documento: ${doc.id}", e)
                            null
                        }
                    })

                    Log.d("FirestoreResult", "Se obtuvieron ${fetchedReviews.size} reseñas")
                    onResult(fetchedReviews)
                }
                .addOnFailureListener { exception ->
                    Log.e("FirestoreError", "Error al obtener usuarios", exception)
                    onResult(emptyList())
                }
        }
        .addOnFailureListener { exception ->
            Log.e("FirestoreError", "Error al obtener reseñas", exception)
            onResult(emptyList())
        }
}

fun updateUsefulCount(emisorId: String, receptorId: String, comment: String, isUseful: Boolean, previousVote: Boolean) {
    val db = FirebaseFirestore.getInstance()
    db.collection("reseñas")
        .whereEqualTo("idEmisor", emisorId)
        .whereEqualTo("idReceptor", receptorId)
        .whereEqualTo("comment", comment)
        .get()
        .addOnSuccessListener { querySnapshot ->
            if (!querySnapshot.isEmpty) {
                val reviewDoc = querySnapshot.documents.first()
                val currentCount = reviewDoc.getLong("usefulCount")?.toInt() ?: 0
                val newCount = when {
                    !previousVote && isUseful -> currentCount + 1
                    previousVote && !isUseful && currentCount > 0 -> currentCount - 1
                    else -> currentCount
                }
                reviewDoc.reference.update("usefulCount", newCount)
                    .addOnSuccessListener {
                        Log.d("FirestoreUpdate", "Contador de útil actualizado a $newCount")
                    }
                    .addOnFailureListener { e ->
                        Log.e("FirestoreUpdate", "Error al actualizar contador: ${e.message}")
                    }
            }
        }
        .addOnFailureListener { e ->
            Log.e("FirestoreQuery", "Error al buscar reseña para actualizar: ${e.message}")
        }
}