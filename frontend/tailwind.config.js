/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        // Paleta "RPG dark"
        bg:      { DEFAULT: '#0b0d12', soft: '#12151c', card: '#161a23' },
        border:  { DEFAULT: '#242a37', soft: '#1c2230' },
        text:    { DEFAULT: '#e6e9ef', muted: '#8b93a7', dim: '#5a6273' },
        brand:   { DEFAULT: '#7c5cff', soft: '#9d85ff', deep: '#5b3fd9' },
        gold:    { DEFAULT: '#f4c95d', soft: '#ffd97a' },
        xp:      { DEFAULT: '#22d3a0', soft: '#4ee5b8' },
        danger:  { DEFAULT: '#ef4d5a' },
        warn:    { DEFAULT: '#f59e0b' },
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
        display: ['"Cinzel"', 'serif'],
      },
      boxShadow: {
        glow:  '0 0 24px rgba(124,92,255,0.35)',
        gold:  '0 0 24px rgba(244,201,93,0.35)',
      },
      backgroundImage: {
        'grid': 'radial-gradient(circle at 1px 1px, rgba(255,255,255,0.05) 1px, transparent 0)',
      },
    },
  },
  plugins: [],
};