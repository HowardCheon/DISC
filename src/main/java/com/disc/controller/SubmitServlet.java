package com.disc.controller;

import com.disc.dao.TestLinkDAO;
import com.disc.dao.AnswerDAO;
import com.disc.dao.ResultDAO;
import com.disc.model.TestLink;
import com.disc.model.Answer;
import com.disc.model.Result;
import com.disc.util.DBUtil;
import com.disc.util.ResultCalculator;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Servlet for handling DISC test submission and result calculation
 */
@WebServlet("/submit")
public class SubmitServlet extends HttpServlet {
    
    private static final Logger logger = Logger.getLogger(SubmitServlet.class.getName());
    
    private TestLinkDAO testLinkDAO;
    private AnswerDAO answerDAO;
    private ResultDAO resultDAO;
    private ResultCalculator resultCalculator;
    private Gson gson;
    
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
            answerDAO = new AnswerDAO();
            resultDAO = new ResultDAO();
            resultCalculator = new ResultCalculator();
            gson = new Gson();
            
            logger.info("SubmitServlet initialized successfully");
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to initialize SubmitServlet", e);
            throw new ServletException("Database initialization failed", e);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            // Get session and validate
            HttpSession session = request.getSession(false);
            if (session == null) {
                sendErrorResponse(response, "세션이 만료되었습니다. 다시 로그인해주세요.");
                return;
            }
            
            // Get test link from session
            TestLink testLink = (TestLink) session.getAttribute("testLink");
            if (testLink == null) {
                sendErrorResponse(response, "검사 정보를 찾을 수 없습니다.");
                return;
            }
            
            // Validate test link status
            if (testLink.getStatus() == TestLink.Status.검사완료) {
                sendErrorResponse(response, "이미 완료된 검사입니다.");
                return;
            }
            
            // Get answers from request
            String answersJson = request.getParameter("answers");
            if (answersJson == null || answersJson.trim().isEmpty()) {
                sendErrorResponse(response, "답변 데이터가 없습니다.");
                return;
            }
            
            // Parse answers
            Map<Integer, Map<String, String>> parsedAnswers = parseAnswers(answersJson);
            if (parsedAnswers.isEmpty()) {
                sendErrorResponse(response, "답변 데이터를 파싱할 수 없습니다.");
                return;
            }
            
            // Validate completeness (should have 28 questions)
            if (parsedAnswers.size() != 28) {
                sendErrorResponse(response, 
                    String.format("모든 문항에 답변해주세요. (현재: %d/28)", parsedAnswers.size()));
                return;
            }
            
            // Validate each answer has both mostLike and leastLike
            for (Map.Entry<Integer, Map<String, String>> entry : parsedAnswers.entrySet()) {
                Integer questionNum = entry.getKey();
                Map<String, String> answer = entry.getValue();
                
                if (!answer.containsKey("mostLike") || !answer.containsKey("leastLike")) {
                    sendErrorResponse(response, 
                        String.format("문항 %d에 불완전한 답변이 있습니다.", questionNum));
                    return;
                }
                
                String mostLike = answer.get("mostLike");
                String leastLike = answer.get("leastLike");
                
                if (mostLike == null || leastLike == null || mostLike.equals(leastLike)) {
                    sendErrorResponse(response, 
                        String.format("문항 %d에 잘못된 답변이 있습니다.", questionNum));
                    return;
                }
            }
            
            // Process submission in transaction
            Long testLinkId = testLink.getId();
            Result result = processSubmission(testLinkId, parsedAnswers);
            
            if (result != null) {
                // Update session with result
                session.setAttribute("testResult", result);
                
                sendSuccessResponse(response, "검사가 성공적으로 완료되었습니다.", result.getId());
            } else {
                sendErrorResponse(response, "결과 처리 중 오류가 발생했습니다.");
            }
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in SubmitServlet", e);
            sendErrorResponse(response, "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
    }
    
