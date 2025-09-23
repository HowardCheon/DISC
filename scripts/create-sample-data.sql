-- DISC 프로젝트 샘플 데이터 생성 스크립트
-- 테스트용 사용자 10명과 검사 데이터 생성

-- 기존 데이터 정리 (개발 환경에서만 사용)
-- DELETE FROM survey_responses WHERE survey_id IN (SELECT id FROM surveys);
-- DELETE FROM surveys;
-- DELETE FROM users;

-- 테스트 사용자 10명 생성
INSERT INTO users (name, email, department, position, created_at) VALUES
('김철수', 'kim.cheolsu@company.com', '영업팀', '팀장', datetime('now', '-30 days')),
('이영희', 'lee.younghee@company.com', '마케팅팀', '과장', datetime('now', '-25 days')),
('박민수', 'park.minsu@company.com', '개발팀', '선임연구원', datetime('now', '-20 days')),
('최지윤', 'choi.jiyoon@company.com', '인사팀', '대리', datetime('now', '-18 days')),
('정우성', 'jung.woosung@company.com', '재무팀', '부장', datetime('now', '-15 days')),
('한소영', 'han.soyoung@company.com', '기획팀', '차장', datetime('now', '-12 days')),
('임동욱', 'lim.dongwook@company.com', '영업팀', '사원', datetime('now', '-10 days')),
('윤서진', 'yoon.seojin@company.com', '마케팅팀', '주임', datetime('now', '-8 days')),
('강태우', 'kang.taewoo@company.com', '개발팀', '책임연구원', datetime('now', '-5 days')),
('배수현', 'bae.suhyun@company.com', '고객서비스팀', '팀장', datetime('now', '-3 days'));

-- 관리자 계정 생성
INSERT INTO admins (username, password, name, email, created_at) VALUES
('admin', 'admin123', '시스템관리자', 'admin@company.com', datetime('now', '-30 days')),
('hr_admin', 'hr123', 'HR관리자', 'hr@company.com', datetime('now', '-30 days'));

-- 사용자별 검사 데이터 생성

-- 김철수 - 3회 검사 (D형 주도적 성향)
INSERT INTO surveys (user_id, sent_date, start_date, completed_date, status, d_score, i_score, s_score, c_score, primary_type, description, strengths, weaknesses, recommendations) VALUES
(1, datetime('now', '-25 days'), datetime('now', '-25 days', '+2 hours'), datetime('now', '-25 days', '+2 hours 15 minutes'), 'COMPLETED',
 85, 65, 45, 55, 'D',
 '강한 주도력과 결과 지향적 성향을 보입니다. 도전적인 업무를 선호하며 빠른 의사결정을 내립니다.',
 '강력한 리더십, 목표 달성 의지, 빠른 의사결정',
 '인내심 부족, 세부사항 간과 가능성',
 '팀원들의 의견을 더 많이 듣고, 세심한 계획 수립 필요');

INSERT INTO surveys (user_id, sent_date, start_date, completed_date, status, d_score, i_score, s_score, c_score, primary_type, description, strengths, weaknesses, recommendations) VALUES
(1, datetime('now', '-15 days'), datetime('now', '-15 days', '+1 hours'), datetime('now', '-15 days', '+1 hours 12 minutes'), 'COMPLETED',
 88, 68, 48, 58, 'D',
 '이전보다 더 강화된 주도력을 보이며, 팀 관리 능력이 향상되었습니다.',
 '강력한 리더십, 목표 달성 의지, 팀 동기부여',
 '완벽주의 경향, 스트레스 관리',
 '위임 능력 개발, 스트레스 관리 기법 습득');

INSERT INTO surveys (user_id, sent_date, start_date, completed_date, status, d_score, i_score, s_score, c_score, primary_type, description, strengths, weaknesses, recommendations) VALUES
(1, datetime('now', '-5 days'), datetime('now', '-5 days', '+3 hours'), datetime('now', '-5 days', '+3 hours 18 minutes'), 'COMPLETED',
 82, 72, 52, 62, 'D',
 '균형잡힌 리더십을 보이며, 타인과의 소통 능력이 크게 향상되었습니다.',
 '균형잡힌 리더십, 향상된 소통능력, 전략적 사고',
 '때로는 성급한 판단',
 '장기적 관점에서의 의사결정 훈련');

-- 이영희 - 2회 검사 (I형 사교적 성향)
INSERT INTO surveys (user_id, sent_date, start_date, completed_date, status, d_score, i_score, s_score, c_score, primary_type, description, strengths, weaknesses, recommendations) VALUES
(2, datetime('now', '-20 days'), datetime('now', '-20 days', '+4 hours'), datetime('now', '-20 days', '+4 hours 22 minutes'), 'COMPLETED',
 45, 90, 65, 40, 'I',
 '뛰어난 대인관계 능력과 창의적 아이디어로 팀에 활력을 불어넣습니다.',
 '뛰어난 소통능력, 창의성, 팀워크',
 '집중력 부족, 세부사항 관리 어려움',
 '계획 수립 능력 개발, 집중력 향상 훈련');

