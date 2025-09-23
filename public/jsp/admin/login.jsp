<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>DISC 관리자 로그인</title>

    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Bootstrap Icons -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css" rel="stylesheet">
    <!-- Custom CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">

    <style>
        body {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        }

        .admin-login-container {
            width: 100%;
            max-width: 450px;
            margin: 0 auto;
        }

        .login-card {
            background: rgba(255, 255, 255, 0.95);
            backdrop-filter: blur(10px);
            border-radius: 20px;
            box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
            border: 1px solid rgba(255, 255, 255, 0.2);
            overflow: hidden;
        }

        .login-header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 40px 30px 30px;
            text-align: center;
        }

        .login-header h1 {
            font-size: 2rem;
            font-weight: 700;
            margin-bottom: 8px;
        }

        .login-header .subtitle {
            font-size: 1rem;
            opacity: 0.9;
            margin: 0;
        }

        .admin-icon {
            width: 80px;
            height: 80px;
            background: rgba(255, 255, 255, 0.2);
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 20px;
            font-size: 2.5rem;
        }

        .login-body {
            padding: 40px 30px;
        }

        .form-floating {
            margin-bottom: 20px;
        }

        .form-floating > .form-control {
            border: 2px solid #e1e8ed;
            border-radius: 12px;
            padding: 1rem 0.75rem;
            height: 60px;
            font-size: 1rem;
            transition: all 0.3s ease;
        }

        .form-floating > .form-control:focus {
            border-color: #667eea;
            box-shadow: 0 0 0 0.2rem rgba(102, 126, 234, 0.25);
        }

        .form-floating > label {
            color: #6c757d;
            font-weight: 500;
        }

        .btn-login {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border: none;
            border-radius: 12px;
            padding: 15px;
            font-size: 1.1rem;
            font-weight: 600;
            width: 100%;
            color: white;
            transition: all 0.3s ease;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        .btn-login:hover {
            transform: translateY(-2px);
            box-shadow: 0 10px 25px rgba(102, 126, 234, 0.3);
            color: white;
        }

        .btn-login:active {
            transform: translateY(0);
        }

        .form-check {
            margin: 25px 0;
        }

        .form-check-input:checked {
            background-color: #667eea;
            border-color: #667eea;
        }

        .form-check-label {
            color: #495057;
            font-weight: 500;
        }

        .alert {
            border-radius: 12px;
            border: none;
            font-weight: 500;
            margin-bottom: 25px;
        }

        .alert-danger {
            background: linear-gradient(135deg, #ff6b6b 0%, #ee5a52 100%);
            color: white;
        }

        .alert-success {
            background: linear-gradient(135deg, #51cf66 0%, #40c057 100%);
            color: white;
        }

        .alert-info {
            background: linear-gradient(135deg, #74c0fc 0%, #339af0 100%);
            color: white;
        }

        .back-link {
            text-align: center;
            margin-top: 30px;
            padding-top: 25px;
            border-top: 1px solid #e1e8ed;
        }

        .back-link a {
            color: #667eea;
            text-decoration: none;
            font-weight: 500;
            transition: all 0.3s ease;
            display: inline-flex;
            align-items: center;
            gap: 8px;
        }

        .back-link a:hover {
            color: #764ba2;
            transform: translateX(-5px);
        }

        .password-toggle {
            position: absolute;
            right: 15px;
            top: 50%;
            transform: translateY(-50%);
            background: none;
            border: none;
            color: #6c757d;
            cursor: pointer;
            z-index: 10;
        }

        .password-toggle:hover {
            color: #667eea;
        }

        .loading-spinner {
            display: none;
            width: 20px;
            height: 20px;
            margin-right: 10px;
        }

        .btn-login:disabled {
            opacity: 0.7;
            cursor: not-allowed;
        }

        /* Animation for form appearance */
        .login-card {
            animation: slideUp 0.6s ease-out;
        }

        @keyframes slideUp {
            from {
                opacity: 0;
                transform: translateY(30px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }

        /* Responsive design */
        @media (max-width: 576px) {
            .admin-login-container {
                margin: 20px;
                max-width: none;
            }

            .login-header {
                padding: 30px 20px 25px;
            }

            .login-header h1 {
                font-size: 1.7rem;
            }

            .admin-icon {
                width: 60px;
                height: 60px;
                font-size: 2rem;
            }

            .login-body {
                padding: 30px 20px;
            }
        }

        /* Security badge */
        .security-badge {
            position: absolute;
            top: 15px;
            right: 15px;
            background: rgba(255, 255, 255, 0.2);
            color: white;
            padding: 5px 12px;
            border-radius: 20px;
            font-size: 0.8rem;
            font-weight: 500;
        }
    </style>
</head>
<body>
    <div class="container-fluid">
        <div class="row justify-content-center">
            <div class="col-12">
                <div class="admin-login-container">
                    <div class="login-card">
                        <!-- Header -->
                        <div class="login-header position-relative">
                            <div class="security-badge">
                                <i class="bi bi-shield-lock"></i> 보안 구역
                            </div>
                            <div class="admin-icon">
                                <i class="bi bi-person-gear"></i>
                            </div>
                            <h1>관리자 로그인</h1>
                            <p class="subtitle">DISC 성격검사 관리 시스템</p>
                        </div>

                        <!-- Body -->
                        <div class="login-body">
                            <!-- Error Messages -->
                            <c:if test="${not empty errorMessage}">
                                <div class="alert alert-danger" role="alert">
                                    <i class="bi bi-exclamation-triangle-fill me-2"></i>
                                    ${errorMessage}
                                </div>
                            </c:if>

                            <c:if test="${not empty param.error}">
                                <div class="alert alert-danger" role="alert">
                                    <i class="bi bi-exclamation-triangle-fill me-2"></i>
                                    아이디 또는 비밀번호가 잘못되었습니다.
                                </div>
                            </c:if>

                            <!-- Success Messages -->
                            <c:if test="${not empty param.message and param.message eq 'logout_success'}">
                                <div class="alert alert-success" role="alert">
                                    <i class="bi bi-check-circle-fill me-2"></i>
                                    성공적으로 로그아웃되었습니다.
                                </div>
                            </c:if>

                            <c:if test="${not empty param.message and param.message eq 'session_expired'}">
                                <div class="alert alert-info" role="alert">
                                    <i class="bi bi-info-circle-fill me-2"></i>
                                    세션이 만료되었습니다. 다시 로그인해주세요.
                                </div>
                            </c:if>

                            <!-- Login Form -->
                            <form action="${pageContext.request.contextPath}/admin/login" method="post"
                                  class="admin-login-form" id="loginForm" novalidate>

                                <!-- Username Field -->
                                <div class="form-floating">
                                    <input type="text" class="form-control" id="username" name="username"
                                           placeholder="관리자 아이디" value="${username}" required
                                           autocomplete="username" autofocus>
                                    <label for="username">
                                        <i class="bi bi-person me-2"></i>관리자 아이디
                                    </label>
                                    <div class="invalid-feedback">
                                        아이디를 입력해주세요.
                                    </div>
                                </div>

                                <!-- Password Field -->
                                <div class="form-floating position-relative">
                                    <input type="password" class="form-control" id="password" name="password"
                                           placeholder="비밀번호" required autocomplete="current-password">
                                    <label for="password">
                                        <i class="bi bi-lock me-2"></i>비밀번호
                                    </label>
                                    <button type="button" class="password-toggle" id="passwordToggle">
                                        <i class="bi bi-eye" id="passwordIcon"></i>
                                    </button>
                                    <div class="invalid-feedback">
                                        비밀번호를 입력해주세요.
                                    </div>
                                </div>

                                <!-- Remember Me -->
                                <div class="form-check">
                                    <input type="checkbox" class="form-check-input" id="rememberMe" name="rememberMe">
                                    <label class="form-check-label" for="rememberMe">
                                        <i class="bi bi-clock-history me-2"></i>로그인 상태 유지 (8시간)
                                    </label>
                                </div>

                                <!-- Login Button -->
                                <button type="submit" class="btn btn-login" id="loginBtn">
                                    <div class="spinner-border spinner-border-sm loading-spinner" role="status">
                                        <span class="visually-hidden">로그인 중...</span>
                                    </div>
                                    <i class="bi bi-box-arrow-in-right me-2"></i>
                                    로그인
                                </button>
                            </form>

                            <!-- Back Link -->
                            <div class="back-link">
                                <a href="${pageContext.request.contextPath}/jsp/login.jsp">
                                    <i class="bi bi-arrow-left"></i>
                                    검사 페이지로 돌아가기
                                </a>
                            </div>
                        </div>
                    </div>

                    <!-- Footer Info -->
                    <div class="text-center mt-4">
                        <small class="text-white-50">
                            <i class="bi bi-info-circle me-1"></i>
                            기본 관리자 계정: admin / admin1234
                        </small>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

    <!-- Custom JavaScript -->
    <script>
        document.addEventListener('DOMContentLoaded', function() {
            const loginForm = document.getElementById('loginForm');
            const loginBtn = document.getElementById('loginBtn');
            const loadingSpinner = document.querySelector('.loading-spinner');
            const passwordToggle = document.getElementById('passwordToggle');
            const passwordInput = document.getElementById('password');
            const passwordIcon = document.getElementById('passwordIcon');

            // Form validation
            loginForm.addEventListener('submit', function(event) {
                if (!loginForm.checkValidity()) {
                    event.preventDefault();
                    event.stopPropagation();
                }

                loginForm.classList.add('was-validated');

                if (loginForm.checkValidity()) {
                    // Show loading state
                    loginBtn.disabled = true;
                    loadingSpinner.style.display = 'inline-block';
                }
            });

            // Password toggle functionality
            passwordToggle.addEventListener('click', function() {
                const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
                passwordInput.setAttribute('type', type);

                if (type === 'password') {
                    passwordIcon.className = 'bi bi-eye';
                } else {
                    passwordIcon.className = 'bi bi-eye-slash';
                }
            });

            // Enter key handling
            document.addEventListener('keypress', function(event) {
                if (event.key === 'Enter' && !loginBtn.disabled) {
                    loginForm.submit();
                }
            });

            // Auto-dismiss alerts after 5 seconds
            const alerts = document.querySelectorAll('.alert');
            alerts.forEach(alert => {
                setTimeout(() => {
                    alert.style.opacity = '0';
                    setTimeout(() => {
                        alert.remove();
                    }, 300);
                }, 5000);
            });

            // Focus management
            const usernameInput = document.getElementById('username');
            if (usernameInput.value.trim() === '') {
                usernameInput.focus();
            } else {
                passwordInput.focus();
            }
        });

        // Security: Prevent form resubmission on page refresh
        if (window.history.replaceState) {
            window.history.replaceState(null, null, window.location.href);
        }
    </script>
</body>
</html>