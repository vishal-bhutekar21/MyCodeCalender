# Proguard and R8 optimization rules for MyCodeCalendar (Release Build)

# -----------------------------------------------------------------------------
# 1. Kotlin & Coroutines
# -----------------------------------------------------------------------------
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-dontwarn kotlinx.coroutines.**
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# -----------------------------------------------------------------------------
# 2. Kotlinx Serialization
# -----------------------------------------------------------------------------
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class * extends kotlinx.serialization.internal.GeneratedSerializer {
    <init>(...);
}
-keep,allowobfuscation,allowshrinking class * extends kotlinx.serialization.internal.GeneratedSerializer

# Keep all Network DTOs
-keep class com.mycodecalendar.core.network.** { *; }
-keep class com.mycodecalendar.domain.model.** { *; }

# -----------------------------------------------------------------------------
# 3. Room Database
# -----------------------------------------------------------------------------
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep class com.mycodecalendar.core.database.** { *; }
-keep class com.mycodecalendar.core.database.entity.** { *; }
-keep class com.mycodecalendar.core.database.dao.** { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>(...);
}

# -----------------------------------------------------------------------------
# 4. Ktor Client
# -----------------------------------------------------------------------------
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**
-dontwarn io.netty.**
-dontwarn org.slf4j.**

# -----------------------------------------------------------------------------
# 5. Jetpack Compose
# -----------------------------------------------------------------------------
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.material3.** { *; }
-keep class androidx.compose.foundation.** { *; }

# -----------------------------------------------------------------------------
# 6. Android Platform & Notifications
# -----------------------------------------------------------------------------
-keep class com.mycodecalendar.core.notifications.** { *; }
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
