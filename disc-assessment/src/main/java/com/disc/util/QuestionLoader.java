package com.disc.util;

import com.disc.model.Question;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import javax.servlet.ServletContext;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Singleton utility class for loading DISC assessment questions from JSON file
 * 
 * This class handles the loading and caching of questions from the questions.json file
 * located in the WEB-INF/data directory.
 */
public class QuestionLoader {
    
    private static final Logger logger = Logger.getLogger(QuestionLoader.class.getName());
    
    // Singleton instance
    private static volatile QuestionLoader instance;
    
    // Questions cache
    private List<Question> questions;
    private boolean loaded = false;
    private long lastModified = 0;
    
    // File path configuration
    private static final String QUESTIONS_FILE = "/WEB-INF/data/questions.json";
    
    // Gson instance for JSON parsing
    private final Gson gson;
    
    /**
     * Private constructor for Singleton pattern
     */
    private QuestionLoader() {
        this.gson = new Gson();
        this.questions = new ArrayList<>();
    }
    
    /**
     * Get the singleton instance of QuestionLoader
     * 
     * @return QuestionLoader instance
     */
    public static QuestionLoader getInstance() {
        if (instance == null) {
            synchronized (QuestionLoader.class) {
                if (instance == null) {
                    instance = new QuestionLoader();
                }
            }
        }
        return instance;
    }
    
    /**
     * Load questions from JSON file
     * 
     * @param servletContext The servlet context to get the real path
     * @return List of questions
     * @throws IOException if file loading fails
     */
    public synchronized List<Question> loadQuestions(ServletContext servletContext) throws IOException {
        String realPath = servletContext.getRealPath(QUESTIONS_FILE);
        if (realPath == null) {
            throw new IOException("Cannot determine real path for questions file: " + QUESTIONS_FILE);
        }
        
        // Check if file has been modified since last load
        java.io.File file = new java.io.File(realPath);
        if (!file.exists()) {
            throw new IOException("Questions file not found: " + realPath);
        }
        
        long fileLastModified = file.lastModified();
        if (loaded && fileLastModified <= lastModified) {
            // Return cached questions if file hasn't been modified
            return new ArrayList<>(questions);
        }
        
        try {
            // Load questions from file
            questions = loadQuestionsFromFile(realPath);
            loaded = true;
            lastModified = fileLastModified;
            
            logger.info("Successfully loaded " + questions.size() + " questions from " + realPath);
            return new ArrayList<>(questions);
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to load questions from file: " + realPath, e);
            throw new IOException("Failed to load questions: " + e.getMessage(), e);
        }
    }
    
    /**
     * Load questions from file without caching check
     * 
     * @param filePath The absolute path to the questions file
     * @return List of questions
     * @throws IOException if file loading fails
     */
    private List<Question> loadQuestionsFromFile(String filePath) throws IOException {
        List<Question> loadedQuestions = new ArrayList<>();
        
        try (FileInputStream fis = new FileInputStream(filePath);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(isr)) {
            
            // Read entire file content
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line).append("\n");
            }
            
            // Parse JSON
            JsonElement jsonElement = JsonParser.parseString(jsonContent.toString());
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonArray questionsArray = jsonObject.getAsJsonArray("questions");
            
            if (questionsArray == null) {
                throw new IOException("Invalid JSON format: 'questions' array not found");
            }
            
            // Parse each question
            for (JsonElement questionElement : questionsArray) {
                try {
                    Question question = parseQuestion(questionElement.getAsJsonObject());
                    if (question.isValid()) {
                        loadedQuestions.add(question);
                    } else {
                        logger.warning("Invalid question found: " + question.getValidationErrors());
                    }
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Error parsing question: " + questionElement, e);
                }
            }
            
            // Validate question count
            if (loadedQuestions.size() != 28) {
                logger.warning("Expected 28 questions, but loaded " + loadedQuestions.size() + " questions");
            }
            
            // Sort questions by number to ensure correct order
            loadedQuestions.sort((q1, q2) -> Integer.compare(q1.getNumber(), q2.getNumber()));
            
