package com.disc.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Question model for DISC personality assessment
 * 
 * This class represents a single question in the DISC assessment with
 * four options corresponding to the four DISC personality types.
 */
public class Question {
    
    /**
     * Question number (1-28)
     */
    private int number;
    
    /**
     * Map of DISC type to option text
     * Keys: "D", "I", "S", "C"
     * Values: Option text for each personality type
     */
    private Map<String, String> options;
    
    /**
     * Default constructor
     */
    public Question() {
        this.options = new HashMap<>();
    }
    
    /**
     * Constructor with question number
     * 
     * @param number The question number
     */
    public Question(int number) {
        this.number = number;
        this.options = new HashMap<>();
    }
    
    /**
     * Constructor with all parameters
     * 
     * @param number The question number
     * @param options Map of DISC type to option text
     */
    public Question(int number, Map<String, String> options) {
        this.number = number;
        this.options = new HashMap<>(options);
    }
    
    /**
     * Constructor with individual options
     * 
     * @param number The question number
     * @param dOption D type option text
     * @param iOption I type option text
     * @param sOption S type option text
     * @param cOption C type option text
     */
    public Question(int number, String dOption, String iOption, String sOption, String cOption) {
        this.number = number;
        this.options = new HashMap<>();
        this.options.put("D", dOption);
        this.options.put("I", iOption);
        this.options.put("S", sOption);
        this.options.put("C", cOption);
    }
    
    // Getters and Setters
    
    /**
     * Get the question number
     * 
     * @return The question number
     */
    public int getNumber() {
        return number;
    }
    
    /**
     * Set the question number
     * 
     * @param number The question number
     */
    public void setNumber(int number) {
        this.number = number;
    }
    
    /**
     * Get all options
     * 
     * @return Map of DISC type to option text
     */
    public Map<String, String> getOptions() {
        return new HashMap<>(options);
    }
    
    /**
     * Set all options
     * 
     * @param options Map of DISC type to option text
     */
    public void setOptions(Map<String, String> options) {
        this.options = new HashMap<>(options);
    }
    
    /**
     * Get option text for a specific DISC type
     * 
     * @param discType The DISC type ("D", "I", "S", or "C")
     * @return The option text for the specified type
     */
    public String getOption(String discType) {
        return options.get(discType);
    }
    
    /**
     * Set option text for a specific DISC type
     * 
     * @param discType The DISC type ("D", "I", "S", or "C")
     * @param optionText The option text
     */
    public void setOption(String discType, String optionText) {
        this.options.put(discType, optionText);
    }
    
    /**
     * Get D type option
     * 
     * @return D type option text
     */
    public String getDOption() {
        return options.get("D");
    }
    
    /**
     * Set D type option
     * 
     * @param dOption D type option text
     */
    public void setDOption(String dOption) {
        this.options.put("D", dOption);
    }
    
    /**
     * Get I type option
     * 
     * @return I type option text
     */
    public String getIOption() {
        return options.get("I");
    }
    
    /**
     * Set I type option
     * 
     * @param iOption I type option text
     */
    public void setIOption(String iOption) {
        this.options.put("I", iOption);
    }
    
    /**
     * Get S type option
     * 
     * @return S type option text
     */
    public String getSOption() {
        return options.get("S");
    }
    
    /**
     * Set S type option
     * 
     * @param sOption S type option text
     */
    public void setSOption(String sOption) {
        this.options.put("S", sOption);
    }
    
    /**
     * Get C type option
     * 
     * @return C type option text
     */
    public String getCOption() {
        return options.get("C");
    }
    
    /**
     * Set C type option
     * 
     * @param cOption C type option text
     */
    public void setCOption(String cOption) {
        this.options.put("C", cOption);
    }
    
    /**
     * Check if the question has all required options
     * 
     * @return true if all four DISC options are present, false otherwise
     */
    public boolean isComplete() {
        return options.containsKey("D") && options.get("D") != null && !options.get("D").trim().isEmpty() &&
               options.containsKey("I") && options.get("I") != null && !options.get("I").trim().isEmpty() &&
               options.containsKey("S") && options.get("S") != null && !options.get("S").trim().isEmpty() &&
               options.containsKey("C") && options.get("C") != null && !options.get("C").trim().isEmpty();
    }
    
    /**
     * Validate the question
     * 
     * @return true if the question is valid, false otherwise
     */
    public boolean isValid() {
        return number > 0 && number <= 28 && isComplete();
    }
    
    /**
     * Get validation errors
     * 
     * @return String describing validation errors, or null if valid
     */
    public String getValidationErrors() {
        StringBuilder errors = new StringBuilder();
        
        if (number <= 0 || number > 28) {
            errors.append("Question number must be between 1 and 28. ");
        }
        
        String[] types = {"D", "I", "S", "C"};
        for (String type : types) {
            if (!options.containsKey(type) || options.get(type) == null || options.get(type).trim().isEmpty()) {
                errors.append("Missing or empty option for type ").append(type).append(". ");
            }
        }
        
        return errors.length() > 0 ? errors.toString().trim() : null;
    }
    
    /**
     * Create a copy of this question
     * 
     * @return A new Question instance with the same data
     */
    public Question copy() {
        return new Question(this.number, this.options);
    }
    
    /**
     * Convert question to JSON-like string representation
     * 
     * @return JSON-like string representation
     */
    public String toJsonString() {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"number\": ").append(number).append(",\n");
        json.append("  \"options\": {\n");
        json.append("    \"D\": \"").append(escapeJson(options.get("D"))).append("\",\n");
        json.append("    \"I\": \"").append(escapeJson(options.get("I"))).append("\",\n");
        json.append("    \"S\": \"").append(escapeJson(options.get("S"))).append("\",\n");
        json.append("    \"C\": \"").append(escapeJson(options.get("C"))).append("\"\n");
        json.append("  }\n");
        json.append("}");
        return json.toString();
    }
    
    /**
     * Escape special characters for JSON
     * 
     * @param text The text to escape
     * @return Escaped text
     */
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Question question = (Question) obj;
        return number == question.number && Objects.equals(options, question.options);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(number, options);
    }
    
    @Override
    public String toString() {
        return "Question{" +
               "number=" + number +
               ", options=" + options +
               '}';
    }
    
    /**
     * Builder pattern for creating Question instances
     */
    public static class Builder {
        private int number;
        private Map<String, String> options = new HashMap<>();
        
        public Builder number(int number) {
            this.number = number;
            return this;
        }
        
        public Builder dOption(String dOption) {
            this.options.put("D", dOption);
            return this;
        }
        
        public Builder iOption(String iOption) {
            this.options.put("I", iOption);
            return this;
        }
        
        public Builder sOption(String sOption) {
            this.options.put("S", sOption);
            return this;
        }
        
        public Builder cOption(String cOption) {
            this.options.put("C", cOption);
            return this;
        }
        
        public Builder option(String discType, String optionText) {
            this.options.put(discType, optionText);
            return this;
        }
        
        public Question build() {
            return new Question(number, options);
        }
    }
    
    /**
     * Create a new builder instance
     * 
     * @return A new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
}