INSERT INTO surveys (user_id, sent_date, start_date, completed_date, status, d_score, i_score, s_score, c_score, primary_type, description, strengths, weaknesses, recommendations) VALUES
(2, datetime('now', '-8 days'), datetime('now', '-8 days', '+2 hours'), datetime('now', '-8 days', '+2 hours 28 minutes'), 'COMPLETED',
 48, 88, 68, 45, 'I',
 '지속적으로 높은 사교성을 유지하며, 안정성도 향상되었습니다.',
 '뛰어난 소통능력, 팀 화합, 적응력',
 '완료 시한 관리, 우선순위 설정',
 '시간 관리 기법 습득, 업무 우선순위 설정 훈련');

-- 박민수 - 2회 검사 (C형 신중한 성향)
INSERT INTO surveys (user_id, sent_date, start_date, completed_date, status, d_score, i_score, s_score, c_score, primary_type, description, strengths, weaknesses, recommendations) VALUES
(3, datetime('now', '-18 days'), datetime('now', '-18 days', '+5 hours'), datetime('now', '-18 days', '+5 hours 35 minutes'), 'COMPLETED',
 35, 45, 55, 95, 'C',
 '매우 분석적이고 체계적인 접근을 선호하며, 정확성을 중시합니다.',
 '분석능력, 정확성, 체계적 사고',
 '의사결정 지연, 완벽주의',
 '의사결정 속도 개선, 적절한 완벽주의 수준 조절');

INSERT INTO surveys (user_id, sent_date, start_date, completed_date, status, d_score, i_score, s_score, c_score, primary_type, description, strengths, weaknesses, recommendations) VALUES
(3, datetime('now', '-6 days'), datetime('now', '-6 days', '+1 hours'), datetime('now', '-6 days', '+1 hours 42 minutes'), 'COMPLETED',
 40, 50, 60, 92, 'C',
 '여전히 높은 신중성을 보이며, 대인관계와 안정성이 향상되었습니다.',
 '분석능력, 품질관리, 신뢰성',
 '변화 적응, 빠른 의사결정',
 '변화 관리 교육, 의사결정 프로세스 간소화');

-- 최지윤 - 1회 검사 (S형 안정적 성향)
INSERT INTO surveys (user_id, sent_date, start_date, completed_date, status, d_score, i_score, s_score, c_score, primary_type, description, strengths, weaknesses, recommendations) VALUES
(4, datetime('now', '-12 days'), datetime('now', '-12 days', '+3 hours'), datetime('now', '-12 days', '+3 hours 25 minutes'), 'COMPLETED',
 40, 55, 85, 60, 'S',
 '팀의 조화를 중시하며, 안정적이고 신뢰할 수 있는 업무 처리를 보입니다.',
 '안정성, 신뢰성, 팀워크',
 '변화 저항, 자기주장 부족',
 '리더십 개발, 변화 적응 능력 향상');

-- 정우성 - 1회 검사 (D형)
INSERT INTO surveys (user_id, sent_date, start_date, completed_date, status, d_score, i_score, s_score, c_score, primary_type, description, strengths, weaknesses, recommendations) VALUES
(5, datetime('now', '-10 days'), datetime('now', '-10 days', '+2 hours'), datetime('now', '-10 days', '+2 hours 18 minutes'), 'COMPLETED',
 78, 52, 38, 72, 'D',
 '재무 분야의 전문성과 강한 추진력을 바탕으로 목표를 달성합니다.',
 '전문성, 추진력, 성과 지향',
 '경직성, 타협 부족',
 '유연성 개발, 협상 기술 향상');

-- 한소영 - 1회 검사 (I형)
INSERT INTO surveys (user_id, sent_date, start_date, completed_date, status, d_score, i_score, s_score, c_score, primary_type, description, strengths, weaknesses, recommendations) VALUES
(6, datetime('now', '-8 days'), datetime('now', '-8 days', '+4 hours'), datetime('now', '-8 days', '+4 hours 30 minutes'), 'COMPLETED',
 55, 82, 58, 45, 'I',
 '창의적 기획력과 뛰어난 프레젠테이션 능력을 보유하고 있습니다.',
 '창의성, 프레젠테이션, 기획력',
 '실행력, 지속성',
 '실행 계획 수립, 프로젝트 관리 교육');

-- 진행 중인 검사들
INSERT INTO surveys (user_id, sent_date, start_date, status) VALUES
(7, datetime('now', '-2 days'), datetime('now', '-2 days', '+1 hours'), 'STARTED'),
(8, datetime('now', '-1 days'), NULL, 'SENT'),
(9, datetime('now', '-1 days'), NULL, 'SENT'),
(10, datetime('now'), NULL, 'SENT');

