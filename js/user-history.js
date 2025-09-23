let selectedUserId = null;
let testHistoryData = [];
let scoreChart = null;

// 사용자 검색
function searchUsers() {
    const searchTerm = document.getElementById('userSearch').value.trim();

    if (!searchTerm) {
        alert('검색어를 입력해주세요.');
        return;
    }

    fetch(`/api/user-history?action=searchUsers&searchTerm=${encodeURIComponent(searchTerm)}`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                displaySearchResults(data.users);
            } else {
                alert(data.message || '검색 중 오류가 발생했습니다.');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('검색 중 오류가 발생했습니다.');
        });
}

// 검색 결과 표시
function displaySearchResults(users) {
    const searchResults = document.getElementById('searchResults');
    const userList = document.getElementById('userList');

    userList.innerHTML = '';

    if (users.length === 0) {
        userList.innerHTML = '<div class="list-group-item text-muted">검색 결과가 없습니다.</div>';
    } else {
        users.forEach(user => {
            const userItem = document.createElement('a');
            userItem.className = 'list-group-item list-group-item-action';
            userItem.href = '#';
            userItem.onclick = (e) => {
                e.preventDefault();
                selectUser(user);
            };

            userItem.innerHTML = `
                <div class="d-flex w-100 justify-content-between">
                    <h6 class="mb-1">${user.name}</h6>
                    <small>${user.position || ''}</small>
                </div>
                <p class="mb-1">${user.email}</p>
                <small>${user.department || ''}</small>
            `;

            userList.appendChild(userItem);
        });
    }

    searchResults.style.display = 'block';
}

// 사용자 선택
function selectUser(user) {
    selectedUserId = user.id;

    // 사용자 정보 표시
    document.getElementById('selectedUserName').textContent = user.name;
    document.getElementById('selectedUserEmail').textContent = user.email;

    // 검사 이력 로드
    loadUserHistory(user.id);

    // UI 업데이트
    document.getElementById('searchResults').style.display = 'none';
    document.getElementById('emptyState').style.display = 'none';
    document.getElementById('selectedUserSection').style.display = 'block';
}

// 사용자 검사 이력 로드
function loadUserHistory(userId) {
    fetch(`/api/user-history?action=getUserHistory&userId=${userId}`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                testHistoryData = data.history;
                document.getElementById('totalTests').textContent = data.totalTests;
                displayTestHistory(data.history);
                updateScoreChart(data.history);
            } else {
                alert(data.message || '이력 로드 중 오류가 발생했습니다.');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('이력 로드 중 오류가 발생했습니다.');
        });
}

// 검사 이력 표시
function displayTestHistory(history) {
    const historyContainer = document.getElementById('testHistory');

    if (history.length === 0) {
        historyContainer.innerHTML = '<p class="text-muted text-center">검사 이력이 없습니다.</p>';
        return;
    }

    historyContainer.innerHTML = '';

    history.forEach((test, index) => {
        const testItem = document.createElement('div');
        testItem.className = 'test-item';
        testItem.dataset.testId = test.id;

        const statusBadge = getStatusBadge(test.status);
        const sentDate = formatDate(test.sentDate);
        const startDate = formatDate(test.startDate);
        const completedDate = formatDate(test.completedDate);

        testItem.innerHTML = `
            <div class="row align-items-center">
                <div class="col-md-2">
                    <small class="text-muted">검사 #${index + 1}</small>
                    <br>
                    ${statusBadge}
                </div>
                <div class="col-md-3">
                    <strong>발송일시</strong><br>
                    <small>${sentDate}</small><br>
                    <strong>시작일시</strong><br>
                    <small>${startDate}</small>
                </div>
                <div class="col-md-2">
                    <strong>완료일시</strong><br>
                    <small>${completedDate}</small>
                </div>
                <div class="col-md-3">
                    <strong>유형: ${test.primaryType || 'N/A'}</strong><br>
                    <small>D:${test.dScore || 0} I:${test.iScore || 0} S:${test.sScore || 0} C:${test.cScore || 0}</small>
                </div>
                <div class="col-md-2 text-end">
                    <button class="btn btn-sm btn-outline-primary me-1" onclick="viewTestResult(${test.id})">
                        <i class="fas fa-eye"></i> 결과보기
                    </button>
                    <button class="btn btn-sm btn-outline-secondary" onclick="toggleTestSelection(${test.id})">
                        <i class="fas fa-check"></i>
                    </button>
                </div>
            </div>
        `;

        historyContainer.appendChild(testItem);
    });
}

// 상태 배지 생성
function getStatusBadge(status) {
    const badges = {
        'SENT': '<span class="badge bg-info status-badge">발송됨</span>',
        'STARTED': '<span class="badge bg-warning status-badge">진행중</span>',
        'COMPLETED': '<span class="badge bg-success status-badge">완료</span>',
        'EXPIRED': '<span class="badge bg-danger status-badge">만료됨</span>'
    };

    return badges[status] || '<span class="badge bg-secondary status-badge">알 수 없음</span>';
}

// 날짜 포맷팅
function formatDate(dateString) {
    if (!dateString) return 'N/A';

    const date = new Date(dateString);
    return date.toLocaleString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

// 검사 결과 보기
function viewTestResult(testId) {
    fetch(`/api/user-history?action=getTestResult&surveyId=${testId}`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                displayTestResult(data.survey);
            } else {
                alert(data.message || '결과 로드 중 오류가 발생했습니다.');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('결과 로드 중 오류가 발생했습니다.');
        });
}

