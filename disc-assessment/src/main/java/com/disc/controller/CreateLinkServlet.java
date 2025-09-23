package com.disc.controller;

import com.disc.dao.TestLinkDAO;
import com.disc.dao.UserDAO;
import com.disc.model.TestLink;
import com.disc.model.User;
import com.disc.model.Admin;
import com.disc.util.DBUtil;
import com.disc.util.SecurityUtil;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Servlet for creating and managing test links
 */
@WebServlet("/admin/create-link")
public class CreateLinkServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(CreateLinkServlet.class.getName());

    private TestLinkDAO testLinkDAO;
    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        super.init();

        try {
            // Initialize database if not already done
            if (!DBUtil.isInitialized()) {
                DBUtil.initialize(getServletContext());
            }

            // Initialize DAOs
            testLinkDAO = new TestLinkDAO();
            userDAO = new UserDAO();

            logger.info("CreateLinkServlet initialized successfully");

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to initialize CreateLinkServlet", e);
            throw new ServletException("Database initialization failed", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Check admin session
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("adminUser") == null) {
            response.sendRedirect(request.getContextPath() + "/admin/login");
            return;
        }

        String action = request.getParameter("action");

        try {
            if ("getUserSuggestions".equals(action)) {
                handleUserSuggestions(request, response);
            } else if ("getHistory".equals(action)) {
                handleGetHistory(request, response);
            } else if ("checkDuplicate".equals(action)) {
                handleCheckDuplicate(request, response);
            } else {
                // Show create link page with history
                showCreateLinkPage(request, response);
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error in CreateLinkServlet", e);
            request.setAttribute("errorMessage", "데이터베이스 오류가 발생했습니다.");
            request.getRequestDispatcher("/jsp/admin/create-link.jsp").forward(request, response);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error in CreateLinkServlet", e);
            request.setAttribute("errorMessage", "시스템 오류가 발생했습니다.");
            request.getRequestDispatcher("/jsp/admin/create-link.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Check admin session
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("adminUser") == null) {
            response.sendRedirect(request.getContextPath() + "/admin/login");
            return;
        }

        String action = request.getParameter("action");

        try {
            if ("createSingle".equals(action)) {
                handleCreateSingleLink(request, response);
            } else if ("createBulk".equals(action)) {
                handleCreateBulkLinks(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error in CreateLinkServlet POST", e);
            sendJsonResponse(response, false, "데이터베이스 오류가 발생했습니다.", null);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error in CreateLinkServlet POST", e);
            sendJsonResponse(response, false, "시스템 오류가 발생했습니다.", null);
        }
    }

    /**
     * Show create link page with history
     */
    private void showCreateLinkPage(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        // Get recent test links history
        List<Map<String, Object>> recentHistory = getRecentTestLinksHistory(20);
        request.setAttribute("recentHistory", recentHistory);

        // Get admin user
        Admin admin = (Admin) request.getSession().getAttribute("adminUser");
        request.setAttribute("adminUser", admin);

        // Convert history to JSON for JavaScript
        Gson gson = new Gson();
        request.setAttribute("recentHistoryJson", gson.toJson(recentHistory));

        request.getRequestDispatcher("/jsp/admin/create-link.jsp").forward(request, response);
    }

    /**
     * Handle user name suggestions for autocomplete
     */
    private void handleUserSuggestions(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        String query = request.getParameter("query");

        // 입력값 유효성 검사
        if (query == null || query.trim().length() < 2) {
            sendJsonResponse(response, true, null, new ArrayList<>());
            return;
        }

        // XSS 및 SQL Injection 방지
        query = SecurityUtil.sanitizeInput(query);
        if (SecurityUtil.containsSQLInjection(query)) {
            logger.warning("SQL Injection attempt detected in user suggestions: " + query);
            sendJsonResponse(response, false, "잘못된 입력입니다.", null);
            return;
        }

        List<String> suggestions = getUserNameSuggestions(query.trim());
        sendJsonResponse(response, true, null, suggestions);
    }

    /**
     * Handle getting test link history
     */
    private void handleGetHistory(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        String userName = request.getParameter("userName");
        int page = parseIntParameter(request.getParameter("page"), 0);
        int limit = parseIntParameter(request.getParameter("limit"), 10);

        List<Map<String, Object>> history;
        if (userName != null && !userName.trim().isEmpty()) {
            history = getTestLinksHistoryByUser(userName.trim(), page, limit);
        } else {
            history = getRecentTestLinksHistory(limit);
        }

        sendJsonResponse(response, true, null, history);
    }

    /**
     * Handle checking for duplicate user
     */
    private void handleCheckDuplicate(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        String userName = request.getParameter("userName");
        if (userName == null || userName.trim().isEmpty()) {
            sendJsonResponse(response, false, "사용자명이 필요합니다.", null);
            return;
        }

        Map<String, Object> duplicateInfo = checkUserDuplicate(userName.trim());
        sendJsonResponse(response, true, null, duplicateInfo);
    }

    /**
     * Handle creating a single test link
     */
    private void handleCreateSingleLink(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        String userName = request.getParameter("userName");
        String confirmDuplicate = request.getParameter("confirmDuplicate");

        // 입력값 유효성 검사
        if (userName == null || userName.trim().isEmpty()) {
            sendJsonResponse(response, false, "사용자명을 입력해주세요.", null);
            return;
        }

        userName = SecurityUtil.sanitizeInput(userName.trim());

        // 사용자명 유효성 검사
        if (!SecurityUtil.isValidUserName(userName)) {
            sendJsonResponse(response, false, "올바른 사용자명을 입력해주세요. (한글, 영문, 숫자만 허용)", null);
            return;
        }

        // SQL Injection 방지
        if (SecurityUtil.containsSQLInjection(userName)) {
            logger.warning("SQL Injection attempt detected in createSingleLink: " + userName);
            sendJsonResponse(response, false, "잘못된 입력입니다.", null);
            return;
        }

        // Check for duplicate unless confirmed
        if (!"true".equals(confirmDuplicate)) {
            Map<String, Object> duplicateInfo = checkUserDuplicate(userName);
            if ((Boolean) duplicateInfo.get("isDuplicate")) {
                Map<String, Object> result = new HashMap<>();
                result.put("requiresConfirmation", true);
                result.put("duplicateInfo", duplicateInfo);
                sendJsonResponse(response, false, "이미 존재하는 사용자입니다. 재발송하시겠습니까?", result);
                return;
            }
        }

        // Create the test link
        Map<String, Object> result = createTestLinkForUser(userName, request);
        if ((Boolean) result.get("success")) {
            sendJsonResponse(response, true, "테스트 링크가 성공적으로 생성되었습니다.", result);
        } else {
            sendJsonResponse(response, false, (String) result.get("message"), null);
        }
    }

    /**
     * Handle creating bulk test links
     */
    private void handleCreateBulkLinks(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        String userNamesText = request.getParameter("userNames");
        if (userNamesText == null || userNamesText.trim().isEmpty()) {
            sendJsonResponse(response, false, "사용자명 목록을 입력해주세요.", null);
            return;
        }

        // Parse user names (one per line)
        List<String> userNames = Arrays.stream(userNamesText.split("\\r?\\n"))
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        if (userNames.isEmpty()) {
            sendJsonResponse(response, false, "유효한 사용자명이 없습니다.", null);
            return;
        }

        if (userNames.size() > 100) {
            sendJsonResponse(response, false, "한 번에 최대 100명까지만 등록할 수 있습니다.", null);
            return;
        }

        // Create test links for all users
        Map<String, Object> result = createBulkTestLinks(userNames, request);
        sendJsonResponse(response, true, "일괄 생성이 완료되었습니다.", result);
    }

    /**
     * Get user name suggestions for autocomplete
     */
    private List<String> getUserNameSuggestions(String query) throws SQLException {
        return userDAO.getUserNameSuggestions(query, 10);
    }

    /**
     * Get recent test links history
     */
    private List<Map<String, Object>> getRecentTestLinksHistory(int limit) throws SQLException {
        return testLinkDAO.getRecentTestLinksWithUserInfo(limit);
    }

    /**
     * Get test links history by user name
     */
    private List<Map<String, Object>> getTestLinksHistoryByUser(String userName, int page, int limit) throws SQLException {
        return testLinkDAO.getTestLinksByUserName(userName, page * limit, limit);
    }

    /**
     * Check if user already has test links
     */
    private Map<String, Object> checkUserDuplicate(String userName) throws SQLException {
        Map<String, Object> result = new HashMap<>();

        User existingUser = userDAO.getUserByName(userName);
        if (existingUser != null) {
            List<TestLink> userLinks = testLinkDAO.getTestLinksByUserId(existingUser.getId());

            result.put("isDuplicate", true);
            result.put("existingUser", existingUser);
            result.put("linkCount", userLinks.size());
            result.put("lastLink", userLinks.isEmpty() ? null : userLinks.get(0));

            // Count completed tests
            long completedCount = userLinks.stream()
                    .filter(link -> link.getStatus() == TestLink.Status.검사완료)
                    .count();
            result.put("completedCount", completedCount);
        } else {
            result.put("isDuplicate", false);
        }

        return result;
    }

    /**
     * Create test link for a single user
     */
    private Map<String, Object> createTestLinkForUser(String userName, HttpServletRequest request) throws SQLException {
        Map<String, Object> result = new HashMap<>();

        try {
            // Get or create user
            User user = userDAO.getOrCreateUser(userName);

            // Generate unique URL
            String uniqueUrl = generateUniqueUrl();

            // Create test link
            TestLink testLink = new TestLink(
                user.getId(),
                uniqueUrl,
                TestLink.Status.대기중,
                null,
                null,
                null
            );

            TestLink savedLink = testLinkDAO.createTestLink(testLink);

            // Get test count for this user
            int testCount = testLinkDAO.getTestLinkCountByUserId(user.getId());

            result.put("success", true);
            result.put("testLink", savedLink);
            result.put("user", user);
            result.put("testCount", testCount);
            result.put("url", request.getScheme() + "://" + request.getServerName() +
                             (request.getServerPort() != 80 && request.getServerPort() != 443 ?
                              ":" + request.getServerPort() : "") +
                             request.getContextPath() + "/test?token=" + uniqueUrl);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating test link for user: " + userName, e);
            result.put("success", false);
            result.put("message", "테스트 링크 생성 중 오류가 발생했습니다.");
        }

        return result;
    }

    /**
     * Create test links for multiple users
     */
    private Map<String, Object> createBulkTestLinks(List<String> userNames, HttpServletRequest request) throws SQLException {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> successList = new ArrayList<>();
        List<Map<String, Object>> failureList = new ArrayList<>();

        for (String userName : userNames) {
            try {
                Map<String, Object> linkResult = createTestLinkForUser(userName, request);
                if ((Boolean) linkResult.get("success")) {
                    successList.add(linkResult);
                } else {
                    Map<String, Object> failure = new HashMap<>();
                    failure.put("userName", userName);
                    failure.put("error", linkResult.get("message"));
                    failureList.add(failure);
                }
            } catch (Exception e) {
                Map<String, Object> failure = new HashMap<>();
                failure.put("userName", userName);
                failure.put("error", "생성 중 오류 발생: " + e.getMessage());
                failureList.add(failure);
                logger.log(Level.WARNING, "Error creating link for user: " + userName, e);
            }
        }

        result.put("successCount", successList.size());
        result.put("failureCount", failureList.size());
        result.put("successList", successList);
        result.put("failureList", failureList);
        result.put("totalRequested", userNames.size());

        return result;
    }

    /**
     * Generate unique URL token
     */
    private String generateUniqueUrl() throws SQLException {
        String token;
        int attempts = 0;
        do {
            token = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            attempts++;
            if (attempts > 10) {
                throw new SQLException("Failed to generate unique URL after 10 attempts");
            }
        } while (testLinkDAO.getTestLinkByUrl(token) != null);

        return token;
    }

    /**
     * Parse integer parameter with default value
     */
    private int parseIntParameter(String param, int defaultValue) {
        if (param == null || param.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(param.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Send JSON response
     */
    private void sendJsonResponse(HttpServletResponse response, boolean success, String message, Object data)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        if (message != null) {
            result.put("message", message);
        }
        if (data != null) {
            result.put("data", data);
        }

        Gson gson = new Gson();
        response.getWriter().write(gson.toJson(result));
    }

    @Override
    public void destroy() {
        super.destroy();
        logger.info("CreateLinkServlet destroyed");
    }
}