<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>DISC 검사 관리 - 사용자 검사 이력</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <style>
        .sidebar {
            min-height: 100vh;
            background-color: #f8f9fa;
        }
        .search-box {
            background: white;
            border-radius: 10px;
            padding: 20px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            margin-bottom: 20px;
        }
        .history-card {
            background: white;
            border-radius: 10px;
            padding: 20px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            margin-bottom: 20px;
        }
        .test-item {
            border: 1px solid #e9ecef;
            border-radius: 8px;
            padding: 15px;
            margin-bottom: 15px;
            transition: all 0.3s ease;
        }
        .test-item:hover {
            box-shadow: 0 4px 15px rgba(0,0,0,0.1);
            transform: translateY(-2px);
        }
        .status-badge {
            font-size: 0.8em;
            padding: 4px 8px;
        }
        .chart-container {
            background: white;
            border-radius: 10px;
            padding: 20px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            margin-bottom: 20px;
        }
        .user-info {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border-radius: 10px;
            padding: 20px;
            margin-bottom: 20px;
        }
        .comparison-section {
            display: none;
        }
        .selected-test {
            border: 2px solid #007bff;
            background-color: #f8f9ff;
        }
    </style>
</head>
<body>
    <div class="container-fluid">
        <div class="row">
            <!-- 사이드바 -->
            <nav class="col-md-2 d-md-block sidebar collapse">
                <div class="position-sticky pt-3">
                    <h4 class="text-center mb-4">DISC 관리</h4>
                    <ul class="nav flex-column">
                        <li class="nav-item">
                            <a class="nav-link" href="dashboard.jsp">
                                <i class="fas fa-tachometer-alt"></i> 대시보드
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="survey-send.jsp">
                                <i class="fas fa-paper-plane"></i> 검사 발송
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="survey-list.jsp">
                                <i class="fas fa-list"></i> 검사 목록
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link active" href="user-history.jsp">
                                <i class="fas fa-history"></i> 사용자 이력
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="results-analysis.jsp">
                                <i class="fas fa-chart-bar"></i> 결과 분석
                            </a>
                        </li>
                    </ul>
                </div>
            </nav>

            <!-- 메인 콘텐츠 -->
            <main class="col-md-10 ms-sm-auto px-md-4">
                <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
                    <h1 class="h2">사용자 검사 이력</h1>
                </div>

                <!-- 사용자 검색 -->
                <div class="search-box">
                    <h5><i class="fas fa-search"></i> 사용자 검색</h5>
                    <div class="row">
                        <div class="col-md-4">
                            <input type="text" class="form-control" id="userSearch" placeholder="이름 또는 이메일 입력">
                        </div>
                        <div class="col-md-3">
                            <button type="button" class="btn btn-primary" onclick="searchUsers()">
                                <i class="fas fa-search"></i> 검색
                            </button>
                        </div>
                    </div>

                    <!-- 검색 결과 -->
                    <div id="searchResults" class="mt-3" style="display: none;">
                        <h6>검색 결과:</h6>
                        <div id="userList" class="list-group">
                        </div>
                    </div>
                </div>

                <!-- 선택된 사용자 정보 -->
                <div id="selectedUserSection" style="display: none;">
                    <div class="user-info">
                        <div class="row">
                            <div class="col-md-8">
                                <h4 id="selectedUserName"></h4>
                                <p class="mb-1" id="selectedUserEmail"></p>
                                <p class="mb-0">총 검사 횟수: <span id="totalTests">0</span>회</p>
                            </div>
                            <div class="col-md-4 text-end">
                                <button class="btn btn-light" onclick="toggleComparison()">
                                    <i class="fas fa-chart-line"></i> 결과 비교
                                </button>
                            </div>
                        </div>
                    </div>

                    <!-- 점수 변화 추이 차트 -->
                    <div id="comparisonSection" class="comparison-section">
                        <div class="chart-container">
                            <h5><i class="fas fa-chart-line"></i> 점수 변화 추이</h5>
                            <canvas id="scoreChart" height="100"></canvas>
                        </div>
                    </div>

                    <!-- 검사 이력 목록 -->
                    <div class="history-card">
                        <div class="d-flex justify-content-between align-items-center mb-3">
                            <h5><i class="fas fa-history"></i> 검사 이력</h5>
                            <div class="btn-group" role="group">
                                <button type="button" class="btn btn-outline-primary btn-sm" onclick="sortHistory('date')">
                                    <i class="fas fa-sort-amount-down"></i> 날짜순
                                </button>
                                <button type="button" class="btn btn-outline-primary btn-sm" onclick="sortHistory('status')">
                                    <i class="fas fa-filter"></i> 상태별
                                </button>
                            </div>
                        </div>
                        <div id="testHistory">
                        </div>
                    </div>
                </div>

                <!-- 빈 상태 -->
                <div id="emptyState" class="text-center mt-5">
                    <i class="fas fa-user-search fa-3x text-muted mb-3"></i>
                    <h5 class="text-muted">사용자를 검색하여 검사 이력을 확인하세요</h5>
                </div>
            </main>
        </div>
    </div>

    <!-- 검사 결과 모달 -->
    <div class="modal fade" id="resultModal" tabindex="-1">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">검사 결과</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body" id="resultContent">
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">닫기</button>
                    <button type="button" class="btn btn-primary" onclick="downloadResult()">
                        <i class="fas fa-download"></i> PDF 다운로드
                    </button>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="../js/user-history.js"></script>
</body>
</html>