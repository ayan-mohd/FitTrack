package com.fittrack;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AppTest {

    @Test
    public void testJavaVersion() {
        String javaVersion = System.getProperty("java.version");
        System.out.println("Running tests with Java Version: " + javaVersion);
        // We expect to be running on at least Java 21 compatible runtime
        // Since we are running with JDK 23, this should pass.
        assertTrue(Integer.parseInt(javaVersion.split("\\.")[0]) >= 21, "Java version should be 21 or higher");
    }
}
