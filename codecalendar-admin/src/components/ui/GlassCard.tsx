import React from 'react';

interface GlassCardProps extends React.HTMLAttributes<HTMLDivElement> {
  children: React.ReactNode;
  className?: string;
  elevated?: boolean;
  glowColor?: string;
}

export const GlassCard: React.FC<GlassCardProps> = ({
  children,
  className = '',
  elevated = false,
  glowColor,
  ...props
}) => {
  return (
    <div
      className={`relative rounded-2xl transition-all duration-300 ${
        elevated ? 'glass-panel-elevated' : 'glass-panel'
      } ${className}`}
      style={
        glowColor
          ? {
              boxShadow: `0 10px 30px -10px ${glowColor}`,
              borderColor: 'rgba(255, 255, 255, 0.12)'
            }
          : undefined
      }
      {...props}
    >
      {children}
    </div>
  );
};
