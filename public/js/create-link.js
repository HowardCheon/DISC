/**
 * Create Link JavaScript
 * Handles autocomplete, form submissions, and link management
 */

document.addEventListener('DOMContentLoaded', function() {
    initializeEventListeners();
    loadHistoryData();
    setupAutocomplete();
});

let autocompleteTimeout;
let currentSuggestions = [];
let selectedSuggestionIndex = -1;

/**
 * Initialize event listeners
 */
function initializeEventListeners() {
    // Single link form
    document.getElementById('singleLinkForm').addEventListener('submit', handleSingleLinkSubmit);

    // Bulk link form
    document.getElementById('bulkLinkForm').addEventListener('submit', handleBulkLinkSubmit);

    // Search functionality
    document.getElementById('searchUser').addEventListener('input', handleUserSearch);

    // Close modals when clicking outside
    document.addEventListener('click', function(e) {
        if (e.target.classList.contains('modal') && e.target.style.display === 'block') {
            e.target.style.display = 'none';
        }
    });
}

/**
 * Setup autocomplete functionality
 */
function setupAutocomplete() {
    const userNameInput = document.getElementById('userName');
    const suggestionsDiv = document.getElementById('userSuggestions');

    userNameInput.addEventListener('input', function() {
        const query = this.value.trim();

        clearTimeout(autocompleteTimeout);

        if (query.length < 2) {
            hideSuggestions();
            return;
        }

        autocompleteTimeout = setTimeout(() => {
            fetchUserSuggestions(query);
        }, 300);
    });

    userNameInput.addEventListener('keydown', function(e) {
        if (currentSuggestions.length === 0) return;

        switch(e.key) {
            case 'ArrowDown':
                e.preventDefault();
                selectedSuggestionIndex = Math.min(selectedSuggestionIndex + 1, currentSuggestions.length - 1);
                updateSuggestionSelection();
                break;

            case 'ArrowUp':
                e.preventDefault();
                selectedSuggestionIndex = Math.max(selectedSuggestionIndex - 1, -1);
                updateSuggestionSelection();
                break;

            case 'Enter':
                if (selectedSuggestionIndex >= 0) {
                    e.preventDefault();
                    selectSuggestion(currentSuggestions[selectedSuggestionIndex]);
                }
                break;

            case 'Escape':
                hideSuggestions();
                break;
        }
    });

    userNameInput.addEventListener('blur', function() {
        // Hide suggestions after a short delay to allow clicks
        setTimeout(hideSuggestions, 200);
    });
}

/**
 * Fetch user suggestions from server
 */
function fetchUserSuggestions(query) {
    fetch(`${getContextPath()}/admin/create-link?action=getUserSuggestions&query=${encodeURIComponent(query)}`)
        .then(response => response.json())
        .then(data => {
            if (data.success && data.data) {
                currentSuggestions = data.data;
                showSuggestions(currentSuggestions);
            }
        })
        .catch(error => {
            console.error('Error fetching suggestions:', error);
            hideSuggestions();
        });
}

/**
 * Show autocomplete suggestions
 */
function showSuggestions(suggestions) {
    const suggestionsDiv = document.getElementById('userSuggestions');

    if (suggestions.length === 0) {
        hideSuggestions();
        return;
    }

    suggestionsDiv.innerHTML = '';
    selectedSuggestionIndex = -1;

    suggestions.forEach((suggestion, index) => {
        const div = document.createElement('div');
        div.className = 'autocomplete-suggestion';
        div.textContent = suggestion;
        div.addEventListener('click', () => selectSuggestion(suggestion));
        suggestionsDiv.appendChild(div);
    });

    suggestionsDiv.style.display = 'block';
}

/**
 * Update suggestion selection visual
 */
function updateSuggestionSelection() {
    const suggestions = document.querySelectorAll('.autocomplete-suggestion');

    suggestions.forEach((suggestion, index) => {
        suggestion.classList.toggle('selected', index === selectedSuggestionIndex);
    });
}

/**
 * Select a suggestion
 */
function selectSuggestion(suggestion) {
    document.getElementById('userName').value = suggestion;
    hideSuggestions();
}

/**
 * Hide autocomplete suggestions
 */
function hideSuggestions() {
    document.getElementById('userSuggestions').style.display = 'none';
    currentSuggestions = [];
    selectedSuggestionIndex = -1;
}

/**
 * Handle single link form submission
 */
