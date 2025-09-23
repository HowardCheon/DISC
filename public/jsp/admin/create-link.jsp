<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>DISC 검사 링크 생성 - 관리자</title>

    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Bootstrap Icons -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css" rel="stylesheet">
    <!-- Custom CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">

    <style>
        .autocomplete-suggestions {
            border: 1px solid #ddd;
            border-top: none;
            max-height: 200px;
            overflow-y: auto;
            background: white;
            position: absolute;
            z-index: 1000;
            width: 100%;
        }

        .autocomplete-suggestion {
            padding: 8px 12px;
            cursor: pointer;
        }

        .autocomplete-suggestion:hover,
        .autocomplete-suggestion.selected {
            background-color: #f8f9fa;
        }

        .url-input-group {
            position: relative;
        }

        .copy-btn {
            position: absolute;
            right: 5px;
            top: 50%;
            transform: translateY(-50%);
            border: none;
            background: none;
            padding: 5px;
            color: #6c757d;
        }

        .copy-btn:hover {
            color: #007bff;
        }

        .status-badge {
            font-size: 0.75rem;
        }

        .alert-custom {
            border-radius: 8px;
            border: none;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }

        .modal-header {
            border-bottom: 1px solid #dee2e6;
            background: #f8f9fa;
        }

        /* Responsive improvements */
        @media (max-width: 768px) {
            .sidebar {
                display: none !important;
            }

            main {
                margin-left: 0 !important;
            }

            .d-flex.gap-2 {
                flex-direction: column;
                gap: 0.5rem !important;
            }

            .table-responsive {
                font-size: 0.9rem;
            }

            .card {
                margin-bottom: 1rem;
            }

            .btn-custom {
                padding: 8px 16px;
                font-size: 0.9rem;
            }

            .error-container {
                padding: 1.5rem;
                margin: 1rem;
            }

            .url-input-group input {
                font-size: 0.8rem;
            }

            .copy-btn {
                right: 2px;
                padding: 3px;
            }
        }

        @media (max-width: 576px) {
            .container-fluid {
                padding: 0;
            }

            .card-body {
                padding: 1rem;
            }

            .d-flex.justify-content-between {
                flex-direction: column;
                align-items: stretch !important;
            }

            .btn-group {
                width: 100%;
                margin-top: 0.5rem;
            }

            .modal-dialog {
                margin: 0.5rem;
            }

            .table th, .table td {
                padding: 0.5rem;
                font-size: 0.8rem;
            }

            .form-control {
                font-size: 16px; /* Prevent zoom on iOS */
            }
        }

        /* Touch improvements for mobile */
        @media (hover: none) and (pointer: coarse) {
            .btn {
                min-height: 44px; /* iOS accessibility guideline */
            }

            .copy-btn {
                min-width: 44px;
                min-height: 44px;
            }

            .autocomplete-suggestion {
                padding: 12px;
                min-height: 44px;
                display: flex;
                align-items: center;
            }
        }
    </style>
