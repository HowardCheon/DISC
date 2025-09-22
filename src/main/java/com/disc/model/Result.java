package com.disc.model;

import java.sql.Timestamp;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

/**
 * Result model class for storing test results
 */
public class Result {
    
    private Long id;
    private Long testLinkId;
    private Integer dScore;
    private Integer iScore;
    private Integer sScore;
    private Integer cScore;
    private String resultType;
    private Timestamp createdAt;
    
    // Associated test link object (for joins)
    private TestLink testLink;
    
    // Constructors
    public Result() {
        this.dScore = 0;
        this.iScore = 0;
        this.sScore = 0;
        this.cScore = 0;
    }
    
    public Result(Long testLinkId, Integer dScore, Integer iScore, Integer sScore, Integer cScore) {
        this.testLinkId = testLinkId;
        this.dScore = dScore != null ? dScore : 0;
        this.iScore = iScore != null ? iScore : 0;
        this.sScore = sScore != null ? sScore : 0;
        this.cScore = cScore != null ? cScore : 0;
        this.resultType = calculateResultType();
    }
    
    public Result(Long id, Long testLinkId, Integer dScore, Integer iScore, 
                 Integer sScore, Integer cScore, String resultType, Timestamp createdAt) {
        this.id = id;
        this.testLinkId = testLinkId;
        this.dScore = dScore != null ? dScore : 0;
        this.iScore = iScore != null ? iScore : 0;
        this.sScore = sScore != null ? sScore : 0;
        this.cScore = cScore != null ? cScore : 0;
        this.resultType = resultType;
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
    
    public Integer getDScore() {
        return dScore;
    }
    
    public void setDScore(Integer dScore) {
        this.dScore = dScore != null ? dScore : 0;
        this.resultType = calculateResultType();
    }
    
    public Integer getIScore() {
        return iScore;
    }
    
    public void setIScore(Integer iScore) {
        this.iScore = iScore != null ? iScore : 0;
        this.resultType = calculateResultType();
    }
    
    public Integer getSScore() {
        return sScore;
    }
    
    public void setSScore(Integer sScore) {
        this.sScore = sScore != null ? sScore : 0;
        this.resultType = calculateResultType();
    }
    
    public Integer getCScore() {
        return cScore;
    }
    
    public void setCScore(Integer cScore) {
        this.cScore = cScore != null ? cScore : 0;
        this.resultType = calculateResultType();
    }
    
    public String getResultType() {
        return resultType;
    }
    
    public void setResultType(String resultType) {
        this.resultType = resultType;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    public TestLink getTestLink() {
        return testLink;
    }
    
    public void setTestLink(TestLink testLink) {
        this.testLink = testLink;
    }
    
    // Helper methods
    public String calculateResultType() {
        int maxScore = Math.max(Math.max(dScore, iScore), Math.max(sScore, cScore));
        
        if (dScore == maxScore) return "D";
        else if (iScore == maxScore) return "I";
        else if (sScore == maxScore) return "S";
        else return "C";
    }
    
    public int getTotalScore() {
        return dScore + iScore + sScore + cScore;
    }
    
    public double getDPercentage() {
        int total = getTotalScore();
        return total > 0 ? (double) dScore / total * 100 : 0;
    }
    
    public double getIPercentage() {
        int total = getTotalScore();
        return total > 0 ? (double) iScore / total * 100 : 0;
    }
    
    public double getSPercentage() {
        int total = getTotalScore();
        return total > 0 ? (double) sScore / total * 100 : 0;
    }
    
    public double getCPercentage() {
        int total = getTotalScore();
        return total > 0 ? (double) cScore / total * 100 : 0;
    }
    
    public boolean isValid() {
        return testLinkId != null && 
               dScore != null && dScore >= 0 &&
               iScore != null && iScore >= 0 &&
               sScore != null && sScore >= 0 &&
               cScore != null && cScore >= 0;
    }
    
    /**
     * Get scores as a map for easy access
     * 
     * @return Map of DISC type to score
     */
    public Map<String, Integer> getScoresAsMap() {
        Map<String, Integer> scores = new HashMap<>();
        scores.put("D", dScore);
        scores.put("I", iScore);
        scores.put("S", sScore);
        scores.put("C", cScore);
        return scores;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Result result = (Result) obj;
        return Objects.equals(id, result.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Result{" +
               "id=" + id +
               ", testLinkId=" + testLinkId +
               ", dScore=" + dScore +
               ", iScore=" + iScore +
               ", sScore=" + sScore +
               ", cScore=" + cScore +
               ", resultType='" + resultType + '\'' +
               ", createdAt=" + createdAt +
               '}';
    }
}