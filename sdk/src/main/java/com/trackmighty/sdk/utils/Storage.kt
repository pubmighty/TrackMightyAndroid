package com.trackmighty.sdk.utils

import android.content.Context
import com.trackmighty.sdk.SdkConstants.Config
import java.util.UUID

/**
 * Manages persistent SDK state in SharedPreferences.
 * device_token is generated once on first app open and survives process kills.
 * It resets only on app uninstall.
 */
internal object Storage {

    fun getOrCreateDeviceToken(context: Context): String {
        val prefs = context.getSharedPreferences(Config.PREFS_NAME, Context.MODE_PRIVATE)
        var token = prefs.getString(Config.PREFS_KEY_DEVICE_TOKEN, null)
        if (token.isNullOrEmpty()) {
            token = UUID.randomUUID().toString()
            prefs.edit().putString(Config.PREFS_KEY_DEVICE_TOKEN, token).apply()
        }
        return token
    }
}