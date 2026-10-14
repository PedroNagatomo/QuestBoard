import { api } from './client';

export interface LeaderboardEntry {
  rank: number;
  userId: string;
  username: string;
  displayName: string;
  avatarUrl?: string;
  playerClass: string;
  level: number;
  score: number;
}

export interface LeaderboardResponse {
  period: string;
  scope: string;
  entries: LeaderboardEntry[];
  generatedAt: number;
}

export const leaderboardApi = {
  top: (period: 'all-time'|'week'|'month' = 'all-time', limit = 20) =>
    api.get<LeaderboardResponse>('/leaderboard', { params: { period, limit } }).then((r) => r.data),
};