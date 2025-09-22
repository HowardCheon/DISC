package com.disc.util;

import com.disc.dao.AnswerDAO;
import com.disc.model.Answer;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Utility class for calculating DISC personality assessment results
 * 
 * DISC Scoring Rules:
 * - Most Like: +2 points
 * - Least Like: 0 points  
 * - Unselected (other 2 options): +1 point each
 * 
 * Each question has 4 options (D, I, S, C), so total points per question = 4
 * With 28 questions, maximum possible score per type = 28 * 2 = 56 points
 * Minimum possible score per type = 0 points
 */
public class ResultCalculator {
    
    private static final Logger logger = Logger.getLogger(ResultCalculator.class.getName());
    
    private AnswerDAO answerDAO;
    
    // DISC types
    private static final String[] DISC_TYPES = {"D", "I", "S", "C"};
    
    // Points for each selection type
    private static final int MOST_LIKE_POINTS = 2;
    private static final int LEAST_LIKE_POINTS = 0;
    private static final int UNSELECTED_POINTS = 1;
    
    public ResultCalculator() {
        this.answerDAO = new AnswerDAO();
    }
    
    /**
     * Calculate DISC scores for a given test link
     * 
     * @param testLinkId The test link ID to calculate scores for
     * @return Map of DISC type to calculated score
     * @throws SQLException if database operation fails
     */
    public Map<String, Integer> calculateScores(Long testLinkId) throws SQLException {
        if (testLinkId == null) {
            throw new IllegalArgumentException("Test link ID cannot be null");
        }
        
        logger.info("Calculating DISC scores for testLinkId: " + testLinkId);
        
        // Get all answers for this test
        List<Answer> answers = answerDAO.getAnswersByTestLinkId(testLinkId);
        
        if (answers.isEmpty()) {
            throw new SQLException("No answers found for testLinkId: " + testLinkId);
        }
        
        if (answers.size() != 28) {
            logger.warning("Expected 28 answers but found " + answers.size() + " for testLinkId: " + testLinkId);
        }
        
        // Initialize scores
        Map<String, Integer> scores = new HashMap<>();
        for (String type : DISC_TYPES) {
            scores.put(type, 0);
        }
        
        // Calculate scores for each answer
        for (Answer answer : answers) {
            Map<String, Integer> questionScores = calculateQuestionScores(answer);
            
            // Add to total scores
            for (String type : DISC_TYPES) {
                scores.put(type, scores.get(type) + questionScores.get(type));
            }
            
            logger.fine("Question " + answer.getQuestionNum() + " scores: " + questionScores);
        }
        
        // Validate scores
        validateScores(scores, answers.size());
        
        logger.info("Final DISC scores for testLinkId " + testLinkId + ": " + scores);
        
        return scores;
    }
    
    /**
     * Calculate scores for a single question
     * 
     * @param answer The answer for one question
     * @return Map of DISC type to score for this question
     */
    private Map<String, Integer> calculateQuestionScores(Answer answer) {
        Map<String, Integer> questionScores = new HashMap<>();
        
        // Initialize all types with unselected points
        for (String type : DISC_TYPES) {
            questionScores.put(type, UNSELECTED_POINTS);
        }
        
        // Validate answer
        if (answer.getMostLike() == null || answer.getLeastLike() == null) {
            throw new IllegalArgumentException("Answer must have both mostLike and leastLike values");
        }
        
        if (answer.getMostLike().equals(answer.getLeastLike())) {
            throw new IllegalArgumentException("MostLike and leastLike cannot be the same");
        }
        
        // Apply most like points
        String mostLike = answer.getMostLike().toUpperCase();
        if (isValidDiscType(mostLike)) {
            questionScores.put(mostLike, MOST_LIKE_POINTS);
        } else {
            logger.warning("Invalid mostLike value: " + mostLike + " for question " + answer.getQuestionNum());
        }
        
        // Apply least like points
        String leastLike = answer.getLeastLike().toUpperCase();
        if (isValidDiscType(leastLike)) {
            questionScores.put(leastLike, LEAST_LIKE_POINTS);
        } else {
            logger.warning("Invalid leastLike value: " + leastLike + " for question " + answer.getQuestionNum());
        }
        
        return questionScores;
    }
    
