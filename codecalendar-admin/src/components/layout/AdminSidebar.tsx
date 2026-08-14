import React from 'react';
import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  BookOpen,
  Megaphone,
  Trophy,
  Users,
  UserX,
  Code2,
  ShieldCheck
} from 'lucide-react';

const NAV_ITEMS = [
  {
    label: 'Dashboard',
    path: '/',
    icon: LayoutDashboard
  },
  {
    label: 'Articles & Sheets',
    path: '/featured-materials',
    icon: BookOpen,
    badge: 'CMS'
  },
  {
    label: 'Broadcasts',
    path: '/broadcasts',
    icon: Megaphone
  },
  {
    label: 'Hackathons',
    path: '/custom-contests',
    icon: Trophy
  },
  {
    label: 'Users Directory',
    path: '/users',
    icon: Users
  },
  {
    label: 'Deletion Queue',
    path: '/deletions',
    icon: UserX,
    danger: true
  }
];

export const AdminSidebar: React.FC = () => {
  return (
    <aside className="w-64 flex-shrink-0 min-h-screen p-4 flex flex-col justify-between border-r border-white/10 bg-[#0A0E17]/90 backdrop-blur-2xl">
      <div>
        {/* Brand Logo */}
        <div className="flex items-center gap-3 px-3 py-4 mb-6">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-[#FF6B00] to-[#FFA048] p-0.5 shadow-lg shadow-orange-500/20 flex items-center justify-center">
            <div className="w-full h-full bg-[#0E121E] rounded-[10px] flex items-center justify-center">
              <Code2 className="w-5 h-5 text-brand-orange" />
            </div>
          </div>
          <div>
            <h1 className="text-base font-extrabold text-white tracking-tight flex items-center gap-1.5">
              <span>CodeCalendar</span>
              <span className="text-[10px] px-1.5 py-0.5 rounded bg-brand-orange/20 text-brand-orange border border-brand-orange/30 font-mono">
                ADMIN
              </span>
            </h1>
            <p className="text-[11px] text-slate-400 font-medium">CMS & Developer Hub</p>
          </div>
        </div>

        {/* Navigation Links */}
        <nav className="space-y-1.5">
          {NAV_ITEMS.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.path}
                to={item.path}
                className={({ isActive }) =>
                  `flex items-center justify-between px-3.5 py-2.5 rounded-xl text-xs font-semibold transition-all duration-200 ${
                    isActive
                      ? 'bg-gradient-to-r from-brand-orange to-[#FF8C33] text-white shadow-lg shadow-orange-500/25'
                      : 'text-slate-400 hover:text-slate-100 hover:bg-white/5'
                  }`
                }
              >
                <div className="flex items-center gap-3">
                  <Icon className="w-4 h-4" />
                  <span>{item.label}</span>
                </div>
                {item.badge && (
                  <span className="text-[9px] px-1.5 py-0.5 rounded-full bg-white/20 text-white font-mono">
                    {item.badge}
                  </span>
                )}
              </NavLink>
            );
          })}
        </nav>
      </div>

      {/* Footer Security Badge */}
      <div className="p-3.5 rounded-xl bg-white/[0.03] border border-white/5 space-y-2">
        <div className="flex items-center gap-2 text-emerald-400 text-xs font-semibold">
          <ShieldCheck className="w-4 h-4" />
          <span>RBAC Protected</span>
        </div>
        <p className="text-[10px] text-slate-400 leading-relaxed">
          Logged in as verified super administrator with Firestore read/write capabilities.
        </p>
      </div>
    </aside>
  );
};
