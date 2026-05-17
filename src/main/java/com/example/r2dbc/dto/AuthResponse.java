package com.example.r2dbc.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Schema(description = "Authentication response with JWT token")
public class AuthResponse {
    
    @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;
    
    @Schema(description = "User UUID (business identifier)", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID uuid;
    
    @Schema(description = "Username", example = "john_doe")
    private String username;
    
    @Schema(description = "User roles", example = "[\"ROLE_USER\"]")
    private List<String> roles;
    
    public AuthResponse() {
    }
    
    public AuthResponse(String token, UUID uuid, String username, List<String> roles) {
        this.token = token;
        this.uuid = uuid;
        this.username = username;
        this.roles = roles;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public UUID getUuid() {
        return uuid;
    }
    
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public List<String> getRoles() {
        return roles;
    }
    
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String token;
        private UUID uuid;
        private String username;
        private List<String> roles;
        
        public Builder token(String token) {
            this.token = token;
            return this;
        }
        
        public Builder uuid(UUID uuid) {
            this.uuid = uuid;
            return this;
        }
        
        public Builder username(String username) {
            this.username = username;
            return this;
        }
        
        public Builder roles(List<String> roles) {
            this.roles = roles;
            return this;
        }
        
        public AuthResponse build() {
            return new AuthResponse(token, uuid, username, roles);
        }
    }
}

