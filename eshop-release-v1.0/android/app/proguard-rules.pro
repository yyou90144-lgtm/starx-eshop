# Keep kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.eshop.starx.**$$serializer { *; }
-keepclassmembers class com.eshop.starx.** {
    *** Companion;
}
-keepclasseswithmembers class com.eshop.starx.** {
    kotlinx.serialization.KSerializer serializer(...);
}
