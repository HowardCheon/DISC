package com.disc.controller;

import com.disc.dao.TestLinkDAO;
import com.disc.dao.ResultDAO;
import com.disc.dao.UserDAO;
import com.disc.dao.AnswerDAO;
import com.disc.model.Admin;
import com.disc.util.DBUtil;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Servlet for admin dashboard with statistics and management features
 */
@WebServlet("/admin/dashboard")
public class DashboardServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(DashboardServlet.class.getName());

    private TestLinkDAO testLinkDAO;
    private ResultDAO resultDAO;
    private UserDAO userDAO;
    private AnswerDAO answerDAO;

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
            resultDAO = new ResultDAO();
            userDAO = new UserDAO();
            answerDAO = new AnswerDAO();

            logger.info("DashboardServlet initialized successfully");

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to initialize DashboardServlet", e);
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

        Admin admin = (Admin) session.getAttribute("adminUser");

        try {
            // Get dashboard statistics
            Map<String, Object> dashboardStats = getDashboardStatistics();

            // Get recent activity
            List<Map<String, Object>> recentActivity = getRecentActivity(10);

            // Get completion rate by date (last 7 days)
            List<Map<String, Object>> completionTrend = getCompletionTrend(7);

            // Get DISC type distribution
            Map<String, Integer> discTypeDistribution = getDiscTypeDistribution();

            // Set attributes for JSP
            request.setAttribute("dashboardStats", dashboardStats);
            request.setAttribute("recentActivity", recentActivity);
            request.setAttribute("completionTrend", completionTrend);
            request.setAttribute("discTypeDistribution", discTypeDistribution);
            request.setAttribute("adminUser", admin);

            // Convert data to JSON for charts
            Gson gson = new Gson();
            request.setAttribute("completionTrendJson", gson.toJson(completionTrend));
            request.setAttribute("discTypeDistributionJson", gson.toJson(discTypeDistribution));

            // Forward to dashboard JSP
            request.getRequestDispatcher("/jsp/admin/dashboard.jsp").forward(request, response);

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error in DashboardServlet", e);
            request.setAttribute("errorMessage", "통계 데이터를 불러오는 중 오류가 발생했습니다.");
            request.getRequestDispatcher("/jsp/admin/dashboard.jsp").forward(request, response);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error in DashboardServlet", e);
            request.setAttribute("errorMessage", "시스템 오류가 발생했습니다.");
            request.getRequestDispatcher("/jsp/admin/dashboard.jsp").forward(request, response);
        }
    }

    /**
     * Get comprehensive dashboard statistics
     */
    private Map<String, Object> getDashboardStatistics() throws SQLException {
        Map<String, Object> stats = new HashMap<>();

        // Today's date for filtering
        LocalDate today = LocalDate.now();
        String todayStr = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // Get today's statistics
        Map<String, Object> todayStats = getTodayStatistics(todayStr);
        stats.put("today", todayStats);

        // Get overall statistics
        stats.put("totalUsers", userDAO.getUserCount());
        stats.put("totalTestLinks", testLinkDAO.getTotalTestLinkCount());
        stats.put("totalCompletedTests", testLinkDAO.getCompletedTestCount());
        stats.put("totalResults", resultDAO.getTotalResultCount());

        // Calculate overall completion rate
        long totalLinks = testLinkDAO.getTotalTestLinkCount();
        long completedLinks = testLinkDAO.getCompletedTestCount();
        double overallCompletionRate = totalLinks > 0 ? (double) completedLinks / totalLinks * 100 : 0;
        stats.put("overallCompletionRate", Math.round(overallCompletionRate * 100.0) / 100.0);

        // Get this week's statistics
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
        Map<String, Object> weekStats = getDateRangeStatistics(weekStart, today);
        stats.put("thisWeek", weekStats);

        // Get this month's statistics
        LocalDate monthStart = today.withDayOfMonth(1);
        Map<String, Object> monthStats = getDateRangeStatistics(monthStart, today);
        stats.put("thisMonth", monthStats);

        return stats;
    }

    /**
     * Get today's specific statistics
     */
    private Map<String, Object> getTodayStatistics(String todayStr) throws SQLException {
        Map<String, Object> todayStats = new HashMap<>();

        // Get today's created test links
        long todayLinks = testLinkDAO.getTestLinkCountByDate(todayStr);
        todayStats.put("linksCreated", todayLinks);

        // Get today's completed tests
        long todayCompleted = testLinkDAO.getCompletedTestCountByDate(todayStr);
        todayStats.put("testsCompleted", todayCompleted);

        // Calculate today's completion rate
        double todayCompletionRate = todayLinks > 0 ? (double) todayCompleted / todayLinks * 100 : 0;
        todayStats.put("completionRate", Math.round(todayCompletionRate * 100.0) / 100.0);

        // Get today's new users
        long todayUsers = userDAO.getUserCountByDate(todayStr);
        todayStats.put("newUsers", todayUsers);

        // Get today's started but not completed tests
        long todayStarted = testLinkDAO.getStartedTestCountByDate(todayStr);
        long todayInProgress = todayStarted - todayCompleted;
        todayStats.put("testsInProgress", todayInProgress);

        return todayStats;
    }

    /**
     * Get statistics for a date range
     */
    private Map<String, Object> getDateRangeStatistics(LocalDate startDate, LocalDate endDate) throws SQLException {
        Map<String, Object> stats = new HashMap<>();

        String startDateStr = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String endDateStr = endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // Get links created in range
        long linksCreated = testLinkDAO.getTestLinkCountInDateRange(startDateStr, endDateStr);
        stats.put("linksCreated", linksCreated);

        // Get tests completed in range
        long testsCompleted = testLinkDAO.getCompletedTestCountInDateRange(startDateStr, endDateStr);
        stats.put("testsCompleted", testsCompleted);

        // Calculate completion rate for range
        double completionRate = linksCreated > 0 ? (double) testsCompleted / linksCreated * 100 : 0;
        stats.put("completionRate", Math.round(completionRate * 100.0) / 100.0);

        // Get new users in range
        long newUsers = userDAO.getUserCountInDateRange(startDateStr, endDateStr);
        stats.put("newUsers", newUsers);

        return stats;
    }

    /**
     * Get recent activity feed
     */
    private List<Map<String, Object>> getRecentActivity(int limit) throws SQLException {
        List<Map<String, Object>> activities = new ArrayList<>();

        // Get recent test completions
        List<Map<String, Object>> recentCompletions = testLinkDAO.getRecentCompletions(limit / 2);
        for (Map<String, Object> completion : recentCompletions) {
            Map<String, Object> activity = new HashMap<>();
            activity.put("type", "completion");
            activity.put("message", completion.get("userName") + "님이 검사를 완료했습니다");
            activity.put("time", completion.get("completedAt"));
            activity.put("icon", "check-circle");
            activity.put("color", "success");
            activities.add(activity);
        }

        // Get recent test starts
        List<Map<String, Object>> recentStarts = testLinkDAO.getRecentStarts(limit / 2);
        for (Map<String, Object> start : recentStarts) {
            Map<String, Object> activity = new HashMap<>();
            activity.put("type", "start");
            activity.put("message", start.get("userName") + "님이 검사를 시작했습니다");
            activity.put("time", start.get("startedAt"));
            activity.put("icon", "play-circle");
            activity.put("color", "info");
            activities.add(activity);
        }

        // Sort by time (most recent first)
        activities.sort((a, b) -> {
            String timeA = (String) a.get("time");
            String timeB = (String) b.get("time");
            return timeB.compareTo(timeA);
        });

        // Return limited results
        return activities.subList(0, Math.min(activities.size(), limit));
    }

    /**
     * Get completion rate trend for the last N days
     */
    private List<Map<String, Object>> getCompletionTrend(int days) throws SQLException {
        List<Map<String, Object>> trend = new ArrayList<>();

        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("MM/dd");

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.format(formatter);

            long linksCreated = testLinkDAO.getTestLinkCountByDate(dateStr);
            long testsCompleted = testLinkDAO.getCompletedTestCountByDate(dateStr);

            double completionRate = linksCreated > 0 ? (double) testsCompleted / linksCreated * 100 : 0;

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.format(displayFormatter));
            dayData.put("linksCreated", linksCreated);
            dayData.put("testsCompleted", testsCompleted);
            dayData.put("completionRate", Math.round(completionRate * 100.0) / 100.0);

            trend.add(dayData);
        }

        return trend;
    }

    /**
     * Get DISC type distribution from completed tests
     */
    private Map<String, Integer> getDiscTypeDistribution() throws SQLException {
        return resultDAO.getDiscTypeDistribution();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if ("refreshStats".equals(action)) {
            // Handle AJAX request for refreshing statistics
            handleRefreshStats(request, response);
        } else {
            // For other POST requests, redirect to GET
            doGet(request, response);
        }
    }

    /**
     * Handle AJAX request to refresh dashboard statistics
     */
    private void handleRefreshStats(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            Map<String, Object> stats = getDashboardStatistics();
            Gson gson = new Gson();
            String jsonResponse = gson.toJson(Map.of(
                "success", true,
                "data", stats,
                "timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            ));

            response.getWriter().write(jsonResponse);

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error refreshing dashboard stats", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\": false, \"message\": \"통계 새로고침 실패\"}");
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        logger.info("DashboardServlet destroyed");
    }
}