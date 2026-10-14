import { api } from './client';

export interface Guild { id: string; name: string; description?: string; emblemUrl?: string; totalXp: number; level: number; ownerId: string; }
export interface GuildMember { id: string; username: string; displayName?: string; avatarUrl?: string; playerClass: string; level: number; totalXp: number; }

export const guildsApi = {
  list:    (page = 0, size = 20) => api.get('/guilds', { params: { page, size } }).then((r) => r.data),
  get:     (id: string) => api.get<Guild>(`/guilds/${id}`).then((r) => r.data),
  members: (id: string) => api.get<GuildMember[]>(`/guilds/${id}/members`).then((r) => r.data),
  create:  (p: { name: string; description?: string; emblemUrl?: string }) =>
              api.post<Guild>('/guilds', p).then((r) => r.data),
  join:    (id: string) => api.post(`/guilds/${id}/join`).then((r) => r.data),
  leave:   () => api.post(`/guilds/leave`).then((r) => r.data),
};