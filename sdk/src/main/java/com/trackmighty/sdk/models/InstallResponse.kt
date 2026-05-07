package com.trackmighty.sdk.models

import com.google.gson.annotations.SerializedName

/**
 * Response from POST /install
 */
data class InstallResponse(
    @SerializedName("success")        val success: Boolean,
    @SerializedName("msg")            val msg: String?        = null,
    @SerializedName("is_new_install") val isNewInstall: Boolean = false,
    @SerializedName("device_id")      val deviceId: Long?     = null,
    @SerializedName("install_id")     val installId: Long?    = null,
    @SerializedName("attribution")    val attribution: AttributionResult? = null
)