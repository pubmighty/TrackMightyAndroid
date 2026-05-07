package com.trackmighty.sdk.tracker

import android.util.Log
import com.trackmighty.sdk.MightyTracker
import com.trackmighty.sdk.SdkConstants
import com.trackmighty.sdk.SdkConstants.Values
import com.trackmighty.sdk.models.AdClickBody
import com.trackmighty.sdk.models.AdImpressionBody
import com.trackmighty.sdk.models.AdMetricsBody
import com.trackmighty.sdk.models.AdRequestBody
import com.trackmighty.sdk.models.AdRevenueBody
import com.trackmighty.sdk.models.ApiResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Tracks ad monetisation events (requests, impressions, clicks, revenue).
 *
 * All methods are fire-and-forget — they launch on a background coroutine
 * and never block the calling thread. Failures are caught silently.
 *
 * Server endpoints:
 *   POST /ads/tracking/request
 *   POST /ads/tracking/impression
 *   POST /ads/tracking/click
 *   POST /ads/tracking/revenue
 *   POST /ads/tracking/metrics
 */
object AdTracker {

    private const val TAG = "TrackMighty.AdTracker"

    // Dedicated coroutine scope — SupervisorJob so one failure doesn't cancel others
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // -------------------------------------------------------------------------
    // Ad Request
    // -------------------------------------------------------------------------

    /**
     * Call when your app makes an ad request to the network.
     *
     * @param adFormat  One of: banner, interstitial, native, rewarded, app_open
     *                  Use SdkConstants.Values.FORMAT_* constants.
     * @param platform  Ad network e.g. "admob", "applovin", "ironsource", "unity"
     * @param country   2-letter ISO country code e.g. "in", "us". Auto-detected if null.
     * @param requests  Number of ad requests (default 1)
     * @param date      Optional YYYY-MM-DD to override the server's IST today default.
     *                  Use this when batching events from a previous day.
     */
    @JvmStatic
    @JvmOverloads
    fun recordRequest(
        adFormat: String,
        platform: String,
        country: String?  = null,
        requests: Int     = 1,
        date: String?     = null
    ) {
        if (!MightyTracker.checkInitialized("AdTracker.recordRequest")) return

        fire {
            RetrofitClient.getApi().recordRequest(
                AdRequestBody(
                    trackingToken = MightyTracker.trackingToken,
                    country       = country?.lowercase() ?: MightyTracker.getCountry(),
                    os            = Values.OS_ANDROID,
                    adFormat      = adFormat.lowercase(),
                    platform      = platform.lowercase(),
                    requests      = requests,
                    date          = date
                )
            )
        }
    }

    // -------------------------------------------------------------------------
    // Ad Impression
    // -------------------------------------------------------------------------

    /**
     * Call when an ad is shown to the user.
     *
     * @param adFormat    One of: banner, interstitial, native, rewarded, app_open
     * @param platform    Ad network e.g. "admob", "applovin"
     * @param country     2-letter ISO code. Auto-detected if null.
     * @param impressions Number of impressions (default 1)
     * @param date        Optional YYYY-MM-DD override.
     */
    @JvmStatic
    @JvmOverloads
    fun recordImpression(
        adFormat: String,
        platform: String,
        country: String?     = null,
        impressions: Int     = 1,
        date: String?        = null
    ) {
        if (!MightyTracker.checkInitialized("AdTracker.recordImpression")) return

        fire {
            RetrofitClient.getApi().recordImpression(
                AdImpressionBody(
                    trackingToken = MightyTracker.trackingToken,
                    country       = country?.lowercase() ?: MightyTracker.getCountry(),
                    os            = Values.OS_ANDROID,
                    adFormat      = adFormat.lowercase(),
                    platform      = platform.lowercase(),
                    impressions   = impressions,
                    date          = date
                )
            )
        }
    }

    // -------------------------------------------------------------------------
    // Ad Click
    // -------------------------------------------------------------------------

    /**
     * Call when a user taps an ad.
     *
     * @param adFormat One of: banner, interstitial, native, rewarded, app_open
     * @param platform Ad network e.g. "admob", "applovin"
     * @param country  2-letter ISO code. Auto-detected if null.
     * @param clicks   Number of clicks (default 1)
     * @param date     Optional YYYY-MM-DD override.
     */
    @JvmStatic
    @JvmOverloads
    fun recordClick(
        adFormat: String,
        platform: String,
        country: String? = null,
        clicks: Int      = 1,
        date: String?    = null
    ) {
        if (!MightyTracker.checkInitialized("AdTracker.recordClick")) return

        fire {
            RetrofitClient.getApi().recordClick(
                AdClickBody(
                    trackingToken = MightyTracker.trackingToken,
                    country       = country?.lowercase() ?: MightyTracker.getCountry(),
                    os            = Values.OS_ANDROID,
                    adFormat      = adFormat.lowercase(),
                    platform      = platform.lowercase(),
                    clicks        = clicks,
                    date          = date
                )
            )
        }
    }

