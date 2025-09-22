/**
 * DISC Result Page JavaScript
 * Handles chart rendering, animations, and print functionality
 */

class DiscResultCharts {
    constructor() {
        this.chartData = window.chartData || {};
        this.resultData = window.resultData || {};
        this.radarChart = null;
        this.barChart = null;

        this.init();
    }

    init() {
        // Wait for DOM to be fully loaded
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', () => this.initializeCharts());
        } else {
            this.initializeCharts();
        }

        // Initialize other features
        this.initializeAnimations();
        this.initializePrintFeatures();
    }

    initializeCharts() {
        try {
            this.createRadarChart();
            this.createBarChart();
            this.animateScoreBars();

            console.log('Charts initialized successfully');
        } catch (error) {
            console.error('Error initializing charts:', error);
            this.showChartError();
        }
    }

    /**
     * Create radar chart showing DISC personality profile
     */
    createRadarChart() {
        const ctx = document.getElementById('radarChart');
        if (!ctx) {
            console.error('Radar chart canvas not found');
            return;
        }

        const scores = this.resultData.scores || {};
        const data = [scores.D || 0, scores.I || 0, scores.S || 0, scores.C || 0];
        const maxScore = Math.max(...data, 28); // Ensure minimum scale

        this.radarChart = new Chart(ctx, {
            type: 'radar',
            data: {
                labels: ['D (주도형)', 'I (사교형)', 'S (안정형)', 'C (신중형)'],
                datasets: [{
                    label: this.resultData.userName + '님의 DISC 점수',
                    data: data,
                    backgroundColor: 'rgba(102, 126, 234, 0.2)',
                    borderColor: 'rgba(102, 126, 234, 0.8)',
                    borderWidth: 3,
                    pointBackgroundColor: [
                        '#e74c3c',  // D - Red
                        '#f39c12',  // I - Orange
                        '#27ae60',  // S - Green
                        '#3498db'   // C - Blue
                    ],
                    pointBorderColor: '#fff',
                    pointBorderWidth: 2,
                    pointRadius: 8,
                    pointHoverRadius: 10,
                    pointHoverBackgroundColor: [
                        '#c0392b',
                        '#e67e22',
                        '#229954',
                        '#2980b9'
                    ]
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: true,
                        position: 'bottom',
                        labels: {
                            font: {
                                size: 14,
                                weight: '600'
                            },
                            color: '#2c3e50',
                            padding: 20
                        }
                    },
                    tooltip: {
                        backgroundColor: 'rgba(0, 0, 0, 0.8)',
                        titleColor: '#fff',
                        bodyColor: '#fff',
                        borderColor: '#667eea',
                        borderWidth: 1,
                        cornerRadius: 8,
                        displayColors: true,
                        callbacks: {
                            label: function(context) {
                                const percentage = ((context.raw / 28) * 100).toFixed(1);
                                return `${context.raw}점 (${percentage}%)`;
                            }
                        }
                    }
                },
                scales: {
                    r: {
                        min: 0,
                        max: Math.max(maxScore, 28),
                        beginAtZero: true,
                        grid: {
                            color: 'rgba(0, 0, 0, 0.1)'
                        },
                        angleLines: {
                            color: 'rgba(0, 0, 0, 0.1)'
                        },
                        pointLabels: {
                            font: {
                                size: 14,
                                weight: '600'
                            },
                            color: '#2c3e50'
                        },
                        ticks: {
                            stepSize: 7,
                            font: {
                                size: 12
                            },
                            color: '#7f8c8d',
                            backdropColor: 'transparent'
                        }
                    }
                },
                animation: {
                    duration: 2000,
                    easing: 'easeInOutQuart'
                }
            }
        });
    }

    /**
     * Create bar chart showing DISC scores
     */
    createBarChart() {
        const ctx = document.getElementById('barChart');
        if (!ctx) {
            console.error('Bar chart canvas not found');
            return;
        }

        const scores = this.resultData.scores || {};
        const data = [scores.D || 0, scores.I || 0, scores.S || 0, scores.C || 0];

        this.barChart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: ['D (주도형)', 'I (사교형)', 'S (안정형)', 'C (신중형)'],
                datasets: [{
                    label: '점수',
                    data: data,
                    backgroundColor: [
                        'rgba(231, 76, 60, 0.8)',   // D - Red
                        'rgba(243, 156, 18, 0.8)',  // I - Orange
                        'rgba(39, 174, 96, 0.8)',   // S - Green
                        'rgba(52, 152, 219, 0.8)'   // C - Blue
                    ],
                    borderColor: [
                        'rgba(231, 76, 60, 1)',
                        'rgba(243, 156, 18, 1)',
                        'rgba(39, 174, 96, 1)',
                        'rgba(52, 152, 219, 1)'
                    ],
                    borderWidth: 2,
                    borderRadius: 8,
                    borderSkipped: false
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    },
                    tooltip: {
                        backgroundColor: 'rgba(0, 0, 0, 0.8)',
                        titleColor: '#fff',
                        bodyColor: '#fff',
                        borderColor: '#667eea',
                        borderWidth: 1,
                        cornerRadius: 8,
                        callbacks: {
                            label: function(context) {
                                const percentage = ((context.raw / 28) * 100).toFixed(1);
                                return `${context.raw}점 (${percentage}%)`;
                            }
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        max: 28,
                        ticks: {
                            stepSize: 7,
                            font: {
                                size: 12,
                                weight: '500'
                            },
                            color: '#7f8c8d'
                        },
                        grid: {
                            color: 'rgba(0, 0, 0, 0.1)',
                            drawBorder: false
                        }
                    },
                    x: {
                        ticks: {
                            font: {
                                size: 12,
                                weight: '600'
                            },
                            color: '#2c3e50'
                        },
                        grid: {
                            display: false
                        }
                    }
                },
                animation: {
                    duration: 2000,
                    easing: 'easeInOutQuart'
                }
            }
        });
    }

    /**
     * Animate score progress bars
     */
    animateScoreBars() {
        const scoreFills = document.querySelectorAll('.score-fill');

        // Reset widths first
        scoreFills.forEach(fill => {
            fill.style.width = '0%';
        });

        // Animate after a short delay
        setTimeout(() => {
            scoreFills.forEach((fill, index) => {
                const targetWidth = fill.style.width;
                fill.style.width = targetWidth;

                // Add pulse effect for the highest score
                const scores = Object.values(this.resultData.scores || {});
                const maxScore = Math.max(...scores);
                const currentScore = scores[index];

                if (currentScore === maxScore && currentScore > 0) {
                    fill.classList.add('highest-score');
                    this.addPulseEffect(fill);
                }
            });
        }, 500);
    }

    /**
     * Add pulse effect to element
     */
    addPulseEffect(element) {
        const style = document.createElement('style');
        style.textContent = `
            .highest-score {
                animation: pulse 2s infinite;
            }

            @keyframes pulse {
                0% { box-shadow: 0 0 0 0 rgba(102, 126, 234, 0.7); }
                70% { box-shadow: 0 0 0 10px rgba(102, 126, 234, 0); }
                100% { box-shadow: 0 0 0 0 rgba(102, 126, 234, 0); }
            }
        `;
        document.head.appendChild(style);
    }

    /**
     * Initialize page animations
     */
    initializeAnimations() {
        // Fade in animation for sections
        const sections = document.querySelectorAll('.chart-container, .type-description, .career-section');

        const observerOptions = {
            threshold: 0.1,
            rootMargin: '0px 0px -50px 0px'
        };

        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.style.opacity = '1';
                    entry.target.style.transform = 'translateY(0)';
                }
            });
        }, observerOptions);

        sections.forEach(section => {
            section.style.opacity = '0';
            section.style.transform = 'translateY(30px)';
            section.style.transition = 'all 0.6s ease-out';
            observer.observe(section);
        });

        // Animate career items on hover
        this.initializeCareerAnimations();
    }

    /**
     * Initialize career item animations
     */
    initializeCareerAnimations() {
        const careerItems = document.querySelectorAll('.career-item');

        careerItems.forEach((item, index) => {
            // Stagger animation
            setTimeout(() => {
                item.style.opacity = '1';
                item.style.transform = 'translateY(0)';
            }, index * 100);

            // Add hover effects
            item.addEventListener('mouseenter', () => {
                item.style.boxShadow = '0 10px 30px rgba(102, 126, 234, 0.3)';
            });

            item.addEventListener('mouseleave', () => {
                item.style.boxShadow = 'none';
            });
        });
    }

    /**
     * Initialize print functionality
     */
    initializePrintFeatures() {
        // Add print styles
        this.addPrintStyles();

        // Handle before print event
        window.addEventListener('beforeprint', () => {
            this.preparePrint();
        });

        // Handle after print event
        window.addEventListener('afterprint', () => {
            this.restoreAfterPrint();
        });
    }

    /**
     * Add print-specific styles
     */
    addPrintStyles() {
        const printStyles = document.createElement('style');
        printStyles.media = 'print';
        printStyles.textContent = `
            @media print {
                * {
                    -webkit-print-color-adjust: exact !important;
                    color-adjust: exact !important;
                }

                body {
                    background: white !important;
                    font-size: 12pt !important;
                }

                .result-container {
                    background: white !important;
                    box-shadow: none !important;
                    max-width: none !important;
                    padding: 0 !important;
                }

                .result-header {
                    background: #667eea !important;
                    color: white !important;
                    print-color-adjust: exact !important;
                }

                .chart-container {
                    page-break-inside: avoid;
                    margin-bottom: 20px;
                }

                .type-description {
                    page-break-inside: avoid;
                    margin-bottom: 20px;
                }

                .career-section {
                    page-break-inside: avoid;
                }

                .action-buttons {
                    display: none !important;
                }

                .charts-section {
                    grid-template-columns: 1fr !important;
                }

                .details-grid {
                    grid-template-columns: 1fr !important;
                }

                .career-grid {
                    grid-template-columns: repeat(2, 1fr) !important;
                }
            }
        `;
        document.head.appendChild(printStyles);
    }

    /**
     * Prepare charts for printing
     */
    preparePrint() {
        // Convert charts to static images for better printing
        try {
            if (this.radarChart) {
                const radarCanvas = this.radarChart.canvas;
                const radarImg = document.createElement('img');
                radarImg.src = radarCanvas.toDataURL('image/png');
                radarImg.style.width = '100%';
                radarImg.style.height = 'auto';
                radarCanvas.style.display = 'none';
                radarCanvas.parentNode.appendChild(radarImg);
                radarImg.id = 'radar-print-img';
            }

            if (this.barChart) {
                const barCanvas = this.barChart.canvas;
                const barImg = document.createElement('img');
                barImg.src = barCanvas.toDataURL('image/png');
                barImg.style.width = '100%';
                barImg.style.height = 'auto';
                barCanvas.style.display = 'none';
                barCanvas.parentNode.appendChild(barImg);
                barImg.id = 'bar-print-img';
            }
        } catch (error) {
            console.warn('Could not convert charts to images for printing:', error);
        }
    }

    /**
     * Restore charts after printing
     */
    restoreAfterPrint() {
        // Remove temporary images and restore canvases
        const radarImg = document.getElementById('radar-print-img');
        const barImg = document.getElementById('bar-print-img');

        if (radarImg) {
            radarImg.remove();
            if (this.radarChart) {
                this.radarChart.canvas.style.display = 'block';
            }
        }

        if (barImg) {
            barImg.remove();
            if (this.barChart) {
                this.barChart.canvas.style.display = 'block';
            }
        }
    }

    /**
     * Show error message if charts fail to load
     */
    showChartError() {
        const chartContainers = document.querySelectorAll('.chart-container canvas');

        chartContainers.forEach(canvas => {
            const errorDiv = document.createElement('div');
            errorDiv.className = 'chart-error';
            errorDiv.innerHTML = `
                <div style="text-align: center; padding: 40px; color: #7f8c8d;">
                    <i style="font-size: 3em; margin-bottom: 15px;">📊</i>
                    <p>차트를 불러올 수 없습니다.</p>
                    <p style="font-size: 0.9em;">브라우저를 새로고침해 주세요.</p>
                </div>
            `;

            canvas.parentNode.replaceChild(errorDiv, canvas);
        });
    }

    /**
     * Destroy charts (cleanup)
     */
    destroy() {
        if (this.radarChart) {
            this.radarChart.destroy();
            this.radarChart = null;
        }

        if (this.barChart) {
            this.barChart.destroy();
            this.barChart = null;
        }
    }
}

