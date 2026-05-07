package com.trackmighty.sdk.tracker

import android.util.Log
import com.trackmighty.sdk.MightyTracker
import com.trackmighty.sdk.SdkConstants
import com.trackmighty.sdk.SdkConstants.Values
import com.trackmighty.sdk.models.ConversionBody
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Tracks purchase and subscription lifecycle events.
 *
 * All methods post to POST /conversion.
 * device_token is read from MightyTracker automatically.
 *
 * Lifecycle flow for subscriptions:
 *   recordTrialStart() → recordSubscriptionPurchased() → recordRenewal() (×N)
 *                                                       → recordCancellation()
 *                                                       → recordExpiry()
 *
 * The server deduplicates on (transaction_id, event_type) so it's safe
 * to call these methods multiple times for the same event.
 */
object ConversionTracker {

    private const val TAG = "TrackMighty.Conversion"

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // -------------------------------------------------------------------------
    // One-time Purchase
    // -------------------------------------------------------------------------

    /**
     * Call after a one-time in-app purchase is confirmed and acknowledged.
     *
     * Play Billing example:
     * ```kotlin
     * billingClient.acknowledgePurchase(params) { result ->
     *     if (result.responseCode == BillingResponseCode.OK) {
     *         ConversionTracker.recordPurchase(
     *             storeProductId = purchase.products[0],
     *             transactionId  = purchase.orderId,
     *             revenue        = 9.99,
     *             currency       = "USD"
     *         )
     *     }
     * }
     * ```
     *
     * @param storeProductId  Google Play product ID e.g. "com.yourapp.coins_500"
     * @param transactionId   Play orderId e.g. "GPA.3345-7890-1234-56789"
     * @param revenue         Purchase price
     * @param currency        3-letter ISO code (default USD)
     * @param convertedAt     ISO 8601 timestamp of purchase. Defaults to server now.
     */
    @JvmStatic
    @JvmOverloads
    fun recordPurchase(
        storeProductId: String,
        transactionId: String,
        revenue: Double,
        currency: String   = SdkConstants.Config.DEFAULT_CURRENCY,
        convertedAt: String? = null
    ) {
        if (!MightyTracker.checkInitialized("ConversionTracker.recordPurchase")) return

        fire(storeProductId, transactionId, Values.EVENT_PURCHASED, revenue, currency,
            convertedAt = convertedAt)
    }

    // -------------------------------------------------------------------------
    // Subscription — Trial Start
    // -------------------------------------------------------------------------

    /**
     * Call when a free trial begins. Revenue is 0.
     *
     * @param storeProductId  e.g. "com.yourapp.monthly"
     * @param transactionId   Play orderId for this subscription
     * @param periodEndAt     ISO 8601 datetime when the trial ends
     * @param convertedAt     ISO 8601 timestamp of event.
     */
    @JvmStatic
    @JvmOverloads
    fun recordTrialStart(
        storeProductId: String,
        transactionId: String,
        periodEndAt: String?  = null,
        convertedAt: String?  = null
    ) {
        if (!MightyTracker.checkInitialized("ConversionTracker.recordTrialStart")) return

        fire(storeProductId, transactionId, Values.EVENT_TRIAL_START, 0.0,
            periodEndAt = periodEndAt, convertedAt = convertedAt)
    }

    // -------------------------------------------------------------------------
    // Subscription — First Paid Conversion
    // -------------------------------------------------------------------------

    /**
     * Call when a subscription converts from trial to paid, or starts paid directly.
     *
     * @param storeProductId  e.g. "com.yourapp.monthly"
     * @param transactionId   Play orderId
     * @param revenue         First billing amount
     * @param currency        3-letter ISO code (default USD)
     * @param periodStartAt   ISO 8601 start of the billing period
     * @param periodEndAt     ISO 8601 end of the billing period
     * @param convertedAt     ISO 8601 timestamp of event.
     */
    @JvmStatic
    @JvmOverloads
    fun recordSubscriptionPurchased(
        storeProductId: String,
        transactionId: String,
        revenue: Double,
        currency: String      = SdkConstants.Config.DEFAULT_CURRENCY,
        periodStartAt: String? = null,
        periodEndAt: String?   = null,
        convertedAt: String?   = null
    ) {
        if (!MightyTracker.checkInitialized("ConversionTracker.recordSubscriptionPurchased")) return

        fire(storeProductId, transactionId, Values.EVENT_PURCHASED, revenue, currency,
            periodStartAt = periodStartAt, periodEndAt = periodEndAt, convertedAt = convertedAt)
    }

    // -------------------------------------------------------------------------
    // Subscription — Renewal
    // -------------------------------------------------------------------------

