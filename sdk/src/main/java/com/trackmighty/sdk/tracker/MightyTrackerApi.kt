package com.trackmighty.sdk.tracker

import com.trackmighty.sdk.SdkConstants.Endpoints
import com.trackmighty.sdk.models.AdClickBody
import com.trackmighty.sdk.models.AdImpressionBody
import com.trackmighty.sdk.models.AdMetricsBody
import com.trackmighty.sdk.models.AdRequestBody
import com.trackmighty.sdk.models.AdRevenueBody
import com.trackmighty.sdk.models.ApiResponse
import com.trackmighty.sdk.models.ConversionBody
import com.trackmighty.sdk.models.InstallBody
import com.trackmighty.sdk.models.InstallResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit interface defining all SDK API endpoints.
 * Uses the path constants from SdkConstants.Endpoints.
 */
interface MightyTrackerApi {

    // -------------------------------------------------------------------------
    // Install / Device Registration
    // -------------------------------------------------------------------------

    /**
     * Registers the device and records the install event.
     * Called automatically from MightyTracker.init() on every app open.
     * Server upserts Device and only creates Install on first open.
     */
    @POST(Endpoints.INSTALL)
    suspend fun recordInstall(
        @Body body: InstallBody
    ): Response<InstallResponse>

    // -------------------------------------------------------------------------
    // Ad Tracking Endpoints
    // -------------------------------------------------------------------------

    @POST(Endpoints.AD_REQUEST)
    suspend fun recordRequest(
        @Body body: AdRequestBody
    ): Response<ApiResponse>

    @POST(Endpoints.AD_IMPRESSION)
    suspend fun recordImpression(
        @Body body: AdImpressionBody
    ): Response<ApiResponse>

    @POST(Endpoints.AD_CLICK)
    suspend fun recordClick(
        @Body body: AdClickBody
    ): Response<ApiResponse>

    @POST(Endpoints.AD_REVENUE)
    suspend fun recordRevenue(
        @Body body: AdRevenueBody
    ): Response<ApiResponse>

    @POST(Endpoints.AD_METRICS)
    suspend fun recordMetrics(
        @Body body: AdMetricsBody
    ): Response<ApiResponse>

    // -------------------------------------------------------------------------
    // Conversion Endpoint
    // -------------------------------------------------------------------------

    @POST(Endpoints.CONVERSION)
    suspend fun recordConversion(
        @Body body: ConversionBody
    ): Response<ApiResponse>
}
