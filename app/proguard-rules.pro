# Apache POI rules
-keep class org.apache.poi.** { *; }
-keep class org.apache.xmlbeans.** { *; }
-keep class schemaorg_apache_xmlbeans.** { *; }
-keep class com.microsoft.schemas.** { *; }

# Keep all classes that might be accessed via reflection
-keep class * extends org.apache.poi.POIDocument { *; }
-keep class * extends org.apache.poi.ss.usermodel.Workbook { *; }
-keep class * extends org.apache.poi.ss.usermodel.Sheet { *; }
-keep class * extends org.apache.poi.ss.usermodel.Row { *; }
-keep class * extends org.apache.poi.ss.usermodel.Cell { *; }

# Keep enum values
-keep class org.apache.poi.ss.usermodel.** { *; }
-keep class org.apache.poi.xwpf.usermodel.** { *; }
-keep class org.apache.poi.hwpf.** { *; }

# Keep all public and protected methods
-keep public class org.apache.poi.** {
    public protected *;
}

# Keep classes with main methods (if any)
-keep class * {
    public static void main(java.lang.String[]);
}

# Keep all classes in the app package
-keep class com.example.ogitts.** { *; }

# Keep Compose classes
-keep class androidx.compose.** { *; }
-keep class androidx.lifecycle.** { *; }
-keep class androidx.activity.** { *; }

# Keep data classes and their properties
-keep class kotlin.** { *; }
-keep class kotlin.jvm.** { *; }
