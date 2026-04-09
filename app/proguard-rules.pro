# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# ── Kotlin Serialization ─────────────────────────────────────────────
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keep,includedescriptorclasses class com.wadjet.**$$serializer { *; }
-keepclassmembers class com.wadjet.** {
    *** Companion;
}
-keepclasseswithmembers class com.wadjet.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ── Retrofit ─────────────────────────────────────────────────────────
-keepattributes Signature, Exceptions
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit

# ── OkHttp / Okio ───────────────────────────────────────────────────
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-dontwarn okio.**

# ── Firebase Crashlytics ─────────────────────────────────────────────
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-keep class com.google.firebase.crashlytics.** { *; }

# ── Room ─────────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ── ONNX Runtime ─────────────────────────────────────────────────────
-keep class ai.onnxruntime.** { *; }
-dontwarn ai.onnxruntime.**

# ── Coil ─────────────────────────────────────────────────────────────
-dontwarn coil3.**

# ── Hilt / Dagger ────────────────────────────────────────────────────
-dontwarn dagger.hilt.internal.**
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# ── Google Sign-In / Credential Manager ──────────────────────────────
-keep class com.google.android.libraries.identity.googleid.** { *; }
-keep class androidx.credentials.** { *; }
-dontwarn com.google.android.libraries.identity.googleid.**
-dontwarn androidx.credentials.**

# ── Enum serialization ───────────────────────────────────────────────
-keepclassmembers class * extends java.lang.Enum {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

#-renamesourcefileattribute SourceFile