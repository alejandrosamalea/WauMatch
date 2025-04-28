package com.example.waumatch.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.waumatch.ui.theme.AquaLight
import com.example.waumatch.ui.theme.OceanBlue
import com.example.waumatch.ui.theme.SkyBlue
import com.example.waumatch.viewmodel.ProfileManager

@Composable
fun ProfileScreen(viewModel: ProfileManager = viewModel(factory = ProfileManagerFactory(LocalContext.current))) {
    val context = LocalContext.current
    val isOwnProfile = true

    val profileData by viewModel.getProfileData().observeAsState(ProfileManager.ProfileData())
    val isDataLoaded by viewModel.getIsDataLoaded().observeAsState(false)
    val isEditing by viewModel.getIsEditing().observeAsState(false)

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.updateProfileImage(it.toString()) }
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
                    .padding(top = 60.dp, bottom = 30.dp)
                    .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
            ) {
                if (isOwnProfile) {
                    IconButton(
                        onClick = { viewModel.toggleEditing() },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 10.dp, end = 20.dp)
                            .background(ComposeColor(0x1A1EB7D9), RoundedCornerShape(20.dp))
                            .size(44.dp)
                    ) {
                        Icon(
                            imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                            contentDescription = if (isEditing) "Save" else "Edit",
                            tint = ComposeColor(0xFF2EDFF2)
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box {
                        Image(
                            painter = rememberAsyncImagePainter(profileData.profileImage),
                            contentDescription = "Profile Image",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .border(3.dp, ComposeColor(0xFF2EDFF2), CircleShape)
                                .run {
                                    if (isEditing) this.alpha(0.8f) else this
                                }
                                .clickable(enabled = isEditing) {
                                    pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                },
                            contentScale = ContentScale.Crop
                        )
                        if (isEditing) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Change Image",
                                tint = ComposeColor.White,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(15.dp))
                    if (isEditing) {
                        TextField(
                            value = profileData.name,
                            onValueChange = { newName ->
                                val updatedData = ProfileManager.ProfileData().apply {
                                    setName(newName)
                                    setSubtitle(profileData.subtitle)
                                    setAbout(profileData.about)
                                    setAvailability(profileData.availability)
                                    setProfileImage(profileData.profileImage)
                                }
                                viewModel.getProfileData().setValue(updatedData) // Usar getProfileData().setValue
                            },
                            modifier = Modifier
                                .width(200.dp)
                                .border(1.dp, ComposeColor(0xFF2EDFF2), RoundedCornerShape(8.dp)),
                            textStyle = LocalTextStyle.current.copy(
                                color = ComposeColor.White,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            ),
                            placeholder = { Text("Tu nombre", color = ComposeColor(0xFF666666)) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = ComposeColor.Transparent,
                                unfocusedContainerColor = ComposeColor.Transparent,
                                focusedIndicatorColor = ComposeColor.Transparent,
                                unfocusedIndicatorColor = ComposeColor.Transparent
                            )
                        )
                    } else {
                        Text(
                            text = if (isDataLoaded) profileData.name else "Cargando...",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = ComposeColor.White
                        )
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    if (isEditing) {
                        TextField(
                            value = profileData.subtitle,
                            onValueChange = { newSubtitle ->
                                val updatedData = ProfileManager.ProfileData().apply {
                                    setName(profileData.name)
                                    setSubtitle(newSubtitle)
                                    setAbout(profileData.about)
                                    setAvailability(profileData.availability)
                                    setProfileImage(profileData.profileImage)
                                }
                                viewModel.getProfileData().setValue(updatedData)
                            },
                            modifier = Modifier
                                .width(200.dp)
                                .border(1.dp, ComposeColor(0xFF2EDFF2), RoundedCornerShape(8.dp)),
                            textStyle = LocalTextStyle.current.copy(
                                color = ComposeColor(0xFF1EB7D9),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            ),
                            placeholder = { Text("Tu descripción corta", color = ComposeColor(0xFF666666)) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = ComposeColor.Transparent,
                                unfocusedContainerColor = ComposeColor.Transparent,
                                focusedIndicatorColor = ComposeColor.Transparent,
                                unfocusedIndicatorColor = ComposeColor.Transparent
                            )
                        )
                    } else {
                        Text(
                            text = profileData.subtitle,
                            fontSize = 16.sp,
                            color = ComposeColor(0xFF1EB7D9)
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatItem(number = "4.9", label = "Rating")
                        StatItem(number = "127", label = "Cuidados")
                        StatItem(number = "3", label = "Años")
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
                    if (isEditing) {
                        TextField(
                            value = profileData.about,
                            onValueChange = { newAbout ->
                                val updatedData = ProfileManager.ProfileData().apply {
                                    setName(profileData.name)
                                    setSubtitle(profileData.subtitle)
                                    setAbout(newAbout)
                                    setAvailability(profileData.availability)
                                    setProfileImage(profileData.profileImage)
                                }
                                viewModel.getProfileData().setValue(updatedData)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .border(1.dp, ComposeColor(0xFF2EDFF2), RoundedCornerShape(8.dp)),
                            textStyle = LocalTextStyle.current.copy(
                                color = ComposeColor.White,
                                fontSize = 15.sp
                            ),
                            placeholder = { Text("Cuéntanos sobre ti...", color = ComposeColor(0xFF666666)) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = ComposeColor.Transparent,
                                unfocusedContainerColor = ComposeColor.Transparent,
                                focusedIndicatorColor = ComposeColor.Transparent,
                                unfocusedIndicatorColor = ComposeColor.Transparent
                            )
                        )
                    } else {
                        Text(
                            text = profileData.about,
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            color = ComposeColor.White,
                            modifier = Modifier.padding(bottom = 15.dp)
                        )
                    }
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Tag(text = "♥️ Amante de los animales")
                        Tag(text = "🏠 Casa con jardín")
                        Tag(text = "📍 Madrid Centro")
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
                    AvailabilityItem(
                        day = "Lun - Vie",
                        time = profileData.availability.weekdays,
                        isEditing = isEditing,
                        onTimeChange = { newTime ->
                            val updatedAvailability = ProfileManager.Availability(newTime, profileData.availability.weekends)
                            val updatedData = ProfileManager.ProfileData().apply {
                                setName(profileData.name)
                                setSubtitle(profileData.subtitle)
                                setAbout(profileData.about)
                                setAvailability(updatedAvailability)
                                setProfileImage(profileData.profileImage)
                            }
                            viewModel.getProfileData().setValue(updatedData)
                        }
                    )
                    AvailabilityItem(
                        day = "Sáb - Dom",
                        time = profileData.availability.weekdays,
                        isEditing = isEditing,
                        onTimeChange = { newTime ->
                            val updatedAvailability = ProfileManager.Availability(profileData.availability.weekdays, newTime)
                            val updatedData = ProfileManager.ProfileData().apply {
                                setName(profileData.name)
                                setSubtitle(profileData.subtitle)
                                setAbout(profileData.about)
                                setAvailability(updatedAvailability)
                                setProfileImage(profileData.profileImage)
                            }
                            viewModel.getProfileData().setValue(updatedData)
                        }
                    )
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
                    Review(
                        reviewerImageUrl = "https://api.a0.dev/assets/image?text=happy%20person%20avatar&aspect=1:1",
                        reviewerName = "Carlos P.",
                        rating = 5,
                        reviewText = "Excelente cuidadora. Mi perro regresó muy feliz y bien cuidado."
                    )
                }
            }
        }
        if (!isOwnProfile) {
            item {
                Button(
                    onClick = { /* Acción de contacto */ },
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
}

// Factory para crear ProfileManager
class ProfileManagerFactory(private val context: android.content.Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileManager::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileManager(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

// Componentes auxiliares (StatItem, Tag, AvailabilityItem, Review)
@Composable
fun StatItem(number: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = number,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = ComposeColor(0xFF2EDFF2)
        )
        Text(
            text = label,
            fontSize = 14.sp,
            color = ComposeColor.White,
            modifier = Modifier.padding(top = 5.dp)
        )
    }
}

@Composable
fun Tag(text: String) {
    Box(
        modifier = Modifier
            .background(ComposeColor(0x262EDFF2), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            color = ComposeColor(0xFF2EDFF2)
        )
    }
}

@Composable
fun AvailabilityItem(day: String, time: String, isEditing: Boolean, onTimeChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = day,
            color = ComposeColor.White,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
        if (isEditing) {
            TextField(
                value = time,
                onValueChange = onTimeChange,
                modifier = Modifier
                    .width(150.dp)
                    .border(1.dp, ComposeColor(0xFF2EDFF2), RoundedCornerShape(8.dp)),
                textStyle = LocalTextStyle.current.copy(
                    color = ComposeColor(0xFF2EDFF2),
                    fontSize = 14.sp,
                    textAlign = TextAlign.End
                ),
                placeholder = { Text("00:00 - 00:00", color = ComposeColor(0xFF666666)) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = ComposeColor.Transparent,
                    unfocusedContainerColor = ComposeColor.Transparent,
                    focusedIndicatorColor = ComposeColor.Transparent,
                    unfocusedIndicatorColor = ComposeColor.Transparent
                )
            )
        } else {
            Text(
                text = time,
                color = ComposeColor(0xFF2EDFF2),
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }
}

@Composable
fun Review(reviewerImageUrl: String, reviewerName: String, rating: Int, reviewText: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Image(
            painter = rememberAsyncImagePainter(reviewerImageUrl),
            contentDescription = "Reviewer Image",
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .border(1.dp, ComposeColor.Gray, CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(15.dp))
        Column {
            Text(
                text = reviewerName,
                fontWeight = FontWeight.Bold,
                color = ComposeColor.White,
                modifier = Modifier.padding(bottom = 5.dp)
            )
            Row(
                modifier = Modifier.padding(bottom = 5.dp)
            ) {
                repeat(rating) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Star",
                        tint = ComposeColor(0xFFFFD700),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Text(
                text = "\"$reviewText\"",
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = ComposeColor.White
            )
        }
    }
}