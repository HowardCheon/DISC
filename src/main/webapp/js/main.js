// DISC Assessment Main JavaScript

// Utility Functions
const DISCUtil = {
    // Format date for display
    formatDate: function(date) {
        if (!(date instanceof Date)) {
            date = new Date(date);
        }
        return date.toLocaleDateString('ko-KR', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    },

    // Generate random token
    generateToken: function(prefix = 'DISC', length = 8) {
        const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
        let result = prefix;
        for (let i = 0; i < length; i++) {
            result += chars.charAt(Math.floor(Math.random() * chars.length));
        }
        return result;
    },

    // Validate form inputs
    validateForm: function(formSelector) {
        const form = document.querySelector(formSelector);
        if (!form) return false;

        const requiredFields = form.querySelectorAll('[required]');
        let isValid = true;

        requiredFields.forEach(field => {
            if (!field.value.trim()) {
                field.classList.add('error');
                isValid = false;
            } else {
                field.classList.remove('error');
            }
        });

        return isValid;
    },

    // Show notification
    showNotification: function(message, type = 'info', duration = 3000) {
        const notification = document.createElement('div');
        notification.className = `notification notification-${type}`;
        notification.textContent = message;
        
        // Apply styles
        Object.assign(notification.style, {
            position: 'fixed',
            top: '20px',
            right: '20px',
            padding: '15px 20px',
            borderRadius: '8px',
            color: 'white',
            fontWeight: '600',
            zIndex: '9999',
            animation: 'slideIn 0.3s ease',
            maxWidth: '300px'
        });

        // Set background color based on type
        const colors = {
            info: '#3498db',
            success: '#27ae60',
            warning: '#f39c12',
            error: '#e74c3c'
        };
        notification.style.backgroundColor = colors[type] || colors.info;

        document.body.appendChild(notification);

        // Auto remove
        setTimeout(() => {
            notification.style.animation = 'slideOut 0.3s ease';
            setTimeout(() => {
                if (notification.parentNode) {
                    notification.parentNode.removeChild(notification);
                }
            }, 300);
        }, duration);
    }
};

// DISC Test Class
class DISCTest {
    constructor() {
        this.currentQuestion = 0;
        this.answers = {};
        this.questions = this.loadQuestions();
        this.scores = { D: 0, I: 0, S: 0, C: 0 };
    }

    loadQuestions() {
        return [
            {
                id: 1,
                text: "다음 중 당신을 가장 잘 설명하는 것은?",
                options: [
                    { text: "결정을 빠르게 내린다", type: "D", value: 3 },
                    { text: "사람들과 함께 있는 것을 좋아한다", type: "I", value: 3 },
                    { text: "신중하고 차분하다", type: "S", value: 3 },
                    { text: "정확성을 중요시한다", type: "C", value: 3 }
                ]
            },
            {
                id: 2,
                text: "압박감이 있는 상황에서 당신은?",
                options: [
                    { text: "도전으로 받아들이고 적극적으로 대응한다", type: "D", value: 3 },
                    { text: "팀원들과 함께 해결책을 찾는다", type: "I", value: 3 },
                    { text: "차근차근 계획을 세워 해결한다", type: "S", value: 3 },
                    { text: "모든 정보를 수집하고 분석한다", type: "C", value: 3 }
                ]
            },
            {
                id: 3,
                text: "새로운 프로젝트를 시작할 때?",
                options: [
                    { text: "즉시 실행에 옮긴다", type: "D", value: 3 },
                    { text: "다른 사람들의 의견을 듣는다", type: "I", value: 3 },
                    { text: "기존 방법을 참고한다", type: "S", value: 3 },
                    { text: "세부 계획을 완벽하게 세운다", type: "C", value: 3 }
                ]
            },
            {
                id: 4,
                text: "회의에서 당신의 역할은?",
                options: [
                    { text: "주도적으로 진행한다", type: "D", value: 3 },
                    { text: "분위기를 밝게 만든다", type: "I", value: 3 },
                    { text: "신중하게 경청한다", type: "S", value: 3 },
                    { text: "구체적인 데이터를 제시한다", type: "C", value: 3 }
                ]
            },
            {
                id: 5,
                text: "갈등 상황에서 당신은?",
                options: [
                    { text: "직접적으로 문제를 해결한다", type: "D", value: 3 },
                    { text: "모든 사람이 만족할 해결책을 찾는다", type: "I", value: 3 },
                    { text: "평화로운 해결을 위해 양보한다", type: "S", value: 3 },
                    { text: "객관적인 근거를 바탕으로 판단한다", type: "C", value: 3 }
                ]
            },
            {
                id: 6,
                text: "업무를 처리할 때?",
                options: [
                    { text: "결과 중심으로 빠르게 처리한다", type: "D", value: 3 },
                    { text: "팀워크를 중시한다", type: "I", value: 3 },
                    { text: "꾸준히 지속적으로 처리한다", type: "S", value: 3 },
                    { text: "정확성과 품질을 중시한다", type: "C", value: 3 }
                ]
            }
        ];
    }

    calculateResult() {
        // Reset scores
        this.scores = { D: 0, I: 0, S: 0, C: 0 };

        // Calculate scores based on answers
        Object.values(this.answers).forEach(answer => {
            const [type, value] = answer.split('_');
            this.scores[type] += parseInt(value);
        });

        // Convert to percentages
        const total = Object.values(this.scores).reduce((a, b) => a + b, 0);
        const percentages = {};
        Object.keys(this.scores).forEach(type => {
            percentages[type] = Math.round((this.scores[type] / total) * 100);
        });

        // Find primary type
        const primaryType = Object.keys(this.scores).reduce((a, b) => 
            this.scores[a] > this.scores[b] ? a : b
        );

        return {
            scores: this.scores,
            percentages: percentages,
            primaryType: primaryType,
            ...this.getTypeDescription(primaryType)
        };
    }

    getTypeDescription(type) {
        const descriptions = {
            D: {
                name: "주도형 (Dominance)",
                description: "결과 지향적이고 결단력이 있으며, 도전을 두려워하지 않습니다. 빠른 결정을 내리고 목표 달성에 집중합니다.",
                strengths: [
                    "강한 리더십과 추진력",
                    "빠른 의사결정 능력",
                    "도전 정신과 혁신적 사고",
                    "결과 중심적 업무 처리",
                    "위기 상황에서의 뛰어난 대응력"
                ],
                developmentAreas: [
                    "타인의 의견을 경청하는 능력",
                    "세부사항에 대한 주의 깊은 검토",
                    "인내심과 참을성 기르기",
                    "팀원들과의 협력적 소통",
                    "감정적 배려와 공감 능력"
                ],
                communicationStyle: "직접적이고 간결한 소통을 선호하며, 핵심을 빠르게 파악하고 전달합니다. 목적과 결과에 집중한 대화를 좋아합니다.",
                workEnvironment: "자율성이 보장되고 도전적인 업무를 할 수 있는 환경을 선호합니다. 빠른 변화와 혁신이 가능한 조직에서 역량을 발휘합니다."
            },
            I: {
                name: "사교형 (Influence)",
                description: "사람들과의 관계를 중시하고 긍정적이며 활발합니다. 팀워크를 통해 목표를 달성하고 주변 사람들에게 영감을 줍니다.",
                strengths: [
                    "뛰어난 대인관계 능력",
                    "긍정적이고 밝은 에너지",
                    "팀 동기부여와 영감 제공",
                    "창의적이고 혁신적인 아이디어",
                    "변화에 대한 적응력"
                ],
                developmentAreas: [
                    "세부사항에 대한 체계적 관리",
                    "일관성 있는 업무 수행",
                    "객관적 분석과 비판적 사고",
                    "시간 관리와 우선순위 설정",
                    "혼자 집중해서 하는 업무 능력"
                ],
                communicationStyle: "열정적이고 표현력이 풍부한 소통을 합니다. 사람들과의 관계를 통해 정보를 교환하고 감정적 연결을 중시합니다.",
                workEnvironment: "사람들과 함께 일할 수 있고 창의성을 발휘할 수 있는 환경을 선호합니다. 자유로운 분위기와 다양한 사람들과의 교류가 가능한 조직을 좋아합니다."
            },
            S: {
                name: "안정형 (Steadiness)",
                description: "안정성과 조화를 중시하며 꾸준하고 신뢰할 수 있습니다. 팀의 화합을 도모하고 일관성 있는 성과를 만들어냅니다.",
                strengths: [
                    "높은 신뢰성과 일관성",
                    "팀 화합과 협력 촉진",
                    "차분하고 안정적인 업무 처리",
                    "타인에 대한 배려와 지원",
                    "지속적이고 꾸준한 노력"
                ],
                developmentAreas: [
                    "변화에 대한 적응력 향상",
                    "적극적인 의견 표현",
                    "새로운 도전에 대한 개방성",
                    "빠른 의사결정 능력",
                    "자기주장과 리더십 개발"
                ],
                communicationStyle: "차분하고 경청 중심의 소통을 합니다. 상대방의 입장을 이해하려 노력하고 갈등을 피하는 평화로운 대화를 선호합니다.",
                workEnvironment: "안정적이고 예측 가능한 업무 환경을 선호합니다. 팀워크가 중시되고 점진적인 변화가 이루어지는 조직에서 최고의 성과를 발휘합니다."
            },
            C: {
                name: "신중형 (Conscientiousness)",
                description: "정확성과 품질을 중시하며 체계적이고 분석적입니다. 완벽한 결과를 위해 세심한 검토와 계획을 통해 업무를 수행합니다.",
                strengths: [
                    "뛰어난 분석력과 문제 해결 능력",
                    "높은 품질과 정확성 추구",
                    "체계적이고 계획적인 업무 처리",
                    "객관적이고 논리적인 판단",
                    "전문성과 기술적 역량"
                ],
                developmentAreas: [
                    "빠른 의사결정과 실행력",
                    "유연성과 적응력 향상",
                    "대인관계와 소통 능력",
                    "완벽주의 성향 조절",
                    "위험 감수와 도전 정신"
                ],
                communicationStyle: "사실과 데이터에 기반한 논리적 소통을 선호합니다. 구체적이고 정확한 정보 전달을 중시하며 신중한 대화를 합니다.",
                workEnvironment: "전문성을 발휘할 수 있고 품질을 중시하는 환경을 선호합니다. 체계적인 프로세스와 명확한 기준이 있는 조직에서 역량을 최대화합니다."
            }
        };

        return descriptions[type] || descriptions.D;
    }
}

// Admin Dashboard Functions
const AdminDashboard = {
    // Initialize dashboard
    init: function() {
        this.bindEvents();
        this.loadStats();
        this.startAutoRefresh();
    },

    // Bind event listeners
    bindEvents: function() {
        // Copy token buttons
        document.querySelectorAll('.btn-copy').forEach(btn => {
            btn.addEventListener('click', this.copyToClipboard);
        });

        // Delete link buttons
        document.querySelectorAll('.btn-danger').forEach(btn => {
            if (btn.textContent.includes('삭제')) {
                btn.addEventListener('click', this.confirmDelete);
            }
        });
    },

    // Copy text to clipboard
    copyToClipboard: function(event) {
        const button = event.target;
        const tokenElement = button.previousElementSibling;
        const token = tokenElement.textContent;

        navigator.clipboard.writeText(token).then(() => {
            DISCUtil.showNotification('토큰이 복사되었습니다: ' + token, 'success');
        }).catch(() => {
            DISCUtil.showNotification('복사에 실패했습니다.', 'error');
        });
    },

    // Confirm delete action
    confirmDelete: function(event) {
        event.preventDefault();
        const confirmed = confirm('정말로 삭제하시겠습니까?');
        if (confirmed) {
            // Implement delete logic here
            DISCUtil.showNotification('삭제되었습니다.', 'success');
        }
    },

    // Load dashboard statistics
    loadStats: function() {
        // This would typically make an AJAX call to get real-time stats
        // For now, we'll simulate with sample data
        console.log('Loading dashboard statistics...');
    },

    // Start auto-refresh for real-time updates
    startAutoRefresh: function() {
        setInterval(() => {
            this.loadStats();
        }, 30000); // Refresh every 30 seconds
    }
};

// Form Validation
const FormValidator = {
    // Initialize form validation
    init: function() {
        this.bindValidation();
    },

    // Bind validation to forms
    bindValidation: function() {
        document.querySelectorAll('form').forEach(form => {
            form.addEventListener('submit', this.validateOnSubmit);
            
            // Real-time validation
            form.querySelectorAll('input, select, textarea').forEach(field => {
                field.addEventListener('blur', this.validateField);
                field.addEventListener('input', this.clearError);
            });
        });
    },

    // Validate form on submit
    validateOnSubmit: function(event) {
        const form = event.target;
        let isValid = true;

        // Check required fields
        form.querySelectorAll('[required]').forEach(field => {
            if (!FormValidator.validateField({ target: field })) {
                isValid = false;
            }
        });

        // Check specific validations
        form.querySelectorAll('input[type="email"]').forEach(field => {
            if (field.value && !FormValidator.isValidEmail(field.value)) {
                FormValidator.showFieldError(field, '올바른 이메일 주소를 입력하세요.');
                isValid = false;
            }
        });

        if (!isValid) {
            event.preventDefault();
            DISCUtil.showNotification('입력 정보를 확인해주세요.', 'error');
        }
    },

    // Validate individual field
    validateField: function(event) {
        const field = event.target;
        
        if (field.required && !field.value.trim()) {
            FormValidator.showFieldError(field, '필수 입력 항목입니다.');
            return false;
        }

        if (field.type === 'email' && field.value && !FormValidator.isValidEmail(field.value)) {
            FormValidator.showFieldError(field, '올바른 이메일 주소를 입력하세요.');
            return false;
        }

        FormValidator.clearFieldError(field);
        return true;
    },

    // Clear error on input
    clearError: function(event) {
        FormValidator.clearFieldError(event.target);
    },

    // Show field error
    showFieldError: function(field, message) {
        field.classList.add('error');
        
        // Remove existing error message
        const existingError = field.parentNode.querySelector('.error-text');
        if (existingError) {
            existingError.remove();
        }

        // Add new error message
        const errorElement = document.createElement('div');
        errorElement.className = 'error-text';
        errorElement.textContent = message;
        errorElement.style.color = '#e74c3c';
        errorElement.style.fontSize = '0.9em';
        errorElement.style.marginTop = '5px';
        
        field.parentNode.appendChild(errorElement);
    },

    // Clear field error
    clearFieldError: function(field) {
        field.classList.remove('error');
        const errorElement = field.parentNode.querySelector('.error-text');
        if (errorElement) {
            errorElement.remove();
        }
    },

    // Email validation
    isValidEmail: function(email) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    }
};

