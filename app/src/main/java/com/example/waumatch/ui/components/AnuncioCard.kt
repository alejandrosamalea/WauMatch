package com.example.waumatch.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.DateRange
import com.example.waumatch.R
import com.example.waumatch.data.local.AnuncioEntity

@Composable
fun AnuncioCard(
    anuncio: AnuncioEntity,
    onClick: () -> Unit,
    onToggleFavorito: (AnuncioEntity) -> Unit
) {
    val userId = "usuario_actual"
    val isSeeker = anuncio.creador == userId

    Card(
        modifier = Modifier
            .width(IntrinsicSize.Max)
            .padding(8.dp)
            .border(
                width = if (isSeeker) 2.dp else 0.dp,
                color = if (isSeeker) Color(0xFF2EDFF2) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Image(
                painter = painterResource(id = R.drawable.perro),
                contentDescription = "Imagen del anuncio",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentScale = ContentScale.Crop
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = anuncio.titulo,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color(0xFF111826),
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 4.dp, start = 12.dp) // Padding solo a la izquierda y abajo
                )
                IconButton(
                    onClick = { onToggleFavorito(anuncio) },
                    modifier = Modifier.padding(start = 8.dp, end = 12.dp)
                ) {
                    Icon(
                        imageVector = if (anuncio.esFavorito) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (anuncio.esFavorito) "Quitar de favoritos" else "Añadir a favoritos",
                        tint = if (anuncio.esFavorito) Color.Red else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                if (isSeeker) {
                    Text(
                        text = "Busca Cuidador",
                        modifier = Modifier
                            .align(Alignment.End)
                            .background(Color(0xFF2EDFF2), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF111826),
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                        )
                    )
                }

                // Creador
                Text(
                    text = "Creador: ${anuncio.creador}",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF666666)),
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // Fechas (fechaInicio - fechaFin)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Rango de fechas",
                        tint = Color(0xFF666666),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${anuncio.fechaInicio} - ${anuncio.fechaFin}",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF666666))
                    )
                }

                // Descripción
                if (isSeeker) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Descripción",
                            tint = Color(0xFF666666),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = anuncio.descripcion,
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF666666))
                        )
                    }
                }
            }
        }
    }
}