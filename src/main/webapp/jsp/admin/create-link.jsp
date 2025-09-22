<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>DISC 검사 링크 생성</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="admin-container">
        <header class="admin-header">
            <h1>DISC 검사 관리 시스템</h1>
            <div class="admin-nav">
                <span>관리자: ${sessionScope.adminUser}</span>
                <a href="${pageContext.request.contextPath}/admin/logout" class="btn-secondary">로그아웃</a>
            </div>
        </header>
        
        <nav class="admin-sidebar">
            <ul>
                <li><a href="${pageContext.request.contextPath}/jsp/admin/dashboard.jsp">대시보드</a></li>
                <li><a href="${pageContext.request.contextPath}/jsp/admin/create-link.jsp" class="active">링크 생성</a></li>
                <li><a href="${pageContext.request.contextPath}/admin/links">링크 관리</a></li>
                <li><a href="${pageContext.request.contextPath}/admin/results">결과 관리</a></li>
                <li><a href="${pageContext.request.contextPath}/admin/statistics">통계</a></li>
            </ul>
        </nav>
        
        <main class="admin-main">
            <div class="page-header">
                <h2>새 검사 링크 생성</h2>
                <p>DISC 검사를 위한 새로운 링크를 생성합니다.</p>
            </div>
            
            <c:if test="${not empty param.success}">
                <div class="success-message">
                    링크가 성공적으로 생성되었습니다!
                </div>
            </c:if>
            
            <c:if test="${not empty param.error}">
                <div class="error-message">
                    링크 생성 중 오류가 발생했습니다: ${param.error}
                </div>
            </c:if>
            
            <form action="${pageContext.request.contextPath}/admin/link/create" method="post" class="create-link-form">
                <div class="form-section">
                    <h3>기본 정보</h3>
                    
                    <div class="form-group">
                        <label for="linkName">링크명 *</label>
                        <input type="text" id="linkName" name="linkName" required 
                               placeholder="예: 2024년 신입사원 DISC 검사">
                        <small>링크를 구분하기 위한 이름입니다.</small>
                    </div>
                    
                    <div class="form-group">
                        <label for="description">설명</label>
                        <textarea id="description" name="description" rows="3" 
                                  placeholder="이 링크의 용도나 대상에 대한 설명을 입력하세요."></textarea>
                    </div>
                </div>
                
                <div class="form-section">
                    <h3>접근 설정</h3>
                    
                    <div class="form-row">
                        <div class="form-group">
                            <label for="maxUsage">최대 사용 횟수</label>
                            <input type="number" id="maxUsage" name="maxUsage" min="1" max="1000" value="50">
                            <small>이 링크로 검사할 수 있는 최대 인원 수</small>
                        </div>
                        
                        <div class="form-group">
                            <label for="expiryDays">유효 기간 (일)</label>
                            <select id="expiryDays" name="expiryDays">
                                <option value="7">7일</option>
                                <option value="14">14일</option>
                                <option value="30" selected>30일</option>
                                <option value="60">60일</option>
                                <option value="90">90일</option>
                                <option value="365">1년</option>
                            </select>
                        </div>
                    </div>
                    
                    <div class="form-group">
                        <label class="checkbox-label">
                            <input type="checkbox" name="requireName" checked> 
                            이름 입력 필수
                        </label>
                        <small>체크 해제 시 익명으로 검사 가능</small>
                    </div>
                    
                    <div class="form-group">
                        <label class="checkbox-label">
                            <input type="checkbox" name="allowRetake"> 
                            재검사 허용
                        </label>
                        <small>같은 사용자가 여러 번 검사할 수 있도록 허용</small>
                    </div>
                </div>
                
                <div class="form-section">
                    <h3>결과 설정</h3>
                    
                    <div class="form-group">
                        <label class="checkbox-label">
                            <input type="checkbox" name="showDetailedResult" checked> 
                            상세 결과 표시
                        </label>
                        <small>체크 해제 시 기본 결과만 표시</small>
                    </div>
                    
                    <div class="form-group">
                        <label class="checkbox-label">
                            <input type="checkbox" name="allowResultShare" checked> 
                            결과 공유 허용
                        </label>
                        <small>사용자가 결과를 공유할 수 있도록 허용</small>
                    </div>
                    
                    <div class="form-group">
                        <label class="checkbox-label">
                            <input type="checkbox" name="sendNotification"> 
                            완료 알림 받기
                        </label>
                        <small>검사 완료 시 관리자에게 이메일 알림</small>
                    </div>
                </div>
                
                <div class="form-section">
                    <h3>고급 설정</h3>
                    
                    <div class="form-group">
                        <label for="customMessage">사용자 메시지</label>
                        <textarea id="customMessage" name="customMessage" rows="3" 
                                  placeholder="검사 시작 전 사용자에게 표시할 메시지 (선택사항)"></textarea>
                    </div>
                    
                    <div class="form-group">
                        <label for="redirectUrl">완료 후 이동 URL</label>
                        <input type="url" id="redirectUrl" name="redirectUrl" 
                               placeholder="https://example.com (선택사항)">
                        <small>검사 완료 후 사용자를 특정 페이지로 이동</small>
                    </div>
                </div>
                
                <div class="form-actions">
                    <button type="button" onclick="previewLink()" class="btn-secondary">미리보기</button>
                    <button type="submit" class="btn-primary">링크 생성</button>
                </div>
            </form>
            
            <!-- 미리보기 모달 -->
            <div id="previewModal" class="modal" style="display: none;">
                <div class="modal-content">
                    <div class="modal-header">
                        <h3>링크 미리보기</h3>
                        <button type="button" onclick="closePreview()" class="modal-close">&times;</button>
                    </div>
                    <div class="modal-body">
                        <div class="preview-info">
                            <h4>링크 정보</h4>
                            <div class="info-row">
                                <span class="label">링크명:</span>
                                <span id="previewName"></span>
                            </div>
                            <div class="info-row">
                                <span class="label">최대 사용:</span>
                                <span id="previewUsage"></span>
                            </div>
                            <div class="info-row">
                                <span class="label">유효기간:</span>
                                <span id="previewExpiry"></span>
                            </div>
                            <div class="info-row">
                                <span class="label">생성될 토큰:</span>
                                <code id="previewToken"></code>
                            </div>
                        </div>
                        
                        <div class="preview-url">
                            <h4>접속 URL</h4>
                            <div class="url-box">
                                <code id="previewUrl"></code>
                                <button type="button" onclick="copyPreviewUrl()" class="btn-copy">복사</button>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" onclick="closePreview()" class="btn-secondary">닫기</button>
                    </div>
                </div>
            </div>
        </main>
    </div>
    
    <script>
        function previewLink() {
            const linkName = document.getElementById('linkName').value || '(링크명 없음)';
            const maxUsage = document.getElementById('maxUsage').value;
            const expiryDays = document.getElementById('expiryDays').value;
            
            // 임시 토큰 생성 (실제로는 서버에서 생성)
            const tempToken = 'DISC' + Math.random().toString(36).substr(2, 8).toUpperCase();
            
            document.getElementById('previewName').textContent = linkName;
            document.getElementById('previewUsage').textContent = maxUsage + '회';
            document.getElementById('previewExpiry').textContent = expiryDays + '일';
            document.getElementById('previewToken').textContent = tempToken;
            
            const baseUrl = window.location.origin + '${pageContext.request.contextPath}/jsp/login.jsp';
            const previewUrl = baseUrl + '?token=' + tempToken;
            document.getElementById('previewUrl').textContent = previewUrl;
            
            document.getElementById('previewModal').style.display = 'block';
        }
        
        function closePreview() {
            document.getElementById('previewModal').style.display = 'none';
        }
        
        function copyPreviewUrl() {
            const url = document.getElementById('previewUrl').textContent;
            navigator.clipboard.writeText(url).then(function() {
                alert('URL이 클립보드에 복사되었습니다.');
            });
        }
        
        // 모달 외부 클릭 시 닫기
        window.onclick = function(event) {
            const modal = document.getElementById('previewModal');
            if (event.target === modal) {
                closePreview();
            }
        }
        
        // 폼 유효성 검사
        document.querySelector('.create-link-form').addEventListener('submit', function(e) {
            const linkName = document.getElementById('linkName').value.trim();
            if (!linkName) {
                e.preventDefault();
                alert('링크명을 입력해주세요.');
                document.getElementById('linkName').focus();
                return;
            }
            
            const maxUsage = parseInt(document.getElementById('maxUsage').value);
            if (maxUsage < 1 || maxUsage > 1000) {
                e.preventDefault();
                alert('최대 사용 횟수는 1~1000 사이여야 합니다.');
                document.getElementById('maxUsage').focus();
                return;
            }
        });
    </script>
    
    <script src="${pageContext.request.contextPath}/js/main.js"></script>
</body>
</html>