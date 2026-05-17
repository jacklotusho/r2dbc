package com.example.r2dbc.entity;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("user_roles")
public class UserRole {
    
    @Column("user_id")
    private Long userId;
    
    @Column("role_id")
    private Long roleId;
    
    public UserRole() {
    }
    
    public UserRole(Long userId, Long roleId) {
        this.userId = userId;
        this.roleId = roleId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Long getRoleId() {
        return roleId;
    }
    
    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private Long userId;
        private Long roleId;
        
        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }
        
        public Builder roleId(Long roleId) {
            this.roleId = roleId;
            return this;
        }
        
        public UserRole build() {
            return new UserRole(userId, roleId);
        }
    }
}

