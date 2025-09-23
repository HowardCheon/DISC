@echo off
REM ==============================================
REM DISC 애플리케이션 빌드 스크립트 (Windows)
REM ==============================================

setlocal enabledelayedexpansion

REM 색상 정의 (Windows 제한적)
set "INFO=[INFO]"
set "SUCCESS=[SUCCESS]"
set "WARNING=[WARNING]"
set "ERROR=[ERROR]"
set "STEP=[STEP]"

echo ==============================================
echo       DISC 애플리케이션 빌드 스크립트
echo ==============================================
echo.

REM 기본 설정
set "BUILD_TYPE=development"
set "SKIP_TESTS=false"
set "CLEAN_BUILD=false"
set "VERBOSE=false"

REM 명령행 파라미터 처리
:parse_args
if "%~1"=="" goto end_parse
if "%~1"=="--production" (
    set "BUILD_TYPE=production"
    shift
    goto parse_args
)
if "%~1"=="--skip-tests" (
    set "SKIP_TESTS=true"
    shift
    goto parse_args
)
if "%~1"=="--clean" (
    set "CLEAN_BUILD=true"
    shift
    goto parse_args
)
if "%~1"=="--verbose" (
    set "VERBOSE=true"
    shift
    goto parse_args
)
if "%~1"=="--help" (
    echo 사용법: %0 [옵션]
    echo.
    echo 옵션:
    echo   --production    프로덕션 빌드 ^(기본: development^)
    echo   --skip-tests    테스트 건너뛰기
    echo   --clean         클린 빌드
    echo   --verbose       상세 로그 출력
    echo   --help          이 도움말 출력
    goto end
)
echo %ERROR% 알 수 없는 옵션: %~1
echo 사용법은 '%0 --help'를 참조하세요.
exit /b 1

:end_parse

REM 빌드 설정 표시
echo %INFO% 빌드 설정:
echo   • 빌드 타입: %BUILD_TYPE%
echo   • 테스트 건너뛰기: %SKIP_TESTS%
echo   • 클린 빌드: %CLEAN_BUILD%
echo   • 상세 로그: %VERBOSE%
echo.

REM 프로젝트 루트 디렉토리
set "SCRIPT_DIR=%~dp0"
set "PROJECT_ROOT=%SCRIPT_DIR%.."

echo %INFO% 프로젝트 루트: %PROJECT_ROOT%

REM 필수 도구 확인
echo %STEP% 1/8 필수 도구 확인

REM Maven 확인
where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo %ERROR% Maven이 설치되어 있지 않습니다.
    pause
    exit /b 1
)
echo %INFO% Maven 확인됨

REM Java 확인
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo %ERROR% Java가 설치되어 있지 않습니다.
    pause
    exit /b 1
)
echo %INFO% Java 확인됨

echo %SUCCESS% 필수 도구 확인 완료

REM 프로젝트 디렉토리로 이동
cd /d "%PROJECT_ROOT%"

REM Git 정보 확인
echo %STEP% 2/8 Git 정보 확인
if exist ".git" (
    for /f "tokens=*" %%i in ('git rev-parse --abbrev-ref HEAD 2^>nul') do set "GIT_BRANCH=%%i"
    for /f "tokens=*" %%i in ('git rev-parse --short HEAD 2^>nul') do set "GIT_COMMIT=%%i"
    echo %INFO% Git 브랜치: !GIT_BRANCH!
    echo %INFO% Git 커밋: !GIT_COMMIT!
) else (
    echo %WARNING% Git 저장소가 아닙니다.
)

REM 백업 및 정리
echo %STEP% 3/8 백업 및 정리

if exist "target\disc-assessment.war" (
    set "BACKUP_DIR=target\backup\%date:~0,4%%date:~5,2%%date:~8,2%_%time:~0,2%%time:~3,2%%time:~6,2%"
    set "BACKUP_DIR=!BACKUP_DIR: =0!"
    mkdir "!BACKUP_DIR!" 2>nul
    copy "target\disc-assessment.war" "!BACKUP_DIR!\" >nul
    echo %INFO% 이전 WAR 파일 백업: !BACKUP_DIR!\disc-assessment.war
)

if "%CLEAN_BUILD%"=="true" (
    echo %INFO% target 디렉토리 정리 중...
    if "%VERBOSE%"=="true" (
        call mvn clean
    ) else (
        call mvn clean -q
    )
    echo %SUCCESS% 정리 완료
)

REM 종속성 확인
echo %STEP% 4/8 종속성 확인
echo %INFO% Maven 종속성 해결 중...

