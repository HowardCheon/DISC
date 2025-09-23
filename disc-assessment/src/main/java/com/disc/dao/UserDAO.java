package com.disc.dao;

import com.disc.model.User;
import com.disc.util.DBUtil;
import org.apache.commons.codec.digest.DigestUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Data Access Object for User operations
 */
public class UserDAO {
    
    private static final Logger logger = Logger.getLogger(UserDAO.class.getName());
    
    /**
     * Create a new user
     * 
     * @param user User object to create
     * @return The created user with ID populated
     * @throws SQLException if creation fails
     */
    public User createUser(User user) throws SQLException {
        if (user == null || user.getName() == null || user.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("User name cannot be null or empty");
        }
        
        // Generate name hash if not provided
        if (user.getNameHash() == null || user.getNameHash().trim().isEmpty()) {
            user.setNameHash(DigestUtils.sha256Hex(user.getName().trim().toLowerCase()));
        }
        
        String sql = """
            INSERT INTO users (name, name_hash, created_at) 
            VALUES (?, ?, CURRENT_TIMESTAMP)
            """;
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, user.getName().trim());
                pstmt.setString(2, user.getNameHash());
                
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Creating user failed, no rows affected");
                }
                
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getLong(1));
                    } else {
                        throw new SQLException("Creating user failed, no ID obtained");
                    }
                }
                
                // Get the created user with timestamp
                return getUserById(user.getId());
            }
        });
    }
    
    /**
     * Get user by ID
     * 
     * @param userId User ID
     * @return User object or null if not found
     * @throws SQLException if query fails
     */
    public User getUserById(Long userId) throws SQLException {
        if (userId == null) {
            return null;
        }
        
        String sql = "SELECT id, name, name_hash, created_at FROM users WHERE id = ?";
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setLong(1, userId);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToUser(rs);
                    }
                    return null;
                }
            }
        });
    }
    
    /**
     * Get user by name hash
     * 
     * @param nameHash Name hash to search for
     * @return User object or null if not found
     * @throws SQLException if query fails
     */
    public User getUserByNameHash(String nameHash) throws SQLException {
        if (nameHash == null || nameHash.trim().isEmpty()) {
            return null;
        }
        
        String sql = "SELECT id, name, name_hash, created_at FROM users WHERE name_hash = ?";
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, nameHash);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToUser(rs);
                    }
                    return null;
                }
            }
        });
    }
    
    /**
     * Get user by name (creates hash and searches)
     * 
     * @param name User name
     * @return User object or null if not found
     * @throws SQLException if query fails
     */
    public User getUserByName(String name) throws SQLException {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        
        String nameHash = DigestUtils.sha256Hex(name.trim().toLowerCase());
        return getUserByNameHash(nameHash);
    }
    
    /**
     * Find or create user by name
     * 
     * @param name User name
     * @return Existing or newly created user
     * @throws SQLException if operation fails
     */
    public User findOrCreateUser(String name) throws SQLException {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("User name cannot be null or empty");
        }
        
        // Try to find existing user
        User existingUser = getUserByName(name);
        if (existingUser != null) {
            return existingUser;
        }
        
        // Create new user if not found
        User newUser = new User(name.trim(), DigestUtils.sha256Hex(name.trim().toLowerCase()));
        return createUser(newUser);
    }
    
    /**
     * Update user information
     * 
     * @param user User object with updated information
     * @return true if update was successful
     * @throws SQLException if update fails
     */
    public boolean updateUser(User user) throws SQLException {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        String sql = "UPDATE users SET name = ?, name_hash = ? WHERE id = ?";
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, user.getName());
                pstmt.setString(2, user.getNameHash());
                pstmt.setLong(3, user.getId());
                
                int rowsAffected = pstmt.executeUpdate();
                return rowsAffected > 0;
            }
        });
    }
    
    /**
     * Delete user by ID
     * 
     * @param userId User ID to delete
     * @return true if deletion was successful
     * @throws SQLException if deletion fails
     */
    public boolean deleteUser(Long userId) throws SQLException {
        if (userId == null) {
            return false;
        }
        
        String sql = "DELETE FROM users WHERE id = ?";
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setLong(1, userId);
                
                int rowsAffected = pstmt.executeUpdate();
                return rowsAffected > 0;
            }
        });
    }
    
    /**
     * Get all users with pagination
     * 
     * @param offset Starting position
     * @param limit Maximum number of records
     * @return List of users
     * @throws SQLException if query fails
     */
    public List<User> getUsers(int offset, int limit) throws SQLException {
        String sql = """
            SELECT id, name, name_hash, created_at 
            FROM users 
            ORDER BY created_at DESC 
            LIMIT ? OFFSET ?
            """;
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, limit);
                pstmt.setInt(2, offset);
                
                List<User> users = new ArrayList<>();
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        users.add(mapResultSetToUser(rs));
                    }
                }
                return users;
            }
        });
    }
    
    /**
     * Get total count of users
     * 
     * @return Total number of users
     * @throws SQLException if query fails
     */
    public long getUserCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users";
        
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
     * Search users by name pattern
     * 
     * @param namePattern Name pattern to search for
     * @param offset Starting position
     * @param limit Maximum number of records
     * @return List of matching users
     * @throws SQLException if query fails
     */
    public List<User> searchUsersByName(String namePattern, int offset, int limit) throws SQLException {
        if (namePattern == null || namePattern.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String sql = """
            SELECT id, name, name_hash, created_at 
            FROM users 
            WHERE name LIKE ? 
            ORDER BY created_at DESC 
            LIMIT ? OFFSET ?
            """;
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, "%" + namePattern.trim() + "%");
                pstmt.setInt(2, limit);
                pstmt.setInt(3, offset);
                
                List<User> users = new ArrayList<>();
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        users.add(mapResultSetToUser(rs));
                    }
                }
                return users;
            }
        });
    }
    
    /**
     * Get users created within a date range
     * 
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of users created within the date range
     * @throws SQLException if query fails
     */
    public List<User> getUsersByDateRange(Timestamp startDate, Timestamp endDate) throws SQLException {
        String sql = """
            SELECT id, name, name_hash, created_at 
            FROM users 
            WHERE created_at BETWEEN ? AND ? 
            ORDER BY created_at DESC
            """;
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setTimestamp(1, startDate);
                pstmt.setTimestamp(2, endDate);
                
                List<User> users = new ArrayList<>();
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        users.add(mapResultSetToUser(rs));
                    }
                }
                return users;
            }
        });
    }
    
    /**
     * Check if user exists by name
     *
     * @param name User name to check
     * @return true if user exists, false otherwise
     * @throws SQLException if query fails
     */
    public boolean userExists(String name) throws SQLException {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        String nameHash = DigestUtils.sha256Hex(name.trim().toLowerCase());
        String sql = "SELECT 1 FROM users WHERE name_hash = ? LIMIT 1";

        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, nameHash);

                try (ResultSet rs = pstmt.executeQuery()) {
                    return rs.next();
                }
            }
        });
    }

    /**
     * Get user name suggestions for autocomplete
     *
     * @param query Search query
     * @param limit Maximum number of suggestions
     * @return List of user name suggestions
     * @throws SQLException if query fails
     */
    public List<String> getUserNameSuggestions(String query, int limit) throws SQLException {
        if (query == null || query.trim().length() < 2) {
            return new ArrayList<>();
        }

        String sql = """
            SELECT DISTINCT name
            FROM users
            WHERE name LIKE ?
            ORDER BY name
            LIMIT ?
            """;

        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, "%" + query.trim() + "%");
                pstmt.setInt(2, limit);

                List<String> suggestions = new ArrayList<>();
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        suggestions.add(rs.getString("name"));
                    }
                }
                return suggestions;
            }
        });
    }

    /**
     * Get or create user by name (alias for findOrCreateUser)
     *
     * @param name User name
     * @return Existing or newly created user
     * @throws SQLException if operation fails
     */
    public User getOrCreateUser(String name) throws SQLException {
        return findOrCreateUser(name);
    }
    
    // ========== STATISTICS METHODS ==========

    /**
     * Get count of users created on a specific date
     */
    public long getUserCountByDate(String date) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE DATE(created_at) = ?";

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
     * Get count of users created in a date range
     */
    public long getUserCountInDateRange(String startDate, String endDate) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE DATE(created_at) BETWEEN ? AND ?";

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
     * Get recent users with their activity
     */
    public List<Map<String, Object>> getRecentUsers(int limit) throws SQLException {
        String sql = """
            SELECT u.*,
                   COUNT(tl.id) as test_count,
                   MAX(tl.completed_at) as last_completed_test
            FROM users u
            LEFT JOIN test_links tl ON u.id = tl.user_id
            GROUP BY u.id, u.name, u.name_hash, u.created_at
            ORDER BY u.created_at DESC
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
                        result.put("name", rs.getString("name"));
                        result.put("createdAt", rs.getTimestamp("created_at"));
                        result.put("testCount", rs.getInt("test_count"));
                        result.put("lastCompletedTest", rs.getTimestamp("last_completed_test"));
                        results.add(result);
                    }
                }
            }
            return results;
        });
    }

    /**
     * Map ResultSet to User object
     *
     * @param rs ResultSet containing user data
     * @return User object
     * @throws SQLException if mapping fails
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        return new User(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("name_hash"),
            rs.getTimestamp("created_at")
        );
    }
}