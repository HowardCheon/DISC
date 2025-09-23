package com.disc.model;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * Admin model class for administrator accounts
 */
public class Admin {
    
    private Long id;
    private String username;
    private String passwordHash;
    private Timestamp lastLoginAt;
    private Timestamp createdAt;
    
    // Constructors
    public Admin() {}
    
    public Admin(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }
    
    public Admin(Long id, String username, String passwordHash, 
                Timestamp lastLoginAt, Timestamp createdAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.lastLoginAt = lastLoginAt;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public Timestamp getLastLoginAt() {
        return lastLoginAt;
    }
    
    public void setLastLoginAt(Timestamp lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    // Helper methods
    public boolean isValid() {
        return username != null && !username.trim().isEmpty() &&
               passwordHash != null && !passwordHash.trim().isEmpty();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Admin admin = (Admin) obj;
        return Objects.equals(id, admin.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Admin{" +
               "id=" + id +
               ", username='" + username + '\'' +
               ", lastLoginAt=" + lastLoginAt +
               ", createdAt=" + createdAt +
               '}';
    }
}