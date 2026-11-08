package com.example.genksuruh.feature.promo

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Kontrak data. ViewModel hanya tahu interface ini. Nanti Supabase tinggal mengimplementasikannya. */
interface PromoRepository {
    val promos: Flow<List<Promo>>
    val claims: Flow<List<VoucherClaim>>

    suspend fun getPromoById(id: String): Promo?
    suspend fun isCodeTaken(code: String, excludePromoId: String?): Boolean
    suspend fun savePromo(promo: Promo): Result<Unit>
    suspend fun deletePromo(id: String): Result<Unit>
    suspend fun undoDelete(): Result<Unit>
    suspend fun claimPromo(promoId: String, userName: String): Result<Promo>
    suspend fun markClaimUsed(claimId: String): Result<Unit>
}

/** Data dummy (disimpan di memori). */
class FakePromoRepository : PromoRepository {

    private val _promos = MutableStateFlow(seedPromos())
    private val _claims = MutableStateFlow(seedClaims())
    private var lastDeleted: DeletedSnapshot? = null

    override val promos: Flow<List<Promo>> = _promos.asStateFlow()
    override val claims: Flow<List<VoucherClaim>> = _claims.asStateFlow()

    override suspend fun getPromoById(id: String): Promo? =
        _promos.value.find { it.id == id }

    override suspend fun isCodeTaken(code: String, excludePromoId: String?): Boolean =
        _promos.value.any { it.code.equals(code, ignoreCase = true) && it.id != excludePromoId }

    override suspend fun savePromo(promo: Promo): Result<Unit> {
        _promos.update { list ->
            if (list.any { it.id == promo.id }) {
                list.map { if (it.id == promo.id) promo else it }
            } else {
                list + promo
            }
        }
        return Result.success(Unit)
    }

    override suspend fun deletePromo(id: String): Result<Unit> {
        val promo = _promos.value.find { it.id == id }
            ?: return Result.failure(NoSuchElementException("Promo tidak ditemukan"))
        lastDeleted = DeletedSnapshot(promo, _claims.value.filter { it.promoId == id })
        _promos.update { list -> list.filterNot { it.id == id } }
        _claims.update { list -> list.filterNot { it.promoId == id } }
        return Result.success(Unit)
    }

    override suspend fun undoDelete(): Result<Unit> {
        val snapshot = lastDeleted
            ?: return Result.failure(IllegalStateException("Tidak ada promo untuk dikembalikan"))
        _promos.update { list -> list + snapshot.promo }
        _claims.update { list -> list + snapshot.claims }
        lastDeleted = null
        return Result.success(Unit)
    }

    override suspend fun claimPromo(promoId: String, userName: String): Result<Promo> {
        val promo = _promos.value.find { it.id == promoId }
            ?: return Result.failure(NoSuchElementException("Promo tidak ditemukan"))
        if (promo.isExpired) {
            return Result.failure(IllegalStateException("Promo sudah berakhir"))
        }
        if (promo.isFull) {
            return Result.failure(IllegalStateException("Kuota promo sudah habis"))
        }
        if (_claims.value.any { it.promoId == promoId && it.userName == userName }) {
            return Result.failure(IllegalStateException("Kamu sudah mengklaim voucher ini sebelumnya"))
        }
        val updated = promo.copy(claimed = promo.claimed + 1)
        _claims.update { list ->
            list + VoucherClaim(
                id = "c${System.nanoTime()}",
                promoId = promoId,
                userName = userName,
                status = VoucherStatus.AKTIF,
                claimedAt = AppClock.TODAY
            )
        }
        _promos.update { list -> list.map { if (it.id == promoId) updated else it } }
        return Result.success(updated)
    }

    override suspend fun markClaimUsed(claimId: String): Result<Unit> {
        if (_claims.value.none { it.id == claimId }) {
            return Result.failure(NoSuchElementException("Klaim tidak ditemukan"))
        }
        _claims.update { list ->
            list.map {
                if (it.id == claimId) {
                    it.copy(status = VoucherStatus.TERPAKAI, usedAt = AppClock.TODAY)
                } else {
                    it
                }
            }
        }
        return Result.success(Unit)
    }
}

