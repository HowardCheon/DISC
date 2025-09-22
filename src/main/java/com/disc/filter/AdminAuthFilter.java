package com.disc.filter;

import com.disc.model.Admin;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Filter to protect admin pages from unauthorized access
 *
 * This filter checks if the user has a valid admin session before allowing
 * access to admin-protected URLs.
 */
@WebFilter(urlPatterns = {"/admin/*", "/jsp/admin/*"})
public class AdminAuthFilter implements Filter {

    private static final Logger logger = Logger.getLogger(AdminAuthFilter.class.getName());

    // Session keys
    private static final String ADMIN_SESSION_KEY = "adminUser";
    private static final String ADMIN_LAST_ACTIVITY_KEY = "adminLastActivity";
    private static final String ADMIN_LOGIN_TIME_KEY = "adminLoginTime";

    // URLs that don't require authentication
    private static final Set<String> EXCLUDED_PATHS = new HashSet<>(Arrays.asList(
        "/admin/login",
        "/admin/login.jsp",
        "/jsp/admin/login.jsp"
    ));

    // Static resources that don't require authentication
    private static final Set<String> EXCLUDED_EXTENSIONS = new HashSet<>(Arrays.asList(
        ".css", ".js", ".png", ".jpg", ".jpeg", ".gif", ".ico", ".svg", ".woff", ".woff2", ".ttf"
    ));

    // Session timeout in minutes
    private static final int SESSION_TIMEOUT_MINUTES = 30;
    private static final int EXTENDED_SESSION_TIMEOUT_MINUTES = 480; // 8 hours

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("AdminAuthFilter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();

        // Remove context path from URI for easier comparison
        String path = requestURI.substring(contextPath.length());

        try {
            // Check if the path should be excluded from authentication
            if (shouldExcludeFromAuth(path)) {
                chain.doFilter(request, response);
                return;
            }

            // Check admin authentication
            if (isValidAdminSession(httpRequest)) {
                // Update last activity time
                updateLastActivity(httpRequest);

                // Allow access to protected resource
                chain.doFilter(request, response);
            } else {
                // Handle unauthorized access
                handleUnauthorizedAccess(httpRequest, httpResponse, path);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in AdminAuthFilter", e);

            // Redirect to error page or login page
            redirectToLogin(httpRequest, httpResponse, "시스템 오류가 발생했습니다.");
        }
    }

    /**
     * Check if the path should be excluded from authentication
     *
     * @param path Request path
     * @return true if should be excluded
     */
    private boolean shouldExcludeFromAuth(String path) {
        // Check exact path matches
        if (EXCLUDED_PATHS.contains(path)) {
            return true;
        }

        // Check file extensions
        for (String extension : EXCLUDED_EXTENSIONS) {
            if (path.toLowerCase().endsWith(extension)) {
                return true;
            }
        }

        // Check if it's a login-related path
        if (path.contains("/login")) {
            return true;
        }

        return false;
    }

    /**
     * Check if the request has a valid admin session
     *
     * @param request HTTP request
     * @return true if valid admin session exists
     */
    private boolean isValidAdminSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            logger.fine("No session found for admin request");
            return false;
        }

        // Check if admin is logged in
        Admin admin = (Admin) session.getAttribute(ADMIN_SESSION_KEY);
        Boolean isLoggedIn = (Boolean) session.getAttribute("isAdminLoggedIn");

        if (admin == null || !Boolean.TRUE.equals(isLoggedIn)) {
            logger.fine("No valid admin user in session");
            return false;
        }

        // Check session timeout
        if (isSessionExpired(session)) {
            logger.info("Admin session expired for user: " + admin.getUsername());

            // Invalidate expired session
            try {
                session.invalidate();
            } catch (IllegalStateException e) {
                logger.warning("Session was already invalidated");
            }

            return false;
        }

