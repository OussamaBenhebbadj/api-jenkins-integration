package com.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MainTest {
    @Test
    void testGetMessage() {
        Main main = new Main();
        assertEquals("Hello from API", main.getMessage());
    }
}