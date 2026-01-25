package com.mabotjatr.taskflow.resource;

import com.mabotjatr.taskflow.dto.AuthRequest;
import com.mabotjatr.taskflow.dto.AuthResponse;
import com.mabotjatr.taskflow.model.User;
import com.mabotjatr.taskflow.service.AuthService;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    AuthService authService;

    // User Registration Endpoint
    @POST
    @Path("/register")
    @Transactional // This annotation is CRITICAL for database writes in Quarkus
    public Response register(AuthRequest request) {
        // 1. Basic Validation
        if (request.username == null || request.password == null || request.email == null) {
            throw new WebApplicationException("Username, email, and password are required", 400);
        }

        if (User.findByUsername(request.username) != null) {
            throw new WebApplicationException("Username already exists", 409); // 409 Conflict
        }

        //Create and save the new user
        User newUser = new User();
        newUser.username = request.username;
        newUser.email = request.email;
        //Hash the password before storing
        newUser.passwordHash = authService.hashPassword(request.password);
        newUser.persist(); // Saves to database

        String token = authService.generateTokenForUser(newUser);

        System.out.println("************************ The user has been registered successfully, token : " + token);

        return Response.ok(new AuthResponse(token, newUser.username))
                .status(Response.Status.CREATED) // HTTP 201
                .build();
    }

    // User Login Endpoint
    @POST
    @Path("/login")
    public Response login(AuthRequest request) {
        // 1. Basic Validation
        if (request.username == null || request.password == null) {
            throw new WebApplicationException("Username and password are required", 400);
        }

        User user = User.findByUsername(request.username);
        if (user == null) {
            throw new WebApplicationException("Invalid credentials", 401);
        }

        if (!authService.verifyPassword(request.password, user.passwordHash)) {
            throw new WebApplicationException("Invalid credentials", 401);
        }

        String token = authService.generateTokenForUser(user);

        System.out.println("************************ The user has been authenticated successfully, token : " + token);
        return Response.ok(new AuthResponse(token, user.username)).build();
    }
}