    /**
     * Call each time a subscription renews for another billing period.
     * Use the same [transactionId] as the original purchase.
     *
     * @param storeProductId  e.g. "com.yourapp.monthly"
     * @param transactionId   Same orderId as original subscription purchase
     * @param revenue         Renewal billing amount
     * @param currency        3-letter ISO code (default USD)
     * @param periodStartAt   ISO 8601 start of new billing period
     * @param periodEndAt     ISO 8601 end of new billing period
     * @param convertedAt     ISO 8601 timestamp of event.
     */
    @JvmStatic
    @JvmOverloads
    fun recordRenewal(
        storeProductId: String,
        transactionId: String,
        revenue: Double,
        currency: String       = SdkConstants.Config.DEFAULT_CURRENCY,
        periodStartAt: String? = null,
        periodEndAt: String?   = null,
        convertedAt: String?   = null
    ) {
        if (!MightyTracker.checkInitialized("ConversionTracker.recordRenewal")) return

        fire(storeProductId, transactionId, Values.EVENT_RENEWED, revenue, currency,
            periodStartAt = periodStartAt, periodEndAt = periodEndAt, convertedAt = convertedAt)
    }

    // -------------------------------------------------------------------------
    // Subscription — Cancellation
    // -------------------------------------------------------------------------

    /**
     * Call when a user cancels their subscription.
     * The subscription may still be active until [periodEndAt].
     *
     * @param storeProductId  e.g. "com.yourapp.monthly"
     * @param transactionId   Original orderId
     * @param periodEndAt     ISO 8601 when the subscription expires after cancellation
     * @param convertedAt     ISO 8601 timestamp of event.
     */
    @JvmStatic
    @JvmOverloads
    fun recordCancellation(
        storeProductId: String,
        transactionId: String,
        periodEndAt: String?  = null,
        convertedAt: String?  = null
    ) {
        if (!MightyTracker.checkInitialized("ConversionTracker.recordCancellation")) return

        fire(storeProductId, transactionId, Values.EVENT_CANCELLED, 0.0,
            periodEndAt = periodEndAt, convertedAt = convertedAt)
    }

    // -------------------------------------------------------------------------
    // Subscription — Expired
    // -------------------------------------------------------------------------

    /**
     * Call when a subscription fully expires after the cancellation grace period.
     *
     * @param storeProductId  e.g. "com.yourapp.monthly"
     * @param transactionId   Original orderId
     * @param convertedAt     ISO 8601 timestamp of event.
     */
    @JvmStatic
    @JvmOverloads
    fun recordExpiry(
        storeProductId: String,
        transactionId: String,
        convertedAt: String? = null
    ) {
        if (!MightyTracker.checkInitialized("ConversionTracker.recordExpiry")) return

        fire(storeProductId, transactionId, Values.EVENT_EXPIRED, 0.0,
            convertedAt = convertedAt)
    }

    // -------------------------------------------------------------------------
    // Refund
    // -------------------------------------------------------------------------

    /**
     * Call when a purchase or subscription payment is refunded.
     *
     * @param storeProductId  e.g. "com.yourapp.monthly"
     * @param transactionId   Original orderId
     * @param revenue         Refunded amount
     * @param currency        3-letter ISO code (default USD)
     * @param convertedAt     ISO 8601 timestamp of event.
     */
    @JvmStatic
    @JvmOverloads
    fun recordRefund(
        storeProductId: String,
        transactionId: String,
        revenue: Double,
        currency: String   = SdkConstants.Config.DEFAULT_CURRENCY,
        convertedAt: String? = null
    ) {
        if (!MightyTracker.checkInitialized("ConversionTracker.recordRefund")) return

        fire(storeProductId, transactionId, Values.EVENT_REFUNDED, revenue, currency,
            convertedAt = convertedAt)
    }

    // -------------------------------------------------------------------------
    // Internal fire-and-forget helper
    // -------------------------------------------------------------------------

    private fun fire(
        storeProductId: String,
        transactionId: String,
        eventType: String,
        revenue: Double,
        currency: String       = SdkConstants.Config.DEFAULT_CURRENCY,
        periodStartAt: String? = null,
        periodEndAt: String?   = null,
        convertedAt: String?   = null
    ) {
        scope.launch {
            try {
                val response = RetrofitClient.getApi().recordConversion(
                    ConversionBody(
                        trackingToken = MightyTracker.trackingToken,
                        deviceToken = MightyTracker.deviceToken,
                        transactionId = transactionId,
                        storeProductId = storeProductId,
                        store = Values.STORE_PLAY,
                        eventType = eventType,
                        revenue = revenue,
                        currency = currency.uppercase(),
                        periodStartAt = periodStartAt,
                        periodEndAt = periodEndAt,
                        convertedAt = convertedAt
                    )
                )

                if (MightyTracker.debugEnabled) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.duplicate == true) {
                            Log.d(TAG, "↩ Duplicate conversion ignored: $eventType $transactionId")
                        } else {
                            Log.d(TAG, "✓ Conversion recorded: $eventType $storeProductId")
                        }
                    } else {
                        Log.w(TAG, "✗ Conversion failed: ${response.code()} ${response.errorBody()?.string()}")
                    }
                }
            } catch (e: Exception) {
                // Silent fail — tracking must never crash host app
                if (MightyTracker.debugEnabled) Log.e(TAG, "Conversion request failed: ${e.message}")
            }
        }
    }
}