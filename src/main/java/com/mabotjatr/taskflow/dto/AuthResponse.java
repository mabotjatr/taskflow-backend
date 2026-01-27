package com.mabotjatr.taskflow.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Authentication response with JWT token")
public class AuthResponse { // possibly make use of  records

    @Schema(description = "JWT token for authentication", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    public String token;

    @Schema(description = "Authenticated username", example = "john_doe")
    public String username;

    public AuthResponse() {}
    public AuthResponse(String token, String username) {
        this.token = token;
        this.username = username;
    }
}