# Sleact

Slack을 모티브로 한 실시간 팀 메시징 서비스

## 기술 스택

**Frontend**

| 분류 | 기술 |
|------|------|
| UI | React 19, TailwindCSS v4, shadcn/ui |
| 라우팅 | React Router v7 |
| 서버 상태 | TanStack Query v5 |
| 클라이언트 상태 | Zustand |
| 실시간 통신 | STOMP, SockJS |
| 테스트 | Vitest, Testing Library |

**Backend**

| 분류 | 기술 |
|------|------|
| 프레임워크 | Spring Boot 3.5, Java 17 |
| 데이터베이스 | MySQL 8 (JPA), Redis 7 |
| 실시간 통신 | WebSocket (STOMP) |
| 인증 | JWT |
| 이메일 | Spring Mail |

## 주요 기능

- 워크스페이스 생성·관리 및 멤버 초대
- 채널 생성·참여 및 실시간 채팅 (STOMP/WebSocket)
- 사용자 간 다이렉트 메시지 (DM)
- 이메일 회원가입 / 로그인

## 구조

```
sleact/
├── front/      # React + TypeScript + Vite
└── backend/    # Spring Boot + MySQL + Redis
```

## 로컬 실행

### 사전 요구사항

- Node.js 20+, pnpm
- JDK 17+
- Docker

### Backend

```bash
cd backend

# MySQL + Redis 컨테이너 실행
docker compose up -d

# 서버 실행 (포트 8080)
./gradlew bootRun
```

### Frontend

```bash
cd front
pnpm install
pnpm dev    # http://localhost:5173
```
