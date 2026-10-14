import { FormEvent, useEffect, useMemo, useState } from 'react';
import { AnimatePresence, motion } from 'framer-motion';
import toast from 'react-hot-toast';
import { Plus, Play, CheckCircle2, XCircle, Trash2, Filter } from 'lucide-react';
import { questsApi, Quest, Difficulty, QuestStatus } from '../api/quests';
import { extractApiError } from '../api/client';
import { STATUS, fmtRelTime } from '../lib/format';
import DifficultyTag from '../components/DifficultyTags';
import { useAuthStore } from '../store/authStore';

const DIFFICULTIES: Difficulty[] = ['TRIVIAL','EASY','MEDIUM','HARD','EPIC','LEGENDARY'];
const STATUS_FILTERS: { value: QuestStatus | 'ALL'; label: string }[] = [
  { value: 'ALL',          label: 'Todas' },
  { value: 'OPEN',         label: 'Abertas' },
  { value: 'IN_PROGRESS',  label: 'Em progresso' },
  { value: 'COMPLETED',    label: 'Concluídas' },
  { value: 'CANCELLED',    label: 'Canceladas' },
];

export default function Quests() {
  const me = useAuthStore((s) => s.user);
  const [quests, setQuests] = useState<Quest[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [filter, setFilter] = useState<QuestStatus | 'ALL'>('ALL');
  const [onlyMine, setOnlyMine] = useState(false);

  async function load() {
    setLoading(true);
    try {
      const page = await questsApi.list({
        status: filter === 'ALL' ? undefined : filter,
        assigneeId: onlyMine ? me?.id : undefined,
        size: 100,
      });
      setQuests(page.content);
    } catch (e) {
      toast.error(extractApiError(e));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { load(); /* eslint-disable-next-line */ }, [filter, onlyMine]);

  const grouped = useMemo(() => {
    return {
      active: quests.filter((q) => q.status !== 'COMPLETED' && q.status !== 'CANCELLED' && q.status !== 'FAILED'),
      done:   quests.filter((q) => q.status === 'COMPLETED' || q.status === 'CANCELLED' || q.status === 'FAILED'),
    };
  }, [quests]);

  return (
    <div className="space-y-5">
      <div className="flex flex-wrap items-center gap-3 justify-between">
        <div>
          <h1 className="text-2xl font-bold">Quests</h1>
          <p className="text-sm text-text-muted">Suas missões, como num bom RPG.</p>
        </div>
        <button className="btn-primary" onClick={() => setShowForm((s) => !s)}>
          <Plus size={18} /> Nova quest
        </button>
      </div>

      <div className="card flex flex-wrap items-center gap-3">
        <Filter size={16} className="text-text-muted" />
        <div className="flex flex-wrap gap-2">
          {STATUS_FILTERS.map((f) => (
            <button
              key={f.value}
              onClick={() => setFilter(f.value)}
              className={`px-3 py-1 rounded-full text-xs font-medium border transition ${
                filter === f.value
                  ? 'bg-brand/20 border-brand/40 text-brand-soft'
                  : 'bg-bg-soft border-border text-text-muted hover:text-text'
              }`}
            >
              {f.label}
            </button>
          ))}
        </div>
        <label className="ml-auto flex items-center gap-2 text-sm text-text-muted">
          <input type="checkbox" checked={onlyMine} onChange={(e) => setOnlyMine(e.target.checked)} />
          Apenas minhas
        </label>
      </div>

      <AnimatePresence>
        {showForm && (
          <motion.div
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: 'auto' }}
            exit={{ opacity: 0, height: 0 }}
            className="overflow-hidden"
          >
            <QuestForm onCreated={() => { setShowForm(false); load(); }} />
          </motion.div>
        )}
      </AnimatePresence>

      {loading ? (
        <p className="text-text-muted">Carregando…</p>
      ) : (
        <>
          <Section title="Em andamento" quests={grouped.active} onReload={load} meId={me?.id} />
          <Section title="Finalizadas" quests={grouped.done} onReload={load} meId={me?.id} />
        </>
      )}
    </div>
  );
}

function Section({ title, quests, onReload, meId }: { title: string; quests: Quest[]; onReload: () => void; meId?: string }) {
  if (quests.length === 0) return null;
  return (
    <div>
      <h2 className="text-sm uppercase tracking-wider text-text-dim mb-2">{title} ({quests.length})</h2>
      <div className="grid md:grid-cols-2 xl:grid-cols-3 gap-3">
        <AnimatePresence>
          {quests.map((q) => <QuestCard key={q.id} quest={q} onReload={onReload} meId={meId} />)}
        </AnimatePresence>
      </div>
    </div>
  );
}

