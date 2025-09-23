#!/bin/bash

# DISC 프로젝트 배포 스크립트
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

# 기본 설정값
TOMCAT_HOME=""
TOMCAT_USER="tomcat"
BACKUP_DIR="$PROJECT_ROOT/backups"
DEPLOY_MODE="local"
AUTO_START=true
CREATE_BACKUP=true

# 설정 파일에서 기본값 로드
CONFIG_FILE="$PROJECT_ROOT/scripts/deploy.config"
if [ -f "$CONFIG_FILE" ]; then
    source "$CONFIG_FILE"
    log_info "설정 파일 로드: $CONFIG_FILE"
fi

# 도움말 함수
show_help() {
    echo "DISC 프로젝트 배포 스크립트"
    echo ""
    echo "사용법: $0 [옵션]"
    echo ""
    echo "옵션:"
    echo "  -t, --tomcat-home PATH    Tomcat 설치 경로 지정"
    echo "  -m, --mode MODE          배포 모드 (local|remote|docker)"
    echo "  -u, --user USER          Tomcat 실행 사용자"
    echo "  -n, --no-backup          백업 생성 건너뛰기"
    echo "  -s, --no-start           배포 후 Tomcat 시작 안함"
    echo "  -b, --build              배포 전 빌드 수행"
    echo "  -h, --help               도움말 표시"
    echo ""
    echo "예시:"
    echo "  $0 -t /opt/tomcat -m local"
    echo "  $0 --build --tomcat-home /usr/local/tomcat"
}

# 파라미터 처리
BUILD_FIRST=false

while [[ $# -gt 0 ]]; do
    case $1 in
        -t|--tomcat-home)
            TOMCAT_HOME="$2"
            shift 2
            ;;
        -m|--mode)
            DEPLOY_MODE="$2"
            shift 2
            ;;
        -u|--user)
            TOMCAT_USER="$2"
            shift 2
            ;;
        -n|--no-backup)
            CREATE_BACKUP=false
            shift
            ;;
        -s|--no-start)
            AUTO_START=false
            shift
            ;;
        -b|--build)
            BUILD_FIRST=true
            shift
            ;;
        -h|--help)
            show_help
            exit 0
            ;;
        *)
            log_error "알 수 없는 옵션: $1"
            show_help
            exit 1
            ;;
    esac
done

log_info "DISC 프로젝트 배포 시작..."
log_info "배포 모드: $DEPLOY_MODE"

# Tomcat 경로 자동 감지
if [ -z "$TOMCAT_HOME" ]; then
    log_info "Tomcat 경로 자동 감지 중..."

    # 일반적인 Tomcat 설치 경로들 확인
    POSSIBLE_PATHS=(
        "/opt/tomcat"
        "/usr/local/tomcat"
        "/usr/share/tomcat"
        "/var/lib/tomcat"
        "/home/tomcat"
        "$HOME/tomcat"
        "/Applications/tomcat"  # macOS
    )

    for path in "${POSSIBLE_PATHS[@]}"; do
        if [ -d "$path" ] && [ -f "$path/bin/catalina.sh" ]; then
            TOMCAT_HOME="$path"
            log_info "Tomcat 발견: $TOMCAT_HOME"
            break
        fi
    done

    if [ -z "$TOMCAT_HOME" ]; then
        log_error "Tomcat 경로를 찾을 수 없습니다. -t 옵션으로 경로를 지정해주세요."
        exit 1
    fi
fi

# Tomcat 경로 유효성 검사
if [ ! -d "$TOMCAT_HOME" ]; then
    log_error "Tomcat 경로가 존재하지 않습니다: $TOMCAT_HOME"
    exit 1
fi

if [ ! -f "$TOMCAT_HOME/bin/catalina.sh" ]; then
    log_error "유효한 Tomcat 설치가 아닙니다: $TOMCAT_HOME"
    exit 1
fi

WEBAPPS_DIR="$TOMCAT_HOME/webapps"
log_info "Tomcat webapps 디렉토리: $WEBAPPS_DIR"

# 빌드 수행 (옵션)
if [ "$BUILD_FIRST" = true ]; then
    log_info "배포 전 빌드 수행..."
    ./scripts/build.sh
    if [ $? -ne 0 ]; then
        log_error "빌드 실패"
        exit 1
    fi
fi

