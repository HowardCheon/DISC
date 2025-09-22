package com.disc.dao;

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
 * Data Access Object for TestLink operations
 */
public class TestLinkDAO {
    
    private static final Logger logger = Logger.getLogger(TestLinkDAO.class.getName());
    
    /**
     * Create a new test link
     * 
     * @param testLink TestLink object to create
     * @return The created test link with ID populated
     * @throws SQLException if creation fails
     */
    public TestLink createTestLink(TestLink testLink) throws SQLException {
        if (testLink == null || testLink.getUserId() == null || 
            testLink.getTestUrl() == null || testLink.getTestUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("TestLink userId and testUrl cannot be null or empty");
        }
        
        String sql = """
            INSERT INTO test_links (user_id, test_url, status, created_at) 
            VALUES (?, ?, ?, CURRENT_TIMESTAMP)
            """;
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setLong(1, testLink.getUserId());
                pstmt.setString(2, testLink.getTestUrl().trim());
                pstmt.setString(3, testLink.getStatusValue());
                
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Creating test link failed, no rows affected");
                }
                
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        testLink.setId(generatedKeys.getLong(1));
                    } else {
                        throw new SQLException("Creating test link failed, no ID obtained");
                    }
                }
                
                return getTestLinkById(testLink.getId());
            }
        });
    }
    
    /**
     * Get test link by ID
     * 
     * @param testLinkId Test link ID
     * @return TestLink object or null if not found
     * @throws SQLException if query fails
     */
    public TestLink getTestLinkById(Long testLinkId) throws SQLException {
        if (testLinkId == null) {
            return null;
        }
        
        String sql = """
            SELECT tl.id, tl.user_id, tl.test_url, tl.status, 
                   tl.started_at, tl.completed_at, tl.created_at,
                   u.name as user_name, u.name_hash, u.created_at as user_created_at
            FROM test_links tl
            LEFT JOIN users u ON tl.user_id = u.id
            WHERE tl.id = ?
            """;
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setLong(1, testLinkId);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToTestLink(rs);
                    }
                    return null;
                }
            }
        });
    }
    
    /**
     * Get test link by URL
     * 
     * @param testUrl Test URL to search for
     * @return TestLink object or null if not found
     * @throws SQLException if query fails
     */
    public TestLink getTestLinkByUrl(String testUrl) throws SQLException {
        if (testUrl == null || testUrl.trim().isEmpty()) {
            return null;
        }
        
        String sql = """
            SELECT tl.id, tl.user_id, tl.test_url, tl.status, 
                   tl.started_at, tl.completed_at, tl.created_at,
                   u.name as user_name, u.name_hash, u.created_at as user_created_at
            FROM test_links tl
            LEFT JOIN users u ON tl.user_id = u.id
            WHERE tl.test_url = ?
            """;
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, testUrl.trim());
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToTestLink(rs);
                    }
                    return null;
                }
            }
        });
    }
    
    /**
     * Get test links by user ID
     * 
     * @param userId User ID
     * @return List of test links for the user
     * @throws SQLException if query fails
     */
    public List<TestLink> getTestLinksByUserId(Long userId) throws SQLException {
        if (userId == null) {
            return new ArrayList<>();
        }
        
        String sql = """
            SELECT tl.id, tl.user_id, tl.test_url, tl.status, 
                   tl.started_at, tl.completed_at, tl.created_at,
                   u.name as user_name, u.name_hash, u.created_at as user_created_at
            FROM test_links tl
            LEFT JOIN users u ON tl.user_id = u.id
            WHERE tl.user_id = ?
            ORDER BY tl.created_at DESC
            """;
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setLong(1, userId);
                
                List<TestLink> testLinks = new ArrayList<>();
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        testLinks.add(mapResultSetToTestLink(rs));
                    }
                }
                return testLinks;
            }
        });
    }
    
    /**
     * Update test link status to started
     * 
     * @param testLinkId Test link ID
     * @return true if update was successful
     * @throws SQLException if update fails
     */
    public boolean updateStartedAt(Long testLinkId) throws SQLException {
        if (testLinkId == null) {
            return false;
        }
        
        String sql = """
            UPDATE test_links 
            SET status = '검사중', started_at = CURRENT_TIMESTAMP 
            WHERE id = ? AND status = '검사전'
            """;
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setLong(1, testLinkId);
                
                int rowsAffected = pstmt.executeUpdate();
                return rowsAffected > 0;
            }
        });
    }
    
    /**
     * Update test link status to completed
     * 
     * @param testLinkId Test link ID
     * @return true if update was successful
     * @throws SQLException if update fails
     */
    public boolean updateCompletedAt(Long testLinkId) throws SQLException {
        if (testLinkId == null) {
            return false;
        }
        
        String sql = """
            UPDATE test_links 
            SET status = '검사완료', completed_at = CURRENT_TIMESTAMP 
            WHERE id = ? AND status = '검사중'
            """;
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setLong(1, testLinkId);
                
                int rowsAffected = pstmt.executeUpdate();
                return rowsAffected > 0;
            }
        });
    }
    
    /**
     * Update test link status
     * 
     * @param testLinkId Test link ID
     * @param status New status
     * @return true if update was successful
     * @throws SQLException if update fails
     */
    public boolean updateStatus(Long testLinkId, TestLink.Status status) throws SQLException {
        if (testLinkId == null || status == null) {
            return false;
        }
        
        String sql = "UPDATE test_links SET status = ? WHERE id = ?";
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, status.getValue());
                pstmt.setLong(2, testLinkId);
                
                int rowsAffected = pstmt.executeUpdate();
                return rowsAffected > 0;
            }
        });
    }
    
    /**
     * Delete test link by ID
     * 
     * @param testLinkId Test link ID to delete
     * @return true if deletion was successful
     * @throws SQLException if deletion fails
     */
    public boolean deleteTestLink(Long testLinkId) throws SQLException {
        if (testLinkId == null) {
            return false;
        }
        
        String sql = "DELETE FROM test_links WHERE id = ?";
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setLong(1, testLinkId);
                
                int rowsAffected = pstmt.executeUpdate();
                return rowsAffected > 0;
            }
        });
    }
    
    /**
     * Get all test links with pagination
     * 
     * @param offset Starting position
     * @param limit Maximum number of records
     * @return List of test links
     * @throws SQLException if query fails
     */
    public List<TestLink> getTestLinks(int offset, int limit) throws SQLException {
        String sql = """
            SELECT tl.id, tl.user_id, tl.test_url, tl.status, 
                   tl.started_at, tl.completed_at, tl.created_at,
                   u.name as user_name, u.name_hash, u.created_at as user_created_at
            FROM test_links tl
            LEFT JOIN users u ON tl.user_id = u.id
            ORDER BY tl.created_at DESC 
            LIMIT ? OFFSET ?
            """;
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, limit);
                pstmt.setInt(2, offset);
                
                List<TestLink> testLinks = new ArrayList<>();
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        testLinks.add(mapResultSetToTestLink(rs));
                    }
                }
                return testLinks;
            }
        });
    }
    
    /**
     * Get test links by status
     * 
     * @param status Test link status
     * @param offset Starting position
     * @param limit Maximum number of records
     * @return List of test links with the specified status
     * @throws SQLException if query fails
     */
    public List<TestLink> getTestLinksByStatus(TestLink.Status status, int offset, int limit) throws SQLException {
        if (status == null) {
            return new ArrayList<>();
        }
        
        String sql = """
            SELECT tl.id, tl.user_id, tl.test_url, tl.status, 
                   tl.started_at, tl.completed_at, tl.created_at,
                   u.name as user_name, u.name_hash, u.created_at as user_created_at
            FROM test_links tl
            LEFT JOIN users u ON tl.user_id = u.id
            WHERE tl.status = ?
            ORDER BY tl.created_at DESC 
            LIMIT ? OFFSET ?
            """;
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, status.getValue());
                pstmt.setInt(2, limit);
                pstmt.setInt(3, offset);
                
                List<TestLink> testLinks = new ArrayList<>();
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        testLinks.add(mapResultSetToTestLink(rs));
                    }
                }
                return testLinks;
            }
        });
    }
    
    /**
     * Get total count of test links
     * 
     * @return Total number of test links
     * @throws SQLException if query fails
     */
    public long getTestLinkCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM test_links";
        
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
     * Get count of test links by status
     * 
     * @param status Test link status
     * @return Number of test links with the specified status
     * @throws SQLException if query fails
     */
    public long getTestLinkCountByStatus(TestLink.Status status) throws SQLException {
        if (status == null) {
            return 0L;
        }
        
        String sql = "SELECT COUNT(*) FROM test_links WHERE status = ?";
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, status.getValue());
                
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
     * Get test links created within a date range
     * 
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of test links created within the date range
     * @throws SQLException if query fails
     */
    public List<TestLink> getTestLinksByDateRange(Timestamp startDate, Timestamp endDate) throws SQLException {
        String sql = """
            SELECT tl.id, tl.user_id, tl.test_url, tl.status, 
                   tl.started_at, tl.completed_at, tl.created_at,
                   u.name as user_name, u.name_hash, u.created_at as user_created_at
            FROM test_links tl
            LEFT JOIN users u ON tl.user_id = u.id
            WHERE tl.created_at BETWEEN ? AND ? 
            ORDER BY tl.created_at DESC
            """;
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setTimestamp(1, startDate);
                pstmt.setTimestamp(2, endDate);
                
                List<TestLink> testLinks = new ArrayList<>();
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        testLinks.add(mapResultSetToTestLink(rs));
                    }
                }
                return testLinks;
            }
        });
    }
    
    /**
     * Get recent test links
     * 
     * @param limit Maximum number of records
     * @return List of recent test links
     * @throws SQLException if query fails
     */
    public List<TestLink> getRecentTestLinks(int limit) throws SQLException {
        String sql = """
            SELECT tl.id, tl.user_id, tl.test_url, tl.status, 
                   tl.started_at, tl.completed_at, tl.created_at,
                   u.name as user_name, u.name_hash, u.created_at as user_created_at
            FROM test_links tl
            LEFT JOIN users u ON tl.user_id = u.id
            ORDER BY tl.created_at DESC 
            LIMIT ?
            """;
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, limit);
                
                List<TestLink> testLinks = new ArrayList<>();
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        testLinks.add(mapResultSetToTestLink(rs));
                    }
                }
                return testLinks;
            }
        });
    }
    
    /**
     * Check if test URL exists
     * 
     * @param testUrl Test URL to check
     * @return true if URL exists, false otherwise
     * @throws SQLException if query fails
     */
    public boolean testUrlExists(String testUrl) throws SQLException {
        if (testUrl == null || testUrl.trim().isEmpty()) {
            return false;
        }
        
        String sql = "SELECT 1 FROM test_links WHERE test_url = ? LIMIT 1";
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, testUrl.trim());
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    return rs.next();
                }
            }
        });
    }
    
    /**
     * Get statistics by status
     * 
     * @return Array of counts: [pending, in_progress, completed]
     * @throws SQLException if query fails
     */
    public long[] getStatusStatistics() throws SQLException {
        String sql = """
            SELECT 
                SUM(CASE WHEN status = '검사전' THEN 1 ELSE 0 END) as pending,
                SUM(CASE WHEN status = '검사중' THEN 1 ELSE 0 END) as in_progress,
                SUM(CASE WHEN status = '검사완료' THEN 1 ELSE 0 END) as completed
            FROM test_links
            """;
        
        return DBUtil.executeWithConnection(connection -> {
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                if (rs.next()) {
                    return new long[]{
                        rs.getLong("pending"),
                        rs.getLong("in_progress"),
                        rs.getLong("completed")
                    };
                }
                return new long[]{0, 0, 0};
            }
        });
    }
    
    /**
     * Map ResultSet to TestLink object
     * 
     * @param rs ResultSet containing test link data
     * @return TestLink object
     * @throws SQLException if mapping fails
     */
    private TestLink mapResultSetToTestLink(ResultSet rs) throws SQLException {
        TestLink testLink = new TestLink(
            rs.getLong("id"),
            rs.getLong("user_id"),
            rs.getString("test_url"),
            TestLink.Status.fromString(rs.getString("status")),
            rs.getTimestamp("started_at"),
            rs.getTimestamp("completed_at"),
            rs.getTimestamp("created_at")
        );
        
        // Map associated user if available
        String userName = rs.getString("user_name");
        if (userName != null) {
            User user = new User(
                rs.getLong("user_id"),
                userName,
                rs.getString("name_hash"),
                rs.getTimestamp("user_created_at")
            );
            testLink.setUser(user);
        }
        
        return testLink;
    }

    // ========== STATISTICS METHODS ==========

    /**
     * Get total count of test links
     */
    public long getTotalTestLinkCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM test_links";

        return DBUtil.executeWithConnection(connection -> {
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                return rs.next() ? rs.getLong(1) : 0;
            }
        });
    }

    /**
     * Get count of completed tests
     */
    public long getCompletedTestCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM test_links WHERE status = '검사완료'";

        return DBUtil.executeWithConnection(connection -> {
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                return rs.next() ? rs.getLong(1) : 0;
            }
        });
    }

    /**
     * Get count of test links created on a specific date
     */
    public long getTestLinkCountByDate(String date) throws SQLException {
        String sql = "SELECT COUNT(*) FROM test_links WHERE DATE(created_at) = ?";

        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, date);
                try (ResultSet rs = pstmt.executeQuery()) {
                    return rs.next() ? rs.getLong(1) : 0;
                }
            }
        });
    }

    /**
     * Get count of completed tests on a specific date
     */
    public long getCompletedTestCountByDate(String date) throws SQLException {
        String sql = "SELECT COUNT(*) FROM test_links WHERE status = '검사완료' AND DATE(completed_at) = ?";

        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, date);
                try (ResultSet rs = pstmt.executeQuery()) {
                    return rs.next() ? rs.getLong(1) : 0;
                }
            }
        });
    }

    /**
     * Get count of started tests on a specific date
     */
    public long getStartedTestCountByDate(String date) throws SQLException {
        String sql = "SELECT COUNT(*) FROM test_links WHERE started_at IS NOT NULL AND DATE(started_at) = ?";

        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, date);
                try (ResultSet rs = pstmt.executeQuery()) {
                    return rs.next() ? rs.getLong(1) : 0;
                }
            }
        });
    }

    /**
     * Get count of test links created in a date range
     */
    public long getTestLinkCountInDateRange(String startDate, String endDate) throws SQLException {
        String sql = "SELECT COUNT(*) FROM test_links WHERE DATE(created_at) BETWEEN ? AND ?";

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
     * Get count of completed tests in a date range
     */
    public long getCompletedTestCountInDateRange(String startDate, String endDate) throws SQLException {
        String sql = "SELECT COUNT(*) FROM test_links WHERE status = '검사완료' AND DATE(completed_at) BETWEEN ? AND ?";

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
     * Get recent test completions with user information
     */
    public List<Map<String, Object>> getRecentCompletions(int limit) throws SQLException {
        String sql = """
            SELECT tl.*, u.name as user_name, u.created_at as user_created_at, u.name_hash
            FROM test_links tl
            JOIN users u ON tl.user_id = u.id
            WHERE tl.status = '검사완료' AND tl.completed_at IS NOT NULL
            ORDER BY tl.completed_at DESC
            LIMIT ?
            """;

        return DBUtil.executeWithConnection(connection -> {
            List<Map<String, Object>> results = new ArrayList<>();
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, limit);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> result = new HashMap<>();
                        result.put("testLinkId", rs.getLong("id"));
                        result.put("userName", rs.getString("user_name"));
                        result.put("completedAt", rs.getTimestamp("completed_at").toString());
                        result.put("testUrl", rs.getString("test_url"));
                        results.add(result);
                    }
                }
            }
            return results;
        });
    }

    /**
     * Get recent test starts with user information
     */
    public List<Map<String, Object>> getRecentStarts(int limit) throws SQLException {
        String sql = """
            SELECT tl.*, u.name as user_name, u.created_at as user_created_at, u.name_hash
            FROM test_links tl
            JOIN users u ON tl.user_id = u.id
            WHERE tl.started_at IS NOT NULL
            ORDER BY tl.started_at DESC
            LIMIT ?
            """;

        return DBUtil.executeWithConnection(connection -> {
            List<Map<String, Object>> results = new ArrayList<>();
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, limit);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> result = new HashMap<>();
                        result.put("testLinkId", rs.getLong("id"));
                        result.put("userName", rs.getString("user_name"));
                        result.put("startedAt", rs.getTimestamp("started_at").toString());
                        result.put("testUrl", rs.getString("test_url"));
                        result.put("status", rs.getString("status"));
                        results.add(result);
                    }
                }
            }
            return results;
        });
    }

    /**
     * Get test completion statistics by hour of day
     */
    public List<Map<String, Object>> getCompletionStatsByHour() throws SQLException {
        String sql = """
            SELECT
                CAST(strftime('%H', completed_at) AS INTEGER) as hour,
                COUNT(*) as count
            FROM test_links
            WHERE status = '검사완료' AND completed_at IS NOT NULL
            GROUP BY hour
            ORDER BY hour
            """;

        return DBUtil.executeWithConnection(connection -> {
            List<Map<String, Object>> results = new ArrayList<>();
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("hour", rs.getInt("hour"));
                    result.put("count", rs.getInt("count"));
                    results.add(result);
                }
            }
            return results;
        });
    }

    /**
     * Get average completion time for tests
     */
    public double getAverageCompletionTimeInMinutes() throws SQLException {
        String sql = """
            SELECT AVG(
                (strftime('%s', completed_at) - strftime('%s', started_at)) / 60.0
            ) as avg_minutes
            FROM test_links
            WHERE status = '검사완료'
            AND started_at IS NOT NULL
            AND completed_at IS NOT NULL
            """;

        return DBUtil.executeWithConnection(connection -> {
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                return rs.next() ? rs.getDouble("avg_minutes") : 0.0;
            }
        });
    }
}