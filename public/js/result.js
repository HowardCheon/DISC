// DISC 결과 페이지 JavaScript
class DiscResultsDisplay {
    constructor() {
        this.results = this.loadResults();
        this.typeDescriptions = {
            D: {
                title: "주도형 (Dominance)",
                description: "결과 지향적이고 도전을 즐기며 직접적인 성향",
                characteristics: [
                    "목표 달성에 집중한다",
                    "도전적인 상황을 선호한다",
                    "빠른 결정을 내린다",
                    "결과를 중시한다",
                    "리더십을 발휘한다"
                ],
                strengths: ["결단력", "추진력", "도전정신", "리더십"],
                weaknesses: ["성급함", "완고함", "세심함 부족"],
                color: "#e53e3e"
            },
            I: {
                title: "사교형 (Influence)",
                description: "사람들과의 관계를 중시하고 활발하며 낙관적인 성향",
                characteristics: [
                    "사람들과의 교류를 즐긴다",
                    "낙관적이고 열정적이다",
                    "창의적인 아이디어를 제시한다",
                    "팀워크를 중시한다",
                    "설득력이 뛰어나다"
                ],
                strengths: ["커뮤니케이션", "창의성", "낙관주의", "설득력"],
                weaknesses: ["집중력 부족", "계획성 부족", "감정적"],
                color: "#38a169"
            },
            S: {
                title: "안정형 (Steadiness)",
                description: "안정성을 추구하고 참을성이 많으며 협조적인 성향",
                characteristics: [
                    "안정적이고 일관성이 있다",
                    "협력을 중시한다",
                    "참을성이 많다",
                    "신뢰할 수 있다",
                    "갈등을 피하려 한다"
                ],
                strengths: ["협조성", "인내심", "신뢰성", "안정성"],
                weaknesses: ["변화 적응 어려움", "자기주장 부족", "우유부단"],
                color: "#3182ce"
            },
            C: {
                title: "신중형 (Conscientiousness)",
                description: "정확성을 중시하고 분석적이며 체계적인 성향",
                characteristics: [
                    "정확성과 품질을 중시한다",
                    "체계적이고 논리적이다",
                    "신중하게 분석한다",
                    "규칙과 절차를 준수한다",
                    "완벽을 추구한다"
                ],
                strengths: ["정확성", "분석력", "체계성", "완벽주의"],
                weaknesses: ["경직성", "결정 지연", "비판적"],
                color: "#805ad5"
            }
        };

        this.init();
    }

    loadResults() {
        const resultsData = localStorage.getItem('disc_test_results');
        if (!resultsData) {
            this.showNoResultsMessage();
            return null;
        }

        try {
            return JSON.parse(resultsData);
        } catch (e) {
            console.error('Error loading results:', e);
            this.showErrorMessage();
            return null;
        }
    }

    init() {
        if (!this.results) return;

        this.displayUserInfo();
        this.displayMainType();
        this.displayScores();
        this.displayCharts();
        this.displayDetailedDescriptions();
    }

    displayUserInfo() {
        const header = document.querySelector('header h1');
        if (header && this.results.userName) {
            header.textContent = `${this.results.userName}님의 DISC 검사 결과`;
        }
    }

    displayMainType() {
        const mainTypeContainer = document.getElementById('mainType');
        const primaryType = this.results.results.primaryType;
        const percentage = this.results.results.percentages[primaryType];
        const typeInfo = this.typeDescriptions[primaryType];

        mainTypeContainer.innerHTML = `
            <div class="primary-type" style="border-left: 5px solid ${typeInfo.color};">
                <h3 style="color: ${typeInfo.color};">${typeInfo.title}</h3>
                <p class="type-description">${typeInfo.description}</p>
                <div class="primary-score">${percentage}%</div>
            </div>
        `;
    }

