<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>잘못된 검사 링크 - DISC 검사</title>

    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Bootstrap Icons -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css" rel="stylesheet">

    <style>
        body {
            background: linear-gradient(135deg, #ffeaa7 0%, #fab1a0 100%);
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
            max-width: 700px;
            margin: 0 auto;
        }

        .error-icon {
            font-size: 8rem;
            color: #fd79a8;
            margin-bottom: 1rem;
            animation: bounce 2s infinite;
        }

        @keyframes bounce {
            0%, 20%, 50%, 80%, 100% { transform: translateY(0); }
            40% { transform: translateY(-20px); }
            60% { transform: translateY(-10px); }
        }

        .error-title {
            font-size: 2.5rem;
            font-weight: bold;
            color: #2d3436;
            margin-bottom: 1rem;
        }

        .error-message {
            font-size: 1.3rem;
            color: #636e72;
            margin-bottom: 2rem;
        }

        .error-description {
            color: #74b9ff;
            margin-bottom: 2rem;
            line-height: 1.6;
            background: #f8f9fa;
            padding: 1.5rem;
            border-radius: 15px;
            border-left: 5px solid #fd79a8;
        }

        .reason-list {
            text-align: left;
            background: #fff5f5;
            padding: 1.5rem;
            border-radius: 15px;
            margin: 2rem 0;
        }

        .reason-list h6 {
            color: #e17055;
            margin-bottom: 1rem;
        }

        .reason-list ul {
            margin-bottom: 0;
        }

        .reason-list li {
            margin-bottom: 0.5rem;
            color: #636e72;
        }

        .btn-custom {
            border-radius: 50px;
            padding: 12px 30px;
            font-weight: 500;
            text-decoration: none;
            transition: all 0.3s ease;
            margin: 0.5rem;
        }

        .btn-custom:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        }

        .contact-info {
            background: #e8f4fd;
            padding: 1.5rem;
            border-radius: 15px;
            margin-top: 2rem;
        }

        @media (max-width: 768px) {
            .error-container {
                margin: 1rem;
                padding: 2rem;
            }

            .error-title {
                font-size: 2rem;
            }

            .error-icon {
                font-size: 5rem;
            }

            .error-message {
                font-size: 1.1rem;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="error-container">
            <div class="error-icon">
                <i class="bi bi-link-45deg"></i>
            </div>

            <h1 class="error-title">잘못된 검사 링크</h1>

            <p class="error-message">
                죄송합니다. 접속하신 DISC 검사 링크가 유효하지 않습니다.
            </p>

            <div class="error-description">
                <i class="bi bi-info-circle text-primary"></i>
                <strong>DISC 성격검사란?</strong><br>
                DISC 검사는 개인의 행동 유형을 4가지 스타일(D-주도형, I-사교형, S-안정형, C-신중형)로 분석하여
                자신의 성격 특성을 이해하고 대인관계나 업무 스타일을 개선하는 데 도움을 주는 검사입니다.
            </div>

            <div class="reason-list">
                <h6><i class="bi bi-exclamation-triangle text-warning"></i> 다음과 같은 이유일 수 있습니다:</h6>
                <ul>
                    <li><strong>만료된 링크:</strong> 검사 링크의 유효기간이 지났습니다</li>
                    <li><strong>잘못된 URL:</strong> 링크가 올바르게 복사되지 않았습니다</li>
                    <li><strong>이미 완료된 검사:</strong> 해당 링크로 이미 검사를 완료했습니다</li>
                    <li><strong>비활성화된 링크:</strong> 관리자가 링크를 비활성화했습니다</li>
                    <li><strong>존재하지 않는 링크:</strong> 잘못된 토큰이거나 삭제된 링크입니다</li>
                </ul>
            </div>

            <div class="d-flex flex-column flex-sm-row gap-3 justify-content-center">
                <a href="${pageContext.request.contextPath}/" class="btn btn-primary btn-custom">
                    <i class="bi bi-house-door"></i> 메인 페이지로
                </a>

                <button onclick="copyCurrentUrl()" class="btn btn-outline-info btn-custom">
                    <i class="bi bi-clipboard"></i> 현재 URL 복사
                </button>
            </div>

            <div class="contact-info">
                <h6 class="text-primary">
                    <i class="bi bi-headset"></i> 도움이 필요하신가요?
                </h6>
                <p class="mb-2">검사 링크를 제공받으신 담당자나 관리자에게 문의해주세요.</p>
                <div class="small text-muted">
                    <p class="mb-1"><strong>현재 URL:</strong></p>
                    <code id="currentUrl" class="text-break">${pageContext.request.requestURL}${pageContext.request.queryString != null ? '?' : ''}${pageContext.request.queryString != null ? pageContext.request.queryString : ''}</code>
                </div>
            </div>

            <hr class="my-4">

            <div class="small text-muted">
                <p class="mb-1">
                    <i class="bi bi-clock"></i>
                    접속 시간: <script>document.write(new Date().toLocaleString('ko-KR'));</script>
                </p>
                <p class="mb-0">
                    위 정보를 담당자에게 전달해주시면 문제 해결에 도움이 됩니다.
                </p>
            </div>
        </div>
    </div>

    <!-- Bootstrap 5 JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

    <script>
        function copyCurrentUrl() {
            const url = document.getElementById('currentUrl').textContent;

            if (navigator.clipboard) {
                navigator.clipboard.writeText(url).then(function() {
                    // 성공 메시지 표시
                    const button = event.target;
                    const originalText = button.innerHTML;
                    button.innerHTML = '<i class="bi bi-check-circle"></i> 복사됨';
                    button.classList.remove('btn-outline-info');
                    button.classList.add('btn-success');

                    setTimeout(function() {
                        button.innerHTML = originalText;
                        button.classList.remove('btn-success');
                        button.classList.add('btn-outline-info');
                    }, 2000);
                }).catch(function(err) {
                    console.error('복사 실패: ', err);
                    fallbackCopyTextToClipboard(url);
                });
            } else {
                fallbackCopyTextToClipboard(url);
            }
        }

        function fallbackCopyTextToClipboard(text) {
            const textArea = document.createElement('textarea');
            textArea.value = text;
            textArea.style.position = 'fixed';
            textArea.style.left = '-999999px';
            textArea.style.top = '-999999px';
            document.body.appendChild(textArea);
            textArea.focus();
            textArea.select();

            try {
                document.execCommand('copy');
                alert('URL이 클립보드에 복사되었습니다.');
            } catch (err) {
                alert('복사에 실패했습니다. 수동으로 복사해주세요.');
            }

            document.body.removeChild(textArea);
        }

        // URL 파라미터에서 토큰 정보 추출해서 표시
        const urlParams = new URLSearchParams(window.location.search);
        const token = urlParams.get('token');
        if (token) {
            document.getElementById('currentUrl').innerHTML += '<br><strong>토큰:</strong> ' + token;
        }
    </script>
</body>
</html>