// 검사 결과 모달 표시
function displayTestResult(survey) {
    const modal = new bootstrap.Modal(document.getElementById('resultModal'));
    const content = document.getElementById('resultContent');

    content.innerHTML = `
        <div class="row">
            <div class="col-md-6">
                <h6>DISC 점수</h6>
                <div class="mb-3">
                    <div class="d-flex justify-content-between">
                        <span>D (주도형):</span>
                        <strong>${survey.dScore || 0}점</strong>
                    </div>
                    <div class="d-flex justify-content-between">
                        <span>I (사교형):</span>
                        <strong>${survey.iScore || 0}점</strong>
                    </div>
                    <div class="d-flex justify-content-between">
                        <span>S (안정형):</span>
                        <strong>${survey.sScore || 0}점</strong>
                    </div>
                    <div class="d-flex justify-content-between">
                        <span>C (신중형):</span>
                        <strong>${survey.cScore || 0}점</strong>
                    </div>
                </div>

                <h6>주요 유형</h6>
                <p class="badge bg-primary">${survey.primaryType || 'N/A'}</p>
            </div>
            <div class="col-md-6">
                <canvas id="resultChart" width="300" height="300"></canvas>
            </div>
        </div>

        <hr>

        <div class="row">
            <div class="col-md-12">
                <h6>특성 설명</h6>
                <p>${survey.description || '설명이 없습니다.'}</p>

                <h6>강점</h6>
                <p>${survey.strengths || '강점 정보가 없습니다.'}</p>

                <h6>약점</h6>
                <p>${survey.weaknesses || '약점 정보가 없습니다.'}</p>

                <h6>개발 제안</h6>
                <p>${survey.recommendations || '제안 사항이 없습니다.'}</p>
            </div>
        </div>
    `;

    modal.show();

    // 결과 차트 생성
    setTimeout(() => {
        createResultChart(survey);
    }, 100);
}

// 개별 결과 차트 생성
function createResultChart(survey) {
    const ctx = document.getElementById('resultChart');
    if (!ctx) return;

    new Chart(ctx, {
        type: 'radar',
        data: {
            labels: ['D (주도형)', 'I (사교형)', 'S (안정형)', 'C (신중형)'],
            datasets: [{
                label: 'DISC 점수',
                data: [survey.dScore || 0, survey.iScore || 0, survey.sScore || 0, survey.cScore || 0],
                backgroundColor: 'rgba(54, 162, 235, 0.2)',
                borderColor: 'rgba(54, 162, 235, 1)',
                borderWidth: 2
            }]
        },
        options: {
            responsive: true,
            scales: {
                r: {
                    beginAtZero: true,
                    max: 100
                }
            }
        }
    });
}

// 점수 변화 추이 차트 업데이트
function updateScoreChart(history) {
    if (history.length < 2) {
        return; // 비교할 데이터가 부족
    }

    const ctx = document.getElementById('scoreChart');
    if (!ctx) return;

    // 기존 차트 제거
    if (scoreChart) {
        scoreChart.destroy();
    }

    // 날짜순으로 정렬 (오래된 순)
    const sortedHistory = [...history].sort((a, b) =>
        new Date(a.completedDate) - new Date(b.completedDate)
    );

    const labels = sortedHistory.map((test, index) => `검사 ${index + 1}`);
    const dScores = sortedHistory.map(test => test.dScore || 0);
    const iScores = sortedHistory.map(test => test.iScore || 0);
    const sScores = sortedHistory.map(test => test.sScore || 0);
    const cScores = sortedHistory.map(test => test.cScore || 0);

    scoreChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [
                {
                    label: 'D (주도형)',
                    data: dScores,
                    borderColor: 'rgb(255, 99, 132)',
                    backgroundColor: 'rgba(255, 99, 132, 0.2)',
                    tension: 0.1
                },
                {
                    label: 'I (사교형)',
                    data: iScores,
                    borderColor: 'rgb(54, 162, 235)',
                    backgroundColor: 'rgba(54, 162, 235, 0.2)',
                    tension: 0.1
                },
                {
                    label: 'S (안정형)',
                    data: sScores,
                    borderColor: 'rgb(255, 205, 86)',
                    backgroundColor: 'rgba(255, 205, 86, 0.2)',
                    tension: 0.1
                },
                {
                    label: 'C (신중형)',
                    data: cScores,
                    borderColor: 'rgb(75, 192, 192)',
                    backgroundColor: 'rgba(75, 192, 192, 0.2)',
                    tension: 0.1
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: true,
                    max: 100
                }
            },
            plugins: {
                legend: {
                    position: 'top',
                },
                title: {
                    display: true,
                    text: 'DISC 점수 변화 추이'
                }
            }
        }
    });
}

// 결과 비교 토글
function toggleComparison() {
    const section = document.getElementById('comparisonSection');
    const isVisible = section.style.display !== 'none';

    section.style.display = isVisible ? 'none' : 'block';

    if (!isVisible && testHistoryData.length >= 2) {
        updateScoreChart(testHistoryData);
    }
}

// 검사 선택 토글 (비교용)
function toggleTestSelection(testId) {
    const testItem = document.querySelector(`[data-test-id="${testId}"]`);
    if (testItem) {
        testItem.classList.toggle('selected-test');
    }
}

// 이력 정렬
function sortHistory(type) {
    let sortedHistory = [...testHistoryData];

    if (type === 'date') {
        sortedHistory.sort((a, b) => new Date(b.completedDate) - new Date(a.completedDate));
    } else if (type === 'status') {
        sortedHistory.sort((a, b) => a.status.localeCompare(b.status));
    }

    displayTestHistory(sortedHistory);
}

// PDF 다운로드
function downloadResult() {
    // 현재 모달의 검사 결과를 PDF로 다운로드하는 기능
    alert('PDF 다운로드 기능은 준비 중입니다.');
}

// 엔터키로 검색
document.addEventListener('DOMContentLoaded', function() {
    document.getElementById('userSearch').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            searchUsers();
        }
    });
});