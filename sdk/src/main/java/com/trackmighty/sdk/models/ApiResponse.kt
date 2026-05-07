package com.trackmighty.sdk.models

import com.google.gson.annotations.SerializedName
import com.trackmighty.sdk.SdkConstants.Fields

/**
 * Generic server response wrapper.
 * All endpoints return { success: bool, msg?: string }
 */
data class ApiResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("msg")     val msg: String?     = null,
    @SerializedName("duplicate") val duplicate: Boolean? = null
)
