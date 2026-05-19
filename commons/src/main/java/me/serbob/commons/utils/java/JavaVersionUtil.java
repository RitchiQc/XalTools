package me.serbob.commons.utils.java;

public class JavaVersionUtil {
    public static int getCurrentJavaVersion() {
        String version = System.getProperty("java.version");

        if (version.startsWith("1.")) {
            version = version.substring(2);
            int dotPos = version.indexOf('.');
            if (dotPos != -1) {
                version = version.substring(0, dotPos);
            }
        } else {
            int dotPos = version.indexOf('.');
            if (dotPos != -1) {
                version = version.substring(0, dotPos);
            }
        }

        return Integer.parseInt(version);
    }
}