# WAR 파일 확인
WAR_FILE=$(find target -name "*.war" -type f | head -1)
if [ -z "$WAR_FILE" ]; then
    log_error "WAR 파일을 찾을 수 없습니다. 빌드를 먼저 수행해주세요."
    log_info "빌드 수행: ./scripts/build.sh"
    exit 1
fi

WAR_NAME=$(basename "$WAR_FILE" .war)
log_info "배포할 WAR 파일: $WAR_FILE"
log_info "애플리케이션 이름: $WAR_NAME"

# 백업 생성
if [ "$CREATE_BACKUP" = true ]; then
    log_info "기존 애플리케이션 백업 생성 중..."

    mkdir -p "$BACKUP_DIR"
    BACKUP_DATE=$(date +%Y%m%d_%H%M%S)

    # 기존 WAR 파일 백업
    if [ -f "$WEBAPPS_DIR/$WAR_NAME.war" ]; then
        cp "$WEBAPPS_DIR/$WAR_NAME.war" "$BACKUP_DIR/${WAR_NAME}_${BACKUP_DATE}.war"
        log_success "WAR 파일 백업: $BACKUP_DIR/${WAR_NAME}_${BACKUP_DATE}.war"
    fi

    # 기존 애플리케이션 디렉토리 백업
    if [ -d "$WEBAPPS_DIR/$WAR_NAME" ]; then
        tar -czf "$BACKUP_DIR/${WAR_NAME}_${BACKUP_DATE}.tar.gz" -C "$WEBAPPS_DIR" "$WAR_NAME"
        log_success "애플리케이션 디렉토리 백업: $BACKUP_DIR/${WAR_NAME}_${BACKUP_DATE}.tar.gz"
    fi
fi

# Tomcat 상태 확인
check_tomcat_status() {
    if pgrep -f "$TOMCAT_HOME" > /dev/null; then
        return 0  # 실행 중
    else
        return 1  # 중단됨
    fi
}

# Tomcat 중지
log_info "Tomcat 중지 중..."
if check_tomcat_status; then
    if [ "$(id -u)" -eq 0 ] && [ "$TOMCAT_USER" != "root" ]; then
        # root로 실행 중이고 다른 사용자로 Tomcat 실행
        sudo -u "$TOMCAT_USER" "$TOMCAT_HOME/bin/shutdown.sh"
    else
        "$TOMCAT_HOME/bin/shutdown.sh"
    fi

    # Tomcat이 완전히 중지될 때까지 대기 (최대 30초)
    for i in {1..30}; do
        if ! check_tomcat_status; then
            log_success "Tomcat 중지 완료"
            break
        fi
        sleep 1
        if [ $i -eq 30 ]; then
            log_warning "Tomcat 중지 시간 초과. 강제 종료합니다."
            pkill -f "$TOMCAT_HOME"
        fi
    done
else
    log_info "Tomcat이 이미 중지되어 있습니다."
fi

# 기존 애플리케이션 제거
log_info "기존 애플리케이션 제거 중..."
if [ -f "$WEBAPPS_DIR/$WAR_NAME.war" ]; then
    rm -f "$WEBAPPS_DIR/$WAR_NAME.war"
    log_info "기존 WAR 파일 제거"
fi

if [ -d "$WEBAPPS_DIR/$WAR_NAME" ]; then
    rm -rf "$WEBAPPS_DIR/$WAR_NAME"
    log_info "기존 애플리케이션 디렉토리 제거"
fi

# 새 WAR 파일 배포
log_info "새 WAR 파일 배포 중..."
cp "$WAR_FILE" "$WEBAPPS_DIR/"
if [ $? -eq 0 ]; then
    log_success "WAR 파일 배포 완료: $WEBAPPS_DIR/$(basename $WAR_FILE)"
else
    log_error "WAR 파일 배포 실패"
    exit 1
fi

# 권한 설정
if [ "$(id -u)" -eq 0 ] && [ "$TOMCAT_USER" != "root" ]; then
    chown "$TOMCAT_USER:$TOMCAT_USER" "$WEBAPPS_DIR/$(basename $WAR_FILE)"
    log_info "파일 소유권을 $TOMCAT_USER 로 변경"
fi

