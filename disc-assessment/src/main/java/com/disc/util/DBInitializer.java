package com.disc.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Database Initializer for DISC Assessment Application
 * 
 * This class handles the creation of database tables, indexes, and initial data
 * for the DISC personality assessment system.
 */
public class DBInitializer {
    
    private static final Logger logger = Logger.getLogger(DBInitializer.class.getName());
    
    // Default admin credentials
    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin1234";
    
    /**
     * Initialize the database schema and default data
     * 
     * @throws SQLException if database initialization fails
     */
    public static void initializeDatabase() throws SQLException {
        logger.info("Starting database initialization...");
        
        try {
            // Create tables
            createTables();
            
            // Create indexes
            createIndexes();
            
            // Insert initial data
            insertInitialData();
            
            logger.info("Database initialization completed successfully");
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database initialization failed", e);
            throw e;
        }
    }
    
    /**
     * Create all required tables
     * 
     * @throws SQLException if table creation fails
     */
    private static void createTables() throws SQLException {
        logger.info("Creating database tables...");
        
        DBUtil.executeWithConnection(connection -> {
            try (Statement stmt = connection.createStatement()) {
                
                // Create users table
                createUsersTable(stmt);
                
                // Create test_links table
                createTestLinksTable(stmt);
                
                // Create answers table
                createAnswersTable(stmt);
                
                // Create results table
                createResultsTable(stmt);
                
                // Create admins table
                createAdminsTable(stmt);
                
                return null;
            }
        });
        
        logger.info("All tables created successfully");
    }
    
    /**
     * Create users table for test takers
     */
    private static void createUsersTable(Statement stmt) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                name_hash TEXT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;
        
