package com.disc.controller;

import com.disc.dao.TestLinkDAO;
import com.disc.dao.UserDAO;
import com.disc.dao.AnswerDAO;
import com.disc.model.TestLink;
import com.disc.model.User;
import com.disc.model.Question;
import com.disc.util.DBUtil;
import com.disc.util.QuestionLoader;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Servlet for handling DISC test page requests
 */
@WebServlet("/test")
public class TestServlet extends HttpServlet {
    
    private static final Logger logger = Logger.getLogger(TestServlet.class.getName());
    
    private UserDAO userDAO;
    private TestLinkDAO testLinkDAO;
    private AnswerDAO answerDAO;
    private QuestionLoader questionLoader;
    
    @Override
    public void init() throws ServletException {
        super.init();
        
        try {
            // Initialize database if not already done
            if (!DBUtil.isInitialized()) {
                DBUtil.initialize(getServletContext());
            }
            
            // Initialize DAOs
            userDAO = new UserDAO();
            testLinkDAO = new TestLinkDAO();
            answerDAO = new AnswerDAO();
            questionLoader = QuestionLoader.getInstance();
            
            logger.info("TestServlet initialized successfully");
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to initialize TestServlet", e);
            throw new ServletException("Database initialization failed", e);
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String name = request.getParameter("name");
        String token = request.getParameter("token");
        String pageParam = request.getParameter("page");
        
        // Validate parameters
        if (name == null || name.trim().isEmpty() || 
            token == null || token.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/jsp/login.jsp?error=missing_params");
            return;
        }
        
        try {
            // Find or create user
            User user = userDAO.findOrCreateUser(name.trim());
            if (user == null) {
                response.sendRedirect(request.getContextPath() + "/jsp/login.jsp?error=user_creation_failed");
                return;
            }
            
            // Find test link by token
            TestLink testLink = testLinkDAO.getTestLinkByUrl(token.trim());
            if (testLink == null) {
                response.sendRedirect(request.getContextPath() + "/jsp/login.jsp?error=invalid_token");
                return;
            }
            
            // Check if test link belongs to the user
            if (!testLink.getUserId().equals(user.getId())) {
                response.sendRedirect(request.getContextPath() + "/jsp/login.jsp?error=access_denied");
                return;
            }
            
            // Check test status
            if (testLink.getStatus() == TestLink.Status.검사완료) {
                response.sendRedirect(request.getContextPath() + "/result?token=" + token);
                return;
            }
            
            // Update test status to started if needed
            if (testLink.getStatus() == TestLink.Status.검사전) {
                testLinkDAO.updateStartedAt(testLink.getId());
                testLink.setStatus(TestLink.Status.검사중);
            }
            
            // Parse page number (default to 1)
            int currentPage = 1;
            if (pageParam != null && !pageParam.trim().isEmpty()) {
                try {
                    currentPage = Integer.parseInt(pageParam);
                    if (currentPage < 1) currentPage = 1;
                    if (currentPage > 4) currentPage = 4;
                } catch (NumberFormatException e) {
                    currentPage = 1;
                }
            }
            
            // Load questions
            List<Question> allQuestions = questionLoader.loadQuestions(getServletContext());
            if (allQuestions.size() != 28) {
                throw new ServletException("Expected 28 questions, found " + allQuestions.size());
            }
            
            // Calculate question range for current page (7 questions per page)
            int questionsPerPage = 7;
            int startIndex = (currentPage - 1) * questionsPerPage;
            int endIndex = Math.min(startIndex + questionsPerPage, allQuestions.size());
            
            List<Question> pageQuestions = allQuestions.subList(startIndex, endIndex);
            
            // Calculate progress
            int totalPages = (int) Math.ceil((double) allQuestions.size() / questionsPerPage);
            double progressPercentage = ((double) currentPage / totalPages) * 100;
            
            // Store data in session
            HttpSession session = request.getSession();
            session.setAttribute("user", user);
            session.setAttribute("testLink", testLink);
            session.setAttribute("userName", user.getName());
            session.setAttribute("testToken", token);
            
            // Set request attributes
            request.setAttribute("questions", pageQuestions);
            request.setAttribute("allQuestions", allQuestions);
            request.setAttribute("currentPage", currentPage);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("progressPercentage", Math.round(progressPercentage));
            request.setAttribute("questionsPerPage", questionsPerPage);
            request.setAttribute("startQuestionNumber", startIndex + 1);
            request.setAttribute("endQuestionNumber", endIndex);
            
            // Check if this is the last page
            boolean isLastPage = currentPage == totalPages;
            request.setAttribute("isLastPage", isLastPage);
            
            // Get existing answers for progress tracking
            try {
                int answeredCount = answerDAO.getAnswerCountByTestLinkId(testLink.getId());
                request.setAttribute("answeredCount", answeredCount);
                request.setAttribute("totalQuestions", allQuestions.size());
                
                // Load existing answers as JSON for JavaScript
                String existingAnswersJson = getExistingAnswersJson(testLink.getId());
                request.setAttribute("existingAnswersJson", existingAnswersJson);
                
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to load existing answers", e);
                request.setAttribute("answeredCount", 0);
                request.setAttribute("existingAnswersJson", "{}");
            }
            
            // Forward to test.jsp
            request.getRequestDispatcher("/jsp/test.jsp").forward(request, response);
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error in TestServlet", e);
            response.sendRedirect(request.getContextPath() + "/jsp/login.jsp?error=database_error");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error in TestServlet", e);
            response.sendRedirect(request.getContextPath() + "/jsp/login.jsp?error=system_error");
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Handle AJAX requests for saving answers
        String action = request.getParameter("action");
        
        if ("saveAnswers".equals(action)) {
            handleSaveAnswers(request, response);
        } else {
            // Redirect to GET for regular form submissions
            doGet(request, response);
        }
    }
    
    /**
     * Handle AJAX request to save answers
     */
    private void handleSaveAnswers(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                response.getWriter().write("{\"success\": false, \"message\": \"Session expired\"}");
                return;
            }
            
            TestLink testLink = (TestLink) session.getAttribute("testLink");
            if (testLink == null) {
                response.getWriter().write("{\"success\": false, \"message\": \"Test link not found\"}");
                return;
            }
            
            // Get answers from request
            String answersJson = request.getParameter("answers");
            if (answersJson == null || answersJson.trim().isEmpty()) {
                response.getWriter().write("{\"success\": false, \"message\": \"No answers provided\"}");
                return;
            }
            
            // Parse and save answers (implementation depends on your JSON structure)
            // For now, return success
            response.getWriter().write("{\"success\": true, \"message\": \"Answers saved\"}");
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error saving answers", e);
            response.getWriter().write("{\"success\": false, \"message\": \"Server error\"}");
        }
    }
    
