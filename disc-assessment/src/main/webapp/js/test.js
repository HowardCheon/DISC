/**
 * DISC Test JavaScript - Enhanced Version
 * Handles test page functionality, validation, and user interactions
 */

class DiscTest {
    constructor() {
        this.testData = window.testData || {};
        this.answers = this.loadAnswersFromStorage();
        this.autoSaveTimer = null;
        this.validationErrors = new Set();
        
        this.init();
    }
    
    init() {
        this.bindEvents();
        this.loadExistingAnswers();
        this.updateUI();
        this.startAutoSave();
        
        // Log initialization
        console.log('DISC Test initialized:', this.testData);
    }
    
    /**
     * Bind event listeners
     */
    bindEvents() {
        // Radio button change events
        document.querySelectorAll('input[type="radio"]').forEach(radio => {
            radio.addEventListener('change', (e) => {
                this.handleAnswerChange(e);
            });
        });
        
        // Option item click events (for better UX)
        document.querySelectorAll('.option-item').forEach(item => {
            item.addEventListener('click', (e) => {
                const radio = item.querySelector('input[type="radio"]');
                if (radio && !radio.checked) {
                    radio.checked = true;
                    this.handleAnswerChange({ target: radio });
                }
            });
        });
        
        // Navigation button events
        const nextBtn = document.getElementById('nextBtn');
        const submitBtn = document.getElementById('submitBtn');
        
        if (nextBtn) {
            nextBtn.addEventListener('click', () => this.handleNextPage());
        }
        
        if (submitBtn) {
            submitBtn.addEventListener('click', () => this.handleSubmit());
        }
        
        // Prevent accidental page reload
        window.addEventListener('beforeunload', (e) => {
            if (this.hasUnsavedChanges()) {
                e.preventDefault();
                e.returnValue = '답변이 저장되지 않을 수 있습니다. 정말 페이지를 나가시겠습니까?';
                return e.returnValue;
            }
        });
        
        // Keyboard shortcuts
        document.addEventListener('keydown', (e) => {
            if (e.ctrlKey || e.metaKey) {
                switch(e.key) {
                    case 's':
                        e.preventDefault();
                        this.saveAnswers();
                        break;
                    case 'ArrowLeft':
                        if (!this.testData.isLastPage) {
                            e.preventDefault();
                            this.handlePrevPage();
                        }
                        break;
                    case 'ArrowRight':
                        if (this.testData.currentPage < this.testData.totalPages) {
                            e.preventDefault();
                            this.handleNextPage();
                        }
                        break;
                }
            }
        });
    }
    
    /**
     * Handle answer selection change
     */
    handleAnswerChange(event) {
        const radio = event.target;
        const questionId = this.extractQuestionId(radio.name);
        const answerType = this.extractAnswerType(radio.name);
        const value = radio.value;
        
        if (!questionId || !answerType) return;
        
        // Initialize question answers if not exists
        if (!this.answers[questionId]) {
            this.answers[questionId] = {};
        }
        
        // Store the answer
        this.answers[questionId][answerType] = value;
        
        // Update UI
        this.updateOptionStyles(radio);
        this.validateQuestion(questionId);
        this.updateProgressInfo();
        this.checkConflicts(questionId);
        
        // Schedule auto-save
        this.scheduleAutoSave();
        
        console.log(`Answer updated: Q${questionId} ${answerType} = ${value}`);
    }
    
    /**
     * Update option visual styles
     */
    updateOptionStyles(selectedRadio) {
        const questionId = this.extractQuestionId(selectedRadio.name);
        const answerType = this.extractAnswerType(selectedRadio.name);
        
        // Update option item styles
        const optionGroup = selectedRadio.closest('.option-group');
        const optionItems = optionGroup.querySelectorAll('.option-item');
        
        optionItems.forEach(item => {
            item.classList.remove('selected');
        });
        
        selectedRadio.closest('.option-item').classList.add('selected');
        
        // Update option group styles
        optionGroup.classList.add('has-selection');
        
        // Clear validation error for this question and type
        this.clearValidationError(questionId, answerType);
    }
    
    /**
     * Check for conflicts between mostLike and leastLike selections
     */
    checkConflicts(questionId) {
        const answer = this.answers[questionId];
        if (!answer || !answer.mostLike || !answer.leastLike) return;
        
        if (answer.mostLike === answer.leastLike) {
            this.showConflictWarning(questionId);
        } else {
            this.clearConflictWarning(questionId);
        }
    }
    
