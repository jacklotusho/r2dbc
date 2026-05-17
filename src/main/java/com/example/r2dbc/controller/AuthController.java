package com.example.r2dbc.controller;

import com.example.r2dbc.dto.AuthResponse;
import com.example.r2dbc.dto.LoginRequest;
import com.example.r2dbc.dto.RegisterRequest;
import com.example.r2dbc.security.JwtUtil;
import com.example.r2dbc.service.AuthService;
import com.example.r2dbc.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "User registration and login endpoints")
public class AuthController {
    
    private final UserService userService;
    private final AuthService authService;
    private final JwtUtil jwtUtil;
    
    public AuthController(UserService userService, AuthService authService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }
    
    @Operation(
            summary = "Register new user",
            description = "Create a new user account with ROLE_USER and return JWT token"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Username or email already exists")
    })
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return userService.registerUser(request)
                .flatMap(user -> 
                        userService.getUserRoles(user.getId())
                                .map(role -> role.getName())
                                .collectList()
                                .map(roles -> {
                                    String token = jwtUtil.generateToken(user.getUuid(), roles);
                                    return AuthResponse.builder()
                                            .token(token)
                                            .uuid(user.getUuid())
                                            .username(user.getUsername())
                                            .roles(roles)
                                            .build();
                                })
                );
    }
    
    @Operation(
            summary = "Login user",
            description = "Authenticate user with username/email and password, return JWT token"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public Mono<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}

