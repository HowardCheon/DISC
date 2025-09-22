<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>DISC 성격검사 - 검사 진행</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .test-container {
            max-width: 900px;
            margin: 0 auto;
            padding: 20px;
        }
        
        .test-header {
            text-align: center;
            margin-bottom: 40px;
            background: white;
            padding: 30px;
            border-radius: 12px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
        }
        
        .test-header h1 {
            color: #2c3e50;
            margin-bottom: 15px;
        }
        
        .user-info {
            color: #7f8c8d;
            margin-bottom: 20px;
            font-size: 1.1em;
        }
        
        .progress-container {
            margin-bottom: 20px;
        }
        
        .progress-info {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 10px;
        }
        
        .progress-text {
            font-weight: 600;
            color: #2c3e50;
        }
        
        .page-info {
            font-size: 0.9em;
            color: #7f8c8d;
        }
        
        .progress-bar {
            width: 100%;
            height: 12px;
            background: #e1e8ed;
            border-radius: 6px;
            overflow: hidden;
            margin-bottom: 10px;
        }
        
        .progress-fill {
            height: 100%;
            background: linear-gradient(90deg, #3498db, #2980b9);
            transition: width 0.5s ease;
            border-radius: 6px;
        }
        
        .questions-container {
            background: white;
            padding: 40px;
            border-radius: 12px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
            margin-bottom: 30px;
        }
        
        .question-item {
            margin-bottom: 40px;
            padding-bottom: 30px;
            border-bottom: 1px solid #e1e8ed;
        }
        
        .question-item:last-child {
            margin-bottom: 0;
            padding-bottom: 0;
            border-bottom: none;
        }
        
        .question-header {
            margin-bottom: 20px;
        }
        
        .question-number {
            display: inline-block;
            background: #3498db;
            color: white;
            padding: 8px 16px;
            border-radius: 20px;
            font-weight: 600;
            margin-bottom: 15px;
            font-size: 0.9em;
        }
        
        .question-text {
            font-size: 1.1em;
            color: #2c3e50;
            line-height: 1.6;
            margin-bottom: 25px;
        }
        
        .options-container {
            display: grid;
            gap: 20px;
        }
        
        .option-group {
            background: #f8f9fa;
            padding: 20px;
            border-radius: 8px;
            border: 2px solid transparent;
            transition: all 0.3s ease;
        }
        
        .option-group.has-selection {
            border-color: #3498db;
            background: #f0f8ff;
        }
        
        .option-group-title {
            font-weight: 600;
            color: #2c3e50;
            margin-bottom: 15px;
            font-size: 1em;
        }
        
        .option-list {
            display: grid;
            gap: 12px;
        }
        
        .option-item {
            display: flex;
            align-items: center;
            padding: 12px 15px;
            background: white;
            border: 2px solid #e1e8ed;
            border-radius: 8px;
            cursor: pointer;
            transition: all 0.3s ease;
        }
        
        .option-item:hover {
            border-color: #3498db;
            background: #f8fafe;
        }
        
        .option-item.selected {
            border-color: #3498db;
            background: #e3f2fd;
        }
        
        .option-item input[type="radio"] {
            margin-right: 12px;
            transform: scale(1.2);
        }
        
        .option-text {
            flex: 1;
            color: #2c3e50;
            font-size: 1em;
            line-height: 1.4;
        }
        
        .validation-message {
            color: #e74c3c;
            font-size: 0.9em;
            margin-top: 10px;
            padding: 10px;
            background: #ffeaa7;
            border-radius: 6px;
            display: none;
        }
        
        .validation-message.show {
            display: block;
        }
        
        .navigation-container {
            background: white;
            padding: 30px;
            border-radius: 12px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
        }
        
        .navigation-buttons {
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 20px;
        }
        
        .nav-button {
            padding: 15px 30px;
            border: none;
            border-radius: 8px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s ease;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 8px;
        }
        
        .btn-prev {
            background: #95a5a6;
            color: white;
        }
        
        .btn-prev:hover:not(:disabled) {
            background: #7f8c8d;
            transform: translateY(-2px);
        }
        
        .btn-next {
            background: linear-gradient(135deg, #3498db, #2980b9);
            color: white;
        }
        
        .btn-next:hover:not(:disabled) {
            background: linear-gradient(135deg, #2980b9, #1f5582);
            transform: translateY(-2px);
        }
        
        .btn-submit {
            background: linear-gradient(135deg, #27ae60, #2ecc71);
            color: white;
        }
        
        .btn-submit:hover:not(:disabled) {
            background: linear-gradient(135deg, #229954, #27ae60);
            transform: translateY(-2px);
        }
        
        .nav-button:disabled {
            opacity: 0.6;
            cursor: not-allowed;
            transform: none !important;
        }
        
        .save-status {
            text-align: center;
            color: #7f8c8d;
            font-size: 0.9em;
        }
        
        .save-status.saved {
            color: #27ae60;
        }
        
        .save-status.error {
            color: #e74c3c;
        }
        
        .page-summary {
            background: #f8f9fa;
            padding: 20px;
            border-radius: 8px;
            margin-bottom: 20px;
            border-left: 4px solid #3498db;
        }
        
        .page-summary h3 {
            margin: 0 0 10px 0;
            color: #2c3e50;
        }
        
        .completion-stats {
            display: flex;
            gap: 20px;
            flex-wrap: wrap;
        }
        
        .stat-item {
            background: white;
            padding: 15px;
            border-radius: 6px;
            border: 1px solid #e1e8ed;
            flex: 1;
            min-width: 120px;
        }
        
        .stat-value {
            font-size: 1.5em;
            font-weight: bold;
            color: #3498db;
        }
        
        .stat-label {
            font-size: 0.9em;
            color: #7f8c8d;
            margin-top: 5px;
        }
        
        /* Responsive Design */
        @media (max-width: 768px) {
            .test-container {
                padding: 10px;
            }
            
            .test-header,
            .questions-container,
            .navigation-container {
                padding: 20px;
            }
            
            .navigation-buttons {
                flex-direction: column;
                gap: 15px;
            }
            
            .nav-button {
                width: 100%;
                justify-content: center;
            }
            
            .completion-stats {
                flex-direction: column;
            }
            
            .question-item {
                margin-bottom: 30px;
                padding-bottom: 20px;
            }
        }
    </style>
</head>
<body>
    <div class="test-container">
        <!-- Test Header -->
        <div class="test-header">
            <h1>DISC 성격검사</h1>
            <div class="user-info">
                <strong>${userName}</strong>님의 검사가 진행 중입니다
            </div>
            
            <!-- Progress Information -->
            <div class="progress-container">
                <div class="progress-info">
                    <span class="progress-text">진행률: ${progressPercentage}%</span>
                    <span class="page-info">페이지 ${currentPage} / ${totalPages}</span>
                </div>
                <div class="progress-bar">
                    <div class="progress-fill" style="width: ${progressPercentage}%"></div>
                </div>
                <div class="page-info">
                    문항 ${startQuestionNumber} - ${endQuestionNumber} / 28
                </div>
            </div>
        </div>
        
        <!-- Page Summary -->
        <div class="page-summary">
            <h3>현재 페이지 안내</h3>
            <p>각 문항에서 <strong>"가장 나와 같은 것"</strong>과 <strong>"가장 나와 다른 것"</strong>을 하나씩 선택해주세요.</p>
            <div class="completion-stats">
                <div class="stat-item">
                    <div class="stat-value">${currentPage}</div>
                    <div class="stat-label">현재 페이지</div>
                </div>
                <div class="stat-item">
                    <div class="stat-value">${fn:length(questions)}</div>
                    <div class="stat-label">이번 페이지 문항</div>
                </div>
                <div class="stat-item">
                    <div class="stat-value">${answeredCount}</div>
                    <div class="stat-label">완료된 문항</div>
                </div>
            </div>
        </div>
        
        <!-- Questions Container -->
        <div class="questions-container">
            <form id="testForm" method="post">
                <input type="hidden" name="name" value="${param.name}">
                <input type="hidden" name="token" value="${param.token}">
                <input type="hidden" name="page" value="${currentPage}">
                
                <c:forEach var="question" items="${questions}" varStatus="status">
                    <div class="question-item" data-question-id="${question.number}">
                        <div class="question-header">
                            <div class="question-number">문항 ${question.number}</div>
                        </div>
                        
                        <!-- Most Like Options -->
                        <div class="option-group" data-type="mostLike" data-question="${question.number}">
                            <div class="option-group-title">🏆 가장 나와 같은 것을 선택하세요</div>
                            <div class="option-list">
                                <div class="option-item" data-type="D">
                                    <input type="radio" name="mostLike_${question.number}" value="D" id="most_${question.number}_D">
                                    <label for="most_${question.number}_D" class="option-text">${question.DOption}</label>
                                </div>
                                <div class="option-item" data-type="I">
                                    <input type="radio" name="mostLike_${question.number}" value="I" id="most_${question.number}_I">
                                    <label for="most_${question.number}_I" class="option-text">${question.IOption}</label>
                                </div>
                                <div class="option-item" data-type="S">
                                    <input type="radio" name="mostLike_${question.number}" value="S" id="most_${question.number}_S">
                                    <label for="most_${question.number}_S" class="option-text">${question.SOption}</label>
                                </div>
                                <div class="option-item" data-type="C">
                                    <input type="radio" name="mostLike_${question.number}" value="C" id="most_${question.number}_C">
                                    <label for="most_${question.number}_C" class="option-text">${question.COption}</label>
                                </div>
                            </div>
                            <div class="validation-message" id="mostLike_${question.number}_error">
                                가장 나와 같은 것을 선택해주세요.
                            </div>
                        </div>
                        
                        <!-- Least Like Options -->
                        <div class="option-group" data-type="leastLike" data-question="${question.number}">
                            <div class="option-group-title">❌ 가장 나와 다른 것을 선택하세요</div>
                            <div class="option-list">
                                <div class="option-item" data-type="D">
                                    <input type="radio" name="leastLike_${question.number}" value="D" id="least_${question.number}_D">
                                    <label for="least_${question.number}_D" class="option-text">${question.DOption}</label>
                                </div>
                                <div class="option-item" data-type="I">
                                    <input type="radio" name="leastLike_${question.number}" value="I" id="least_${question.number}_I">
                                    <label for="least_${question.number}_I" class="option-text">${question.IOption}</label>
                                </div>
                                <div class="option-item" data-type="S">
                                    <input type="radio" name="leastLike_${question.number}" value="S" id="least_${question.number}_S">
                                    <label for="least_${question.number}_S" class="option-text">${question.SOption}</label>
                                </div>
                                <div class="option-item" data-type="C">
                                    <input type="radio" name="leastLike_${question.number}" value="C" id="least_${question.number}_C">
                                    <label for="least_${question.number}_C" class="option-text">${question.COption}</label>
                                </div>
                            </div>
                            <div class="validation-message" id="leastLike_${question.number}_error">
                                가장 나와 다른 것을 선택해주세요.
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </form>
        </div>
        
        <!-- Navigation -->
        <div class="navigation-container">
            <div class="save-status" id="saveStatus">
                답변이 자동으로 저장됩니다
            </div>
            
            <div class="navigation-buttons">
                <c:choose>
                    <c:when test="${currentPage > 1}">
                        <a href="?name=${param.name}&token=${param.token}&page=${currentPage - 1}" 
                           class="nav-button btn-prev" id="prevBtn">
                            ← 이전 페이지
                        </a>
                    </c:when>
                    <c:otherwise>
                        <div></div>
                    </c:otherwise>
                </c:choose>
                
                <c:choose>
                    <c:when test="${isLastPage}">
                        <button type="button" class="nav-button btn-submit" id="submitBtn">
                            검사 완료 및 결과 보기 →
                        </button>
                    </c:when>
                    <c:otherwise>
                        <button type="button" class="nav-button btn-next" id="nextBtn">
                            다음 페이지 →
                        </button>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
    
    <!-- Hidden data for JavaScript -->
    <script type="text/javascript">
        window.testData = {
            userName: '${userName}',
            testToken: '${param.token}',
            currentPage: ${currentPage},
            totalPages: ${totalPages},
            isLastPage: ${isLastPage},
            questionsPerPage: ${questionsPerPage},
            totalQuestions: ${totalQuestions},
            contextPath: '${pageContext.request.contextPath}',
            existingAnswers: ${existingAnswersJson}
        };
    </script>
    
    <script src="${pageContext.request.contextPath}/js/test.js"></script>
</body>
</html>