<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>DISC 관리자 대시보드</title>

    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Bootstrap Icons -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css" rel="stylesheet">
    <!-- Chart.js -->
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <!-- Custom CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">

    <style>
        .dashboard-container {
            background-color: #f8f9fa;
            min-height: 100vh;
        }

        .navbar-custom {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
        }

        .navbar-brand {
            font-weight: 700;
            font-size: 1.5rem;
        }

        .stats-row {
            margin-bottom: 30px;
        }

        .stat-card {
            background: white;
            border-radius: 15px;
            padding: 25px;
            box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
            border: 1px solid rgba(0, 0, 0, 0.05);
            transition: transform 0.2s ease, box-shadow 0.2s ease;
            height: 100%;
        }

        .stat-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
        }

        .stat-icon {
            width: 60px;
            height: 60px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1.5rem;
            color: white;
            margin-bottom: 15px;
        }

        .stat-icon.primary { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
        .stat-icon.success { background: linear-gradient(135deg, #56ab2f 0%, #a8e6cf 100%); }
        .stat-icon.warning { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
        .stat-icon.info { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }

        .stat-number {
            font-size: 2.5rem;
            font-weight: 700;
            color: #2c3e50;
            margin-bottom: 5px;
        }

        .stat-label {
            color: #6c757d;
            font-weight: 500;
            font-size: 1rem;
        }

        .stat-change {
            font-size: 0.9rem;
            margin-top: 10px;
        }

        .stat-change.positive {
            color: #28a745;
        }

        .stat-change.negative {
            color: #dc3545;
        }

        .chart-card {
            background: white;
            border-radius: 15px;
            padding: 25px;
            box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
            border: 1px solid rgba(0, 0, 0, 0.05);
            margin-bottom: 30px;
        }

        .chart-title {
            font-size: 1.3rem;
            font-weight: 600;
            color: #2c3e50;
            margin-bottom: 20px;
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .chart-container {
            position: relative;
            height: 300px;
        }

        .chart-container.small {
            height: 250px;
        }

        .activity-feed {
            max-height: 400px;
            overflow-y: auto;
        }

        .activity-item {
            display: flex;
            align-items: center;
            padding: 15px;
            border-bottom: 1px solid #f1f3f4;
            transition: background-color 0.2s ease;
        }

        .activity-item:hover {
            background-color: #f8f9fa;
        }

        .activity-item:last-child {
            border-bottom: none;
        }

        .activity-icon {
            width: 40px;
            height: 40px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            margin-right: 15px;
            font-size: 1rem;
        }

        .activity-icon.success { background-color: #d4edda; color: #155724; }
        .activity-icon.info { background-color: #cce7ff; color: #0056b3; }
        .activity-icon.warning { background-color: #fff3cd; color: #856404; }

        .activity-content {
            flex-grow: 1;
        }

        .activity-message {
            font-weight: 500;
            color: #2c3e50;
            margin-bottom: 2px;
        }

        .activity-time {
            font-size: 0.85rem;
            color: #6c757d;
        }

        .refresh-btn {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border: none;
            color: white;
            padding: 8px 16px;
            border-radius: 8px;
            font-weight: 500;
            transition: all 0.2s ease;
        }

        .refresh-btn:hover {
            transform: translateY(-1px);
            box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
            color: white;
        }

        .quick-stats {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border-radius: 15px;
            padding: 25px;
            margin-bottom: 30px;
        }

        .quick-stats h4 {
            margin-bottom: 20px;
            font-weight: 600;
        }

        .quick-stat-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 8px 0;
            border-bottom: 1px solid rgba(255, 255, 255, 0.2);
        }

        .quick-stat-item:last-child {
            border-bottom: none;
        }

        .percentage-badge {
            background: rgba(255, 255, 255, 0.2);
            padding: 4px 12px;
            border-radius: 20px;
            font-weight: 600;
        }

        @media (max-width: 768px) {
            .stat-card {
                margin-bottom: 20px;
            }

            .chart-container {
                height: 250px;
            }
        }
    </style>
</head>
<body>
    <div class="dashboard-container">
        <!-- Navigation -->
        <nav class="navbar navbar-expand-lg navbar-custom">
            <div class="container-fluid">
                <a class="navbar-brand text-white" href="#">
                    <i class="bi bi-speedometer2 me-2"></i>
                    DISC 관리자 대시보드
                </a>
                <div class="navbar-nav ms-auto">
                    <div class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle text-white" href="#" role="button" data-bs-toggle="dropdown">
                            <i class="bi bi-person-circle me-1"></i>
                            ${adminUser.username}
                        </a>
                        <ul class="dropdown-menu">
                            <li><a class="dropdown-item" href="${pageContext.request.contextPath}/admin/login?action=logout">
                                <i class="bi bi-box-arrow-right me-2"></i>로그아웃
                            </a></li>
                        </ul>
                    </div>
                </div>
            </div>
        </nav>

        <div class="container-fluid py-4">
            <!-- Error Message -->
            <c:if test="${not empty errorMessage}">
                <div class="alert alert-danger alert-dismissible fade show" role="alert">
                    <i class="bi bi-exclamation-triangle-fill me-2"></i>
                    ${errorMessage}
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
            </c:if>

            <!-- Quick Statistics -->
            <div class="quick-stats">
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <h4><i class="bi bi-calendar-today me-2"></i>오늘의 통계</h4>
                    <button type="button" class="refresh-btn" onclick="refreshDashboard()">
                        <i class="bi bi-arrow-clockwise me-1"></i>새로고침
                    </button>
                </div>
                <div class="row">
                    <div class="col-md-3">
                        <div class="quick-stat-item">
                            <span>발송된 링크</span>
                            <span class="percentage-badge">${dashboardStats.today.linksCreated}개</span>
                        </div>
                    </div>
                    <div class="col-md-3">
                        <div class="quick-stat-item">
                            <span>완료된 검사</span>
                            <span class="percentage-badge">${dashboardStats.today.testsCompleted}개</span>
                        </div>
                    </div>
                    <div class="col-md-3">
                        <div class="quick-stat-item">
                            <span>완료율</span>
                            <span class="percentage-badge">${dashboardStats.today.completionRate}%</span>
                        </div>
                    </div>
                    <div class="col-md-3">
                        <div class="quick-stat-item">
                            <span>신규 사용자</span>
                            <span class="percentage-badge">${dashboardStats.today.newUsers}명</span>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Main Statistics Cards -->
            <div class="row stats-row">
                <div class="col-xl-3 col-md-6">
                    <div class="stat-card">
                        <div class="stat-icon primary">
                            <i class="bi bi-link-45deg"></i>
                        </div>
                        <div class="stat-number">${dashboardStats.totalTestLinks}</div>
                        <div class="stat-label">총 테스트 링크</div>
                        <div class="stat-change positive">
                            <i class="bi bi-arrow-up"></i>
                            이번 주 +${dashboardStats.thisWeek.linksCreated}개
                        </div>
                    </div>
                </div>

                <div class="col-xl-3 col-md-6">
                    <div class="stat-card">
                        <div class="stat-icon success">
                            <i class="bi bi-check-circle"></i>
                        </div>
                        <div class="stat-number">${dashboardStats.totalCompletedTests}</div>
                        <div class="stat-label">완료된 검사</div>
                        <div class="stat-change positive">
                            <i class="bi bi-arrow-up"></i>
                            이번 주 +${dashboardStats.thisWeek.testsCompleted}개
                        </div>
                    </div>
                </div>

                <div class="col-xl-3 col-md-6">
                    <div class="stat-card">
                        <div class="stat-icon warning">
                            <i class="bi bi-percent"></i>
                        </div>
                        <div class="stat-number">${dashboardStats.overallCompletionRate}%</div>
                        <div class="stat-label">전체 완료율</div>
                        <div class="stat-change positive">
                            <i class="bi bi-arrow-up"></i>
                            이번 주 ${dashboardStats.thisWeek.completionRate}%
                        </div>
                    </div>
                </div>

                <div class="col-xl-3 col-md-6">
                    <div class="stat-card">
                        <div class="stat-icon info">
                            <i class="bi bi-people"></i>
                        </div>
                        <div class="stat-number">${dashboardStats.totalUsers}</div>
                        <div class="stat-label">총 사용자</div>
                        <div class="stat-change positive">
                            <i class="bi bi-arrow-up"></i>
                            이번 주 +${dashboardStats.thisWeek.newUsers}명
                        </div>
                    </div>
                </div>
            </div>

            <!-- Charts Row -->
            <div class="row">
                <!-- Completion Trend Chart -->
                <div class="col-lg-8">
                    <div class="chart-card">
                        <div class="chart-title">
                            <i class="bi bi-graph-up text-primary"></i>
                            완료율 추이 (최근 7일)
                        </div>
                        <div class="chart-container">
                            <canvas id="completionTrendChart"></canvas>
                        </div>
                    </div>
                </div>

                <!-- DISC Type Distribution -->
                <div class="col-lg-4">
                    <div class="chart-card">
                        <div class="chart-title">
                            <i class="bi bi-pie-chart text-success"></i>
                            DISC 유형 분포
                        </div>
                        <div class="chart-container small">
                            <canvas id="discTypeChart"></canvas>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Activity Feed -->
            <div class="row">
                <div class="col-12">
                    <div class="chart-card">
                        <div class="chart-title">
                            <i class="bi bi-activity text-info"></i>
                            최근 활동
                        </div>
                        <div class="activity-feed">
                            <c:choose>
                                <c:when test="${not empty recentActivity}">
                                    <c:forEach items="${recentActivity}" var="activity">
                                        <div class="activity-item">
                                            <div class="activity-icon ${activity.color}">
                                                <i class="bi bi-${activity.icon}"></i>
                                            </div>
                                            <div class="activity-content">
                                                <div class="activity-message">${activity.message}</div>
                                                <div class="activity-time">${activity.time}</div>
                                            </div>
                                        </div>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <div class="text-center py-4 text-muted">
                                        <i class="bi bi-inbox display-4"></i>
                                        <p class="mt-2">최근 활동이 없습니다.</p>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

    <!-- Chart Data -->
    <script>
        // Data from server
        window.completionTrendData = ${completionTrendJson};
        window.discTypeData = ${discTypeDistributionJson};

        // Initialize charts when page loads
        document.addEventListener('DOMContentLoaded', function() {
            initializeCharts();
        });

        function initializeCharts() {
            // Completion Trend Chart
            const completionCtx = document.getElementById('completionTrendChart').getContext('2d');
            const labels = window.completionTrendData.map(item => item.date);
            const completionData = window.completionTrendData.map(item => item.completionRate);
            const linksData = window.completionTrendData.map(item => item.linksCreated);
            const completedData = window.completionTrendData.map(item => item.testsCompleted);

            new Chart(completionCtx, {
                type: 'line',
                data: {
                    labels: labels,
                    datasets: [{
                        label: '완료율 (%)',
                        data: completionData,
                        borderColor: 'rgb(102, 126, 234)',
                        backgroundColor: 'rgba(102, 126, 234, 0.1)',
                        tension: 0.4,
                        fill: true,
                        yAxisID: 'y'
                    }, {
                        label: '발송 링크',
                        data: linksData,
                        borderColor: 'rgb(54, 162, 235)',
                        backgroundColor: 'rgba(54, 162, 235, 0.1)',
                        tension: 0.4,
                        yAxisID: 'y1'
                    }, {
                        label: '완료 검사',
                        data: completedData,
                        borderColor: 'rgb(34, 197, 94)',
                        backgroundColor: 'rgba(34, 197, 94, 0.1)',
                        tension: 0.4,
                        yAxisID: 'y1'
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: {
                            position: 'top',
                        }
                    },
                    scales: {
                        y: {
                            type: 'linear',
                            display: true,
                            position: 'left',
                            title: {
                                display: true,
                                text: '완료율 (%)'
                            }
                        },
                        y1: {
                            type: 'linear',
                            display: true,
                            position: 'right',
                            title: {
                                display: true,
                                text: '검사 수'
                            },
                            grid: {
                                drawOnChartArea: false,
                            },
                        }
                    }
                }
            });

            // DISC Type Distribution Chart
            const discCtx = document.getElementById('discTypeChart').getContext('2d');
            const discLabels = ['D (주도형)', 'I (사교형)', 'S (안정형)', 'C (신중형)'];
            const discValues = [
                window.discTypeData.D || 0,
                window.discTypeData.I || 0,
                window.discTypeData.S || 0,
                window.discTypeData.C || 0
            ];

            new Chart(discCtx, {
                type: 'doughnut',
                data: {
                    labels: discLabels,
                    datasets: [{
                        data: discValues,
                        backgroundColor: [
                            '#e74c3c',
                            '#f39c12',
                            '#27ae60',
                            '#3498db'
                        ],
                        borderWidth: 3,
                        borderColor: '#fff'
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: {
                            position: 'bottom',
                            labels: {
                                padding: 20,
                                usePointStyle: true
                            }
                        }
                    }
                }
            });
        }

        // Refresh dashboard function
        function refreshDashboard() {
            const refreshBtn = document.querySelector('.refresh-btn');
            const originalText = refreshBtn.innerHTML;

            refreshBtn.innerHTML = '<i class="bi bi-arrow-clockwise me-1"></i>새로고침 중...';
            refreshBtn.disabled = true;

            fetch('${pageContext.request.contextPath}/admin/dashboard?action=refreshStats', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'X-Requested-With': 'XMLHttpRequest'
                },
                body: 'action=refreshStats'
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    // Update UI with new data
                    location.reload();
                } else {
                    console.error('Failed to refresh dashboard:', data.message);
                }
            })
            .catch(error => {
                console.error('Error refreshing dashboard:', error);
            })
            .finally(() => {
                refreshBtn.innerHTML = originalText;
                refreshBtn.disabled = false;
            });
        }
    </script>
</body>
</html>