package com.mabotjatr.taskflow.service;

import com.mabotjatr.taskflow.model.User;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.jboss.logging.Logger;

import java.util.Set;

@ApplicationScoped
public class JwtUserService implements UserService {

    private final SecurityIdentity securityIdentity;
    private final Logger logger;

    @Inject
    public JwtUserService(SecurityIdentity securityIdentity, Logger logger)
    {
        this.securityIdentity = securityIdentity;
        this.logger = logger;
    }

    /**
     * Gets the current authenticated user from the security context
     *
     * @return the authenticated user
     * @throws WebApplicationException if user is not found or not authenticated
     */
    @Override
    public User getCurrentUser() {
        // Check if user is authenticated
        if (securityIdentity == null || securityIdentity.isAnonymous()) {
            logger.warn("************* Unauthenticated access attempt ******************");
            throw new WebApplicationException("User not authenticated",
                    Response.Status.UNAUTHORIZED);
        }

        String username = securityIdentity.getPrincipal().getName();
        logger.debugf("*************** Looking up user: %s", username);

        User user = User.findByUsername(username);
        if (user == null) {
            logger.errorf("*************** User not found in database: %s", username);
            throw new WebApplicationException("User not found",
                    Response.Status.UNAUTHORIZED);
        }

        logger.infof("*************** User authenticated: %s (ID: %s)", user.username, user.id);
        return user;
    }

    /**
     * Alternative method with additional validation
     *
     * @return the authenticated user with roles
     */
    public AuthenticatedUser getAuthenticatedUser() {
        User user = getCurrentUser();

        return new AuthenticatedUser(
                user,
                securityIdentity.getRoles(),
                securityIdentity.getPrincipal().getName()
        );
    }

    /**
     * Checks if the current user has a specific role
     *
     * @param role the role to check
     * @return true if user has the role
     */
    public boolean hasRole(String role) {
        return securityIdentity.hasRole(role);
    }

    /**
     * Gets the username from the security context without database lookup
     * Useful for logging or when you only need the username
     *
     * @return the username
     */
    public String getUsername() {
        if (securityIdentity == null || securityIdentity.isAnonymous()) {
            return "anonymous";
        }
        return securityIdentity.getPrincipal().getName();
    }

    /**
     * DTO for authenticated user with additional context
     */
    public static class AuthenticatedUser {
        private final User user;
        private final Set<String> roles;
        private final String username;

        public AuthenticatedUser(User user, Set<String> roles, String username) {
            this.user = user;
            this.roles = roles;
            this.username = username;
        }

        public User getUser() { return user; }
        public Set<String> getRoles() { return roles; }
        public String getUsername() { return username; }
        public boolean hasRole(String role) { return roles.contains(role); }
    }
}