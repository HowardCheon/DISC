#!/bin/bash

# ==============================================
# DISC 애플리케이션 배포 스크립트
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

# 기본 설정값
TOMCAT_HOME=""
WAR_FILE=""
BACKUP_ENABLED="true"
RESTART_TOMCAT="true"
WAIT_FOR_DEPLOYMENT="true"
DEPLOYMENT_TIMEOUT=60

# 스크립트 시작
echo "=============================================="
echo "      DISC 애플리케이션 배포 스크립트"
echo "=============================================="
echo

# 명령행 파라미터 처리
while [[ $# -gt 0 ]]; do
    case $1 in
        --tomcat-home)
            TOMCAT_HOME="$2"
            shift 2
            ;;
        --war-file)
            WAR_FILE="$2"
            shift 2
            ;;
        --no-backup)
            BACKUP_ENABLED="false"
            shift
            ;;
        --no-restart)
            RESTART_TOMCAT="false"
            shift
            ;;
        --no-wait)
            WAIT_FOR_DEPLOYMENT="false"
            shift
            ;;
        --timeout)
            DEPLOYMENT_TIMEOUT="$2"
            shift 2
            ;;
        --help)
            echo "사용법: $0 [옵션]"
            echo ""
            echo "옵션:"
            echo "  --tomcat-home PATH     Tomcat 설치 경로 지정"
            echo "  --war-file PATH        배포할 WAR 파일 경로"
            echo "  --no-backup            기존 애플리케이션 백업 안함"
            echo "  --no-restart           Tomcat 재시작 안함"
            echo "  --no-wait              배포 완료 대기 안함"
            echo "  --timeout SECONDS      배포 대기 시간 (기본: 60초)"
            echo "  --help                 이 도움말 출력"
            echo ""
            echo "환경 변수:"
            echo "  TOMCAT_HOME           Tomcat 설치 경로"
            echo "  DISC_WAR_FILE         WAR 파일 경로"
            exit 0
            ;;
        *)
            log_error "알 수 없는 옵션: $1"
            echo "사용법은 '$0 --help'를 참조하세요."
            exit 1
            ;;
    esac
done

# 프로젝트 루트 디렉토리 확인
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# Tomcat 경로 자동 감지
if [ -z "$TOMCAT_HOME" ]; then
    # 환경 변수에서 확인
    if [ -n "$TOMCAT_HOME" ]; then
        log_info "환경 변수에서 Tomcat 경로 감지: $TOMCAT_HOME"
    else
        # 일반적인 경로에서 찾기
        POSSIBLE_PATHS=(
            "/opt/tomcat"
            "/usr/local/tomcat"
            "/var/lib/tomcat9"
            "/usr/share/tomcat9"
            "$HOME/apache-tomcat-*"
            "/Applications/apache-tomcat-*"  # macOS
        )

        for path in "${POSSIBLE_PATHS[@]}"; do
            if [ -d "$path" ] && [ -f "$path/bin/catalina.sh" ]; then
                TOMCAT_HOME="$path"
                log_info "Tomcat 자동 감지: $TOMCAT_HOME"
                break
            fi
        done

        if [ -z "$TOMCAT_HOME" ]; then
            log_error "Tomcat 설치 경로를 찾을 수 없습니다."
            echo "다음 중 하나의 방법으로 Tomcat 경로를 지정해주세요:"
            echo "  1. --tomcat-home 옵션 사용"
            echo "  2. TOMCAT_HOME 환경 변수 설정"
            echo "  3. 표준 경로에 Tomcat 설치"
            exit 1
        fi
    fi
fi

# WAR 파일 경로 확인
if [ -z "$WAR_FILE" ]; then
    if [ -n "$DISC_WAR_FILE" ]; then
        WAR_FILE="$DISC_WAR_FILE"
    else
        WAR_FILE="$PROJECT_ROOT/target/disc-assessment.war"
    fi
fi

# 설정 검증
log_step "1/7 배포 설정 검증"

log_info "배포 설정:"
echo "  • Tomcat 경로: $TOMCAT_HOME"
echo "  • WAR 파일: $WAR_FILE"
echo "  • 백업 사용: $BACKUP_ENABLED"
echo "  • Tomcat 재시작: $RESTART_TOMCAT"
echo "  • 배포 대기: $WAIT_FOR_DEPLOYMENT"
echo

