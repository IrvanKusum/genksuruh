
package com.example.genksuruh.feature.cart.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.genksuruh.core.theme.GenkSuruhTheme
import com.example.genksuruh.core.theme.Primary700
import com.example.genksuruh.core.theme.Secondary500
import com.example.genksuruh.feature.cart.formatRupiah

/**
 * Ringkasan keranjang sederhana:
 * jumlah jasa dipilih, total, dan tombol Checkout.
 */
@Composable
fun CartSummaryCard(
    selectedCount: Int,
    total: Long,
    isCheckoutEnabled: Boolean,
    onCheckoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Informasi jumlah dan total.
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = "$selectedCount item dipilih",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Total:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = formatRupiah(total),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Primary700
                    )
                }
            }

            // Tombol Checkout oranye.
            Button(
                onClick = onCheckoutClick,
                enabled = isCheckoutEnabled,
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Secondary500
                )
            ) {
                Text(
                    text = "Checkout ($selectedCount)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.width(3.dp))

                androidx.compose.material3.Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = null
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CartSummaryCardPreview() {
    GenkSuruhTheme {
        CartSummaryCard(
            selectedCount = 2,
            total = 13_000,
            isCheckoutEnabled = true,
            onCheckoutClick = {}
        )
    }
}
