<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page isErrorPage="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>DISC 성격검사 - 오류</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .error-container {
            max-width: 600px;
            margin: 100px auto;
            padding: 40px;
            text-align: center;
        }
        
        .error-box {
            background: white;
            padding: 40px;
            border-radius: 12px;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
        }
        
        .error-icon {
            font-size: 4em;
            color: #e74c3c;
            margin-bottom: 20px;
        }
        
        .error-title {
            color: #2c3e50;
            margin-bottom: 20px;
        }
        
        .error-message {
            color: #7f8c8d;
            margin-bottom: 30px;
            line-height: 1.6;
        }
        
        .action-buttons {
            display: flex;
            gap: 15px;
            justify-content: center;
            flex-wrap: wrap;
        }
    </style>
</head>
<body>
    <div class="error-container">
        <div class="error-box">
            <div class="error-icon">⚠️</div>
            
            <h1 class="error-title">오류가 발생했습니다</h1>
            
            <div class="error-message">
                <c:choose>
                    <c:when test="${param.message == 'database_error'}">
                        데이터베이스 연결에 문제가 발생했습니다.<br>
                        잠시 후 다시 시도해주세요.
                    </c:when>
                    <c:when test="${param.message == 'result_calculation_failed'}">
                        검사 결과 계산 중 오류가 발생했습니다.<br>
                        관리자에게 문의해주세요.
                    </c:when>
                    <c:when test="${param.message == 'system_error'}">
                        시스템 오류가 발생했습니다.<br>
                        잠시 후 다시 시도해주세요.
                    </c:when>
                    <c:otherwise>
                        알 수 없는 오류가 발생했습니다.<br>
                        관리자에게 문의해주세요.
                    </c:otherwise>
                </c:choose>
            </div>
            
            <div class="action-buttons">
                <a href="javascript:history.back()" class="btn-secondary">이전 페이지</a>
                <a href="${pageContext.request.contextPath}/jsp/login.jsp" class="btn-primary">처음으로</a>
            </div>
        </div>
    </div>
</body>
</html>