package com.disc.controller;

import com.disc.dao.AdminDAO;
import com.disc.model.Admin;
import com.disc.util.DBUtil;
import org.apache.commons.codec.digest.DigestUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Servlet for handling admin authentication
 */
@WebServlet("/admin/login")
public class AdminLoginServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(AdminLoginServlet.class.getName());

    // Session constants
    private static final String ADMIN_SESSION_KEY = "adminUser";
    private static final String ADMIN_LOGIN_TIME_KEY = "adminLoginTime";
    private static final String ADMIN_LAST_ACTIVITY_KEY = "adminLastActivity";
    private static final int SESSION_TIMEOUT_MINUTES = 30;

    private AdminDAO adminDAO;

    @Override
    public void init() throws ServletException {
        super.init();

        try {
            // Initialize database if not already done
            if (!DBUtil.isInitialized()) {
                DBUtil.initialize(getServletContext());
            }

            // Initialize DAO
            adminDAO = new AdminDAO();

            logger.info("AdminLoginServlet initialized successfully");

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to initialize AdminLoginServlet", e);
            throw new ServletException("Database initialization failed", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if ("logout".equals(action)) {
            handleLogout(request, response);
            return;
        }

        // Check if admin is already logged in
        HttpSession session = request.getSession(false);
        if (session != null && isValidAdminSession(session)) {
            // Already logged in, redirect to dashboard
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            return;
        }

        // Show login form
        request.getRequestDispatcher("/jsp/admin/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String rememberMe = request.getParameter("rememberMe");

        // Validate input parameters
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {

            request.setAttribute("errorMessage", "사용자명과 비밀번호를 모두 입력해주세요.");
            request.getRequestDispatcher("/jsp/admin/login.jsp").forward(request, response);
            return;
        }

        try {
            // Authenticate admin
            Admin admin = authenticateAdmin(username.trim(), password);

            if (admin != null) {
                // Login successful
                createAdminSession(request, admin, "true".equals(rememberMe));

                // Update last login time
                updateLastLogin(admin.getId());

                // Log successful login
                logger.info("Admin login successful: " + username +
                           " from IP: " + getClientIP(request));

                // Redirect to dashboard or requested page
                String redirectUrl = getRedirectUrl(request);
                response.sendRedirect(redirectUrl);

            } else {
                // Login failed
                logger.warning("Admin login failed: " + username +
                              " from IP: " + getClientIP(request));

                request.setAttribute("errorMessage", "잘못된 사용자명 또는 비밀번호입니다.");
                request.setAttribute("username", username); // Keep username for convenience
                request.getRequestDispatcher("/jsp/admin/login.jsp").forward(request, response);
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error during admin login", e);
            request.setAttribute("errorMessage", "시스템 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            request.getRequestDispatcher("/jsp/admin/login.jsp").forward(request, response);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error during admin login", e);
            request.setAttribute("errorMessage", "로그인 처리 중 오류가 발생했습니다.");
            request.getRequestDispatcher("/jsp/admin/login.jsp").forward(request, response);
        }
    }

    /**
     * Authenticate admin user
     *
     * @param username Admin username
     * @param password Plain text password
     * @return Admin object if authentication successful, null otherwise
     * @throws SQLException if database error occurs
     */
    private Admin authenticateAdmin(String username, String password) throws SQLException {
        // Hash the provided password
        String hashedPassword = hashPassword(password);

        // Get admin by username
        Admin admin = adminDAO.getAdminByUsername(username);

        if (admin != null && admin.getPasswordHash().equals(hashedPassword)) {
            return admin;
        }

        return null;
    }

    /**
     * Create admin session
     *
     * @param request HTTP request
     * @param admin Admin user
     * @param rememberMe Whether to extend session
     */
    private void createAdminSession(HttpServletRequest request, Admin admin, boolean rememberMe) {
        HttpSession session = request.getSession(true);

        // Set session timeout
        int timeoutMinutes = rememberMe ? 480 : SESSION_TIMEOUT_MINUTES; // 8 hours vs 30 minutes
        session.setMaxInactiveInterval(timeoutMinutes * 60);

        // Store admin information in session
        session.setAttribute(ADMIN_SESSION_KEY, admin);
        session.setAttribute(ADMIN_LOGIN_TIME_KEY, LocalDateTime.now());
        session.setAttribute(ADMIN_LAST_ACTIVITY_KEY, LocalDateTime.now());

        // Additional session attributes
        session.setAttribute("adminId", admin.getId());
        session.setAttribute("adminUsername", admin.getUsername());
        session.setAttribute("isAdminLoggedIn", true);

        logger.info("Admin session created for user: " + admin.getUsername() +
                   " (timeout: " + timeoutMinutes + " minutes)");
    }

    /**
     * Handle admin logout
     *
     * @param request HTTP request
     * @param response HTTP response
     * @throws IOException if redirect fails
     */
    private void handleLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);

        if (session != null) {
            String username = (String) session.getAttribute("adminUsername");

            // Invalidate session
            session.invalidate();

            logger.info("Admin logout: " + (username != null ? username : "unknown"));
        }

        // Redirect to login page with logout message
        response.sendRedirect(request.getContextPath() +
                             "/admin/login?message=logout_success");
    }

    /**
     * Check if admin session is valid
     *
     * @param session HTTP session
     * @return true if valid admin session exists
     */
    private boolean isValidAdminSession(HttpSession session) {
        if (session == null) {
            return false;
        }

        Admin admin = (Admin) session.getAttribute(ADMIN_SESSION_KEY);
        Boolean isLoggedIn = (Boolean) session.getAttribute("isAdminLoggedIn");

        if (admin == null || !Boolean.TRUE.equals(isLoggedIn)) {
            return false;
        }

        // Update last activity time
        session.setAttribute(ADMIN_LAST_ACTIVITY_KEY, LocalDateTime.now());

        return true;
    }

    /**
     * Update admin's last login time
     *
     * @param adminId Admin ID
     */
    private void updateLastLogin(Long adminId) {
        try {
            adminDAO.updateLastLogin(adminId);
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Failed to update last login time for admin: " + adminId, e);
        }
    }

    /**
     * Get redirect URL after successful login
     *
     * @param request HTTP request
     * @return Redirect URL
     */
    private String getRedirectUrl(HttpServletRequest request) {
        // Check for requested URL before login
        String requestedUrl = (String) request.getSession().getAttribute("requestedAdminUrl");

        if (requestedUrl != null && !requestedUrl.isEmpty()) {
            // Remove from session and use it
            request.getSession().removeAttribute("requestedAdminUrl");
            return requestedUrl;
        }

        // Default to admin dashboard
        return request.getContextPath() + "/admin/dashboard";
    }

    /**
     * Get client IP address
     *
     * @param request HTTP request
     * @return Client IP address
     */
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");

        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty() && !"unknown".equalsIgnoreCase(xRealIP)) {
            return xRealIP;
        }

        return request.getRemoteAddr();
    }

    /**
     * Hash password using SHA-256
     *
     * @param password Plain text password
     * @return Hashed password
     */
    private String hashPassword(String password) {
        return DigestUtils.sha256Hex(password);
    }

    /**
     * Check if request is from AJAX
     *
     * @param request HTTP request
     * @return true if AJAX request
     */
    private boolean isAjaxRequest(HttpServletRequest request) {
        String requestedWith = request.getHeader("X-Requested-With");
        return "XMLHttpRequest".equals(requestedWith);
    }

    @Override
    public void destroy() {
        super.destroy();
        logger.info("AdminLoginServlet destroyed");
    }
}