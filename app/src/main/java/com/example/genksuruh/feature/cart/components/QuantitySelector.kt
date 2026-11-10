
package com.example.genksuruh.feature.cart.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.genksuruh.core.theme.GenkSuruhTheme
import com.example.genksuruh.feature.cart.MIN_CART_ITEM_QUANTITY

/**
 * Komponen stepper quantity reusable.
 * Quantity dan seluruh aksi dikelola melalui parameter dan callback.
 */
@Composable
fun QuantitySelector(
    quantity: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    modifier: Modifier = Modifier,
    minQuantity: Int = MIN_CART_ITEM_QUANTITY
) {
    Row(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tombol Kurang
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surface)
                .clickable(
                    enabled = quantity > minQuantity,
                    onClick = onDecrease
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Remove,
                contentDescription = "Kurangi jumlah",
                modifier = Modifier.size(20.dp),
                tint = if (quantity > minQuantity) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = 0.4f
                    )
                }
            )
        }

        // Angka Quantity
        Text(
            text = quantity.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(36.dp)
        )

        // Tombol Tambah
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primary)
                .clickable(onClick = onIncrease),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Tambah jumlah",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun QuantitySelectorPreview() {
    GenkSuruhTheme {
        QuantitySelector(
            quantity = 1,
            onIncrease = {},
            onDecrease = {}
        )
    }
}
