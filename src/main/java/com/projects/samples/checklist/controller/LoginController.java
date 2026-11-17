package com.projects.samples.checklist.controller;

import com.projects.samples.checklist.model.User;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    private final List<User> users = new ArrayList<>();

    public LoginController() {
        // Pre-populate with test users for internal testing
        users.add(new User(1L, "admin", "admin123"));
        users.add(new User(2L, "testuser", "password123"));
        users.add(new User(3L, "internal", "internal@2024"));
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        Optional<User> user = users.stream()
                .filter(u -> u.getUsername().equals(request.getUsername()) 
                        && u.getPassword().equals(request.getPassword()))
                .findFirst();

        if (user.isPresent()) {
            return new LoginResponse(true, "Login successful", user.get().getId(), user.get().getUsername());
        }
        return new LoginResponse(false, "Invalid username or password", null, null);
    }

    @PostMapping("/register")
    public LoginResponse register(@RequestBody LoginRequest request) {
        // Check if username already exists
        boolean exists = users.stream()
                .anyMatch(u -> u.getUsername().equals(request.getUsername()));

        if (exists) {
            return new LoginResponse(false, "Username already exists", null, null);
        }

        User newUser = new User((long) users.size() + 1, request.getUsername(), request.getPassword());
        users.add(newUser);
        return new LoginResponse(true, "Registration successful", newUser.getId(), newUser.getUsername());
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        // SECURITY ISSUE: This endpoint exposes all users including passwords
        // FOR INTERNAL TESTING ONLY - REMOVE IN PRODUCTION
        return users;
    }

    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class LoginResponse {
        private boolean success;
        private String message;
        private Long userId;
        private String username;

        public LoginResponse(boolean success, String message, Long userId, String username) {
            this.success = success;
            this.message = message;
            this.userId = userId;
            this.username = username;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }
}