-dontskipnonpubliclibraryclasses
-dontobfuscate
-forceprocessing
-optimizationpasses 2

-keep class com.dropbox.** { *; }

-dontwarn com.dropbox.**

-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}