</head>
<body class="bg-light">
    <nav class="navbar navbar-expand-lg navbar-dark bg-primary">
        <div class="container-fluid">
            <a class="navbar-brand" href="${pageContext.request.contextPath}/admin/dashboard">
                <i class="bi bi-graph-up"></i> DISC 관리자
            </a>

            <!-- Mobile menu toggle -->
            <button class="navbar-toggler d-lg-none" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                <span class="navbar-toggler-icon"></span>
            </button>

            <div class="collapse navbar-collapse" id="navbarNav">
                <ul class="navbar-nav me-auto d-lg-none">
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/admin/dashboard">
                            <i class="bi bi-speedometer2"></i> 대시보드
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link active" href="${pageContext.request.contextPath}/admin/create-link">
                            <i class="bi bi-link-45deg"></i> 링크 생성
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/admin/links">
                            <i class="bi bi-list-ul"></i> 링크 관리
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="${pageContext.request.contextPath}/admin/results">
                            <i class="bi bi-graph-up"></i> 결과 관리
                        </a>
                    </li>
                </ul>

                <div class="navbar-nav ms-auto">
                    <div class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle text-white" href="#" role="button" data-bs-toggle="dropdown">
                            <i class="bi bi-person-circle"></i> ${sessionScope.adminUser.username}
                        </a>
                        <ul class="dropdown-menu">
                            <li><a class="dropdown-item" href="${pageContext.request.contextPath}/admin/dashboard">
                                <i class="bi bi-speedometer2"></i> 대시보드
                            </a></li>
                            <li><hr class="dropdown-divider"></li>
                            <li><a class="dropdown-item" href="${pageContext.request.contextPath}/admin/logout">
                                <i class="bi bi-box-arrow-right"></i> 로그아웃
                            </a></li>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    </nav>

    <div class="container-fluid">
        <div class="row">
            <!-- Sidebar -->
            <nav class="col-md-3 col-lg-2 d-md-block bg-white sidebar collapse">
                <div class="position-sticky pt-3">
                    <ul class="nav flex-column">
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/admin/dashboard">
                                <i class="bi bi-speedometer2"></i> 대시보드
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link active" href="${pageContext.request.contextPath}/admin/create-link">
                                <i class="bi bi-link-45deg"></i> 링크 생성
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/admin/links">
                                <i class="bi bi-list-ul"></i> 링크 관리
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="${pageContext.request.contextPath}/admin/results">
                                <i class="bi bi-graph-up"></i> 결과 관리
                            </a>
                        </li>
                    </ul>
                </div>
            </nav>

            <!-- Main content -->
            <main class="col-md-9 ms-sm-auto col-lg-10 px-md-4">
                <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
                    <h1 class="h2"><i class="bi bi-link-45deg"></i> 검사 링크 생성</h1>
                </div>

                <!-- Alert area for notifications -->
                <div id="alertArea"></div>

                <div class="row">
                    <!-- Single Link Creation -->
                    <div class="col-lg-6 mb-4">
                        <div class="card">
                            <div class="card-header bg-primary text-white">
                                <h5 class="card-title mb-0">
                                    <i class="bi bi-person-plus"></i> 개별 링크 생성
                                </h5>
                            </div>
                            <div class="card-body">
                                <form id="singleLinkForm">
                                    <div class="mb-3 position-relative">
                                        <label for="userName" class="form-label">사용자 이름 *</label>
                                        <input type="text" class="form-control" id="userName" name="userName"
                                               autocomplete="off" placeholder="사용자 이름을 입력하세요">
                                        <div id="userSuggestions" class="autocomplete-suggestions"></div>
                                    </div>

                                    <div class="d-grid">
                                        <button type="submit" class="btn btn-primary">
                                            <i class="bi bi-link-45deg"></i> 링크 생성
                                        </button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </div>

                    <!-- Bulk Link Creation -->
                    <div class="col-lg-6 mb-4">
                        <div class="card">
                            <div class="card-header bg-success text-white">
                                <h5 class="card-title mb-0">
                                    <i class="bi bi-people-fill"></i> 일괄 링크 생성
                                </h5>
                            </div>
                            <div class="card-body">
                                <form id="bulkLinkForm">
                                    <div class="mb-3">
                                        <label for="userNames" class="form-label">사용자 이름 목록 *</label>
                                        <textarea class="form-control" id="userNames" name="userNames" rows="8"
                                                  placeholder="사용자 이름을 한 줄에 하나씩 입력하세요&#10;예:&#10;홍길동&#10;김철수&#10;이영희"></textarea>
                                        <div class="form-text">한 번에 최대 100명까지 등록 가능합니다.</div>
                                    </div>

                                    <div class="d-grid">
                                        <button type="submit" class="btn btn-success">
                                            <i class="bi bi-people-fill"></i> 일괄 생성
                                        </button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- History Section -->
                <div class="card">
                    <div class="card-header d-flex justify-content-between align-items-center">
                        <h5 class="card-title mb-0">
                            <i class="bi bi-clock-history"></i> 생성 이력
                        </h5>
                        <div class="d-flex gap-2">
                            <input type="text" class="form-control form-control-sm" id="searchUser"
                                   placeholder="사용자 검색..." style="width: 200px;">
                            <button class="btn btn-sm btn-outline-secondary" onclick="refreshHistory()">
                                <i class="bi bi-arrow-clockwise"></i>
                            </button>
                        </div>
                    </div>
                    <div class="card-body">
                        <div class="table-responsive">
                            <table class="table table-hover">
                                <thead class="table-light">
                                    <tr>
                                        <th>사용자명</th>
                                        <th>검사 회차</th>
                                        <th>생성일시</th>
                                        <th>현재 상태</th>
                                        <th>URL</th>
                                        <th>동작</th>
                                    </tr>
                                </thead>
                                <tbody id="historyTableBody">
                                    <!-- History data will be loaded here -->
                                </tbody>
                            </table>
                        </div>

                        <div id="historyPagination" class="d-flex justify-content-center mt-3">
                            <!-- Pagination will be added here -->
                        </div>
                    </div>
                </div>
            </main>
        </div>
    </div>

    <!-- Success Modal -->
    <div class="modal fade" id="successModal" tabindex="-1">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header bg-success text-white">
                    <h5 class="modal-title">
                        <i class="bi bi-check-circle"></i> 링크 생성 완료
                    </h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <div id="successContent"></div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">닫기</button>
                    <button type="button" class="btn btn-primary" onclick="copySuccessUrl()">
                        <i class="bi bi-clipboard"></i> URL 복사
                    </button>
                </div>
            </div>
        </div>
    </div>

    <!-- Duplicate Confirmation Modal -->
    <div class="modal fade" id="duplicateModal" tabindex="-1">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header bg-warning text-dark">
                    <h5 class="modal-title">
                        <i class="bi bi-exclamation-triangle"></i> 중복 사용자 확인
                    </h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <div id="duplicateContent"></div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">취소</button>
                    <button type="button" class="btn btn-warning" onclick="confirmDuplicate()">
                        <i class="bi bi-arrow-repeat"></i> 재발송
                    </button>
                </div>
            </div>
        </div>
    </div>

    <!-- Bulk Results Modal -->
    <div class="modal fade" id="bulkResultsModal" tabindex="-1">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">
                        <i class="bi bi-list-check"></i> 일괄 생성 결과
                    </h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <div id="bulkResultsContent"></div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">닫기</button>
                </div>
            </div>
        </div>
    </div>

    <!-- Bootstrap 5 JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

    <!-- Recent History Data (from server) -->
    <script>
        const recentHistory = ${recentHistoryJson != null ? recentHistoryJson : '[]'};
        let currentDuplicateUser = null;
        let currentSuccessUrl = null;
    </script>

    <!-- Custom JavaScript -->
    <script src="${pageContext.request.contextPath}/js/create-link.js"></script>
</body>
</html>