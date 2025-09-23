package com.disc.controller;

import com.disc.dao.SurveyDAO;
import com.disc.dao.UserDAO;
import com.disc.model.Survey;
import com.disc.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

@WebServlet("/api/user-history")
public class UserHistoryServlet extends HttpServlet {
    private UserDAO userDAO;
    private SurveyDAO surveyDAO;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        userDAO = new UserDAO();
        surveyDAO = new SurveyDAO();
        objectMapper = new ObjectMapper();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");

        try {
            if ("searchUsers".equals(action)) {
                handleSearchUsers(request, response);
            } else if ("getUserHistory".equals(action)) {
                handleGetUserHistory(request, response);
            } else if ("getTestResult".equals(action)) {
                handleGetTestResult(request, response);
            } else {
                sendErrorResponse(response, "Invalid action");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(response, "서버 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private void handleSearchUsers(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String searchTerm = request.getParameter("searchTerm");

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            sendErrorResponse(response, "검색어를 입력해주세요");
            return;
        }

        List<User> users = userDAO.searchUsers(searchTerm.trim());

        ObjectNode result = objectMapper.createObjectNode();
        result.put("success", true);

        ArrayNode userArray = objectMapper.createArrayNode();
        for (User user : users) {
            ObjectNode userNode = objectMapper.createObjectNode();
            userNode.put("id", user.getId());
            userNode.put("name", user.getName());
            userNode.put("email", user.getEmail());
            userNode.put("department", user.getDepartment());
            userNode.put("position", user.getPosition());
            userArray.add(userNode);
        }

        result.set("users", userArray);

        response.getWriter().write(objectMapper.writeValueAsString(result));
    }

    private void handleGetUserHistory(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String userIdStr = request.getParameter("userId");

        if (userIdStr == null) {
            sendErrorResponse(response, "사용자 ID가 필요합니다");
            return;
        }

        try {
            Long userId = Long.parseLong(userIdStr);
            User user = userDAO.getUserById(userId);

            if (user == null) {
                sendErrorResponse(response, "사용자를 찾을 수 없습니다");
                return;
            }

            List<Survey> testHistory = getUserTestHistory(userId);

            ObjectNode result = objectMapper.createObjectNode();
            result.put("success", true);

            // 사용자 정보
            ObjectNode userNode = objectMapper.createObjectNode();
            userNode.put("id", user.getId());
            userNode.put("name", user.getName());
            userNode.put("email", user.getEmail());
            userNode.put("department", user.getDepartment());
            userNode.put("position", user.getPosition());
            result.set("user", userNode);

            // 검사 이력
            ArrayNode historyArray = objectMapper.createArrayNode();
            for (Survey survey : testHistory) {
                ObjectNode surveyNode = objectMapper.createObjectNode();
                surveyNode.put("id", survey.getId());
                surveyNode.put("sentDate", survey.getSentDate() != null ? survey.getSentDate().toString() : "");
                surveyNode.put("startDate", survey.getStartDate() != null ? survey.getStartDate().toString() : "");
                surveyNode.put("completedDate", survey.getCompletedDate() != null ? survey.getCompletedDate().toString() : "");
                surveyNode.put("status", survey.getStatus());
                surveyNode.put("dScore", survey.getDScore());
                surveyNode.put("iScore", survey.getIScore());
                surveyNode.put("sScore", survey.getSScore());
                surveyNode.put("cScore", survey.getCScore());
                surveyNode.put("primaryType", survey.getPrimaryType());
                surveyNode.put("description", survey.getDescription());
                historyArray.add(surveyNode);
            }

            result.set("history", historyArray);
            result.put("totalTests", testHistory.size());

            response.getWriter().write(objectMapper.writeValueAsString(result));

        } catch (NumberFormatException e) {
            sendErrorResponse(response, "잘못된 사용자 ID입니다");
        }
    }

    private void handleGetTestResult(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String surveyIdStr = request.getParameter("surveyId");

        if (surveyIdStr == null) {
            sendErrorResponse(response, "검사 ID가 필요합니다");
            return;
        }

        try {
            Long surveyId = Long.parseLong(surveyIdStr);
            Survey survey = surveyDAO.getSurveyById(surveyId);

            if (survey == null) {
                sendErrorResponse(response, "검사를 찾을 수 없습니다");
                return;
            }

            ObjectNode result = objectMapper.createObjectNode();
            result.put("success", true);

            ObjectNode surveyNode = objectMapper.createObjectNode();
            surveyNode.put("id", survey.getId());
            surveyNode.put("dScore", survey.getDScore());
            surveyNode.put("iScore", survey.getIScore());
            surveyNode.put("sScore", survey.getSScore());
            surveyNode.put("cScore", survey.getCScore());
            surveyNode.put("primaryType", survey.getPrimaryType());
            surveyNode.put("description", survey.getDescription());
            surveyNode.put("recommendations", survey.getRecommendations());
            surveyNode.put("strengths", survey.getStrengths());
            surveyNode.put("weaknesses", survey.getWeaknesses());
            surveyNode.put("completedDate", survey.getCompletedDate() != null ? survey.getCompletedDate().toString() : "");

            result.set("survey", surveyNode);

            response.getWriter().write(objectMapper.writeValueAsString(result));

        } catch (NumberFormatException e) {
            sendErrorResponse(response, "잘못된 검사 ID입니다");
        }
    }

    private List<Survey> getUserTestHistory(Long userId) {
        List<Survey> allSurveys = surveyDAO.getSurveysByUserId(userId);

        // 완료된 검사만 필터링
        List<Survey> completedSurveys = new ArrayList<>();
        for (Survey survey : allSurveys) {
            if ("COMPLETED".equals(survey.getStatus())) {
                completedSurveys.add(survey);
            }
        }

        // 완료일시 기준으로 내림차순 정렬 (최신순)
        Collections.sort(completedSurveys, new Comparator<Survey>() {
            @Override
            public int compare(Survey s1, Survey s2) {
                if (s1.getCompletedDate() == null && s2.getCompletedDate() == null) {
                    return 0;
                }
                if (s1.getCompletedDate() == null) {
                    return 1;
                }
                if (s2.getCompletedDate() == null) {
                    return -1;
                }
                return s2.getCompletedDate().compareTo(s1.getCompletedDate());
            }
        });

        return completedSurveys;
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        ObjectNode errorResponse = objectMapper.createObjectNode();
        errorResponse.put("success", false);
        errorResponse.put("message", message);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}