            // Validate question numbering
            validateQuestionNumbering(loadedQuestions);
            
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Error parsing questions JSON file", e);
        }
        
        return loadedQuestions;
    }
    
    /**
     * Parse a single question from JSON object
     * 
     * @param questionJson The JSON object representing a question
     * @return Parsed Question object
     */
    private Question parseQuestion(JsonObject questionJson) {
        int number = questionJson.get("number").getAsInt();
        JsonObject optionsJson = questionJson.getAsJsonObject("options");
        
        Map<String, String> options = new HashMap<>();
        options.put("D", optionsJson.get("D").getAsString());
        options.put("I", optionsJson.get("I").getAsString());
        options.put("S", optionsJson.get("S").getAsString());
        options.put("C", optionsJson.get("C").getAsString());
        
        return new Question(number, options);
    }
    
    /**
     * Validate that questions are numbered correctly (1-28)
     * 
     * @param questions List of questions to validate
     * @throws IOException if numbering is invalid
     */
    private void validateQuestionNumbering(List<Question> questions) throws IOException {
        for (int i = 0; i < questions.size(); i++) {
            int expectedNumber = i + 1;
            int actualNumber = questions.get(i).getNumber();
            
            if (actualNumber != expectedNumber) {
                throw new IOException("Question numbering error: expected " + expectedNumber + 
                                    " but found " + actualNumber + " at position " + i);
            }
        }
    }
    
    /**
     * Get a specific question by number
     * 
     * @param servletContext The servlet context
     * @param questionNumber The question number (1-28)
     * @return The question, or null if not found
     * @throws IOException if loading fails
     */
    public Question getQuestion(ServletContext servletContext, int questionNumber) throws IOException {
        List<Question> allQuestions = loadQuestions(servletContext);
        
        if (questionNumber < 1 || questionNumber > allQuestions.size()) {
            return null;
        }
        
        return allQuestions.get(questionNumber - 1);
    }
    
    /**
     * Get a subset of questions
     * 
     * @param servletContext The servlet context
     * @param startNumber Starting question number (inclusive)
     * @param endNumber Ending question number (inclusive)
     * @return List of questions in the specified range
     * @throws IOException if loading fails
     */
    public List<Question> getQuestions(ServletContext servletContext, int startNumber, int endNumber) throws IOException {
        List<Question> allQuestions = loadQuestions(servletContext);
        
        if (startNumber < 1 || endNumber > allQuestions.size() || startNumber > endNumber) {
            return new ArrayList<>();
        }
        
        return new ArrayList<>(allQuestions.subList(startNumber - 1, endNumber));
    }
    
    /**
     * Get questions as JSON string
     * 
     * @param servletContext The servlet context
     * @return JSON string representation of all questions
     * @throws IOException if loading fails
     */
    public String getQuestionsAsJson(ServletContext servletContext) throws IOException {
        List<Question> allQuestions = loadQuestions(servletContext);
        
        JsonObject result = new JsonObject();
        JsonArray questionsArray = new JsonArray();
        
        for (Question question : allQuestions) {
            JsonObject questionJson = new JsonObject();
            questionJson.addProperty("number", question.getNumber());
            
            JsonObject optionsJson = new JsonObject();
            optionsJson.addProperty("D", question.getDOption());
            optionsJson.addProperty("I", question.getIOption());
            optionsJson.addProperty("S", question.getSOption());
            optionsJson.addProperty("C", question.getCOption());
            
            questionJson.add("options", optionsJson);
            questionsArray.add(questionJson);
        }
        
        result.add("questions", questionsArray);
        return gson.toJson(result);
    }
    
    /**
     * Reload questions from file (force refresh)
     * 
     * @param servletContext The servlet context
     * @return List of questions
     * @throws IOException if loading fails
     */
    public synchronized List<Question> reloadQuestions(ServletContext servletContext) throws IOException {
        loaded = false;
        lastModified = 0;
        return loadQuestions(servletContext);
    }
    
    /**
     * Check if questions are loaded
     * 
     * @return true if questions are loaded, false otherwise
     */
    public boolean isLoaded() {
        return loaded;
    }
    
    /**
     * Get the number of loaded questions
     * 
     * @return Number of questions, or 0 if not loaded
     */
    public int getQuestionCount() {
        return loaded ? questions.size() : 0;
    }
    
    /**
     * Clear the questions cache
     */
    public synchronized void clearCache() {
        questions.clear();
        loaded = false;
        lastModified = 0;
        logger.info("Questions cache cleared");
    }
    
    /**
     * Get file information
     * 
     * @param servletContext The servlet context
     * @return File information string
     */
    public String getFileInfo(ServletContext servletContext) {
        try {
            String realPath = servletContext.getRealPath(QUESTIONS_FILE);
            if (realPath == null) {
                return "File path cannot be determined";
            }
            
            java.io.File file = new java.io.File(realPath);
            if (!file.exists()) {
                return "File does not exist: " + realPath;
            }
            
            return String.format("File: %s\nSize: %d bytes\nLast Modified: %s\nLoaded: %s\nQuestions Count: %d",
                                realPath,
                                file.length(),
                                new java.util.Date(file.lastModified()),
                                loaded,
                                getQuestionCount());
                                
        } catch (Exception e) {
            return "Error getting file info: " + e.getMessage();
        }
    }
    
    /**
     * Validate questions data integrity
     * 
     * @param servletContext The servlet context
     * @return Validation report
     */
    public String validateQuestions(ServletContext servletContext) {
        try {
            List<Question> allQuestions = loadQuestions(servletContext);
            StringBuilder report = new StringBuilder();
            
            report.append("Question Validation Report\n");
            report.append("=========================\n");
            report.append("Total Questions: ").append(allQuestions.size()).append("\n");
            report.append("Expected: 28\n\n");
            
            int validCount = 0;
            int invalidCount = 0;
            
            for (Question question : allQuestions) {
                if (question.isValid()) {
                    validCount++;
                } else {
                    invalidCount++;
                    report.append("Question ").append(question.getNumber())
                          .append(": ").append(question.getValidationErrors()).append("\n");
                }
            }
            
            report.append("\nSummary:\n");
            report.append("Valid Questions: ").append(validCount).append("\n");
            report.append("Invalid Questions: ").append(invalidCount).append("\n");
            report.append("Status: ").append(invalidCount == 0 ? "PASSED" : "FAILED").append("\n");
            
            return report.toString();
            
        } catch (Exception e) {
            return "Validation failed: " + e.getMessage();
        }
    }
    
    /**
     * Get questions for a specific DISC type (for analysis purposes)
     * 
     * @param servletContext The servlet context
     * @param discType The DISC type ("D", "I", "S", or "C")
     * @return List of option texts for the specified type
     * @throws IOException if loading fails
     */
    public List<String> getOptionsForType(ServletContext servletContext, String discType) throws IOException {
        List<Question> allQuestions = loadQuestions(servletContext);
        List<String> options = new ArrayList<>();
        
        for (Question question : allQuestions) {
            String option = question.getOption(discType);
            if (option != null && !option.trim().isEmpty()) {
                options.add(option);
            }
        }
        
        return options;
    }
}