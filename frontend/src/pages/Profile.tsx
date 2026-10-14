import { FormEvent, useState } from 'react';
import { motion } from 'framer-motion';
import toast from 'react-hot-toast';
import { usersApi } from '../api/users';
import { useAuthStore } from '../store/authStore';
import { extractApiError } from '../api/client';
import { PLAYER_CLASSES } from '../lib/format';

const CLASSES = Object.keys(PLAYER_CLASSES);

export default function Profile() {
  const { user, setUser } = useAuthStore();
  const [form, setForm] = useState({
    displayName: user?.displayName ?? '',
    avatarUrl: user?.avatarUrl ?? '',
    playerClass: user?.playerClass ?? 'DEV',
  });
  const [saving, setSaving] = useState(false);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setSaving(true);
    try {
      const updated = await usersApi.updateMe(form);
      setUser(updated);
      toast.success('Perfil atualizado!');
    } catch (e) {
      toast.error(extractApiError(e));
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="max-w-lg mx-auto">
      <h1 className="text-2xl font-bold mb-4">Perfil</h1>
      <motion.form onSubmit={onSubmit} initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }} className="card space-y-4">
        <div>
          <label className="label">Nome de exibição</label>
          <input className="input" value={form.displayName}
            onChange={(e) => setForm({ ...form, displayName: e.target.value })} />
        </div>
        <div>
          <label className="label">URL do avatar</label>
          <input className="input" value={form.avatarUrl}
            onChange={(e) => setForm({ ...form, avatarUrl: e.target.value })}
            placeholder="https://..." />
        </div>
        <div>
          <label className="label">Classe</label>
          <select className="input" value={form.playerClass}
            onChange={(e) => setForm({ ...form, playerClass: e.target.value as any })}>
            {CLASSES.map((c) => <option key={c} value={c}>{PLAYER_CLASSES[c].emoji} {PLAYER_CLASSES[c].label}</option>)}
          </select>
        </div>
        <button className="btn-primary w-full" disabled={saving}>{saving ? 'Salvando…' : 'Salvar'}</button>
      </motion.form>
    </div>
  );
}