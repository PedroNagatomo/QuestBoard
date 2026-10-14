import { api } from './client';

export interface LootItem { id: string; name: string; description?: string; imageUrl?: string;
  costCoins: number; stock?: number; rarity: string; active: boolean; }

export const lootApi = {
  catalog: () => api.get('/marketplace', { params: { size: 50 } }).then((r) => r.data),
  redeem:  (lootId: string) => api.post('/marketplace/redeem', { lootId }).then((r) => r.data),
};