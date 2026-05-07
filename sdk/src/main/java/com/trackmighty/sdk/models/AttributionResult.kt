package com.trackmighty.sdk.models

import com.google.gson.annotations.SerializedName

data class AttributionResult(
    @SerializedName("method")      val method: String?,
    @SerializedName("confidence")  val confidence: Double?,
    @SerializedName("campaign_id") val campaignId: Long?,
    @SerializedName("network_id")  val networkId: Long?
)
