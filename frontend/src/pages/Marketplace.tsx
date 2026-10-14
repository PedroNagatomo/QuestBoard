import { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import toast from 'react-hot-toast';
import { Gift, Coins } from 'lucide-react';
import { lootApi, LootItem } from '../api/loot';
import { extractApiError } from '../api/client';
import { RARITY } from '../lib/format';
import { useAuthStore } from '../store/authStore';
import { usersApi } from '../api/users';

export default function Marketplace() {
  const { user, setUser } = useAuthStore();
  const [items, setItems] = useState<LootItem[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        const page: any = await lootApi.catalog();
        setItems(page.content ?? []);
      } catch (e) {
        toast.error(extractApiError(e));
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  async function redeem(item: LootItem) {
    if (!user) return;
    if (user.coins < item.costCoins) {
      toast.error('Coins insuficientes!');
      return;
    }
    try {
      await lootApi.redeem(item.id);
      toast.success(`Resgatado: ${item.name}! 🎁`);
      setUser({ ...user, coins: user.coins - item.costCoins });
      const fresh: any = await lootApi.catalog();
      setItems(fresh.content ?? []);
    } catch (e) {
      toast.error(extractApiError(e));
    }
  }

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">Marketplace</h1>
          <p className="text-sm text-text-muted">Gaste coins em recompensas reais.</p>
        </div>
        <div className="badge-pill">
          <Coins size={14} className="text-gold" />
          <span className="font-semibold">{user?.coins ?? 0} coins</span>
        </div>
      </div>

      {loading ? <p className="text-text-muted">Carregando…</p> :
        items.length === 0 ? (
          <div className="card text-center py-12">
            <Gift className="mx-auto text-text-dim mb-3" size={40} />
            <p className="text-text-muted">Sem itens no marketplace.</p>
          </div>
        ) : (
          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
            {items.map((it, i) => {
              const r = RARITY[it.rarity as keyof typeof RARITY] ?? RARITY.COMMON;
              const canAfford = (user?.coins ?? 0) >= it.costCoins;
              return (
                <motion.div
                  key={it.id}
                  initial={{ opacity: 0, y: 12 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: i * 0.04 }}
                  className={`card border-2 ${r.color}`}
                >
                  <div className="text-center py-4">
                    <div className="text-4xl mb-2">
                      {it.rarity === 'LEGENDARY' ? '👑' : it.rarity === 'EPIC' ? '💎' : it.rarity === 'RARE' ? '⭐' : '🎁'}
                    </div>
                    <p className={`text-xs font-bold uppercase tracking-wider mb-1 ${r.color.split(' ')[1]}`}>
                      {r.label}
                    </p>
                    <h3 className="font-semibold">{it.name}</h3>
                    {it.description && <p className="text-xs text-text-muted mt-1 line-clamp-2">{it.description}</p>}
                  </div>
                  <div className="border-t border-border/60 pt-3 flex items-center justify-between">
                    <div className="flex items-center gap-1 text-gold font-semibold">
                      <Coins size={14} />
                      {it.costCoins}
                    </div>
                    <button
                      onClick={() => redeem(it)}
                      disabled={!canAfford || it.stock === 0}
                      className="btn-primary text-xs px-3 py-1"
                    >
                      {it.stock === 0 ? 'Esgotado' : canAfford ? 'Resgatar' : 'Sem coins'}
                    </button>
                  </div>
                </motion.div>
              );
            })}
          </div>
        )}
    </div>
  );
}