# 项目混淆规则（默认不混淆，如需开启 release 混淆请按需添加）
# Retrofit / Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-keep class com.example.lifehelper.network.** { *; }
-keep class com.example.lifehelper.data.db.entity.** { *; }
