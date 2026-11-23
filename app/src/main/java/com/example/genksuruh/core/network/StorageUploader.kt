package com.example.genksuruh.core.network

import io.github.jan.supabase.storage.storage

object StorageUploader {
    suspend fun upload(
        path: String,
        bytes: ByteArray,
        overwrite: Boolean = true
    ): String {
        val bucket = SupabaseClient.client.storage.from("genksuruh-assets")
        bucket.upload(path, bytes) {
            upsert = overwrite
        }
        return bucket.publicUrl(path)
    }
}
