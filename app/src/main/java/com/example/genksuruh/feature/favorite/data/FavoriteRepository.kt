package com.example.genksuruh.feature.favorite.data
/**
 * Sumber data DUMMY (di memori). Nanti diganti Supabase (tabel "favorites").
 */
class FavoriteRepository {

    private val allServices = listOf(
        FavoriteService(
            id = "1",
            title = "Antar Makanan Kantin FILKOM",
            providerName = "Rian Suruhan",
            providerInfo = "Rizky A. (Teknik Informatika '22)",
            category = "Antar Makanan",
            price = 5000,
            originalPrice = 6500,
            discountLabel = "Diskon 20%",
            rating = 4.8,
            reviewCount = 128,
            bannerLabel = "KANTIN FILKOM UB",
            area = "Area Kampus UB (Gedung A, F, & Lab)",
            estimate = "15 - 30 menit setelah dipesan",
            description = "Layanan titip beli dan antar makanan langsung dari seluruh tenant kantin FILKOM " +
                    "ke ruang kelas, gazebo, atau kos sekitar gerbang Veteran. Makanan diantar hangat, bersih, " +
                    "dan hemat ongkir untuk sesama mahasiswa!"
        ),
        FavoriteService(
            id = "2",
            title = "Beli Jajan Malam",
            providerName = "Dimas Satset",
            providerInfo = "Dimas S. (Teknik Elektro '23)",
            category = "Beli Jajan",
            price = 7000,
            rating = 4.9,
            reviewCount = 94,
            bannerLabel = "JAJAN MALAM",
            area = "Area Kos Sekitar Kampus UB",
            estimate = "20 - 40 menit setelah dipesan",
            description = "Lagi lapar tengah malam saat ngerjain tugas? Titip beli gorengan, camilan, " +
                    "minuman, dan jajan malam favoritmu. Diantar sampai depan pintu kos."
        ),
        FavoriteService(
            id = "3",
            title = "Temani Nonton Konser",
            providerName = "Salsa Partner",
            providerInfo = "Salsa M. (Manajemen '22)",
            category = "Nonton Konser",
            price = 50000,
            rating = 5.0,
            reviewCount = 42,
            bannerLabel = "TEMAN KONSER",
            area = "Malang Kota dan sekitarnya",
            estimate = "Sesuai jadwal acara",
            description = "Tidak ada teman untuk nonton konser? Aku temani dari antre masuk sampai acara " +
                    "selesai, aman dan seru. Tiket dibeli sendiri ya."
        ),
        FavoriteService(
            id = "4",
            title = "Bantu Ganti Gas Elpiji",
            providerName = "Budi Perkasa",
            providerInfo = "Budi P. (Teknik Mesin '21)",
            category = "Ganti Gas Elpiji",
            price = 10000,
            rating = 4.7,
            reviewCount = 19,
            bannerLabel = "GAS ELPIJI",
            area = "Area Kos Sekitar Kampus UB",
            estimate = "10 - 20 menit setelah dipesan",
            description = "Bantu pasang dan ganti tabung gas elpiji 3 kg di kosmu dengan aman, " +
                    "termasuk cek selang dan regulator."
        ),
        FavoriteService(
            id = "5",
            title = "Titip Fotokopi Tugas",
            providerName = "Ilham Mandiri",
            providerInfo = "Ilham R. (Sistem Informasi '23)",
            category = "Fotokopi",
            price = 3000,
            rating = 4.8,
            reviewCount = 61,
            bannerLabel = "FOTOKOPI & PRINT",
            area = "Area Kampus UB",
            estimate = "30 - 45 menit setelah dipesan",
            description = "Titip fotokopi dan print tugas, modul, atau jurnal. Hasil rapi dan langsung " +
                    "diantar ke lokasimu sebelum kelas dimulai."
        ),
        FavoriteService(
            id = "6",
            title = "Antar Laundry Kiloan",
            providerName = "Nadia Fresh",
            providerInfo = "Nadia K. (Akuntansi '22)",
            category = "Laundry",
            price = 8000,
            rating = 4.6,
            reviewCount = 33,
            bannerLabel = "LAUNDRY KILOAN",
            area = "Area Kos Sekitar Kampus UB",
            estimate = "Antar jemput 1 - 2 jam",
            description = "Layanan jemput cucian kotormu, antar ke laundry kiloan langganan, lalu antar kembali " +
                    "setelah selesai. Tinggal duduk manis."
        ),
        FavoriteService(
            id = "7",
            title = "Bantu Bersihkan Kamar Kos",
            providerName = "Aris Resik",
            providerInfo = "Aris W. (Teknik Pengairan '22)",
            category = "Bersih Kos",
            price = 25000,
            originalPrice = 30000,
            discountLabel = "Diskon Akhir Pekan",
            rating = 4.9,
            reviewCount = 57,
            bannerLabel = "BERSIH KOS",
            area = "Area Kos Dinoyo & Watumujur",
            estimate = "1 - 2 jam pengerjaan",
            description = "Bantu sapu, pel, bersihkan debu kamar kos, dan buang sampah. Cocok saat kamu lagi sibuk tugas besar atau ujian."
        ),
        FavoriteService(
            id = "8",
            title = "Antar Jemput Stasiun Kota Baru",
            providerName = "Bayu Ride",
            providerInfo = "Bayu P. (FIA '21)",
            category = "Antar Jemput",
            price = 15000,
            rating = 4.8,
            reviewCount = 83,
            bannerLabel = "ANTAR JEMPUT",
            area = "Stasiun Malang Kota Baru & UB",
            estimate = "20 - 30 menit perjalanan",
            description = "Jasa antar jemput mahasiswa ke stasiun kereta atau terminal. Tepat waktu, bawa helm bersih dan jas hujan."
        ),
        FavoriteService(
            id = "9",
            title = "Format Dokumen & Skripsi",
            providerName = "Dian Editor",
            providerInfo = "Dian T. (Sastra Inggris '20)",
            category = "Tugas Kuliah",
            price = 20000,
            rating = 5.0,
            reviewCount = 39,
            bannerLabel = "FORMAT TUGAS",
            area = "Online / File dikirim via WhatsApp",
            estimate = "1 hari pengerjaan",
            description = "Bantu perbaiki margin, daftar isi otomatis, penomoran halaman, dan daftar pustaka sesuai pedoman penulisan UB."
        )
    )

    // Awalnya sebagian masuk favorit, sisanya ada di katalog siap ditambahkan
    private val favoriteIds = mutableListOf("1", "2", "3", "5")

    fun getFavorites(): List<FavoriteService> =
        favoriteIds.mapNotNull { id -> allServices.find { it.id == id } }

    fun getAllCatalog(): List<FavoriteService> = allServices

    fun getAllCategories(): List<String> = allServices.map { it.category }.distinct()

    fun getById(id: String): FavoriteService? = allServices.find { it.id == id }

    fun isFavorite(id: String): Boolean = id in favoriteIds

    fun add(id: String): Boolean {
        if (id !in favoriteIds && allServices.any { it.id == id }) {
            favoriteIds.add(id)
            return true
        }
        return false
    }

    fun remove(id: String): Int {
        val index = favoriteIds.indexOf(id)
        if (index >= 0) favoriteIds.removeAt(index)
        return index
    }

    fun restore(id: String, index: Int) {
        if (id in favoriteIds) return
        favoriteIds.add(index.coerceIn(0, favoriteIds.size), id)
    }

    fun reset() {
        favoriteIds.clear()
        favoriteIds.addAll(listOf("1", "2", "3", "5"))
    }
}
