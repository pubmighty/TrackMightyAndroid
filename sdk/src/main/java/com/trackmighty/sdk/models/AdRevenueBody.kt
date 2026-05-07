package com.trackmighty.sdk.models

import com.google.gson.annotations.SerializedName
import com.trackmighty.sdk.SdkConstants.Fields

// ---------------------------------------------------------------------------
// Ad Tracking Request Bodies
// ---------------------------------------------------------------------------

/**
 * Body for POST /ads/tracking/revenue
 * Matches server revenueSchema fields exactly.
 */
data class AdRevenueBody(
    @SerializedName(Fields.TRACKING_TOKEN) val trackingToken: String,
    @SerializedName(Fields.COUNTRY)        val country: String,
    @SerializedName(Fields.OS)             val os: String,
    @SerializedName(Fields.AD_FORMAT)      val adFormat: String,
    @SerializedName(Fields.PLATFORM)       val platform: String,
    @SerializedName(Fields.REVENUE)        val revenue: Double,
    @SerializedName(Fields.CURRENCY)       val currency: String,
    @SerializedName(Fields.DATE)           val date: String? = null
)