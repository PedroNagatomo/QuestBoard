import { motion } from 'framer-motion';

interface Props {
  level: number;
  size?: 'sm' | 'md' | 'lg';
}

export default function LevelBadge({ level, size = 'md' }: Props) {
  const dim = size === 'sm' ? 'w-8 h-8 text-xs' : size === 'lg' ? 'w-16 h-16 text-xl' : 'w-12 h-12 text-sm';
  return (
    <motion.div
      initial={{ scale: 0.8, rotate: -8 }}
      animate={{ scale: 1, rotate: 0 }}
      className={`${dim} rounded-full bg-gradient-to-br from-brand to-brand-deep
                  ring-2 ring-brand-soft/50 shadow-glow
                  flex flex-col items-center justify-center font-bold leading-none`}
    >
      <span className="text-[9px] opacity-70 font-medium">LVL</span>
      <span className="text-lg">{level}</span>
    </motion.div>
  );
}