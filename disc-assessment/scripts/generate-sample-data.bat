@echo off
REM ==============================================
REM DISC 애플리케이션 샘플 데이터 생성 스크립트 (Windows)
REM ==============================================

setlocal enabledelayedexpansion

REM 색상 정의 (Windows에서는 제한적)
set "INFO=[INFO]"
set "SUCCESS=[SUCCESS]"
set "WARNING=[WARNING]"
set "ERROR=[ERROR]"

echo ==============================================
echo   DISC 애플리케이션 샘플 데이터 생성 도구
echo ==============================================
echo.

REM 프로젝트 루트 디렉토리 확인
set "SCRIPT_DIR=%~dp0"
set "PROJECT_ROOT=%SCRIPT_DIR%.."

echo %INFO% 프로젝트 루트: %PROJECT_ROOT%

REM Maven 설치 확인
where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo %ERROR% Maven이 설치되어 있지 않습니다. Maven을 먼저 설치해주세요.
    pause
    exit /b 1
)

echo %INFO% Maven 확인됨

REM Java 설치 확인
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo %ERROR% Java가 설치되어 있지 않습니다. JDK 17+를 먼저 설치해주세요.
    pause
    exit /b 1
)

echo %INFO% Java 확인됨

REM 프로젝트 디렉토리로 이동
cd /d "%PROJECT_ROOT%"

REM 확인 메시지
echo.
echo %WARNING% 이 스크립트는 다음 작업을 수행합니다:
echo   • 기존 샘플 데이터 정리
echo   • 10명의 테스트 사용자 생성
echo   • 7명은 검사 완료 상태로 설정
echo   • 3명은 대기중/진행중 상태로 설정
echo   • 다양한 DISC 유형 결과 생성
echo.

set /p "choice=계속하시겠습니까? (y/N): "
if /i not "%choice%"=="y" (
    echo %INFO% 스크립트가 취소되었습니다.
    pause
    exit /b 0
)

REM 1. 프로젝트 컴파일
echo %INFO% 프로젝트 컴파일 중...
call mvn clean compile -q
if %errorlevel% neq 0 (
    echo %ERROR% 컴파일 실패
    pause
    exit /b 1
)
echo %SUCCESS% 컴파일 완료

REM 2. 의존성 복사
if not exist "target\lib" (
    echo %INFO% 의존성 라이브러리 복사 중...
    call mvn dependency:copy-dependencies -DoutputDirectory=target/lib -q
)

REM 3. 클래스패스 설정
set "CLASSPATH=target\classes"
for %%f in (target\lib\*.jar) do (
    set "CLASSPATH=!CLASSPATH!;%%f"
)

REM 4. 샘플 데이터 생성 실행
echo %INFO% 샘플 데이터 생성 중...
echo.

java -cp "%CLASSPATH%" com.disc.util.SampleDataGenerator
if %errorlevel% equ 0 (
    echo.
    echo %SUCCESS% 샘플 데이터 생성이 완료되었습니다!

    echo.
    echo %INFO% 생성된 샘플 사용자 목록:
    echo   1. 김철수 ^(검사완료^)
    echo   2. 이영희 ^(검사완료^)
    echo   3. 박민준 ^(검사완료^)
    echo   4. 최지은 ^(검사완료^)
    echo   5. 정현우 ^(검사완료^)
    echo   6. 강미영 ^(검사완료^)
    echo   7. 윤도현 ^(검사완료^)
    echo   8. 임소진 ^(진행중^)
    echo   9. 조성민 ^(대기중^)
    echo  10. 한예린 ^(대기중^)

    echo.
    echo %INFO% 이제 관리자 페이지에서 샘플 데이터를 확인할 수 있습니다:
    echo   • 대시보드: http://localhost:8080/disc-assessment/admin/dashboard
    echo   • 링크 생성: http://localhost:8080/disc-assessment/admin/create-link

) else (
    echo %ERROR% 샘플 데이터 생성 중 오류가 발생했습니다.
    pause
    exit /b 1
)

echo.
echo ==============================================
echo           샘플 데이터 생성 완료!
echo ==============================================

pause