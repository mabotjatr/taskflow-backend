package com.mabotjatr.taskflow.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Authentication request data")
public class AuthRequest {

    @Schema(description = "Username for registration/login", example = "john_doe", required = true)
    public String username;

    @Schema(description = "Email address (for registration only)", example = "john@example.com")
    public String email;

    @Schema(description = "Password", example = "securePassword123", required = true, minLength = 6)
    public String password;

    // Constructors, getters, and setters
    public AuthRequest() {}
    public AuthRequest(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}