    /**
     * Show conflict warning
     */
    showConflictWarning(questionId) {
        const questionItem = document.querySelector(`[data-question-id="${questionId}"]`);
        if (!questionItem) return;
        
        let warningElement = questionItem.querySelector('.conflict-warning');
        if (!warningElement) {
            warningElement = document.createElement('div');
            warningElement.className = 'conflict-warning validation-message show';
            warningElement.innerHTML = '⚠️ 같은 답변을 선택할 수 없습니다. 다른 답변을 선택해주세요.';
            questionItem.appendChild(warningElement);
        } else {
            warningElement.classList.add('show');
        }
        
        this.validationErrors.add(`conflict_${questionId}`);
    }
    
    /**
     * Clear conflict warning
     */
    clearConflictWarning(questionId) {
        const questionItem = document.querySelector(`[data-question-id="${questionId}"]`);
        if (!questionItem) return;
        
        const warningElement = questionItem.querySelector('.conflict-warning');
        if (warningElement) {
            warningElement.classList.remove('show');
        }
        
        this.validationErrors.delete(`conflict_${questionId}`);
    }
    
    /**
     * Validate a specific question
     */
    validateQuestion(questionId) {
        const answer = this.answers[questionId];
        const hasComplete = answer && answer.mostLike && answer.leastLike;
        const hasConflict = answer && answer.mostLike === answer.leastLike;
        
        // Clear previous validation errors
        this.clearValidationError(questionId, 'mostLike');
        this.clearValidationError(questionId, 'leastLike');
        
        if (!hasComplete) {
            if (!answer || !answer.mostLike) {
                this.showValidationError(questionId, 'mostLike');
            }
            if (!answer || !answer.leastLike) {
                this.showValidationError(questionId, 'leastLike');
            }
        }
        
        return hasComplete && !hasConflict;
    }
    
    /**
     * Show validation error
     */
    showValidationError(questionId, answerType) {
        const errorElement = document.getElementById(`${answerType}_${questionId}_error`);
        if (errorElement) {
            errorElement.classList.add('show');
        }
        
        this.validationErrors.add(`${answerType}_${questionId}`);
    }
    
    /**
     * Clear validation error
     */
    clearValidationError(questionId, answerType) {
        const errorElement = document.getElementById(`${answerType}_${questionId}_error`);
        if (errorElement) {
            errorElement.classList.remove('show');
        }
        
        this.validationErrors.delete(`${answerType}_${questionId}`);
    }
    
    /**
     * Validate current page
     */
    validateCurrentPage() {
        const currentPageQuestions = this.getCurrentPageQuestions();
        let isValid = true;
        
        this.validationErrors.clear();
        
        currentPageQuestions.forEach(questionId => {
            if (!this.validateQuestion(questionId)) {
                isValid = false;
            }
        });
        
        return isValid;
    }
    
    /**
     * Get question IDs for current page
     */
    getCurrentPageQuestions() {
        const questionItems = document.querySelectorAll('.question-item');
        return Array.from(questionItems).map(item => 
            parseInt(item.getAttribute('data-question-id'))
        );
    }
    
    /**
     * Handle next page navigation
     */
    handleNextPage() {
        if (this.validationErrors.size > 0) {
            this.showValidationSummary();
            this.scrollToFirstError();
            return;
        }
        
        if (!this.validateCurrentPage()) {
            this.showValidationSummary();
            this.scrollToFirstError();
            return;
        }
        
        // Save current answers before navigating
        this.saveAnswers(() => {
            const nextPage = this.testData.currentPage + 1;
            const url = `${this.testData.contextPath}/test?name=${encodeURIComponent(this.testData.userName)}&token=${encodeURIComponent(this.testData.testToken)}&page=${nextPage}`;
            window.location.href = url;
        });
    }
    
    /**
     * Handle previous page navigation
     */
    handlePrevPage() {
        this.saveAnswers(() => {
            const prevPage = this.testData.currentPage - 1;
            const url = `${this.testData.contextPath}/test?name=${encodeURIComponent(this.testData.userName)}&token=${encodeURIComponent(this.testData.testToken)}&page=${prevPage}`;
            window.location.href = url;
        });
    }
    
    /**
     * Handle test submission
     */
    handleSubmit() {
        if (this.validationErrors.size > 0) {
            this.showValidationSummary();
            this.scrollToFirstError();
            return;
        }
        
        if (!this.validateCurrentPage()) {
            this.showValidationSummary();
            this.scrollToFirstError();
            return;
        }
        
        // Check if all 28 questions are answered
        this.checkCompleteness((isComplete) => {
            if (!isComplete) {
                this.showIncompleteTestWarning();
                return;
            }
            
            this.showSubmissionConfirmation();
        });
    }
    
