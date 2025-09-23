// 정적 DISC 테스트 JavaScript
class StaticDiscTest {
    constructor() {
        this.currentQuestion = 0;
        this.answers = {};
        this.userName = this.getUserName();
        this.init();
    }

    init() {
        if (!this.userName) {
            this.getUserNameInput();
            return;
        }

        this.loadSavedAnswers();
        this.renderCurrentQuestion();
        this.bindEvents();
    }

    getUserName() {
        const urlParams = new URLSearchParams(window.location.search);
        let name = urlParams.get('name');

        if (!name) {
            name = localStorage.getItem('disc_user_name');
        }

        return name;
    }

    getUserNameInput() {
        const name = prompt("안녕하세요! DISC 성격유형 검사를 시작하겠습니다.\n\n성함을 입력해주세요:");

        if (!name || name.trim() === '') {
            alert("성함을 입력해야 검사를 진행할 수 있습니다.");
            window.location.href = 'index.html';
            return;
        }

        this.userName = name.trim();
        localStorage.setItem('disc_user_name', this.userName);

        // URL에 이름 추가
        const url = new URL(window.location);
        url.searchParams.set('name', this.userName);
        window.history.replaceState(null, null, url);

        this.init();
    }

    renderCurrentQuestion() {
        const question = DISC_QUESTIONS[this.currentQuestion];
        if (!question) {
            this.showResults();
            return;
        }

        // 질문 번호 업데이트
        document.getElementById('currentQuestion').textContent = this.currentQuestion + 1;

        // 진행바 업데이트
        const progress = ((this.currentQuestion + 1) / DISC_QUESTIONS.length) * 100;
        document.getElementById('progress').style.width = progress + '%';

        // 질문 텍스트 업데이트
        document.getElementById('questionText').textContent =
            `다음 중 가장 자신과 비슷한 것과 가장 다른 것을 각각 선택하세요:`;

        // 옵션 렌더링
        this.renderOptions('mostLike', question.options, '가장 비슷한 것');
        this.renderOptions('leastLike', question.options, '가장 다른 것');

        // 이전 답변 복원
        this.restoreAnswers();

        // 네비게이션 버튼 상태 업데이트
        this.updateNavigationButtons();
    }

    renderOptions(type, options, title) {
        const container = document.getElementById(type);
        container.innerHTML = `<h3>${title}</h3>`;

        options.forEach((option, index) => {
            const optionDiv = document.createElement('div');
            optionDiv.className = 'option-item';
            optionDiv.innerHTML = `
                <input type="radio" id="${type}_${this.currentQuestion}_${index}"
                       name="${type}_${this.currentQuestion}" value="${index}">
                <label for="${type}_${this.currentQuestion}_${index}">${option.text}</label>
            `;
            container.appendChild(optionDiv);
        });
    }

    restoreAnswers() {
        const answer = this.answers[this.currentQuestion];
        if (answer) {
            if (answer.mostLike !== undefined) {
                const radio = document.querySelector(`input[name="mostLike_${this.currentQuestion}"][value="${answer.mostLike}"]`);
                if (radio) radio.checked = true;
            }
            if (answer.leastLike !== undefined) {
                const radio = document.querySelector(`input[name="leastLike_${this.currentQuestion}"][value="${answer.leastLike}"]`);
                if (radio) radio.checked = true;
            }
        }
    }

    bindEvents() {
        // 라디오 버튼 변경 이벤트
        document.addEventListener('change', (e) => {
            if (e.target.type === 'radio') {
                this.handleAnswerChange(e);
            }
        });

        // 네비게이션 버튼 이벤트
        document.getElementById('prevBtn').addEventListener('click', () => {
            this.previousQuestion();
        });

        document.getElementById('nextBtn').addEventListener('click', () => {
            this.nextQuestion();
        });
    }

    handleAnswerChange(event) {
        const radio = event.target;
        const name = radio.name;
        const value = parseInt(radio.value);

        if (name.startsWith('mostLike_')) {
            if (!this.answers[this.currentQuestion]) {
                this.answers[this.currentQuestion] = {};
            }
            this.answers[this.currentQuestion].mostLike = value;
        } else if (name.startsWith('leastLike_')) {
            if (!this.answers[this.currentQuestion]) {
                this.answers[this.currentQuestion] = {};
            }
            this.answers[this.currentQuestion].leastLike = value;
        }

        this.checkForConflicts();
        this.updateNavigationButtons();
        this.saveAnswers();
    }

    checkForConflicts() {
        const answer = this.answers[this.currentQuestion];
        if (answer && answer.mostLike === answer.leastLike) {
            this.showConflictWarning();
        } else {
            this.hideConflictWarning();
        }
    }

