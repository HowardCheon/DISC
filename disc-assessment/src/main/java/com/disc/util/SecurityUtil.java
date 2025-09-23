package com.disc.util;

import org.apache.commons.text.StringEscapeUtils;
import java.util.regex.Pattern;

/**
 * Security utility class for XSS prevention and input validation
 */
public class SecurityUtil {

    // XSS 방지를 위한 패턴들
    private static final Pattern SCRIPT_PATTERN = Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE);
    private static final Pattern SRC_PATTERN = Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern SRC_PATTERN2 = Pattern.compile("src[\r\n]*=[\r\n]*\\\"(.*?)\\\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern SCRIPT_PATTERN2 = Pattern.compile("</script>", Pattern.CASE_INSENSITIVE);
    private static final Pattern SCRIPT_PATTERN3 = Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern EVAL_PATTERN = Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern EXPRESSION_PATTERN = Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern JAVASCRIPT_PATTERN = Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE);
    private static final Pattern VBSCRIPT_PATTERN = Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE);
    private static final Pattern ONLOAD_PATTERN = Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    /**
     * XSS 공격을 방지하기 위해 HTML을 이스케이프 처리
     *
     * @param input 입력 문자열
     * @return 이스케이프 처리된 문자열
     */
    public static String escapeHtml(String input) {
        if (input == null) {
            return null;
        }
        return StringEscapeUtils.escapeHtml4(input);
    }

    /**
     * 사용자 입력에서 위험한 스크립트 제거
     *
     * @param input 입력 문자열
     * @return 정제된 문자열
     */
    public static String cleanXSS(String input) {
        if (input == null) {
            return null;
        }

        // HTML 이스케이프 처리
        String value = escapeHtml(input);

        // 위험한 패턴들 제거
        value = SCRIPT_PATTERN.matcher(value).replaceAll("");
        value = SRC_PATTERN.matcher(value).replaceAll("");
        value = SRC_PATTERN2.matcher(value).replaceAll("");
        value = SCRIPT_PATTERN2.matcher(value).replaceAll("");
        value = SCRIPT_PATTERN3.matcher(value).replaceAll("");
        value = EVAL_PATTERN.matcher(value).replaceAll("");
        value = EXPRESSION_PATTERN.matcher(value).replaceAll("");
        value = JAVASCRIPT_PATTERN.matcher(value).replaceAll("");
        value = VBSCRIPT_PATTERN.matcher(value).replaceAll("");
        value = ONLOAD_PATTERN.matcher(value).replaceAll("");

        return value;
    }

    /**
     * 사용자 이름 유효성 검사
     *
     * @param userName 사용자 이름
     * @return 유효한 경우 true
     */
    public static boolean isValidUserName(String userName) {
        if (userName == null || userName.trim().isEmpty()) {
            return false;
        }

        // 길이 제한 (1-50자)
        if (userName.length() > 50) {
            return false;
        }

        // 특수문자 제한 (기본적인 한글, 영문, 숫자, 공백만 허용)
        Pattern validPattern = Pattern.compile("^[가-힣a-zA-Z0-9\\s]+$");
        return validPattern.matcher(userName).matches();
    }

    /**
     * URL 토큰 유효성 검사
     *
     * @param token URL 토큰
     * @return 유효한 경우 true
     */
    public static boolean isValidToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        // 토큰 길이 검사 (16자)
        if (token.length() != 16) {
            return false;
        }

        // 영문자와 숫자만 허용
        Pattern tokenPattern = Pattern.compile("^[a-zA-Z0-9]+$");
        return tokenPattern.matcher(token).matches();
    }

    /**
     * 설명/메모 필드 유효성 검사
     *
     * @param description 설명 텍스트
     * @return 유효한 경우 true
     */
    public static boolean isValidDescription(String description) {
        if (description == null) {
            return true; // null은 허용 (선택적 필드)
        }

        // 길이 제한 (500자)
        return description.length() <= 500;
    }

    /**
     * SQL Injection 방지를 위한 문자열 검증
     *
     * @param input 입력 문자열
     * @return 위험한 문자열 포함 여부
     */
    public static boolean containsSQLInjection(String input) {
        if (input == null) {
            return false;
        }

        String lowercaseInput = input.toLowerCase();

        // 위험한 SQL 키워드들
        String[] sqlKeywords = {
            "drop", "delete", "insert", "update", "create", "alter", "truncate",
            "union", "select", "from", "where", "having", "group by", "order by",
            "exec", "execute", "sp_", "xp_", "/*", "*/", "--", ";",
            "char(", "ascii(", "substring(", "len(", "@@", "waitfor"
        };

        for (String keyword : sqlKeywords) {
            if (lowercaseInput.contains(keyword)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 안전한 정수 파싱
     *
     * @param value 파싱할 문자열
     * @param defaultValue 기본값
     * @return 파싱된 정수 또는 기본값
     */
    public static int safeParseInt(String value, int defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 안전한 긴 정수 파싱
     *
     * @param value 파싱할 문자열
     * @param defaultValue 기본값
     * @return 파싱된 긴 정수 또는 기본값
     */
    public static long safeParseLong(String value, long defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }

        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * CSRF 토큰 생성
     *
     * @return CSRF 토큰
     */
    public static String generateCSRFToken() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 입력값 정제 (XSS 방지 + 트림)
     *
     * @param input 입력값
     * @return 정제된 입력값
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }

        // 앞뒤 공백 제거
        String trimmed = input.trim();

        // XSS 정제
        return cleanXSS(trimmed);
    }
}