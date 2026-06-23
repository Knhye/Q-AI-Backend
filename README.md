# Q&AI — AI 기반 개발자 기술 면접 준비 서비스

> 경북 소프트웨어 마이스터 고등학교 캡스톤 프로젝트

AI를 활용해 백엔드·프론트엔드·AI 분야의 기술 면접 질문을 자동 생성하고, 사용자 답변에 대한 AI 피드백을 제공하는 **백엔드 API 서버**입니다. 노트북으로 질의응답을 분류·저장하고, FCM 푸시 알림으로 꾸준한 학습을 독려합니다.

---

## 기술 스택

| 분류 | 기술 |
|---|---|
| Framework | Spring Boot 3.5.7 |
| Language | Java 21 |
| ORM | Spring Data JPA (Hibernate) |
| Database | MySQL 8.0 |
| Migration | Flyway |
| Auth | JWT (JJWT 0.12.6) + Apple OAuth 2.0 |
| Cache / Session | Redis (Apache Commons Pool2) |
| AI | OpenAI GPT (openai-gpt3-java 0.18.2) |
| Push | Firebase Admin SDK 9.2.0 (FCM) |
| HTTP Client | Spring WebFlux (WebClient) |
| API Docs | Springdoc OpenAPI 2.8.6 (Swagger UI) |
| Build | Gradle 8.14.3 |
| Container | Docker (multi-stage, eclipse-temurin:21-jre-alpine) |

---

## 주요 기능

- **AI 면접 질문 생성** — 과목·세부 주제·난이도 조합으로 GPT 기반 질문 자동 생성
- **AI 피드백** — 사용자 답변을 분석해 강점·약점을 포함한 피드백 자동 제공
- **노트북** — Q&A를 커스텀 컬렉션으로 분류·저장·관리
- **푸시 알림** — FCM을 통한 학습 독려 알림, 구독 설정 및 읽음 처리
- **인증** — 이메일/비밀번호 회원가입·로그인, Apple OAuth, 토큰 갱신, 블랙리스트 로그아웃
- **사용자 관리** — 프로필 수정, 비밀번호 변경, FCM 토큰 등록, 회원탈퇴

---

## 질문 분류 체계

### 과목 (Subject)

| 코드 | 설명 |
|---|---|
| `BACKEND` | 백엔드 |
| `FRONTEND` | 프론트엔드 |
| `AI` | 인공지능 |

### 난이도 (Level)

| 코드 | 설명 |
|---|---|
| `EASY` | 쉬움 |
| `NORMAL` | 보통 |
| `HARD` | 어려움 |

### 세부 주제 (SubjectDetail)

| 과목 | 코드 | 설명 |
|---|---|---|
| BACKEND | `SERVER_ARCHITECTURE` | 서버 기본 구조 |
| | `API_DESIGN` | API 설계 |
| | `AUTHENTICATION_AUTHORIZATION` | 인증 · 인가 |
| | `DATABASE` | 데이터베이스 |
| | `ORM_DATA_ACCESS` | ORM / 데이터 접근 |
| | `PERFORMANCE_SCALABILITY` | 성능 · 확장성 |
| | `ERROR_HANDLING_STABILITY` | 에러 처리 · 안정성 |
| FRONTEND | `BROWSER_RUNTIME` | 브라우저 동작 원리 |
| | `JAVASCRIPT_CORE` | JavaScript 핵심 |
| | `REACT_FRAMEWORK` | 프레임워크 (React) |
| | `STATE_MANAGEMENT` | 상태 관리 |
| | `FRONTEND_PERFORMANCE` | 성능 최적화 |
| | `FRONTEND_NETWORK` | 네트워크 |
| | `UI_UX_PERSPECTIVE` | UI · UX 관점 |
| AI | `MACHINE_LEARNING_FOUNDATION` | 머신러닝 기본 개념 |
| | `MODEL_TRAINING_PROCESS` | 모델 학습 과정 |
| | `DEEP_LEARNING_FOUNDATION` | 딥러닝 기본 |
| | `MODEL_EVALUATION` | 모델 성능 평가 |
| | `SERVICE_APPLICATION` | 서비스 적용 관점 |
| | `DATA_AND_ETHICS` | 데이터 · 윤리 |

---

## 시작하기

### 사전 요구사항

- Java 21+
- MySQL 8.0
- Redis
- Firebase 프로젝트 (FCM)
- OpenAI API Key

### 빌드

```bash
./gradlew clean build
```

### 환경 변수 설정

`src/main/resources/application.yml`을 생성하고 아래 항목을 설정합니다.

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/qnai_db
    username: YOUR_DB_USER
    password: YOUR_DB_PASSWORD
  data:
    redis:
      host: localhost
      port: 6379