    /**
     * Check test completeness
     */
    checkCompleteness(callback) {
        const totalAnswered = Object.keys(this.answers).length;
        const allValid = Object.values(this.answers).every(answer => 
            answer.mostLike && answer.leastLike && answer.mostLike !== answer.leastLike
        );
        
        const isComplete = totalAnswered === 28 && allValid;
        callback(isComplete);
    }
    
    /**
     * Show incomplete test warning
     */
    showIncompleteTestWarning() {
        const totalAnswered = Object.keys(this.answers).length;
        const message = `검사가 완료되지 않았습니다.\n\n` +
                       `- 총 28문항 중 ${totalAnswered}문항 완료\n` +
                       `- 모든 문항에 답변해주세요.\n\n` +
                       `이전 페이지로 돌아가서 답변을 완료해주세요.`;
        
        alert(message);
    }
    
    /**
     * Show submission confirmation
     */
    showSubmissionConfirmation() {
        const message = `DISC 성격검사를 완료하시겠습니까?\n\n` +
                       `모든 답변이 저장되고 결과를 확인할 수 있습니다.\n` +
                       `제출 후에는 답변을 수정할 수 없습니다.`;
        
        if (confirm(message)) {
            this.submitTest();
        }
    }
    
    /**
     * Submit the test
     */
    submitTest() {
        this.updateSaveStatus('검사 결과를 제출하는 중...', 'saving');
        
        // Disable submit button
        const submitBtn = document.getElementById('submitBtn');
        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.textContent = '제출 중...';
        }
        
        // Prepare submission data
        const submissionData = {
            action: 'submitTest',
            name: this.testData.userName,
            token: this.testData.testToken,
            answers: JSON.stringify(this.answers)
        };
        