    /**
     * Process the complete submission workflow
     */
    private Result processSubmission(Long testLinkId, Map<Integer, Map<String, String>> parsedAnswers) 
            throws SQLException {
        
        return DBUtil.executeTransaction(connection -> {
            logger.info("Starting submission process for testLinkId: " + testLinkId);
            
            // Step 1: Delete existing answers (in case of resubmission)
            int deletedCount = answerDAO.deleteAnswersByTestLinkId(testLinkId);
            if (deletedCount > 0) {
                logger.info("Deleted " + deletedCount + " existing answers for testLinkId: " + testLinkId);
            }
            
            // Step 2: Save new answers
            List<Answer> answers = new ArrayList<>();
            for (Map.Entry<Integer, Map<String, String>> entry : parsedAnswers.entrySet()) {
                Integer questionNum = entry.getKey();
                Map<String, String> answerData = entry.getValue();
                
                Answer answer = new Answer(
                    testLinkId,
                    questionNum,
                    answerData.get("mostLike"),
                    answerData.get("leastLike")
                );
                
                answers.add(answer);
            }
            
            // Bulk insert answers
            int savedCount = answerDAO.bulkInsertAnswers(answers);
            logger.info("Saved " + savedCount + " answers for testLinkId: " + testLinkId);
            
            if (savedCount != 28) {
                throw new SQLException("Expected to save 28 answers, but saved " + savedCount);
            }
            
            // Step 3: Calculate DISC scores
            Map<String, Integer> scores = resultCalculator.calculateScores(testLinkId);
            logger.info("Calculated scores for testLinkId " + testLinkId + ": " + scores);
            
            // Step 4: Create and save result
            Result result = new Result(
                testLinkId,
                scores.get("D"),
                scores.get("I"),
                scores.get("S"),
                scores.get("C")
            );
            
            // Save or update result
            Result savedResult = resultDAO.saveOrUpdateResult(result);
            logger.info("Saved result with ID: " + savedResult.getId());
            
            // Step 5: Update test link status to completed
            boolean statusUpdated = testLinkDAO.updateCompletedAt(testLinkId);
            if (!statusUpdated) {
                throw new SQLException("Failed to update test link status to completed");
            }
            
            logger.info("Updated test link status to completed for testLinkId: " + testLinkId);
            
            return savedResult;
        });
    }
    
    /**
     * Parse JSON answers into structured format
     */
    private Map<Integer, Map<String, String>> parseAnswers(String answersJson) {
        Map<Integer, Map<String, String>> result = new HashMap<>();
        
        try {
            JsonObject jsonObject = JsonParser.parseString(answersJson).getAsJsonObject();
            
            for (String questionIdStr : jsonObject.keySet()) {
                try {
                    Integer questionId = Integer.parseInt(questionIdStr);
                    JsonObject answerObj = jsonObject.getAsJsonObject(questionIdStr);
                    
                    Map<String, String> answerMap = new HashMap<>();
                    
                    if (answerObj.has("mostLike") && !answerObj.get("mostLike").isJsonNull()) {
                        answerMap.put("mostLike", answerObj.get("mostLike").getAsString());
                    }
                    
                    if (answerObj.has("leastLike") && !answerObj.get("leastLike").isJsonNull()) {
                        answerMap.put("leastLike", answerObj.get("leastLike").getAsString());
                    }
                    
                    // Only add if both answers are present
                    if (answerMap.size() == 2) {
                        result.put(questionId, answerMap);
                    }
                    
                } catch (NumberFormatException e) {
                    logger.log(Level.WARNING, "Invalid question ID: " + questionIdStr, e);
                }
            }
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error parsing answers JSON: " + answersJson, e);
        }
        
        return result;
    }
    
    /**
     * Send success response
     */
    private void sendSuccessResponse(HttpServletResponse response, String message, Long resultId) 
            throws IOException {
        
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("success", true);
        jsonResponse.addProperty("message", message);
        if (resultId != null) {
            jsonResponse.addProperty("resultId", resultId);
        }
        
        response.getWriter().write(jsonResponse.toString());
    }
    
    /**
     * Send error response
     */
    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("success", false);
        jsonResponse.addProperty("message", message);
        
        response.getWriter().write(jsonResponse.toString());
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Redirect GET requests to test page
        response.sendRedirect(request.getContextPath() + "/test");
    }
    
    @Override
    public void destroy() {
        super.destroy();
        logger.info("SubmitServlet destroyed");
    }
}