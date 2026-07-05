import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
  },
  define: {
    // sockjs-client references the Node.js `global` object; map it to
    // `globalThis` so it works in the browser.
    global: 'globalThis',
  },
});