        // Submit to server
        fetch(`${this.testData.contextPath}/submit`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: Object.keys(submissionData)
                .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(submissionData[key])}`)
                .join('&')
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                // Clear local storage
                this.clearAnswersFromStorage();
                
                // Show success message
                this.updateSaveStatus('검사가 성공적으로 완료되었습니다!', 'saved');
                
                // Redirect to results after a short delay
                setTimeout(() => {
                    window.location.href = `${this.testData.contextPath}/result?token=${encodeURIComponent(this.testData.testToken)}`;
                }, 1000);
            } else {
                throw new Error(data.message || 'Submission failed');
            }
        })
        .catch(error => {
            console.error('Submission error:', error);
            this.updateSaveStatus('제출 중 오류가 발생했습니다. 다시 시도해주세요.', 'error');
            
            // Re-enable submit button
            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.textContent = '검사 완료 및 결과 보기 →';
            }
        });
    }
    
    /**
     * Show validation summary
     */
    showValidationSummary() {
        const errorCount = this.validationErrors.size;
        if (errorCount === 0) return;
        
        let message = `다음 항목을 확인해주세요:\n\n`;
        
        this.validationErrors.forEach(error => {
            if (error.startsWith('conflict_')) {
                const questionId = error.replace('conflict_', '');
                message += `• 문항 ${questionId}: 같은 답변을 선택할 수 없습니다\n`;
            } else if (error.includes('mostLike')) {
                const questionId = error.replace('mostLike_', '');
                message += `• 문항 ${questionId}: 가장 나와 같은 것을 선택해주세요\n`;
            } else if (error.includes('leastLike')) {
                const questionId = error.replace('leastLike_', '');
                message += `• 문항 ${questionId}: 가장 나와 다른 것을 선택해주세요\n`;
            }
        });
        
        alert(message);
    }
    
    /**
     * Scroll to first validation error
     */
    scrollToFirstError() {
        const firstErrorElement = document.querySelector('.validation-message.show');
        if (firstErrorElement) {
            firstErrorElement.scrollIntoView({ 
                behavior: 'smooth', 
                block: 'center' 
            });
        }
    }
    
    /**
     * Save answers to server
     */
    saveAnswers(callback) {
        const saveData = {
            action: 'saveAnswers',
            name: this.testData.userName,
            token: this.testData.testToken,
            page: this.testData.currentPage,
            answers: JSON.stringify(this.answers)
        };
        
        this.updateSaveStatus('저장 중...', 'saving');
        
        fetch(`${this.testData.contextPath}/test`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: Object.keys(saveData)
                .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(saveData[key])}`)
                .join('&')
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                this.updateSaveStatus('자동 저장됨', 'saved');
                this.saveAnswersToStorage();
                if (callback) callback();
            } else {
                throw new Error(data.message || 'Save failed');
            }
        })
        .catch(error => {
            console.error('Save error:', error);
            this.updateSaveStatus('저장 실패 (로컬에 임시 저장됨)', 'error');
            this.saveAnswersToStorage(); // Fallback to local storage
            if (callback) callback();
        });
    }
    
    /**
     * Schedule auto-save
     */
    scheduleAutoSave() {
        if (this.autoSaveTimer) {
            clearTimeout(this.autoSaveTimer);
        }
        
        this.autoSaveTimer = setTimeout(() => {
            this.saveAnswers();
        }, 2000); // Save after 2 seconds of inactivity
    }
    
    /**
     * Start auto-save interval
     */
    startAutoSave() {
        // Auto-save every 30 seconds
        setInterval(() => {
            if (this.hasUnsavedChanges()) {
                this.saveAnswers();
            }
        }, 30000);
    }
    
    /**
     * Check if there are unsaved changes
     */
    hasUnsavedChanges() {
        const stored = this.loadAnswersFromStorage();
        return JSON.stringify(this.answers) !== JSON.stringify(stored);
    }
    
    /**
     * Update save status display
     */
    updateSaveStatus(message, type = '') {
        const statusElement = document.getElementById('saveStatus');
        if (statusElement) {
            statusElement.textContent = message;
            statusElement.className = `save-status ${type}`;
        }
    }
    
    /**
     * Update progress information
     */
    updateProgressInfo() {
        const totalAnswered = Object.keys(this.answers).length;
        const progressPercentage = Math.round((totalAnswered / 28) * 100);
        
        // Update progress bar if visible
        const progressFill = document.querySelector('.progress-fill');
        if (progressFill) {
            progressFill.style.width = `${progressPercentage}%`;
        }
        
        // Update answered count if visible
        const answeredCountElement = document.querySelector('.stat-value');
        if (answeredCountElement && answeredCountElement.parentElement.querySelector('.stat-label')?.textContent === '완료된 문항') {
            answeredCountElement.textContent = totalAnswered;
        }
    }
    
    /**
     * Load existing answers from server data
     */
    loadExistingAnswers() {
        if (this.testData.existingAnswers) {
            Object.assign(this.answers, this.testData.existingAnswers);
            this.restoreAnswerSelections();
        }
    }
    
    /**
     * Restore answer selections in UI
     */
    restoreAnswerSelections() {
        Object.keys(this.answers).forEach(questionId => {
            const answer = this.answers[questionId];
            
            if (answer.mostLike) {
                const radio = document.querySelector(`input[name="mostLike_${questionId}"][value="${answer.mostLike}"]`);
                if (radio) {
                    radio.checked = true;
                    this.updateOptionStyles(radio);
                }
            }
            
            if (answer.leastLike) {
                const radio = document.querySelector(`input[name="leastLike_${questionId}"][value="${answer.leastLike}"]`);
                if (radio) {
                    radio.checked = true;
                    this.updateOptionStyles(radio);
                }
            }
            
            this.validateQuestion(parseInt(questionId));
        });
    }
    
    /**
     * Local storage methods
     */
    getStorageKey() {
        return `discTest_${this.testData.testToken}`;
    }
    
    saveAnswersToStorage() {
        try {
            localStorage.setItem(this.getStorageKey(), JSON.stringify(this.answers));
        } catch (error) {
            console.warn('Could not save to localStorage:', error);
        }
    }
    
    loadAnswersFromStorage() {
        try {
            const stored = localStorage.getItem(this.getStorageKey());
            return stored ? JSON.parse(stored) : {};
        } catch (error) {
            console.warn('Could not load from localStorage:', error);
            return {};
        }
    }
    
    clearAnswersFromStorage() {
        try {
            localStorage.removeItem(this.getStorageKey());
        } catch (error) {
            console.warn('Could not clear localStorage:', error);
        }
    }
    
    /**
     * Utility methods
     */
    extractQuestionId(inputName) {
        const match = inputName.match(/_(\d+)$/);
        return match ? parseInt(match[1]) : null;
    }
    
    extractAnswerType(inputName) {
        if (inputName.startsWith('mostLike_')) return 'mostLike';
        if (inputName.startsWith('leastLike_')) return 'leastLike';
        return null;
    }
    
    updateUI() {
        this.updateProgressInfo();
        
        // Update page title with progress
        const totalAnswered = Object.keys(this.answers).length;
        document.title = `DISC 검사 (${totalAnswered}/28) - 페이지 ${this.testData.currentPage}`;
    }
}

// Initialize test when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    // Check if we're on the test page
    if (window.testData) {
        window.discTest = new DiscTest();
    }
    
    // Add smooth transitions
    document.body.style.opacity = '0';
    document.body.style.transition = 'opacity 0.3s ease-in-out';
    
    setTimeout(() => {
        document.body.style.opacity = '1';
    }, 100);
});

// Prevent form resubmission on page refresh
if (window.history.replaceState) {
    window.history.replaceState(null, null, window.location.href);
}