# ============================================================
# Phantom Net — ProGuard / R8 Rules
# ============================================================

# ---- Compose ----
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

# ---- JNI / Native Bridge ----
# Keep all classes that call native methods (JNI will look them up by name)
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep SignalBridge (called from native Rust code via JNI)
-keep class com.phantomnet.core.crypto.SignalBridge { *; }
-keep class com.phantomnet.core.network.** { *; }

# ---- Data classes used by the app ----
-keep class com.phantomnet.app.domain.model.** { *; }
-keep class com.phantomnet.app.domain.NetworkStatus { *; }

# ---- Kotlin coroutines ----
-dontwarn kotlinx.coroutines.**

# ---- General Android ----
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends android.app.Application
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
