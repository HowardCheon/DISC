<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>DISC 성격검사 - 로그인</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="container">
        <div class="login-box">
            <h1>DISC 성격검사</h1>
            <p class="subtitle">당신의 성격 유형을 알아보세요</p>
            
            <c:if test="${not empty param.error}">
                <div class="error-message">
                    잘못된 접근입니다. 유효한 링크를 통해 접속해주세요.
                </div>
            </c:if>
            
            <form action="${pageContext.request.contextPath}/test" method="get" class="login-form">
                <div class="form-group">
                    <label for="name">이름</label>
                    <input type="text" id="name" name="name" required 
                           placeholder="성함을 입력해주세요">
                </div>
                
                <div class="form-group">
                    <label for="token">검사 코드</label>
                    <input type="text" id="token" name="token" required 
                           placeholder="검사 코드를 입력해주세요">
                </div>
                
                <button type="submit" class="btn-primary">검사 시작</button>
            </form>
            
            <div class="admin-link">
                <a href="${pageContext.request.contextPath}/jsp/admin/login.jsp">관리자 로그인</a>
            </div>
        </div>
    </div>
    
    <script src="${pageContext.request.contextPath}/js/main.js"></script>
</body>
</html>