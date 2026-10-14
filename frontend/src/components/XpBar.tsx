import { motion } from 'framer-motion';
import { fmtXp } from '../lib/format';

interface Props {
  xp: number;
  xpToNext: number;
  showLabels?: boolean;
}

export default function XpBar({ xp, xpToNext, showLabels = true }: Props) {
  const pct = xpToNext === 0 ? 100 : Math.min(100, (xp / xpToNext) * 100);
  return (
    <div className="w-full">
      {showLabels && (
        <div className="flex justify-between text-xs text-text-muted mb-1">
          <span>{fmtXp(xp)} XP</span>
          <span>{fmtXp(xpToNext)} XP</span>
        </div>
      )}
      <div className="h-2 bg-bg-soft rounded-full overflow-hidden border border-border">
        <motion.div
          className="h-full bg-gradient-to-r from-xp to-xp-soft"
          initial={{ width: 0 }}
          animate={{ width: `${pct}%` }}
          transition={{ duration: 0.8, ease: 'easeOut' }}
        />
      </div>
    </div>
  );
}