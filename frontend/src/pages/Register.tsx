import { FormEvent, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { useAuthStore } from '../store/authStore';
import { extractApiError } from '../api/client';

const classes = ['DEV','DESIGNER','PM','QA','DEVOPS','MARKETING','OTHER'];

export default function Register() {
  const nav = useNavigate();
  const { register, loading } = useAuthStore();
  const [form, setForm] = useState({
    username: '', email: '', password: '', displayName: '', playerClass: 'DEV',
  });

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    try {
      await register(form);
      toast.success('Conta criada! Bem-vindo à guilda.');
      nav('/dashboard');
    } catch (err) {
      toast.error(extractApiError(err));
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center p-4">
      <div className="w-full max-w-md card p-8">
        <h1 className="font-display text-3xl font-bold text-brand-soft text-center">Nova Lenda</h1>
        <p className="text-center text-text-muted mt-2 mb-6">Crie seu personagem</p>

        <form onSubmit={onSubmit} className="space-y-4">
          <div>
            <label className="label">Usuário</label>
            <input className="input" value={form.username}
              onChange={(e) => setForm({ ...form, username: e.target.value })} required />
          </div>
          <div>
            <label className="label">Email</label>
            <input className="input" type="email" value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })} required />
          </div>
          <div>
            <label className="label">Senha (mín. 8)</label>
            <input className="input" type="password" value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })} minLength={8} required />
          </div>
          <div>
            <label className="label">Nome de exibição</label>
            <input className="input" value={form.displayName}
              onChange={(e) => setForm({ ...form, displayName: e.target.value })} />
          </div>
          <div>
            <label className="label">Classe</label>
            <select className="input" value={form.playerClass}
              onChange={(e) => setForm({ ...form, playerClass: e.target.value })}>
              {classes.map((c) => <option key={c}>{c}</option>)}
            </select>
          </div>
          <button className="btn-primary w-full" disabled={loading}>
            {loading ? 'Criando…' : 'Criar conta'}
          </button>
        </form>

        <p className="text-center text-sm text-text-muted mt-6">
          Já tem conta? <Link to="/login" className="text-brand-soft hover:underline">Entrar</Link>
        </p>
      </div>
    </div>
  );
}