# Tomcat 설치 확인
if [ ! -d "$TOMCAT_HOME" ]; then
    log_error "Tomcat 경로가 존재하지 않습니다: $TOMCAT_HOME"
    exit 1
fi

if [ ! -f "$TOMCAT_HOME/bin/catalina.sh" ]; then
    log_error "올바른 Tomcat 설치가 아닙니다: $TOMCAT_HOME"
    exit 1
fi

# WAR 파일 확인
if [ ! -f "$WAR_FILE" ]; then
    log_error "WAR 파일을 찾을 수 없습니다: $WAR_FILE"
    echo "먼저 빌드를 실행하세요: ./scripts/build.sh"
    exit 1
fi

WAR_SIZE=$(du -h "$WAR_FILE" | cut -f1)
log_success "WAR 파일 확인: $WAR_FILE ($WAR_SIZE)"

# 권한 확인
WEBAPPS_DIR="$TOMCAT_HOME/webapps"
if [ ! -w "$WEBAPPS_DIR" ]; then
    log_error "webapps 디렉토리에 쓰기 권한이 없습니다: $WEBAPPS_DIR"
    echo "sudo 권한이 필요할 수 있습니다."
    exit 1
fi

log_success "배포 설정 검증 완료"

# 2. Tomcat 상태 확인
log_step "2/7 Tomcat 상태 확인"

# Tomcat 프로세스 확인
TOMCAT_PID=""
if command -v pgrep &> /dev/null; then
    TOMCAT_PID=$(pgrep -f "catalina" || echo "")
fi

if [ -n "$TOMCAT_PID" ]; then
    log_info "Tomcat이 실행 중입니다 (PID: $TOMCAT_PID)"
    TOMCAT_RUNNING=true
else
    log_info "Tomcat이 실행되지 않고 있습니다"
    TOMCAT_RUNNING=false
fi

# 3. 기존 애플리케이션 백업
log_step "3/7 기존 애플리케이션 백업"

EXISTING_WAR="$WEBAPPS_DIR/disc-assessment.war"
EXISTING_DIR="$WEBAPPS_DIR/disc-assessment"

if [ "$BACKUP_ENABLED" = "true" ]; then
    BACKUP_DIR="$PROJECT_ROOT/backup/$(date +%Y%m%d_%H%M%S)"
    mkdir -p "$BACKUP_DIR"

    if [ -f "$EXISTING_WAR" ]; then
        cp "$EXISTING_WAR" "$BACKUP_DIR/"
        log_info "기존 WAR 파일 백업: $BACKUP_DIR/disc-assessment.war"
    fi

    if [ -d "$EXISTING_DIR" ]; then
        cp -r "$EXISTING_DIR" "$BACKUP_DIR/"
        log_info "기존 애플리케이션 디렉토리 백업: $BACKUP_DIR/disc-assessment/"
    fi

    log_success "백업 완료: $BACKUP_DIR"
else
    log_info "백업을 건너뛰었습니다"
fi

# 4. 기존 애플리케이션 정리
log_step "4/7 기존 애플리케이션 정리"

if [ -f "$EXISTING_WAR" ]; then
    rm -f "$EXISTING_WAR"
    log_info "기존 WAR 파일 삭제"
fi

if [ -d "$EXISTING_DIR" ]; then
    rm -rf "$EXISTING_DIR"
    log_info "기존 애플리케이션 디렉토리 삭제"
fi

# Tomcat 작업 디렉토리 정리
WORK_DIR="$TOMCAT_HOME/work/Catalina/localhost/disc-assessment"
if [ -d "$WORK_DIR" ]; then
    rm -rf "$WORK_DIR"
    log_info "Tomcat 작업 디렉토리 정리"
fi

log_success "기존 애플리케이션 정리 완료"

# 5. 새 WAR 파일 배포
log_step "5/7 새 WAR 파일 배포"

cp "$WAR_FILE" "$EXISTING_WAR"
log_success "새 WAR 파일 배포 완료: $EXISTING_WAR"