        stmt.execute(sql);
        logger.fine("Users table created");
    }
    
    /**
     * Create test_links table for managing test URLs
     */
    private static void createTestLinksTable(Statement stmt) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS test_links (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                test_url TEXT UNIQUE NOT NULL,
                status TEXT DEFAULT '검사전' CHECK(status IN ('검사전', '검사중', '검사완료')),
                started_at TIMESTAMP,
                completed_at TIMESTAMP,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
            )
            """;
        
        stmt.execute(sql);
        logger.fine("Test_links table created");
    }
    
    /**
     * Create answers table for storing test responses
     */
    private static void createAnswersTable(Statement stmt) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS answers (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                test_link_id INTEGER NOT NULL,
                question_num INTEGER NOT NULL,
                most_like TEXT,
                least_like TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY(test_link_id) REFERENCES test_links(id) ON DELETE CASCADE,
                UNIQUE(test_link_id, question_num)
            )
            """;
        
        stmt.execute(sql);
        logger.fine("Answers table created");
    }
    
    /**
     * Create results table for storing test results
     */
    private static void createResultsTable(Statement stmt) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS results (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                test_link_id INTEGER UNIQUE NOT NULL,
                d_score INTEGER DEFAULT 0,
                i_score INTEGER DEFAULT 0,
                s_score INTEGER DEFAULT 0,
                c_score INTEGER DEFAULT 0,
                result_type TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY(test_link_id) REFERENCES test_links(id) ON DELETE CASCADE
            )
            """;
        
        stmt.execute(sql);
        logger.fine("Results table created");
    }
    
    /**
     * Create admins table for administrator accounts
     */
    private static void createAdminsTable(Statement stmt) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS admins (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                password_hash TEXT NOT NULL,
                last_login_at TIMESTAMP,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;
        
        stmt.execute(sql);
        logger.fine("Admins table created");
    }
    
    /**
     * Create all required indexes for performance optimization
     * 
     * @throws SQLException if index creation fails
     */
    private static void createIndexes() throws SQLException {
        logger.info("Creating database indexes...");
        
        DBUtil.executeWithConnection(connection -> {
            try (Statement stmt = connection.createStatement()) {
                
                // Users table indexes
                createIndexIfNotExists(stmt, "idx_users_name_hash", "users", "name_hash");
                
                // Test_links table indexes
                createIndexIfNotExists(stmt, "idx_test_links_url", "test_links", "test_url");
                createIndexIfNotExists(stmt, "idx_test_links_user_id", "test_links", "user_id");
                createIndexIfNotExists(stmt, "idx_test_links_status", "test_links", "status");
                createIndexIfNotExists(stmt, "idx_test_links_created_at", "test_links", "created_at");
                
                // Answers table indexes
                createIndexIfNotExists(stmt, "idx_answers_test_link_id", "answers", "test_link_id");
                createIndexIfNotExists(stmt, "idx_answers_question_num", "answers", "question_num");
                
                // Results table indexes
                createIndexIfNotExists(stmt, "idx_results_test_link_id", "results", "test_link_id");
                createIndexIfNotExists(stmt, "idx_results_result_type", "results", "result_type");
                createIndexIfNotExists(stmt, "idx_results_created_at", "results", "created_at");
                
                // Admins table indexes
                createIndexIfNotExists(stmt, "idx_admins_username", "admins", "username");
                
                return null;
            }
        });
        
        logger.info("All indexes created successfully");
    }
    
    /**
     * Create an index if it doesn't already exist
     */
    private static void createIndexIfNotExists(Statement stmt, String indexName, String tableName, String columnName) throws SQLException {
        // Check if index exists
        String checkSql = """
            SELECT name FROM sqlite_master 
            WHERE type='index' AND name=?
            """;
        
        try (PreparedStatement pstmt = stmt.getConnection().prepareStatement(checkSql)) {
            pstmt.setString(1, indexName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    // Index doesn't exist, create it
                    String createSql = String.format("CREATE INDEX %s ON %s (%s)", indexName, tableName, columnName);
                    stmt.execute(createSql);
                    logger.fine("Index created: " + indexName);
                }
            }
        }
    }
    
    /**
     * Insert initial data (default admin account)
     * 
     * @throws SQLException if data insertion fails
     */
    private static void insertInitialData() throws SQLException {
        logger.info("Inserting initial data...");
        
        // Check if admin already exists
        if (adminExists()) {
            logger.info("Admin account already exists, skipping initial data insertion");
            return;
        }
        
        // Insert default admin account
        insertDefaultAdmin();
        
        logger.info("Initial data inserted successfully");
    }
    
    /**
     * Check if admin account already exists
     * 
     * @return true if admin exists, false otherwise
     * @throws SQLException if query fails
     */
    private static boolean adminExists() throws SQLException {
        return DBUtil.executeWithConnection(connection -> {
            String sql = "SELECT COUNT(*) FROM admins WHERE username = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, DEFAULT_ADMIN_USERNAME);
                try (ResultSet rs = pstmt.executeQuery()) {
                    return rs.next() && rs.getInt(1) > 0;
                }
            }
        });
    }
    
    /**
     * Insert default admin account
     * 
     * @throws SQLException if insertion fails
     */
    private static void insertDefaultAdmin() throws SQLException {
        DBUtil.executeWithConnection(connection -> {
            String sql = """
                INSERT INTO admins (username, password_hash, created_at) 
                VALUES (?, ?, CURRENT_TIMESTAMP)
                """;
            
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, DEFAULT_ADMIN_USERNAME);
                pstmt.setString(2, hashPassword(DEFAULT_ADMIN_PASSWORD));
                
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    logger.info("Default admin account created: " + DEFAULT_ADMIN_USERNAME);
                } else {
                    throw new SQLException("Failed to create default admin account");
                }
                
                return null;
            }
        });
    }
    
    /**
     * Hash password using SHA-256
     * 
     * @param password The plain text password
     * @return The hashed password
     */
    private static String hashPassword(String password) {
        return DigestUtils.sha256Hex(password);
    }
    
    /**
     * Verify database schema integrity
     * 
     * @return true if schema is valid, false otherwise
     */
    public static boolean verifySchema() {
        try {
            return DBUtil.executeWithConnection(connection -> {
                try (Statement stmt = connection.createStatement()) {
                    
                    // Check all required tables exist
                    String[] requiredTables = {"users", "test_links", "answers", "results", "admins"};
                    
                    for (String table : requiredTables) {
                        String sql = """
                            SELECT name FROM sqlite_master 
                            WHERE type='table' AND name=?
                            """;
                        
                        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                            pstmt.setString(1, table);
                            try (ResultSet rs = pstmt.executeQuery()) {
                                if (!rs.next()) {
                                    logger.warning("Required table missing: " + table);
                                    return false;
                                }
                            }
                        }
                    }
                    
                    logger.info("Database schema verification passed");
                    return true;
                }
            });
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Schema verification failed", e);
            return false;
        }
    }
    
    /**
     * Get table information for debugging
     * 
     * @param tableName The name of the table
     * @return Table schema information
     */
    public static String getTableInfo(String tableName) {
        try {
            return DBUtil.executeWithConnection(connection -> {
                String sql = "PRAGMA table_info(" + tableName + ")";
                StringBuilder info = new StringBuilder();
                info.append("Table: ").append(tableName).append("\n");
                
                try (Statement stmt = connection.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {
                    
                    while (rs.next()) {
                        info.append("  Column: ").append(rs.getString("name"))
                            .append(" (").append(rs.getString("type")).append(")")
                            .append(rs.getBoolean("notnull") ? " NOT NULL" : "")
                            .append(rs.getString("dflt_value") != null ? " DEFAULT " + rs.getString("dflt_value") : "")
                            .append(rs.getBoolean("pk") ? " PRIMARY KEY" : "")
                            .append("\n");
                    }
                }
                
                return info.toString();
            });
        } catch (SQLException e) {
            return "Error getting table info: " + e.getMessage();
        }
    }
    
    /**
     * Get database statistics
     * 
     * @return Database statistics
     */
    public static String getDatabaseStatistics() {
        try {
            return DBUtil.executeWithConnection(connection -> {
                StringBuilder stats = new StringBuilder();
                
                String[] tables = {"users", "test_links", "answers", "results", "admins"};
                
                for (String table : tables) {
                    String sql = "SELECT COUNT(*) as count FROM " + table;
                    try (Statement stmt = connection.createStatement();
                         ResultSet rs = stmt.executeQuery(sql)) {
                        
                        if (rs.next()) {
                            stats.append(table).append(": ").append(rs.getInt("count")).append(" records\n");
                        }
                    }
                }
                
                return stats.toString();
            });
        } catch (SQLException e) {
            return "Error getting statistics: " + e.getMessage();
        }
    }
    
    /**
     * Clean up test data (for development/testing purposes)
     * WARNING: This will delete all test data!
     * 
     * @throws SQLException if cleanup fails
     */
    public static void cleanupTestData() throws SQLException {
        logger.warning("Cleaning up all test data...");
        
        DBUtil.executeTransaction(connection -> {
            try (Statement stmt = connection.createStatement()) {
                // Delete in reverse order of dependencies
                stmt.execute("DELETE FROM results");
                stmt.execute("DELETE FROM answers");
                stmt.execute("DELETE FROM test_links");
                stmt.execute("DELETE FROM users");
                
                // Reset auto-increment counters
                stmt.execute("DELETE FROM sqlite_sequence WHERE name IN ('users', 'test_links', 'answers', 'results')");
                
                logger.info("Test data cleanup completed");
                return null;
            }
        });
    }
}