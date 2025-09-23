#!/bin/bash

# DISC 프로젝트 Maven 빌드 스크립트
# 작성일: $(date +%Y-%m-%d)

set -e  # 오류 발생 시 스크립트 중단

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 로그 함수들
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 프로젝트 루트 디렉토리 설정
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

log_info "DISC 프로젝트 빌드 시작..."
log_info "프로젝트 경로: $PROJECT_ROOT"

# Java 및 Maven 버전 확인
log_info "Java 및 Maven 버전 확인 중..."

if ! command -v java &> /dev/null; then
    log_error "Java가 설치되어 있지 않습니다. Java 8 이상을 설치해주세요."
    exit 1
fi

if ! command -v mvn &> /dev/null; then
    log_error "Maven이 설치되어 있지 않습니다. Maven을 설치해주세요."
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | grep -oP 'version "?(1\.)?\K\d+' | head -1)
log_info "Java 버전: $JAVA_VERSION"

if [ "$JAVA_VERSION" -lt 8 ]; then
    log_error "Java 8 이상이 필요합니다. 현재 버전: $JAVA_VERSION"
    exit 1
fi

MVN_VERSION=$(mvn -version | head -1 | cut -d' ' -f3)
log_info "Maven 버전: $MVN_VERSION"

# 빌드 옵션 파라미터 처리
CLEAN_BUILD=false
SKIP_TESTS=false
PROFILE="development"

while [[ $# -gt 0 ]]; do
    case $1 in
        -c|--clean)
            CLEAN_BUILD=true
            shift
            ;;
        -s|--skip-tests)
            SKIP_TESTS=true
            shift
            ;;
        -p|--profile)
            PROFILE="$2"
            shift 2
            ;;
        -h|--help)
            echo "사용법: $0 [옵션]"
            echo "옵션:"
            echo "  -c, --clean        Clean 빌드 수행"
            echo "  -s, --skip-tests   테스트 건너뛰기"
            echo "  -p, --profile      빌드 프로필 지정 (development|production)"
            echo "  -h, --help         도움말 표시"
            exit 0
            ;;
        *)
            log_error "알 수 없는 옵션: $1"
            exit 1
            ;;
    esac
done

log_info "빌드 설정:"
log_info "  - Clean 빌드: $CLEAN_BUILD"
log_info "  - 테스트 건너뛰기: $SKIP_TESTS"
log_info "  - 빌드 프로필: $PROFILE"

# pom.xml 파일 존재 확인
if [ ! -f "pom.xml" ]; then
    log_error "pom.xml 파일을 찾을 수 없습니다. Maven 프로젝트인지 확인해주세요."
    exit 1
fi

# target 디렉토리 정리 (Clean 빌드인 경우)
if [ "$CLEAN_BUILD" = true ]; then
    log_info "이전 빌드 결과물 정리 중..."
    mvn clean
    if [ $? -eq 0 ]; then
        log_success "정리 완료"
    else
        log_error "정리 실패"
        exit 1
    fi
fi

# 의존성 다운로드
log_info "의존성 다운로드 중..."
mvn dependency:resolve
if [ $? -eq 0 ]; then
    log_success "의존성 다운로드 완료"
else
    log_error "의존성 다운로드 실패"
    exit 1
fi

# 소스 코드 컴파일
log_info "소스 코드 컴파일 중..."
mvn compile
if [ $? -eq 0 ]; then
    log_success "컴파일 완료"
else
    log_error "컴파일 실패"
    exit 1
fi

# 테스트 실행 (건너뛰기 옵션이 false인 경우)
if [ "$SKIP_TESTS" = false ]; then
    log_info "테스트 실행 중..."
    mvn test
    if [ $? -eq 0 ]; then
        log_success "모든 테스트 통과"
    else
        log_error "테스트 실패"
        exit 1
    fi
else
    log_warning "테스트를 건너뜁니다."
fi

# WAR 파일 생성
log_info "WAR 파일 생성 중..."
if [ "$SKIP_TESTS" = true ]; then
    mvn package -DskipTests -P"$PROFILE"
else
    mvn package -P"$PROFILE"
fi

if [ $? -eq 0 ]; then
    log_success "WAR 파일 생성 완료"
else
    log_error "WAR 파일 생성 실패"
    exit 1
fi

# 빌드 결과 확인
WAR_FILE=$(find target -name "*.war" -type f | head -1)
if [ -n "$WAR_FILE" ]; then
    WAR_SIZE=$(du -h "$WAR_FILE" | cut -f1)
    log_success "빌드 완료: $WAR_FILE (크기: $WAR_SIZE)"
else
    log_error "WAR 파일을 찾을 수 없습니다."
    exit 1
fi

# 빌드 정보 파일 생성
BUILD_INFO_FILE="target/build-info.txt"
cat > "$BUILD_INFO_FILE" << EOF
DISC 프로젝트 빌드 정보
=======================
빌드 일시: $(date)
빌드 프로필: $PROFILE
Java 버전: $JAVA_VERSION
Maven 버전: $MVN_VERSION
WAR 파일: $WAR_FILE
WAR 크기: $WAR_SIZE
Git 커밋: $(git rev-parse --short HEAD 2>/dev/null || echo "N/A")
Git 브랜치: $(git branch --show-current 2>/dev/null || echo "N/A")
EOF

log_info "빌드 정보가 $BUILD_INFO_FILE 에 저장되었습니다."

# 배포용 디렉토리 생성
DEPLOY_DIR="target/deploy"
mkdir -p "$DEPLOY_DIR"

# WAR 파일과 설정 파일들을 배포 디렉토리로 복사
cp "$WAR_FILE" "$DEPLOY_DIR/"
cp "$BUILD_INFO_FILE" "$DEPLOY_DIR/"

# 설정 파일들이 있다면 복사
if [ -f "src/main/resources/config.properties" ]; then
    cp "src/main/resources/config.properties" "$DEPLOY_DIR/"
fi

if [ -d "scripts" ]; then
    cp -r scripts "$DEPLOY_DIR/"
fi

log_success "배포용 파일들이 $DEPLOY_DIR 에 준비되었습니다."

# 빌드 통계
log_info "빌드 통계:"
log_info "  - 총 빌드 시간: $(date)"
log_info "  - WAR 파일 경로: $WAR_FILE"
log_info "  - 배포 디렉토리: $DEPLOY_DIR"

# 다음 단계 안내
log_info ""
log_info "다음 단계:"
log_info "  1. 배포: ./scripts/deploy.sh"
log_info "  2. 수동 배포: WAR 파일을 Tomcat webapps 디렉토리에 복사"
log_info "  3. 테스트: http://localhost:8080/disc 접속"

log_success "빌드가 성공적으로 완료되었습니다!"