    /**
     * Validate if a string is a valid DISC type
     * 
     * @param type The type to validate
     * @return true if valid DISC type, false otherwise
     */
    private boolean isValidDiscType(String type) {
        if (type == null) return false;
        
        for (String validType : DISC_TYPES) {
            if (validType.equals(type.toUpperCase())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Validate calculated scores for consistency
     * 
     * @param scores The calculated scores
     * @param questionCount The number of questions answered
     */
    private void validateScores(Map<String, Integer> scores, int questionCount) {
        // Calculate total points
        int totalPoints = scores.values().stream().mapToInt(Integer::intValue).sum();
        int expectedTotal = questionCount * 4; // Each question distributes 4 points total
        
        if (totalPoints != expectedTotal) {
            logger.warning("Score validation failed: expected total " + expectedTotal + 
                          " but got " + totalPoints + " for " + questionCount + " questions");
        }
        
        // Check individual score ranges
        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            String type = entry.getKey();
            Integer score = entry.getValue();
            
            int minPossible = 0; // All least-like
            int maxPossible = questionCount * 2; // All most-like
            
            if (score < minPossible || score > maxPossible) {
                logger.warning("Score out of range for type " + type + ": " + score + 
                              " (expected " + minPossible + "-" + maxPossible + ")");
            }
        }
    }
    
    /**
     * Calculate percentage scores based on raw scores
     * 
     * @param scores Raw DISC scores
     * @return Map of DISC type to percentage
     */
    public Map<String, Double> calculatePercentages(Map<String, Integer> scores) {
        Map<String, Double> percentages = new HashMap<>();
        
        int totalScore = scores.values().stream().mapToInt(Integer::intValue).sum();
        
        if (totalScore == 0) {
            // All percentages are 0 if no total score
            for (String type : DISC_TYPES) {
                percentages.put(type, 0.0);
            }
        } else {
            for (String type : DISC_TYPES) {
                double percentage = (scores.get(type).doubleValue() / totalScore) * 100.0;
                percentages.put(type, Math.round(percentage * 10.0) / 10.0); // Round to 1 decimal
            }
        }
        
        return percentages;
    }
    
    /**
     * Determine the primary DISC type based on scores
     * 
     * @param scores DISC scores
     * @return Primary DISC type (D, I, S, or C)
     */
    public String determinePrimaryType(Map<String, Integer> scores) {
        String primaryType = "D"; // Default
        int maxScore = scores.get("D");
        
        for (String type : DISC_TYPES) {
            if (scores.get(type) > maxScore) {
                maxScore = scores.get(type);
                primaryType = type;
            }
        }
        
        return primaryType;
    }
    
    /**
     * Get detailed type description
     * 
     * @param primaryType The primary DISC type
     * @return Type description
     */
    public String getTypeDescription(String primaryType) {
        switch (primaryType.toUpperCase()) {
            case "D":
                return "주도형 (Dominance) - 도전적이고 결단력이 있으며, 목표 달성을 위해 강력하게 추진하는 유형";
            case "I":
                return "사교형 (Influence) - 사람들과의 관계를 중시하고 긍정적이며 활발한 유형";
            case "S":
                return "안정형 (Steadiness) - 안정성과 조화를 중시하며 꾸준하고 신뢰할 수 있는 유형";
            case "C":
                return "신중형 (Conscientiousness) - 정확성과 품질을 중시하며 체계적이고 분석적인 유형";
            default:
                return "알 수 없는 유형";
        }
    }
    
    /**
     * Calculate detailed result with all information
     * 
     * @param testLinkId Test link ID
     * @return Detailed result map
     * @throws SQLException if calculation fails
     */
    public Map<String, Object> calculateDetailedResult(Long testLinkId) throws SQLException {
        Map<String, Object> result = new HashMap<>();
        
        // Calculate raw scores
        Map<String, Integer> scores = calculateScores(testLinkId);
        
        // Calculate percentages
        Map<String, Double> percentages = calculatePercentages(scores);
        
        // Determine primary type
        String primaryType = determinePrimaryType(scores);
        
        // Get type description
        String typeDescription = getTypeDescription(primaryType);
        
        // Assemble result
        result.put("scores", scores);
        result.put("percentages", percentages);
        result.put("primaryType", primaryType);
        result.put("typeDescription", typeDescription);
        result.put("testLinkId", testLinkId);
        
        return result;
    }
    
    /**
     * Recalculate scores for a test (utility method for data correction)
     * 
     * @param testLinkId Test link ID to recalculate
     * @return Updated scores
     * @throws SQLException if operation fails
     */
    public Map<String, Integer> recalculateScores(Long testLinkId) throws SQLException {
        logger.info("Recalculating scores for testLinkId: " + testLinkId);
        
        Map<String, Integer> newScores = calculateScores(testLinkId);
        
        // Note: This method only calculates, doesn't save to database
        // Use ResultDAO to save the updated scores if needed
        
        return newScores;
    }
    
    /**
     * Get scoring statistics for analysis
     * 
     * @param testLinkId Test link ID
     * @return Statistics map
     * @throws SQLException if operation fails
     */
    public Map<String, Object> getScoringStatistics(Long testLinkId) throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        
        List<Answer> answers = answerDAO.getAnswersByTestLinkId(testLinkId);
        Map<String, Integer> scores = calculateScores(testLinkId);
        
        // Basic stats
        stats.put("totalQuestions", answers.size());
        stats.put("totalPossibleScore", answers.size() * 2);
        stats.put("actualTotalScore", scores.values().stream().mapToInt(Integer::intValue).sum());
        
        // Type distribution
        Map<String, Integer> mostLikeCount = new HashMap<>();
        Map<String, Integer> leastLikeCount = new HashMap<>();
        
        for (String type : DISC_TYPES) {
            mostLikeCount.put(type, 0);
            leastLikeCount.put(type, 0);
        }
        
        for (Answer answer : answers) {
            String mostLike = answer.getMostLike().toUpperCase();
            String leastLike = answer.getLeastLike().toUpperCase();
            
            mostLikeCount.put(mostLike, mostLikeCount.get(mostLike) + 1);
            leastLikeCount.put(leastLike, leastLikeCount.get(leastLike) + 1);
        }
        
        stats.put("mostLikeDistribution", mostLikeCount);
        stats.put("leastLikeDistribution", leastLikeCount);
        
        return stats;
    }
}