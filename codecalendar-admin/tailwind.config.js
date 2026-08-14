/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        canvas: {
          dark: '#07090E',
          card: 'rgba(18, 24, 38, 0.70)',
          border: 'rgba(255, 255, 255, 0.08)',
          hover: 'rgba(255, 255, 255, 0.04)',
        },
        brand: {
          orange: '#FF6B00',
          orangeGlow: 'rgba(255, 107, 0, 0.25)',
          indigo: '#818CF8',
          emerald: '#10B981',
          rose: '#EF4444',
          cyan: '#06B6D4',
          purple: '#A855F7',
        }
      },
      backdropBlur: {
        xs: '2px',
        glass: '20px',
        heavy: '32px',
      },
      borderRadius: {
        '2xl': '18px',
        '3xl': '24px',
      }
    },
  },
  plugins: [],
}
