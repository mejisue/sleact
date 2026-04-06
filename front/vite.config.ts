import { defineConfig } from 'vitest/config'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import path from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  define: {
    global: 'globalThis',
  },
  test: {
    // 브라우저 DOM 환경 시뮬레이션 (React 컴포넌트 테스트에 필수)
    environment: 'jsdom',
    // 각 테스트 파일 실행 전 setup 파일 실행
    setupFiles: ['./src/test/setup.ts'],
    // Jest처럼 전역 함수(describe, it, expect) 사용 가능
    globals: true,
  },
})