function QuestCard({ quest, onReload, meId }: { quest: Quest; onReload: () => void; meId?: string }) {
  const [busy, setBusy] = useState(false);
  const mine = quest.assigneeId === meId;
  const terminal = quest.status === 'COMPLETED' || quest.status === 'CANCELLED' || quest.status === 'FAILED';

  async function action(fn: () => Promise<any>, label: string) {
    setBusy(true);
    try {
      await fn();
      toast.success(label);
      onReload();
    } catch (e) {
      toast.error(extractApiError(e));
    } finally {
      setBusy(false);
    }
  }

  return (
    <motion.div
      layout
      initial={{ opacity: 0, y: 10 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, scale: 0.95 }}
      className={`card ${terminal ? 'opacity-70' : ''}`}
    >
      <div className="flex items-start justify-between gap-2 mb-2">
        <h3 className="font-semibold leading-tight">{quest.title}</h3>
        <DifficultyTag value={quest.difficulty} />
      </div>

      {quest.description && (
        <p className="text-sm text-text-muted line-clamp-2 mb-3">{quest.description}</p>
      )}

      <div className="flex flex-wrap gap-2 text-xs text-text-muted mb-3">
        <span className={`font-medium ${STATUS[quest.status]?.color ?? ''}`}>{STATUS[quest.status]?.label}</span>
        {quest.assigneeUsername && <span>· @{quest.assigneeUsername}</span>}
        {quest.dueDate && <span>· Prazo: {fmtRelTime(quest.dueDate)}</span>}
      </div>

      <div className="flex items-center justify-between text-xs">
        <div className="flex gap-3">
          <span className="text-xp font-semibold">+{quest.xpReward} XP</span>
          <span className="text-gold font-semibold">+{quest.coinReward} 🪙</span>
        </div>
        <div className="flex gap-1">
          {!terminal && (
            <>
              {quest.status === 'OPEN' && mine && (
                <button disabled={busy} onClick={() => action(() => questsApi.start(quest.id), 'Quest iniciada!')}
                  className="btn-ghost p-1.5" title="Iniciar">
                  <Play size={16} />
                </button>
              )}
              {mine && quest.status !== 'COMPLETED' && (
                <button disabled={busy} onClick={() => action(() => questsApi.complete(quest.id), 'Quest completa! 🎉')}
                  className="btn-ghost p-1.5 text-emerald-400" title="Completar">
                  <CheckCircle2 size={16} />
                </button>
              )}
              <button disabled={busy} onClick={() => action(() => questsApi.cancel(quest.id), 'Quest cancelada')}
                className="btn-ghost p-1.5 text-amber-400" title="Cancelar">
                <XCircle size={16} />
              </button>
              <button disabled={busy} onClick={() => action(() => questsApi.remove(quest.id), 'Quest excluída')}
                className="btn-ghost p-1.5 text-danger" title="Excluir">
                <Trash2 size={16} />
              </button>
            </>
          )}
        </div>
      </div>
    </motion.div>
  );
}

function QuestForm({ onCreated }: { onCreated: () => void }) {
  const me = useAuthStore((s) => s.user);
  const [form, setForm] = useState({
    title: '', description: '', difficulty: 'EASY' as Difficulty,
    assignToMe: true, dueDate: '', tags: '',
  });
  const [saving, setSaving] = useState(false);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setSaving(true);
    try {
      await questsApi.create({
        title: form.title,
        description: form.description || undefined,
        difficulty: form.difficulty,
        assigneeId: form.assignToMe ? me?.id : undefined,
        dueDate: form.dueDate ? new Date(form.dueDate).toISOString() : undefined,
        tags: form.tags ? form.tags.split(',').map((t) => t.trim()).filter(Boolean) : undefined,
      });
      toast.success('Quest criada! ⚔️');
      onCreated();
    } catch (e) {
      toast.error(extractApiError(e));
    } finally {
      setSaving(false);
    }
  }

  return (
    <form onSubmit={onSubmit} className="card space-y-3">
      <h2 className="font-semibold">Nova quest</h2>
      <div className="grid md:grid-cols-2 gap-3">
        <div className="md:col-span-2">
          <label className="label">Título</label>
          <input className="input" required value={form.title}
            onChange={(e) => setForm({ ...form, title: e.target.value })} />
        </div>
        <div className="md:col-span-2">
          <label className="label">Descrição</label>
          <textarea className="input" rows={3} value={form.description}
            onChange={(e) => setForm({ ...form, description: e.target.value })} />
        </div>
        <div>
          <label className="label">Dificuldade</label>
          <select className="input" value={form.difficulty}
            onChange={(e) => setForm({ ...form, difficulty: e.target.value as Difficulty })}>
            {DIFFICULTIES.map((d) => <option key={d}>{d}</option>)}
          </select>
        </div>
        <div>
          <label className="label">Prazo (opcional)</label>
          <input type="datetime-local" className="input" value={form.dueDate}
            onChange={(e) => setForm({ ...form, dueDate: e.target.value })} />
        </div>
        <div>
          <label className="label">Tags (vírgula)</label>
          <input className="input" value={form.tags}
            onChange={(e) => setForm({ ...form, tags: e.target.value })} placeholder="bug, frontend" />
        </div>
        <label className="flex items-center gap-2 text-sm mt-6">
          <input type="checkbox" checked={form.assignToMe}
            onChange={(e) => setForm({ ...form, assignToMe: e.target.checked })} />
          Atribuir a mim
        </label>
      </div>
      <div className="flex justify-end gap-2">
        <button type="submit" className="btn-primary" disabled={saving}>
          {saving ? 'Criando…' : 'Criar quest'}
        </button>
      </div>
    </form>
  );
}