function handleSingleLinkSubmit(e) {
    e.preventDefault();

    const userName = document.getElementById('userName').value.trim();

    if (!userName) {
        showAlert('사용자 이름을 입력해주세요.', 'warning');
        return;
    }

    const submitBtn = e.target.querySelector('button[type="submit"]');
    setButtonLoading(submitBtn, true);

    const formData = new FormData();
    formData.append('action', 'createSingle');
    formData.append('userName', userName);

    fetch(`${getContextPath()}/admin/create-link`, {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        setButtonLoading(submitBtn, false);

        if (data.success) {
            if (data.data && data.data.requiresConfirmation) {
                showDuplicateConfirmation(userName, data.data.duplicateInfo);
            } else {
                showSuccess(data.data);
                document.getElementById('singleLinkForm').reset();
                refreshHistory();
            }
        } else {
            showAlert(data.message || '링크 생성에 실패했습니다.', 'danger');
        }
    })
    .catch(error => {
        setButtonLoading(submitBtn, false);
        console.error('Error:', error);
        showAlert('시스템 오류가 발생했습니다.', 'danger');
    });
}

/**
 * Handle bulk link form submission
 */
function handleBulkLinkSubmit(e) {
    e.preventDefault();

    const userNamesText = document.getElementById('userNames').value.trim();

    if (!userNamesText) {
        showAlert('사용자 이름 목록을 입력해주세요.', 'warning');
        return;
    }

    const userNames = userNamesText.split('\n')
        .map(name => name.trim())
        .filter(name => name.length > 0);

    if (userNames.length === 0) {
        showAlert('유효한 사용자 이름이 없습니다.', 'warning');
        return;
    }

    if (userNames.length > 100) {
        showAlert('한 번에 최대 100명까지만 등록할 수 있습니다.', 'warning');
        return;
    }

    const submitBtn = e.target.querySelector('button[type="submit"]');
    setButtonLoading(submitBtn, true);

    const formData = new FormData();
    formData.append('action', 'createBulk');
    formData.append('userNames', userNamesText);

    fetch(`${getContextPath()}/admin/create-link`, {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        setButtonLoading(submitBtn, false);

        if (data.success) {
            showBulkResults(data.data);
            document.getElementById('bulkLinkForm').reset();
            refreshHistory();
        } else {
            showAlert(data.message || '일괄 생성에 실패했습니다.', 'danger');
        }
    })
    .catch(error => {
        setButtonLoading(submitBtn, false);
        console.error('Error:', error);
        showAlert('시스템 오류가 발생했습니다.', 'danger');
    });
}

/**
 * Show duplicate confirmation dialog
 */
function showDuplicateConfirmation(userName, duplicateInfo) {
    currentDuplicateUser = userName;

    const content = `
        <div class="alert alert-warning">
            <strong>${userName}</strong> 사용자는 이미 등록되어 있습니다.
        </div>
        <div class="row">
            <div class="col-md-6">
                <small class="text-muted">기존 링크 수:</small><br>
                <strong>${duplicateInfo.linkCount}개</strong>
            </div>
            <div class="col-md-6">
                <small class="text-muted">완료된 검사:</small><br>
                <strong>${duplicateInfo.completedCount}개</strong>
            </div>
        </div>
        <p class="mt-3 mb-0">새로운 링크를 생성하시겠습니까?</p>
    `;

    document.getElementById('duplicateContent').innerHTML = content;

    const modal = new bootstrap.Modal(document.getElementById('duplicateModal'));
    modal.show();
}

/**
 * Confirm duplicate user link creation
 */
function confirmDuplicate() {
    if (!currentDuplicateUser) return;

    const formData = new FormData();
    formData.append('action', 'createSingle');
    formData.append('userName', currentDuplicateUser);
    formData.append('confirmDuplicate', 'true');

    fetch(`${getContextPath()}/admin/create-link`, {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            showSuccess(data.data);
            document.getElementById('singleLinkForm').reset();
            refreshHistory();
        } else {
            showAlert(data.message || '링크 생성에 실패했습니다.', 'danger');
        }

        bootstrap.Modal.getInstance(document.getElementById('duplicateModal')).hide();
        currentDuplicateUser = null;
    })
    .catch(error => {
        console.error('Error:', error);
        showAlert('시스템 오류가 발생했습니다.', 'danger');
        bootstrap.Modal.getInstance(document.getElementById('duplicateModal')).hide();
    });
}

