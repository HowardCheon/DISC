package com.disc.model;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * User model class for DISC assessment test takers
 */
public class User {
    
    private Long id;
    private String name;
    private String nameHash;
    private Timestamp createdAt;
    
    // Constructors
    public User() {}
    
    public User(String name, String nameHash) {
        this.name = name;
        this.nameHash = nameHash;
    }
    
    public User(Long id, String name, String nameHash, Timestamp createdAt) {
        this.id = id;
        this.name = name;
        this.nameHash = nameHash;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getNameHash() {
        return nameHash;
    }
    
    public void setNameHash(String nameHash) {
        this.nameHash = nameHash;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return Objects.equals(id, user.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "User{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", nameHash='" + nameHash + '\'' +
               ", createdAt=" + createdAt +
               '}';
    }
}