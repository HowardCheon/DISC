package com.disc.dao;

import com.disc.model.Answer;
import com.disc.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Data Access Object for Answer operations
 */
public class AnswerDAO {
    
    private static final Logger logger = Logger.getLogger(AnswerDAO.class.getName());
    
    /**
     * Create a new answer
     * 
     * @param answer Answer object to create
     * @return The created answer with ID populated
     * @throws SQLException if creation fails
     */
    public Answer createAnswer(Answer answer) throws SQLException {
        if (answer == null || !answer.isValid()) {
            throw new IllegalArgumentException("Invalid answer data");
        }
        
        String sql = """
            INSERT INTO answers (test_link_id, question_num, most_like, least_like, created_at) 
            VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)
            """;
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setLong(1, answer.getTestLinkId());
                pstmt.setInt(2, answer.getQuestionNum());
                pstmt.setString(3, answer.getMostLike());
                pstmt.setString(4, answer.getLeastLike());
                
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Creating answer failed, no rows affected");
                }
                
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        answer.setId(generatedKeys.getLong(1));
                    } else {
                        throw new SQLException("Creating answer failed, no ID obtained");
                    }
                }
                
                return getAnswerById(answer.getId());
            }
        });
    }
    
    /**
     * Save or update answer (upsert operation)
     * 
     * @param answer Answer object to save or update
     * @return The saved/updated answer
     * @throws SQLException if operation fails
     */
    public Answer saveOrUpdateAnswer(Answer answer) throws SQLException {
        if (answer == null || !answer.isValid()) {
            throw new IllegalArgumentException("Invalid answer data");
        }
        
        // Check if answer already exists for this test_link_id and question_num
        Answer existingAnswer = getAnswerByTestLinkAndQuestion(answer.getTestLinkId(), answer.getQuestionNum());
        
        if (existingAnswer != null) {
            // Update existing answer
            existingAnswer.setMostLike(answer.getMostLike());
            existingAnswer.setLeastLike(answer.getLeastLike());
            updateAnswer(existingAnswer);
            return existingAnswer;
        } else {
            // Create new answer
            return createAnswer(answer);
        }
    }
    
    /**
     * Get answer by ID
     * 
     * @param answerId Answer ID
     * @return Answer object or null if not found
     * @throws SQLException if query fails
     */
    public Answer getAnswerById(Long answerId) throws SQLException {
        if (answerId == null) {
            return null;
        }
        
        String sql = """
            SELECT id, test_link_id, question_num, most_like, least_like, created_at 
            FROM answers WHERE id = ?
            """;
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setLong(1, answerId);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToAnswer(rs);
                    }
                    return null;
                }
            }
        });
    }
    
    /**
     * Get answer by test link ID and question number
     * 
     * @param testLinkId Test link ID
     * @param questionNum Question number
     * @return Answer object or null if not found
     * @throws SQLException if query fails
     */
    public Answer getAnswerByTestLinkAndQuestion(Long testLinkId, Integer questionNum) throws SQLException {
        if (testLinkId == null || questionNum == null) {
            return null;
        }
        
        String sql = """
            SELECT id, test_link_id, question_num, most_like, least_like, created_at 
            FROM answers WHERE test_link_id = ? AND question_num = ?
            """;
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setLong(1, testLinkId);
                pstmt.setInt(2, questionNum);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToAnswer(rs);
                    }
                    return null;
                }
            }
        });
    }
    
    /**
     * Get all answers for a test link
     * 
     * @param testLinkId Test link ID
     * @return List of answers for the test link
     * @throws SQLException if query fails
     */
    public List<Answer> getAnswersByTestLinkId(Long testLinkId) throws SQLException {
        if (testLinkId == null) {
            return new ArrayList<>();
        }
        
        String sql = """
            SELECT id, test_link_id, question_num, most_like, least_like, created_at 
            FROM answers 
            WHERE test_link_id = ? 
            ORDER BY question_num
            """;
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setLong(1, testLinkId);
                
                List<Answer> answers = new ArrayList<>();
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        answers.add(mapResultSetToAnswer(rs));
                    }
                }
                return answers;
            }
        });
    }
    
    /**
     * Get answers as a map for easy access by question number
     * 
     * @param testLinkId Test link ID
     * @return Map of question number to Answer object
     * @throws SQLException if query fails
     */
    public Map<Integer, Answer> getAnswersMapByTestLinkId(Long testLinkId) throws SQLException {
        List<Answer> answers = getAnswersByTestLinkId(testLinkId);
        Map<Integer, Answer> answerMap = new HashMap<>();
        
        for (Answer answer : answers) {
            answerMap.put(answer.getQuestionNum(), answer);
        }
        
        return answerMap;
    }
    
    /**
     * Update answer
     * 
     * @param answer Answer object with updated information
     * @return true if update was successful
     * @throws SQLException if update fails
     */
    public boolean updateAnswer(Answer answer) throws SQLException {
        if (answer == null || answer.getId() == null || !answer.isValid()) {
            throw new IllegalArgumentException("Invalid answer data or missing ID");
        }
        
        String sql = """
            UPDATE answers 
            SET most_like = ?, least_like = ? 
            WHERE id = ?
            """;
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, answer.getMostLike());
                pstmt.setString(2, answer.getLeastLike());
                pstmt.setLong(3, answer.getId());
                
                int rowsAffected = pstmt.executeUpdate();
                return rowsAffected > 0;
            }
        });
    }
    
    /**
     * Delete answer by ID
     * 
     * @param answerId Answer ID to delete
     * @return true if deletion was successful
     * @throws SQLException if deletion fails
     */
    public boolean deleteAnswer(Long answerId) throws SQLException {
        if (answerId == null) {
            return false;
        }
        
        String sql = "DELETE FROM answers WHERE id = ?";
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setLong(1, answerId);
                
                int rowsAffected = pstmt.executeUpdate();
                return rowsAffected > 0;
            }
        });
    }
    
    /**
     * Delete all answers for a test link
     * 
     * @param testLinkId Test link ID
     * @return Number of deleted answers
     * @throws SQLException if deletion fails
     */
    public int deleteAnswersByTestLinkId(Long testLinkId) throws SQLException {
        if (testLinkId == null) {
            return 0;
        }
        
        String sql = "DELETE FROM answers WHERE test_link_id = ?";
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setLong(1, testLinkId);
                
                return pstmt.executeUpdate();
            }
        });
    }
    
    /**
     * Get answer count for a test link
     * 
     * @param testLinkId Test link ID
     * @return Number of answers for the test link
     * @throws SQLException if query fails
     */
    public int getAnswerCountByTestLinkId(Long testLinkId) throws SQLException {
        if (testLinkId == null) {
            return 0;
        }
        
        String sql = "SELECT COUNT(*) FROM answers WHERE test_link_id = ?";
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setLong(1, testLinkId);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                    return 0;
                }
            }
        });
    }
    
    /**
     * Check if test is complete (has all 28 answers)
     * 
     * @param testLinkId Test link ID
     * @return true if test has all 28 answers, false otherwise
     * @throws SQLException if query fails
     */
    public boolean isTestComplete(Long testLinkId) throws SQLException {
        return getAnswerCountByTestLinkId(testLinkId) >= 28;
    }
    
    /**
     * Get missing question numbers for a test
     * 
     * @param testLinkId Test link ID
     * @return List of missing question numbers
     * @throws SQLException if query fails
     */
    public List<Integer> getMissingQuestions(Long testLinkId) throws SQLException {
        if (testLinkId == null) {
            return new ArrayList<>();
        }
        
        String sql = """
            WITH RECURSIVE questions(n) AS (
                SELECT 1
                UNION ALL
                SELECT n + 1 FROM questions WHERE n < 28
            )
            SELECT n FROM questions
            WHERE n NOT IN (
                SELECT question_num FROM answers WHERE test_link_id = ?
            )
            ORDER BY n
            """;
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setLong(1, testLinkId);
                
                List<Integer> missingQuestions = new ArrayList<>();
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        missingQuestions.add(rs.getInt("n"));
                    }
                }
                return missingQuestions;
            }
        });
    }
    
    /**
     * Calculate DISC scores from answers
     * 
     * @param testLinkId Test link ID
     * @return Map of DISC type to score (D, I, S, C)
     * @throws SQLException if calculation fails
     */
    public Map<String, Integer> calculateDiscScores(Long testLinkId) throws SQLException {
        List<Answer> answers = getAnswersByTestLinkId(testLinkId);
        
        Map<String, Integer> scores = new HashMap<>();
        scores.put("D", 0);
        scores.put("I", 0);
        scores.put("S", 0);
        scores.put("C", 0);
        
        for (Answer answer : answers) {
            // Add 2 points for "most like"
            String mostLike = answer.getMostLike();
            if (mostLike != null && scores.containsKey(mostLike)) {
                scores.put(mostLike, scores.get(mostLike) + 2);
            }
            
            // Subtract 1 point for "least like"
            String leastLike = answer.getLeastLike();
            if (leastLike != null && scores.containsKey(leastLike)) {
                scores.put(leastLike, Math.max(0, scores.get(leastLike) - 1));
            }
        }
        
        return scores;
    }
    
    /**
     * Get total count of answers
     * 
     * @return Total number of answers
     * @throws SQLException if query fails
     */
    public long getAnswerCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM answers";
        
        return DBUtil.executeWithConnection(connection -> {
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                if (rs.next()) {
                    return rs.getLong(1);
                }
                return 0L;
            }
        });
    }
    
    /**
     * Get answers with pagination
     * 
     * @param offset Starting position
     * @param limit Maximum number of records
     * @return List of answers
     * @throws SQLException if query fails
     */
    public List<Answer> getAnswers(int offset, int limit) throws SQLException {
        String sql = """
            SELECT id, test_link_id, question_num, most_like, least_like, created_at 
            FROM answers 
            ORDER BY created_at DESC 
            LIMIT ? OFFSET ?
            """;
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, limit);
                pstmt.setInt(2, offset);
                
                List<Answer> answers = new ArrayList<>();
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        answers.add(mapResultSetToAnswer(rs));
                    }
                }
                return answers;
            }
        });
    }
    
    /**
     * Get answers by date range
     * 
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of answers created within the date range
     * @throws SQLException if query fails
     */
    public List<Answer> getAnswersByDateRange(Timestamp startDate, Timestamp endDate) throws SQLException {
        String sql = """
            SELECT id, test_link_id, question_num, most_like, least_like, created_at 
            FROM answers 
            WHERE created_at BETWEEN ? AND ? 
            ORDER BY created_at DESC
            """;
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setTimestamp(1, startDate);
                pstmt.setTimestamp(2, endDate);
                
                List<Answer> answers = new ArrayList<>();
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        answers.add(mapResultSetToAnswer(rs));
                    }
                }
                return answers;
            }
        });
    }
    
    /**
     * Bulk insert answers for a test
     * 
     * @param answers List of answers to insert
     * @return Number of successfully inserted answers
     * @throws SQLException if bulk insert fails
     */
    public int bulkInsertAnswers(List<Answer> answers) throws SQLException {
        if (answers == null || answers.isEmpty()) {
            return 0;
        }
        
        String sql = """
            INSERT INTO answers (test_link_id, question_num, most_like, least_like, created_at) 
            VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)
            """;
        
        return DBUtil.executeTransaction(connection -> {
            int insertedCount = 0;
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                for (Answer answer : answers) {
                    if (answer.isValid()) {
                        pstmt.setLong(1, answer.getTestLinkId());
                        pstmt.setInt(2, answer.getQuestionNum());
                        pstmt.setString(3, answer.getMostLike());
                        pstmt.setString(4, answer.getLeastLike());
                        pstmt.addBatch();
                        insertedCount++;
                    }
                }
                
                pstmt.executeBatch();
            }
            return insertedCount;
        });
    }
    
    /**
     * Map ResultSet to Answer object
     * 
     * @param rs ResultSet containing answer data
     * @return Answer object
     * @throws SQLException if mapping fails
     */
    private Answer mapResultSetToAnswer(ResultSet rs) throws SQLException {
        return new Answer(
            rs.getLong("id"),
            rs.getLong("test_link_id"),
            rs.getInt("question_num"),
            rs.getString("most_like"),
            rs.getString("least_like"),
            rs.getTimestamp("created_at")
        );
    }
}