    // -------------------------------------------------------------------------
    // Ad Revenue
    // -------------------------------------------------------------------------

    /**
     * Call when a paid impression fires (e.g. AdMob onPaidEvent).
     * Revenue is automatically converted to USD on the server.
     *
     * AdMob example:
     * ```kotlin
     * rewardedAd.setOnPaidEventListener { adValue ->
     *     AdTracker.recordRevenue(
     *         adFormat = SdkConstants.Values.FORMAT_REWARDED,
     *         platform = "admob",
     *         revenue  = adValue.valueMicros / 1_000_000.0,
     *         currency = adValue.currencyCode
     *     )
     * }
     * ```
     *
     * @param adFormat  One of: banner, interstitial, native, rewarded, app_open
     * @param platform  Ad network e.g. "admob", "applovin"
     * @param revenue   Revenue amount in [currency]
     * @param currency  3-letter ISO 4217 code e.g. "USD", "INR" (default USD)
     * @param country   2-letter ISO code. Auto-detected if null.
     * @param date      Optional YYYY-MM-DD override.
     */
    @JvmStatic
    @JvmOverloads
    fun recordRevenue(
        adFormat: String,
        platform: String,
        revenue: Double,
        currency: String = SdkConstants.Config.DEFAULT_CURRENCY,
        country: String? = null,
        date: String?    = null
    ) {
        if (!MightyTracker.checkInitialized("AdTracker.recordRevenue")) return

        fire {
            RetrofitClient.getApi().recordRevenue(
                AdRevenueBody(
                    trackingToken = MightyTracker.trackingToken,
                    country       = country?.lowercase() ?: MightyTracker.getCountry(),
                    os            = Values.OS_ANDROID,
                    adFormat      = adFormat.lowercase(),
                    platform      = platform.lowercase(),
                    revenue       = revenue,
                    currency      = currency.uppercase(),
                    date          = date
                )
            )
        }
    }

    // -------------------------------------------------------------------------
    // Batched Metrics
    // -------------------------------------------------------------------------

    /**
     * Send multiple metrics in one call.
     * At least one of requests / impressions / clicks / revenue must be non-null.
     *
     * @param adFormat    One of: banner, interstitial, native, rewarded, app_open
     * @param platform    Ad network e.g. "admob", "applovin"
     * @param requests    Ad requests count (null = not sent)
     * @param impressions Ad impressions count (null = not sent)
     * @param clicks      Ad click count (null = not sent)
     * @param revenue     Revenue amount (null = not sent)
     * @param currency    3-letter ISO code (only used when revenue != null)
     * @param country     2-letter ISO code. Auto-detected if null.
     * @param date        Optional YYYY-MM-DD override.
     */
    @JvmStatic
    @JvmOverloads
    fun recordMetrics(
        adFormat: String,
        platform: String,
        requests: Int?    = null,
        impressions: Int? = null,
        clicks: Int?      = null,
        revenue: Double?  = null,
        currency: String  = SdkConstants.Config.DEFAULT_CURRENCY,
        country: String?  = null,
        date: String?     = null
    ) {
        if (!MightyTracker.checkInitialized("AdTracker.recordMetrics")) return

        if (requests == null && impressions == null && clicks == null && revenue == null) {
            if (MightyTracker.debugEnabled) Log.w(TAG, "recordMetrics: all metrics null — ignored")
            return
        }

        fire {
            RetrofitClient.getApi().recordMetrics(
                AdMetricsBody(
                    trackingToken = MightyTracker.trackingToken,
                    country       = country?.lowercase() ?: MightyTracker.getCountry(),
                    os            = Values.OS_ANDROID,
                    adFormat      = adFormat.lowercase(),
                    platform      = platform.lowercase(),
                    requests      = requests,
                    impressions   = impressions,
                    clicks        = clicks,
                    revenue       = revenue,
                    currency      = if (revenue != null) currency.uppercase() else null,
                    date          = date
                )
            )
        }
    }

    // -------------------------------------------------------------------------
    // Internal fire-and-forget launcher
    // -------------------------------------------------------------------------

    private fun fire(block: suspend () -> retrofit2.Response<ApiResponse>) {
        scope.launch {
            try {
                val response = block()
                if (MightyTracker.debugEnabled) {
                    if (response.isSuccessful) {
                        Log.d(TAG, "✓ ${response.raw().request.url} → ${response.code()}")
                    } else {
                        Log.w(TAG, "✗ ${response.raw().request.url} → ${response.code()} ${response.errorBody()?.string()}")
                    }
                }
            } catch (e: Exception) {
                // Silent fail — tracking must never crash host app
                if (MightyTracker.debugEnabled) Log.e(TAG, "Request failed: ${e.message}")
            }
        }
    }
}