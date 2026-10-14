import axios, { AxiosError } from 'axios';
import { useAuthStore } from '../store/authStore';

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? '/api',
  headers: { 'Content-Type': 'application/json' },
});

// Envia o accessToken em toda requisição
api.interceptors.request.use((config) => {
  const token = useAuthStore.getState().accessToken;
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Se 401, tenta refresh uma vez; se falhar, logout
let refreshing: Promise<string | null> | null = null;

api.interceptors.response.use(
  (r) => r,
  async (err: AxiosError) => {
    const original: any = err.config;
    if (err.response?.status === 401 && !original._retry) {
      original._retry = true;
      if (!refreshing) {
        refreshing = useAuthStore.getState().refresh().finally(() => { refreshing = null; });
      }
      const newToken = await refreshing;
      if (newToken) {
        original.headers.Authorization = `Bearer ${newToken}`;
        return api(original);
      }
      useAuthStore.getState().logout();
    }
    return Promise.reject(err);
  }
);

export function extractApiError(e: unknown): string {
  if (axios.isAxiosError(e)) {
    const data = e.response?.data as any;
    return data?.message ?? data?.error ?? e.message;
  }
  return (e as Error)?.message ?? 'Erro inesperado';
}