/**
 * Show success modal
 */
function showSuccess(data) {
    currentSuccessUrl = data.url;

    const content = `
        <div class="alert alert-success">
            <strong>${data.user.name}</strong> 사용자의 링크가 생성되었습니다.
        </div>
        <div class="mb-3">
            <label class="form-label">생성된 URL:</label>
            <div class="url-input-group">
                <input type="text" class="form-control" value="${data.url}" readonly>
                <button type="button" class="copy-btn" onclick="copyToClipboard('${data.url}')">
                    <i class="bi bi-clipboard"></i>
                </button>
            </div>
        </div>
        <div class="row">
            <div class="col-md-6">
                <small class="text-muted">사용자 ID:</small><br>
                <strong>${data.user.id}</strong>
            </div>
            <div class="col-md-6">
                <small class="text-muted">총 검사 횟수:</small><br>
                <strong>${data.testCount}회</strong>
            </div>
        </div>
    `;

    document.getElementById('successContent').innerHTML = content;

    const modal = new bootstrap.Modal(document.getElementById('successModal'));
    modal.show();
}

/**
 * Copy success URL to clipboard
 */
function copySuccessUrl() {
    if (currentSuccessUrl) {
        copyToClipboard(currentSuccessUrl);
    }
}

/**
 * Show bulk results modal
 */
function showBulkResults(data) {
    let content = `
        <div class="row mb-3">
            <div class="col-md-4">
                <div class="card text-center">
                    <div class="card-body">
                        <h3 class="text-success">${data.successCount}</h3>
                        <small>성공</small>
                    </div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="card text-center">
                    <div class="card-body">
                        <h3 class="text-danger">${data.failureCount}</h3>
                        <small>실패</small>
                    </div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="card text-center">
                    <div class="card-body">
                        <h3 class="text-info">${data.totalRequested}</h3>
                        <small>총 요청</small>
                    </div>
                </div>
            </div>
        </div>
    `;

    if (data.successList && data.successList.length > 0) {
        content += '<h6 class="text-success">성공한 링크들:</h6>';
        content += '<div class="table-responsive mb-3">';
        content += '<table class="table table-sm">';
        content += '<thead><tr><th>사용자명</th><th>URL</th><th>동작</th></tr></thead><tbody>';

        data.successList.forEach(item => {
            content += `
                <tr>
                    <td>${item.user.name}</td>
                    <td><small>${item.url.substring(0, 50)}...</small></td>
                    <td>
                        <button class="btn btn-sm btn-outline-primary" onclick="copyToClipboard('${item.url}')">
                            <i class="bi bi-clipboard"></i>
                        </button>
                    </td>
                </tr>
            `;
        });

        content += '</tbody></table></div>';
    }

    if (data.failureList && data.failureList.length > 0) {
        content += '<h6 class="text-danger">실패한 항목들:</h6>';
        content += '<div class="table-responsive">';
        content += '<table class="table table-sm">';
        content += '<thead><tr><th>사용자명</th><th>오류</th></tr></thead><tbody>';

        data.failureList.forEach(item => {
            content += `
                <tr>
                    <td>${item.userName}</td>
                    <td class="text-danger"><small>${item.error}</small></td>
                </tr>
            `;
        });

        content += '</tbody></table></div>';
    }

    document.getElementById('bulkResultsContent').innerHTML = content;

    const modal = new bootstrap.Modal(document.getElementById('bulkResultsModal'));
    modal.show();
}

/**
 * Handle user search
 */
function handleUserSearch(e) {
    const query = e.target.value.trim();

    if (query.length === 0) {
        loadHistoryData();
        return;
    }

    fetch(`${getContextPath()}/admin/create-link?action=getHistory&userName=${encodeURIComponent(query)}`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                renderHistoryTable(data.data || []);
            }
        })
        .catch(error => {
            console.error('Error searching history:', error);
        });
}

/**
 * Load history data
 */
function loadHistoryData() {
    if (typeof recentHistory !== 'undefined' && recentHistory) {
        renderHistoryTable(recentHistory);
    } else {
        refreshHistory();
    }
}

/**
 * Refresh history data from server
 */
function refreshHistory() {
    fetch(`${getContextPath()}/admin/create-link?action=getHistory`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                renderHistoryTable(data.data || []);
            }
        })
        .catch(error => {
            console.error('Error refreshing history:', error);
        });
}

