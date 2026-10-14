import { api } from './client';

export interface UserResponse {
  id: string;
  username: string;
  email: string;
  displayName: string;
  avatarUrl?: string;
  playerClass: 'DEV'|'DESIGNER'|'PM'|'QA'|'DEVOPS'|'MARKETING'|'OTHER';
  level: number;
  xp: number;
  xpToNextLevel: number;
  totalXp: number;
  coins: number;
  currentStreak: number;
  longestStreak: number;
  guildId?: string;
  guildName?: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: UserResponse;
}

export const authApi = {
  login: (username: string, password: string) =>
    api.post<AuthResponse>('/auth/login', { username, password }).then((r) => r.data),

  register: (payload: {
    username: string; email: string; password: string;
    displayName?: string; playerClass?: string;
  }) => api.post<AuthResponse>('/auth/register', payload).then((r) => r.data),

  refresh: (refreshToken: string) =>
    api.post<AuthResponse>('/auth/refresh', { refreshToken }).then((r) => r.data),

  me: () => api.get<UserResponse>('/auth/me').then((r) => r.data),
};