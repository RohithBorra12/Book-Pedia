package com.bookstore.user.service;

import com.bookstore.user.dto.AuthResponse;
import com.bookstore.user.dto.LoginRequest;
import com.bookstore.user.dto.RegisterRequest;
import com.bookstore.user.dto.UserResponse;
import com.bookstore.user.model.Role;
import com.bookstore.user.model.User;
import com.bookstore.user.repository.UserRepository;
import com.bookstore.user.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository repository;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    public UserResponse register(RegisterRequest request) {
        String username = request.username().strip(); // String.strip() - Java 11
        if (repository.existsByUsername(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken");
        }
        User saved = repository.save(new User(username, encoder.encode(request.password()), Role.USER));
        log.info("New user registered: {}", saved.getUsername());
        return UserResponse.from(saved);
    }

    public AuthResponse login(LoginRequest request) {
        // Optional + lambdas (Java 8)
        User user = repository.findByUsername(request.username())
                .filter(u -> encoder.matches(request.password(), u.getPassword()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));

        log.info("User logged in: {} ({})", user.getUsername(), user.getRole());
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }

    public List<UserResponse> findAll() {
        // Streams + method reference (Java 8), Stream.toList() (Java 16)
        return repository.findAll().stream().map(UserResponse::from).toList();
    }
}
