# SpeakNote

SpeakNote는 웹에서 음성을 녹음하고,  
음성 인식(STT)과 화자 분리, 대화 요약 처리하여  
관리할 수 있는 개인 프로젝트입니다.

브라우저에서 녹음한 음성을 서버로 업로드하고,  
AI 처리를 거쳐 다시 재생 가능한 형태로 제공합니다.

URL: https://speaknote-415073718712.asia-northeast3.run.app/
---

## 주요 기능

- 브라우저 기반 음성 녹음 (WAV)
- 음성 인식 및 화자 분리
- 대화 정제 및 요약 생성
- 녹음 파일 저장 및 재생
- 히스토리 조회 (커서 기반 페이징)

---

## 기술 스택

Backend
- Java 17
- Spring Boot
- MyBatis
- MySQL 8
- Flyway
- SLF4J + Logback

Frontend
- JavaScript
- HTML과 CSS는 생성형 AI를 사용하여 제작하였습니다.

외부 라이브러리
- extendable-media-recorder (https://github.com/chrisguttandin/extendable-media-recorder)

Infra
- Google Cloud Run
- Google Compute Engine VM

AI
- OpenAI API
    - 음성 인식 및 화자 분리
    - 대화 요약 생성

---

## 데이터베이스 마이그레이션 (Flyway)

Flyway를 사용해 데이터베이스를 관리합니다.

- 애플리케이션 시작 시 마이그레이션 자동 실행
- 환경별 데이터베이스 불일치 방지
- 데이터베이스 변경 이력 추적 가능

마이그레이션 SQL은 `db/migration` 디렉터리에서 관리됩니다.

---

## 스케줄러 (만료 데이터 정리)

Spring Scheduler를 사용해  
녹음 파일과 DB 데이터의 수명을 관리합니다.

- 매일 자정 스케줄 실행
- 일정 기간이 지난 음성 파일 삭제
- 파일 삭제 성공 시에만 DB 레코드 삭제

cron 설정:

@Scheduled(cron = "0 0 0 * * *")

이를 통해 불필요한 저장 공간 사용을 방지합니다.

---

## 로깅 

SLF4J + Logback 기반 로깅을 사용하여 로그 파일을 남깁니다.

주요 로그 대상:
- 음성 업로드 요청
- AI API 호출 성공 및 실패
- DB 저장 및 조회
- 스케줄러 실행 결과
- 파일 삭제 성공 및 실패

### 요청 식별자 관리

- recorderId를 쿠키로 발급
- Filter에서 MDC에 recorderId를 주입
- 요청 단위 로그 추적 가능

MDC.put("recorderId", recorderId);

운영 중 특정 사용자 요청 흐름을 쉽게 추적할 수 있습니다.

---
## 주석

- JavaDoc 형태의 주석으로 코드의 가독성 확보
---
## 예외 처리 설계

- 예외 발생 시 예외를 상위로 전파하지 않음
- 결과 객체(success 여부)로 흐름 제어

---

## 접근 제어

- 오디오 파일 직접 경로 접근 차단
- 세션 기반으로 허용된 파일만 스트리밍 가능
- 히스토리 조회 시 접근 권한 자동 갱신

GET /api/audio/{idx} 요청 시  
서버에서 세션 검증 후 스트리밍을 허용합니다.

---

