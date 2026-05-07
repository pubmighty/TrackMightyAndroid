package com.trackmighty.sdk.tracker

import com.google.gson.GsonBuilder
import com.trackmighty.sdk.SdkConstants.Config
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Builds and holds the single Retrofit + OkHttp instance for the SDK.
 * Recreated each time MightyTracker.init() is called (in case baseUrl changes).
 */
internal object RetrofitClient {

    private var api: MightyTrackerApi? = null

    /**
     * (Re)initializes the Retrofit client with the given [baseUrl].
     * Called from MightyTracker.init().
     *
     * @param baseUrl   e.g. https://api.yourdomain.com/v1/sdk/
     * @param debug     If true, logs all HTTP request/response bodies to Logcat
     */
    fun init(baseUrl: String, debug: Boolean) {
        // Ensure baseUrl always ends with /
        val normalizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(Config.CONNECT_TIMEOUT_SEC, TimeUnit.SECONDS)
            .readTimeout(Config.READ_TIMEOUT_SEC, TimeUnit.SECONDS)
            .writeTimeout(Config.WRITE_TIMEOUT_SEC, TimeUnit.SECONDS)
            .apply {
                if (debug) {
                    // Logs full request + response body when debug = true
                    val logging = HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    }
                    addInterceptor(logging)
                }
            }
            .build()

        // serializeNulls = false so null fields are omitted from JSON
        // (important for optional fields like date, period_start_at etc.)
        val gson = GsonBuilder()
            .serializeNulls()
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(normalizedUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        api = retrofit.create(MightyTrackerApi::class.java)
    }

    /**
     * Returns the initialized API instance.
     * Throws if init() was not called yet.
     */
    fun getApi(): MightyTrackerApi {
        return api ?: throw IllegalStateException(
            "MightyTracker SDK not initialized. Call MightyTracker.init() first."
        )
    }
}
