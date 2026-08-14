import React from 'react';
import { ShieldAlert, LogOut } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { GlassCard } from '../components/ui/GlassCard';

export const UnauthorizedPage: React.FC = () => {
  const { user, logout } = useAuth();

  return (
    <div className="min-h-screen flex items-center justify-center p-4 bg-[#07090E] relative">
      <GlassCard className="w-full max-w-md p-8 text-center space-y-6 border-rose-500/30 bg-rose-950/10">
        <div className="p-4 rounded-full bg-rose-500/20 text-rose-400 w-16 h-16 mx-auto flex items-center justify-center border border-rose-500/30">
          <ShieldAlert className="w-8 h-8" />
        </div>

        <div className="space-y-2">
          <h1 className="text-xl font-black text-white">403 - Access Denied</h1>
          <p className="text-xs text-slate-300 leading-relaxed">
            The account <strong className="text-white font-mono">{user?.email || 'Unknown User'}</strong> is not listed on the Super Admin Whitelist.
          </p>
        </div>

        <p className="text-[11px] text-slate-400">
          If you are a maintainer, contact <span className="text-brand-orange font-mono">vishal.bhutekar1@gmail.com</span> to request CMS privileges.
        </p>

        <div className="pt-4 flex items-center justify-center gap-3">
          <button
            type="button"
            onClick={logout}
            className="flex items-center gap-2 px-4 py-2.5 rounded-xl bg-white/10 hover:bg-white/15 text-white text-xs font-bold transition-all border border-white/10"
          >
            <LogOut className="w-4 h-4" />
            <span>Sign Out & Try Another Account</span>
          </button>
        </div>
      </GlassCard>
    </div>
  );
};
