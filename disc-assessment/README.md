# DISC 성격검사 웹 애플리케이션

DISC 성격 유형 진단을 위한 엔터프라이즈급 웹 기반 애플리케이션입니다. 관리자가 검사 링크를 생성하고, 사용자가 온라인으로 검사를 완료한 후 상세한 결과 분석을 제공합니다.

## 🆕 최신 업데이트 (v2.0)

### 새로운 기능
- ✅ **고급 링크 관리**: 개별/일괄 링크 생성, 자동완성, 중복 처리
- ✅ **실시간 대시보드**: Chart.js 기반 시각화 및 통계
- ✅ **강화된 보안**: XSS/CSRF 방지, SQL Injection 보안
- ✅ **반응형 디자인**: 모바일/태블릿 완벽 지원
- ✅ **에러 처리**: 사용자 친화적 에러 페이지

## 🎯 주요 기능

### 📊 사용자 기능
- **개인별 맞춤 검사**: 고유 링크를 통한 개인화된 검사 진행
- **28문항 DISC 검사**: 신뢰성 있는 DISC 이론 기반 질문
- **상세한 결과 분석**:
  - Chart.js 기반 레이더 차트 및 막대 그래프
  - 4가지 유형별 점수 및 백분율
  - 주요 유형에 대한 한글 설명 (특징, 강점, 개발영역)
  - 추천 직업 및 역할 가이드
- **결과 인쇄**: 깔끔한 PDF 형태 인쇄 기능

### 🔧 관리자 기능
- **보안 로그인**: SHA-256 암호화, 세션 타임아웃 관리, CSRF 보호
- **고급 링크 관리**:
  - 개별 링크 생성 (자동완성 지원)
  - 일괄 링크 생성 (최대 100명)
  - 중복 사용자 확인 및 재발송
  - 실시간 생성 이력 조회
  - 원클릭 URL 복사
- **실시간 대시보드**:
  - 오늘 발송된 링크 수
  - 완료율 통계 및 트렌드 분석
  - DISC 유형 분포 차트
  - 실시간 활동 피드
- **종합 통계**: 일간/주간/월간 성과 분석

## 🏗️ 기술 스택

### Backend
- **Java 17+** - 핵심 개발 언어
- **Maven** - 빌드 및 의존성 관리
- **Servlet/JSP** - 웹 애플리케이션 프레임워크
- **SQLite** - 경량 데이터베이스
- **Apache Commons** - 유틸리티 라이브러리

### Frontend
- **Bootstrap 5** - 반응형 UI 프레임워크
- **Chart.js** - 데이터 시각화
- **JavaScript ES6+** - 인터랙티브 기능
- **CSS3** - 모던 스타일링

### 보안
- **다층 보안 필터** - XSS, CSRF, SQL Injection 방지
- **세션 보안** - HttpOnly 쿠키, 세션 타임아웃
- **SHA-256 암호화** - 비밀번호 해싱
- **입력값 검증** - SecurityUtil을 통한 안전한 입력 처리
- **에러 처리** - 정보 노출 방지를 위한 안전한 에러 페이지

## 📋 요구사항

- **JDK 17+**
- **Apache Tomcat 9.0+**
- **Maven 3.6+**
- **모던 웹 브라우저** (Chrome, Firefox, Safari, Edge)

## 🚀 빠른 시작 가이드

### 1. 환경 준비
```bash
# JDK 17+ 설치 확인
java -version

# Maven 설치 확인
mvn -version

# Tomcat 9+ 다운로드 및 설치
# https://tomcat.apache.org/download-90.cgi
```

### 2. 저장소 클론 및 빌드
```bash
git clone https://github.com/HowardCheon/DISC.git
cd DISC/disc-assessment

# 프로젝트 빌드
mvn clean compile
mvn package
```

### 3. Tomcat 배포
```bash
# 방법 1: WAR 파일 복사 배포
cp target/disc-assessment.war $TOMCAT_HOME/webapps/

# 방법 2: Maven을 통한 직접 배포 (개발 환경)
mvn tomcat7:deploy

# Tomcat 시작
$TOMCAT_HOME/bin/startup.sh  # Linux/Mac
$TOMCAT_HOME/bin/startup.bat # Windows
```

