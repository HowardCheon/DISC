package com.disc.filter;

import com.disc.util.SecurityUtil;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * XSS (Cross-Site Scripting) 방지 필터
 */
@WebFilter(urlPatterns = {"/*"}, filterName = "XSSFilter")
public class XSSFilter implements Filter {

    private static final Logger logger = Logger.getLogger(XSSFilter.class.getName());

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("XSS Filter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // 정적 리소스는 필터링 제외
        String requestURI = httpRequest.getRequestURI();
        if (requestURI.endsWith(".css") ||
            requestURI.endsWith(".js") ||
            requestURI.endsWith(".png") ||
            requestURI.endsWith(".jpg") ||
            requestURI.endsWith(".gif") ||
            requestURI.endsWith(".ico")) {

            chain.doFilter(request, response);
            return;
        }

        // XSS 방지 래퍼로 요청 감싸기
        XSSRequestWrapper wrappedRequest = new XSSRequestWrapper(httpRequest);
        chain.doFilter(wrappedRequest, response);
    }

    @Override
    public void destroy() {
        logger.info("XSS Filter destroyed");
    }

    /**
     * XSS 방지를 위한 HttpServletRequest 래퍼 클래스
     */
    private static class XSSRequestWrapper extends HttpServletRequestWrapper {

        public XSSRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getParameter(String name) {
            String value = super.getParameter(name);
            return SecurityUtil.sanitizeInput(value);
        }

        @Override
        public String[] getParameterValues(String name) {
            String[] values = super.getParameterValues(name);
            if (values == null) {
                return null;
            }

            String[] sanitizedValues = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                sanitizedValues[i] = SecurityUtil.sanitizeInput(values[i]);
            }
            return sanitizedValues;
        }

        @Override
        public String getHeader(String name) {
            String value = super.getHeader(name);
            return SecurityUtil.sanitizeInput(value);
        }
    }
}