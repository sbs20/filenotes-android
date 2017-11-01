-dontskipnonpubliclibraryclasses
-dontobfuscate
-forceprocessing
-optimizationpasses 2

#-keep class com.dropbox.** { *; }
-keep class android.support.v7.widget.SearchView { *; }

#-dontwarn com.dropbox.**
-dontwarn okio.**
-dontwarn okhttp3.**
-dontwarn com.squareup.okhttp.**
-dontwarn com.google.appengine.**
-dontwarn javax.servlet.**

-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}
