<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>페이지를 찾을 수 없습니다 - DISC 검사</title>

    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Bootstrap Icons -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css" rel="stylesheet">

    <style>
        body {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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
            color: #6c757d;
            margin-bottom: 1rem;
        }

        .error-code {
            font-size: 6rem;
            font-weight: bold;
            color: #495057;
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
                <i class="bi bi-exclamation-triangle"></i>
            </div>

            <div class="error-code">404</div>

            <h1 class="error-message">페이지를 찾을 수 없습니다</h1>

            <p class="error-description">
                요청하신 페이지가 존재하지 않거나 이동되었을 수 있습니다.<br>
                URL을 다시 확인해주시거나 아래 버튼을 통해 이동해주세요.
            </p>

            <div class="d-flex flex-column flex-sm-row gap-3 justify-content-center">
                <a href="${pageContext.request.contextPath}/" class="btn btn-primary btn-custom">
                    <i class="bi bi-house-door"></i> 메인 페이지로
                </a>

                <button onclick="history.back()" class="btn btn-outline-secondary btn-custom">
                    <i class="bi bi-arrow-left"></i> 이전 페이지로
                </button>
            </div>

            <hr class="my-4">

            <div class="small text-muted">
                <p class="mb-1">문제가 지속되면 관리자에게 문의해주세요.</p>
                <p class="mb-0">
                    <strong>요청 URL:</strong>
                    <code>${pageContext.request.requestURL}</code>
                </p>
                <p class="mb-0">
                    <strong>시간:</strong>
                    <script>document.write(new Date().toLocaleString('ko-KR'));</script>
                </p>
            </div>
        </div>
    </div>

    <!-- Bootstrap 5 JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>