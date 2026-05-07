package com.trackmighty.sdk


/**
 * SdkConstants
 *
 * Single source of truth for every API endpoint path and JSON field name.
 * If your backend changes a path or field key, update it here — nowhere else.
 */
object SdkConstants {

    // -------------------------------------------------------------------------
    // API Endpoint Paths
    // These are appended to the baseUrl set in MightyTracker.init()
    // -------------------------------------------------------------------------
    object Endpoints {
        const val BASE_URL    = "https://api.trackmighty.com/v1/"

        const val INSTALL       = "sdk/install"
        const val AD_REQUEST    = "sdk/ads/tracking/request"
        const val AD_IMPRESSION = "sdk/ads/tracking/impression"
        const val AD_CLICK      = "sdk/ads/tracking/click"
        const val AD_REVENUE    = "sdk/ads/tracking/revenue"
        const val AD_METRICS    = "sdk/ads/tracking/metrics"
        const val CONVERSION    = "sdk/conversion"
    }

    // -------------------------------------------------------------------------
    // JSON Field Names
    // Match server-side Joi schema field names exactly.
    // -------------------------------------------------------------------------
    object Fields {
        // Shared base fields
        const val TRACKING_TOKEN    = "tracking_token"
        const val DEVICE_TOKEN      = "device_token"
        const val COUNTRY           = "country"
        const val OS                = "os"
        const val DATE              = "date"

        const val STORE             = "store"

        // Ad tracking fields
        const val AD_FORMAT         = "ad_format"
        const val PLATFORM          = "platform"
        const val REQUESTS          = "requests"
        const val IMPRESSIONS       = "impressions"
        const val CLICKS            = "clicks"
        const val REVENUE           = "revenue"
        const val CURRENCY          = "currency"

        // Conversion fields
        const val TRANSACTION_ID    = "transaction_id"
        const val STORE_PRODUCT_ID  = "store_product_id"
        const val EVENT_TYPE        = "event_type"
        const val PERIOD_START_AT   = "period_start_at"
        const val PERIOD_END_AT     = "period_end_at"
        const val CONVERTED_AT      = "converted_at"
    }

    // -------------------------------------------------------------------------
    // Fixed Values
    // -------------------------------------------------------------------------
    object Values {
        const val OS_ANDROID  = "android"
        const val STORE_PLAY  = "play_store"
        const val STORE_IOS   = "app_store"

        // event_type values — match server conversionSchema VALID_EVENT_TYPES
        const val EVENT_TRIAL_START = "trial_start"
        const val EVENT_PURCHASED   = "purchased"
        const val EVENT_RENEWED     = "renewed"
        const val EVENT_CANCELLED   = "cancelled"
        const val EVENT_EXPIRED     = "expired"
        const val EVENT_REFUNDED    = "refunded"

        // ad_format values — match server VALID_FORMATS
        const val FORMAT_BANNER        = "banner"
        const val FORMAT_INTERSTITIAL  = "interstitial"
        const val FORMAT_NATIVE        = "native"
        const val FORMAT_REWARDED      = "rewarded"
        const val FORMAT_APP_OPEN      = "app_open"
    }

    // -------------------------------------------------------------------------
    // SDK Config Defaults
    // -------------------------------------------------------------------------
    object Config {
        const val SDK_VERSION_NAME  = "1.0.0"
        const val DEFAULT_CURRENCY     = "USD"
        const val CONNECT_TIMEOUT_SEC  = 10L
        const val READ_TIMEOUT_SEC     = 10L
        const val WRITE_TIMEOUT_SEC    = 10L
        const val PREFS_NAME           = "mighty_tracker_prefs"
        const val PREFS_KEY_DEVICE_TOKEN = "device_token"
    }
}