package com.example.genksuruh.feature.favorite.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import com.example.genksuruh.feature.favorite.data.FavoriteService
import com.example.genksuruh.feature.favorite.viewmodel.FavoriteViewModel

@Composable
fun FavoriteDetailScreen(
    serviceId: String,
    viewModel: FavoriteViewModel,
    onBack: () -> Unit,
    onOrderClick: (String) -> Unit
) {
    val service = remember(serviceId) { viewModel.getById(serviceId) }
    val favorites by viewModel.favorites.collectAsState()
    val isFavorite = service != null && favorites.any { it.id == service.id }

    if (service == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Jasa tidak ditemukan", style = FavType.body14)
        }
    } else {
        DetailContent(
            service = service,
            isFavorite = isFavorite,
            onBack = onBack,
            onToggleFavorite = {
                if (isFavorite) {
                    // ditangani lewat dialog
                } else {
                    viewModel.add(service)
                }
            },
            onDeleteConfirmed = {
                viewModel.remove(service)
            },
            onOrderClick = { onOrderClick(service.id) }
        )
    }
}

@Composable
private fun DetailContent(
    service: FavoriteService,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDeleteConfirmed: () -> Unit,
    onOrderClick: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val visual = categoryStyle(service.category)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .background(Brush.linearGradient(visual.gradient))
            ) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = visual.icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.22f))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = service.bannerLabel,
                            style = FavType.label11.copy(color = Color.White)
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    RoundIconButton(
                        icon = Icons.Rounded.ChevronLeft,
                        tint = FavColors.TextMain,
                        description = "Kembali",
                        onClick = onBack
                    )
                    RoundIconButton(
                        icon = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        tint = if (isFavorite) FavColors.Coral500 else FavColors.TextSecondary,
                        description = if (isFavorite) "Hapus dari favorit" else "Tambah ke favorit",
                        onClick = {
                            if (isFavorite) showDialog = true else onToggleFavorite()
                        }
                    )
                }
            }

            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
                Text(text = service.title, style = FavType.headline20)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        tint = FavColors.Sand500,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(text = ratingText(service.rating), style = FavType.mediumMain14)
                    Spacer(Modifier.width(4.dp))
                    Text(text = "(${service.reviewCount} ulasan)", style = FavType.caption12)
                    Spacer(Modifier.width(10.dp))
                    Pill(
                        text = service.category,
                        background = FavColors.Primary100,
                        textColor = FavColors.Primary700
                    )
                }

                service.discountLabel?.let { label ->
                    Spacer(Modifier.height(8.dp))
                    Pill(
                        text = label,
                        background = Color(0xFFFFF1C7),
                        textColor = Color(0xFFB36B00),
                        icon = Icons.Rounded.Bolt
                    )
                }

                Spacer(Modifier.height(16.dp))
                Text(text = "Biaya Jasa", style = FavType.caption12)
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(text = rupiah(service.price), style = FavType.price24)
                    service.originalPrice?.let {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = rupiah(it),
                            style = FavType.strike14,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = FavColors.Divider)
                Spacer(Modifier.height(16.dp))

                InfoRow(Icons.Rounded.Person, "Penyedia Jasa (Mahasiswa)", service.providerInfo)
                Spacer(Modifier.height(16.dp))
                InfoRow(Icons.Rounded.LocationOn, "Area Layanan", service.area)
                Spacer(Modifier.height(16.dp))
                InfoRow(Icons.Rounded.Schedule, "Estimasi Waktu Antar", service.estimate)

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = FavColors.Divider)
                Spacer(Modifier.height(16.dp))

                Text(text = "Deskripsi Layanan", style = FavType.section16)
                Spacer(Modifier.height(8.dp))
                Text(text = service.description, style = FavType.body14)
            }
        }

        Surface(color = Color.White, shadowElevation = 12.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isFavorite) {
                    OutlinedButton(
                        onClick = { showDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, FavColors.Danger),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = FavColors.Danger),
                        modifier = Modifier
                            .weight(0.42f)
                            .height(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Hapus Favorit",
                            style = FavType.title14.copy(color = FavColors.Danger),
                            maxLines = 1
                        )
                    }
                } else {
                    Button(
                        onClick = onToggleFavorite,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FavColors.Coral500),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        modifier = Modifier
                            .weight(0.42f)
                            .height(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Favorite,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Tambah Favorit",
                            style = FavType.button14,
                            maxLines = 1
                        )
                    }
                }

                val interaction = remember { MutableInteractionSource() }
                val pressed by interaction.collectIsPressedAsState()
                Button(
                    onClick = onOrderClick,
                    interactionSource = interaction,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (pressed) FavColors.Primary700 else FavColors.Primary500
                    ),
                    modifier = Modifier
                        .weight(0.58f)
                        .height(52.dp)
                ) {
                    Text(text = "Pesan Sekarang", style = FavType.button14, maxLines = 1)
                }
            }
        }
    }

    if (showDialog) {
        DeleteConfirmDialog(
            onDismiss = { showDialog = false },
            onConfirm = {
                showDialog = false
                onDeleteConfirmed()
            }
        )
    }
}

@Composable
private fun RoundIconButton(
    icon: ImageVector,
    tint: Color,
    description: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.95f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = tint,
            modifier = Modifier.size(26.dp)
        )
    }
}

@Composable
private fun Pill(
    text: String,
    background: Color,
    textColor: Color,
    icon: ImageVector? = null
) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(background)
            .padding(horizontal = 12.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(2.dp))
        }
        Text(text = text, style = FavType.label11.copy(color = textColor))
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(FavColors.Primary100),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = FavColors.Primary700,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(text = label, style = FavType.caption12)
            Text(text = value, style = FavType.mediumMain14)
        }
    }
}
