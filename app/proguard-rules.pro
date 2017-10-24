-dontskipnonpubliclibraryclasses
-dontobfuscate
-forceprocessing
-optimizationpasses 2

-keep class com.dropbox.** { *; }
-keep class android.support.v7.widget.SearchView { *; }

-dontwarn com.dropbox.**

-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}
