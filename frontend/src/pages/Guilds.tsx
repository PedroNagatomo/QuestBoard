import { FormEvent, useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import toast from 'react-hot-toast';
import { Shield, Users, Plus } from 'lucide-react';
import { guildsApi, Guild, GuildMember } from '../api/guilds';
import { extractApiError } from '../api/client';
import { useAuthStore } from '../store/authStore';
import { PLAYER_CLASSES, fmtXp } from '../lib/format';

export default function Guilds() {
  const me = useAuthStore((s) => s.user);
  const setUser = useAuthStore((s) => s.setUser);
  const [guilds, setGuilds] = useState<Guild[]>([]);
  const [loading, setLoading] = useState(true);
  const [selected, setSelected] = useState<Guild | null>(null);
  const [members, setMembers] = useState<GuildMember[]>([]);
  const [showForm, setShowForm] = useState(false);

  async function load() {
    setLoading(true);
    try {
      const page: any = await guildsApi.list(0, 50);
      setGuilds(page.content ?? []);
    } catch (e) {
      toast.error(extractApiError(e));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { load(); }, []);

  async function select(g: Guild) {
    setSelected(g);
    try {
      setMembers(await guildsApi.members(g.id));
    } catch (e) {
      toast.error(extractApiError(e));
    }
  }

  async function join(g: Guild) {
    try {
      await guildsApi.join(g.id);
      toast.success(`Você entrou na guilda ${g.name}!`);
      const fresh = await guildsApi.list().then((r: any) => r.content);
      const updatedGuild = fresh.find((x: Guild) => x.id === g.id);
      if (updatedGuild && me) setUser({ ...me, guildId: g.id, guildName: g.name });
      load();
    } catch (e) {
      toast.error(extractApiError(e));
    }
  }

  async function leave() {
    try {
      await guildsApi.leave();
      toast.success('Você saiu da guilda.');
      if (me) setUser({ ...me, guildId: undefined, guildName: undefined });
      load();
    } catch (e) {
      toast.error(extractApiError(e));
    }
  }

  return (
    <div className="space-y-5">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">Guildas</h1>
          <p className="text-sm text-text-muted">Times competindo juntos.</p>
        </div>
        <div className="flex gap-2">
          {me?.guildId && <button className="btn-ghost" onClick={leave}>Sair da minha guilda</button>}
          <button className="btn-primary" onClick={() => setShowForm((s) => !s)}>
            <Plus size={18} /> Criar guilda
          </button>
        </div>
      </div>

      {showForm && (
        <GuildForm onCreated={() => { setShowForm(false); load(); }} />
      )}

      <div className="grid lg:grid-cols-3 gap-4">
        <div className="lg:col-span-2 space-y-3">
          {loading ? <p className="text-text-muted">Carregando…</p> :
            guilds.length === 0 ? <p className="text-text-muted">Sem guildas ainda.</p> :
            guilds.map((g) => (
              <motion.button
                key={g.id}
                onClick={() => select(g)}
                initial={{ opacity: 0, y: 8 }}
                animate={{ opacity: 1, y: 0 }}
                className={`card text-left w-full transition ${
                  selected?.id === g.id ? 'ring-2 ring-brand/50' : 'hover:border-brand/30'
                }`}
              >
                <div className="flex items-center gap-3">
                  <div className="w-12 h-12 rounded-lg bg-gradient-to-br from-brand to-xp flex items-center justify-center">
                    <Shield size={22} />
                  </div>
                  <div className="flex-1">
                    <div className="flex items-center gap-2">
                      <h3 className="font-semibold">{g.name}</h3>
                      {me?.guildId === g.id && <span className="badge-pill text-brand-soft border-brand/40">Minha</span>}
                    </div>
                    <p className="text-xs text-text-muted">Nível {g.level} · {fmtXp(g.totalXp)} XP</p>
                    {g.description && <p className="text-sm text-text-muted mt-1 line-clamp-1">{g.description}</p>}
                  </div>
                  {!me?.guildId && (
                    <button className="btn-primary text-xs px-3 py-1"
                      onClick={(e) => { e.stopPropagation(); join(g); }}>
                      Entrar
                    </button>
                  )}
                </div>
              </motion.button>
            ))}
        </div>

        <div className="card h-fit">
          <div className="flex items-center gap-2 mb-4">
            <Users size={18} />
            <h2 className="font-semibold">{selected ? `Membros — ${selected.name}` : 'Selecione uma guilda'}</h2>
          </div>
          {!selected ? (
            <p className="text-sm text-text-muted">Escolha uma guilda à esquerda para ver os membros.</p>
          ) : members.length === 0 ? (
            <p className="text-sm text-text-muted">Sem membros ainda.</p>
          ) : (
            <ul className="space-y-2">
              {members.map((m, i) => {
                const cls = PLAYER_CLASSES[m.playerClass] ?? PLAYER_CLASSES.OTHER;
                return (
                  <li key={m.id} className="flex items-center gap-2 text-sm">
                    <span className="text-text-dim w-5 text-right">{i + 1}</span>
                    <span className="flex-1 truncate">{m.displayName ?? m.username}</span>
                    <span className={`text-xs ${cls.color}`}>{cls.emoji}</span>
                    <span className="text-xs text-xp font-medium">{fmtXp(m.totalXp)}</span>
                  </li>
                );
              })}
            </ul>
          )}
        </div>
      </div>
    </div>
  );
}

function GuildForm({ onCreated }: { onCreated: () => void }) {
  const [form, setForm] = useState({ name: '', description: '' });
  const [saving, setSaving] = useState(false);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setSaving(true);
    try {
      await guildsApi.create(form);
      toast.success('Guilda criada! 🏰');
      onCreated();
    } catch (e) {
      toast.error(extractApiError(e));
    } finally {
      setSaving(false);
    }
  }

  return (
    <form onSubmit={onSubmit} className="card space-y-3">
      <h2 className="font-semibold">Nova guilda</h2>
      <div>
        <label className="label">Nome</label>
        <input className="input" required value={form.name}
          onChange={(e) => setForm({ ...form, name: e.target.value })} />
      </div>
      <div>
        <label className="label">Descrição</label>
        <textarea className="input" rows={2} value={form.description}
          onChange={(e) => setForm({ ...form, description: e.target.value })} />
      </div>
      <button className="btn-primary" disabled={saving}>{saving ? 'Criando…' : 'Criar guilda'}</button>
    </form>
  );
}