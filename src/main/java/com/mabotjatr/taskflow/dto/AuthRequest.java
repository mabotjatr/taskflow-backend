package com.mabotjatr.taskflow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Authentication request data")
public class AuthRequest {// possibly make use of  records

    @Schema(description = "Username for registration/login", example = "john_doe", required = true)
    public String username;

    @Schema(description = "Email address (for registration only)", example = "john@example.com")
    public String email;

    @Schema(description = "Password", example = "securePassword123", required = true, minLength = 6)
    public String password;
}