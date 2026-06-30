# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.copymanga.downloader.**$$serializer { *; }
-keepclassmembers class com.copymanga.downloader.** {
    *** Companion;
}
-keepclasseswithmembers class com.copymanga.downloader.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Retrofit
-keepattributes Signature, Exceptions
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**

# PdfBox-Android
-keep class com.tom_roush.pdfbox.** { *; }
-dontwarn com.tom_roush.**
-dontwarn org.bouncycastle.**

# Coil
-dontwarn coil.**

# Application models and DTOs (kotlinx.serialization)
-keep class com.copymanga.downloader.data.model.** { *; }
-keep class com.copymanga.downloader.data.remote.dto.** { *; }
-keepclassmembers class com.copymanga.downloader.data.model.** { <init>(...); }
-keepclassmembers class com.copymanga.downloader.data.remote.dto.** { <init>(...); }

# Enum values used by kotlinx.serialization
-keepclassmembers enum com.copymanga.downloader.data.model.** { *; }
-keepclassmembers enum com.copymanga.downloader.data.remote.dto.** { *; }

# FileProvider path used by FileProvider reflection
-keepclassmembers class android.support.v4.content.FileProvider { *; }
-keepclassmembers class androidx.core.content.FileProvider { *; }
