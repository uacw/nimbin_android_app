# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# === GENERAL RULES ===
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose
-dontoptimize
-dontpreverify

# === KEEP ANNOTATIONS ===
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes SourceFile,LineNumberTable

# === KOTLIN ===
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# === KOTLINX SERIALIZATION ===
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

# Serializer for classes with named companion objects are retrieved using the companion object name.
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

# Serializer for classes with named companion objects are retrieved using the companion object name.
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepnames class <1>$$serializer {
    public static <1>$$serializer INSTANCE;
}

# If a class has a serializer, keep the serializer
-if @kotlinx.serialization.Serializable class ** {
    public static **$Companion Companion;
}
-keepclasseswithmembers class <1>$Companion {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep `Companion` object fields of serializable classes.
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

# === SHARED MODULE DTO ===
-keep class tech.nimbus.shared.dto.** { *; }
-keep class tech.nimbus.shared.dto.request.** { *; }

# === KTOR CLIENT ===
-keep class io.ktor.** { *; }
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.atomicfu.**
-dontwarn io.netty.**
-dontwarn com.typesafe.**
-dontwarn org.slf4j.**

# === HILT ===
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponent
-keep class **_HiltModules*
-keep class **_*Factory { *; }

# === COMPOSE ===
-keep class androidx.compose.** { *; }
-keep class androidx.compose.runtime.** { *; }
-dontwarn androidx.compose.**

# === DATASTORE ===
-keep class androidx.datastore.** { *; }
-keep class androidx.datastore.preferences.** { *; }

# === TIMBER ===
-keep class timber.log.** { *; }

# === FIREBASE ===
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# === REMOVE LOGGING IN RELEASE ===
-assumenosideeffects class timber.log.Timber {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# === OPTIMIZATION ===
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification

# === ENUM ===
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# === PARCELABLE ===
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# === REFLECTION ===
-keep class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# === NATIVE METHODS ===
-keepclasseswithmembernames class * {
    native <methods>;
}

# === WEBVIEW ===
-keepclassmembers class fqcn.of.javascript.interface.for.webview {
    public *;
}

# === CUSTOM APPLICATION CLASSES ===
-keep public class tech.nimbus.nimbin.NimBinApplication
-keep public class tech.nimbus.nimbin.MainActivity

# === CUSTOM RULES FOR NIMBIN ===
-keep class tech.nimbus.nimbin.data.remote.TokenProvider { *; }
-keep class tech.nimbus.nimbin.domain.repository.** { *; }
-keep class tech.nimbus.nimbin.domain.usecase.** { *; }

# === JAVA MANAGEMENT CLASSES ===
-keep class java.lang.management.ManagementFactory { *; }
-keep class java.lang.management.RuntimeMXBean { *; }
