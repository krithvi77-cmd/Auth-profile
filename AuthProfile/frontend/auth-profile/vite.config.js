import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],

  server: {
    host: true,
    port: 5173,
    allowedHosts: [
      'krithvishai-99v1t1cy-5173.zcodecorp.in'
    ],
    proxy: {
      // Frontend calls /api/... -> proxied to Tomcat at /authPr/api/...
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => '/authPr' + path,
      },
    },
  }
})
