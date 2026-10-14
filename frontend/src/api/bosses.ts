import { api } from './client';

export interface Boss { id: string; name: string; description?: string; imageUrl?: string;
  totalHp: number; currentHp: number; hpPercent: number; status: string;
  startsAt?: string; endsAt?: string; guildId?: string; }

export const bossesApi = {
  listActive: () => api.get<Boss[]>('/bosses').then((r) => r.data),
  get:        (id: string) => api.get<Boss>(`/bosses/${id}`).then((r) => r.data),
  create:     (p: { name: string; description?: string; totalHp: number; guildId?: string }) =>
                  api.post<Boss>('/bosses', p).then((r) => r.data),
  damage:     (id: string, damage: number) =>
                  api.post<Boss>(`/bosses/${id}/damage`, { damage }).then((r) => r.data),
};