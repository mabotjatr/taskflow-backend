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
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Authentication", description = "User authentication and registration endpoints")
public class AuthResource {

    @Inject
    AuthService authService;

    // User Registration Endpoint
    @POST
    @Path("/register")
    @Transactional
    @Operation(summary = "Register a new user", description = "Creates a new user account and returns a JWT token")
    @APIResponses({
            @APIResponse(
                    responseCode = "201",
                    description = "User created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class))),
            @APIResponse(
                    responseCode = "400",
                    description = "Invalid input data"),
            @APIResponse(
                    responseCode = "409",
                    description = "Username already exists")
    })
    public Response register(@RequestBody(description = "User registration data", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthRequest.class))) AuthRequest request) {
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
    @Operation(summary = "Authenticate user",description = "Login with username and password to receive JWT token")
    @APIResponses({@APIResponse(responseCode = "200",description = "Login successful",content = @Content(mediaType = "application/json",schema = @Schema(implementation = AuthResponse.class))),
            @APIResponse(responseCode = "401",description = "Invalid credentials")})
    public Response login( @RequestBody( description = "User login credentials",required = true,content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthRequest.class)))AuthRequest request) {
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