package com.trackmighty.sdk.models

import com.google.gson.annotations.SerializedName
import com.trackmighty.sdk.SdkConstants.Fields
import com.trackmighty.sdk.SdkConstants.Values.OS_ANDROID

// ---------------------------------------------------------------------------
// Add these two classes to your existing ApiModels.kt file
// ---------------------------------------------------------------------------

/**
 * Body for POST /install
 * Matches server installSchema fields exactly.
 */
data class InstallBody(
    @SerializedName(Fields.TRACKING_TOKEN)   val trackingToken: String,
    @SerializedName(Fields.DEVICE_TOKEN)     val deviceToken: String,
    @SerializedName("gaid")                  val gaid: String?             = null,
    @SerializedName("android_id")            val androidId: String?        = null,
    @SerializedName("os")            val os: String        = OS_ANDROID,
    @SerializedName("os_version")            val osVersion: String?        = null,
    @SerializedName("device_model")          val deviceModel: String?      = null,
    @SerializedName("device_brand")          val deviceBrand: String?      = null,
    @SerializedName(Fields.COUNTRY)          val country: String?          = null,
    @SerializedName("language")              val language: String?         = null,
    @SerializedName("timezone")              val timezone: String?         = null,
    @SerializedName("app_version")           val appVersion: String?       = null,
    @SerializedName("sdk_version")           val sdkVersion: String?       = null,
    @SerializedName("install_referrer")      val installReferrer: String?  = null,
    @SerializedName("store")                 val store: String             = "play_store",
    @SerializedName("screen_resolution")     val screenResolution: String? = null
)