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
        this.displayChart();
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

    displayChart() {
        const canvas = document.getElementById('resultChart');
        if (!canvas) return;

        const ctx = canvas.getContext('2d');
        const types = ['D', 'I', 'S', 'C'];
        const percentages = types.map(type => this.results.results.percentages[type]);
        const colors = types.map(type => this.typeDescriptions[type].color);

        this.drawBarChart(ctx, canvas.width, canvas.height, types, percentages, colors);
    }

    drawBarChart(ctx, width, height, labels, data, colors) {
        ctx.clearRect(0, 0, width, height);

        const padding = 40;
        const chartWidth = width - padding * 2;
        const chartHeight = height - padding * 2;
        const barWidth = chartWidth / labels.length * 0.6;
        const maxValue = Math.max(...data, 50); // 최소 50%까지 표시

        // 배경
        ctx.fillStyle = '#f7fafc';
        ctx.fillRect(0, 0, width, height);

        // 축 그리기
        ctx.strokeStyle = '#e2e8f0';
        ctx.lineWidth = 1;

        // Y축 격자
        for (let i = 0; i <= 5; i++) {
            const y = padding + (chartHeight / 5) * i;
            ctx.beginPath();
            ctx.moveTo(padding, y);
            ctx.lineTo(width - padding, y);
            ctx.stroke();

            // Y축 라벨
            ctx.fillStyle = '#718096';
            ctx.font = '12px Arial';
            ctx.textAlign = 'right';
            ctx.fillText(`${Math.round((5 - i) * maxValue / 5)}%`, padding - 5, y + 4);
        }

        // 막대 그래프 그리기
        labels.forEach((label, index) => {
            const barHeight = (data[index] / maxValue) * chartHeight;
            const x = padding + (chartWidth / labels.length) * index + (chartWidth / labels.length - barWidth) / 2;
            const y = padding + chartHeight - barHeight;

            // 막대
            ctx.fillStyle = colors[index];
            ctx.fillRect(x, y, barWidth, barHeight);

            // 값 표시
            ctx.fillStyle = '#2d3748';
            ctx.font = 'bold 14px Arial';
            ctx.textAlign = 'center';
            ctx.fillText(`${data[index]}%`, x + barWidth / 2, y - 10);

            // 라벨
            ctx.fillStyle = '#4a5568';
            ctx.font = 'bold 16px Arial';
            ctx.fillText(label, x + barWidth / 2, height - padding + 20);
        });
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