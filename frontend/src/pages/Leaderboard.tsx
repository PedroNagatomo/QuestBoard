import { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import toast from 'react-hot-toast';
import { Crown, Trophy, Medal } from 'lucide-react';
import { leaderboardApi, LeaderboardEntry } from '../api/leaderboard';
import { extractApiError } from '../api/client';
import { PLAYER_CLASSES, fmtXp } from '../lib/format';
import { useAuthStore } from '../store/authStore';

type Period = 'week' | 'month' | 'all-time';

export default function Leaderboard() {
  const me = useAuthStore((s) => s.user);
  const connectWs = useAuthStore((s) => s.connectWebSocket);
  const [period, setPeriod] = useState<Period>('all-time');
  const [entries, setEntries] = useState<LeaderboardEntry[]>([]);
  const [loading, setLoading] = useState(true);

  async function load(p: Period) {
    setLoading(true);
    try {
      const res = await leaderboardApi.top(p, 100);
      setEntries(res.entries);
    } catch (e) {
      toast.error(extractApiError(e));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { load(period); }, [period]);

  // WebSocket: atualiza em tempo real
  useEffect(() => {
    connectWs({
      onLeaderboard: (data) => {
        if (data?.period === 'all-time' && period === 'all-time' && Array.isArray(data.entries)) {
          setEntries((prev) => prev.map((e) => {
            const fresh = data.entries.find((x: any) => x.userId === e.userId);
            return fresh ? { ...e, score: fresh.score, level: fresh.level } : e;
          }));
        }
      },
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [period]);

  return (
    <div className="space-y-5">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold">Ranking</h1>
          <p className="text-sm text-text-muted">Acompanhe em tempo real quem está subindo.</p>
        </div>
        <div className="flex gap-2">
          {(['week','month','all-time'] as Period[]).map((p) => (
            <button key={p}
              onClick={() => setPeriod(p)}
              className={`px-3 py-1.5 rounded-full text-xs font-medium border transition ${
                period === p
                  ? 'bg-brand/20 border-brand/40 text-brand-soft'
                  : 'bg-bg-soft border-border text-text-muted hover:text-text'
              }`}
            >
              {p === 'week' ? 'Semana' : p === 'month' ? 'Mês' : 'Geral'}
            </button>
          ))}
        </div>
      </div>

      <div className="card overflow-hidden p-0">
        {loading ? (
          <p className="p-6 text-text-muted">Carregando…</p>
        ) : entries.length === 0 ? (
          <p className="p-6 text-text-muted">Sem dados ainda. Complete uma quest!</p>
        ) : (
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-border text-text-muted">
                <th className="text-left py-3 px-4 w-16">#</th>
                <th className="text-left py-3 px-4">Jogador</th>
                <th className="text-left py-3 px-4 hidden md:table-cell">Classe</th>
                <th className="text-right py-3 px-4">Nível</th>
                <th className="text-right py-3 px-4">Score</th>
              </tr>
            </thead>
            <tbody>
              {entries.map((e, i) => {
                const cls = PLAYER_CLASSES[e.playerClass] ?? PLAYER_CLASSES.OTHER;
                const isMe = e.userId === me?.id;
                return (
                  <motion.tr
                    key={e.userId}
                    initial={{ opacity: 0, x: -8 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ delay: Math.min(i * 0.02, 0.4) }}
                    className={`border-b border-border/50 ${isMe ? 'bg-brand/10' : 'hover:bg-bg-soft/40'}`}
                  >
                    <td className="py-3 px-4">
                      {e.rank === 1 ? <Crown className="text-gold" size={18} /> :
                       e.rank === 2 ? <Trophy className="text-slate-300" size={18} /> :
                       e.rank === 3 ? <Medal className="text-amber-600" size={18} /> :
                       <span className="text-text-muted">{e.rank}</span>}
                    </td>
                    <td className="py-3 px-4">
                      <div className="flex items-center gap-3">
                        <div className="w-8 h-8 rounded-full bg-gradient-to-br from-brand to-xp flex items-center justify-center text-xs font-bold">
                          {(e.displayName ?? e.username)[0].toUpperCase()}
                        </div>
                        <div>
                          <p className="font-medium">{e.displayName ?? e.username}</p>
                          <p className="text-xs text-text-muted">@{e.username}</p>
                        </div>
                      </div>
                    </td>
                    <td className={`py-3 px-4 hidden md:table-cell ${cls.color}`}>
                      {cls.emoji} {cls.label}
                    </td>
                    <td className="py-3 px-4 text-right font-mono">{e.level}</td>
                    <td className="py-3 px-4 text-right font-semibold text-xp">{fmtXp(e.score)} XP</td>
                  </motion.tr>
                );
              })}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}