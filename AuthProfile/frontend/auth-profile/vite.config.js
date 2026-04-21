import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],

    base: '/authPr/',

  build: {
    outDir: path.resolve(__dirname, '../../backend/authPr/src/main/webapp'),
    emptyOutDir: false,
  },

  server: {
    host: true,
    port: 5173,
    allowedHosts: [
      'krithvishai-99v1t1cy-5173.zcodecorp.in'
    ],
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => '/authPr' + path,
      },
    },
  }
})
