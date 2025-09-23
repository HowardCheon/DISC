package com.disc.filter;

import com.disc.util.SecurityUtil;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * CSRF (Cross-Site Request Forgery) 방지 필터
 */
@WebFilter(urlPatterns = {"/admin/*"}, filterName = "CSRFFilter")
public class CSRFFilter implements Filter {

    private static final Logger logger = Logger.getLogger(CSRFFilter.class.getName());
    private static final String CSRF_TOKEN_ATTRIBUTE = "csrfToken";
    private static final String CSRF_TOKEN_PARAMETER = "csrfToken";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("CSRF Filter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // GET 요청이나 로그인 페이지는 CSRF 검사 제외
        String method = httpRequest.getMethod();
        String requestURI = httpRequest.getRequestURI();

        if ("GET".equalsIgnoreCase(method) ||
            requestURI.contains("/login") ||
            requestURI.contains("/logout") ||
            requestURI.endsWith(".css") ||
            requestURI.endsWith(".js") ||
            requestURI.endsWith(".png") ||
            requestURI.endsWith(".jpg") ||
            requestURI.endsWith(".gif")) {

            // GET 요청인 경우 CSRF 토큰 생성
            if ("GET".equalsIgnoreCase(method)) {
                ensureCSRFToken(httpRequest);
            }

            chain.doFilter(request, response);
            return;
        }

        // POST, PUT, DELETE 요청에 대해 CSRF 토큰 검증
        HttpSession session = httpRequest.getSession(false);
        if (session == null) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "No session");
            return;
        }

        String sessionToken = (String) session.getAttribute(CSRF_TOKEN_ATTRIBUTE);
        String requestToken = httpRequest.getParameter(CSRF_TOKEN_PARAMETER);

        // AJAX 요청의 경우 헤더에서도 토큰 확인
        if (requestToken == null) {
            requestToken = httpRequest.getHeader("X-CSRF-Token");
        }

        if (sessionToken == null || requestToken == null || !sessionToken.equals(requestToken)) {
            logger.warning("CSRF token validation failed for request: " + requestURI +
                          " from IP: " + getClientIP(httpRequest));
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF token validation failed");
            return;
        }

        chain.doFilter(request, response);
    }

    /**
     * CSRF 토큰이 없으면 생성
     */
    private void ensureCSRFToken(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        String token = (String) session.getAttribute(CSRF_TOKEN_ATTRIBUTE);

        if (token == null) {
            token = SecurityUtil.generateCSRFToken();
            session.setAttribute(CSRF_TOKEN_ATTRIBUTE, token);
        }
    }

    /**
     * 클라이언트 IP 주소 가져오기
     */
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }

        return request.getRemoteAddr();
    }

    @Override
    public void destroy() {
        logger.info("CSRF Filter destroyed");
    }
}