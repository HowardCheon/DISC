package com.disc.model;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * Answer model class for storing test responses
 */
public class Answer {
    
    private Long id;
    private Long testLinkId;
    private Integer questionNum;
    private String mostLike;
    private String leastLike;
    private Timestamp createdAt;
    
    // Constructors
    public Answer() {}
    
    public Answer(Long testLinkId, Integer questionNum, String mostLike, String leastLike) {
        this.testLinkId = testLinkId;
        this.questionNum = questionNum;
        this.mostLike = mostLike;
        this.leastLike = leastLike;
    }
    
    public Answer(Long id, Long testLinkId, Integer questionNum, 
                 String mostLike, String leastLike, Timestamp createdAt) {
        this.id = id;
        this.testLinkId = testLinkId;
        this.questionNum = questionNum;
        this.mostLike = mostLike;
        this.leastLike = leastLike;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getTestLinkId() {
        return testLinkId;
    }
    
    public void setTestLinkId(Long testLinkId) {
        this.testLinkId = testLinkId;
    }
    
    public Integer getQuestionNum() {
        return questionNum;
    }
    
    public void setQuestionNum(Integer questionNum) {
        this.questionNum = questionNum;
    }
    
    public String getMostLike() {
        return mostLike;
    }
    
    public void setMostLike(String mostLike) {
        this.mostLike = mostLike;
    }
    
    public String getLeastLike() {
        return leastLike;
    }
    
    public void setLeastLike(String leastLike) {
        this.leastLike = leastLike;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    // Helper methods
    public boolean isValid() {
        return testLinkId != null && 
               questionNum != null && questionNum > 0 && questionNum <= 28 &&
               mostLike != null && !mostLike.trim().isEmpty() &&
               leastLike != null && !leastLike.trim().isEmpty() &&
               !mostLike.equals(leastLike);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Answer answer = (Answer) obj;
        return Objects.equals(id, answer.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Answer{" +
               "id=" + id +
               ", testLinkId=" + testLinkId +
               ", questionNum=" + questionNum +
               ", mostLike='" + mostLike + '\'' +
               ", leastLike='" + leastLike + '\'' +
               ", createdAt=" + createdAt +
               '}';
    }
}