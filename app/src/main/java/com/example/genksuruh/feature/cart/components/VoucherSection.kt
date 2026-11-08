
package com.example.genksuruh.feature.cart.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.genksuruh.core.theme.Danger
import com.example.genksuruh.core.theme.Primary100
import com.example.genksuruh.core.theme.Primary700
import com.example.genksuruh.core.theme.Success
import com.example.genksuruh.feature.cart.VoucherStatus
import com.example.genksuruh.feature.cart.formatRupiah

/**
 * Input, loading, error, dan hasil penerapan voucher pada Checkout.
 */
@Composable
fun VoucherSection(
    code: String,
    status: VoucherStatus,
    message: String?,
    discountPercent: Int,
    minimumTransaction: Long,
    discountAmount: Long,
    subtotal: Long,
    onCodeChange: (String) -> Unit,
    onApplyClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.LocalOffer,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(19.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = "Punya Voucher?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.weight(1f))

            if (status == VoucherStatus.APPLIED) {
                Text(
                    text = "Voucher diterapkan",
                    style = MaterialTheme.typography.labelSmall,
                    color = Success,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(15.dp),
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                when (status) {
                    VoucherStatus.APPLIED -> AppliedVoucherContent(
                        code = code,
                        discountPercent = discountPercent,
                        minimumTransaction = minimumTransaction,
                        discountAmount = discountAmount,
                        onRemoveClick = onRemoveClick
                    )

                    VoucherStatus.VALIDATING -> VoucherInputRow(
                        code = code,
                        enabled = false,
                        isLoading = true,
                        onCodeChange = onCodeChange,
                        onApplyClick = onApplyClick
                    )

                    else -> {
                        VoucherInputRow(
                            code = code,
                            enabled = true,
                            isLoading = false,
                            onCodeChange = onCodeChange,
                            onApplyClick = onApplyClick
                        )

                        when (status) {
                            VoucherStatus.NOT_FOUND,
                            VoucherStatus.MINIMUM_NOT_MET -> VoucherMessage(
                                message = buildString {
                                    append(message.orEmpty())

                                    if (status == VoucherStatus.MINIMUM_NOT_MET) {
                                        append(
                                            " Subtotal saat ini ${formatRupiah(subtotal)}."
                                        )
                                    }
                                },
                                isError = true
                            )

                            VoucherStatus.INPUT -> VoucherMessage(
                                message = "Tekan Gunakan untuk memvalidasi voucher.",
                                isError = false
                            )

                            else -> VoucherMessage(
                                message = message
                                    ?: "Diskon dan promo divalidasi oleh sistem kampus.",
                                isError = false
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// RESPONSIVE VOUCHER INPUT
// ============================================================

@Composable
private fun VoucherInputRow(
    code: String,
    enabled: Boolean,
    isLoading: Boolean,
    onCodeChange: (String) -> Unit,
    onApplyClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = code,
            onValueChange = onCodeChange,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            enabled = enabled,
            singleLine = true,
            placeholder = {
                Text(
                    text = "Masukkan kode voucher",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.ConfirmationNumber,
                    contentDescription = null,
                    modifier = Modifier.size(21.dp)
                )
            },
            shape = RoundedCornerShape(10.dp)
        )

        Button(
            onClick = onApplyClick,
            enabled = enabled && code.isNotBlank(),
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(min = 96.dp, max = 124.dp),
            shape = RoundedCornerShape(10.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 8.dp,
                vertical = 4.dp
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = Primary700
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(17.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )

                Spacer(modifier = Modifier.width(5.dp))

                Text(
                    text = "Memeriksa...",
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelSmall
                )
            } else {
                Text(
                    text = "Gunakan",
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ============================================================
// APPLIED VOUCHER
// ============================================================

@Composable
private fun AppliedVoucherContent(
    code: String,
    discountPercent: Int,
    minimumTransaction: Long,
    discountAmount: Long,
    onRemoveClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Success.copy(alpha = 0.10f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .background(
                        Success,
                        RoundedCornerShape(50)
                    )
                    .padding(
                        horizontal = 9.dp,
                        vertical = 5.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(13.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "Voucher Diterapkan",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            TextButton(onClick = onRemoveClick) {
                Icon(
                    imageVector = Icons.Filled.DeleteOutline,
                    contentDescription = null,
                    tint = Danger,
                    modifier = Modifier.size(16.dp)
                )

                Text(
                    text = "Hapus",
                    color = Danger
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Success,
                shape = RoundedCornerShape(9.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalOffer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(9.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$code • Diskon $discountPercent% Kampus",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Success,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Min. Belanja ${formatRupiah(minimumTransaction)} • Hemat ${formatRupiah(discountAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Schedule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(15.dp)
            )

            Spacer(modifier = Modifier.width(5.dp))

            Text(
                text = "Voucher diterapkan pada checkout ini",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ============================================================
// VOUCHER MESSAGE
// ============================================================

@Composable
private fun VoucherMessage(
    message: String,
    isError: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (isError) {
                    Danger.copy(alpha = 0.08f)
                } else {
                    Primary100.copy(alpha = 0.55f)
                },
                shape = RoundedCornerShape(9.dp)
            )
            .padding(9.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = if (isError) {
                Icons.Filled.ErrorOutline
            } else {
                Icons.Filled.Info
            },
            contentDescription = null,
            tint = if (isError) Danger else Primary700,
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = if (isError) {
                Danger
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}
