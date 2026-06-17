package com.siems;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SiemsApplicationTests {

    @Test
    void contextLoads() {
        // Verifies the full Spring context (including security, JPA, MapStruct beans) wires up correctly
    }
}
