# ---------------------------------------------------------------------------
# CornerKick Planner ProGuard / R8 rules
# ---------------------------------------------------------------------------

# Keep generic signatures & annotations used by kotlinx.serialization.
-keepattributes *Annotation*, InnerClasses, Signature, RuntimeVisibleAnnotations, AnnotationDefault

# --- kotlinx.serialization ---------------------------------------------------
# Keep the serializer() companion accessors and generated serializers.
-keepclassmembers class ** {
    *** Companion;
}
-keepclasseswithmembers class ** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep every @Serializable model in this app together with its generated
# $$serializer so JSON (DataStore) and Retrofit conversion stay stable in R8.
-keep,includedescriptorclasses class com.cornerkick.planner.**$$serializer { *; }
-keepclassmembers class com.cornerkick.planner.** {
    *** Companion;
}
-keep class com.cornerkick.planner.data.model.** { *; }
-keep class com.cornerkick.planner.data.remote.dto.** { *; }

# --- Retrofit / OkHttp -------------------------------------------------------
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keepclassmembers,allowshrinking,allowobfuscation interface retrofit2.* {
    @retrofit2.http.* <methods>;
}
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Kotlin metadata.
-keep class kotlin.Metadata { *; }
