package com.example.genksuruh.feature.promo

import android.util.Log
import com.example.genksuruh.core.network.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage // BARU
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID // BARU

private const val TAG = "PromoRepo"

@Serializable
private data class PromoRow(
    val id: String,
    val title: String,
    val code: String,
    val category: String = "",
    val emoji: String = "🎟️",
    @SerialName("discount_type") val discountType: String,
    @SerialName("discount_value") val discountValue: Int,
    @SerialName("max_discount") val maxDiscount: Int = 0,
    @SerialName("min_order") val minOrder: Int = 0,
    @SerialName("service_id") val serviceId: String? = null,
    @SerialName("start_date") val startDate: String,
    @SerialName("end_date") val endDate: String,
    val quota: Int = 0,
    @SerialName("claimed_count") val claimedCount: Int = 0,
    val description: String? = null,
    val terms: List<String>? = null,
    @SerialName("banner_url") val bannerUrl: String? = null, // BARU
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
private data class ClaimRow(
    val id: String,
    @SerialName("promo_id") val promoId: String,
    @SerialName("user_id") val userId: String,
    val status: String = "",
    @SerialName("claimed_at") val claimedAt: String? = null,
    @SerialName("used_at") val usedAt: String? = null
)

private fun JsonObject.str(vararg keys: String): String? =
    keys.firstNotNullOfOrNull { k -> this[k]?.jsonPrimitive?.contentOrNull }

private fun JsonObject.personName(): String? =
    str("full_name", "name", "display_name", "username")

private fun isPercent(raw: String): Boolean =
    raw.uppercase().let { it.startsWith("PERS") || it.startsWith("PERC") || it == "%" }

private fun nowIso(): String =
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        .apply { timeZone = TimeZone.getTimeZone("UTC") }
        .format(Date())

/** myName = nama tampilan user yang login. Identitas klaim selalu memakai UUID dari sesi auth. */
class SupabasePromoRepository(private val myName: String) : PromoRepository {

    private val db get() = SupabaseClient.client.postgrest

    private val _promos = MutableStateFlow<List<Promo>>(emptyList())
    private val _claims = MutableStateFlow<List<VoucherClaim>>(emptyList())

    // Setiap layar mulai membaca, data dimuat ulang (aman setelah login ulang / ganti akun).
    override val promos: Flow<List<Promo>> = _promos.asStateFlow().onStart { refresh() }
    override val claims: Flow<List<VoucherClaim>> = _claims.asStateFlow()

    private var lastDeleted: Promo? = null
    private var serviceRows: List<JsonObject> = emptyList()
    private var profileNames: Map<String, String> = emptyMap()

    // Menyesuaikan penulisan nilai dengan data yang sudah ada di database
    private var persenToken = "PERSEN"
    private var nominalToken = "NOMINAL"
    private var aktifToken = "AKTIF"
    private var terpakaiToken = "TERPAKAI"

    private fun myId(): String? = SupabaseClient.client.auth.currentUserOrNull()?.id

    private suspend fun refresh() {
        try {
            val me = myId()

            profileNames = runCatching {
                db.from("profiles").select().decodeList<JsonObject>()
                    .mapNotNull { p -> p.str("id")?.let { it to (p.personName() ?: "Pengguna") } }
                    .toMap()
            }.getOrDefault(emptyMap())

            serviceRows = runCatching {
                db.from("services").select().decodeList<JsonObject>()
            }.getOrDefault(emptyList())

            val rows = db.from("promos").select().decodeList<PromoRow>()
            rows.firstOrNull { isPercent(it.discountType) }?.let { persenToken = it.discountType }
            rows.firstOrNull { !isPercent(it.discountType) }?.let { nominalToken = it.discountType }
            _promos.value = rows.map { it.toPromo() }

            val claimRows = db.from("voucher_claims").select().decodeList<ClaimRow>()
            claimRows.firstOrNull { !it.status.uppercase().startsWith("TERPAKAI") && it.status.isNotBlank() }
                ?.let { aktifToken = it.status }
            claimRows.firstOrNull { it.status.uppercase().startsWith("TERPAKAI") }
                ?.let { terpakaiToken = it.status }

            _claims.value = claimRows.map { c ->
                VoucherClaim(
                    id = c.id,
                    promoId = c.promoId,
                    userName = if (c.userId == me) myName else profileNames[c.userId] ?: "Pengguna",
                    status = if (c.status.uppercase().startsWith("TERPAKAI")) {
                        VoucherStatus.TERPAKAI
                    } else {
                        VoucherStatus.AKTIF
                    },
                    claimedAt = c.claimedAt?.take(10).orEmpty(),
                    usedAt = c.usedAt?.take(10)
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "refresh gagal: ${e.message}", e)
        }
    }

    private fun PromoRow.toPromo(): Promo {
        val service = serviceRows.firstOrNull { it.str("id") == serviceId }
        val providerName = service?.str("provider_name")
            ?: service?.str("provider_id")?.let { profileNames[it] }
            ?: ""
        return Promo(
            id = id,
            title = title,
            code = code,
            category = category,
            emoji = emoji,
            discountType = if (isPercent(discountType)) DiscountType.PERSEN else DiscountType.NOMINAL,
            discountValue = discountValue,
            maxDiscount = maxDiscount,
            minOrder = minOrder,
            serviceName = service?.str("title", "name").orEmpty(),
            providerName = providerName,
            startDate = startDate,
            endDate = endDate,
            quota = quota,
            claimed = claimedCount,
            description = description.orEmpty(),
            terms = terms ?: emptyList(),
            bannerUrl = bannerUrl // BARU
        )
    }

    private fun promoBody(promo: Promo) = buildJsonObject {
        val serviceId = serviceRows
            .firstOrNull { it.str("title", "name") == promo.serviceName }
            ?.str("id")
        put("title", promo.title)
        put("code", promo.code)
        put("category", promo.category)
        put("emoji", promo.emoji)
        put(
            "discount_type",
            if (promo.discountType == DiscountType.PERSEN) persenToken else nominalToken
        )
        put("discount_value", promo.discountValue)
        put("max_discount", promo.maxDiscount)
        put("min_order", promo.minOrder)
        if (serviceId != null) put("service_id", serviceId) else put("service_id", JsonNull)
        put("start_date", promo.startDate)
        put("end_date", promo.endDate)
        put("quota", promo.quota)
        put("description", promo.description)
        put("terms", JsonArray(promo.terms.map { JsonPrimitive(it) }))
        put("banner_url", promo.bannerUrl) // BARU
    }

    override suspend fun getPromoById(id: String): Promo? = _promos.value.find { it.id == id }

    override suspend fun isCodeTaken(code: String, excludePromoId: String?): Boolean =
        _promos.value.any { it.code.equals(code, true) && it.id != excludePromoId }

    // BARU: unggah foto ke bucket promo-banners, hasilnya alamat (URL) foto
    override suspend fun uploadBanner(bytes: ByteArray, mimeType: String): Result<String> = runCatching {
        val ext = when (mimeType) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
        }
        val path = "${myId() ?: "anon"}/${UUID.randomUUID()}.$ext"
        val bucket = SupabaseClient.client.storage.from("promo-banners")
        bucket.upload(path, bytes)
        bucket.publicUrl(path)
    }.onFailure { Log.e(TAG, "uploadBanner gagal: ${it.message}", it) }

    override suspend fun savePromo(promo: Promo): Result<Unit> = runCatching {
        val exists = _promos.value.any { it.id == promo.id }
        if (exists) {
            db.from("promos").update(promoBody(promo)) { filter { eq("id", promo.id) } }
        } else {
            db.from("promos").insert(promoBody(promo)) // id dibuat otomatis oleh database
        }
        refresh()
    }.onFailure { Log.e(TAG, "savePromo gagal: ${it.message}", it) }

    override suspend fun deletePromo(id: String): Result<Unit> = runCatching {
        lastDeleted = _promos.value.find { it.id == id }
        db.from("promos").delete { filter { eq("id", id) } }
        refresh()
    }.onFailure { Log.e(TAG, "deletePromo gagal: ${it.message}", it) }

    /** Mengembalikan promo (tanpa klaim lama). Database membuat id baru. */
    override suspend fun undoDelete(): Result<Unit> = runCatching {
        val promo = lastDeleted ?: error("Tidak ada promo untuk dikembalikan")
        db.from("promos").insert(promoBody(promo))
        lastDeleted = null
        refresh()
    }.onFailure { Log.e(TAG, "undoDelete gagal: ${it.message}", it) }

    override suspend fun claimPromo(promoId: String, userName: String): Result<Promo> = runCatching {
        val uid = myId() ?: error("Silakan login dulu")
        db.from("voucher_claims").insert(
            buildJsonObject {
                put("promo_id", promoId)
                put("user_id", uid)
                put("status", aktifToken)
            }
        )
        refresh() // claimed_count dinaikkan trigger on_voucher_claimed_changed
        _promos.value.first { it.id == promoId }
    }.recoverCatching { e ->
        Log.e(TAG, "claimPromo gagal: ${e.message}", e)
        val msg = e.message.orEmpty()
        throw when {
            msg.contains("duplicate", true) || msg.contains("unique", true) ->
                IllegalStateException("Kamu sudah mengklaim voucher ini sebelumnya")
            msg.contains("row-level", true) ->
                IllegalStateException("Tidak punya izin mengklaim (cek policy database)")
            msg.contains("quota", true) || msg.contains("kuota", true) ->
                IllegalStateException("Kuota promo sudah habis")
            else -> e
        }
    }

    override suspend fun markClaimUsed(claimId: String): Result<Unit> = runCatching {
        db.from("voucher_claims").update(
            buildJsonObject {
                put("status", terpakaiToken)
                put("used_at", nowIso())
            }
        ) { filter { eq("id", claimId) } }
        refresh()
    }.onFailure { Log.e(TAG, "markClaimUsed gagal: ${it.message}", it) }
}