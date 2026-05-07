package com.trackmighty.sdk.models

import com.google.gson.annotations.SerializedName
import com.trackmighty.sdk.SdkConstants.Fields

// ---------------------------------------------------------------------------
// Ad Tracking Request Bodies
// ---------------------------------------------------------------------------

/**
 * Body for POST /ads/tracking/metrics
 * Matches server metricsSchema fields exactly.
 * Nullable fields are omitted from JSON when null (handled by Gson + @SerializedName).
 */
data class AdMetricsBody(
    @SerializedName(Fields.TRACKING_TOKEN) val trackingToken: String,
    @SerializedName(Fields.COUNTRY)        val country: String,
    @SerializedName(Fields.OS)             val os: String,
    @SerializedName(Fields.AD_FORMAT)      val adFormat: String,
    @SerializedName(Fields.PLATFORM)       val platform: String,
    @SerializedName(Fields.REQUESTS)       val requests: Int?    = null,
    @SerializedName(Fields.IMPRESSIONS)    val impressions: Int? = null,
    @SerializedName(Fields.CLICKS)         val clicks: Int?      = null,
    @SerializedName(Fields.REVENUE)        val revenue: Double?  = null,
    @SerializedName(Fields.CURRENCY)       val currency: String? = null,
    @SerializedName(Fields.DATE)           val date: String?     = null
)
