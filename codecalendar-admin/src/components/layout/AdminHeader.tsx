import React from 'react';
import { LogOut, User as UserIcon, Shield, Radio } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

export const AdminHeader: React.FC = () => {
  const { user, logout } = useAuth();

  return (
    <header className="h-16 border-b border-white/10 bg-[#0A0E17]/80 backdrop-blur-xl px-8 flex items-center justify-between sticky top-0 z-30">
      {/* Live System Indicator */}
      <div className="flex items-center gap-3">
        <div className="flex items-center gap-2 px-3 py-1.5 rounded-full bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-xs font-semibold">
          <Radio className="w-3.5 h-3.5 animate-pulse text-emerald-400" />
          <span>Firestore Live Sync Active</span>
        </div>
      </div>

      {/* Admin User Info & Actions */}
      <div className="flex items-center gap-4">
        <div className="flex items-center gap-3 pl-4 border-l border-white/10">
          {user?.photoURL ? (
            <img
              src={user.photoURL}
              alt="Avatar"
              className="w-8 h-8 rounded-full border border-white/20 object-cover"
            />
          ) : (
            <div className="w-8 h-8 rounded-full bg-brand-orange/20 border border-brand-orange/30 text-brand-orange flex items-center justify-center font-bold text-xs">
              <UserIcon className="w-4 h-4" />
            </div>
          )}

          <div className="hidden sm:block text-left">
            <p className="text-xs font-bold text-white flex items-center gap-1.5">
              <span>{user?.displayName || 'Administrator'}</span>
              <Shield className="w-3 h-3 text-brand-orange" />
            </p>
            <p className="text-[10px] text-slate-400 font-mono">
              {user?.email || 'admin@mycodecalendar.app'}
            </p>
          </div>

          <button
            type="button"
            onClick={logout}
            className="p-2 ml-2 rounded-xl text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 border border-transparent hover:border-rose-500/20 transition-all text-xs"
            title="Sign Out"
          >
            <LogOut className="w-4 h-4" />
          </button>
        </div>
      </div>
    </header>
  );
};
