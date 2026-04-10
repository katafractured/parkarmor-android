# ProGuard rules for ParkArmor

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public <methods>;
}
-keep interface * extends androidx.room.Dao

# Google Play Services - Location
-keep class com.google.android.gms.location.** { *; }
-keep interface com.google.android.gms.location.** { *; }

# Google Play Services - Maps
-keep class com.google.android.gms.maps.** { *; }
-keep interface com.google.android.gms.maps.** { *; }

# Google Maps Compose
-keep class com.google.maps.android.compose.** { *; }

# Compose
-keep @androidx.compose.runtime.Composable class * { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Suppress error prone warnings
-dontwarn com.google.errorprone.annotations.**
-dontwarn org.checkerframework.**
-dontwarn javax.annotation.**

# Keep view constructors for inflation
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

# Keep custom views
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
}

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
