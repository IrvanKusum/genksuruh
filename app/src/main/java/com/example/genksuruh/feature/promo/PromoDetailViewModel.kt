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

enum class ClaimButtonState { CLAIMABLE, CLAIMED, EXPIRED, FULL }

data class PromoDetailUiState(
    val isLoading: Boolean = true,
    val promo: Promo? = null,
    val isDeleting: Boolean = false,
    val isOwner: Boolean = false,
    val claimButtonState: ClaimButtonState = ClaimButtonState.CLAIMABLE,
    val providerClaims: List<ClaimItem> = emptyList(),
    val showClaimSuccess: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val markTarget: ClaimItem? = null
)

sealed interface DetailEvent {
    data class Message(val text: String) : DetailEvent
    object Deleted : DetailEvent
}

class PromoDetailViewModel(
    private val promoId: String,
    private val repository: PromoRepository,
    private val session: SessionRepository
) : ViewModel() {

    private data class LocalState(
        val showClaimSuccess: Boolean = false,
        val showDeleteDialog: Boolean = false,
        val markClaimId: String? = null,
        val isDeleting: Boolean = false
    )

    private val local = MutableStateFlow(LocalState())

    private val _events = Channel<DetailEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    val uiState: StateFlow<PromoDetailUiState> = combine(
        repository.promos,
        repository.claims,
        session.isProvider,
        local
    ) { promos, claims, isProvider, localState ->
        buildState(promos, claims, isProvider, localState)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PromoDetailUiState()
    )

    private fun buildState(
        promos: List<Promo>,
        claims: List<VoucherClaim>,
        isProvider: Boolean,
        ls: LocalState
    ): PromoDetailUiState {
        val promo = promos.find { it.id == promoId }
        val isOwner = isProvider && promo != null && promo.providerName == session.currentProvider
        val myClaim = claims.find {
            it.promoId == promoId && it.userName == session.currentUser
        }

        val claimButtonState = when {
            myClaim != null -> ClaimButtonState.CLAIMED
            promo?.isExpired == true -> ClaimButtonState.EXPIRED
            promo?.isFull == true -> ClaimButtonState.FULL
            else -> ClaimButtonState.CLAIMABLE
        }

        val providerClaims = if (isOwner && promo != null) {
            claims
                .filter { it.promoId == promoId }
                .reversed()
                .map { ClaimItem(it, promo.code, it.effectiveStatus(promo)) }
        } else {
            emptyList()
        }

        val markTarget = ls.markClaimId?.let { id ->
            providerClaims.find { it.claim.id == id }
        }

        return PromoDetailUiState(
            isLoading = false,
            promo = promo,
            isDeleting = ls.isDeleting,
            isOwner = isOwner,
            claimButtonState = claimButtonState,
            providerClaims = providerClaims,
            showClaimSuccess = ls.showClaimSuccess,
            showDeleteDialog = ls.showDeleteDialog,
            markTarget = markTarget
        )
    }

    fun onClaimClick() {
        viewModelScope.launch {
            repository.claimPromo(promoId, session.currentUser)
                .onSuccess { local.update { s -> s.copy(showClaimSuccess = true) } }
                .onFailure { error ->
                    _events.trySend(DetailEvent.Message(error.message ?: "Gagal mengklaim voucher"))
                }
        }
    }

    fun onClaimSuccessDismiss() {
        local.update { it.copy(showClaimSuccess = false) }
    }

    fun onDeleteClick() {
        local.update { it.copy(showDeleteDialog = true) }
    }

    fun onDeleteDismiss() {
        local.update { it.copy(showDeleteDialog = false) }
    }

    fun onDeleteConfirm() {
        local.update { it.copy(showDeleteDialog = false, isDeleting = true) }
        viewModelScope.launch {
            repository.deletePromo(promoId)
                .onSuccess { _events.trySend(DetailEvent.Deleted) }
                .onFailure { error ->
                    local.update { s -> s.copy(isDeleting = false) }
                    _events.trySend(DetailEvent.Message(error.message ?: "Gagal menghapus promo"))
                }
        }
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
                .onSuccess { _events.trySend(DetailEvent.Message("Voucher ditandai terpakai")) }
                .onFailure { error ->
                    _events.trySend(DetailEvent.Message(error.message ?: "Gagal menandai voucher"))
                }
        }
    }
}
