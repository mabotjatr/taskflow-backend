package com.mabotjatr.taskflow.util;

import io.smallrye.jwt.build.Jwt;
import java.util.Set;

public class JWTCreator {

    /**
     *  This method create JWT token for test
     * @param username the login username
     * @return a token as a string
     */
    public static String createTestToken(String username) {
        return Jwt.issuer("https:localhost:8080")
                .upn(username)
                .groups(Set.of("USER"))
                .sign();
    }
}