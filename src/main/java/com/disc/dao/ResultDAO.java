package com.disc.dao;

import com.disc.model.Result;
import com.disc.model.TestLink;
import com.disc.model.User;
import com.disc.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Data Access Object for Result operations
 */
public class ResultDAO {
    
    private static final Logger logger = Logger.getLogger(ResultDAO.class.getName());
    
    /**
     * Create a new result
     * 
     * @param result Result object to create
     * @return The created result with ID populated
     * @throws SQLException if creation fails
     */
    public Result createResult(Result result) throws SQLException {
        if (result == null || !result.isValid()) {
            throw new IllegalArgumentException("Invalid result data");
        }
        
        String sql = """
            INSERT INTO results (test_link_id, d_score, i_score, s_score, c_score, result_type, created_at) 
            VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            """;
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setLong(1, result.getTestLinkId());
                pstmt.setInt(2, result.getDScore());
                pstmt.setInt(3, result.getIScore());
                pstmt.setInt(4, result.getSScore());
                pstmt.setInt(5, result.getCScore());
                pstmt.setString(6, result.getResultType());
                
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Creating result failed, no rows affected");
                }
                
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        result.setId(generatedKeys.getLong(1));
                    } else {
                        throw new SQLException("Creating result failed, no ID obtained");
                    }
                }
                
                return getResultById(result.getId());
            }
        });
    }
    
    /**
     * Save or update result (upsert operation)
     * 
     * @param result Result object to save or update
     * @return The saved/updated result
     * @throws SQLException if operation fails
     */
    public Result saveOrUpdateResult(Result result) throws SQLException {
        if (result == null || !result.isValid()) {
            throw new IllegalArgumentException("Invalid result data");
        }
        
        // Check if result already exists for this test_link_id
        Result existingResult = getResultByTestLinkId(result.getTestLinkId());
        
        if (existingResult != null) {
            // Update existing result
            existingResult.setDScore(result.getDScore());
            existingResult.setIScore(result.getIScore());
            existingResult.setSScore(result.getSScore());
            existingResult.setCScore(result.getCScore());
            existingResult.setResultType(result.getResultType());
            updateResult(existingResult);
            return existingResult;
        } else {
            // Create new result
            return createResult(result);
        }
    }
    
    /**
     * Get result by ID
     * 
     * @param resultId Result ID
     * @return Result object or null if not found
     * @throws SQLException if query fails
     */
    public Result getResultById(Long resultId) throws SQLException {
        if (resultId == null) {
            return null;
        }
        
        String sql = """
            SELECT r.id, r.test_link_id, r.d_score, r.i_score, r.s_score, r.c_score, 
                   r.result_type, r.created_at,
                   tl.user_id, tl.test_url, tl.status, tl.started_at, tl.completed_at, tl.created_at as tl_created_at,
                   u.name as user_name, u.name_hash, u.created_at as user_created_at
            FROM results r
            LEFT JOIN test_links tl ON r.test_link_id = tl.id
            LEFT JOIN users u ON tl.user_id = u.id
            WHERE r.id = ?
            """;
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setLong(1, resultId);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToResult(rs);
                    }
                    return null;
                }
            }
        });
    }
    
    /**
     * Get result by test link ID
     * 
     * @param testLinkId Test link ID
     * @return Result object or null if not found
     * @throws SQLException if query fails
     */
    public Result getResultByTestLinkId(Long testLinkId) throws SQLException {
        if (testLinkId == null) {
            return null;
        }
        
        String sql = """
            SELECT r.id, r.test_link_id, r.d_score, r.i_score, r.s_score, r.c_score, 
                   r.result_type, r.created_at,
                   tl.user_id, tl.test_url, tl.status, tl.started_at, tl.completed_at, tl.created_at as tl_created_at,
                   u.name as user_name, u.name_hash, u.created_at as user_created_at
            FROM results r
            LEFT JOIN test_links tl ON r.test_link_id = tl.id
            LEFT JOIN users u ON tl.user_id = u.id
            WHERE r.test_link_id = ?
            """;
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setLong(1, testLinkId);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToResult(rs);
                    }
                    return null;
                }
            }
        });
    }
    
    /**
     * Get results by user ID
     * 
     * @param userId User ID
     * @return List of results for the user
     * @throws SQLException if query fails
     */
    public List<Result> getResultsByUserId(Long userId) throws SQLException {
        if (userId == null) {
            return new ArrayList<>();
        }
        
        String sql = """
            SELECT r.id, r.test_link_id, r.d_score, r.i_score, r.s_score, r.c_score, 
                   r.result_type, r.created_at,
                   tl.user_id, tl.test_url, tl.status, tl.started_at, tl.completed_at, tl.created_at as tl_created_at,
                   u.name as user_name, u.name_hash, u.created_at as user_created_at
            FROM results r
            JOIN test_links tl ON r.test_link_id = tl.id
            LEFT JOIN users u ON tl.user_id = u.id
            WHERE tl.user_id = ?
            ORDER BY r.created_at DESC
            """;
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setLong(1, userId);
                
                List<Result> results = new ArrayList<>();
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        results.add(mapResultSetToResult(rs));
                    }
                }
                return results;
            }
        });
    }
    
    /**
     * Update result
     * 
     * @param result Result object with updated information
     * @return true if update was successful
     * @throws SQLException if update fails
     */
    public boolean updateResult(Result result) throws SQLException {
        if (result == null || result.getId() == null || !result.isValid()) {
            throw new IllegalArgumentException("Invalid result data or missing ID");
        }
        
        String sql = """
            UPDATE results 
            SET d_score = ?, i_score = ?, s_score = ?, c_score = ?, result_type = ?
            WHERE id = ?
            """;
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, result.getDScore());
                pstmt.setInt(2, result.getIScore());
                pstmt.setInt(3, result.getSScore());
                pstmt.setInt(4, result.getCScore());
                pstmt.setString(5, result.getResultType());
                pstmt.setLong(6, result.getId());
                
                int rowsAffected = pstmt.executeUpdate();
                return rowsAffected > 0;
            }
        });
    }
    
    /**
     * Delete result by ID
     * 
     * @param resultId Result ID to delete
     * @return true if deletion was successful
     * @throws SQLException if deletion fails
     */
    public boolean deleteResult(Long resultId) throws SQLException {
        if (resultId == null) {
            return false;
        }
        
        String sql = "DELETE FROM results WHERE id = ?";
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setLong(1, resultId);
                
                int rowsAffected = pstmt.executeUpdate();
                return rowsAffected > 0;
            }
        });
    }
    
    /**
     * Delete result by test link ID
     * 
     * @param testLinkId Test link ID
     * @return true if deletion was successful
     * @throws SQLException if deletion fails
     */
    public boolean deleteResultByTestLinkId(Long testLinkId) throws SQLException {
        if (testLinkId == null) {
            return false;
        }
        
        String sql = "DELETE FROM results WHERE test_link_id = ?";
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setLong(1, testLinkId);
                
                int rowsAffected = pstmt.executeUpdate();
                return rowsAffected > 0;
            }
        });
    }
    
    /**
     * Get all results with pagination
     * 
     * @param offset Starting position
     * @param limit Maximum number of records
     * @return List of results
     * @throws SQLException if query fails
     */
    public List<Result> getResults(int offset, int limit) throws SQLException {
        String sql = """
            SELECT r.id, r.test_link_id, r.d_score, r.i_score, r.s_score, r.c_score, 
                   r.result_type, r.created_at,
                   tl.user_id, tl.test_url, tl.status, tl.started_at, tl.completed_at, tl.created_at as tl_created_at,
                   u.name as user_name, u.name_hash, u.created_at as user_created_at
            FROM results r
            LEFT JOIN test_links tl ON r.test_link_id = tl.id
            LEFT JOIN users u ON tl.user_id = u.id
            ORDER BY r.created_at DESC 
            LIMIT ? OFFSET ?
            """;
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, limit);
                pstmt.setInt(2, offset);
                
                List<Result> results = new ArrayList<>();
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        results.add(mapResultSetToResult(rs));
                    }
                }
                return results;
            }
        });
    }
    
    /**
     * Get results by DISC type
     * 
     * @param resultType DISC type (D, I, S, C)
     * @param offset Starting position
     * @param limit Maximum number of records
     * @return List of results with the specified type
     * @throws SQLException if query fails
     */
    public List<Result> getResultsByType(String resultType, int offset, int limit) throws SQLException {
        if (resultType == null || resultType.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String sql = """
            SELECT r.id, r.test_link_id, r.d_score, r.i_score, r.s_score, r.c_score, 
                   r.result_type, r.created_at,
                   tl.user_id, tl.test_url, tl.status, tl.started_at, tl.completed_at, tl.created_at as tl_created_at,
                   u.name as user_name, u.name_hash, u.created_at as user_created_at
            FROM results r
            LEFT JOIN test_links tl ON r.test_link_id = tl.id
            LEFT JOIN users u ON tl.user_id = u.id
            WHERE r.result_type = ?
            ORDER BY r.created_at DESC 
            LIMIT ? OFFSET ?
            """;
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, resultType.trim().toUpperCase());
                pstmt.setInt(2, limit);
                pstmt.setInt(3, offset);
                
                List<Result> results = new ArrayList<>();
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        results.add(mapResultSetToResult(rs));
                    }
                }
                return results;
            }
        });
    }
    
    /**
     * Get total count of results
     * 
     * @return Total number of results
     * @throws SQLException if query fails
     */
    public long getResultCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM results";
        
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
     * Get count of results by type
     * 
     * @param resultType DISC type
     * @return Number of results with the specified type
     * @throws SQLException if query fails
     */
    public long getResultCountByType(String resultType) throws SQLException {
        if (resultType == null || resultType.trim().isEmpty()) {
            return 0L;
        }
        
        String sql = "SELECT COUNT(*) FROM results WHERE result_type = ?";
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, resultType.trim().toUpperCase());
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getLong(1);
                    }
                    return 0L;
                }
            }
        });
    }
    
    /**
     * Get results created within a date range
     * 
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of results created within the date range
     * @throws SQLException if query fails
     */
    public List<Result> getResultsByDateRange(Timestamp startDate, Timestamp endDate) throws SQLException {
        String sql = """
            SELECT r.id, r.test_link_id, r.d_score, r.i_score, r.s_score, r.c_score, 
                   r.result_type, r.created_at,
                   tl.user_id, tl.test_url, tl.status, tl.started_at, tl.completed_at, tl.created_at as tl_created_at,
                   u.name as user_name, u.name_hash, u.created_at as user_created_at
            FROM results r
            LEFT JOIN test_links tl ON r.test_link_id = tl.id
            LEFT JOIN users u ON tl.user_id = u.id
            WHERE r.created_at BETWEEN ? AND ? 
            ORDER BY r.created_at DESC
            """;
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setTimestamp(1, startDate);
                pstmt.setTimestamp(2, endDate);
                
                List<Result> results = new ArrayList<>();
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        results.add(mapResultSetToResult(rs));
                    }
                }
                return results;
            }
        });
    }
    
    /**
     * Get recent results
     * 
     * @param limit Maximum number of records
     * @return List of recent results
     * @throws SQLException if query fails
     */
    public List<Result> getRecentResults(int limit) throws SQLException {
        String sql = """
            SELECT r.id, r.test_link_id, r.d_score, r.i_score, r.s_score, r.c_score, 
                   r.result_type, r.created_at,
                   tl.user_id, tl.test_url, tl.status, tl.started_at, tl.completed_at, tl.created_at as tl_created_at,
                   u.name as user_name, u.name_hash, u.created_at as user_created_at
            FROM results r
            LEFT JOIN test_links tl ON r.test_link_id = tl.id
            LEFT JOIN users u ON tl.user_id = u.id
            ORDER BY r.created_at DESC 
            LIMIT ?
            """;
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, limit);
                
                List<Result> results = new ArrayList<>();
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        results.add(mapResultSetToResult(rs));
                    }
                }
                return results;
            }
        });
    }
    
    /**
     * Get DISC type distribution statistics
     * 
     * @return Map of DISC type to count
     * @throws SQLException if query fails
     */
    public Map<String, Long> getTypeDistribution() throws SQLException {
        String sql = """
            SELECT result_type, COUNT(*) as count 
            FROM results 
            GROUP BY result_type
            ORDER BY result_type
            """;
        
        return DBUtil.executeWithConnection(connection -> {
            Map<String, Long> distribution = new HashMap<>();
            // Initialize with 0 counts
            distribution.put("D", 0L);
            distribution.put("I", 0L);
            distribution.put("S", 0L);
            distribution.put("C", 0L);
            
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                while (rs.next()) {
                    String type = rs.getString("result_type");
                    Long count = rs.getLong("count");
                    if (type != null) {
                        distribution.put(type, count);
                    }
                }
            }
            return distribution;
        });
    }
    
    /**
     * Get average scores for each DISC type
     * 
     * @return Map of DISC type to average score
     * @throws SQLException if query fails
     */
    public Map<String, Double> getAverageScores() throws SQLException {
        String sql = """
            SELECT 
                AVG(CAST(d_score AS FLOAT)) as avg_d,
                AVG(CAST(i_score AS FLOAT)) as avg_i,
                AVG(CAST(s_score AS FLOAT)) as avg_s,
                AVG(CAST(c_score AS FLOAT)) as avg_c
            FROM results
            """;
        
        return DBUtil.executeWithConnection(connection -> {
            Map<String, Double> averages = new HashMap<>();
            
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                if (rs.next()) {
                    averages.put("D", rs.getDouble("avg_d"));
                    averages.put("I", rs.getDouble("avg_i"));
                    averages.put("S", rs.getDouble("avg_s"));
                    averages.put("C", rs.getDouble("avg_c"));
                } else {
                    // Return 0 averages if no data
                    averages.put("D", 0.0);
                    averages.put("I", 0.0);
                    averages.put("S", 0.0);
                    averages.put("C", 0.0);
                }
            }
            return averages;
        });
    }
    
    /**
     * Check if result exists for test link
     * 
     * @param testLinkId Test link ID
     * @return true if result exists, false otherwise
     * @throws SQLException if query fails
     */
    public boolean resultExists(Long testLinkId) throws SQLException {
        if (testLinkId == null) {
            return false;
        }
        
        String sql = "SELECT 1 FROM results WHERE test_link_id = ? LIMIT 1";
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setLong(1, testLinkId);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    return rs.next();
                }
            }
        });
    }
    
    /**
     * Map ResultSet to Result object
     * 
     * @param rs ResultSet containing result data
     * @return Result object
     * @throws SQLException if mapping fails
     */
    private Result mapResultSetToResult(ResultSet rs) throws SQLException {
        Result result = new Result(
            rs.getLong("id"),
            rs.getLong("test_link_id"),
            rs.getInt("d_score"),
            rs.getInt("i_score"),
            rs.getInt("s_score"),
            rs.getInt("c_score"),
            rs.getString("result_type"),
            rs.getTimestamp("created_at")
        );
        
        // Map associated test link if available
        Long userId = rs.getLong("user_id");
        if (userId != null && userId > 0) {
            TestLink testLink = new TestLink(
                rs.getLong("test_link_id"),
                userId,
                rs.getString("test_url"),
                TestLink.Status.fromString(rs.getString("status")),
                rs.getTimestamp("started_at"),
                rs.getTimestamp("completed_at"),
                rs.getTimestamp("tl_created_at")
            );
            
            // Map associated user if available
            String userName = rs.getString("user_name");
            if (userName != null) {
                User user = new User(
                    userId,
                    userName,
                    rs.getString("name_hash"),
                    rs.getTimestamp("user_created_at")
                );
                testLink.setUser(user);
            }
            
            result.setTestLink(testLink);
        }
        
        return result;
    }

    // ========== STATISTICS METHODS ==========

    /**
     * Get total count of results
     */
    public long getTotalResultCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM results";

        return DBUtil.executeWithConnection(connection -> {
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                return rs.next() ? rs.getLong(1) : 0;
            }
        });
    }

    /**
     * Get DISC type distribution
     */
    public Map<String, Integer> getDiscTypeDistribution() throws SQLException {
        String sql = "SELECT result_type, COUNT(*) as count FROM results WHERE result_type IS NOT NULL GROUP BY result_type";

        return DBUtil.executeWithConnection(connection -> {
            Map<String, Integer> distribution = new HashMap<>();

            // Initialize with zeros
            distribution.put("D", 0);
            distribution.put("I", 0);
            distribution.put("S", 0);
            distribution.put("C", 0);

            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    String type = rs.getString("result_type");
                    int count = rs.getInt("count");
                    if (type != null) {
                        distribution.put(type, count);
                    }
                }
            }
            return distribution;
        });
    }

    /**
     * Get average scores for each DISC type
     */
    public Map<String, Double> getAverageDiscScores() throws SQLException {
        String sql = """
            SELECT
                AVG(CAST(d_score AS REAL)) as avg_d,
                AVG(CAST(i_score AS REAL)) as avg_i,
                AVG(CAST(s_score AS REAL)) as avg_s,
                AVG(CAST(c_score AS REAL)) as avg_c
            FROM results
            """;

        return DBUtil.executeWithConnection(connection -> {
            Map<String, Double> averages = new HashMap<>();
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    averages.put("D", rs.getDouble("avg_d"));
                    averages.put("I", rs.getDouble("avg_i"));
                    averages.put("S", rs.getDouble("avg_s"));
                    averages.put("C", rs.getDouble("avg_c"));
                } else {
                    averages.put("D", 0.0);
                    averages.put("I", 0.0);
                    averages.put("S", 0.0);
                    averages.put("C", 0.0);
                }
            }
            return averages;
        });
    }

    /**
     * Get results created in a date range
     */
    public long getResultCountInDateRange(String startDate, String endDate) throws SQLException {
        String sql = "SELECT COUNT(*) FROM results WHERE DATE(created_at) BETWEEN ? AND ?";

        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, startDate);
                pstmt.setString(2, endDate);
                try (ResultSet rs = pstmt.executeQuery()) {
                    return rs.next() ? rs.getLong(1) : 0;
                }
            }
        });
    }

    /**
     * Get recent results with user information
     */
    public List<Map<String, Object>> getRecentResults(int limit) throws SQLException {
        String sql = """
            SELECT r.*,
                   u.name as user_name,
                   r.created_at as result_created_at
            FROM results r
            JOIN test_links tl ON r.test_link_id = tl.id
            JOIN users u ON tl.user_id = u.id
            ORDER BY r.created_at DESC
            LIMIT ?
            """;

        return DBUtil.executeWithConnection(connection -> {
            List<Map<String, Object>> results = new ArrayList<>();
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, limit);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> result = new HashMap<>();
                        result.put("id", rs.getLong("id"));
                        result.put("userName", rs.getString("user_name"));
                        result.put("resultType", rs.getString("result_type"));
                        result.put("dScore", rs.getInt("d_score"));
                        result.put("iScore", rs.getInt("i_score"));
                        result.put("sScore", rs.getInt("s_score"));
                        result.put("cScore", rs.getInt("c_score"));
                        result.put("createdAt", rs.getTimestamp("result_created_at"));
                        results.add(result);
                    }
                }
            }
            return results;
        });
    }

    /**
     * Get score distribution statistics
     */
    public Map<String, Object> getScoreDistributionStats() throws SQLException {
        String sql = """
            SELECT
                MIN(d_score) as min_d, MAX(d_score) as max_d,
                MIN(i_score) as min_i, MAX(i_score) as max_i,
                MIN(s_score) as min_s, MAX(s_score) as max_s,
                MIN(c_score) as min_c, MAX(c_score) as max_c,
                COUNT(*) as total_results
            FROM results
            """;

        return DBUtil.executeWithConnection(connection -> {
            Map<String, Object> stats = new HashMap<>();
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    stats.put("minD", rs.getInt("min_d"));
                    stats.put("maxD", rs.getInt("max_d"));
                    stats.put("minI", rs.getInt("min_i"));
                    stats.put("maxI", rs.getInt("max_i"));
                    stats.put("minS", rs.getInt("min_s"));
                    stats.put("maxS", rs.getInt("max_s"));
                    stats.put("minC", rs.getInt("min_c"));
                    stats.put("maxC", rs.getInt("max_c"));
                    stats.put("totalResults", rs.getInt("total_results"));
                }
            }
            return stats;
        });
    }
}