# Claude Code 프로젝트 설정

## 자동화 설정

### Git 자동 커밋/푸시
- **설정**: 모든 코드 변경사항은 자동으로 커밋하고 GitHub에 푸시
- **동작**: 파일 수정 완료 시 즉시 git add → commit → push 실행
- **커밋 메시지 형식**: feat/fix/refactor 등의 conventional commit 사용

### 개발 워크플로우
1. 코드 수정
2. 자동 git add (수정된 모든 파일)
3. 자동 커밋 (의미있는 메시지와 함께)
4. 자동 GitHub 푸시

## 프로젝트 정보
- **프로젝트명**: DISC 성격유형 검사 시스템
- **기술스택**: HTML, CSS, JavaScript, Canvas API
- **주요기능**: DISC 검사, 결과 분석, 관리자 대시보드

## 명령어
- `npm run lint`: 코드 린팅 (해당사항 없음)
- `npm run test`: 테스트 실행 (해당사항 없음)
- 정적 HTML 프로젝트로 빌드 과정 없음