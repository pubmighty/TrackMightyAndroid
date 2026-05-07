package com.trackmighty.sdk

import android.content.Context
import android.util.Log
import com.trackmighty.sdk.SdkConstants.Endpoints.BASE_URL
import com.trackmighty.sdk.tracker.InstallTracker
import com.trackmighty.sdk.tracker.RetrofitClient
import com.trackmighty.sdk.utils.DeviceInfo
import com.trackmighty.sdk.utils.Storage

/**
 * MightyTracker SDK entry point.
 *
 * Call [init] once in your Application.onCreate() before any tracking calls.
 *
 * ```kotlin
 * class MyApp : Application() {
 *     override fun onCreate() {
 *         super.onCreate()
 *         MightyTracker.init(
 *             context       = this,
 *             trackingToken = "your_property_tracking_token",
 *             baseUrl       = "https://api.yourdomain.com/v1/sdk"
 *         )
 *     }
 * }
 * ```
 */
object MightyTracker {

    private const val TAG = "MightyTracker"

    internal var trackingToken: String = ""
        private set

    internal var deviceToken: String = ""
        private set

    internal var appContext: Context? = null
        private set

    private var initialized = false
    internal var debugEnabled = false
        private set

    /**
     * Initialize the SDK.
     *
     * @param context       Application or Activity context — we store applicationContext internally
     * @param trackingToken Your property's tracking_token from the MightyTracker dashboard
     * @param baseUrl       Your backend base URL e.g. https://api.yourdomain.com/v1/sdk
     * @param debug         Set true during development to see full HTTP logs in Logcat
     */
    @JvmStatic
    fun init(
        context: Context,
        trackingToken: String,
        debug: Boolean = false
    ) {
        appContext     = context.applicationContext
        this.trackingToken = trackingToken.trim()
        this.debugEnabled  = debug

        // Generate or restore the persistent device_token
        deviceToken = Storage.getOrCreateDeviceToken(context.applicationContext)

        // Build the Retrofit client
        RetrofitClient.init(BASE_URL, debug)

        initialized = true

        if (debug) Log.d(TAG, "SDK initialized — trackingToken=$trackingToken deviceToken=$deviceToken baseUrl=$BASE_URL")

        // Auto-fire install event in background
        // Safe to call on every open — server upserts device, only creates
        // Install once. This also updates last_seen_at on returning users.
        InstallTracker.recordInstall(context.applicationContext)

    }

    @JvmStatic
    fun isInitialized(): Boolean = initialized

    /**
     * Guard for all tracker methods.
     * Returns false and logs a warning if init() has not been called.
     */
    internal fun checkInitialized(caller: String): Boolean {
        if (!initialized) {
            Log.w(TAG, "$caller called before MightyTracker.init() — ignoring.")
            return false
        }
        return true
    }

    /**
     * Returns device locale country as 2-letter lowercase ISO code.
     * Used as a fallback when country is not passed explicitly by the developer.
     */
    internal fun getCountry(): String {
        return appContext?.let { DeviceInfo.getCountry(it) } ?: "us"
    }
}