### 4. 접속 및 초기 설정
```bash
# 브라우저에서 접속
http://localhost:8080/disc-assessment

# 관리자 로그인
- URL: http://localhost:8080/disc-assessment/admin/login
- 기본 계정: admin / admin1234
- ⚠️ 보안상 첫 로그인 후 비밀번호를 반드시 변경하세요
```

### 5. 데이터베이스 초기화
- ✅ SQLite 데이터베이스가 자동으로 생성됩니다
- 📍 DB 파일 위치: `webapps/disc-assessment/WEB-INF/database/`
- 🔄 초기 데이터는 애플리케이션 시작 시 자동 생성됩니다

## 📖 사용 가이드

### 관리자 워크플로우
1. **로그인**: `/admin/login`에서 관리자 계정으로 로그인
2. **대시보드 확인**: 실시간 통계 및 활동 모니터링
3. **링크 생성**:
   - 개별 생성: 자동완성으로 빠른 입력
   - 일괄 생성: 최대 100명까지 한번에
   - 중복 확인: 기존 사용자 재발송 여부 선택
4. **링크 관리**: 생성 이력 조회 및 URL 복사
5. **결과 모니터링**: 완료된 검사 결과 및 통계 확인

### 사용자 워크플로우
1. **링크 접속**: 관리자가 제공한 개인 링크 클릭
2. **개인정보 입력**: 이름 입력 및 검사 시작
3. **검사 진행**: 28개 문항에 대한 응답 (약 5-10분 소요)
4. **결과 확인**: 상세한 DISC 분석 결과 검토
5. **결과 저장**: 인쇄 또는 스크린샷으로 결과 보관

## 📊 DISC 유형 설명

### D형 (주도형) - Dominance 💪
- **특징**: 결과 지향적, 결단력, 도전 정신
- **강점**: 강한 리더십, 빠른 의사결정, 목표 지향적 사고
- **적합 직업**: CEO, 영업 관리자, 프로젝트 매니저

### I형 (사교형) - Influence 🌟
- **특징**: 사람 중심적, 긍정적, 활발함
- **강점**: 대인관계 능력, 팀 동기부여, 창의적 아이디어
- **적합 직업**: 마케팅 전문가, 홍보 담당자, 교육 트레이너

### S형 (안정형) - Steadiness 🤝
- **특징**: 안정성 추구, 조화 중시, 신뢰성
- **강점**: 팀 화합 촉진, 일관성 있는 업무, 타인 지원
- **적합 직업**: 간호사, 교사, 사회복지사, 고객서비스

### C형 (신중형) - Conscientiousness 🔍
- **특징**: 정확성 추구, 체계적, 분석적
- **강점**: 뛰어난 분석력, 높은 품질 추구, 전문성
- **적합 직업**: 회계사, 엔지니어, 연구원, 품질관리

## 🗄️ 데이터베이스 구조

### 주요 테이블
- **users**: 사용자 정보 관리
- **test_links**: 개별 검사 링크 및 상태
- **answers**: 사용자 응답 데이터
- **results**: 계산된 DISC 점수 및 유형
- **admins**: 관리자 계정 정보

## 🔧 개발 환경 설정

### IDE 설정 (IntelliJ IDEA 권장)
1. **프로젝트 Import**:
   ```bash
   File → Open → DISC/disc-assessment/pom.xml
   ```

2. **JDK 설정**:
   ```bash
   File → Project Structure → Project → SDK → 17+
   ```

3. **Tomcat 서버 설정**:
   ```bash
   Run → Edit Configurations → + → Tomcat Server → Local
   - Application Server: Tomcat 9.0+
   - Deployment: Add Artifact → disc-assessment:war exploded
   ```

4. **개발 모드 실행**:
   ```bash
   # Maven을 통한 빠른 실행
   mvn tomcat7:run

   # 또는 IDE에서 Tomcat 서버 실행
   Run → Run 'Tomcat'
   ```

### 코드 품질 도구
```bash
# 코드 스타일 검사
mvn checkstyle:check

# 보안 취약점 스캔
mvn org.owasp:dependency-check-maven:check

# 테스트 실행 (구현 시)
mvn test
```

## 📈 성능 최적화

