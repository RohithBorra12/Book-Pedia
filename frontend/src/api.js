import axios from "axios";

// All calls go to /api/... ; nginx (or the Vite proxy) forwards them to the right service.
const api = axios.create({ baseURL: "/api" });

export function getSession() {
  const raw = localStorage.getItem("session");
  return raw ? JSON.parse(raw) : null;
}
export function saveSession(session) {
  localStorage.setItem("session", JSON.stringify(session));
}
export function clearSession() {
  localStorage.removeItem("session");
}

// Attach the JWT to every request
api.interceptors.request.use((config) => {
  const session = getSession();
  if (session?.token) config.headers.Authorization = `Bearer ${session.token}`;
  return config;
});

// Token expired or invalid -> back to login
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 && getSession()) {
      clearSession();
      window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);

export function errorMessage(error) {
  if (error.response?.status === 403) return "You don't have permission to do that.";
  return error.response?.data?.message || error.message || "Request failed.";
}

export default api;
