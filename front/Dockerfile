# ─────────────────────────────────────────
# Stage 1: Build
# ─────────────────────────────────────────
FROM node:20-alpine AS builder

RUN npm install -g pnpm

WORKDIR /app

# 의존성 파일만 먼저 복사해 레이어 캐시 활용
COPY package.json pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile

COPY . .

# 빌드 시점에 API URL 주입 (--build-arg로 오버라이드 가능)
ARG VITE_API_BASE_URL
ARG VITE_WS_BASE_URL
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
ENV VITE_WS_BASE_URL=$VITE_WS_BASE_URL

RUN pnpm build

# ─────────────────────────────────────────
# Stage 2: Serve
# ─────────────────────────────────────────
FROM nginx:alpine

COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
