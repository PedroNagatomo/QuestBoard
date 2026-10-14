import { api } from './client';

export type Difficulty = 'TRIVIAL'|'EASY'|'MEDIUM'|'HARD'|'EPIC'|'LEGENDARY';
export type QuestStatus = 'OPEN'|'IN_PROGRESS'|'IN_REVIEW'|'COMPLETED'|'FAILED'|'CANCELLED';

export interface Quest {
  id: string;
  title: string;
  description?: string;
  difficulty: Difficulty;
  status: QuestStatus;
  xpReward: number;
  coinReward: number;
  dueDate?: string;
  completedAt?: string;
  assigneeId?: string;
  assigneeUsername?: string;
  createdById: string;
  guildId?: string;
  parentBossId?: string;
  tags?: string[];
  createdAt: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export const questsApi = {
  list: (params: { status?: QuestStatus; assigneeId?: string; page?: number; size?: number }) =>
    api.get<Page<Quest>>('/quests', { params }).then((r) => r.data),

  get: (id: string) => api.get<Quest>(`/quests/${id}`).then((r) => r.data),

  create: (payload: {
    title: string; description?: string; difficulty?: Difficulty;
    assigneeId?: string; guildId?: string; dueDate?: string; tags?: string[];
  }) => api.post<Quest>('/quests', payload).then((r) => r.data),

  complete: (id: string) => api.post<Quest>(`/quests/${id}/complete`, {}).then((r) => r.data),
  start:    (id: string) => api.post<Quest>(`/quests/${id}/start`, {}).then((r) => r.data),
  cancel:   (id: string) => api.post<Quest>(`/quests/${id}/cancel`, {}).then((r) => r.data),
  remove:   (id: string) => api.delete(`/quests/${id}`).then((r) => r.data),
};