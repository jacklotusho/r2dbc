package com.example.r2dbc.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "User profile information")
public class UserProfileResponse {
    
    @Schema(description = "User UUID (business identifier)", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID uuid;
    
    @Schema(description = "Username", example = "john_doe")
    private String username;
    
    @Schema(description = "Email address", example = "john@example.com")
    private String email;
    
    @Schema(description = "User roles", example = "[\"ROLE_USER\", \"ROLE_ADMIN\"]")
    private List<String> roles;
    
    @Schema(description = "Account creation timestamp", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
    
    public UserProfileResponse() {
    }
    
    public UserProfileResponse(UUID uuid, String username, String email, List<String> roles, LocalDateTime createdAt) {
        this.uuid = uuid;
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.createdAt = createdAt;
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
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public List<String> getRoles() {
        return roles;
    }
    
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private UUID uuid;
        private String username;
        private String email;
        private List<String> roles;
        private LocalDateTime createdAt;
        
        public Builder uuid(UUID uuid) {
            this.uuid = uuid;
            return this;
        }
        
        public Builder username(String username) {
            this.username = username;
            return this;
        }
        
        public Builder email(String email) {
            this.email = email;
            return this;
        }
        
        public Builder roles(List<String> roles) {
            this.roles = roles;
            return this;
        }
        
        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        public UserProfileResponse build() {
            return new UserProfileResponse(uuid, username, email, roles, createdAt);
        }
    }
}

