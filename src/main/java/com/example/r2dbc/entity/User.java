package com.example.r2dbc.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("users")
public class User {
    
    @Id
    private Long id;
    
    @Column("uuid")
    private UUID uuid;
    
    @Column("username")
    private String username;
    
    @Column("email")
    private String email;
    
    @Column("password_hash")
    private String passwordHash;
    
    @Column("enabled")
    private boolean enabled;
    
    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("auth_provider")
    private String authProvider = "LOCAL";
    
    public User() {
    }
    
    public User(Long id, UUID uuid, String username, String email, String passwordHash, boolean enabled, LocalDateTime createdAt, String authProvider) {
        this.id = id;
        this.uuid = uuid;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.enabled = enabled;
        this.createdAt = createdAt;
        this.authProvider = authProvider != null ? authProvider : "LOCAL";
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getAuthProvider() {
        return authProvider;
    }

    public void setAuthProvider(String authProvider) {
        this.authProvider = authProvider;
    }

    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private Long id;
        private UUID uuid;
        private String username;
        private String email;
        private String passwordHash;
        private boolean enabled;
        private LocalDateTime createdAt;
        private String authProvider = "LOCAL";

        public Builder id(Long id) {
            this.id = id;
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
        
        public Builder email(String email) {
            this.email = email;
            return this;
        }
        
        public Builder passwordHash(String passwordHash) {
            this.passwordHash = passwordHash;
            return this;
        }
        
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }
        
        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder authProvider(String authProvider) {
            this.authProvider = authProvider;
            return this;
        }

        public User build() {
            return new User(id, uuid, username, email, passwordHash, enabled, createdAt, authProvider);
        }
    }
}

