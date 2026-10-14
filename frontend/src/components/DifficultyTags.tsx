import { DIFFICULTY } from '../lib/format';

export default function DifficultyTag({ value }: { value: keyof typeof DIFFICULTY }) {
  const d = DIFFICULTY[value] ?? DIFFICULTY.EASY;
  return <span className={`badge-pill ${d.color}`}>{d.label}</span>;
}