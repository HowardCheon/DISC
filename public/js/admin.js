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
    alert(`${result.userName}님의 상세 결과:\n주요 유형: ${result.results.primaryType}\nD: ${result.results.percentages.D}%\nI: ${result.results.percentages.I}%\nS: ${result.results.percentages.S}%\nC: ${result.results.percentages.C}%`);
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

// 페이지 로드시 초기화
document.addEventListener('DOMContentLoaded', function() {
    window.adminDashboard = new AdminDashboard();
});