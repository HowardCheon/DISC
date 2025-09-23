package com.disc.model;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * TestLink model class for managing test URLs and status
 */
public class TestLink {
    
    public enum Status {
        검사전("검사전"),
        검사중("검사중"),
        검사완료("검사완료");
        
        private final String value;
        
        Status(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        public static Status fromString(String value) {
            for (Status status : Status.values()) {
                if (status.value.equals(value)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Invalid status: " + value);
        }
    }
    
    private Long id;
    private Long userId;
    private String testUrl;
    private Status status;
    private Timestamp startedAt;
    private Timestamp completedAt;
    private Timestamp createdAt;
    
    // Associated user object (for joins)
    private User user;
    
    // Constructors
    public TestLink() {
        this.status = Status.검사전;
    }
    
    public TestLink(Long userId, String testUrl) {
        this.userId = userId;
        this.testUrl = testUrl;
        this.status = Status.검사전;
    }
    
    public TestLink(Long id, Long userId, String testUrl, Status status, 
                   Timestamp startedAt, Timestamp completedAt, Timestamp createdAt) {
        this.id = id;
        this.userId = userId;
        this.testUrl = testUrl;
        this.status = status;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getTestUrl() {
        return testUrl;
    }
    
    public void setTestUrl(String testUrl) {
        this.testUrl = testUrl;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public String getStatusValue() {
        return status != null ? status.getValue() : null;
    }
    
    public void setStatusValue(String statusValue) {
        this.status = statusValue != null ? Status.fromString(statusValue) : Status.검사전;
    }
    
    public Timestamp getStartedAt() {
        return startedAt;
    }
    
    public void setStartedAt(Timestamp startedAt) {
        this.startedAt = startedAt;
    }
    
    public Timestamp getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(Timestamp completedAt) {
        this.completedAt = completedAt;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    // Helper methods
    public boolean isStarted() {
        return status == Status.검사중 || status == Status.검사완료;
    }
    
    public boolean isCompleted() {
        return status == Status.검사완료;
    }
    
    public boolean canStart() {
        return status == Status.검사전;
    }
    
    public boolean canComplete() {
        return status == Status.검사중;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TestLink testLink = (TestLink) obj;
        return Objects.equals(id, testLink.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "TestLink{" +
               "id=" + id +
               ", userId=" + userId +
               ", testUrl='" + testUrl + '\'' +
               ", status=" + status +
               ", startedAt=" + startedAt +
               ", completedAt=" + completedAt +
               ", createdAt=" + createdAt +
               '}';
    }
}