# 데이터베이스 초기화 (선택사항)
DB_SCRIPT="$PROJECT_ROOT/scripts/create-sample-data.sql"
if [ -f "$DB_SCRIPT" ]; then
    read -p "샘플 데이터를 생성하시겠습니까? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        log_info "데이터베이스 초기화 중..."
        # SQLite 데이터베이스인 경우
        if command -v sqlite3 &> /dev/null; then
            sqlite3 "$PROJECT_ROOT/database/disc.db" < "$DB_SCRIPT"
            log_success "샘플 데이터 생성 완료"
        else
            log_warning "sqlite3 명령을 찾을 수 없습니다. 수동으로 데이터베이스를 초기화해주세요."
        fi
    fi
fi

# Tomcat 시작
if [ "$AUTO_START" = true ]; then
    log_info "Tomcat 시작 중..."
    if [ "$(id -u)" -eq 0 ] && [ "$TOMCAT_USER" != "root" ]; then
        sudo -u "$TOMCAT_USER" "$TOMCAT_HOME/bin/startup.sh"
    else
        "$TOMCAT_HOME/bin/startup.sh"
    fi

    # Tomcat이 시작될 때까지 대기
    log_info "Tomcat 시작 대기 중..."
    for i in {1..60}; do
        if check_tomcat_status; then
            log_success "Tomcat 시작 완료"
            break
        fi
        sleep 1
        if [ $i -eq 60 ]; then
            log_error "Tomcat 시작 시간 초과"
            exit 1
        fi
    done

    # 애플리케이션 배포 대기
    log_info "애플리케이션 배포 대기 중..."
    for i in {1..120}; do
        if [ -d "$WEBAPPS_DIR/$WAR_NAME" ]; then
            log_success "애플리케이션 배포 완료"
            break
        fi
        sleep 1
        if [ $i -eq 120 ]; then
            log_warning "애플리케이션 배포 시간 초과"
        fi
    done
fi

# 배포 결과 확인
log_info "배포 결과 확인 중..."

# 파일 존재 확인
if [ -f "$WEBAPPS_DIR/$(basename $WAR_FILE)" ]; then
    log_success "WAR 파일 배포 확인"
else
    log_error "WAR 파일 배포 실패"
    exit 1
fi

# 애플리케이션 디렉토리 확인
if [ -d "$WEBAPPS_DIR/$WAR_NAME" ]; then
    log_success "애플리케이션 압축 해제 확인"
else
    log_warning "애플리케이션이 아직 압축 해제되지 않았습니다."
fi

# 배포 정보 파일 생성
DEPLOY_INFO_FILE="$PROJECT_ROOT/deploy-info.txt"
cat > "$DEPLOY_INFO_FILE" << EOF
DISC 프로젝트 배포 정보
======================
배포 일시: $(date)
배포 모드: $DEPLOY_MODE
Tomcat 경로: $TOMCAT_HOME
WAR 파일: $(basename $WAR_FILE)
배포 경로: $WEBAPPS_DIR/$(basename $WAR_FILE)
백업 디렉토리: $BACKUP_DIR
Git 커밋: $(git rev-parse --short HEAD 2>/dev/null || echo "N/A")
Git 브랜치: $(git branch --show-current 2>/dev/null || echo "N/A")
EOF

log_info "배포 정보가 $DEPLOY_INFO_FILE 에 저장되었습니다."

# 접속 정보 안내
log_info ""
log_info "배포 완료 정보:"
log_info "  - 애플리케이션 URL: http://localhost:8080/$WAR_NAME"
log_info "  - 관리자 페이지: http://localhost:8080/$WAR_NAME/admin"
log_info "  - Tomcat 관리자: http://localhost:8080/manager"
log_info ""
log_info "다음 단계:"
log_info "  1. 브라우저에서 애플리케이션 접속 테스트"
log_info "  2. 데이터베이스 연결 확인"
log_info "  3. 주요 기능 테스트"

# 간단한 헬스 체크
if command -v curl &> /dev/null; then
    log_info "애플리케이션 헬스 체크 중..."
    sleep 5  # 애플리케이션 시작 대기

    HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:8080/$WAR_NAME" || echo "000")
    if [ "$HTTP_STATUS" = "200" ]; then
        log_success "애플리케이션이 정상적으로 실행 중입니다!"
    elif [ "$HTTP_STATUS" = "404" ]; then
        log_warning "애플리케이션 접속 불가 (404). 조금 더 기다려주세요."
    else
        log_warning "애플리케이션 상태 확인 불가 (HTTP $HTTP_STATUS)"
    fi
fi

log_success "배포가 성공적으로 완료되었습니다!"