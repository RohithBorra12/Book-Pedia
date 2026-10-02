package com.bookstore.user.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private final JwtUtil jwtUtil = new JwtUtil("test-secret-key-that-is-at-least-32-bytes-long", 60_000);

    @Test
    void createsAndReadsToken() {
        String token = jwtUtil.generateToken("alice", "ADMIN");
        Claims claims = jwtUtil.parse(token);

        assertEquals("alice", claims.getSubject());
        assertEquals("ADMIN", claims.get("role", String.class));
    }

    @Test
    void rejectsTokenSignedWithAnotherKey() {
        JwtUtil other = new JwtUtil("a-completely-different-secret-key-1234567890", 60_000);
        String token = other.generateToken("mallory", "ADMIN");

        assertThrows(JwtException.class, () -> jwtUtil.parse(token));
    }
}
