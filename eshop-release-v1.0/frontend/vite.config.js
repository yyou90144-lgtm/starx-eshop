import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver, VantResolver } from 'unplugin-vue-components/resolvers'

export default defineConfig({
  plugins: [
    vue(),
    Components({
      resolvers: [
        ElementPlusResolver({ importStyle: 'css' }),
        VantResolver(),
      ],
    }),
  ],
  build: {
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (!id.includes('node_modules')) return undefined
          if (id.includes('element-plus')) return 'element-plus'
          if (id.includes('vant')) return 'vant'
          if (id.includes('axios')) return 'axios'
          if (
            id.includes('/vue/')
            || id.includes('\\vue\\')
            || id.includes('vue-router')
            || id.includes('pinia')
          ) {
            return 'vue-vendor'
          }
          return undefined
        },
      },
    },
    chunkSizeWarningLimit: 900,
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