    displayScores() {
        const types = ['D', 'I', 'S', 'C'];
        types.forEach(type => {
            const scoreElement = document.getElementById(`${type.toLowerCase()}Score`);
            const typeCard = document.getElementById(`${type.toLowerCase()}Type`);
            const percentage = this.results.results.percentages[type];
            const typeInfo = this.typeDescriptions[type];

            if (scoreElement) {
                scoreElement.textContent = `${percentage}%`;
                scoreElement.style.color = typeInfo.color;
            }

            if (typeCard) {
                typeCard.style.borderLeft = `4px solid ${typeInfo.color}`;
                if (type === this.results.results.primaryType) {
                    typeCard.classList.add('primary-type-card');
                    typeCard.style.backgroundColor = `${typeInfo.color}15`;
                }
            }
        });
    }

    displayCharts() {
        const types = ['D', 'I', 'S', 'C'];
        const scores = types.map(type => this.results.results.scores[type]);
        const percentages = types.map(type => this.results.results.percentages[type]);

        // 그래프 I: 기본 점수 (0-28 스케일)
        this.drawChart1(scores, types);

        // 그래프 II: 정규화 점수 (중앙선 기준)
        this.drawChart2(percentages, types);

        // 그래프 III: 상대적 편차 (+/- 값)
        this.drawChart3(percentages, types);
    }

    // 그래프 I: 기본 점수 차트 (0-28 스케일, 상단에서 시작)
    drawChart1(scores, types) {
        const canvas = document.getElementById('chart1');
        if (!canvas) return;
        const ctx = canvas.getContext('2d');
        const width = canvas.width;
        const height = canvas.height;

        ctx.clearRect(0, 0, width, height);

        const padding = 40;
        const chartWidth = width - padding * 2;
        const chartHeight = height - padding * 2;
        const colWidth = chartWidth / 4;
        const maxScore = 28;

        // 배경
        ctx.fillStyle = '#f8f9fa';
        ctx.fillRect(0, 0, width, height);

        // 격자선과 라벨 (왼쪽에서 오른쪽으로: D, I, S, C)
        ctx.strokeStyle = '#dee2e6';
        ctx.lineWidth = 1;

        // 수평 격자선 (1-7 스케일)
        for (let i = 1; i <= 7; i++) {
            const y = padding + ((8 - i) * chartHeight / 7);
            ctx.beginPath();
            ctx.moveTo(padding, y);
            ctx.lineTo(width - padding, y);
            ctx.stroke();

            // 왼쪽 라벨
            ctx.fillStyle = '#6c757d';
            ctx.font = '12px Arial';
            ctx.textAlign = 'right';
            ctx.fillText(i.toString(), padding - 10, y + 4);
        }

        // 수직선 및 데이터 포인트
        types.forEach((type, index) => {
            const x = padding + colWidth * index + colWidth / 2;

            // 수직선
            ctx.beginPath();
            ctx.moveTo(x, padding);
            ctx.lineTo(x, height - padding);
            ctx.stroke();

            // 데이터 포인트
            const score = scores[index];
            const normalizedScore = Math.min(Math.max(score / 4, 1), 7); // 0-28을 1-7로 변환
            const dotY = padding + ((8 - normalizedScore) * chartHeight / 7);

            ctx.fillStyle = '#000';
            ctx.beginPath();
            ctx.arc(x, dotY, 4, 0, 2 * Math.PI);
            ctx.fill();

            // 점수 표시
            ctx.fillStyle = '#000';
            ctx.font = 'bold 12px Arial';
            ctx.textAlign = 'center';
            ctx.fillText(score.toString(), x, dotY - 10);

            // 하단 타입 라벨
            ctx.fillStyle = '#000';
            ctx.font = 'bold 16px Arial';
            ctx.fillText(type, x, height - 10);
        });

        // 상단 제목
        ctx.fillStyle = '#000';
        ctx.font = 'bold 14px Arial';
        ctx.textAlign = 'center';
        ctx.fillText('강도', width / 2, 20);
    }

