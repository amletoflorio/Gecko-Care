# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Hilt / Dagger handles its own consumer rules via AAR metadata.

# Keep Kotlin metadata for reflection-based libraries
-keepattributes *Annotation*, InnerClasses
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