-- 만료된 검사
INSERT INTO surveys (user_id, sent_date, status) VALUES
(7, datetime('now', '-8 days'), 'EXPIRED');

-- 샘플 질문 데이터 (기본 DISC 질문)
INSERT INTO questions (question_text, question_type, question_order) VALUES
('나는 결과를 빠르게 얻는 것을 선호한다', 'D', 1),
('나는 사람들과 함께 일하는 것을 즐긴다', 'I', 2),
('나는 안정적이고 예측 가능한 환경을 선호한다', 'S', 3),
('나는 정확한 정보와 데이터를 중요하게 생각한다', 'C', 4),
('나는 도전적인 목표 설정을 좋아한다', 'D', 5),
('나는 다른 사람들에게 영향력을 행사하는 것을 즐긴다', 'I', 6),
('나는 팀의 화합을 중요하게 생각한다', 'S', 7),
('나는 체계적이고 논리적인 접근을 선호한다', 'C', 8),
('나는 빠른 의사결정을 내리는 편이다', 'D', 9),
('나는 새로운 사람들을 만나는 것을 좋아한다', 'I', 10),
('나는 갈등 상황을 피하려고 한다', 'S', 11),
('나는 모든 세부사항을 확인하고 싶어한다', 'C', 12),
('나는 경쟁적인 환경에서 더 잘 일한다', 'D', 13),
('나는 팀 분위기를 밝게 만드는 역할을 한다', 'I', 14),
('나는 일관성 있는 업무 방식을 선호한다', 'S', 15),
('나는 품질 기준을 높게 설정한다', 'C', 16),
('나는 권한을 가지고 의사결정하는 것을 선호한다', 'D', 17),
('나는 사람들을 설득하는 능력이 뛰어나다', 'I', 18),
('나는 변화보다는 안정성을 추구한다', 'S', 19),
('나는 신중하게 계획을 세우는 편이다', 'C', 20);

-- 샘플 응답 데이터 (완료된 검사에 대해서만)
-- 김철수의 첫 번째 검사 응답
INSERT INTO survey_responses (survey_id, question_id, answer_value) VALUES
-- D형 성향 답변 (1-5점 척도에서 높은 점수)
(1, 1, 5), (1, 5, 5), (1, 9, 4), (1, 13, 5), (1, 17, 4),
-- I형 성향 답변 (중간 점수)
(1, 2, 3), (1, 6, 4), (1, 10, 3), (1, 14, 3), (1, 18, 3),
-- S형 성향 답변 (낮은 점수)
(1, 3, 2), (1, 7, 3), (1, 11, 2), (1, 15, 2), (1, 19, 2),
-- C형 성향 답변 (중간 점수)
(1, 4, 3), (1, 8, 3), (1, 12, 3), (1, 16, 3), (1, 20, 3);

-- 추가 응답 데이터는 필요에 따라 생성...

-- 통계 조회를 위한 뷰 생성
CREATE VIEW survey_statistics AS
SELECT
    DATE(completed_date) as survey_date,
    COUNT(*) as completed_count,
    AVG(d_score) as avg_d_score,
    AVG(i_score) as avg_i_score,
    AVG(s_score) as avg_s_score,
    AVG(c_score) as avg_c_score
FROM surveys
WHERE status = 'COMPLETED'
    AND completed_date IS NOT NULL
GROUP BY DATE(completed_date)
ORDER BY survey_date DESC;

-- 사용자별 통계 뷰
CREATE VIEW user_survey_summary AS
SELECT
    u.id as user_id,
    u.name,
    u.email,
    u.department,
    COUNT(s.id) as total_surveys,
    COUNT(CASE WHEN s.status = 'COMPLETED' THEN 1 END) as completed_surveys,
    COUNT(CASE WHEN s.status = 'SENT' THEN 1 END) as pending_surveys,
    MAX(s.completed_date) as last_completed_date
FROM users u
LEFT JOIN surveys s ON u.id = s.user_id
GROUP BY u.id, u.name, u.email, u.department;

-- 인덱스 생성 (성능 향상)
CREATE INDEX idx_surveys_user_id ON surveys(user_id);
CREATE INDEX idx_surveys_status ON surveys(status);
CREATE INDEX idx_surveys_completed_date ON surveys(completed_date);
CREATE INDEX idx_survey_responses_survey_id ON survey_responses(survey_id);
CREATE INDEX idx_users_email ON users(email);

-- 샘플 데이터 생성 완료 메시지
SELECT 'Sample data creation completed successfully!' as message;
SELECT COUNT(*) as total_users FROM users;
SELECT COUNT(*) as total_surveys FROM surveys;
SELECT status, COUNT(*) as count FROM surveys GROUP BY status;