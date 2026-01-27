package com.mabotjatr.taskflow.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.mabotjatr.taskflow.model.User;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
public class AuthService {

    private final String BASE_URL = "https://taskflow-app.com";
    private final Logger logger;

    @Inject
    public AuthService(Logger logger)
    {
        this.logger = logger;
    }


    public String hashPassword(String plainPassword) {
        return BCrypt.withDefaults().hashToString(12, plainPassword.toCharArray());
    }

    public boolean verifyPassword(String plainPassword, String storedHash) {
        return BCrypt.verifyer().verify(plainPassword.toCharArray(), storedHash).verified;
    }

    public String generateTokenForUser(User user) {
        try {
            logger.info("**************** DEBUG: Generating token for user: " + user.username);
            logger.info("**************** DEBUG: User roles: " + user.getRolesList());

            return Jwt.issuer(BASE_URL)
                    .subject(user.username)
                    .upn(user.username)
                    .groups(user.getRolesList() == null
                                    ? Set.of("USER")
                                    : new HashSet<>(user.getRolesList())
                    )
                    .expiresIn(Duration.ofHours(24))
                    .sign();

        } catch (Exception e) {
            logger.error("*********************DEBUG: JWT generation failed: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}