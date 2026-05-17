package com.example.r2dbc.controller;

import com.example.r2dbc.dto.UserProfileResponse;
import com.example.r2dbc.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @Operation(
            summary = "Get current user profile",
            description = "Returns the profile of the currently authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User profile retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserProfileResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    })
    @GetMapping("/me")
    public Mono<UserProfileResponse> getCurrentUser(Authentication authentication) {
        UUID userUuid = (UUID) authentication.getPrincipal();
        
        return userService.findByUuid(userUuid)
                .flatMap(user -> 
                        userService.getUserRoles(user.getId())
                                .map(role -> role.getName())
                                .collectList()
                                .map(roles -> UserProfileResponse.builder()
                                        .uuid(user.getUuid())
                                        .username(user.getUsername())
                                        .email(user.getEmail())
                                        .roles(roles)
                                        .createdAt(user.getCreatedAt())
                                        .build())
                );
    }
    
    @Operation(
            summary = "Get all users",
            description = "Returns a list of all users (Admin only)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Users retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserProfileResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ROLE_ADMIN")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Flux<UserProfileResponse> getAllUsers() {
        return userService.getAllUsers()
                .flatMap(user -> 
                        userService.getUserRoles(user.getId())
                                .map(role -> role.getName())
                                .collectList()
                                .map(roles -> UserProfileResponse.builder()
                                        .uuid(user.getUuid())
                                        .username(user.getUsername())
                                        .email(user.getEmail())
                                        .roles(roles)
                                        .createdAt(user.getCreatedAt())
                                        .build())
                );
    }
}

