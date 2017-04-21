-keepattributes *Annotation*

-keep class android.support.v7.internal.** { *; }
-keep interface android.support.v7.internal.** { *; }
-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }

-keep class com.google.** { *;}
-keep interface com.google.** { *;}
-dontwarn com.google.**

-keep class ru.zabbkitserver.android.remote.model.** { *;}
-keep interface ru.zabbkitserver.android.remote.model.** { *;}
