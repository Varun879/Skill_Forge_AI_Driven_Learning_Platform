import React from 'react';

export const Logo: React.FC<{ className?: string; size?: number }> = ({ className, size = 36 }) => {
  return (
    <div 
      className={cn("flex items-center justify-center shrink-0", className)} 
      style={{ 
        width: size, 
        height: size,
        borderRadius: size * 0.25,
        background: 'linear-gradient(135deg, #6366f1 0%, #4f46e5 50%, #2563eb 100%)',
        overflow: 'hidden'
      }}
    >
      <svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg" style={{ width: '70%', height: '70%' }}>
        {/* Top Bar */}
        <path 
          d="M22 32H78" 
          stroke="white" 
          strokeWidth="12" 
          strokeLinecap="round" 
        />
        {/* Left Vertical */}
        <path 
          d="M22 32V50" 
          stroke="white" 
          strokeWidth="12" 
          strokeLinecap="round" 
        />
        {/* Middle Bar */}
        <path 
          d="M22 50H65" 
          stroke="white" 
          strokeWidth="12" 
          strokeLinecap="round" 
        />
        {/* Middle Vertical */}
        <path 
          d="M44 50V68" 
          stroke="white" 
          strokeWidth="12" 
          strokeLinecap="round" 
        />
        {/* Bottom Bar */}
        <path 
          d="M22 68H44" 
          stroke="white" 
          strokeWidth="12" 
          strokeLinecap="round" 
        />
      </svg>
    </div>
  );
};

import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';

function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}
