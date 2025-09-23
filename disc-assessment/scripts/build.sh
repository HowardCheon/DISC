#!/bin/bash

# ==============================================
# DISC 애플리케이션 빌드 스크립트
# ==============================================

set -e  # 오류 발생 시 스크립트 중단

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

# 로그 함수
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

log_step() {
    echo -e "${PURPLE}[STEP]${NC} $1"
}

# 스크립트 시작
echo "=============================================="
echo "      DISC 애플리케이션 빌드 스크립트"
echo "=============================================="
echo

# 명령행 파라미터 처리
BUILD_TYPE="development"
SKIP_TESTS="false"
CLEAN_BUILD="false"
VERBOSE="false"

while [[ $# -gt 0 ]]; do
    case $1 in
        --production)
            BUILD_TYPE="production"
            shift
            ;;
        --skip-tests)
            SKIP_TESTS="true"
            shift
            ;;
        --clean)
            CLEAN_BUILD="true"
            shift
            ;;
        --verbose)
            VERBOSE="true"
            shift
            ;;
        --help)
            echo "사용법: $0 [옵션]"
            echo ""
            echo "옵션:"
            echo "  --production    프로덕션 빌드 (기본: development)"
            echo "  --skip-tests    테스트 건너뛰기"
            echo "  --clean         클린 빌드"
            echo "  --verbose       상세 로그 출력"
            echo "  --help          이 도움말 출력"
            exit 0
            ;;
        *)
            log_error "알 수 없는 옵션: $1"
            echo "사용법은 '$0 --help'를 참조하세요."
            exit 1
            ;;
    esac
done

# 빌드 설정 표시
log_info "빌드 설정:"
echo "  • 빌드 타입: $BUILD_TYPE"
echo "  • 테스트 건너뛰기: $SKIP_TESTS"
echo "  • 클린 빌드: $CLEAN_BUILD"
echo "  • 상세 로그: $VERBOSE"
echo

# 프로젝트 루트 디렉토리 확인
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

log_info "프로젝트 루트: $PROJECT_ROOT"

# 필수 도구 확인
log_step "1/8 필수 도구 확인"

# Maven 확인
if ! command -v mvn &> /dev/null; then
    log_error "Maven이 설치되어 있지 않습니다."
    exit 1
fi
MVN_VERSION=$(mvn -version | head -n 1)
log_info "Maven: $MVN_VERSION"

# Java 확인
if ! command -v java &> /dev/null; then
    log_error "Java가 설치되어 있지 않습니다."
    exit 1
fi
JAVA_VERSION=$(java -version 2>&1 | head -n 1)
log_info "Java: $JAVA_VERSION"

# JDK 17+ 버전 확인
JAVA_MAJOR_VERSION=$(java -version 2>&1 | grep -oP 'version "([0-9]+)' | grep -oP '[0-9]+' | head -n 1)
if [ "$JAVA_MAJOR_VERSION" -lt 17 ]; then
    log_error "JDK 17 이상이 필요합니다. 현재 버전: $JAVA_MAJOR_VERSION"
    exit 1
fi

log_success "필수 도구 확인 완료"

# 프로젝트 디렉토리로 이동
cd "$PROJECT_ROOT"

# 2. Git 정보 확인
log_step "2/8 Git 정보 확인"
if [ -d ".git" ]; then
    GIT_BRANCH=$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "unknown")
    GIT_COMMIT=$(git rev-parse --short HEAD 2>/dev/null || echo "unknown")
    GIT_STATUS=$(git status --porcelain 2>/dev/null | wc -l || echo "0")

    log_info "Git 브랜치: $GIT_BRANCH"
    log_info "Git 커밋: $GIT_COMMIT"

    if [ "$GIT_STATUS" -gt 0 ]; then
        log_warning "작업 디렉토리에 커밋되지 않은 변경사항이 있습니다."
    fi
else
    log_warning "Git 저장소가 아닙니다."
fi

# 3. 백업 및 정리
log_step "3/8 백업 및 정리"

# 이전 빌드 백업
if [ -f "target/disc-assessment.war" ]; then
    BACKUP_DIR="target/backup/$(date +%Y%m%d_%H%M%S)"
    mkdir -p "$BACKUP_DIR"
    cp "target/disc-assessment.war" "$BACKUP_DIR/"
    log_info "이전 WAR 파일 백업: $BACKUP_DIR/disc-assessment.war"
fi

# 클린 빌드인 경우 target 디렉토리 정리
if [ "$CLEAN_BUILD" = "true" ]; then
    log_info "target 디렉토리 정리 중..."
    mvn clean $([ "$VERBOSE" = "false" ] && echo "-q")
    log_success "정리 완료"
