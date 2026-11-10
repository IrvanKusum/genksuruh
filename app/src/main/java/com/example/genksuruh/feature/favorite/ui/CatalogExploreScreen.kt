package com.example.genksuruh.feature.favorite.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.genksuruh.feature.favorite.data.FavoriteService
import com.example.genksuruh.feature.favorite.viewmodel.FavoriteEvent
import com.example.genksuruh.feature.favorite.viewmodel.FavoriteViewModel
import kotlinx.coroutines.launch

@Composable
fun CatalogExploreScreen(
    viewModel: FavoriteViewModel,
    onItemClick: (String) -> Unit,
    onNavigateToFavorites: () -> Unit
) {
    val catalogServices by viewModel.visibleCatalogServices.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val selectedCategory by viewModel.selectedCatalogCategory.collectAsState()
    val query by viewModel.catalogQuery.collectAsState()

    var searching by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is FavoriteEvent.Added -> {
                    scope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar(
                            message = "${event.item.title} ditambahkan ke favorit!"
                        )
                    }
                }
                is FavoriteEvent.Removed -> {
                    scope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar(
                            message = "${event.item.title} dihapus dari favorit."
                        )
                    }
                }
            }
        }
    }

    Scaffold(
        containerColor = FavColors.Bg,
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        topBar = {
            Surface(color = Color.White, shadowElevation = 2.dp) {
                Column(modifier = Modifier.statusBarsPadding()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .padding(start = 16.dp, end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (searching) {
                            OutlinedTextField(
                                value = query,
                                onValueChange = viewModel::setCatalogQuery,
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                textStyle = FavType.bodyMain14,
                                placeholder = { Text("Cari jasa di katalog...", style = FavType.body14) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = FavColors.Primary500,
                                    unfocusedBorderColor = FavColors.Divider,
                                    cursorColor = FavColors.Primary500
                                )
                            )
                        } else {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Katalog Jasa Kampus", style = FavType.screenTitle)
                                Text(
                                    text = "Tandai hati untuk menyimpan ke favorit",
                                    style = FavType.caption12
                                )
                            }
                        }
                        IconButton(onClick = {
                            if (searching) viewModel.setCatalogQuery("")
                            searching = !searching
                        }) {
                            Icon(
                                imageVector = if (searching) Icons.Rounded.Close else Icons.Rounded.Search,
                                contentDescription = if (searching) "Tutup" else "Cari",
                                tint = FavColors.TextMain,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    CategoryChipRow(
                        categories = viewModel.categories,
                        selected = selectedCategory,
                        onSelect = viewModel::selectCatalogCategory
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (catalogServices.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Tidak ada jasa yang sesuai pencarian",
                        style = FavType.body14,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(catalogServices, key = { it.id }) { service ->
                        val isFav = favorites.any { it.id == service.id }
                        CatalogServiceCard(
                            service = service,
                            isFavorite = isFav,
                            onClick = { onItemClick(service.id) },
                            onToggleFavorite = { viewModel.toggleFavorite(service) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CatalogServiceCard(
    service: FavoriteService,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val visual = categoryStyle(service.category)
    val shape = RoundedCornerShape(12.dp)

    Column(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = shape,
                ambientColor = FavColors.Primary500.copy(alpha = 0.2f),
                spotColor = FavColors.Primary500.copy(alpha = 0.2f)
            )
            .clip(shape)
            .background(Color.White)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .background(visual.background),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = visual.icon,
                contentDescription = null,
                tint = visual.iconTint,
                modifier = Modifier.size(52.dp)
            )

            // Tombol Bookmark / Favorite
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.95f))
                    .clickable(onClick = onToggleFavorite),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = if (isFavorite) "Hapus favorit" else "Tambah favorit",
                    tint = if (isFavorite) FavColors.Coral500 else FavColors.TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = service.title,
                style = FavType.title14,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = service.providerName,
                style = FavType.caption12,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = null,
                    tint = FavColors.Sand500,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.size(4.dp))
                Text(text = ratingText(service.rating), style = FavType.captionMain12)
                Spacer(Modifier.size(4.dp))
                Text(text = "(${service.reviewCount})", style = FavType.caption12)
            }
            Spacer(Modifier.height(8.dp))
            Text(text = "mulai dari", style = FavType.caption12)
            Text(text = rupiah(service.price), style = FavType.price14)
        }
    }
}

