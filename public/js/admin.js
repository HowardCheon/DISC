// 관리자 페이지 JavaScript
class AdminDashboard {
    constructor() {
        this.testResults = [];
        this.init();
    }

    init() {
        // 로그인 확인
        if (!this.checkAuthication()) {
            window.location.href = 'admin-login.html';
            return;
        }

        this.loadTestResults();
        this.updateDashboard();
        this.loadRecentTests();
    }

    checkAuthication() {
        const isLoggedIn = localStorage.getItem('admin_logged_in');
        const loginTime = localStorage.getItem('admin_login_time');

        if (isLoggedIn !== 'true' || !loginTime) {
            return false;
        }

        // 세션 만료 확인 (24시간)
        const loginDate = new Date(loginTime);
        const now = new Date();
        const hoursDiff = (now - loginDate) / (1000 * 60 * 60);

        if (hoursDiff >= 24) {
            this.logout();
            return false;
        }

        return true;
    }

    loadTestResults() {
        // LocalStorage에서 모든 테스트 결과 로드
        const results = [];
        for (let i = 0; i < localStorage.length; i++) {
            const key = localStorage.key(i);
            if (key && key.startsWith('disc_test_results_')) {
                try {
                    const result = JSON.parse(localStorage.getItem(key));
                    results.push(result);
                } catch (e) {
                    console.error('Error parsing result:', e);
                }
            }
        }

        // 기존 결과가 없으면 샘플 데이터 추가
        if (results.length === 0) {
            this.addSampleData();
            return this.loadTestResults();
        }

        this.testResults = results.sort((a, b) => new Date(b.completedAt) - new Date(a.completedAt));
    }

    addSampleData() {
        const sampleResults = [
            {
                userName: '김영희',
                completedAt: new Date(Date.now() - 1000 * 60 * 60 * 2).toISOString(),
                results: {
                    scores: { D: 8, I: 15, S: 12, C: 9 },
                    percentages: { D: 18, I: 34, S: 27, C: 21 },
                    primaryType: 'I'
                }
            },
            {
                userName: '박철수',
                completedAt: new Date(Date.now() - 1000 * 60 * 60 * 24).toISOString(),
                results: {
                    scores: { D: 16, I: 8, S: 10, C: 14 },
                    percentages: { D: 33, I: 17, S: 21, C: 29 },
                    primaryType: 'D'
                }
            },
            {
                userName: '이미영',
                completedAt: new Date(Date.now() - 1000 * 60 * 60 * 24 * 2).toISOString(),
                results: {
                    scores: { D: 6, I: 10, S: 18, C: 12 },
                    percentages: { D: 13, I: 22, S: 39, C: 26 },
                    primaryType: 'S'
                }
            }
        ];

        sampleResults.forEach((result, index) => {
            localStorage.setItem(`disc_test_results_sample_${index}`, JSON.stringify(result));
        });
    }

    updateDashboard() {
        const now = new Date();
        const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
        const weekAgo = new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000);

        const todayTests = this.testResults.filter(result =>
            new Date(result.completedAt) >= today
        ).length;

        const weekTests = this.testResults.filter(result =>
            new Date(result.completedAt) >= weekAgo
        ).length;

        const avgScore = this.testResults.length > 0 ?
            Math.round(this.testResults.reduce((sum, result) => {
                const total = Object.values(result.results.percentages).reduce((a, b) => a + b, 0);
                return sum + (total / 4);
            }, 0) / this.testResults.length) : 0;

