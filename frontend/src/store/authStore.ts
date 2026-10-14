import { create } from 'zustand';
import { authApi, AuthResponse, UserResponse } from '../api/auth';
import { connectWs, disconnectWs } from '../ws/stompClient';

interface AuthState {
  user: UserResponse | null;
  accessToken: string | null;
  refreshToken: string | null;
  loading: boolean;
  bootstrapped: boolean;

  bootstrap: () => Promise<void>;
  login: (username: string, password: string) => Promise<void>;
  register: (payload: Parameters<typeof authApi.register>[0]) => Promise<void>;
  logout: () => void;
  refresh: () => Promise<string | null>;
  setUser: (u: UserResponse) => void;
  connectWebSocket: (handlers: { onNotification?: (n: any) => void; onLeaderboard?: (l: any) => void }) => void;
}

const LS_KEY = 'questboard.auth';

function persist(state: { accessToken: string | null; refreshToken: string | null; user: UserResponse | null }) {
  if (state.accessToken && state.refreshToken) {
    localStorage.setItem(LS_KEY, JSON.stringify(state));
  } else {
    localStorage.removeItem(LS_KEY);
  }
}

function load(): { accessToken: string | null; refreshToken: string | null; user: UserResponse | null } {
  try {
    const raw = localStorage.getItem(LS_KEY);
    if (!raw) return { accessToken: null, refreshToken: null, user: null };
    return JSON.parse(raw);
  } catch {
    return { accessToken: null, refreshToken: null, user: null };
  }
}

export const useAuthStore = create<AuthState>((set, get) => ({
  ...load(),
  loading: false,
  bootstrapped: false,

  bootstrap: async () => {
    const { accessToken } = get();
    if (!accessToken) { set({ bootstrapped: true }); return; }
    try {
      const me = await authApi.me();
      set({ user: me, bootstrapped: true });
    } catch {
      set({ user: null, accessToken: null, refreshToken: null, bootstrapped: true });
      persist({ accessToken: null, refreshToken: null, user: null });
    }
  },

  login: async (username, password) => {
    set({ loading: true });
    try {
      const res: AuthResponse = await authApi.login(username, password);
      set({ accessToken: res.accessToken, refreshToken: res.refreshToken, user: res.user });
      persist({ accessToken: res.accessToken, refreshToken: res.refreshToken, user: res.user });
    } finally {
      set({ loading: false });
    }
  },

  register: async (payload) => {
    set({ loading: true });
    try {
      const res = await authApi.register(payload);
      set({ accessToken: res.accessToken, refreshToken: res.refreshToken, user: res.user });
      persist({ accessToken: res.accessToken, refreshToken: res.refreshToken, user: res.user });
    } finally {
      set({ loading: false });
    }
  },

  logout: () => {
    disconnectWs();
    set({ user: null, accessToken: null, refreshToken: null });
    persist({ accessToken: null, refreshToken: null, user: null });
  },

  refresh: async () => {
    const rt = get().refreshToken;
    if (!rt) return null;
    try {
      const res = await authApi.refresh(rt);
      set({ accessToken: res.accessToken, refreshToken: res.refreshToken, user: res.user });
      persist({ accessToken: res.accessToken, refreshToken: res.refreshToken, user: res.user });
      return res.accessToken;
    } catch {
      get().logout();
      return null;
    }
  },

  setUser: (u) => {
    set({ user: u });
    persist({ accessToken: get().accessToken, refreshToken: get().refreshToken, user: u });
  },

  connectWebSocket: ({ onNotification, onLeaderboard }) => {
    const token = get().accessToken;
    if (!token) return;
    connectWs(token, { onNotification, onLeaderboard });
  },
}));