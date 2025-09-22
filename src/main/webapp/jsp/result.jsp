<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>DISC 성격검사 결과 - ${userName}님</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/chartjs-plugin-datalabels@2"></script>
    <style>
        .result-container {
            max-width: 1200px;
            margin: 0 auto;
            padding: 20px;
            background-color: #f8f9fa;
            min-height: 100vh;
        }

        .result-header {
            text-align: center;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 40px 30px;
            border-radius: 15px;
            margin-bottom: 30px;
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
        }

        .result-header h1 {
            font-size: 2.5em;
            margin-bottom: 10px;
            font-weight: 700;
        }

        .user-name {
            font-size: 1.3em;
            opacity: 0.9;
            margin-bottom: 15px;
        }

        .primary-type {
            display: inline-block;
            background: rgba(255, 255, 255, 0.2);
            padding: 15px 30px;
            border-radius: 50px;
            font-size: 1.5em;
            font-weight: 600;
            margin-top: 10px;
        }

        .charts-section {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 30px;
            margin-bottom: 40px;
        }

        .chart-container {
            background: white;
            padding: 30px;
            border-radius: 15px;
            box-shadow: 0 5px 20px rgba(0, 0, 0, 0.08);
            position: relative;
        }

        .chart-title {
            text-align: center;
            font-size: 1.4em;
            font-weight: 600;
            color: #2c3e50;
            margin-bottom: 25px;
            padding-bottom: 10px;
            border-bottom: 2px solid #ecf0f1;
        }

        .radar-chart-container {
            position: relative;
            height: 350px;
        }

        .bar-chart-container {
            position: relative;
            height: 350px;
        }

        .score-bars {
            margin-top: 20px;
        }

        .score-bar {
            display: flex;
            align-items: center;
            margin-bottom: 15px;
            padding: 12px;
            background: #f8f9fa;
            border-radius: 10px;
            transition: transform 0.2s ease;
        }

        .score-bar:hover {
            transform: translateX(5px);
        }

        .score-label {
            width: 100px;
            font-weight: 600;
            color: #2c3e50;
        }

        .score-value {
            min-width: 50px;
            text-align: right;
            font-weight: 700;
            font-size: 1.1em;
        }

        .score-progress {
            flex: 1;
            height: 20px;
            background: #ecf0f1;
            border-radius: 10px;
            margin: 0 15px;
            overflow: hidden;
            position: relative;
        }

        .score-fill {
            height: 100%;
            border-radius: 10px;
            transition: width 1s ease-in-out;
            position: relative;
        }

        .score-fill.d-type { background: linear-gradient(90deg, #e74c3c, #c0392b); }
        .score-fill.i-type { background: linear-gradient(90deg, #f39c12, #e67e22); }
        .score-fill.s-type { background: linear-gradient(90deg, #27ae60, #229954); }
        .score-fill.c-type { background: linear-gradient(90deg, #3498db, #2980b9); }

        .type-description {
            background: white;
            border-radius: 15px;
            padding: 30px;
            margin-bottom: 30px;
            box-shadow: 0 5px 20px rgba(0, 0, 0, 0.08);
        }

        .type-header {
            display: flex;
            align-items: center;
            margin-bottom: 25px;
            padding-bottom: 15px;
            border-bottom: 3px solid #ecf0f1;
        }

        .type-emoji {
            font-size: 3em;
            margin-right: 20px;
        }

        .type-info h2 {
            color: #2c3e50;
            font-size: 2em;
            margin-bottom: 5px;
        }

        .type-subtitle {
            color: #7f8c8d;
            font-size: 1.2em;
            font-style: italic;
        }

        .characteristics {
            font-size: 1.1em;
            line-height: 1.8;
            color: #34495e;
            margin-bottom: 30px;
            text-align: justify;
        }

        .details-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 30px;
            margin-bottom: 30px;
        }

        .detail-section {
            background: #f8f9fa;
            padding: 25px;
            border-radius: 12px;
            border-left: 5px solid;
        }

        .detail-section.strengths { border-left-color: #27ae60; }
        .detail-section.development { border-left-color: #f39c12; }
        .detail-section.communication { border-left-color: #3498db; }
        .detail-section.work-environment { border-left-color: #9b59b6; }

        .detail-section h3 {
            color: #2c3e50;
            font-size: 1.3em;
            margin-bottom: 15px;
            display: flex;
            align-items: center;
        }

        .detail-section h3::before {
            content: "✦";
            margin-right: 10px;
            font-size: 1.2em;
        }

        .detail-section.strengths h3::before { color: #27ae60; }
        .detail-section.development h3::before { color: #f39c12; }
        .detail-section.communication h3::before { color: #3498db; }
        .detail-section.work-environment h3::before { color: #9b59b6; }

        .detail-list {
            list-style: none;
            padding: 0;
        }

        .detail-list li {
            padding: 8px 0;
            border-bottom: 1px solid #ecf0f1;
            position: relative;
            padding-left: 20px;
        }

        .detail-list li:last-child {
            border-bottom: none;
        }

        .detail-list li::before {
            content: "•";
            position: absolute;
            left: 0;
            color: #3498db;
            font-weight: bold;
        }

        .career-section {
            background: white;
            border-radius: 15px;
            padding: 30px;
            margin-bottom: 30px;
            box-shadow: 0 5px 20px rgba(0, 0, 0, 0.08);
        }

        .career-section h3 {
            color: #2c3e50;
            font-size: 1.5em;
            margin-bottom: 20px;
            text-align: center;
        }

        .career-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 15px;
        }

        .career-item {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 15px 20px;
            border-radius: 10px;
            text-align: center;
            font-weight: 500;
            transition: transform 0.2s ease;
        }

        .career-item:hover {
            transform: translateY(-3px);
        }

        .action-buttons {
            text-align: center;
            margin-top: 40px;
            padding: 30px;
            background: white;
            border-radius: 15px;
            box-shadow: 0 5px 20px rgba(0, 0, 0, 0.08);
        }

        .btn-print {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            padding: 15px 40px;
            border-radius: 50px;
            font-size: 1.1em;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s ease;
            margin: 0 10px;
        }

        .btn-print:hover {
            transform: translateY(-2px);
            box-shadow: 0 10px 25px rgba(102, 126, 234, 0.3);
        }

        .btn-secondary {
            background: #95a5a6;
        }

        .btn-secondary:hover {
            background: #7f8c8d;
            box-shadow: 0 10px 25px rgba(149, 165, 166, 0.3);
        }

        @media print {
            body { background: white !important; }
            .result-container { box-shadow: none !important; }
            .action-buttons { display: none !important; }
            .chart-container { break-inside: avoid; }
            .type-description { break-inside: avoid; }
        }

        @media (max-width: 768px) {
            .charts-section {
                grid-template-columns: 1fr;
            }

            .details-grid {
                grid-template-columns: 1fr;
            }

            .result-header h1 {
                font-size: 2em;
            }

            .type-header {
                flex-direction: column;
                text-align: center;
            }

            .type-emoji {
                margin-bottom: 15px;
            }
        }

        .loading {
            text-align: center;
            padding: 50px;
            color: #7f8c8d;
        }

        .fade-in {
            animation: fadeIn 0.8s ease-in;
        }

        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(20px); }
            to { opacity: 1; transform: translateY(0); }
        }
    </style>
</head>
<body>
    <div class="result-container fade-in">
        <!-- Header Section -->
        <div class="result-header">
            <h1>🎯 DISC 성격검사 결과</h1>
            <p class="user-name">${userName}님의 성격 유형 분석 결과입니다</p>
            <div class="primary-type">
                ${typeDescription.emoji} ${typeDescription.name}
            </div>
        </div>

        <!-- Charts Section -->
        <div class="charts-section">
            <!-- Radar Chart -->
            <div class="chart-container">
                <h3 class="chart-title">🕸️ 성격 유형 레이더 차트</h3>
                <div class="radar-chart-container">
                    <canvas id="radarChart"></canvas>
                </div>
            </div>

            <!-- Bar Chart -->
            <div class="chart-container">
                <h3 class="chart-title">📊 유형별 점수 분포</h3>
                <div class="bar-chart-container">
                    <canvas id="barChart"></canvas>
                </div>
                <div class="score-bars">
                    <div class="score-bar">
                        <div class="score-label">D (주도형)</div>
                        <div class="score-progress">
                            <div class="score-fill d-type" style="width: ${percentages.D}%"></div>
                        </div>
                        <div class="score-value">${result.dScore}점</div>
                    </div>
                    <div class="score-bar">
                        <div class="score-label">I (사교형)</div>
                        <div class="score-progress">
                            <div class="score-fill i-type" style="width: ${percentages.I}%"></div>
                        </div>
                        <div class="score-value">${result.iScore}점</div>
                    </div>
                    <div class="score-bar">
                        <div class="score-label">S (안정형)</div>
                        <div class="score-progress">
                            <div class="score-fill s-type" style="width: ${percentages.S}%"></div>
                        </div>
                        <div class="score-value">${result.sScore}점</div>
                    </div>
                    <div class="score-bar">
                        <div class="score-label">C (신중형)</div>
                        <div class="score-progress">
                            <div class="score-fill c-type" style="width: ${percentages.C}%"></div>
                        </div>
                        <div class="score-value">${result.cScore}점</div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Primary Type Description -->
        <div class="type-description">
            <div class="type-header">
                <div class="type-emoji">${typeDescription.emoji}</div>
                <div class="type-info">
                    <h2>${typeDescription.name}</h2>
                    <p class="type-subtitle">${typeDescription.subtitle}</p>
                </div>
            </div>

            <div class="characteristics">
                ${typeDescription.characteristics}
            </div>

            <div class="details-grid">
                <div class="detail-section strengths">
                    <h3>💪 주요 강점</h3>
                    <ul class="detail-list">
                        <c:forEach items="${typeDescription.strengths}" var="strength">
                            <li>${strength}</li>
                        </c:forEach>
                    </ul>
                </div>

                <div class="detail-section development">
                    <h3>🎯 개발 영역</h3>
                    <ul class="detail-list">
                        <c:forEach items="${typeDescription.developmentAreas}" var="area">
                            <li>${area}</li>
                        </c:forEach>
                    </ul>
                </div>

                <div class="detail-section communication">
                    <h3>💬 소통 스타일</h3>
                    <p>${typeDescription.communicationStyle}</p>
                </div>

                <div class="detail-section work-environment">
                    <h3>🏢 선호하는 업무 환경</h3>
                    <p>${typeDescription.workEnvironment}</p>
                </div>
            </div>
        </div>

        <!-- Career Recommendations -->
        <div class="career-section">
            <h3>🚀 추천 직업 및 역할</h3>
            <div class="career-grid">
                <c:forEach items="${careerRecommendations}" var="career">
                    <div class="career-item">${career}</div>
                </c:forEach>
            </div>
        </div>

        <!-- Action Buttons -->
        <div class="action-buttons">
            <button class="btn-print" onclick="window.print()">
                🖨️ 결과 인쇄하기
            </button>
            <button class="btn-print btn-secondary" onclick="location.href='${pageContext.request.contextPath}/jsp/login.jsp'">
                🏠 처음으로
            </button>
        </div>
    </div>

    <!-- Chart Data for JavaScript -->
    <script>
        // Chart data from server
        window.chartData = ${chartDataJson};

        // Additional data
        window.resultData = {
            userName: '${userName}',
            primaryType: '${primaryType}',
            scores: {
                D: ${result.dScore},
                I: ${result.iScore},
                S: ${result.sScore},
                C: ${result.cScore}
            },
            percentages: {
                D: ${percentages.D},
                I: ${percentages.I},
                S: ${percentages.S},
                C: ${percentages.C}
            }
        };
    </script>

    <script src="${pageContext.request.contextPath}/js/result.js"></script>
</body>
</html>