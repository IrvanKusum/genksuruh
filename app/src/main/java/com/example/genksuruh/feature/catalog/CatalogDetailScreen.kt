package com.example.genksuruh.feature.catalog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.genksuruh.core.model.Service
import com.example.genksuruh.core.theme.Accent500
import com.example.genksuruh.core.theme.Secondary500
import com.example.genksuruh.core.util.toRupiah
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.genksuruh.core.theme.GenkSuruhTheme

private val TextMuted = Color(0xFF64748B)
private val ChipGray = Color(0xFFF1F5F9)

// SCREEN: menghubungkan ViewModel dengan Content
@Composable
fun CatalogDetailScreen(
    onBack: () -> Unit = {},
    onGoToCart: () -> Unit = {},
    isAdmin: Boolean = false,
    onEditClick: (String) -> Unit = {},
    viewModel: CatalogDetailViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    when (val s = state) {
        is CatalogDetailUiState.Loading -> Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }

        is CatalogDetailUiState.NotFound -> Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Jasa tidak ditemukan", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            Button(onClick = onBack) { Text("Kembali") }
        }

        is CatalogDetailUiState.Success -> CatalogDetailContent(
            service = s.service,
            inCart = s.inCart,
            snackbarHostState = snackbarHostState,
            onBack = onBack,
            onFavoriteClick = viewModel::toggleFavorite,
            onCartClick = {
                if (s.inCart) {
                    onGoToCart()
                } else {
                    viewModel.addToCart()
                    scope.launch { snackbarHostState.showSnackbar("Ditambahkan ke keranjang") }
                }
            },
            isAdmin = isAdmin,
            onEditClick = { onEditClick(s.service.id) }
        )
    }
}

// CONTENT: hanya menerima nilai dan lambda
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CatalogDetailContent(
    service: Service,
    inCart: Boolean,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onFavoriteClick: () -> Unit,
    onCartClick: () -> Unit,
    isAdmin: Boolean = false,
    onEditClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState() // state tampilan murni (posisi scroll)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            DetailBottomBar(
                price = service.price,
                inCart = inCart,
                isAdmin = isAdmin,
                onClick = if (isAdmin) onEditClick else onCartClick
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.verticalScroll(scrollState)) {
                Box {
                    HeroImage(
                        service = service,
                        modifier = Modifier.fillMaxWidth().height(260.dp)
                    )
                    Column(
                        modifier = Modifier
                            .padding(top = 236.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                            .background(Color.White)
                            .padding(16.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                service.category,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(Modifier.height(12.dp))

                        Text(
                            service.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Star, null, tint = Accent500, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.size(4.dp))
                            Text(
                                "${service.rating} (${service.reviewCount} ulasan)",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.size(12.dp))
                            Text(
                                "Lihat ulasan",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.clickable { /* nanti: ke fitur Ulasan (Ahsana) */ }
                            )
                        }
                        Spacer(Modifier.height(16.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                                .padding(16.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                service.price.toRupiah(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.size(6.dp))
                            Text(
                                "/ sekali pesan",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = ChipGray)
                        Spacer(Modifier.height(16.dp))

                        SectionTitle("Deskripsi Layanan")
                        Text(
                            service.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )

                        Spacer(Modifier.height(20.dp))
                        SectionTitle("Area Layanan")
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            service.areas.forEach { area ->
                                Surface(shape = RoundedCornerShape(50), color = ChipGray) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Rounded.LocationOn, null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.size(4.dp))
                                        Text(area, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(20.dp))
                        SectionTitle("Jam Operasional")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.background)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.AccessTime, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.size(12.dp))
                            Text(
                                service.hours,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(Modifier.height(20.dp))
                        ProviderCard(service)
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }

            // Bar atas: transparan di atas foto, putih + judul saat di-scroll
            val progress = (scrollState.value / 600f).coerceIn(0f, 1f)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = progress))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoundIconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Kembali")
                }
                if (progress > 0.9f) {
                    Text(
                        service.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
                    )
                } else {
                    Spacer(Modifier.weight(1f))
                }
                RoundIconButton(onClick = onFavoriteClick) {
                    Icon(
                        if (service.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        "Favorit",
                        tint = if (service.isFavorite) Secondary500 else TextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroImage(service: Service, modifier: Modifier = Modifier) {
    when {
        service.imageRes != null -> Image(
            painter = painterResource(service.imageRes),
            contentDescription = service.title,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
        service.imageUrl != null -> AsyncImage(
            model = service.imageUrl,
            contentDescription = service.title,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
        else -> Box(
            modifier = modifier.background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.Image, null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}
@Composable
private fun RoundIconButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.White)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { content() }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun ProviderCard(service: Service) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text("PENYEDIA JASA", style = MaterialTheme.typography.labelMedium, color = TextMuted)
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    service.providerName.first().toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        service.providerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.size(4.dp))
                    Icon(
                        Icons.Rounded.Verified, null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Star, null, tint = Accent500, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.size(4.dp))
                    Text("${service.rating}", style = MaterialTheme.typography.bodySmall)
                }
            }
            OutlinedButton(
                onClick = { /* nanti: hubungi penyedia */ },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("Hubungi", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun DetailBottomBar(
    price: Int,
    inCart: Boolean,
    isAdmin: Boolean,
    onClick: () -> Unit
) {
    Surface(color = Color.White, shadowElevation = 12.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    if (isAdmin) "Harga Jasa" else "Total Ongkos",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
                Text(
                    price.toRupiah(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.size(16.dp))
            Button(
                onClick = onClick,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Secondary500)
            ) {
                Icon(
                    if (isAdmin) Icons.Rounded.Edit else Icons.Rounded.ShoppingCart,
                    null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    when {
                        isAdmin -> "Edit Jasa"
                        inCart -> "Lihat Keranjang"
                        else -> "Tambah ke Keranjang"
                    },
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CatalogDetailPreview() {
    GenkSuruhTheme {
        CatalogDetailContent(
            service = DummyServices.list.first(),
            inCart = false,
            snackbarHostState = remember { SnackbarHostState() },
            onBack = {},
            onFavoriteClick = {},
            onCartClick = {}
        )
    }
}