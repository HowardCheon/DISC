<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>서버 오류 - DISC 검사</title>

    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Bootstrap Icons -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css" rel="stylesheet">

    <style>
        body {
            background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        }

        .error-container {
            background: white;
            border-radius: 20px;
            box-shadow: 0 20px 40px rgba(0,0,0,0.1);
            padding: 3rem;
            text-align: center;
            max-width: 600px;
            margin: 0 auto;
        }

        .error-icon {
            font-size: 8rem;
            color: #dc3545;
            margin-bottom: 1rem;
            animation: pulse 2s infinite;
        }

        @keyframes pulse {
            0% { transform: scale(1); }
            50% { transform: scale(1.05); }
            100% { transform: scale(1); }
        }

        .error-code {
            font-size: 6rem;
            font-weight: bold;
            color: #dc3545;
            margin-bottom: 1rem;
        }

        .error-message {
            font-size: 1.5rem;
            color: #6c757d;
            margin-bottom: 2rem;
        }

        .error-description {
            color: #868e96;
            margin-bottom: 2rem;
            line-height: 1.6;
        }

        .btn-custom {
            border-radius: 50px;
            padding: 12px 30px;
            font-weight: 500;
            text-decoration: none;
            transition: all 0.3s ease;
        }

        .btn-custom:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        }

        .error-details {
            background: #f8f9fa;
            border-radius: 10px;
            padding: 1rem;
            margin: 1rem 0;
            border-left: 4px solid #dc3545;
        }

        @media (max-width: 768px) {
            .error-container {
                margin: 1rem;
                padding: 2rem;
            }

            .error-code {
                font-size: 4rem;
            }

            .error-icon {
                font-size: 5rem;
            }

            .error-message {
                font-size: 1.2rem;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="error-container">
            <div class="error-icon">
                <i class="bi bi-exclamation-octagon"></i>
            </div>

            <div class="error-code">500</div>

            <h1 class="error-message">서버 내부 오류</h1>

            <p class="error-description">
                죄송합니다. 서버에서 오류가 발생했습니다.<br>
                잠시 후 다시 시도해주시거나 관리자에게 문의해주세요.
            </p>

            <div class="error-details">
                <h6 class="text-danger">
                    <i class="bi bi-info-circle"></i> 기술적 정보
                </h6>
                <div class="small text-start">
                    <p class="mb-1"><strong>요청 URI:</strong> ${pageContext.request.requestURI}</p>
                    <p class="mb-1"><strong>요청 방법:</strong> ${pageContext.request.method}</p>
                    <p class="mb-1"><strong>사용자 에이전트:</strong> ${header['User-Agent']}</p>
                    <p class="mb-0"><strong>시간:</strong>
                        <script>document.write(new Date().toLocaleString('ko-KR'));</script>
                    </p>
                    <%-- 개발 환경에서만 예외 정보 표시 --%>
                    <%
                        String serverInfo = application.getServerInfo();
                        if (serverInfo != null && (serverInfo.contains("development") || serverInfo.contains("localhost"))) {
                            if (exception != null) {
                    %>
                        <hr class="my-2">
                        <p class="mb-1"><strong>예외 유형:</strong> <%= exception.getClass().getSimpleName() %></p>
                        <p class="mb-0"><strong>메시지:</strong> <%= exception.getMessage() != null ? exception.getMessage() : "메시지 없음" %></p>
                    <%
                            }
                        }
                    %>
                </div>
            </div>

            <div class="d-flex flex-column flex-sm-row gap-3 justify-content-center">
                <a href="${pageContext.request.contextPath}/" class="btn btn-primary btn-custom">
                    <i class="bi bi-house-door"></i> 메인 페이지로
                </a>

                <button onclick="location.reload()" class="btn btn-outline-secondary btn-custom">
                    <i class="bi bi-arrow-clockwise"></i> 페이지 새로고침
                </button>

                <button onclick="history.back()" class="btn btn-outline-secondary btn-custom">
                    <i class="bi bi-arrow-left"></i> 이전 페이지로
                </button>
            </div>

            <hr class="my-4">

            <div class="small text-muted">
                <p class="mb-1">
                    <i class="bi bi-shield-check"></i>
                    이 오류는 자동으로 로그에 기록되었습니다.
                </p>
                <p class="mb-0">
                    지속적인 문제 발생 시 시스템 관리자에게 위의 기술적 정보와 함께 문의해주세요.
                </p>
            </div>
        </div>
    </div>

    <!-- Bootstrap 5 JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

    <script>
        // 5초 후 자동 새로고침 옵션 (개발 환경에서만)
        if (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') {
            setTimeout(function() {
                if (confirm('5초가 지났습니다. 페이지를 새로고침하시겠습니까?')) {
                    location.reload();
                }
            }, 5000);
        }
    </script>
</body>
</html>