    // 그래프 II: 정규화 점수 차트 (중앙선 기준)
    drawChart2(percentages, types) {
        const canvas = document.getElementById('chart2');
        if (!canvas) return;
        const ctx = canvas.getContext('2d');
        const width = canvas.width;
        const height = canvas.height;

        ctx.clearRect(0, 0, width, height);

        const padding = 40;
        const chartWidth = width - padding * 2;
        const chartHeight = height - padding * 2;
        const colWidth = chartWidth / 4;
        const centerY = padding + chartHeight / 2;

        // 배경
        ctx.fillStyle = '#f8f9fa';
        ctx.fillRect(0, 0, width, height);

        // 격자선
        ctx.strokeStyle = '#dee2e6';
        ctx.lineWidth = 1;

        // 수평 격자선 (1-7 스케일)
        for (let i = 1; i <= 7; i++) {
            const y = padding + ((8 - i) * chartHeight / 7);
            ctx.beginPath();
            ctx.moveTo(padding, y);
            ctx.lineTo(width - padding, y);
            ctx.stroke();

            // 왼쪽 라벨
            ctx.fillStyle = '#6c757d';
            ctx.font = '12px Arial';
            ctx.textAlign = 'right';
            ctx.fillText(i.toString(), padding - 10, y + 4);
        }

        // 중앙선 강조
        ctx.strokeStyle = '#adb5bd';
        ctx.lineWidth = 2;
        ctx.beginPath();
        ctx.moveTo(padding, centerY);
        ctx.lineTo(width - padding, centerY);
        ctx.stroke();

        // 데이터 포인트와 연결선
        const points = [];
        types.forEach((type, index) => {
            const x = padding + colWidth * index + colWidth / 2;

            // 수직선
            ctx.strokeStyle = '#dee2e6';
            ctx.lineWidth = 1;
            ctx.beginPath();
            ctx.moveTo(x, padding);
            ctx.lineTo(x, height - padding);
            ctx.stroke();

            // 정규화된 점수 계산 (25%를 중앙선으로)
            const normalizedScore = (percentages[index] - 25) / 25 * 3.5 + 4; // 1-7 스케일로 변환
            const clampedScore = Math.min(Math.max(normalizedScore, 1), 7);
            const dotY = padding + ((8 - clampedScore) * chartHeight / 7);

            points.push({ x, y: dotY });

            // 데이터 포인트
            ctx.fillStyle = '#000';
            ctx.beginPath();
            ctx.arc(x, dotY, 4, 0, 2 * Math.PI);
            ctx.fill();

            // 점수 표시
            ctx.fillStyle = '#000';
            ctx.font = 'bold 12px Arial';
            ctx.textAlign = 'center';
            ctx.fillText(percentages[index].toString(), x, dotY - 10);

            // 하단 타입 라벨
            ctx.fillStyle = '#000';
            ctx.font = 'bold 16px Arial';
            ctx.fillText(type, x, height - 10);
        });

        // 점들을 연결하는 선
        ctx.strokeStyle = '#000';
        ctx.lineWidth = 2;
        ctx.beginPath();
        ctx.moveTo(points[0].x, points[0].y);
        for (let i = 1; i < points.length; i++) {
            ctx.lineTo(points[i].x, points[i].y);
        }
        ctx.stroke();

        // 상단 제목
        ctx.fillStyle = '#000';
        ctx.font = 'bold 14px Arial';
        ctx.textAlign = 'center';
        ctx.fillText('강도', width / 2, 20);
    }

