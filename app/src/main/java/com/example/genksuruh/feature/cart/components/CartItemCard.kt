package com.example.genksuruh.feature.cart.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.genksuruh.core.theme.GenkSuruhTheme
import com.example.genksuruh.core.theme.Primary100
import com.example.genksuruh.core.theme.Primary700
import com.example.genksuruh.core.theme.Success
import com.example.genksuruh.feature.cart.CartItem
import com.example.genksuruh.feature.cart.formatRupiah

/**
 * Kartu untuk satu item di daftar Keranjang (LazyColumn). Stateless: seluruh
 * data dan aksi datang dari luar supaya mudah dipakai ulang & di-Preview.
 */
@Composable
fun CartItemCard(
    item: CartItem,
    onSelectionChange: (Boolean) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onIncreaseQuantity: () -> Unit,
    onDecreaseQuantity: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Checkbox(checked = item.isSelected, onCheckedChange = onSelectionChange)

                CartItemThumbnail(category = item.category)

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.serviceName,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Verified,
                            contentDescription = null,
                            tint = Success,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = item.providerName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = "Hapus ${item.serviceName}",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                CartItemInfoRow(icon = Icons.Filled.LocationOn, text = item.location)
                Spacer(modifier = Modifier.height(4.dp))
                CartItemInfoRow(icon = Icons.Filled.Schedule, text = "${item.date} \u2022 ${item.time}")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Ongkos Jasa",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatRupiah(item.price),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Primary700
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Filled.EditNote,
                            contentDescription = "Catatan / edit pesanan",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    QuantitySelector(
                        quantity = item.quantity,
                        onIncrease = onIncreaseQuantity,
                        onDecrease = onDecreaseQuantity
                    )
                }
            }
        }
    }
}

@Composable
private fun CartItemThumbnail(category: String) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Primary100),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.ShoppingBag,
            contentDescription = category,
            tint = Primary700
        )
    }
}

@Composable
private fun CartItemInfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CartItemCardPreview() {
    GenkSuruhTheme {
        CartItemCard(
            item = CartItem(
                id = "preview",
                serviceName = "Antar Makanan Kantin FILKOM",
                category = "Antar Makanan",
                providerName = "Dimas Pratama",
                price = 8_000,
                location = "Gedung FILKOM Lt. 2",
                date = "1 November 2026",
                time = "12.30 WIB",
                quantity = 1,
                isSelected = true
            ),
            onSelectionChange = {},
            onEditClick = {},
            onDeleteClick = {},
            onIncreaseQuantity = {},
            onDecreaseQuantity = {}
        )
    }
}