    showConflictWarning() {
        let warning = document.querySelector('.conflict-warning');
        if (!warning) {
            warning = document.createElement('div');
            warning.className = 'conflict-warning';
            warning.style.cssText = `
                background: #ffebee;
                color: #c62828;
                padding: 10px;
                margin: 10px 0;
                border-radius: 4px;
                border-left: 4px solid #c62828;
            `;
            warning.textContent = '⚠️ 같은 답변을 선택할 수 없습니다. 다른 답변을 선택해주세요.';
            document.querySelector('.question-container').appendChild(warning);
        }
        warning.style.display = 'block';
    }

    hideConflictWarning() {
        const warning = document.querySelector('.conflict-warning');
        if (warning) {
            warning.style.display = 'none';
        }
    }

    updateNavigationButtons() {
        const prevBtn = document.getElementById('prevBtn');
        const nextBtn = document.getElementById('nextBtn');

        // 이전 버튼
        prevBtn.disabled = this.currentQuestion === 0;

        // 다음 버튼
        const answer = this.answers[this.currentQuestion];
        const hasValidAnswer = answer &&
                              answer.mostLike !== undefined &&
                              answer.leastLike !== undefined &&
                              answer.mostLike !== answer.leastLike;

        nextBtn.disabled = !hasValidAnswer;

        // 마지막 질문이면 버튼 텍스트 변경
        if (this.currentQuestion === DISC_QUESTIONS.length - 1) {
            nextBtn.textContent = '결과 보기';
        } else {
            nextBtn.textContent = '다음';
        }
    }

    previousQuestion() {
        if (this.currentQuestion > 0) {
            this.currentQuestion--;
            this.renderCurrentQuestion();
        }
    }

    nextQuestion() {
        const answer = this.answers[this.currentQuestion];

        if (!answer || answer.mostLike === undefined || answer.leastLike === undefined) {
            alert('모든 항목을 선택해주세요.');
            return;
        }

        if (answer.mostLike === answer.leastLike) {
            alert('같은 답변을 선택할 수 없습니다. 다른 답변을 선택해주세요.');
            return;
        }

        if (this.currentQuestion < DISC_QUESTIONS.length - 1) {
            this.currentQuestion++;
            this.renderCurrentQuestion();
        } else {
            this.completeTest();
        }
    }

    completeTest() {
        if (Object.keys(this.answers).length < DISC_QUESTIONS.length) {
            alert('모든 질문에 답변해주세요.');
            return;
        }

        // 결과 계산
        const results = this.calculateResults();

        // 결과 저장
        localStorage.setItem('disc_test_results', JSON.stringify({
            userName: this.userName,
            answers: this.answers,
            results: results,
            completedAt: new Date().toISOString()
        }));

        // 결과 페이지로 이동
        window.location.href = 'result.html';
    }

    calculateResults() {
        const scores = { D: 0, I: 0, S: 0, C: 0 };

        Object.keys(this.answers).forEach(questionIndex => {
            const answer = this.answers[questionIndex];
            const question = DISC_QUESTIONS[parseInt(questionIndex)];

            if (answer.mostLike !== undefined) {
                const mostLikeOption = question.options[answer.mostLike];
                scores[mostLikeOption.type] += 2;
            }

            if (answer.leastLike !== undefined) {
                const leastLikeOption = question.options[answer.leastLike];
                scores[leastLikeOption.type] -= 1;
            }
        });

        // 음수 점수를 0으로 조정
        Object.keys(scores).forEach(type => {
            if (scores[type] < 0) scores[type] = 0;
        });

        // 백분율 계산
        const total = Object.values(scores).reduce((sum, score) => sum + score, 0);
        const percentages = {};

        Object.keys(scores).forEach(type => {
            percentages[type] = total > 0 ? Math.round((scores[type] / total) * 100) : 0;
        });

        // 주요 성격 유형 결정
        const primaryType = Object.keys(percentages).reduce((a, b) =>
            percentages[a] > percentages[b] ? a : b
        );

        return {
            scores: scores,
            percentages: percentages,
            primaryType: primaryType
        };
    }

    saveAnswers() {
        localStorage.setItem('disc_test_progress', JSON.stringify({
            currentQuestion: this.currentQuestion,
            answers: this.answers,
            userName: this.userName
        }));
    }

    loadSavedAnswers() {
        const saved = localStorage.getItem('disc_test_progress');
        if (saved) {
            try {
                const data = JSON.parse(saved);
                if (data.userName === this.userName) {
                    this.currentQuestion = data.currentQuestion || 0;
                    this.answers = data.answers || {};
                }
            } catch (e) {
                console.warn('Could not load saved progress:', e);
            }
        }
    }

    showResults() {
        this.completeTest();
    }
}

// 전역 함수들 (하위 호환성)
function previousQuestion() {
    if (window.staticDiscTest) {
        window.staticDiscTest.previousQuestion();
    }
}

function nextQuestion() {
    if (window.staticDiscTest) {
        window.staticDiscTest.nextQuestion();
    }
}

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    window.staticDiscTest = new StaticDiscTest();
});