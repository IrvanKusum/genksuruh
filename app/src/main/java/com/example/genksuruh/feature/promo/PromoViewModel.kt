package com.example.genksuruh.feature.promo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow

data class UiEvent(
    val message: String,
    val actionLabel: String? = null,
    val success: Boolean = false,
    val onAction: (() -> Unit)? = null
)

class PromoViewModel : ViewModel() {
    val promos = PromoRepository.promos
    val claims = PromoRepository.claims

    /** Sementara untuk tes. Nanti ditentukan dari akun yang login. */
    var isProvider by mutableStateOf(false)

    /** 0 = Promo Tersedia, 1 = Voucher Saya */
    var listTab by mutableIntStateOf(0)

    val events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 8)

    fun showMessage(msg: String, success: Boolean = false) {
        events.tryEmit(UiEvent(msg, success = success))
    }

    /** Return null kalau berhasil, atau pesan error untuk dialog gagal. */
    fun claim(promoId: String): String? =
        PromoRepository.claimPromo(promoId, PromoRepository.CURRENT_USER)

    fun markUsed(claimId: String) {
        PromoRepository.markUsed(claimId)
        showMessage("Voucher ditandai terpakai")
    }

    fun deletePromo(id: String) {
        val snapshot = PromoRepository.deletePromo(id) ?: return
        events.tryEmit(
            UiEvent("Promo berhasil dihapus", actionLabel = "Urungkan") {
                PromoRepository.restore(snapshot)
            }
        )
    }

    fun savePromo(promo: Promo, isNew: Boolean) {
        PromoRepository.upsertPromo(promo)
        showMessage(if (isNew) "Promo berhasil dibuat" else "Perubahan disimpan")
    }

    /** Voucher aktif otomatis Kedaluwarsa kalau promonya lewat tanggal berakhir. */
    fun effectiveStatus(claim: VoucherClaim, promo: Promo?): VoucherStatus =
        if (claim.status == VoucherStatus.AKTIF && promo?.isExpired == true)
            VoucherStatus.KEDALUWARSA else claim.status
}