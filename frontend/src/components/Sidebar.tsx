import { NavLink, useNavigate } from "react-router-dom";
import {
  LayoutDashboard,
  Sword,
  Shield,
  Skull,
  Trophy,
  Store,
  LogOut,
  UserCircle,
  Plug,
} from "lucide-react";
import clsx from "clsx";
import { useAuthStore } from "../store/authStore";

const items = [
  { to: "/dashboard", label: "Dashboard", icon: LayoutDashboard },
  { to: "/quests", label: "Quests", icon: Sword },
  { to: "/guilds", label: "Guildas", icon: Shield },
  { to: "/bosses", label: "Boss Fights", icon: Skull },
  { to: "/leaderboard", label: "Ranking", icon: Trophy },
  { to: "/marketplace", label: "Loot", icon: Store },
  { to: '/integrations', label: 'Integrações', icon: Plug },
  { to: "/profile", label: "Perfil", icon: UserCircle },
];

export default function Sidebar() {
  const { user, logout } = useAuthStore();
  const nav = useNavigate();

  return (
    <aside className="w-64 shrink-0 bg-bg-soft/60 border-r border-border flex flex-col">
      <div className="p-5 border-b border-border">
        <h1 className="font-display text-2xl font-bold text-brand-soft tracking-wide">
          QuestBoard
        </h1>
        <p className="text-xs text-text-dim mt-1">Gamificação para equipes</p>
      </div>

      {user && (
        <div className="p-4 border-b border-border">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-full bg-gradient-to-br from-brand to-xp flex items-center justify-center font-bold">
              {user.displayName?.[0]?.toUpperCase() ??
                user.username[0].toUpperCase()}
            </div>
            <div className="min-w-0">
              <p className="font-semibold truncate">
                {user.displayName ?? user.username}
              </p>
              <p className="text-xs text-text-muted">
                Nv {user.level} · {user.totalXp} XP · {user.coins} 🪙
              </p>
            </div>
          </div>
        </div>
      )}

      <nav className="flex-1 p-3 space-y-1">
        {items.map(({ to, label, icon: Icon }) => (
          <NavLink
            key={to}
            to={to}
            className={({ isActive }) =>
              clsx(
                "flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition",
                isActive
                  ? "bg-brand/15 text-brand-soft border border-brand/30"
                  : "text-text-muted hover:bg-bg-card hover:text-text",
              )
            }
          >
            <Icon size={18} />
            {label}
          </NavLink>
        ))}
      </nav>

      <div className="p-3 border-t border-border">
        <button
          onClick={() => {
            logout();
            nav("/login");
          }}
          className="btn-ghost w-full justify-start"
        >
          <LogOut size={18} /> Sair
        </button>
      </div>
    </aside>
  );
}
