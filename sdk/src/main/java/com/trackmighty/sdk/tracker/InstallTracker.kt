package com.trackmighty.sdk.tracker

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.trackmighty.sdk.MightyTracker
import com.trackmighty.sdk.SdkConstants.Values
import com.trackmighty.sdk.SdkConstants.Config
import com.trackmighty.sdk.models.InstallBody
import com.trackmighty.sdk.utils.DeviceInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.TimeZone
import kotlin.coroutines.resume

/**
 * Handles device registration and install tracking.
 *
 * Call [recordInstall] once from MightyTracker.init() — it is safe to call
 * on every app open. The server upserts the Device and only creates a new
 * Install record on the very first open.
 *
 * What this does:
 *  1. Collects device metadata (model, brand, os version, locale, timezone)
 *  2. Reads GAID if available
 *  3. Reads the Google Play Install Referrer (contains your click_id)
 *  4. Posts everything to POST /install
 *  5. Server runs attribution and responds with campaign/network info
 */
internal object InstallTracker {

    private const val TAG = "MightyTracker.Install"

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * Called automatically from MightyTracker.init().
     * Collects all device data and fires the install event in the background.
     */
    fun recordInstall(context: Context) {
        scope.launch {
            try {
                val appContext = context.applicationContext

                // Collect device metadata
                val osVersion        = Build.VERSION.RELEASE
                val deviceModel      = Build.MODEL.trim()
                val deviceBrand      = Build.MANUFACTURER.trim()
                val country          = DeviceInfo.getCountry(appContext)
                val language         = getLanguage(appContext)
                val timezone         = TimeZone.getDefault().id
                val appVersion       = getAppVersion(appContext)
                val screenResolution = getScreenResolution(appContext)

                // GAID — must be off main thread
                val gaid = withContext(Dispatchers.IO) {
                    DeviceInfo.getGaid(appContext)
                }

                // Android ID — fallback advertising ID
                val androidId = getAndroidId(appContext)

                // Install referrer — read from Google Play
                // Contains your original tracking URL with click_id
                val installReferrer = readInstallReferrer(appContext)

                if (MightyTracker.debugEnabled) {
                    Log.d(TAG, "Recording install — gaid=${gaid?.take(8)}... referrer=${installReferrer?.take(50)}...")
                }

                val response = RetrofitClient.getApi().recordInstall(
                    InstallBody(
                        trackingToken = MightyTracker.trackingToken,
                        deviceToken = MightyTracker.deviceToken,
                        gaid = gaid,
                        androidId = androidId,
                        os = Values.OS_ANDROID,
                        osVersion = osVersion,
                        deviceModel = deviceModel,
                        deviceBrand = deviceBrand,
                        country = country,
                        language = language,
                        timezone = timezone,
                        appVersion = appVersion,
                        sdkVersion = Config.SDK_VERSION_NAME,
                        installReferrer = installReferrer,
                        store = Values.STORE_PLAY,
                        screenResolution = screenResolution
                    )
                )

                if (MightyTracker.debugEnabled) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.isNewInstall == true) {
                            Log.d(TAG, "✓ New install recorded — attribution=${body.attribution?.method} campaign=${body.attribution?.campaignId}")
                        } else {
                            Log.d(TAG, "✓ Returning user — device updated")
                        }
                    } else {
                        Log.w(TAG, "✗ Install failed: ${response.code()} ${response.errorBody()?.string()}")
                    }
                }
            } catch (e: Exception) {
                // Silent fail — never crash the host app
                if (MightyTracker.debugEnabled) Log.e(TAG, "Install request failed: ${e.message}")
            }
        }
    }

    // -------------------------------------------------------------------------
    // Install Referrer — reads the Google Play Install Referrer
    // -------------------------------------------------------------------------

    /**
     * Reads the Google Play Install Referrer string asynchronously.
     *
     * The referrer contains the tracking URL the user clicked before installing,
     * which includes our click_id for exact attribution matching.
     *
     * Requires: implementation 'com.android.installreferrer:installreferrer:2.2'
     * in the host app's build.gradle.
     *
     * Returns null if the referrer is unavailable or the library is not present.
     */
    private suspend fun readInstallReferrer(context: Context): String? {
        return suspendCancellableCoroutine { continuation ->
            try {
                val referrerClient = InstallReferrerClient.newBuilder(context).build()

                referrerClient.startConnection(object : InstallReferrerStateListener {
                    override fun onInstallReferrerSetupFinished(responseCode: Int) {
                        try {
                            if (responseCode == InstallReferrerClient.InstallReferrerResponse.OK) {
                                val referrer = referrerClient.installReferrer
                                    .installReferrer
                                continuation.resume(referrer)
                            } else {
                                continuation.resume(null)
                            }
                        } catch (e: Exception) {
                            continuation.resume(null)
                        } finally {
                            try { referrerClient.endConnection() } catch (_: Exception) {}
                        }
                    }

                    override fun onInstallReferrerServiceDisconnected() {
                        if (continuation.isActive) continuation.resume(null)
                    }
                })

                continuation.invokeOnCancellation {
                    try { referrerClient.endConnection() } catch (_: Exception) {}
                }
            } catch (e: Exception) {
                // install-referrer library not in host app or other error
                continuation.resume(null)
            }
        }
    }

    // -------------------------------------------------------------------------
    // Device metadata helpers
    // -------------------------------------------------------------------------

    private fun getLanguage(context: Context): String {
        val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            @Suppress("DEPRECATION")
            context.resources.configuration.locale
        }
        // Returns e.g. "en-IN", "hi-IN"
        return "${locale.language}-${locale.country}"
    }

    private fun getAppVersion(context: Context): String? {
        return try {
            val pInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            pInfo.versionName
        } catch (e: Exception) {
            null
        }
    }

    @Suppress("DEPRECATION", "HardwareIds")
    private fun getAndroidId(context: Context): String? {
        return try {
            android.provider.Settings.Secure.getString(
                context.contentResolver,
                android.provider.Settings.Secure.ANDROID_ID
            )?.takeIf { it.isNotEmpty() && it != "9774d56d682e549c" } // exclude known bad value
        } catch (e: Exception) {
            null
        }
    }

    private fun getScreenResolution(context: Context): String {
        val metrics = context.resources.displayMetrics
        return "${metrics.widthPixels}x${metrics.heightPixels}"
    }
}