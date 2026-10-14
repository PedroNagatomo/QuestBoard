import { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import toast from 'react-hot-toast';
import { Sword, Flame, Trophy, Coins, Skull, Award } from 'lucide-react';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts';
import { usersApi, UserProgress } from '../api/users';
import { useAuthStore } from '../store/authStore';
import LevelBadge from '../components/LevelBadge';
import XpBar from '../components/XpBar';
import { PLAYER_CLASSES } from '../lib/format';
import { extractApiError } from '../api/client';

export default function Dashboard() {
  const { user, setUser } = useAuthStore();
  const [progress, setProgress] = useState<UserProgress | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try {
        const [p, me] = await Promise.all([usersApi.progress(), usersApi.me()]);
        setProgress(p);
        setUser(me);
      } catch (e) {
        toast.error(extractApiError(e));
      } finally {
        setLoading(false);
      }
    })();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  if (loading) return <div className="text-text-muted">Carregando dashboard…</div>;
  if (!progress || !user) return null;

  const cls = PLAYER_CLASSES[user.playerClass] ?? PLAYER_CLASSES.OTHER;

  // Mock de XP últimos 7 dias (substituiremos quando tivermos endpoint)
  const xpSeries = Array.from({ length: 7 }).map((_, i) => ({
    day: new Date(Date.now() - (6 - i) * 86400000).toLocaleDateString('pt-BR', { weekday: 'short' }),
    xp: Math.round((progress.totalXp / 7) * (0.5 + Math.random() * 1.2)),
  }));

  const stats = [
    { icon: Sword,  label: 'Quests completas', value: progress.questsCompleted, color: 'text-sky-400' },
    { icon: Flame,  label: 'Streak atual',     value: `${progress.currentStreak}d`, color: 'text-orange-400' },
    { icon: Trophy, label: 'Recorde de streak',value: `${progress.longestStreak}d`, color: 'text-gold' },
    { icon: Coins,  label: 'Coins',            value: progress.coins, color: 'text-gold' },
    { icon: Skull,  label: 'Bosses derrotados',value: progress.bossesDefeated, color: 'text-fuchsia-400' },
    { icon: Award,  label: 'Badges',           value: progress.badges.length, color: 'text-brand-soft' },
  ];

  return (
    <div className="space-y-6">
      <motion.div initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }} className="card">
        <div className="flex items-center gap-5">
          <LevelBadge level={user.level} size="lg" />
          <div className="flex-1">
            <div className="flex items-baseline gap-3">
              <h1 className="text-2xl font-bold">{user.displayName ?? user.username}</h1>
              <span className={`text-sm font-medium ${cls.color}`}>{cls.emoji} {cls.label}</span>
            </div>
            <p className="text-sm text-text-muted mb-3">
              {progress.totalXp.toLocaleString('pt-BR')} XP totais
            </p>
            <XpBar xp={progress.xp} xpToNext={progress.xpToNext} />
          </div>
        </div>
      </motion.div>

      <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-3">
        {stats.map((s, i) => (
          <motion.div
            key={s.label}
            initial={{ opacity: 0, y: 12 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: i * 0.05 }}
            className="card"
          >
            <s.icon className={`${s.color} mb-2`} size={20} />
            <p className="text-xs text-text-muted">{s.label}</p>
            <p className="text-xl font-bold">{s.value}</p>
          </motion.div>
        ))}
      </div>

      <div className="grid lg:grid-cols-2 gap-4">
        <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="card">
          <h2 className="font-semibold mb-4">XP — últimos 7 dias</h2>
          <div className="h-56">
            <ResponsiveContainer>
              <BarChart data={xpSeries}>
                <CartesianGrid strokeDasharray="3 3" stroke="#242a37" />
                <XAxis dataKey="day" stroke="#8b93a7" fontSize={12} />
                <YAxis stroke="#8b93a7" fontSize={12} />
                <Tooltip
                  contentStyle={{ background: '#161a23', border: '1px solid #242a37', borderRadius: 8 }}
                  labelStyle={{ color: '#8b93a7' }}
                />
                <Bar dataKey="xp" fill="#7c5cff" radius={[6, 6, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </motion.div>

        <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="card">
          <h2 className="font-semibold mb-4">Badges conquistadas</h2>
          {progress.badges.length === 0 ? (
            <p className="text-text-muted text-sm">Nenhuma badge ainda. Complete quests para ganhar!</p>
          ) : (
            <div className="grid grid-cols-3 gap-3">
              {progress.badges.map((b) => (
                <div key={b.code} className="card-soft bg-bg-soft border border-border rounded-lg p-3 text-center">
                  <div className="text-3xl mb-1">{b.rarity === 'LEGENDARY' ? '👑' : b.rarity === 'EPIC' ? '💎' : b.rarity === 'RARE' ? '⭐' : '🏅'}</div>
                  <p className="text-xs font-medium">{b.name}</p>
                  <p className="text-[10px] text-text-dim">{b.rarity}</p>
                </div>
              ))}
            </div>
          )}
        </motion.div>
      </div>

      <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="card">
        <h2 className="font-semibold mb-4">Achievements</h2>
        {progress.achievements.length === 0 ? (
          <p className="text-text-muted text-sm">Sem progresso em achievements ainda.</p>
        ) : (
          <div className="space-y-3">
            {progress.achievements.map((a) => {
              const pct = Math.min(100, (a.progress / a.target) * 100);
              return (
                <div key={a.code}>
                  <div className="flex justify-between text-sm mb-1">
                    <span>{a.completed ? '🏆 ' : ''}{a.name}</span>
                    <span className="text-text-muted">{a.progress} / {a.target}</span>
                  </div>
                  <div className="h-2 bg-bg-soft rounded-full overflow-hidden">
                    <motion.div
                      className={`h-full ${a.completed ? 'bg-gold' : 'bg-brand'}`}
                      initial={{ width: 0 }}
                      animate={{ width: `${pct}%` }}
                      transition={{ duration: 0.8 }}
                    />
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </motion.div>
    </div>
  );
}