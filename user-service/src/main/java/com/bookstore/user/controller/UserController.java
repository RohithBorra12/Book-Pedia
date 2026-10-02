package com.bookstore.user.controller;

import com.bookstore.user.dto.UserResponse;
import com.bookstore.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;

    /** Any logged-in user: shows who you are according to your token. */
    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {
        var roles = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        return Map.of("username", authentication.getName(), "roles", roles);
    }

    /** ADMIN only - role-based security on a method. */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> allUsers() {
        return authService.findAll();
    }
}
