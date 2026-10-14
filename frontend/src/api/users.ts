import { api } from './client';
import { UserResponse } from './auth';

export interface UserProgress {
  userId: string;
  level: number;
  xp: number;
  xpToNext: number;
  progressPercent: number;
  totalXp: number;
  coins: number;
  currentStreak: number;
  longestStreak: number;
  questsCompleted: number;
  bossesDefeated: number;
  badges: { code: string; name: string; iconUrl?: string; rarity: string }[];
  achievements: { code: string; name: string; progress: number; target: number; completed: boolean }[];
}

export const usersApi = {
  me:        () => api.get<UserResponse>('/users/me').then((r) => r.data),
  updateMe:  (p: { displayName?: string; avatarUrl?: string; playerClass?: string }) =>
                api.patch<UserResponse>('/users/me', p).then((r) => r.data),
  progress:  (id?: string) =>
                api.get<UserProgress>(id ? `/users/${id}/progress` : '/users/me/progress').then((r) => r.data),
  badges:    () => api.get(`/users/me/badges`).then((r) => r.data),
  list:      (q?: string, page = 0, size = 20) =>
                api.get('/users', { params: { q, page, size } }).then((r) => r.data),
};