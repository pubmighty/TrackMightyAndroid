package com.trackmighty.sdk.models

import com.google.gson.annotations.SerializedName
import com.trackmighty.sdk.SdkConstants.Fields

// ---------------------------------------------------------------------------
// Ad Tracking Request Bodies
// ---------------------------------------------------------------------------

/**
 * Body for POST /ads/tracking/request
 * Matches server requestSchema fields exactly.
 */
data class AdRequestBody(
    @SerializedName(Fields.TRACKING_TOKEN) val trackingToken: String,
    @SerializedName(Fields.COUNTRY)        val country: String,
    @SerializedName(Fields.OS)             val os: String,
    @SerializedName(Fields.AD_FORMAT)      val adFormat: String,
    @SerializedName(Fields.PLATFORM)       val platform: String,
    @SerializedName(Fields.REQUESTS)       val requests: Int,
    @SerializedName(Fields.DATE)           val date: String? = null
)