    // 그래프 III: 상대적 편차 차트 (+/- 값)
    drawChart3(percentages, types) {
        const canvas = document.getElementById('chart3');
        if (!canvas) return;
        const ctx = canvas.getContext('2d');
        const width = canvas.width;
        const height = canvas.height;

        ctx.clearRect(0, 0, width, height);

        const padding = 40;
        const chartWidth = width - padding * 2;
        const chartHeight = height - padding * 2;
        const colWidth = chartWidth / 4;
        const centerY = padding + chartHeight / 2;
        const average = percentages.reduce((a, b) => a + b, 0) / percentages.length;

        // 배경
        ctx.fillStyle = '#f8f9fa';
        ctx.fillRect(0, 0, width, height);

        // 격자선
        ctx.strokeStyle = '#dee2e6';
        ctx.lineWidth = 1;

        // 수평 격자선 (-27 to +27)
        for (let i = -27; i <= 27; i += 9) {
            const normalizedPos = (i + 27) / 54; // 0-1 범위로 정규화
            const y = padding + (1 - normalizedPos) * chartHeight;

            ctx.beginPath();
            ctx.moveTo(padding, y);
            ctx.lineTo(width - padding, y);
            ctx.stroke();

            // 라벨
            if (i !== 0) {
                ctx.fillStyle = '#6c757d';
                ctx.font = '12px Arial';
                ctx.textAlign = 'right';
                ctx.fillText(i > 0 ? `+${i}` : i.toString(), padding - 10, y + 4);
            }
        }

        // 중앙선 (0선) 강조
        ctx.strokeStyle = '#adb5bd';
        ctx.lineWidth = 2;
        ctx.beginPath();
        ctx.moveTo(padding, centerY);
        ctx.lineTo(width - padding, centerY);
        ctx.stroke();

        // 0 라벨
        ctx.fillStyle = '#000';
        ctx.font = 'bold 12px Arial';
        ctx.textAlign = 'right';
        ctx.fillText('0', padding - 10, centerY + 4);

        // 데이터 포인트와 연결선
        const points = [];
        types.forEach((type, index) => {
            const x = padding + colWidth * index + colWidth / 2;

            // 수직선
            ctx.strokeStyle = '#dee2e6';
            ctx.lineWidth = 1;
            ctx.beginPath();
            ctx.moveTo(x, padding);
            ctx.lineTo(x, height - padding);
            ctx.stroke();

            // 편차 계산
            const deviation = percentages[index] - average;
            const clampedDeviation = Math.min(Math.max(deviation, -27), 27);
            const normalizedPos = (clampedDeviation + 27) / 54;
            const dotY = padding + (1 - normalizedPos) * chartHeight;

            points.push({ x, y: dotY });

            // 데이터 포인트
            ctx.fillStyle = deviation >= 0 ? '#28a745' : '#dc3545';
            ctx.beginPath();
            ctx.arc(x, dotY, 4, 0, 2 * Math.PI);
            ctx.fill();

            // 편차 값 표시
            ctx.fillStyle = '#000';
            ctx.font = 'bold 12px Arial';
            ctx.textAlign = 'center';
            const deviationText = deviation >= 0 ? `+${Math.round(deviation)}` : Math.round(deviation).toString();
            ctx.fillText(deviationText, x, dotY - 10);

            // 하단 타입 라벨
            ctx.fillStyle = '#000';
            ctx.font = 'bold 16px Arial';
            ctx.fillText(type, x, height - 10);
        });

        // 점들을 연결하는 선
        ctx.strokeStyle = '#000';
        ctx.lineWidth = 2;
        ctx.beginPath();
        ctx.moveTo(points[0].x, points[0].y);
        for (let i = 1; i < points.length; i++) {
            ctx.lineTo(points[i].x, points[i].y);
        }
        ctx.stroke();

        // 상단 제목
        ctx.fillStyle = '#000';
        ctx.font = 'bold 14px Arial';
        ctx.textAlign = 'center';
        ctx.fillText('강도', width / 2, 20);
    }

    displayDetailedDescriptions() {
        const container = document.querySelector('.type-descriptions');
        if (!container) return;

        const types = ['D', 'I', 'S', 'C'];
        container.innerHTML = '';

        types.forEach(type => {
            const typeInfo = this.typeDescriptions[type];
            const percentage = this.results.results.percentages[type];
            const isHighScore = percentage >= 25;

            const typeCard = document.createElement('div');
            typeCard.className = `type-card ${type === this.results.results.primaryType ? 'primary' : ''}`;
            typeCard.id = `${type.toLowerCase()}Type`;

            typeCard.innerHTML = `
                <div class="type-header">
                    <h4 style="color: ${typeInfo.color};">${typeInfo.title}</h4>
                    <div class="score" style="color: ${typeInfo.color};">${percentage}%</div>
                </div>
                <p class="type-description">${typeInfo.description}</p>

                ${isHighScore ? `
                    <div class="characteristics">
                        <h5>주요 특성:</h5>
                        <ul>
                            ${typeInfo.characteristics.map(char => `<li>${char}</li>`).join('')}
                        </ul>
                    </div>

                    <div class="strengths-weaknesses">
                        <div class="strengths">
                            <h6>강점:</h6>
                            <div class="tags">
                                ${typeInfo.strengths.map(strength =>
                                    `<span class="tag strength">${strength}</span>`
                                ).join('')}
                            </div>
                        </div>

                        <div class="weaknesses">
                            <h6>개발 영역:</h6>
                            <div class="tags">
                                ${typeInfo.weaknesses.map(weakness =>
                                    `<span class="tag weakness">${weakness}</span>`
                                ).join('')}
                            </div>
                        </div>
                    </div>
                ` : ''}
            `;

            container.appendChild(typeCard);
        });
    }

