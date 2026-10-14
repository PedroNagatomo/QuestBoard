import { useEffect, useState } from "react";
import toast from "react-hot-toast";
import confetti from "canvas-confetti";
import Sidebar from "./Sidebar";
import LevelUpModal from "./LevelUpModal";
import { useAuthStore } from "../store/authStore";
import { Outlet, useLocation } from "react-router-dom";

export default function Layout() {
  const { connectWebSocket, accessToken } = useAuthStore();
  const [levelUpTo, setLevelUpTo] = useState<number | null>(null);
  const location = useLocation();
  const fullBleed = location.pathname === "/world";

  useEffect(() => {
    if (!accessToken) return;
    connectWebSocket({
      onNotification: (msg) => {
        const type = msg.type;
        const p = msg.payload ?? {};
        if (type === "LEVEL_UP") {
          setLevelUpTo(p.level);
        } else if (type === "XP_GAINED") {
          toast.success(`+${p.xpDelta} XP · +${p.coinDelta} 🪙`, {
            duration: 3000,
          });
        } else if (type === "BADGE_UNLOCKED") {
          toast(`🏅 Nova badge: ${p.name}`, { duration: 5000 });
          if (p.rarity === "LEGENDARY") {
            confetti({
              particleCount: 120,
              spread: 90,
              origin: { y: 0.6 },
              colors: ["#f4c95d", "#ffd97a"],
            });
          }
        } else if (type === "ACHIEVEMENT_UNLOCKED") {
          toast.success(`🏆 ${p.name} desbloqueada!`, { duration: 5000 });
        }
      },
      onLeaderboard: () => {},
    });
  }, [accessToken, connectWebSocket]);

  return (
    <div className="min-h-screen flex">
      <Sidebar />
      <main className="flex-1 overflow-x-hidden">
        <div className={fullBleed ? "p-4" : "max-w-7xl mx-auto p-6"}>
          <Outlet />
        </div>
      </main>
      <LevelUpModal level={levelUpTo} onClose={() => setLevelUpTo(null)} />
    </div>
  );
}