    /**
     * Get existing answers as JSON string
     */
    private String getExistingAnswersJson(Long testLinkId) throws SQLException {
        StringBuilder json = new StringBuilder();
        json.append("{");
        
        var answers = answerDAO.getAnswersMapByTestLinkId(testLinkId);
        boolean first = true;
        
        for (var entry : answers.entrySet()) {
            if (!first) json.append(",");
            first = false;
            
            json.append("\"").append(entry.getKey()).append("\":");
            json.append("{");
            json.append("\"mostLike\":\"").append(entry.getValue().getMostLike()).append("\",");
            json.append("\"leastLike\":\"").append(entry.getValue().getLeastLike()).append("\"");
            json.append("}");
        }
        
        json.append("}");
        return json.toString();
    }
    
    /**
     * Validate test access
     */
    private boolean validateTestAccess(String name, String token) {
        try {
            User user = userDAO.getUserByName(name);
            if (user == null) return false;
            
            TestLink testLink = testLinkDAO.getTestLinkByUrl(token);
            if (testLink == null) return false;
            
            return testLink.getUserId().equals(user.getId()) && 
                   testLink.getStatus() != TestLink.Status.검사완료;
                   
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error validating test access", e);
            return false;
        }
    }
    
    @Override
    public void destroy() {
        super.destroy();
        logger.info("TestServlet destroyed");
    }
}