        document.getElementById('totalTests').textContent = this.testResults.length;
        document.getElementById('todayTests').textContent = todayTests;
        document.getElementById('weekTests').textContent = weekTests;
        document.getElementById('avgScore').textContent = avgScore + '%';
    }

    loadRecentTests() {
        const recentTests = this.testResults.slice(0, 5);
        const container = document.getElementById('recentTestsList');

        if (recentTests.length === 0) {
            container.innerHTML = '<p>최근 테스트 결과가 없습니다.</p>';
            return;
        }

        container.innerHTML = recentTests.map(result => `
            <div class="recent-test-item">
                <div class="test-info">
                    <strong>${result.userName}</strong>
                    <span class="test-type">${result.results.primaryType} 유형</span>
                </div>
                <div class="test-meta">
                    <span class="test-date">${new Date(result.completedAt).toLocaleString('ko-KR')}</span>
                </div>
            </div>
        `).join('');
    }

    logout() {
        localStorage.removeItem('admin_logged_in');
        localStorage.removeItem('admin_username');
        localStorage.removeItem('admin_login_time');
        window.location.href = 'admin-login.html';
    }

    searchResults() {
        const searchTerm = document.getElementById('searchInput').value.toLowerCase();
        const dateFilter = document.getElementById('dateFilter').value;

        let filtered = this.testResults;

        if (searchTerm) {
            filtered = filtered.filter(result =>
                result.userName.toLowerCase().includes(searchTerm)
            );
        }

        if (dateFilter) {
            const filterDate = new Date(dateFilter);
            filtered = filtered.filter(result => {
                const resultDate = new Date(result.completedAt);
                return resultDate.toDateString() === filterDate.toDateString();
            });
        }

        this.displayResultsTable(filtered);
    }

    displayResultsTable(results = this.testResults) {
        const container = document.getElementById('resultsTable');

        if (results.length === 0) {
            container.innerHTML = '<p>검색 결과가 없습니다.</p>';
            return;
        }

        container.innerHTML = `
            <table class="results-table">
                <thead>
                    <tr>
                        <th>이름</th>
                        <th>주요 유형</th>
                        <th>D</th>
                        <th>I</th>
                        <th>S</th>
                        <th>C</th>
                        <th>완료 시간</th>
                        <th>액션</th>
                    </tr>
                </thead>
                <tbody>
                    ${results.map((result, index) => `
                        <tr>
                            <td>${result.userName}</td>
                            <td class="type-${result.results.primaryType}">${result.results.primaryType}</td>
                            <td>${result.results.percentages.D}%</td>
                            <td>${result.results.percentages.I}%</td>
                            <td>${result.results.percentages.S}%</td>
                            <td>${result.results.percentages.C}%</td>
                            <td>${new Date(result.completedAt).toLocaleString('ko-KR')}</td>
                            <td>
                                <button onclick="viewResult(${index})" class="btn btn-sm">상세보기</button>
                                <button onclick="deleteResult(${index})" class="btn btn-sm btn-danger">삭제</button>
                            </td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>
        `;
    }

    exportResults() {
        const dataStr = JSON.stringify(this.testResults, null, 2);
        const dataBlob = new Blob([dataStr], { type: 'application/json' });

        const link = document.createElement('a');
        link.href = URL.createObjectURL(dataBlob);
        link.download = `disc_results_${new Date().toISOString().slice(0, 10)}.json`;
        link.click();
    }

    clearOldData() {
        if (!confirm('30일 이전 데이터를 정말 삭제하시겠습니까?')) {
            return;
        }

        const thirtyDaysAgo = new Date(Date.now() - 30 * 24 * 60 * 60 * 1000);
        let deletedCount = 0;

        for (let i = 0; i < localStorage.length; i++) {
            const key = localStorage.key(i);
            if (key && key.startsWith('disc_test_results_')) {
                try {
                    const result = JSON.parse(localStorage.getItem(key));
                    if (new Date(result.completedAt) < thirtyDaysAgo) {
                        localStorage.removeItem(key);
                        deletedCount++;
                        i--; // 인덱스 조정
                    }
                } catch (e) {
                    console.error('Error processing result:', e);
                }
            }
        }

        alert(`${deletedCount}개의 오래된 데이터가 삭제되었습니다.`);
        this.loadTestResults();
        this.updateDashboard();
    }

    clearAllData() {
        if (!confirm('정말 모든 테스트 데이터를 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) {
            return;
        }

        if (!confirm('마지막 확인입니다. 정말 모든 데이터를 삭제하시겠습니까?')) {
            return;
        }

        let deletedCount = 0;
        const keysToDelete = [];

        for (let i = 0; i < localStorage.length; i++) {
            const key = localStorage.key(i);
            if (key && key.startsWith('disc_test_')) {
                keysToDelete.push(key);
            }
        }

        keysToDelete.forEach(key => {
            localStorage.removeItem(key);
            deletedCount++;
        });

        alert(`${deletedCount}개의 데이터가 삭제되었습니다.`);
        this.testResults = [];
        this.updateDashboard();
        this.loadRecentTests();
        this.displayResultsTable();
    }

    backupData() {
        const allData = {};

        for (let i = 0; i < localStorage.length; i++) {
            const key = localStorage.key(i);
            if (key && key.startsWith('disc_test_')) {
                allData[key] = localStorage.getItem(key);
            }
        }

        const dataStr = JSON.stringify(allData, null, 2);
        const dataBlob = new Blob([dataStr], { type: 'application/json' });

        const link = document.createElement('a');
        link.href = URL.createObjectURL(dataBlob);
        link.download = `disc_backup_${new Date().toISOString().slice(0, 10)}.json`;
        link.click();
    }
}

// 전역 함수들
function showSection(sectionName) {
    // 모든 섹션 숨기기
    document.querySelectorAll('.admin-section').forEach(section => {
        section.classList.remove('active');
    });

    // 모든 네비게이션 버튼 비활성화
    document.querySelectorAll('.nav-btn').forEach(btn => {
        btn.classList.remove('active');
    });

    // 선택된 섹션 표시
    document.getElementById(sectionName).classList.add('active');
    event.target.classList.add('active');

    // 결과 섹션이면 테이블 표시
    if (sectionName === 'results') {
        window.adminDashboard.displayResultsTable();
    }
}

function logout() {
    window.adminDashboard.logout();
}

function searchResults() {
    window.adminDashboard.searchResults();
}

function exportResults() {
    window.adminDashboard.exportResults();
}

function clearOldData() {
    window.adminDashboard.clearOldData();
}

function clearAllData() {
    window.adminDashboard.clearAllData();
}

function backupData() {
    window.adminDashboard.backupData();
}

function viewResult(index) {
    const result = window.adminDashboard.testResults[index];
    showResultModal(result);
}

function deleteResult(index) {
    const result = window.adminDashboard.testResults[index];
    if (!confirm(`${result.userName}님의 결과를 삭제하시겠습니까?`)) {
        return;
    }

    // LocalStorage에서 해당 결과 찾아서 삭제
    for (let i = 0; i < localStorage.length; i++) {
        const key = localStorage.key(i);
        if (key && key.startsWith('disc_test_results_')) {
            try {
                const stored = JSON.parse(localStorage.getItem(key));
                if (stored.userName === result.userName && stored.completedAt === result.completedAt) {
                    localStorage.removeItem(key);
                    break;
                }
            } catch (e) {
                console.error('Error deleting result:', e);
            }
        }
    }

    // 데이터 다시 로드
    window.adminDashboard.loadTestResults();
    window.adminDashboard.updateDashboard();
    window.adminDashboard.loadRecentTests();
    window.adminDashboard.displayResultsTable();
}

// DISC 결과 표시 클래스 (result.js에서 가져옴)
class AdminResultsDisplay {
    constructor() {
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
    }

    displayResult(result) {
        this.displayModalTitle(result);
        this.displayMainType(result);
        this.displayCharts(result);
        this.displayDetailedDescriptions(result);
    }

    displayModalTitle(result) {
        const title = document.getElementById('modalTitle');
        if (title) {
            title.textContent = `${result.userName}님의 DISC 검사 결과`;
        }
    }

    displayMainType(result) {
        const mainTypeContainer = document.getElementById('modalMainType');
        const primaryType = result.results.primaryType;
        const percentage = result.results.percentages[primaryType];
        const typeInfo = this.typeDescriptions[primaryType];

        mainTypeContainer.innerHTML = `
            <div class="primary-type" style="border-left: 5px solid ${typeInfo.color};">
                <h3 style="color: ${typeInfo.color};">${typeInfo.title}</h3>
                <p class="type-description">${typeInfo.description}</p>
                <div class="primary-score">${percentage}%</div>
            </div>
        `;
    }

    displayCharts(result) {
        const types = ['D', 'I', 'S', 'C'];
        const scores = types.map(type => result.results.scores[type]);
        const percentages = types.map(type => result.results.percentages[type]);

        this.drawChart1(scores, types);
        this.drawChart2(percentages, types);
        this.drawChart3(percentages, types);
    }

    drawChart1(scores, types) {
        const canvas = document.getElementById('modalChart1');
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

    drawChart2(percentages, types) {
        const canvas = document.getElementById('modalChart2');
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

    drawChart3(percentages, types) {
        const canvas = document.getElementById('modalChart3');
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

    displayDetailedDescriptions(result) {
        const container = document.getElementById('modalTypeDescriptions');
        if (!container) return;

        const types = ['D', 'I', 'S', 'C'];
        container.innerHTML = '';

        types.forEach(type => {
            const typeInfo = this.typeDescriptions[type];
            const percentage = result.results.percentages[type];
            const isHighScore = percentage >= 25;

            const typeCard = document.createElement('div');
            typeCard.className = `type-card ${type === result.results.primaryType ? 'primary' : ''}`;
            typeCard.id = `modal${type.toLowerCase()}Type`;

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
}

// 모달 관련 함수들
function showResultModal(result) {
    const modal = document.getElementById('resultModal');
    const resultDisplay = new AdminResultsDisplay();

    resultDisplay.displayResult(result);
    modal.style.display = 'block';

    // 모달 외부 클릭 시 닫기
    modal.onclick = function(event) {
        if (event.target === modal) {
            closeResultModal();
        }
    };
}

function closeResultModal() {
    const modal = document.getElementById('resultModal');
    modal.style.display = 'none';
}

function printModalResult() {
    // 모달 내용만 인쇄하도록 구현
    const modalBody = document.querySelector('#resultModal .modal-body');
    const originalContents = document.body.innerHTML;
    const printContents = modalBody.innerHTML;

    document.body.innerHTML = printContents;
    window.print();
    document.body.innerHTML = originalContents;

    // 페이지 새로고침을 통해 이벤트 리스너 복원
    location.reload();
}

// CSS 스타일 추가
const modalStyles = `
    <style>
        .modal {
            display: none;
            position: fixed;
            z-index: 1000;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            overflow: auto;
            background-color: rgba(0,0,0,0.4);
        }

        .modal-content {
            background-color: #fefefe;
            margin: 2% auto;
            padding: 0;
            border: 1px solid #888;
            border-radius: 8px;
            width: 90%;
            max-width: 1200px;
            max-height: 90vh;
            overflow: auto;
        }

        .modal-header {
            padding: 20px;
            background-color: #f8f9fa;
            border-bottom: 1px solid #dee2e6;
            border-radius: 8px 8px 0 0;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .modal-header h2 {
            margin: 0;
            color: #333;
        }

        .close {
            color: #aaa;
            font-size: 28px;
            font-weight: bold;
            cursor: pointer;
            line-height: 1;
        }

        .close:hover,
        .close:focus {
            color: #000;
            text-decoration: none;
        }

        .modal-body {
            padding: 20px;
        }

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

        .chart-section h5 {
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

        .result-charts h4 {
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

        @media (max-width: 768px) {
            .modal-content {
                width: 95%;
                margin: 5% auto;
            }

            .strengths-weaknesses {
                grid-template-columns: 1fr;
            }
        }
    </style>
`;

// 페이지 로드시 초기화
document.addEventListener('DOMContentLoaded', function() {
    // 모달 스타일 추가
    document.head.insertAdjacentHTML('beforeend', modalStyles);

    window.adminDashboard = new AdminDashboard();
});