        return true;
    }

    /**
     * Check if the session has expired
     *
     * @param session HTTP session
     * @return true if session is expired
     */
    private boolean isSessionExpired(HttpSession session) {
        LocalDateTime lastActivity = (LocalDateTime) session.getAttribute(ADMIN_LAST_ACTIVITY_KEY);
        LocalDateTime loginTime = (LocalDateTime) session.getAttribute(ADMIN_LOGIN_TIME_KEY);

        if (lastActivity == null || loginTime == null) {
            return true;
        }

        LocalDateTime now = LocalDateTime.now();

        // Determine timeout based on session settings
        int maxInactiveInterval = session.getMaxInactiveInterval();
        int timeoutMinutes = maxInactiveInterval > 0 ? maxInactiveInterval / 60 : SESSION_TIMEOUT_MINUTES;

        // Check if session has been inactive too long
        if (lastActivity.plusMinutes(timeoutMinutes).isBefore(now)) {
            return true;
        }

        // Additional check: maximum session duration (for security)
        int maxSessionHours = (timeoutMinutes > SESSION_TIMEOUT_MINUTES) ? 8 : 2;
        if (loginTime.plusHours(maxSessionHours).isBefore(now)) {
            logger.info("Admin session exceeded maximum duration");
            return true;
        }

        return false;
    }

    /**
     * Update the last activity time in the session
     *
     * @param request HTTP request
     */
    private void updateLastActivity(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session != null) {
            session.setAttribute(ADMIN_LAST_ACTIVITY_KEY, LocalDateTime.now());
        }
    }

    /**
     * Handle unauthorized access attempts
     *
     * @param request HTTP request
     * @param response HTTP response
     * @param requestedPath The path that was requested
     * @throws IOException if redirect fails
     */
    private void handleUnauthorizedAccess(HttpServletRequest request, HttpServletResponse response,
                                        String requestedPath) throws IOException {

        // Log unauthorized access attempt
        String clientIP = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");

        logger.warning(String.format("Unauthorized admin access attempt: IP=%s, Path=%s, UserAgent=%s",
                                   clientIP, requestedPath, userAgent));

        // Store the requested URL for redirect after login
        if (!requestedPath.contains("/login") && !isAjaxRequest(request)) {
            String fullRequestedUrl = request.getRequestURL().toString();
            String queryString = request.getQueryString();

            if (queryString != null) {
                fullRequestedUrl += "?" + queryString;
            }

            HttpSession session = request.getSession(true);
            session.setAttribute("requestedAdminUrl", fullRequestedUrl);
        }

        // Handle different types of requests
        if (isAjaxRequest(request)) {
            handleAjaxUnauthorizedAccess(response);
        } else {
            redirectToLogin(request, response, "로그인이 필요합니다.");
        }
    }

    /**
     * Handle unauthorized AJAX requests
     *
     * @param response HTTP response
     * @throws IOException if writing response fails
     */
    private void handleAjaxUnauthorizedAccess(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = "{\"error\": \"unauthorized\", \"message\": \"로그인이 필요합니다.\", \"redirectUrl\": \"/admin/login\"}";
        response.getWriter().write(jsonResponse);
    }

    /**
     * Redirect to admin login page
     *
     * @param request HTTP request
     * @param response HTTP response
     * @param message Message to display
     * @throws IOException if redirect fails
     */
    private void redirectToLogin(HttpServletRequest request, HttpServletResponse response, String message)
            throws IOException {

        String contextPath = request.getContextPath();
        String redirectUrl = contextPath + "/admin/login";

        if (message != null && !message.isEmpty()) {
            if (message.contains("만료")) {
                redirectUrl += "?message=session_expired";
            } else if (message.contains("오류")) {
                redirectUrl += "?message=system_error";
            } else {
                redirectUrl += "?message=login_required";
            }
        }

        response.sendRedirect(redirectUrl);
    }

    /**
     * Check if the request is an AJAX request
     *
     * @param request HTTP request
     * @return true if AJAX request
     */
    private boolean isAjaxRequest(HttpServletRequest request) {
        String requestedWith = request.getHeader("X-Requested-With");
        String contentType = request.getHeader("Content-Type");
        String accept = request.getHeader("Accept");

        return "XMLHttpRequest".equals(requestedWith) ||
               (contentType != null && contentType.contains("application/json")) ||
               (accept != null && accept.contains("application/json"));
    }

    /**
     * Get the client's IP address
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

        String xForwarded = request.getHeader("X-Forwarded");
        if (xForwarded != null && !xForwarded.isEmpty() && !"unknown".equalsIgnoreCase(xForwarded)) {
            return xForwarded;
        }

        String forwarded = request.getHeader("Forwarded");
        if (forwarded != null && !forwarded.isEmpty() && !"unknown".equalsIgnoreCase(forwarded)) {
            return forwarded;
        }

        return request.getRemoteAddr();
    }

    /**
     * Check if the user agent appears to be a bot or crawler
     *
     * @param userAgent User agent string
     * @return true if appears to be a bot
     */
    private boolean isBot(String userAgent) {
        if (userAgent == null) {
            return false;
        }

        String lowerUserAgent = userAgent.toLowerCase();
        String[] botKeywords = {"bot", "crawler", "spider", "scraper", "curl", "wget"};

        for (String keyword : botKeywords) {
            if (lowerUserAgent.contains(keyword)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void destroy() {
        logger.info("AdminAuthFilter destroyed");
    }
}