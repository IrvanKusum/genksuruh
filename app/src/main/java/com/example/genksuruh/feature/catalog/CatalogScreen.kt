package com.example.genksuruh.feature.catalog

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.genksuruh.core.auth.LocalAccountName
import com.example.genksuruh.core.theme.Secondary500
import com.example.genksuruh.feature.profile.ProfileEntryButton
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.genksuruh.feature.cart.CartRepository

private val TextMuted = Color(0xFF64748B)


@Composable
fun CatalogScreen(
    onServiceClick: (String) -> Unit = {},
    onAddClick: () -> Unit = {},
    showAddButton: Boolean = true,
    onProfileClick: () -> Unit = {},
    viewModel: CatalogViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                CatalogHeader(onProfileClick = onProfileClick)
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                SearchBar(query = state.query, onQueryChange = viewModel::onQueryChange)
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                CategoryRow(
                    selected = state.selectedCategory,
                    onSelected = viewModel::onCategorySelected
                )
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                PriceRow(
                    selected = state.priceFilter,
                    count = state.services.size,
                    showCount = !state.isLoading,
                    onSelected = viewModel::onPriceSelected
                )
            }

            when {
                state.isLoading -> {
                    items(6) { SkeletonCard() }
                }

                state.isEmpty -> {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        EmptyState(onReset = viewModel::resetFilters)
                    }
                }

                else -> {
                    items(state.services, key = { it.id }) { service ->
                        ServiceCard(
                            service = service,
                            onClick = { onServiceClick(service.id) },
                            onFavoriteClick = { viewModel.toggleFavorite(service.id) },
                            onCartClick = if (showAddButton) null else {
                                {
                                    val added = CartRepository.shared.addService(service)
                                    Toast.makeText(
                                        context,
                                        if (added) "Ditambahkan ke keranjang" else "Sudah ada di keranjang",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) { TrustBanner() }
                }
            }
        }

        if (showAddButton) {
            ExtendedFloatingActionButton(
                onClick = onAddClick,
                containerColor = Secondary500,
                contentColor = Color.White,
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text("Tambah Jasa", fontWeight = FontWeight.SemiBold) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )
        }
    }
}


@Composable
private fun CatalogHeader(onProfileClick: () -> Unit) {
    val firstName = LocalAccountName.current.trim().substringBefore(" ")

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Bolt, null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "GENKSURUH.CO",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                Text("Beranda", style = MaterialTheme.typography.titleLarge)
            }
            IconButton(onClick = {}) {
                Icon(
                    Icons.Rounded.Notifications,
                    contentDescription = "Notifikasi",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(Modifier.size(4.dp))
            ProfileEntryButton(onClick = onProfileClick)
        }

        Spacer(Modifier.height(8.dp))

        Text(
            if (firstName.isNotEmpty()) "Halo, $firstName \uD83D\uDC4B" else "Halo \uD83D\uDC4B",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted
        )
        Spacer(Modifier.height(2.dp))
        Text("Mau suruh apa hari ini?", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Cari jasa suruhan...", color = TextMuted) },
        leadingIcon = { Icon(Icons.Rounded.Search, null, tint = TextMuted) },
        trailingIcon = { Icon(Icons.Rounded.Tune, null, tint = MaterialTheme.colorScheme.primary) },
        singleLine = true,
        shape = RoundedCornerShape(50),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Transparent
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryRow(selected: String, onSelected: (String) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(CatalogCategories) { category ->
            FilterChip(
                selected = category == selected,
                onClick = { onSelected(category) },
                label = { Text(category, style = MaterialTheme.typography.labelLarge) },
                shape = RoundedCornerShape(50),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.White,
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PriceRow(
    selected: PriceFilter,
    count: Int,
    showCount: Boolean,
    onSelected: (PriceFilter) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Harga", style = MaterialTheme.typography.labelLarge, color = TextMuted)
        Spacer(Modifier.size(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            listOf(PriceFilter.UNDER_10K, PriceFilter.RANGE_10_50K).forEach { filter ->
                FilterChip(
                    selected = selected == filter,
                    onClick = { onSelected(filter) },
                    label = { Text(filter.label, style = MaterialTheme.typography.labelMedium) },
                    shape = RoundedCornerShape(50),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color.White,
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
        if (showCount) {
            Text("$count jasa aktif", style = MaterialTheme.typography.bodySmall, color = TextMuted)
        }
    }
}

@Composable
private fun SkeletonCard() {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "skeletonAlpha"
    )
    val gray = Color(0xFFE2E8F0)
    Column(
        modifier = Modifier
            .alpha(alpha)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(4f / 3f).background(gray))
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.fillMaxWidth(0.4f).height(10.dp).clip(RoundedCornerShape(4.dp)).background(gray))
            Box(Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(4.dp)).background(gray))
            Box(Modifier.fillMaxWidth(0.6f).height(14.dp).clip(RoundedCornerShape(4.dp)).background(gray))
            Box(Modifier.fillMaxWidth(0.5f).height(18.dp).clip(RoundedCornerShape(4.dp)).background(gray))
        }
    }
}

@Composable
private fun EmptyState(onReset: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.SearchOff, null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(44.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text("Jasa tidak ditemukan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text(
            "Coba kata kunci atau kategori lain",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onReset) { Text("Reset Filter") }
    }
}

@Composable
private fun TrustBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Rounded.VerifiedUser, null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.size(12.dp))
        Column {
            Text("Runner Terverifikasi Kampus", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "Semua runner terdaftar resmi dengan KTM dan diverifikasi admin GenkSuruh.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
    }
}