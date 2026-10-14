import { useEffect } from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import { useAuthStore } from "./store/authStore";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Dashboard from "./pages/Dashboard";
import Quests from "./pages/Quests";
import Guilds from "./pages/Guilds";
import Bosses from "./pages/Bosses";
import Leaderboard from "./pages/Leaderboard";
import Marketplace from "./pages/Marketplace";
import Layout from "./components/Layout";
import ProtectedRoute from "./components/ProtectedRoute";
import Profile from "./pages/Profile";
import Integrations from "./pages/Integrations";

export default function App() {
  const bootstrap = useAuthStore(
    (s: { bootstrap: () => Promise<void> }) => s.bootstrap,
  );
  useEffect(() => {
    bootstrap();
  }, [bootstrap]);

  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />

      <Route element={<ProtectedRoute />}>
        <Route element={<Layout />}>
          <Route index element={<Navigate to="/dashboard" replace />} />
          <Route path="/profile" element={<Profile />} />
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/quests" element={<Quests />} />
          <Route path="/guilds" element={<Guilds />} />
          <Route path="/bosses" element={<Bosses />} />
          <Route path="/leaderboard" element={<Leaderboard />} />
          <Route path="/marketplace" element={<Marketplace />} />
          <Route path="/integrations" element={<Integrations />} />
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}
