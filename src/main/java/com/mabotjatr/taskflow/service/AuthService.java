package com.mabotjatr.taskflow.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.mabotjatr.taskflow.model.User;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
public class AuthService {

    public String hashPassword(String plainPassword) {
        return BCrypt.withDefaults().hashToString(12, plainPassword.toCharArray());
    }

    public boolean verifyPassword(String plainPassword, String storedHash) {
        return BCrypt.verifyer().verify(plainPassword.toCharArray(), storedHash).verified;
    }

    public String generateTokenForUser(User user) {
        try {
            System.out.println("**************** DEBUG: Generating token for user: " + user.username);
            System.out.println("**************** DEBUG: User roles: " + user.getRolesList());

            return Jwt.issuer("https://taskflow-app.com")
                    .subject(user.username)
                    .upn(user.username)
                    .groups(user.getRolesList() == null
                                    ? Set.of("USER")
                                    : new HashSet<>(user.getRolesList())
                    )
                    .expiresIn(Duration.ofHours(24))
                    .sign();

        } catch (Exception e) {
            System.err.println("*********************DEBUG: JWT generation failed: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}