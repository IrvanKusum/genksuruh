package com.example.genksuruh.feature.promo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Semua yang ditampilkan layar Daftar Promo ada di sini. */
data class PromoListUiState(
    val isLoading: Boolean = true,
    val isProvider: Boolean = false,
    val userName: String = "",
    val providerName: String = "",
    val selectedTab: Int = 0,

    val banners: List<Promo> = emptyList(),
    val selectedCategory: String = "Semua",
    val availablePromos: List<PromoItem> = emptyList(),

    val voucherFilter: VoucherStatus? = null,
    val totalVouchers: Int = 0,
    val vouchers: List<VoucherItem> = emptyList(),

    val myPromos: List<Promo> = emptyList(),
    val providerClaims: List<ClaimItem> = emptyList(),
    val activePromoCount: Int = 0,
    val totalClaimCount: Int = 0,
    val usedClaimCount: Int = 0,

    val claimSuccessCode: String? = null,
    val voucherSheet: Promo? = null,
    val markTarget: ClaimItem? = null,
    val deleteTarget: Promo? = null
)

class PromoListViewModel(
    private val repository: PromoRepository,
    private val session: SessionRepository
) : ViewModel() {

    private data class LocalState(
        val selectedTab: Int = 0,
        val selectedCategory: String = "Semua",
        val voucherFilter: VoucherStatus? = null,
        val claimSuccessCode: String? = null,
        val voucherSheetPromoId: String? = null,
        val markClaimId: String? = null,
        val deletePromoId: String? = null
    )

    private val local = MutableStateFlow(LocalState())

    private val _events = Channel<PromoEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    val uiState: StateFlow<PromoListUiState> = combine(
        repository.promos,
        repository.claims,
        session.isProvider,
        local
    ) { promos, claims, isProvider, localState ->
        buildState(promos, claims, isProvider, localState)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PromoListUiState()
    )

    private fun buildState(
        promos: List<Promo>,
        claims: List<VoucherClaim>,
        isProvider: Boolean,
        ls: LocalState
    ): PromoListUiState {
        val user = session.currentUser
        val providerName = session.currentProvider
        val promoById = promos.associateBy { it.id }

        val available = promos
            .filter {
                !it.isExpired &&
                        (ls.selectedCategory == "Semua" || it.category == ls.selectedCategory)
            }
            .map { promo ->
                PromoItem(
                    promo = promo,
                    claimedByMe = claims.any { it.promoId == promo.id && it.userName == user }
                )
            }
        val banners = promos.filter { !it.isExpired && !it.isFull }.take(3)

        val allMyVouchers = claims
            .filter { it.userName == user }
            .mapNotNull { claim ->
                promoById[claim.promoId]?.let { promo ->
                    VoucherItem(claim, promo, claim.effectiveStatus(promo))
                }
            }
        val vouchers = allMyVouchers.filter {
            ls.voucherFilter == null || it.status == ls.voucherFilter
        }

        val myPromos = promos.filter { it.providerName == providerName }
        val myPromoIds = myPromos.map { it.id }.toSet()
        val providerClaims = claims
            .filter { it.promoId in myPromoIds }
            .reversed()
            .mapNotNull { claim ->
                promoById[claim.promoId]?.let { promo ->
                    ClaimItem(claim, promo.code, claim.effectiveStatus(promo))
                }
            }

        val markTarget = ls.markClaimId?.let { id ->
            claims.find { it.id == id }?.let { claim ->
                val promo = promoById[claim.promoId]
                ClaimItem(claim, promo?.code ?: "-", claim.effectiveStatus(promo))
            }
        }

        return PromoListUiState(
            isLoading = false,
            isProvider = isProvider,
            userName = user,
            providerName = providerName,
            selectedTab = ls.selectedTab,
            banners = banners,
            selectedCategory = ls.selectedCategory,
            availablePromos = available,
            voucherFilter = ls.voucherFilter,
            totalVouchers = allMyVouchers.size,
            vouchers = vouchers,
            myPromos = myPromos,
            providerClaims = providerClaims,
            activePromoCount = myPromos.count { !it.isExpired },
            totalClaimCount = myPromos.sumOf { it.claimed },
            usedClaimCount = providerClaims.count { it.status == VoucherStatus.TERPAKAI },
            claimSuccessCode = ls.claimSuccessCode,
            voucherSheet = ls.voucherSheetPromoId?.let { promoById[it] },
            markTarget = markTarget,
            deleteTarget = ls.deletePromoId?.let { promoById[it] }
        )
    }

    // ---------- Aksi dari tampilan ----------

    fun onProviderModeChange(enabled: Boolean) {
        session.setProviderMode(enabled)
        local.update { it.copy(selectedTab = 0) }
    }

    fun onTabSelected(tab: Int) {
        local.update { it.copy(selectedTab = tab) }
    }

    fun onCategorySelected(category: String) {
        local.update { it.copy(selectedCategory = category) }
    }

    fun onVoucherFilterSelected(status: VoucherStatus?) {
        local.update { it.copy(voucherFilter = status) }
    }

    fun onClaimClick(promoId: String) {
        viewModelScope.launch {
            repository.claimPromo(promoId, session.currentUser)
                .onSuccess { promo ->
                    local.update { it.copy(claimSuccessCode = promo.code) }
                }
                .onFailure { error ->
                    _events.trySend(PromoEvent(error.message ?: "Gagal mengklaim voucher"))
                }
        }
    }

    fun onClaimSuccessDismiss() {
        local.update { it.copy(claimSuccessCode = null) }
    }

    fun onSeeMyVouchers() {
        local.update { it.copy(claimSuccessCode = null, selectedTab = 1, voucherFilter = null) }
    }

    fun onUseVoucherClick(promoId: String) {
        local.update { it.copy(voucherSheetPromoId = promoId) }
    }

    fun onVoucherSheetDismiss() {
        local.update { it.copy(voucherSheetPromoId = null) }
    }

    fun onMarkUsedClick(claimId: String) {
        local.update { it.copy(markClaimId = claimId) }
    }

    fun onMarkUsedDismiss() {
        local.update { it.copy(markClaimId = null) }
    }

    fun onMarkUsedConfirm() {
        val claimId = local.value.markClaimId ?: return
        local.update { it.copy(markClaimId = null) }
        viewModelScope.launch {
            repository.markClaimUsed(claimId)
                .onSuccess { _events.trySend(PromoEvent("Voucher ditandai terpakai")) }
                .onFailure { error ->
                    _events.trySend(PromoEvent(error.message ?: "Gagal menandai voucher"))
                }
        }
    }

    fun onDeleteClick(promoId: String) {
        local.update { it.copy(deletePromoId = promoId) }
    }

    fun onDeleteDismiss() {
        local.update { it.copy(deletePromoId = null) }
    }

    fun onDeleteConfirm() {
        val promoId = local.value.deletePromoId ?: return
        local.update { it.copy(deletePromoId = null) }
        viewModelScope.launch {
            repository.deletePromo(promoId)
                .onSuccess { _events.trySend(PromoEvent("Promo berhasil dihapus", undoable = true)) }
                .onFailure { error ->
                    _events.trySend(PromoEvent(error.message ?: "Gagal menghapus promo"))
                }
        }
    }

    fun onUndoDelete() {
        viewModelScope.launch {
            repository.undoDelete()
                .onSuccess { _events.trySend(PromoEvent("Promo dikembalikan")) }
        }
    }
}
