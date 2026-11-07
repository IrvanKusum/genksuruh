package com.example.genksuruh.feature.review

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.genksuruh.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ReviewFormScreen(
    vm: ReviewViewModel,
    isEdit: Boolean,
    onClose: () -> Unit,
    onDone: () -> Unit,
) {
    val existing = remember { if (isEdit) vm.state.value.myReview else null }
    var rating by rememberSaveable { mutableIntStateOf(existing?.rating ?: 0) }
    var comment by rememberSaveable { mutableStateOf(existing?.comment ?: "") }
    var isSending by remember { mutableStateOf(false) }
    var showErrors by remember { mutableStateOf(false) }

    val snackbarHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val ratingError = showErrors && rating == 0
    val commentError = showErrors && comment.trim().length < 10
    val isValid = rating > 0 && comment.trim().length >= 10

    fun submit() {
        isSending = true
        vm.submitReview(rating, comment.trim()) { ok ->
            isSending = false
            if (ok) onDone()
            else scope.launch {
                val r = snackbarHost.showSnackbar(
                    "Gagal mengirim ulasan. Periksa koneksi internet kamu.",
                    actionLabel = "Coba Lagi",
                )
                if (r == SnackbarResult.ActionPerformed) submit()
            }
        }
    }

    Scaffold(
        containerColor = ScreenBg,
        topBar = {
            Surface(color = Color.White, shadowElevation = 2.dp) {
                Row(
                    Modifier.statusBarsPadding().padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onClose, enabled = !isSending) {
                        Icon(Icons.Default.Close, "Tutup")
                    }
                    Text(
                        if (isEdit) "Edit Ulasan" else "Tulis Ulasan",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        },
        snackbarHost = {
            SnackbarHost(snackbarHost) {
                Snackbar(it, containerColor = DangerRed, contentColor = Color.White, actionColor = Color.White)
            }
        },
        bottomBar = {
            Surface(color = Color.White, shadowElevation = 8.dp) {
                Column(Modifier.navigationBarsPadding().padding(16.dp)) {
                    Button(
                        onClick = { if (isValid) submit() else showErrors = true },
                        enabled = !isSending,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            // Tampak abu sampai form valid, tapi tetap bisa ditekan untuk memunculkan error
                            containerColor = if (isValid) Coral else StarEmpty,
                            contentColor = if (isValid) Color.White else TextGray,
                            disabledContainerColor = Coral.copy(alpha = 0.7f),
                            disabledContentColor = Color.White,
                        ),
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text("Mengirim...", fontWeight = FontWeight.SemiBold)
                        } else {
                            Text(
                                if (isEdit) "Simpan Perubahan" else "Kirim Ulasan",
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                    TextButton(
                        onClick = onClose,
                        enabled = !isSending,
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Batal", color = TextGray) }
                }
            }
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Kartu info jasa
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(BlueSoft).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)).background(Color.White),
                    contentAlignment = Alignment.Center,
                ) { Text(vm.serviceEmoji, fontSize = 28.sp) }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(vm.serviceName, style = MaterialTheme.typography.titleSmall)
                    Text("oleh ${vm.providerName}", style = MaterialTheme.typography.bodySmall, color = TextGray)
                    Text(vm.servicePrice, style = MaterialTheme.typography.labelLarge, color = OceanBlue)
                }
            }

            // Input bintang + komentar
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Bagaimana pengalamanmu?", style = MaterialTheme.typography.titleSmall)
                    Row {
                        for (i in 1..5) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "$i bintang",
                                tint = if (i <= rating) SandYellow else StarEmpty,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .clickable(enabled = !isSending) { rating = i }
                                    .padding(4.dp),
                            )
                        }
                    }
                    if (rating > 0) {
                        Text(
                            ratingLabel(rating),
                            style = MaterialTheme.typography.labelLarge,
                            color = OceanBlue,
                        )
                    }
                    if (ratingError) {
                        Text(
                            "Pilih rating bintang dulu ya",
                            style = MaterialTheme.typography.labelSmall,
                            color = DangerRed,
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { if (it.length <= 300) comment = it },
                        enabled = !isSending,
                        isError = commentError,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Komentar") },
                        placeholder = { Text("Ceritakan pengalamanmu memakai jasa ini...") },
                        minLines = 5,
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp),
                        supportingText = {
                            Row(Modifier.fillMaxWidth()) {
                                Text(
                                    if (commentError) "Komentar minimal 10 karakter" else "Minimal 10 karakter",
                                    color = if (commentError) DangerRed else TextGray,
                                    modifier = Modifier.weight(1f),
                                )
                                Text("${comment.length}/300", color = TextGray)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OceanBlue,
                            unfocusedBorderColor = BorderGray,
                            errorBorderColor = DangerRed,
                        ),
                    )
                }
            }
        }
    }
}