- **데이터베이스 연결 풀링**: 효율적인 DB 연결 관리
- **세션 최적화**: 불필요한 세션 데이터 최소화
- **정적 리소스 캐싱**: CSS/JS 파일 브라우저 캐싱
- **Chart.js 지연 로딩**: 필요시에만 차트 렌더링

## 🛡️ 보안 기능

### 다층 보안 아키텍처
- **인증 & 인가**:
  - AdminAuthFilter를 통한 관리자 접근 제어
  - 세션 기반 인증, HttpOnly 쿠키
  - 60분 세션 타임아웃

- **입력 검증 & 필터링**:
  - XSSFilter: 모든 입력값 XSS 방지 처리
  - CSRFFilter: 관리자 영역 CSRF 토큰 검증
  - SecurityUtil: SQL Injection 패턴 탐지

- **데이터 보호**:
  - SHA-256 해시를 통한 안전한 비밀번호 저장
  - PreparedStatement로 SQL Injection 방지
  - 사용자 이름 해시화로 개인정보 보호

- **에러 처리**:
  - 정보 노출 방지를 위한 사용자 친화적 에러 페이지
  - 개발/운영 환경별 차별화된 오류 정보 표시
  - 보안 이벤트 로깅

## 🤝 기여하기

### 개발 참여 방법
1. **저장소 Fork 및 클론**:
   ```bash
   git clone https://github.com/YOUR_USERNAME/DISC.git
   cd DISC
   ```

2. **개발 브랜치 생성**:
   ```bash
   git checkout -b feature/your-feature-name
   ```

3. **개발 및 테스트**:
   ```bash
   # 코드 작성
   # 테스트 실행
   mvn test

   # 코드 품질 검사
   mvn checkstyle:check
   ```

4. **커밋 및 푸시**:
   ```bash
   git add .
   git commit -m "✨ Add: 새로운 기능 설명"
   git push origin feature/your-feature-name
   ```

5. **Pull Request 생성**:
   - GitHub에서 PR 생성
   - 명확한 제목과 설명 작성
   - 리뷰어 지정

### 개발 규칙
- 🏗️ **아키텍처**: MVC 패턴 유지
- 🔒 **보안**: 모든 입력값 검증 필수
- 📱 **반응형**: 모바일 우선 디자인
- 🧪 **테스트**: 새 기능은 테스트 코드 작성
- 📝 **문서화**: 주요 변경사항은 README 업데이트

## 📝 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

## 📞 지원 및 문의

### 기술 지원
- **이슈 리포트**: [GitHub Issues](https://github.com/HowardCheon/DISC/issues)
- **기능 요청**: [GitHub Discussions](https://github.com/HowardCheon/DISC/discussions)
- **보안 취약점**: security@disc-assessment.com (이메일)

### 문제 해결 가이드
1. **빌드 오류**: `mvn clean install`로 의존성 재설치
2. **DB 연결 오류**: `webapps/disc-assessment/WEB-INF/database/` 권한 확인
3. **세션 오류**: 브라우저 쿠키 및 캐시 삭제
4. **권한 오류**: Tomcat 실행 계정의 파일 시스템 권한 확인

### 운영 환경 체크리스트
- [ ] HTTPS 설정 (`web.xml`에서 secure=true 설정)
- [ ] 기본 관리자 비밀번호 변경
- [ ] 데이터베이스 백업 설정
- [ ] 로그 모니터링 설정
- [ ] 방화벽 설정 (8080 포트 제한)

---

## 🎯 로드맵

### v2.1 (예정)
- [ ] 이메일 알림 기능
- [ ] 결과 PDF 자동 생성
- [ ] 다국어 지원 (영어)
- [ ] REST API 제공

### v3.0 (예정)
- [ ] 팀 분석 기능
- [ ] 고급 통계 및 리포트
- [ ] 모바일 앱 연동
- [ ] 클라우드 배포 지원

---

**DISC 성격검사 웹 애플리케이션**으로 팀의 성격 유형을 파악하고 더 나은 협업을 만들어보세요! 🚀

[![Stars](https://img.shields.io/github/stars/HowardCheon/DISC?style=social)](https://github.com/HowardCheon/DISC)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Version](https://img.shields.io/badge/version-2.0-green.svg)](https://github.com/HowardCheon/DISC/releases)
