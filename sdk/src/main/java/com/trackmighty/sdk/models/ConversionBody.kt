package com.trackmighty.sdk.models

import com.google.gson.annotations.SerializedName
import com.trackmighty.sdk.SdkConstants.Fields


/**
 * Body for POST /conversion
 * Matches server conversionSchema fields exactly.
 */
data class ConversionBody(
    @SerializedName(Fields.TRACKING_TOKEN)   val trackingToken: String,
    @SerializedName(Fields.DEVICE_TOKEN)     val deviceToken: String,
    @SerializedName(Fields.TRANSACTION_ID)   val transactionId: String,
    @SerializedName(Fields.STORE_PRODUCT_ID) val storeProductId: String,
    @SerializedName(Fields.STORE)            val store: String,
    @SerializedName(Fields.EVENT_TYPE)       val eventType: String,
    @SerializedName(Fields.REVENUE)          val revenue: Double,
    @SerializedName(Fields.CURRENCY)         val currency: String,
    @SerializedName(Fields.PERIOD_START_AT)  val periodStartAt: String? = null,
    @SerializedName(Fields.PERIOD_END_AT)    val periodEndAt: String?   = null,
    @SerializedName(Fields.CONVERTED_AT)     val convertedAt: String?   = null
)
