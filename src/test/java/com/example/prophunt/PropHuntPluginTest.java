package com.example.prophunt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic unit tests for PropHunt plugin.
 */
class PropHuntPluginTest {

    @Test
    @DisplayName("Plugin class should be loadable")
    void pluginClassShouldBeLoadable() {
        // Verify the class can be loaded without errors
        assertDoesNotThrow(() -> Class.forName("com.example.prophunt.PropHuntPlugin"));
    }

    @Test
    @DisplayName("Basic assertion test")
    void basicTest() {
        // Simple test to verify test framework is working
        assertTrue(true, "Basic test should pass");
    }
}
