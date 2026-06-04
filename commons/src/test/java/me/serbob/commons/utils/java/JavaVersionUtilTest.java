package me.serbob.commons.utils.java;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JavaVersionUtilTest {

    @Test
    void getCurrentJavaVersion_returnsPositiveNumber() {
        int version = JavaVersionUtil.getCurrentJavaVersion();
        assertTrue(version > 0, "Java version should be positive");
    }

    @Test
    void getCurrentJavaVersion_returnsAtLeast17() {
        int version = JavaVersionUtil.getCurrentJavaVersion();
        assertTrue(version >= 17, "Java version should be at least 17 for this project");
    }
}