# WAR 파일 검증
if [ ! -f "$EXISTING_WAR" ]; then
    log_error "WAR 파일 복사에 실패했습니다"
    exit 1
fi

DEPLOYED_SIZE=$(du -h "$EXISTING_WAR" | cut -f1)
log_info "배포된 파일 크기: $DEPLOYED_SIZE"

# 6. Tomcat 재시작
log_step "6/7 Tomcat 재시작"

if [ "$RESTART_TOMCAT" = "true" ]; then
    if [ "$TOMCAT_RUNNING" = "true" ]; then
        log_info "Tomcat 중지 중..."
        "$TOMCAT_HOME/bin/shutdown.sh" || true

        # 프로세스 종료 대기
        STOP_TIMEOUT=30
        for i in $(seq 1 $STOP_TIMEOUT); do
            if [ -z "$(pgrep -f catalina || echo "")" ]; then
                break
            fi
            sleep 1
            if [ $i -eq $STOP_TIMEOUT ]; then
                log_warning "Tomcat이 정상적으로 종료되지 않았습니다. 강제 종료합니다."
                pkill -f catalina || true
            fi
        done
        log_success "Tomcat 중지 완료"
    fi

    log_info "Tomcat 시작 중..."
    "$TOMCAT_HOME/bin/startup.sh"

    sleep 3  # 시작 대기

    log_success "Tomcat 시작 완료"
else
    log_info "Tomcat 재시작을 건너뛰었습니다"
fi

# 7. 배포 확인
log_step "7/7 배포 확인"

if [ "$WAIT_FOR_DEPLOYMENT" = "true" ]; then
    log_info "애플리케이션 배포 대기 중... (최대 ${DEPLOYMENT_TIMEOUT}초)"

    # 배포 대기
    for i in $(seq 1 $DEPLOYMENT_TIMEOUT); do
        if [ -d "$EXISTING_DIR" ]; then
            log_success "애플리케이션 배포 완료 (${i}초 소요)"
            break
        fi

        if [ $i -eq $DEPLOYMENT_TIMEOUT ]; then
            log_error "배포 시간 초과"
            exit 1
        fi

        sleep 1
        if [ $((i % 10)) -eq 0 ]; then
            log_info "대기 중... (${i}/${DEPLOYMENT_TIMEOUT}초)"
        fi
    done

    # 애플리케이션 접근 테스트
    log_info "애플리케이션 접근 테스트 중..."

    for i in $(seq 1 30); do
        if curl -s -f "http://localhost:8080/disc-assessment/" > /dev/null 2>&1; then
            log_success "애플리케이션이 정상적으로 실행되고 있습니다"
            break
        fi

        if [ $i -eq 30 ]; then
            log_warning "애플리케이션 접근 테스트 실패. 수동으로 확인해주세요."
        fi

        sleep 2
    done
else
    log_info "배포 대기를 건너뛰었습니다"
fi

# 배포 정보 기록
DEPLOY_LOG="$PROJECT_ROOT/deploy.log"
echo "$(date): 배포 성공 - WAR: $WAR_FILE, Tomcat: $TOMCAT_HOME" >> "$DEPLOY_LOG"

# 성공 메시지
echo
echo "=============================================="
echo "              배포 성공!"
echo "=============================================="
echo
log_success "애플리케이션 URL: http://localhost:8080/disc-assessment/"
log_success "관리자 페이지: http://localhost:8080/disc-assessment/admin/login"
echo
log_info "기본 관리자 계정:"
echo "  • 사용자명: admin"
echo "  • 비밀번호: admin1234"
echo "  • ⚠️  보안상 첫 로그인 후 비밀번호를 변경하세요!"
echo
log_info "다음 단계:"
echo "  1. 브라우저에서 애플리케이션 접속 확인"
echo "  2. 관리자 로그인 및 비밀번호 변경"
echo "  3. 샘플 데이터 생성: ./scripts/generate-sample-data.sh"
echo "  4. 프로덕션 설정 적용 (HTTPS, DB 백업 등)"
echo

# Tomcat 로그 확인 안내
if [ -f "$TOMCAT_HOME/logs/catalina.out" ]; then
    echo "Tomcat 로그 확인:"
    echo "  tail -f $TOMCAT_HOME/logs/catalina.out"
    echo
fi