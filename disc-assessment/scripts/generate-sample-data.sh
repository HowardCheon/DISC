#!/bin/bash

# ==============================================
# DISC 애플리케이션 샘플 데이터 생성 스크립트
# ==============================================

set -e  # 오류 발생 시 스크립트 중단

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
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

# 스크립트 시작
echo "=============================================="
echo "  DISC 애플리케이션 샘플 데이터 생성 도구"
echo "=============================================="
echo

# 프로젝트 루트 디렉토리 확인
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

log_info "프로젝트 루트: $PROJECT_ROOT"

# Maven 설치 확인
if ! command -v mvn &> /dev/null; then
    log_error "Maven이 설치되어 있지 않습니다. Maven을 먼저 설치해주세요."
    exit 1
fi

log_info "Maven 버전: $(mvn -version | head -n 1)"

# Java 설치 확인
if ! command -v java &> /dev/null; then
    log_error "Java가 설치되어 있지 않습니다. JDK 17+를 먼저 설치해주세요."
    exit 1
fi

log_info "Java 버전: $(java -version 2>&1 | head -n 1)"

# 프로젝트 디렉토리로 이동
cd "$PROJECT_ROOT"

# 확인 메시지
echo
log_warning "이 스크립트는 다음 작업을 수행합니다:"
echo "  • 기존 샘플 데이터 정리"
echo "  • 10명의 테스트 사용자 생성"
echo "  • 7명은 검사 완료 상태로 설정"
echo "  • 3명은 대기중/진행중 상태로 설정"
echo "  • 다양한 DISC 유형 결과 생성"
echo

read -p "계속하시겠습니까? (y/N): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    log_info "스크립트가 취소되었습니다."
    exit 0
fi

# 1. 프로젝트 컴파일
log_info "프로젝트 컴파일 중..."
if mvn clean compile -q; then
    log_success "컴파일 완료"
else
    log_error "컴파일 실패"
    exit 1
fi

# 2. 클래스패스 설정
CLASSPATH="target/classes"
for jar in target/lib/*.jar; do
    if [ -f "$jar" ]; then
        CLASSPATH="$CLASSPATH:$jar"
    fi
done

# Maven 의존성 복사 (lib 디렉토리가 없는 경우)
if [ ! -d "target/lib" ]; then
    log_info "의존성 라이브러리 복사 중..."
    mvn dependency:copy-dependencies -DoutputDirectory=target/lib -q
fi

# 3. 샘플 데이터 생성 실행
log_info "샘플 데이터 생성 중..."
echo

if java -cp "$CLASSPATH" com.disc.util.SampleDataGenerator; then
    echo
    log_success "샘플 데이터 생성이 완료되었습니다!"

    echo
    log_info "생성된 샘플 사용자 목록:"
    echo "  1. 김철수 (검사완료)"
    echo "  2. 이영희 (검사완료)"
    echo "  3. 박민준 (검사완료)"
    echo "  4. 최지은 (검사완료)"
    echo "  5. 정현우 (검사완료)"
    echo "  6. 강미영 (검사완료)"
    echo "  7. 윤도현 (검사완료)"
    echo "  8. 임소진 (진행중)"
    echo "  9. 조성민 (대기중)"
    echo " 10. 한예린 (대기중)"

    echo
    log_info "이제 관리자 페이지에서 샘플 데이터를 확인할 수 있습니다:"
    echo "  • 대시보드: http://localhost:8080/disc-assessment/admin/dashboard"
    echo "  • 링크 생성: http://localhost:8080/disc-assessment/admin/create-link"

else
    log_error "샘플 데이터 생성 중 오류가 발생했습니다."
    exit 1
fi

echo
echo "=============================================="
echo "          샘플 데이터 생성 완료!"
echo "=============================================="