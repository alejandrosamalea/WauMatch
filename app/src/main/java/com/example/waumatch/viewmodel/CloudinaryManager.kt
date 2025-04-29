package com.example.waumatch.viewmodel

import android.content.Context
import com.cloudinary.android.MediaManager

object CloudinaryManager {
    private var initialized = false

    @JvmStatic
    fun init(context: Context) {
        if (initialized) return

        val config = hashMapOf(
            "cloud_name" to "dpze0nt5g",
            "api_key" to "915399522846556",
            "api_secret" to "PRqyJFcGFmTvH526bGikWCaPWCw"
        )
        MediaManager.init(context, config)
        initialized = true
    }
}