/**
 * Additional utility functions for result page
 */
const ResultUtils = {
    /**
     * Format percentage with proper styling
     */
    formatPercentage: function(value) {
        return `${Math.round(value)}%`;
    },

    /**
     * Get type color by DISC type
     */
    getTypeColor: function(type) {
        const colors = {
            'D': '#e74c3c',
            'I': '#f39c12',
            'S': '#27ae60',
            'C': '#3498db'
        };
        return colors[type.toUpperCase()] || '#95a5a6';
    },

    /**
     * Copy result URL to clipboard
     */
    copyResultUrl: function() {
        const url = window.location.href;

        if (navigator.clipboard && window.isSecureContext) {
            navigator.clipboard.writeText(url).then(() => {
                this.showNotification('결과 URL이 클립보드에 복사되었습니다.', 'success');
            }).catch(() => {
                this.fallbackCopyToClipboard(url);
            });
        } else {
            this.fallbackCopyToClipboard(url);
        }
    },

    /**
     * Fallback copy method for older browsers
     */
    fallbackCopyToClipboard: function(text) {
        const textArea = document.createElement('textarea');
        textArea.value = text;
        textArea.style.position = 'fixed';
        textArea.style.opacity = '0';
        document.body.appendChild(textArea);
        textArea.focus();
        textArea.select();

        try {
            document.execCommand('copy');
            this.showNotification('결과 URL이 클립보드에 복사되었습니다.', 'success');
        } catch (err) {
            this.showNotification('URL 복사에 실패했습니다.', 'error');
        }

        document.body.removeChild(textArea);
    },

    /**
     * Show notification message
     */
    showNotification: function(message, type = 'info') {
        const notification = document.createElement('div');
        notification.className = `notification notification-${type}`;
        notification.textContent = message;

        notification.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            background: ${type === 'success' ? '#27ae60' : type === 'error' ? '#e74c3c' : '#3498db'};
            color: white;
            padding: 15px 20px;
            border-radius: 8px;
            z-index: 10000;
            font-weight: 500;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
            animation: slideInRight 0.3s ease-out;
        `;

        document.body.appendChild(notification);

        setTimeout(() => {
            notification.style.animation = 'slideOutRight 0.3s ease-in forwards';
            setTimeout(() => {
                if (notification.parentNode) {
                    notification.parentNode.removeChild(notification);
                }
            }, 300);
        }, 3000);
    }
};

// Add notification animations
const notificationStyles = document.createElement('style');
notificationStyles.textContent = `
    @keyframes slideInRight {
        from {
            transform: translateX(100%);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }

    @keyframes slideOutRight {
        from {
            transform: translateX(0);
            opacity: 1;
        }
        to {
            transform: translateX(100%);
            opacity: 0;
        }
    }
`;
document.head.appendChild(notificationStyles);

// Initialize result charts when page loads
let discResultCharts;

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
        discResultCharts = new DiscResultCharts();
    });
} else {
    discResultCharts = new DiscResultCharts();
}

// Cleanup on page unload
window.addEventListener('beforeunload', () => {
    if (discResultCharts) {
        discResultCharts.destroy();
    }
});

// Export for global access
window.DiscResultCharts = DiscResultCharts;
window.ResultUtils = ResultUtils;