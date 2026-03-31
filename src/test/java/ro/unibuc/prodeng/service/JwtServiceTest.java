package ro.unibuc.prodeng.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
    }

    @Test
    void testGenerateToken_withValidData_returnsToken() {
        // ARRANGE
        String email = "test@example.com";
        String role = "USER";

        // ACT
        String token = jwtService.generateToken(email, role);

        // ASSERT
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testExtractEmail_fromValidToken_returnsCorrectEmail() {
        // ARRANGE
        String email = "test@example.com";
        String role = "USER";
        String token = jwtService.generateToken(email, role);

        // ACT
        String extractedEmail = jwtService.extractEmail(token);

        // ASSERT
        assertEquals(email, extractedEmail);
    }

    @Test
    void testExtractRole_fromValidToken_returnsCorrectRole() {
        // ARRANGE
        String email = "test@example.com";
        String role = "ADMIN";
        String token = jwtService.generateToken(email, role);

        // ACT
        String extractedRole = jwtService.extractRole(token);

        // ASSERT
        assertEquals(role, extractedRole);
    }

    @Test
    void testIsTokenValid_withValidToken_returnsTrue() {
        // ARRANGE
        String token = jwtService.generateToken("test@example.com", "USER");

        // ACT
        boolean result = jwtService.isTokenValid(token);

        // ASSERT
        assertTrue(result);
    }

    @Test
    void testIsTokenValid_withInvalidToken_returnsFalse() {
        // ARRANGE
        String invalidToken = "invalid.token.value";

        // ACT
        boolean result = jwtService.isTokenValid(invalidToken);

        // ASSERT
        assertFalse(result);
    }

    @Test
    void testExtractEmail_withInvalidToken_throwsException() {
        // ARRANGE
        String invalidToken = "invalid.token";

        // ACT & ASSERT
        assertThrows(Exception.class, () -> jwtService.extractEmail(invalidToken));
    }

    @Test
    void testExtractRole_withInvalidToken_throwsException() {
        // ARRANGE
        String invalidToken = "invalid.token";

        // ACT & ASSERT
        assertThrows(Exception.class, () -> jwtService.extractRole(invalidToken));
    }
}