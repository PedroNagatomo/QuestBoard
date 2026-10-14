import { FormEvent, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { useAuthStore } from '../store/authStore';
import { extractApiError } from '../api/client';

export default function Login() {
  const nav = useNavigate();
  const { login, loading } = useAuthStore();
  const [username, setUsername] = useState('admin');
  const [password, setPassword] = useState('admin123');

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    try {
      await login(username, password);
      toast.success('Bem-vindo de volta!');
      nav('/dashboard');
    } catch (err) {
      toast.error(extractApiError(err));
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center p-4">
      <div className="w-full max-w-md card p-8">
        <h1 className="font-display text-3xl font-bold text-brand-soft text-center">QuestBoard</h1>
        <p className="text-center text-text-muted mt-2 mb-6">Entre para continuar sua jornada</p>

        <form onSubmit={onSubmit} className="space-y-4">
          <div>
            <label className="label">Usuário</label>
            <input className="input" value={username} onChange={(e) => setUsername(e.target.value)} required />
          </div>
          <div>
            <label className="label">Senha</label>
            <input className="input" type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
          </div>
          <button className="btn-primary w-full" disabled={loading}>
            {loading ? 'Entrando…' : 'Entrar'}
          </button>
        </form>

        <p className="text-center text-sm text-text-muted mt-6">
          Novo por aqui?{' '}
          <Link to="/register" className="text-brand-soft hover:underline">Criar conta</Link>
        </p>
      </div>
    </div>
  );
}