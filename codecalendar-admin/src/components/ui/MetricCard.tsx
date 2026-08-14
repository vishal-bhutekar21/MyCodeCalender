import React from 'react';
import type { LucideIcon } from 'lucide-react';
import { GlassCard } from './GlassCard';

interface MetricCardProps {
  title: string;
  value: number | string;
  changeText?: string;
  isPositive?: boolean;
  icon: LucideIcon;
  accentColor: string;
  glowColor?: string;
}

export const MetricCard: React.FC<MetricCardProps> = ({
  title,
  value,
  changeText,
  isPositive = true,
  icon: Icon,
  accentColor,
  glowColor
}) => {
  return (
    <GlassCard className="p-6 overflow-hidden group hover:border-white/20" glowColor={glowColor}>
      <div className="flex items-start justify-between">
        <div>
          <p className="text-xs font-semibold uppercase tracking-wider text-slate-400">
            {title}
          </p>
          <h3 className="mt-2 text-3xl font-extrabold text-white tracking-tight">
            {value}
          </h3>
          {changeText && (
            <p className={`mt-2 text-xs font-medium flex items-center gap-1 ${
              isPositive ? 'text-emerald-400' : 'text-rose-400'
            }`}>
              <span>{isPositive ? '↑' : '↓'}</span>
              <span>{changeText}</span>
            </p>
          )}
        </div>
        <div
          className="p-3 rounded-xl border border-white/10 transition-transform duration-300 group-hover:scale-110"
          style={{ backgroundColor: `${accentColor}1A`, color: accentColor }}
        >
          <Icon className="w-6 h-6" />
        </div>
      </div>
    </GlassCard>
  );
};