if "%VERBOSE%"=="true" (
    call mvn dependency:resolve
) else (
    call mvn dependency:resolve -q
)

if %errorlevel% neq 0 (
    echo %ERROR% 종속성 해결 실패
    pause
    exit /b 1
)
echo %SUCCESS% 종속성 해결 완료

REM 컴파일
echo %STEP% 5/8 소스 코드 컴파일
echo %INFO% Java 소스 코드 컴파일 중...

if "%VERBOSE%"=="true" (
    call mvn compile
) else (
    call mvn compile -q
)

if %errorlevel% neq 0 (
    echo %ERROR% 컴파일 실패
    pause
    exit /b 1
)
echo %SUCCESS% 컴파일 완료

REM 테스트 실행
echo %STEP% 6/8 테스트 실행

if "%SKIP_TESTS%"=="true" (
    echo %WARNING% 테스트를 건너뛰었습니다.
) else (
    echo %INFO% 단위 테스트 실행 중...

    if "%VERBOSE%"=="true" (
        call mvn test
    ) else (
        call mvn test -q
    )

    if %errorlevel% neq 0 (
        echo %ERROR% 테스트 실패
        pause
        exit /b 1
    )
    echo %SUCCESS% 모든 테스트 통과
)

REM 패키징
echo %STEP% 7/8 패키징

set "PACKAGE_CMD=mvn package"

if "%BUILD_TYPE%"=="production" (
    set "PACKAGE_CMD=%PACKAGE_CMD% -Pproduction"
    echo %INFO% 프로덕션 프로파일로 패키징 중...
) else (
    echo %INFO% 개발 프로파일로 패키징 중...
)

if "%SKIP_TESTS%"=="true" (
    set "PACKAGE_CMD=%PACKAGE_CMD% -DskipTests"
)

if "%VERBOSE%"=="false" (
    set "PACKAGE_CMD=%PACKAGE_CMD% -q"
)

call %PACKAGE_CMD%

if %errorlevel% neq 0 (
    echo %ERROR% 패키징 실패
    pause
    exit /b 1
)
echo %SUCCESS% 패키징 완료

REM 빌드 결과 확인
echo %STEP% 8/8 빌드 결과 확인

set "WAR_FILE=target\disc-assessment.war"
if exist "%WAR_FILE%" (
    for %%A in ("%WAR_FILE%") do set "WAR_SIZE=%%~zA"
    set /a "WAR_SIZE_MB=WAR_SIZE/1024/1024"
    echo %SUCCESS% WAR 파일 생성 완료: %WAR_FILE% ^(!WAR_SIZE_MB!MB^)

    REM 빌드 정보 파일 생성
    set "BUILD_INFO_FILE=target\build-info.txt"
    echo # DISC 애플리케이션 빌드 정보 > "!BUILD_INFO_FILE!"
    echo 빌드 시간: %date% %time% >> "!BUILD_INFO_FILE!"
    echo 빌드 타입: %BUILD_TYPE% >> "!BUILD_INFO_FILE!"
    echo Git 브랜치: !GIT_BRANCH! >> "!BUILD_INFO_FILE!"
    echo Git 커밋: !GIT_COMMIT! >> "!BUILD_INFO_FILE!"
    echo WAR 파일 크기: !WAR_SIZE_MB!MB >> "!BUILD_INFO_FILE!"

    echo %INFO% 빌드 정보: !BUILD_INFO_FILE!

) else (
    echo %ERROR% WAR 파일이 생성되지 않았습니다.
    pause
    exit /b 1
)

REM 성공 메시지
echo.
echo ==============================================
echo               빌드 성공!
echo ==============================================
echo.
echo %SUCCESS% 빌드 타입: %BUILD_TYPE%
echo %SUCCESS% 출력 파일: %WAR_FILE%
echo %SUCCESS% 파일 크기: !WAR_SIZE_MB!MB
echo.
echo %INFO% 다음 단계:
echo   1. 배포: scripts\deploy.bat
echo   2. 샘플 데이터 생성: scripts\generate-sample-data.bat
echo   3. Tomcat에 수동 배포: copy %WAR_FILE% %%TOMCAT_HOME%%\webapps\

REM 배포 스크립트 실행 여부 확인
if "%BUILD_TYPE%"=="production" (
    echo.
    set /p "choice=바로 배포하시겠습니까? (y/N): "
    if /i "!choice!"=="y" (
        echo %INFO% 배포 스크립트 실행 중...
        call "%SCRIPT_DIR%deploy.bat" --war-file "%WAR_FILE%"
    )
)

:end
pause