jwt:
  secret: YOUR_JWT_SECRET_KEY_MIN_32_CHARS
  access-token-expiration: 3600000    # 1시간 (ms)
  refresh-token-expiration: 604800000 # 7일 (ms)

openai:
  api-key: YOUR_OPENAI_API_KEY

firebase:
  project-id: YOUR_FIREBASE_PROJECT_ID
```

Firebase 서비스 계정 JSON 파일을 `src/main/resources/firebase-service-account.json`에 위치시킵니다.

### 데이터베이스 마이그레이션

Flyway가 애플리케이션 시작 시 `db/migration/` 하위 스크립트를 자동으로 실행합니다.

---

## 실행

```bash
# 개발 실행
./gradlew bootRun

# 프로덕션 빌드 후 실행
./gradlew clean build -x test
java -jar build/libs/*SNAPSHOT.jar

# Docker
docker build -t qnai:latest .
docker run -p 8080:8080 qnai:latest
```

서버가 실행되면 Swagger UI에서 API 문서를 확인할 수 있습니다.

```
http://localhost:8080/swagger-ui.html
```

---

## API 개요

| 태그 | 엔드포인트 | 설명 |
|---|---|---|
| **Auth** | `POST /api/auth/signup` | 회원가입 |
| | `POST /api/auth/login` | 로그인 |
| | `POST /api/auth/login/apple` | Apple 소셜 로그인 |
| | `POST /api/auth/refresh` | 액세스 토큰 재발급 |
| | `POST /api/auth/logout` | 로그아웃 (토큰 블랙리스트) |
| | `DELETE /api/auth/{id}` | 회원탈퇴 |
| **QnA** | `POST /api/qna/question` | AI 면접 질문 생성 |
| | `GET /api/qna/recent` | 최신 질문 목록 조회 |
| | `GET /api/qna/{id}` | Q&A 상세 조회 |
| | `PATCH /api/qna/{id}` | 답변 수정 |
| | `POST /api/qna/feedback` | AI 피드백 생성 |
| | `DELETE /api/qna/{id}` | Q&A 삭제 |
| **Notebook** | `POST /api/notebook` | 노트북 생성 |
| | `GET /api/notebook` | 노트북 목록 조회 |
| | `GET /api/notebook/{id}` | 노트북 상세 조회 |
| | `DELETE /api/notebook/{id}` | 노트북 삭제 |
| | `POST /api/notebook/items` | 노트북에 Q&A 추가 |
| | `DELETE /api/notebook/items` | 노트북에서 Q&A 제외 |
| **User** | `GET /api/user` | 내 정보 조회 |
| | `PUT /api/user` | 내 정보 수정 |
| | `PATCH /api/user/password` | 비밀번호 변경 |
| | `PATCH /api/user/fcm` | FCM 토큰 등록/갱신 |
| **Notification** | `GET /api/notification` | 알림 목록 조회 |
| | `PATCH /api/notification` | 알림 읽음 처리 |
| | `PATCH /api/notification/settings` | 알림 구독/해지 설정 |

---

## 스크립트

| 명령 | 설명 |
|---|---|
| `./gradlew bootRun` | 개발 서버 실행 |
| `./gradlew clean build` | 프로덕션 빌드 |
| `./gradlew test` | 단위 테스트 실행 |
| `docker build -t qnai:latest .` | Docker 이미지 빌드 |

---

## 프로젝트 구조

```
src/
└── main/java/com/example/qnai/
    ├── QnaiApplication.java        # 애플리케이션 진입점
    ├── common/                     # 공통 응답 래퍼, AI 헬스체커
    ├── config/                     # Security, JWT, Redis, Firebase, Swagger, WebClient
    ├── controller/                 # REST 컨트롤러 (Auth, QnA, Notebook, User, Notification)
    ├── dto/                        # 요청/응답 DTO (fcm, gpt, notebook, notification, qna, user)
    ├── entity/                     # JPA 엔티티
    │   ├── enums/                  # Subject, SubjectDetail, Level
    │   └── Users, QnA, Notebook, Notification, RefreshToken, UserNotificationSetting
    ├── global/
    │   ├── exception/              # 커스텀 예외 클래스
    │   └── handler/                # 전역 예외 핸들러
    ├── repository/                 # 데이터 접근 계층 (JPA + Redis)
    │   └── adapter/                # JPA / Redis 어댑터
    ├── scheduler/                  # 푸시 알림 스케줄러 (Cron)
    └── service/                    # 비즈니스 로직 (Auth, QnA, Notebook, User, Notification, AI, FCM, AppleOAuth)
```

---

## 라이선스

Private — 경북 소프트웨어 마이스터 고등학교 캡스톤 프로젝트