// Progress Bar Animation
const ProgressBar = {
    animate: function(element, targetWidth, duration = 1000) {
        if (!element) return;

        let startWidth = 0;
        const startTime = performance.now();

        function animateStep(currentTime) {
            const elapsed = currentTime - startTime;
            const progress = Math.min(elapsed / duration, 1);
            
            const currentWidth = startWidth + (targetWidth - startWidth) * progress;
            element.style.width = currentWidth + '%';

            if (progress < 1) {
                requestAnimationFrame(animateStep);
            }
        }

        requestAnimationFrame(animateStep);
    }
};

// Local Storage Manager
const StorageManager = {
    // Save data to localStorage
    save: function(key, data) {
        try {
            localStorage.setItem(key, JSON.stringify(data));
            return true;
        } catch (error) {
            console.error('Error saving to localStorage:', error);
            return false;
        }
    },

    // Load data from localStorage
    load: function(key) {
        try {
            const data = localStorage.getItem(key);
            return data ? JSON.parse(data) : null;
        } catch (error) {
            console.error('Error loading from localStorage:', error);
            return null;
        }
    },

    // Remove data from localStorage
    remove: function(key) {
        try {
            localStorage.removeItem(key);
            return true;
        } catch (error) {
            console.error('Error removing from localStorage:', error);
            return false;
        }
    },

    // Clear all DISC-related data
    clearAll: function() {
        const keys = Object.keys(localStorage);
        keys.forEach(key => {
            if (key.startsWith('disc_')) {
                localStorage.removeItem(key);
            }
        });
    }
};

// Initialize everything when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    // Initialize form validation
    FormValidator.init();

    // Initialize admin dashboard if on admin page
    if (document.querySelector('.admin-container')) {
        AdminDashboard.init();
    }

    // Initialize progress bar animations if present
    document.querySelectorAll('.bar-fill').forEach(bar => {
        const targetWidth = parseFloat(bar.style.width) || 0;
        bar.style.width = '0%';
        setTimeout(() => {
            ProgressBar.animate(bar, targetWidth, 1500);
        }, 500);
    });

    // Add smooth scrolling for anchor links
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            const target = document.querySelector(this.getAttribute('href'));
            if (target) {
                target.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });

    // Add print functionality
    const printButtons = document.querySelectorAll('[onclick*="print"]');
    printButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            window.print();
        });
    });
});

// CSS Animations
const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from { transform: translateX(100%); opacity: 0; }
        to { transform: translateX(0); opacity: 1; }
    }
    
    @keyframes slideOut {
        from { transform: translateX(0); opacity: 1; }
        to { transform: translateX(100%); opacity: 0; }
    }
    
    .error {
        border-color: #e74c3c !important;
        box-shadow: 0 0 0 3px rgba(231, 76, 60, 0.1) !important;
    }
`;
document.head.appendChild(style);