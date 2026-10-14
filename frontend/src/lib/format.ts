export const PLAYER_CLASSES: Record<string, { label: string; color: string; emoji: string }> = {
  DEV:       { label: 'Dev',       color: 'text-sky-400',     emoji: '⚔️' },
  DESIGNER:  { label: 'Designer',  color: 'text-pink-400',    emoji: '🎨' },
  PM:        { label: 'PM',        color: 'text-amber-400',   emoji: '🧭' },
  QA:        { label: 'QA',        color: 'text-emerald-400', emoji: '🛡️' },
  DEVOPS:    { label: 'DevOps',    color: 'text-violet-400',  emoji: '⚙️' },
  MARKETING: { label: 'Marketing', color: 'text-orange-400',  emoji: '📣' },
  OTHER:     { label: 'Outro',     color: 'text-slate-400',   emoji: '✨' },
};

export const DIFFICULTY = {
  TRIVIAL:   { label: 'Trivial',   color: 'text-slate-300 bg-slate-500/15 border-slate-500/30' },
  EASY:      { label: 'Fácil',     color: 'text-emerald-300 bg-emerald-500/15 border-emerald-500/30' },
  MEDIUM:    { label: 'Médio',     color: 'text-sky-300 bg-sky-500/15 border-sky-500/30' },
  HARD:      { label: 'Difícil',   color: 'text-amber-300 bg-amber-500/15 border-amber-500/30' },
  EPIC:      { label: 'Épico',     color: 'text-fuchsia-300 bg-fuchsia-500/15 border-fuchsia-500/30' },
  LEGENDARY: { label: 'Lendário',  color: 'text-gold bg-gold/15 border-gold/40' },
} as const;

export const STATUS = {
  OPEN:        { label: 'Aberta',       color: 'text-sky-300' },
  IN_PROGRESS: { label: 'Em progresso', color: 'text-amber-300' },
  IN_REVIEW:   { label: 'Em review',    color: 'text-violet-300' },
  COMPLETED:   { label: 'Concluída',    color: 'text-emerald-300' },
  FAILED:      { label: 'Falhou',       color: 'text-danger' },
  CANCELLED:   { label: 'Cancelada',    color: 'text-text-dim' },
} as const;

export const RARITY = {
  COMMON:    { label: 'Comum',    color: 'border-slate-500/40 text-slate-300' },
  RARE:      { label: 'Raro',     color: 'border-sky-500/40 text-sky-300' },
  EPIC:      { label: 'Épico',    color: 'border-fuchsia-500/40 text-fuchsia-300' },
  LEGENDARY: { label: 'Lendário', color: 'border-gold/50 text-gold' },
} as const;

export function fmtXp(n: number): string {
  if (n >= 1_000_000) return (n / 1_000_000).toFixed(1) + 'M';
  if (n >= 1_000)     return (n / 1_000).toFixed(1) + 'k';
  return String(n);
}

export function fmtRelTime(iso: string | undefined): string {
  if (!iso) return '—';
  const d = new Date(iso);
  const diff = Date.now() - d.getTime();
  const min = Math.floor(diff / 60000);
  if (min < 1) return 'agora';
  if (min < 60) return `${min}min`;
  const h = Math.floor(min / 60);
  if (h < 24) return `${h}h`;
  const days = Math.floor(h / 24);
  if (days < 30) return `${days}d`;
  return d.toLocaleDateString('pt-BR');
}

export function fmtDate(iso?: string): string {
  if (!iso) return '—';
  return new Date(iso).toLocaleDateString('pt-BR', { day: '2-digit', month: 'short' });
}