    showNoResultsMessage() {
        document.body.innerHTML = `
            <div class="container">
                <div class="no-results">
                    <h2>검사 결과를 찾을 수 없습니다</h2>
                    <p>먼저 DISC 검사를 완료해주세요.</p>
                    <a href="index.html" class="btn btn-primary">검사 시작하기</a>
                </div>
            </div>
        `;
    }

    showErrorMessage() {
        document.body.innerHTML = `
            <div class="container">
                <div class="error">
                    <h2>결과를 불러오는 중 오류가 발생했습니다</h2>
                    <p>검사를 다시 시도해주세요.</p>
                    <a href="index.html" class="btn btn-primary">검사 다시 시작</a>
                </div>
            </div>
        `;
    }
}

// CSS 스타일 추가
const additionalStyles = `
    <style>
        .charts-container {
            display: flex;
            gap: 20px;
            justify-content: center;
            margin: 20px 0;
            flex-wrap: wrap;
        }

        .chart-section {
            flex: 1;
            min-width: 300px;
            background: #ffffff;
            border: 1px solid #dee2e6;
            border-radius: 8px;
            padding: 15px;
            text-align: center;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }

        .chart-section h4 {
            margin: 0 0 15px 0;
            font-size: 16px;
            font-weight: bold;
            color: #333;
        }

        .chart-section canvas {
            border: 1px solid #e9ecef;
            background: #fff;
        }

        .result-charts {
            margin: 30px 0;
        }

        .result-charts h3 {
            text-align: center;
            margin-bottom: 20px;
            color: #333;
        }

        @media (max-width: 1024px) {
            .charts-container {
                flex-direction: column;
                align-items: center;
            }

            .chart-section {
                width: 100%;
                max-width: 400px;
            }
        }

        .primary-type {
            padding: 20px;
            margin: 20px 0;
            background: #f7fafc;
            border-radius: 8px;
            text-align: center;
        }

        .primary-score {
            font-size: 2.5em;
            font-weight: bold;
            margin-top: 10px;
        }

        .type-card {
            border: 1px solid #e2e8f0;
            border-radius: 8px;
            padding: 20px;
            margin: 15px 0;
            transition: all 0.3s ease;
        }

        .type-card:hover {
            box-shadow: 0 4px 12px rgba(0,0,0,0.1);
            transform: translateY(-2px);
        }

        .type-card.primary {
            border-width: 2px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.1);
        }

        .type-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 10px;
        }

        .characteristics ul {
            list-style-type: none;
            padding: 0;
        }

        .characteristics li {
            padding: 5px 0;
            padding-left: 20px;
            position: relative;
        }

        .characteristics li:before {
            content: "✓";
            position: absolute;
            left: 0;
            color: #38a169;
            font-weight: bold;
        }

        .strengths-weaknesses {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 20px;
            margin-top: 15px;
        }

        .tags {
            display: flex;
            flex-wrap: wrap;
            gap: 8px;
            margin-top: 8px;
        }

        .tag {
            padding: 4px 12px;
            border-radius: 16px;
            font-size: 0.85em;
            font-weight: 500;
        }

        .tag.strength {
            background: #c6f6d5;
            color: #22543d;
        }

        .tag.weakness {
            background: #fed7d7;
            color: #742a2a;
        }

        .no-results, .error {
            text-align: center;
            padding: 60px 20px;
        }

        @media (max-width: 768px) {
            .strengths-weaknesses {
                grid-template-columns: 1fr;
            }
        }
    </style>
`;

// 페이지 로드 시 스타일 추가 및 초기화
document.addEventListener('DOMContentLoaded', function() {
    // 스타일 추가
    document.head.insertAdjacentHTML('beforeend', additionalStyles);

    // 결과 표시 초기화
    window.discResults = new DiscResultsDisplay();
});