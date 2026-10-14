import { useEffect, useState } from 'react';
import { AnimatePresence, motion } from 'framer-motion';
import confetti from 'canvas-confetti';
import { Sparkles } from 'lucide-react';

interface Props {
  level: number | null;   // null = fechado
  onClose: () => void;
}

export default function LevelUpModal({ level, onClose }: Props) {
  const [open, setOpen] = useState(false);

  useEffect(() => {
    if (level != null) {
      setOpen(true);
      // Confete
      const duration = 2500;
      const end = Date.now() + duration;
      const colors = ['#7c5cff', '#22d3a0', '#f4c95d', '#ef4d5a'];
      (function frame() {
        confetti({ particleCount: 4, angle: 60, spread: 55, origin: { x: 0 }, colors });
        confetti({ particleCount: 4, angle: 120, spread: 55, origin: { x: 1 }, colors });
        if (Date.now() < end) requestAnimationFrame(frame);
      })();
    }
  }, [level]);

  return (
    <AnimatePresence>
      {open && level != null && (
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm"
          onClick={() => { setOpen(false); onClose(); }}
        >
          <motion.div
            initial={{ scale: 0.5, rotate: -8, y: 40 }}
            animate={{ scale: 1, rotate: 0, y: 0 }}
            exit={{ scale: 0.8, opacity: 0 }}
            transition={{ type: 'spring', stiffness: 220, damping: 18 }}
            className="relative card p-10 text-center max-w-md mx-4 border-2 border-gold/40"
            onClick={(e) => e.stopPropagation()}
          >
            <motion.div
              animate={{ rotate: [0, 10, -10, 0] }}
              transition={{ duration: 1.4, repeat: Infinity, repeatDelay: 1 }}
              className="text-6xl mb-4"
            >
              <Sparkles className="inline text-gold" size={64} />
            </motion.div>
            <p className="text-sm uppercase tracking-[0.3em] text-gold/80 mb-2">Level Up</p>
            <h2 className="font-display text-6xl font-bold text-gold mb-3">{level}</h2>
            <p className="text-text-muted">Você ficou mais forte.</p>

            <button className="btn-primary mt-6 w-full" onClick={() => { setOpen(false); onClose(); }}>
              Continuar a jornada
            </button>
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  );
}