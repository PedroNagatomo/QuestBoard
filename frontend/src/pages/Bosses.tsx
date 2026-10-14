import { FormEvent, useEffect, useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import toast from 'react-hot-toast';
import { Swords, Plus, Skull } from 'lucide-react';
import { bossesApi, Boss } from '../api/bosses';
import { extractApiError } from '../api/client';

export default function Bosses() {
  const [bosses, setBosses] = useState<Boss[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);

  async function load() {
    setLoading(true);
    try { setBosses(await bossesApi.listActive()); }
    catch (e) { toast.error(extractApiError(e)); }
    finally { setLoading(false); }
  }

  useEffect(() => { load(); }, []);

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">Boss Fights</h1>
          <p className="text-sm text-text-muted">Projetos grandes viram caçadas épicas.</p>
        </div>
        <button className="btn-primary" onClick={() => setShowForm((s) => !s)}>
          <Plus size={18} /> Novo boss
        </button>
      </div>

      <AnimatePresence>
        {showForm && (
          <motion.div initial={{ opacity: 0, height: 0 }} animate={{ opacity: 1, height: 'auto' }} exit={{ opacity: 0, height: 0 }} className="overflow-hidden">
            <BossForm onCreated={() => { setShowForm(false); load(); }} />
          </motion.div>
        )}
      </AnimatePresence>

      {loading ? <p className="text-text-muted">Carregando…</p> :
        bosses.length === 0 ? (
          <div className="card text-center py-12">
            <Skull className="mx-auto text-text-dim mb-3" size={40} />
            <p className="text-text-muted">Nenhum boss ativo. Crie um para começar a caçada.</p>
          </div>
        ) : (
          <div className="grid md:grid-cols-2 gap-4">
            {bosses.map((b) => <BossCard key={b.id} boss={b} onUpdate={load} />)}
          </div>
        )}
    </div>
  );
}

function BossCard({ boss, onUpdate }: { boss: Boss; onUpdate: () => void }) {
  const [busy, setBusy] = useState(false);
  const [shake, setShake] = useState(false);

  async function attack() {
    setBusy(true);
    setShake(true);
    setTimeout(() => setShake(false), 400);
    try {
      const updated = await bossesApi.damage(boss.id, 50);
      if (updated.status === 'DEFEATED') {
        toast.success(`💀 ${updated.name} foi derrotado!`);
      }
      onUpdate();
    } catch (e) {
      toast.error(extractApiError(e));
    } finally {
      setBusy(false);
    }
  }

  const hpColor = boss.hpPercent > 60 ? 'bg-emerald-500' : boss.hpPercent > 30 ? 'bg-amber-500' : 'bg-danger';

  return (
    <motion.div
      animate={shake ? { x: [0, -6, 6, -4, 4, 0] } : {}}
      transition={{ duration: 0.35 }}
      className="card"
    >
      <div className="flex items-center gap-4 mb-4">
        <div className="w-16 h-16 rounded-lg bg-gradient-to-br from-fuchsia-600 to-brand-deep flex items-center justify-center">
          <Skull size={28} />
        </div>
        <div className="flex-1">
          <h3 className="font-bold text-lg">{boss.name}</h3>
          <p className="text-xs text-text-muted">{boss.status}</p>
        </div>
      </div>

      {boss.description && <p className="text-sm text-text-muted mb-4">{boss.description}</p>}

      <div className="mb-3">
        <div className="flex justify-between text-xs mb-1">
          <span className="text-text-muted">HP</span>
          <span className="font-mono">{boss.currentHp} / {boss.totalHp}</span>
        </div>
        <div className="h-3 bg-bg-soft rounded-full overflow-hidden border border-border">
          <motion.div
            className={`h-full ${hpColor}`}
            initial={false}
            animate={{ width: `${boss.hpPercent}%` }}
            transition={{ duration: 0.6 }}
          />
        </div>
      </div>

      {boss.status === 'ACTIVE' && (
        <button disabled={busy} onClick={attack} className="btn-primary w-full">
          <Swords size={18} /> Atacar (50 dmg)
        </button>
      )}
    </motion.div>
  );
}

function BossForm({ onCreated }: { onCreated: () => void }) {
  const [form, setForm] = useState({ name: '', description: '', totalHp: 1000 });
  const [saving, setSaving] = useState(false);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setSaving(true);
    try {
      await bossesApi.create(form);
      toast.success('Boss invocado! 🐉');
      onCreated();
    } catch (e) {
      toast.error(extractApiError(e));
    } finally {
      setSaving(false);
    }
  }

  return (
    <form onSubmit={onSubmit} className="card space-y-3">
      <h2 className="font-semibold">Invocar novo boss</h2>
      <div className="grid md:grid-cols-2 gap-3">
        <div className="md:col-span-2">
          <label className="label">Nome</label>
          <input className="input" required value={form.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })} />
        </div>
        <div className="md:col-span-2">
          <label className="label">Descrição</label>
          <textarea className="input" rows={2} value={form.description}
            onChange={(e) => setForm({ ...form, description: e.target.value })} />
        </div>
        <div>
          <label className="label">HP total</label>
          <input type="number" className="input" min={100} value={form.totalHp}
            onChange={(e) => setForm({ ...form, totalHp: Number(e.target.value) })} />
        </div>
      </div>
      <button className="btn-primary" disabled={saving}>{saving ? 'Invocando…' : 'Invocar'}</button>
    </form>
  );
}