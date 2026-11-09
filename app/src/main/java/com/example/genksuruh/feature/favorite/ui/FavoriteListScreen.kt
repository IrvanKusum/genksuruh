package com.example.genksuruh.feature.favorite.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.genksuruh.feature.favorite.viewmodel.FavoriteEvent
import com.example.genksuruh.feature.favorite.viewmodel.FavoriteViewModel
import kotlinx.coroutines.launch

@Composable
fun FavoriteListScreen(
    viewModel: FavoriteViewModel,
    onItemClick: (String) -> Unit,
    onExploreClick: () -> Unit
) {
    val favorites by viewModel.favorites.collectAsState()
    val visible by viewModel.visibleFavorites.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val query by viewModel.query.collectAsState()

    var searching by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is FavoriteEvent.Removed -> {
                    scope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        val result = snackbarHostState.showSnackbar(
                            message = "${event.item.title} dihapus dari favorit",
                            actionLabel = "URUNGKAN",
                            duration = SnackbarDuration.Short
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            viewModel.undoRemove()
                        }
                    }
                }
                is FavoriteEvent.Added -> {
                    scope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar(
                            message = "${event.item.title} ditambahkan ke favorit",
                            duration = SnackbarDuration.Short
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
            FavoriteTopBar(
                searching = searching,
                query = query,
                onQueryChange = viewModel::setQuery,
                onSearchToggle = {
                    if (searching) viewModel.setQuery("")
                    searching = !searching
                },
                showFilters = favorites.isNotEmpty(),
                categories = viewModel.categories,
                selectedCategory = selectedCategory,
                onCategorySelect = viewModel::selectCategory
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            ) { data ->
                Snackbar(
                    shape = RoundedCornerShape(12.dp),
                    containerColor = FavColors.TextMain,
                    contentColor = Color.White,
                    action = {
                        data.visuals.actionLabel?.let { label ->
                            TextButton(onClick = { data.performAction() }) {
                                Text(
                                    text = label,
                                    style = FavType.title14.copy(color = FavColors.Primary300)
                                )
                            }
                        }
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = null,
                            tint = FavColors.Primary300,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.size(12.dp))
                        Text(
                            text = data.visuals.message,
                            style = FavType.bodyMain14.copy(color = Color.White)
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                favorites.isEmpty() -> EmptyFavoriteState(onExploreClick = onExploreClick)

                visible.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ada jasa yang cocok",
                        style = FavType.body14,
                        textAlign = TextAlign.Center
                    )
                }

                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(visible, key = { it.id }) { service ->
                        FavoriteCard(
                            service = service,
                            onClick = { onItemClick(service.id) },
                            onHeartClick = { viewModel.remove(service) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteTopBar(
    searching: Boolean,
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchToggle: () -> Unit,
    showFilters: Boolean,
    categories: List<String>,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(searching) {
        if (searching) focusRequester.requestFocus()
    }

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
                        onValueChange = onQueryChange,
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        textStyle = FavType.bodyMain14,
                        placeholder = { Text("Cari jasa favorit", style = FavType.body14) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FavColors.Primary500,
                            unfocusedBorderColor = FavColors.Divider,
                            cursorColor = FavColors.Primary500
                        )
                    )
                } else {
                    Text(
                        text = "Favorit Saya",
                        style = FavType.screenTitle,
                        modifier = Modifier.weight(1f)
                    )
                }
                IconButton(onClick = onSearchToggle) {
                    Icon(
                        imageVector = if (searching) Icons.Rounded.Close else Icons.Rounded.Search,
                        contentDescription = if (searching) "Tutup pencarian" else "Cari",
                        tint = FavColors.TextMain,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
            if (showFilters) {
                CategoryChipRow(
                    categories = categories,
                    selected = selectedCategory,
                    onSelect = onCategorySelect
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}
