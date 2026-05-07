# TrackMighty SDK ProGuard rules
# Keep all public API classes so developers can call them from obfuscated host apps

-keep class com.trackmighty.app.MightyTracker { *; }
-keep class com.trackmighty.app.tracker.AdTracker { *; }
-keep class com.trackmighty.app.tracker.ConversionTracker { *; }
-keep class com.trackmighty.app.SdkConstants { *; }
-keep class com.mightytracker.sdk.SdkConstants$* { *; }

# Keep network model classes so Gson can deserialize them
-keep class com.mightytracker.sdk.internal.network.** { *; }

# Retrofit
-keepattributes Signature
-keepattributes Exceptions
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Gson
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Optional GAID — suppress warnings if play-services not present
-dontwarn com.google.android.gms.**
-keep class com.google.android.gms.ads.identifier.** { *; }
