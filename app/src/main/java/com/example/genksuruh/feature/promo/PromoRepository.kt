package com.example.genksuruh.feature.promo

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Repository dummy (in-memory). Nanti isi fungsi-fungsinya diganti
 * dengan panggilan Supabase (tabel vouchers & voucher_claims).
 */
object PromoRepository {
    const val TODAY = "2026-11-01"          // "hari ini" sesuai desain
    const val CURRENT_USER = "Irvan"
    const val CURRENT_PROVIDER_ID = "dimas"
    const val CURRENT_PROVIDER_NAME = "Dimas Pratama"

    private val _promos = MutableStateFlow(seedPromos())
    val promos: StateFlow<List<Promo>> = _promos.asStateFlow()

    private val _claims = MutableStateFlow(seedClaims())
    val claims: StateFlow<List<VoucherClaim>> = _claims.asStateFlow()

    fun getPromo(id: String): Promo? = _promos.value.find { it.id == id }

    fun upsertPromo(promo: Promo) {
        _promos.update { list ->
            if (list.any { it.id == promo.id }) list.map { if (it.id == promo.id) promo else it }
            else list + promo
        }
    }

    fun deletePromo(id: String): DeletedSnapshot? {
        val promo = getPromo(id) ?: return null
        val related = _claims.value.filter { it.promoId == id }
        _promos.update { list -> list.filterNot { it.id == id } }
        _claims.update { list -> list.filterNot { it.promoId == id } }
        return DeletedSnapshot(promo, related)
    }

    fun restore(snapshot: DeletedSnapshot) {
        _promos.update { it + snapshot.promo }
        _claims.update { it + snapshot.claims }
    }

    /** Return null kalau sukses, atau pesan error. */
    fun claimPromo(promoId: String, user: String): String? {
        val promo = getPromo(promoId) ?: return "Promo tidak ditemukan."
        if (promo.isExpired) return "Promo sudah berakhir."
        if (promo.isFull) return "Kuota promo sudah habis."
        if (_claims.value.any { it.promoId == promoId && it.userName == user }) {
            return "Kamu sudah mengklaim voucher ini sebelumnya."
        }
        _claims.update {
            it + VoucherClaim("c${System.nanoTime()}", promoId, user, VoucherStatus.AKTIF, TODAY)
        }
        _promos.update { list ->
            list.map { if (it.id == promoId) it.copy(claimed = it.claimed + 1) else it }
        }
        return null
    }

    fun markUsed(claimId: String) {
        _claims.update { list ->
            list.map {
                if (it.id == claimId) it.copy(status = VoucherStatus.TERPAKAI, usedAt = TODAY) else it
            }
        }
    }
}

private fun seedPromos() = listOf(
    Promo(
        id = "p1", title = "Hemat 50% Antar Makanan Pertama", code = "GENKPERTAMA",
        category = "Antar Makanan", emoji = "🍱",
        discountType = DiscountType.PERSEN, discountValue = 50, maxDiscount = 5000, minOrder = 8000,
        serviceName = "Antar Makanan Kantin FILKOM",
        providerName = "Ryul Anggora", ownerId = "ryul",
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
        serviceName = "Titip Beli Jajan Indomaret",
        providerName = "Nada Daja", ownerId = "nada",
        startDate = "2026-11-01", endDate = "2026-11-15", quota = 50, claimed = 31,
        description = "Potongan biaya titip untuk belanja di Indomaret area kampus.",
        terms = defaultTerms
    ),
    Promo(
        id = "p3", title = "Diskon Rp 20.000 Temani Nonton Konser", code = "KONSER20",
        category = "Temani Acara", emoji = "🎤",
        discountType = DiscountType.NOMINAL, discountValue = 20000, minOrder = 75000,
        serviceName = "Temani Nonton Konser",
        providerName = "Frans Leonardo",
        ownerId = "frans", // dummy: sesuai desain, KONSER20 tampil di "Promo Saya" milik Dimas
        startDate = "2026-11-01", endDate = "2026-11-30", quota = 30, claimed = 12,
        description = "Ada teman buat nonton konser, hemat Rp 20.000.",
        terms = defaultTerms
    ),
    Promo(
        id = "p4", title = "Diskon 20% Ganti Gas Elpiji", code = "GASHEMAT20",
        category = "Bantu Rumah Tangga", emoji = "🔥",
        discountType = DiscountType.PERSEN, discountValue = 20,
        serviceName = "Ganti Gas Elpiji 3 Kg",
        providerName = "Louis Elliot", ownerId = "louis",
        startDate = "2026-11-01", endDate = "2026-11-20", quota = 40, claimed = 40,
        description = "Diskon jasa ganti gas elpiji 3 Kg.",
        terms = defaultTerms
    ),
    Promo(
        id = "p5", title = "Diskon 15% Fotokopi dan Jilid Skripsi", code = "SKRIPSI15",
        category = "Print dan Fotokopi", emoji = "🖨️",
        discountType = DiscountType.PERSEN, discountValue = 15,
        serviceName = "Fotokopi dan Jilid Skripsi",
        providerName = "Oppa Gajima", ownerId = "oppa",
        startDate = "2026-10-01", endDate = "2026-10-20", quota = 60, claimed = 48,
        description = "Diskon fotokopi dan jilid untuk mahasiswa tingkat akhir.",
        terms = defaultTerms
    )
)

// GENKPERTAMA sengaja belum diklaim Irvan supaya alur "Klaim" bisa dites.
// Setelah diklaim, daftar Voucher Saya sama persis dengan desain.
private fun seedClaims() = listOf(
    VoucherClaim("c1", "p3", "Irvan", VoucherStatus.AKTIF, "2026-10-30"),
    VoucherClaim("c2", "p2", "Irvan", VoucherStatus.TERPAKAI, "2026-10-25", "2026-10-28"),
    VoucherClaim("c3", "p5", "Irvan", VoucherStatus.AKTIF, "2026-10-10"),
    VoucherClaim("c4", "p1", "Alya Rahma", VoucherStatus.AKTIF, "2026-11-01"),
    VoucherClaim("c5", "p1", "Fajar Nugroho", VoucherStatus.AKTIF, "2026-11-01"),
    VoucherClaim("c6", "p1", "Putri Anjani", VoucherStatus.TERPAKAI, "2026-11-01", "2026-11-01")
)