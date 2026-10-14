import { FormEvent, useState } from 'react';
import { motion } from 'framer-motion';
import toast from 'react-hot-toast';
import { Github, CheckCircle2, Send } from 'lucide-react';
import { integrationsApi } from '../api/integrations';
import { extractApiError } from '../api/client';

export default function Integrations() {
  const [form, setForm] = useState({
    source: 'GITHUB' as const,
    type: 'ISSUE_CREATED' as const,
    externalId: 'demo-' + Date.now(),
    title: 'Corrigir bug de autenticação',
    description: 'Ocorre quando o token expira',
    difficultyHint: 'critical',
    url: 'https://github.com/org/repo/issues/1',
  });
  const [sending, setSending] = useState(false);

  async function send(e: FormEvent) {
    e.preventDefault();
    setSending(true);
    try {
      await integrationsApi.test(form);
      toast.success('Evento enviado! Verifique as quests.');
    } catch (err) {
      toast.error(extractApiError(err));
    } finally {
      setSending(false);
    }
  }

  return (
    <div className="space-y-5 max-w-3xl">
      <div>
        <h1 className="text-2xl font-bold">Integrações</h1>
        <p className="text-sm text-text-muted">
          Jira, GitHub e Trello criam quests automaticamente. Slack notifica o time.
        </p>
      </div>

      <div className="grid md:grid-cols-3 gap-3">
        {[
          { icon: Github, name: 'GitHub', desc: 'Issues e PRs viram quests' },
          { icon: CheckCircle2, name: 'Jira', desc: 'Issues e transições' },
          { icon: Send, name: 'Slack', desc: 'Notificações do time' },
        ].map((c) => (
          <div key={c.name} className="card">
            <c.icon className="text-brand-soft mb-2" />
            <h3 className="font-semibold">{c.name}</h3>
            <p className="text-xs text-text-muted">{c.desc}</p>
          </div>
        ))}
      </div>

      <motion.form onSubmit={send} initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="card space-y-3">
        <h2 className="font-semibold">Simular evento externo</h2>
        <p className="text-xs text-text-muted">
          Útil para testar o pipeline sem configurar a ferramenta real.
        </p>
        <div className="grid md:grid-cols-2 gap-3">
          <div>
            <label className="label">Source</label>
            <select className="input" value={form.source}
              onChange={(e) => setForm({ ...form, source: e.target.value as any })}>
              <option>GITHUB</option><option>JIRA</option><option>TRELLO</option>
            </select>
          </div>
          <div>
            <label className="label">Tipo</label>
            <select className="input" value={form.type}
              onChange={(e) => setForm({ ...form, type: e.target.value as any })}>
              <option>ISSUE_CREATED</option><option>ISSUE_COMPLETED</option>
              <option>ISSUE_UPDATED</option><option>PR_MERGED</option>
            </select>
          </div>
          <div className="md:col-span-2">
            <label className="label">External ID</label>
            <input className="input" value={form.externalId}
              onChange={(e) => setForm({ ...form, externalId: e.target.value })} />
          </div>
          <div className="md:col-span-2">
            <label className="label">Título</label>
            <input className="input" value={form.title}
              onChange={(e) => setForm({ ...form, title: e.target.value })} />
          </div>
          <div className="md:col-span-2">
            <label className="label">Descrição</label>
            <textarea className="input" rows={2} value={form.description}
              onChange={(e) => setForm({ ...form, description: e.target.value })} />
          </div>
          <div>
            <label className="label">Dica de dificuldade</label>
            <input className="input" value={form.difficultyHint}
              onChange={(e) => setForm({ ...form, difficultyHint: e.target.value })}
              placeholder="critical, high, bug, medium..." />
          </div>
          <div>
            <label className="label">URL</label>
            <input className="input" value={form.url}
              onChange={(e) => setForm({ ...form, url: e.target.value })} />
          </div>
        </div>
        <button className="btn-primary" disabled={sending}>
          {sending ? 'Enviando…' : 'Enviar evento'}
        </button>
      </motion.form>
    </div>
  );
}