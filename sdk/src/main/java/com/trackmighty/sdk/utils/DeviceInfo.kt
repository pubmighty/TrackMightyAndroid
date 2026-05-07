package com.trackmighty.sdk.utils

import android.content.Context
import android.os.Build

/**
 * Collects device metadata needed for tracking and attribution.
 */
internal object DeviceInfo {

    /**
     * Returns the 2-letter ISO country code from the device locale in lowercase.
     * e.g. "in", "us", "gb"
     */
    fun getCountry(context: Context): String {
        val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            @Suppress("DEPRECATION")
            context.resources.configuration.locale
        }
        return locale.country.lowercase().ifEmpty { "us" }
    }

    /**
     * Attempts to read the Google Advertising ID off the main thread.
     * Returns null if the user opted out or play-services is unavailable.
     * Uses reflection so play-services-ads-identifier remains compileOnly.
     */
    fun getGaid(context: Context): String? {
        return try {
            val clientClass = Class.forName(
                "com.google.android.gms.ads.identifier.AdvertisingIdClient"
            )
            val info = clientClass.getMethod("getAdvertisingIdInfo", Context::class.java)
                .invoke(null, context)
            val isLat = info!!.javaClass
                .getMethod("isLimitAdTrackingEnabled")
                .invoke(info) as Boolean
            if (isLat) null
            else info.javaClass.getMethod("getId").invoke(info) as? String
        } catch (e: Exception) {
            null
        }
    }
}
