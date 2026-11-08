package com.example.genksuruh.feature.catalog

import com.example.genksuruh.core.model.Service

val CatalogCategories = listOf(
    "Semua", "Antar Makanan", "Beli Jajan", "Temani Acara", "Bantu Kos", "Print & Copy"
)

object DummyServices {
    val list = listOf(
        Service(
            id = "svc-1",
            title = "Antar Makanan Kantin FILKOM",
            category = "Antar Makanan",
            price = 8000,
            description = "Lagi sibuk kuliah atau kerja kelompok? Aku antarkan makanan dari kantin FILKOM ke lokasimu di area kampus. Maksimal 3 menu per pesanan.",
            areas = listOf("Area FILKOM", "Perpustakaan Pusat", "Asrama Putra"),
            hours = "Senin - Jumat, 08.00 - 16.00",
            providerId = "usr-dimas",
            providerName = "Dimas Pratama",
            rating = 4.8,
            reviewCount = 124,
            imageRes = null, // nanti: R.drawable.img_antar_makanan
            isFavorite = true
        ),
        Service(
            id = "svc-2",
            title = "Titip Beli Jajan Indomaret",
            category = "Beli Jajan",
            price = 5000,
            description = "Mau jajan tapi malas keluar? Titip saja, aku belikan di Indomaret terdekat.",
            areas = listOf("Area FILKOM", "Asrama Putri"),
            hours = "Setiap hari, 09.00 - 21.00",
            providerId = "usr-nadia",
            providerName = "Nadia Putri",
            rating = 4.9,
            reviewCount = 89
        ),
        Service(
            id = "svc-3",
            title = "Temani Nonton Konser",
            category = "Temani Acara",
            price = 75000,
            description = "Nggak ada teman nonton konser? Aku temani dari berangkat sampai pulang.",
            areas = listOf("Dalam kota Malang"),
            hours = "Sesuai jadwal acara",
            providerId = "usr-rafi",
            providerName = "Rafi Ahmad",
            rating = 4.7,
            reviewCount = 31,
            isFavorite = true
        ),
        Service(
            id = "svc-4",
            title = "Ganti Gas Elpiji 3 Kg",
            category = "Bantu Kos",
            price = 15000,
            description = "Gas habis di kos? Aku belikan dan pasangkan sampai kompor menyala.",
            areas = listOf("Sekitar kampus"),
            hours = "Setiap hari, 08.00 - 20.00",
            providerId = "usr-bagas",
            providerName = "Bagas Saputra",
            rating = 4.6,
            reviewCount = 57
        ),
        Service(
            id = "svc-5",
            title = "Fotokopi & Jilid Skripsi",
            category = "Print & Copy",
            price = 12000,
            description = "Fotokopi, print, dan jilid skripsi, diantar ke lokasimu.",
            areas = listOf("Area FILKOM", "Perpustakaan Pusat"),
            hours = "Senin - Sabtu, 08.00 - 17.00",
            providerId = "usr-salsa",
            providerName = "Salsa Maharani",
            rating = 4.8,
            reviewCount = 76
        ),
        Service(
            id = "svc-6",
            title = "Antar Jemput Galon Air",
            category = "Bantu Kos",
            price = 10000,
            description = "Galon habis? Aku jemput di depot dan antar sampai depan kamar kos.",
            areas = listOf("Sekitar kampus"),
            hours = "Setiap hari, 08.00 - 20.00",
            providerId = "usr-bagas",
            providerName = "Bagas Saputra",
            rating = 4.5,
            reviewCount = 42
        )
    )
}