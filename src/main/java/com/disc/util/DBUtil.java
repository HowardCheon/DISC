package com.disc.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.File;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.servlet.ServletContext;

/**
 * SQLite Database Connection Utility
 * 
 * This class manages SQLite database connections with connection pooling
 * for the DISC assessment application.
 */
public class DBUtil {
    
    private static final Logger logger = Logger.getLogger(DBUtil.class.getName());
    
    // Database configuration
    private static final String DB_NAME = "disc.db";
    private static final String DB_DIRECTORY = "/WEB-INF/data/";
    
    // Connection pool configuration
    private static final int INITIAL_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 20;
    private static final int CONNECTION_TIMEOUT = 30000; // 30 seconds
    
    // Connection pool
    private static ConcurrentLinkedQueue<Connection> connectionPool = new ConcurrentLinkedQueue<>();
    private static int currentPoolSize = 0;
    private static String dbPath = null;
    private static boolean initialized = false;
    
    // SQLite JDBC URL
    private static String jdbcUrl = null;
    
    /**
     * Initialize the database utility with servlet context
     * 
     * @param servletContext The servlet context to get the real path
     * @throws SQLException if database initialization fails
     */
    public static synchronized void initialize(ServletContext servletContext) throws SQLException {
        if (initialized) {
            return;
        }
        
        try {
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            
            // Get the real path to the database directory
            String realPath = servletContext.getRealPath(DB_DIRECTORY);
            if (realPath == null) {
                throw new SQLException("Cannot determine real path for database directory");
            }
            
            // Create directory if it doesn't exist
            File dbDirectory = new File(realPath);
            if (!dbDirectory.exists()) {
                if (!dbDirectory.mkdirs()) {
                    throw new SQLException("Cannot create database directory: " + realPath);
                }
            }
            
            // Set database path
            dbPath = realPath + File.separator + DB_NAME;
            jdbcUrl = "jdbc:sqlite:" + dbPath;
            
            // Initialize connection pool
            initializeConnectionPool();
            
            // Initialize database schema
            DBInitializer.initializeDatabase();
            
            initialized = true;
            logger.info("Database initialized successfully at: " + dbPath);
            
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found", e);
        } catch (Exception e) {
            throw new SQLException("Database initialization failed", e);
        }
    }
    
