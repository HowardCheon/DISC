package com.disc.controller;

import com.disc.dao.TestLinkDAO;
import com.disc.dao.ResultDAO;
import com.disc.dao.UserDAO;
import com.disc.model.TestLink;
import com.disc.model.Result;
import com.disc.model.User;
import com.disc.util.DBUtil;
import com.disc.util.ResultCalculator;
import com.disc.util.DiscTypeDescriptions;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Servlet for displaying DISC test results
 */
@WebServlet("/result")
public class ResultServlet extends HttpServlet {
    
    private static final Logger logger = Logger.getLogger(ResultServlet.class.getName());
    
    private TestLinkDAO testLinkDAO;
    private ResultDAO resultDAO;
    private UserDAO userDAO;
    private ResultCalculator resultCalculator;
    
    @Override
    public void init() throws ServletException {
        super.init();
        
        try {
            // Initialize database if not already done
            if (!DBUtil.isInitialized()) {
                DBUtil.initialize(getServletContext());
            }
            
            // Initialize DAOs and utilities
            testLinkDAO = new TestLinkDAO();
            resultDAO = new ResultDAO();
            userDAO = new UserDAO();
            resultCalculator = new ResultCalculator();
            
            logger.info("ResultServlet initialized successfully");
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to initialize ResultServlet", e);
            throw new ServletException("Database initialization failed", e);
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String token = request.getParameter("token");
        
        // Validate token parameter
        if (token == null || token.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/jsp/login.jsp?error=missing_token");
            return;
        }
        
        try {
            // Find test link by token
            TestLink testLink = testLinkDAO.getTestLinkByUrl(token.trim());
            if (testLink == null) {
                response.sendRedirect(request.getContextPath() + "/jsp/login.jsp?error=invalid_token");
                return;
            }
            
            // Check if test is completed
            if (testLink.getStatus() != TestLink.Status.검사완료) {
                // Redirect back to test page if not completed
                User user = userDAO.getUserById(testLink.getUserId());
                if (user != null) {
                    String redirectUrl = String.format("%s/test?name=%s&token=%s", 
                        request.getContextPath(),
                        java.net.URLEncoder.encode(user.getName(), "UTF-8"),
                        java.net.URLEncoder.encode(token, "UTF-8"));
                    response.sendRedirect(redirectUrl);
                } else {
                    response.sendRedirect(request.getContextPath() + "/jsp/login.jsp?error=user_not_found");
                }
                return;
            }
            
            // Get result from database
            Result result = resultDAO.getResultByTestLinkId(testLink.getId());
            if (result == null) {
                // Result not found, try to calculate it
                logger.warning("Result not found for testLinkId: " + testLink.getId() + ", attempting to calculate");
                
                try {
                    Map<String, Integer> scores = resultCalculator.calculateScores(testLink.getId());
                    result = new Result(
                        testLink.getId(),
                        scores.get("D"),
                        scores.get("I"),
                        scores.get("S"),
                        scores.get("C")
                    );
                    result = resultDAO.saveOrUpdateResult(result);
                    
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, "Failed to calculate result for testLinkId: " + testLink.getId(), e);
                    response.sendRedirect(request.getContextPath() + "/jsp/error.jsp?message=result_calculation_failed");
                    return;
                }
            }
            
            // Get user information
            User user = userDAO.getUserById(testLink.getUserId());
            if (user == null) {
                response.sendRedirect(request.getContextPath() + "/jsp/login.jsp?error=user_not_found");
                return;
            }
            
            // Calculate additional result data
            Map<String, Object> detailedResult = resultCalculator.calculateDetailedResult(testLink.getId());
            Map<String, Double> percentages = resultCalculator.calculatePercentages(result.getScoresAsMap());
            
            // Get DISC type descriptions
            String primaryType = result.getResultType();
            Map<String, Object> typeDescription = DiscTypeDescriptions.getTypeDescription(primaryType);
            Map<String, Map<String, Object>> allTypeDescriptions = DiscTypeDescriptions.getAllTypeDescriptions();
            List<String> careerRecommendations = DiscTypeDescriptions.getCareerRecommendations(primaryType);
            Map<String, Object> compatibility = DiscTypeDescriptions.getTypeCompatibility(primaryType);
            
            // Prepare chart data for JSON serialization
            Map<String, Object> chartData = new HashMap<>();
            chartData.put("scores", result.getScoresAsMap());
            chartData.put("percentages", percentages);
            chartData.put("labels", Arrays.asList("D (주도형)", "I (사교형)", "S (안정형)", "C (신중형)"));
            chartData.put("colors", Arrays.asList("#e74c3c", "#f39c12", "#27ae60", "#3498db"));
            
            // Convert chart data to JSON string for JSP
            Gson gson = new Gson();
            String chartDataJson = gson.toJson(chartData);
            
            // Prepare result data for JSP
            request.setAttribute("result", result);
            request.setAttribute("testLink", testLink);
            request.setAttribute("user", user);
            request.setAttribute("percentages", percentages);
            request.setAttribute("detailedResult", detailedResult);
            request.setAttribute("primaryType", primaryType);
            request.setAttribute("typeDescription", typeDescription);
            request.setAttribute("allTypeDescriptions", allTypeDescriptions);
            request.setAttribute("careerRecommendations", careerRecommendations);
            request.setAttribute("compatibility", compatibility);
            request.setAttribute("chartDataJson", chartDataJson);
            
            // Set user name for display
            request.setAttribute("userName", user.getName());
            
            // Store in session for potential future use
            HttpSession session = request.getSession();
            session.setAttribute("testResult", result);
            session.setAttribute("userName", user.getName());
            
            // Forward to result page
            request.getRequestDispatcher("/jsp/result.jsp").forward(request, response);
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error in ResultServlet", e);
            response.sendRedirect(request.getContextPath() + "/jsp/error.jsp?message=database_error");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error in ResultServlet", e);
            response.sendRedirect(request.getContextPath() + "/jsp/error.jsp?message=system_error");
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Handle POST requests (usually from test submission)
        String action = request.getParameter("action");
        
        if ("submitTest".equals(action)) {
            // Redirect to SubmitServlet for processing
            request.getRequestDispatcher("/submit").forward(request, response);
        } else {
            // For other POST requests, redirect to GET
            doGet(request, response);
        }
    }
    
    @Override
    public void destroy() {
        super.destroy();
        logger.info("ResultServlet destroyed");
    }
}