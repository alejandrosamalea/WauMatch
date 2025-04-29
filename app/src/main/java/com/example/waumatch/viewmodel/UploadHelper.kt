package com.example.waumatch.viewmodel

import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.UploadCallback

fun uploadImage(uri: Uri, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
    MediaManager.get().upload(uri)
        .option("resource_type", "image")
        .callback(object : UploadCallback {
            override fun onStart(requestId: String) {}
            override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
            override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                val url = resultData["secure_url"] as String
                onSuccess(url) // Devolvemos la URL de la imagen subida
            }
            override fun onError(requestId: String, error: com.cloudinary.android.callback.ErrorInfo) {
                onError(error.description)
            }
            override fun onReschedule(requestId: String, error: com.cloudinary.android.callback.ErrorInfo) {}
        })
        .dispatch()
}