fi

# 4. 종속성 확인
log_step "4/8 종속성 확인"
log_info "Maven 종속성 해결 중..."

if [ "$VERBOSE" = "true" ]; then
    mvn dependency:resolve
else
    mvn dependency:resolve -q
fi

log_success "종속성 해결 완료"

# 5. 컴파일
log_step "5/8 소스 코드 컴파일"
log_info "Java 소스 코드 컴파일 중..."

COMPILE_CMD="mvn compile"
if [ "$VERBOSE" = "false" ]; then
    COMPILE_CMD="$COMPILE_CMD -q"
fi

if eval $COMPILE_CMD; then
    log_success "컴파일 완료"
else
    log_error "컴파일 실패"
    exit 1
fi

# 6. 테스트 실행
log_step "6/8 테스트 실행"

if [ "$SKIP_TESTS" = "true" ]; then
    log_warning "테스트를 건너뛰었습니다."
else
    log_info "단위 테스트 실행 중..."

    TEST_CMD="mvn test"
    if [ "$VERBOSE" = "false" ]; then
        TEST_CMD="$TEST_CMD -q"
    fi

    if eval $TEST_CMD; then
        log_success "모든 테스트 통과"
    else
        log_error "테스트 실패"
        exit 1
    fi
fi

# 7. 리소스 처리 및 패키징
log_step "7/8 패키징"

# 프로덕션 빌드인 경우 프로덕션 프로파일 사용
PACKAGE_CMD="mvn package"
if [ "$BUILD_TYPE" = "production" ]; then
    PACKAGE_CMD="$PACKAGE_CMD -Pproduction"
    log_info "프로덕션 프로파일로 패키징 중..."
else
    log_info "개발 프로파일로 패키징 중..."
fi

if [ "$SKIP_TESTS" = "true" ]; then
    PACKAGE_CMD="$PACKAGE_CMD -DskipTests"
fi

if [ "$VERBOSE" = "false" ]; then
    PACKAGE_CMD="$PACKAGE_CMD -q"
fi

if eval $PACKAGE_CMD; then
    log_success "패키징 완료"
else
    log_error "패키징 실패"
    exit 1
fi

# 8. 빌드 결과 확인
log_step "8/8 빌드 결과 확인"

WAR_FILE="target/disc-assessment.war"
if [ -f "$WAR_FILE" ]; then
    WAR_SIZE=$(du -h "$WAR_FILE" | cut -f1)
    log_success "WAR 파일 생성 완료: $WAR_FILE ($WAR_SIZE)"

    # 체크섬 생성
    if command -v sha256sum &> /dev/null; then
        CHECKSUM=$(sha256sum "$WAR_FILE" | cut -d' ' -f1)
        echo "$CHECKSUM  disc-assessment.war" > "target/disc-assessment.war.sha256"
        log_info "체크섬: $CHECKSUM"
    fi

    # 빌드 정보 파일 생성
    BUILD_INFO_FILE="target/build-info.txt"
    cat > "$BUILD_INFO_FILE" << EOF
# DISC 애플리케이션 빌드 정보
빌드 시간: $(date)
빌드 타입: $BUILD_TYPE
Git 브랜치: ${GIT_BRANCH:-unknown}
Git 커밋: ${GIT_COMMIT:-unknown}
Java 버전: $JAVA_VERSION
Maven 버전: $MVN_VERSION
WAR 파일 크기: $WAR_SIZE
체크섬: ${CHECKSUM:-unavailable}
EOF

    log_info "빌드 정보: $BUILD_INFO_FILE"

else
    log_error "WAR 파일이 생성되지 않았습니다."
    exit 1
fi

# 성공 메시지
echo
echo "=============================================="
echo "              빌드 성공!"
echo "=============================================="
echo
log_success "빌드 타입: $BUILD_TYPE"
log_success "출력 파일: $WAR_FILE"
log_success "파일 크기: $WAR_SIZE"
echo
log_info "다음 단계:"
echo "  1. 배포: ./scripts/deploy.sh"
echo "  2. 샘플 데이터 생성: ./scripts/generate-sample-data.sh"
echo "  3. Tomcat에 수동 배포: cp $WAR_FILE \$TOMCAT_HOME/webapps/"
echo

# 배포 스크립트 실행 여부 확인
if [ "$BUILD_TYPE" = "production" ]; then
    echo
    read -p "바로 배포하시겠습니까? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        log_info "배포 스크립트 실행 중..."
        exec "$SCRIPT_DIR/deploy.sh" --war-file "$WAR_FILE"
    fi
fi