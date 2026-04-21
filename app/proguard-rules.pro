# Add project specific ProGuard rules here.
# Keep Room entities
-keep class com.kiosq.data.entity.** { *; }
# Keep Gson models
-keep class com.kiosq.util.BackupData { *; }
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn com.google.gson.**