/**
 * Render history table
 */
function renderHistoryTable(historyData) {
    const tbody = document.getElementById('historyTableBody');

    if (!historyData || historyData.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted">생성된 링크가 없습니다.</td></tr>';
        return;
    }

    tbody.innerHTML = '';

    historyData.forEach((item, index) => {
        const row = document.createElement('tr');

        // Calculate test count for user
        const testCount = getTestCountForUser(historyData, item.userName, index);

        // Format date
        const createdDate = new Date(item.createdAt);
        const formattedDate = createdDate.toLocaleString('ko-KR');

        // Status badge
        const statusClass = getStatusClass(item.status);
        const statusText = getStatusText(item.status);

        // Generate URL
        const testUrl = `${window.location.origin}${getContextPath()}/test?token=${item.testUrl}`;

        row.innerHTML = `
            <td><strong>${item.userName}</strong></td>
            <td><span class="badge bg-secondary">${testCount}차</span></td>
            <td><small>${formattedDate}</small></td>
            <td><span class="badge ${statusClass} status-badge">${statusText}</span></td>
            <td>
                <div class="url-input-group">
                    <input type="text" class="form-control form-control-sm" value="${testUrl}" readonly>
                    <button type="button" class="copy-btn" onclick="copyToClipboard('${testUrl}')">
                        <i class="bi bi-clipboard"></i>
                    </button>
                </div>
            </td>
            <td>
                <button class="btn btn-sm btn-outline-primary" onclick="copyToClipboard('${testUrl}')" title="URL 복사">
                    <i class="bi bi-clipboard"></i>
                </button>
            </td>
        `;

        tbody.appendChild(row);
    });
}

/**
 * Get test count for user (nth test for this user)
 */
function getTestCountForUser(historyData, userName, currentIndex) {
    let count = 1;
    for (let i = historyData.length - 1; i > currentIndex; i--) {
        if (historyData[i].userName === userName) {
            count++;
        }
    }
    return count;
}

/**
 * Get status CSS class
 */
function getStatusClass(status) {
    switch (status) {
        case '대기중': return 'bg-warning text-dark';
        case '진행중': return 'bg-info text-white';
        case '검사완료': return 'bg-success text-white';
        default: return 'bg-secondary text-white';
    }
}

/**
 * Get status text
 */
function getStatusText(status) {
    return status || '알 수 없음';
}

/**
 * Copy text to clipboard
 */
function copyToClipboard(text) {
    if (navigator.clipboard) {
        navigator.clipboard.writeText(text).then(() => {
            showAlert('클립보드에 복사되었습니다.', 'info', 2000);
        }).catch(err => {
            console.error('Could not copy text: ', err);
            fallbackCopyTextToClipboard(text);
        });
    } else {
        fallbackCopyTextToClipboard(text);
    }
}

/**
 * Fallback copy function for older browsers
 */
function fallbackCopyTextToClipboard(text) {
    const textArea = document.createElement('textarea');
    textArea.value = text;
    textArea.style.position = 'fixed';
    textArea.style.left = '-999999px';
    textArea.style.top = '-999999px';
    document.body.appendChild(textArea);
    textArea.focus();
    textArea.select();

    try {
        document.execCommand('copy');
        showAlert('클립보드에 복사되었습니다.', 'info', 2000);
    } catch (err) {
        console.error('Fallback: Oops, unable to copy', err);
        showAlert('복사에 실패했습니다.', 'warning');
    }

    document.body.removeChild(textArea);
}

/**
 * Show alert message
 */
function showAlert(message, type = 'info', duration = 5000) {
    const alertArea = document.getElementById('alertArea');

    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type} alert-dismissible fade show alert-custom`;
    alertDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;

    alertArea.appendChild(alertDiv);

    // Auto remove after duration
    setTimeout(() => {
        if (alertDiv.parentNode) {
            alertDiv.remove();
        }
    }, duration);
}

/**
 * Set button loading state
 */
function setButtonLoading(button, isLoading) {
    if (isLoading) {
        button.disabled = true;
        const originalText = button.innerHTML;
        button.setAttribute('data-original-text', originalText);
        button.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>처리 중...';
    } else {
        button.disabled = false;
        const originalText = button.getAttribute('data-original-text');
        if (originalText) {
            button.innerHTML = originalText;
        }
    }
}

/**
 * Get context path
 */
function getContextPath() {
    return window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1)) || '';
}