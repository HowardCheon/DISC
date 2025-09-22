package com.disc.dao;

import com.disc.model.Admin;
import com.disc.util.DBUtil;
import org.apache.commons.codec.digest.DigestUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Data Access Object for Admin operations
 */
public class AdminDAO {
    
    private static final Logger logger = Logger.getLogger(AdminDAO.class.getName());
    
    /**
     * Create a new admin
     * 
     * @param admin Admin object to create
     * @return The created admin with ID populated
     * @throws SQLException if creation fails
     */
    public Admin createAdmin(Admin admin) throws SQLException {
        if (admin == null || !admin.isValid()) {
            throw new IllegalArgumentException("Invalid admin data");
        }
        
        // Hash password if it's not already hashed
        String passwordHash = admin.getPasswordHash();
        if (passwordHash == null || passwordHash.length() != 64) {
            // Assume it's plain text, hash it
            passwordHash = DigestUtils.sha256Hex(passwordHash);
        }
        
        String sql = """
            INSERT INTO admins (username, password_hash, created_at) 
            VALUES (?, ?, CURRENT_TIMESTAMP)
            """;
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, admin.getUsername().trim().toLowerCase());
                pstmt.setString(2, passwordHash);
                
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Creating admin failed, no rows affected");
                }
                
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        admin.setId(generatedKeys.getLong(1));
                        admin.setPasswordHash(passwordHash);
                    } else {
                        throw new SQLException("Creating admin failed, no ID obtained");
                    }
                }
                
                return getAdminById(admin.getId());
            }
        });
    }
    
    /**
     * Get admin by ID
     * 
     * @param adminId Admin ID
     * @return Admin object or null if not found
     * @throws SQLException if query fails
     */
    public Admin getAdminById(Long adminId) throws SQLException {
        if (adminId == null) {
            return null;
        }
        
        String sql = """
            SELECT id, username, password_hash, last_login_at, created_at 
            FROM admins WHERE id = ?
            """;
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setLong(1, adminId);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToAdmin(rs);
                    }
                    return null;
                }
            }
        });
    }
    
    /**
     * Get admin by username
     * 
     * @param username Admin username
     * @return Admin object or null if not found
     * @throws SQLException if query fails
     */
    public Admin getAdminByUsername(String username) throws SQLException {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        
        String sql = """
            SELECT id, username, password_hash, last_login_at, created_at 
            FROM admins WHERE username = ?
            """;
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, username.trim().toLowerCase());
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToAdmin(rs);
                    }
                    return null;
                }
            }
        });
    }
    
    /**
     * Authenticate admin with username and password
     * 
     * @param username Admin username
     * @param password Plain text password
     * @return Admin object if authentication successful, null otherwise
     * @throws SQLException if query fails
     */
    public Admin authenticate(String username, String password) throws SQLException {
        if (username == null || username.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            return null;
        }
        
        Admin admin = getAdminByUsername(username);
        if (admin == null) {
            return null;
        }
        
        // Verify password
        String hashedPassword = DigestUtils.sha256Hex(password);
        if (hashedPassword.equals(admin.getPasswordHash())) {
            // Update last login time
            updateLastLogin(admin.getId());
            admin.setLastLoginAt(new Timestamp(System.currentTimeMillis()));
            return admin;
        }
        
        return null;
    }
    
    /**
     * Update admin information
     * 
     * @param admin Admin object with updated information
     * @return true if update was successful
     * @throws SQLException if update fails
     */
    public boolean updateAdmin(Admin admin) throws SQLException {
        if (admin == null || admin.getId() == null || !admin.isValid()) {
            throw new IllegalArgumentException("Invalid admin data or missing ID");
        }
        
        String sql = """
            UPDATE admins 
            SET username = ?, password_hash = ? 
            WHERE id = ?
            """;
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, admin.getUsername().trim().toLowerCase());
                pstmt.setString(2, admin.getPasswordHash());
                pstmt.setLong(3, admin.getId());
                
                int rowsAffected = pstmt.executeUpdate();
                return rowsAffected > 0;
            }
        });
    }
    
    /**
     * Update admin password
     * 
     * @param adminId Admin ID
     * @param newPassword New plain text password
     * @return true if update was successful
     * @throws SQLException if update fails
     */
    public boolean updatePassword(Long adminId, String newPassword) throws SQLException {
        if (adminId == null || newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Admin ID and password cannot be null or empty");
        }
        
        String hashedPassword = DigestUtils.sha256Hex(newPassword);
        String sql = "UPDATE admins SET password_hash = ? WHERE id = ?";
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, hashedPassword);
                pstmt.setLong(2, adminId);
                
                int rowsAffected = pstmt.executeUpdate();
                return rowsAffected > 0;
            }
        });
    }
    
    /**
     * Update last login time
     * 
     * @param adminId Admin ID
     * @return true if update was successful
     * @throws SQLException if update fails
     */
    public boolean updateLastLogin(Long adminId) throws SQLException {
        if (adminId == null) {
            return false;
        }
        
        String sql = "UPDATE admins SET last_login_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setLong(1, adminId);
                
                int rowsAffected = pstmt.executeUpdate();
                return rowsAffected > 0;
            }
        });
    }
    
    /**
     * Delete admin by ID
     * 
     * @param adminId Admin ID to delete
     * @return true if deletion was successful
     * @throws SQLException if deletion fails
     */
    public boolean deleteAdmin(Long adminId) throws SQLException {
        if (adminId == null) {
            return false;
        }
        
        String sql = "DELETE FROM admins WHERE id = ?";
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setLong(1, adminId);
                
                int rowsAffected = pstmt.executeUpdate();
                return rowsAffected > 0;
            }
        });
    }
    
    /**
     * Get all admins with pagination
     * 
     * @param offset Starting position
     * @param limit Maximum number of records
     * @return List of admins
     * @throws SQLException if query fails
     */
    public List<Admin> getAdmins(int offset, int limit) throws SQLException {
        String sql = """
            SELECT id, username, password_hash, last_login_at, created_at 
            FROM admins 
            ORDER BY created_at DESC 
            LIMIT ? OFFSET ?
            """;
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, limit);
                pstmt.setInt(2, offset);
                
                List<Admin> admins = new ArrayList<>();
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        admins.add(mapResultSetToAdmin(rs));
                    }
                }
                return admins;
            }
        });
    }
    
    /**
     * Get all admins
     * 
     * @return List of all admins
     * @throws SQLException if query fails
     */
    public List<Admin> getAllAdmins() throws SQLException {
        String sql = """
            SELECT id, username, password_hash, last_login_at, created_at 
            FROM admins 
            ORDER BY username
            """;
        
        return DBUtil.executeWithConnection(connection -> {
            List<Admin> admins = new ArrayList<>();
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                while (rs.next()) {
                    admins.add(mapResultSetToAdmin(rs));
                }
            }
            return admins;
        });
    }
    
    /**
     * Get total count of admins
     * 
     * @return Total number of admins
     * @throws SQLException if query fails
     */
    public long getAdminCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM admins";
        
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
     * Check if admin exists by username
     * 
     * @param username Username to check
     * @return true if admin exists, false otherwise
     * @throws SQLException if query fails
     */
    public boolean adminExists(String username) throws SQLException {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        
        String sql = "SELECT 1 FROM admins WHERE username = ? LIMIT 1";
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, username.trim().toLowerCase());
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    return rs.next();
                }
            }
        });
    }
    
    /**
     * Check if username is available (not taken)
     * 
     * @param username Username to check
     * @return true if username is available, false if taken
     * @throws SQLException if query fails
     */
    public boolean isUsernameAvailable(String username) throws SQLException {
        return !adminExists(username);
    }
    
    /**
     * Check if username is available for update (not taken by another admin)
     * 
     * @param username Username to check
     * @param excludeAdminId Admin ID to exclude from check
     * @return true if username is available, false if taken
     * @throws SQLException if query fails
     */
    public boolean isUsernameAvailableForUpdate(String username, Long excludeAdminId) throws SQLException {
        if (username == null || username.trim().isEmpty() || excludeAdminId == null) {
            return false;
        }
        
        String sql = "SELECT 1 FROM admins WHERE username = ? AND id != ? LIMIT 1";
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, username.trim().toLowerCase());
                pstmt.setLong(2, excludeAdminId);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    return !rs.next(); // Available if no results found
                }
            }
        });
    }
    
    /**
     * Get admins who logged in within a date range
     * 
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of admins who logged in within the date range
     * @throws SQLException if query fails
     */
    public List<Admin> getAdminsByLoginDateRange(Timestamp startDate, Timestamp endDate) throws SQLException {
        String sql = """
            SELECT id, username, password_hash, last_login_at, created_at 
            FROM admins 
            WHERE last_login_at BETWEEN ? AND ? 
            ORDER BY last_login_at DESC
            """;
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setTimestamp(1, startDate);
                pstmt.setTimestamp(2, endDate);
                
                List<Admin> admins = new ArrayList<>();
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        admins.add(mapResultSetToAdmin(rs));
                    }
                }
                return admins;
            }
        });
    }
    
    /**
     * Get recently active admins
     * 
     * @param limit Maximum number of records
     * @return List of recently active admins
     * @throws SQLException if query fails
     */
    public List<Admin> getRecentlyActiveAdmins(int limit) throws SQLException {
        String sql = """
            SELECT id, username, password_hash, last_login_at, created_at 
            FROM admins 
            WHERE last_login_at IS NOT NULL 
            ORDER BY last_login_at DESC 
            LIMIT ?
            """;
        
        return DBUtil.executeWithConnection(connection -> {
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, limit);
                
                List<Admin> admins = new ArrayList<>();
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        admins.add(mapResultSetToAdmin(rs));
                    }
                }
                return admins;
            }
        });
    }
    
    /**
     * Get admins who have never logged in
     * 
     * @return List of admins who have never logged in
     * @throws SQLException if query fails
     */
    public List<Admin> getAdminsNeverLoggedIn() throws SQLException {
        String sql = """
            SELECT id, username, password_hash, last_login_at, created_at 
            FROM admins 
            WHERE last_login_at IS NULL 
            ORDER BY created_at DESC
            """;
        
        return DBUtil.executeWithConnection(connection -> {
            List<Admin> admins = new ArrayList<>();
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                while (rs.next()) {
                    admins.add(mapResultSetToAdmin(rs));
                }
            }
            return admins;
        });
    }
    
    /**
     * Verify current password for an admin
     * 
     * @param adminId Admin ID
     * @param currentPassword Current plain text password
     * @return true if password is correct, false otherwise
     * @throws SQLException if query fails
     */
    public boolean verifyCurrentPassword(Long adminId, String currentPassword) throws SQLException {
        if (adminId == null || currentPassword == null) {
            return false;
        }
        
        Admin admin = getAdminById(adminId);
        if (admin == null) {
            return false;
        }
        
        String hashedPassword = DigestUtils.sha256Hex(currentPassword);
        return hashedPassword.equals(admin.getPasswordHash());
    }
    
    /**
     * Change admin password with current password verification
     * 
     * @param adminId Admin ID
     * @param currentPassword Current plain text password
     * @param newPassword New plain text password
     * @return true if password change was successful, false otherwise
     * @throws SQLException if operation fails
     */
    public boolean changePassword(Long adminId, String currentPassword, String newPassword) throws SQLException {
        if (!verifyCurrentPassword(adminId, currentPassword)) {
            return false;
        }
        
        return updatePassword(adminId, newPassword);
    }
    
    /**
     * Get login statistics
     * 
     * @return Array of counts: [total_admins, logged_in_admins, never_logged_in_admins]
     * @throws SQLException if query fails
     */
    public long[] getLoginStatistics() throws SQLException {
        String sql = """
            SELECT 
                COUNT(*) as total,
                SUM(CASE WHEN last_login_at IS NOT NULL THEN 1 ELSE 0 END) as logged_in,
                SUM(CASE WHEN last_login_at IS NULL THEN 1 ELSE 0 END) as never_logged_in
            FROM admins
            """;
        
        return DBUtil.executeWithConnection(connection -> {
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                if (rs.next()) {
                    return new long[]{
                        rs.getLong("total"),
                        rs.getLong("logged_in"),
                        rs.getLong("never_logged_in")
                    };
                }
                return new long[]{0, 0, 0};
            }
        });
    }
    
    /**
     * Map ResultSet to Admin object
     * 
     * @param rs ResultSet containing admin data
     * @return Admin object
     * @throws SQLException if mapping fails
     */
    private Admin mapResultSetToAdmin(ResultSet rs) throws SQLException {
        return new Admin(
            rs.getLong("id"),
            rs.getString("username"),
            rs.getString("password_hash"),
            rs.getTimestamp("last_login_at"),
            rs.getTimestamp("created_at")
        );
    }
}