    /**
     * Initialize the connection pool
     * 
     * @throws SQLException if connection pool initialization fails
     */
    private static void initializeConnectionPool() throws SQLException {
        for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
            Connection connection = createNewConnection();
            connectionPool.offer(connection);
            currentPoolSize++;
        }
        logger.info("Connection pool initialized with " + INITIAL_POOL_SIZE + " connections");
    }
    
    /**
     * Create a new database connection
     * 
     * @return A new SQLite connection
     * @throws SQLException if connection creation fails
     */
    private static Connection createNewConnection() throws SQLException {
        if (jdbcUrl == null) {
            throw new SQLException("Database not initialized. Call initialize() first.");
        }
        
        Connection connection = DriverManager.getConnection(jdbcUrl);
        
        // Enable foreign key constraints
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
            stmt.execute("PRAGMA journal_mode = WAL"); // Write-Ahead Logging for better concurrency
            stmt.execute("PRAGMA synchronous = NORMAL"); // Balanced performance/safety
            stmt.execute("PRAGMA temp_store = MEMORY"); // Store temporary data in memory
            stmt.execute("PRAGMA mmap_size = 268435456"); // 256MB memory-mapped I/O
        }
        
        return connection;
    }
    
    /**
     * Get a connection from the pool
     * 
     * @return A database connection
     * @throws SQLException if no connection is available
     */
    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            throw new SQLException("Database not initialized. Call initialize() first.");
        }
        
        Connection connection = connectionPool.poll();
        
        if (connection == null || connection.isClosed()) {
            // Create new connection if pool is empty or connection is closed
            if (currentPoolSize < MAX_POOL_SIZE) {
                connection = createNewConnection();
                currentPoolSize++;
                logger.fine("Created new connection. Pool size: " + currentPoolSize);
            } else {
                throw new SQLException("Maximum pool size reached. No connections available.");
            }
        }
        
        // Test connection validity
        if (!isConnectionValid(connection)) {
            connection.close();
            currentPoolSize--;
            return getConnection(); // Recursive call to get a new connection
        }
        
        return connection;
    }
    
    /**
     * Return a connection to the pool
     * 
     * @param connection The connection to return
     */
    public static void releaseConnection(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed() && isConnectionValid(connection)) {
                    // Reset connection state
                    connection.setAutoCommit(true);
                    connectionPool.offer(connection);
                } else {
                    connection.close();
                    currentPoolSize--;
                }
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Error releasing connection", e);
                try {
                    connection.close();
                    currentPoolSize--;
                } catch (SQLException ex) {
                    logger.log(Level.SEVERE, "Error closing connection", ex);
                }
            }
        }
    }
    
    /**
     * Check if a connection is valid
     * 
     * @param connection The connection to check
     * @return true if the connection is valid, false otherwise
     */
    private static boolean isConnectionValid(Connection connection) {
        try {
            return connection != null && 
                   !connection.isClosed() && 
                   connection.isValid(CONNECTION_TIMEOUT / 1000);
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * Execute a database operation with automatic connection management
     * 
     * @param operation The database operation to execute
     * @return The result of the operation
     * @throws SQLException if the operation fails
     */
    public static <T> T executeWithConnection(DatabaseOperation<T> operation) throws SQLException {
        Connection connection = getConnection();
        try {
            return operation.execute(connection);
        } finally {
            releaseConnection(connection);
        }
    }
    
    /**
     * Execute a database transaction with automatic connection management
     * 
     * @param operation The database operation to execute
     * @return The result of the operation
     * @throws SQLException if the operation fails
     */
    public static <T> T executeTransaction(DatabaseOperation<T> operation) throws SQLException {
        Connection connection = getConnection();
        try {
            connection.setAutoCommit(false);
            T result = operation.execute(connection);
            connection.commit();
            return result;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                logger.log(Level.SEVERE, "Error rolling back transaction", rollbackEx);
            }
            throw e;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Error resetting auto-commit", e);
            }
            releaseConnection(connection);
        }
    }
    
    /**
     * Get the database file path
     * 
     * @return The absolute path to the database file
     */
    public static String getDatabasePath() {
        return dbPath;
    }
    
    /**
     * Get the current pool size
     * 
     * @return The current number of connections in the pool
     */
    public static int getCurrentPoolSize() {
        return currentPoolSize;
    }
    
    /**
     * Get the available connections count
     * 
     * @return The number of available connections in the pool
     */
    public static int getAvailableConnections() {
        return connectionPool.size();
    }
    
    /**
     * Close all connections and clean up resources
     */
    public static synchronized void cleanup() {
        logger.info("Cleaning up database connections...");
        
        Connection connection;
        while ((connection = connectionPool.poll()) != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Error closing connection during cleanup", e);
            }
        }
        
        currentPoolSize = 0;
        initialized = false;
        logger.info("Database cleanup completed");
    }
    
    /**
     * Check if the database is initialized
     * 
     * @return true if initialized, false otherwise
     */
    public static boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Functional interface for database operations
     * 
     * @param <T> The return type of the operation
     */
    @FunctionalInterface
    public interface DatabaseOperation<T> {
        T execute(Connection connection) throws SQLException;
    }
    
    /**
     * Health check for the database
     * 
     * @return true if database is healthy, false otherwise
     */
    public static boolean healthCheck() {
        try {
            return executeWithConnection(connection -> {
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("SELECT 1");
                    return true;
                }
            });
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Database health check failed", e);
            return false;
        }
    }
    
    /**
     * Get database statistics
     * 
     * @return Database statistics as a formatted string
     */
    public static String getDatabaseStats() {
        try {
            return executeWithConnection(connection -> {
                StringBuilder stats = new StringBuilder();
                stats.append("Database Path: ").append(dbPath).append("\n");
                stats.append("Pool Size: ").append(currentPoolSize).append("\n");
                stats.append("Available Connections: ").append(connectionPool.size()).append("\n");
                stats.append("Database File Exists: ").append(new File(dbPath).exists()).append("\n");
                
                try (Statement stmt = connection.createStatement()) {
                    // Get database size
                    var rs = stmt.executeQuery("PRAGMA page_count");
                    if (rs.next()) {
                        int pageCount = rs.getInt(1);
                        rs.close();
                        
                        rs = stmt.executeQuery("PRAGMA page_size");
                        if (rs.next()) {
                            int pageSize = rs.getInt(1);
                            long dbSize = (long) pageCount * pageSize;
                            stats.append("Database Size: ").append(dbSize).append(" bytes\n");
                        }
                    }
                }
                
                return stats.toString();
            });
        } catch (SQLException e) {
            return "Error getting database stats: " + e.getMessage();
        }
    }
}