private fun seedPromos() = listOf(
    Promo(
        id = "p1", title = "Hemat 50% Antar Makanan Pertama", code = "GENKPERTAMA",
        category = "Antar Makanan", emoji = "🍱",
        discountType = DiscountType.PERSEN, discountValue = 50, maxDiscount = 5000, minOrder = 8000,
        serviceName = "Antar Makanan Kantin FILKOM", providerName = "Dimas Pratama",
        startDate = "2026-11-01", endDate = "2026-11-30", quota = 100, claimed = 76,
        description = "Khusus pesanan antar makanan pertama kamu di GenkSuruh.co. Hemat sampai Rp 5.000!",
        terms = listOf(
            "Satu voucher untuk satu pengguna.",
            "Hanya untuk pesanan pertama.",
            "Tidak bisa digabung dengan promo lain."
        )
    ),
    Promo(
        id = "p2", title = "Gratis Biaya Titip Jajan", code = "JAJANGRATIS",
        category = "Beli Jajan", emoji = "🛒",
        discountType = DiscountType.NOMINAL, discountValue = 5000,
        serviceName = "Titip Beli Jajan Indomaret", providerName = "Nadia Putri",
        startDate = "2026-11-01", endDate = "2026-11-15", quota = 50, claimed = 31,
        description = "Potongan biaya titip untuk belanja di Indomaret area kampus.",
        terms = defaultTerms
    ),
    Promo(
        id = "p3", title = "Diskon Rp 20.000 Temani Nonton Konser", code = "KONSER20",
        category = "Temani Acara", emoji = "🎤",
        discountType = DiscountType.NOMINAL, discountValue = 20000, minOrder = 75000,
        serviceName = "Temani Nonton Konser", providerName = "Rafi Ahmad",
        startDate = "2026-11-01", endDate = "2026-11-30", quota = 30, claimed = 12,
        description = "Ada teman buat nonton konser, hemat Rp 20.000.",
        terms = defaultTerms
    ),
    Promo(
        id = "p4", title = "Diskon 20% Ganti Gas Elpiji", code = "GASHEMAT20",
        category = "Bantu Rumah Tangga", emoji = "🔥",
        discountType = DiscountType.PERSEN, discountValue = 20,
        serviceName = "Ganti Gas Elpiji 3 Kg", providerName = "Bagas Saputra",
        startDate = "2026-11-01", endDate = "2026-11-20", quota = 40, claimed = 40,
        description = "Diskon jasa ganti gas elpiji 3 Kg.",
        terms = defaultTerms
    ),
    Promo(
        id = "p5", title = "Diskon 15% Fotokopi dan Jilid Skripsi", code = "SKRIPSI15",
        category = "Print dan Fotokopi", emoji = "🖨️",
        discountType = DiscountType.PERSEN, discountValue = 15,
        serviceName = "Fotokopi dan Jilid Skripsi", providerName = "Salsa Maharani",
        startDate = "2026-10-01", endDate = "2026-10-20", quota = 60, claimed = 48,
        description = "Diskon fotokopi dan jilid untuk mahasiswa tingkat akhir.",
        terms = defaultTerms
    )
)

private fun seedClaims() = listOf(
    VoucherClaim("c1", "p3", "Irvan", VoucherStatus.AKTIF, "2026-10-30"),
    VoucherClaim("c2", "p2", "Irvan", VoucherStatus.TERPAKAI, "2026-10-25", "2026-10-28"),
    VoucherClaim("c3", "p5", "Irvan", VoucherStatus.AKTIF, "2026-10-10"),
    VoucherClaim("c4", "p1", "Alya Rahma", VoucherStatus.AKTIF, "2026-11-01"),
    VoucherClaim("c5", "p1", "Fajar Nugroho", VoucherStatus.AKTIF, "2026-11-01"),
    VoucherClaim("c6", "p1", "Putri Anjani", VoucherStatus.TERPAKAI, "2026-11-01", "2026-11-01")
)

/** Sesi pengguna dummy. Nanti diganti data login. */
class SessionRepository {
    val currentUser: String = "Irvan"
    val currentProvider: String = "Dimas Pratama"

    private val _isProvider = MutableStateFlow(false)
    val isProvider: StateFlow<Boolean> = _isProvider.asStateFlow()

    fun setProviderMode(enabled: Boolean) {
        _isProvider.value = enabled
    }
}

/** Tempat membuat ViewModel beserta dependency-nya. */
object PromoModule {
    val repository: PromoRepository by lazy { FakePromoRepository() }
    val session: SessionRepository by lazy { SessionRepository() }

    val listFactory: ViewModelProvider.Factory = viewModelFactory {
        initializer { PromoListViewModel(repository, session) }
    }

    fun detailFactory(promoId: String): ViewModelProvider.Factory = viewModelFactory {
        initializer { PromoDetailViewModel(promoId, repository, session) }
    }

    fun formFactory(promoId: String): ViewModelProvider.Factory = viewModelFactory {
        initializer { PromoFormViewModel(promoId, repository, session) }
    }
}