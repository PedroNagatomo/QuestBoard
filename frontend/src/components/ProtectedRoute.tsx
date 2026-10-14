import { Navigate, Outlet } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';

export default function ProtectedRoute() {
  const { accessToken, bootstrapped } = useAuthStore();
  if (!bootstrapped) return <div className="p-8 text-text-muted">Carregando…</div>;
  if (!accessToken) return <Navigate to="/login" replace />;
  return <Outlet />;
}