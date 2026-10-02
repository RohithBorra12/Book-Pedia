import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// In development (npm run dev) these proxies send API calls to the services on your machine.
// In Docker/Kubernetes, nginx.conf does the same job.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      "/api/auth": "http://localhost:8081",
      "/api/users": "http://localhost:8081",
      "/api/books": "http://localhost:8082",
      "/api/orders": "http://localhost:8083",
    },
  },
});
