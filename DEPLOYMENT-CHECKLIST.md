# DISC 프로젝트 배포 체크리스트

## 📋 배포 전 준비사항

### 1. 시스템 요구사항 확인
- [ ] Java 8 이상 설치 확인
- [ ] Maven 3.6 이상 설치 확인
- [ ] Apache Tomcat 9.0 이상 설치 확인
- [ ] SQLite3 설치 확인 (개발 환경)
- [ ] MySQL/PostgreSQL 설치 확인 (운영 환경)

### 2. 환경 설정 확인
- [ ] `src/main/resources/config.properties` 환경별 설정 완료
- [ ] 데이터베이스 연결 정보 확인
- [ ] 이메일 SMTP 설정 확인
- [ ] 파일 업로드 디렉토리 권한 확인

### 3. 소스 코드 준비
- [ ] 최신 소스 코드 체크아웃
- [ ] Git 브랜치 확인 (main/master)
- [ ] 변경사항 커밋 완료
- [ ] 코드 리뷰 완료

## 🔧 개발 환경 배포

### 1. 데이터베이스 준비
```bash
# 데이터베이스 디렉토리 생성
mkdir -p database

# 샘플 데이터 생성
sqlite3 database/disc.db < scripts/create-sample-data.sql
```

### 2. 빌드 및 배포
```bash
# 권한 설정
chmod +x scripts/build.sh
chmod +x scripts/deploy.sh

# 빌드 실행
./scripts/build.sh --clean

# 배포 실행
./scripts/deploy.sh --build --tomcat-home /opt/tomcat
```

### 3. 동작 확인
- [ ] http://localhost:8080/disc 접속 확인
- [ ] 관리자 페이지 접속 확인 (admin/admin123)
- [ ] 데이터베이스 연결 확인
- [ ] 검사 발송 기능 테스트
- [ ] 검사 진행 기능 테스트
- [ ] 결과 분석 기능 테스트

## 🚀 운영 환경 배포

### 1. 사전 준비
- [ ] 서버 리소스 확인 (CPU, 메모리, 디스크)
- [ ] 백업 계획 수립
- [ ] 롤백 계획 수립
- [ ] 점검 시간 공지

### 2. 데이터베이스 설정
```sql
-- MySQL 데이터베이스 생성
CREATE DATABASE disc_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'disc_user'@'localhost' IDENTIFIED BY 'strong_password';
GRANT ALL PRIVILEGES ON disc_db.* TO 'disc_user'@'localhost';
FLUSH PRIVILEGES;
```

### 3. 설정 파일 수정
```properties
# config.properties 운영 환경 설정
app.environment=production
db.url=jdbc:mysql://localhost:3306/disc_db
db.username=disc_user
db.password=strong_password
mail.smtp.host=your-smtp-server.com
```

### 4. 보안 설정
- [ ] 방화벽 설정 확인
- [ ] SSL 인증서 설정
- [ ] 데이터베이스 접근 제한
- [ ] 관리자 계정 비밀번호 변경
- [ ] 파일 업로드 제한 설정

### 5. 모니터링 설정
- [ ] 로그 모니터링 설정
- [ ] 성능 모니터링 설정
- [ ] 알림 설정 (이메일, Slack)
- [ ] 백업 스케줄 설정

## 🧪 테스트 체크리스트

### 1. 기능 테스트
- [ ] 사용자 등록/로그인
- [ ] 검사 발송 기능
- [ ] 검사 응답 기능
- [ ] 결과 분석 기능
- [ ] 관리자 기능
- [ ] 사용자 이력 조회
- [ ] 통계 및 리포트

### 2. 성능 테스트
- [ ] 동시 사용자 처리 능력
- [ ] 데이터베이스 쿼리 성능
- [ ] 파일 업로드/다운로드 성능
- [ ] 메모리 사용량 확인

### 3. 보안 테스트
- [ ] SQL 인젝션 방어
- [ ] XSS 방어
- [ ] CSRF 방어
- [ ] 세션 관리
- [ ] 입력 검증

## 📊 배포 후 확인사항

### 1. 즉시 확인
- [ ] 애플리케이션 정상 기동
- [ ] 데이터베이스 연결 확인
- [ ] 로그 확인 (에러 없음)
- [ ] 메인 페이지 접속 확인

### 2. 24시간 후 확인
- [ ] 시스템 안정성 확인
- [ ] 성능 지표 확인
- [ ] 에러 로그 분석
- [ ] 사용자 피드백 수집

### 3. 1주일 후 확인
- [ ] 통계 데이터 분석
- [ ] 성능 트렌드 분석
- [ ] 사용자 활동 분석
- [ ] 개선사항 도출

## 🔄 롤백 절차

### 1. 긴급 롤백
```bash
# 이전 버전으로 롤백
cp backups/disc_20240101_120000.war $TOMCAT_HOME/webapps/
sudo systemctl restart tomcat
```

### 2. 데이터베이스 롤백
```bash
# 데이터베이스 백업 복원
mysql -u disc_user -p disc_db < backups/disc_db_20240101_120000.sql
```

## 📁 파일 구조 확인

```
DISC/
├── src/main/
│   ├── java/com/disc/
│   ├── resources/
│   │   ├── config.properties          ✓
│   │   └── templates/
│   └── webapp/
│       ├── admin/
│       │   ├── dashboard.jsp          ✓
│       │   ├── survey-send.jsp        ✓
│       │   ├── survey-list.jsp        ✓
│       │   ├── user-history.jsp       ✓
│       │   └── results-analysis.jsp   ✓
│       ├── js/
│       │   ├── dashboard.js           ✓
│       │   ├── survey-send.js         ✓
│       │   ├── survey-list.js         ✓
│       │   ├── user-history.js        ✓
│       │   └── results-analysis.js    ✓
│       └── WEB-INF/
├── scripts/
│   ├── build.sh                       ✓
│   ├── deploy.sh                      ✓
│   ├── deploy.config                  ✓
│   └── create-sample-data.sql         ✓
├── database/
├── backups/
├── logs/
└── uploads/
```

## 🆘 트러블슈팅

### 일반적인 문제들

#### 1. Tomcat 시작 실패
```bash
# 로그 확인
tail -f $TOMCAT_HOME/logs/catalina.out

# 포트 충돌 확인
netstat -tulpn | grep :8080

# Java 버전 확인
java -version
```

#### 2. 데이터베이스 연결 실패
```bash
# 데이터베이스 서비스 상태 확인
sudo systemctl status mysql

# 방화벽 확인
sudo ufw status

# 연결 테스트
mysql -u disc_user -p -h localhost disc_db
```

#### 3. 권한 문제
```bash
# 파일 권한 확인
ls -la $TOMCAT_HOME/webapps/

# 소유권 변경
sudo chown -R tomcat:tomcat $TOMCAT_HOME/webapps/disc/
```

#### 4. 메모리 부족
```bash
# JVM 힙 사이즈 조정
export JAVA_OPTS="-Xms512m -Xmx2048m -XX:PermSize=256m"
```

## 📞 지원 연락처

- **개발팀**: dev-team@company.com
- **시스템 관리**: sysadmin@company.com
- **긴급 연락**: +82-10-1234-5678

## 📝 배포 기록

| 날짜 | 버전 | 담당자 | 변경사항 | 상태 |
|------|------|--------|----------|------|
| 2024-01-01 | 1.0.0 | 개발팀 | 초기 배포 | 완료 |
| | | | | |

---

**마지막 업데이트**: 2024년 1월 1일
**문서 버전**: 1.0.0