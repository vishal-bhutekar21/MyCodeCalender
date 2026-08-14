import React, { useEffect, useState } from 'react';
import {
  Users,
  Search,
  User as UserIcon,
  Smartphone,
  ShieldCheck,
  Loader2
} from 'lucide-react';
import { GlassCard } from '../components/ui/GlassCard';
import type { UserAccount } from '../types';
import { subscribeToUsers } from '../services/firestoreService';

export const UsersDirectory: React.FC = () => {
  const [users, setUsers] = useState<UserAccount[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [searchQuery, setSearchQuery] = useState<string>('');

  useEffect(() => {
    const unsubscribe = subscribeToUsers((items) => {
      setUsers(items);
      setLoading(false);
    });
    return () => unsubscribe();
  }, []);

  const filteredUsers = users.filter((u) => {
    const term = searchQuery.toLowerCase();
    const matchesName = (u.displayName || '').toLowerCase().includes(term);
    const matchesEmail = (u.email || '').toLowerCase().includes(term);
    const matchesUid = (u.uid || '').toLowerCase().includes(term);
    const matchesHandles = Object.values(u.connectedAccountsMap || {}).some((h) =>
      h.toLowerCase().includes(term)
    );

    return matchesName || matchesEmail || matchesUid || matchesHandles;
  });

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-white flex items-center gap-2">
            <Users className="w-6 h-6 text-brand-orange" />
            <span>Registered Users Directory</span>
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Real-time directory of authenticated developers and their cloud-synced platform handles.
          </p>
        </div>

        <div className="px-3.5 py-1.5 rounded-xl bg-white/5 border border-white/10 text-xs font-semibold text-slate-300">
          Total Users: <span className="text-brand-orange font-bold font-mono">{users.length}</span>
        </div>
      </div>

      {/* Search Filter */}
      <GlassCard className="p-4">
        <div className="relative w-full max-w-md">
          <Search className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" />
          <input
            type="text"
            placeholder="Search by name, email, UID, or platform handle (@handle)..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full pl-9 pr-4 py-2 rounded-xl text-xs text-white glass-input placeholder:text-slate-500"
          />
        </div>
      </GlassCard>

      {/* Users Table */}
      {loading ? (
        <div className="py-20 flex flex-col items-center justify-center text-slate-400">
          <Loader2 className="w-8 h-8 animate-spin text-brand-orange mb-2" />
          <p className="text-xs">Loading Registered Users...</p>
        </div>
      ) : filteredUsers.length === 0 ? (
        <GlassCard className="py-16 text-center">
          <div className="p-4 rounded-full bg-white/5 w-14 h-14 mx-auto flex items-center justify-center text-slate-400 mb-3">
            <Users className="w-6 h-6" />
          </div>
          <h3 className="text-base font-bold text-white">No users found</h3>
          <p className="text-xs text-slate-400 mt-1">
            {searchQuery ? 'No accounts match the current query.' : 'Users will appear here as they log into the app.'}
          </p>
        </GlassCard>
      ) : (
        <GlassCard className="overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="border-b border-white/10 bg-black/30 text-slate-400 uppercase tracking-wider font-semibold">
                <tr>
                  <th className="py-3.5 px-5">Developer Profile</th>
                  <th className="py-3.5 px-5">Provider</th>
                  <th className="py-3.5 px-5">Connected Handles</th>
                  <th className="py-3.5 px-5">App Version</th>
                  <th className="py-3.5 px-5">User ID</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-white/5">
                {filteredUsers.map((u) => (
                  <tr key={u.uid} className="hover:bg-white/[0.02] transition-colors">
                    {/* Name & Avatar */}
                    <td className="py-4 px-5">
                      <div className="flex items-center gap-3">
                        {u.photoUrl ? (
                          <img
                            src={u.photoUrl}
                            alt="Avatar"
                            className="w-9 h-9 rounded-full border border-white/20 object-cover"
                          />
                        ) : (
                          <div className="w-9 h-9 rounded-full bg-brand-orange/20 border border-brand-orange/30 text-brand-orange flex items-center justify-center font-bold">
                            <UserIcon className="w-4 h-4" />
                          </div>
                        )}
                        <div>
                          <p className="font-bold text-white text-sm flex items-center gap-1.5">
                            <span>{u.displayName || 'Developer'}</span>
                            {u.email === 'vishal.bhutekar1@gmail.com' && (
                              <span title="Super Admin">
                                <ShieldCheck className="w-3.5 h-3.5 text-brand-orange" />
                              </span>
                            )}
                          </p>
                          <p className="text-[11px] text-slate-400 font-mono">{u.email || 'No email attached'}</p>
                        </div>
                      </div>
                    </td>

                    {/* Auth Provider */}
                    <td className="py-4 px-5">
                      <span className="px-2.5 py-1 rounded-md text-[10px] font-bold uppercase tracking-wider bg-white/5 border border-white/10 text-slate-300">
                        {u.authProvider || 'Google'}
                      </span>
                    </td>

                    {/* Connected Handles */}
                    <td className="py-4 px-5">
                      {u.connectedAccountsMap && Object.keys(u.connectedAccountsMap).length > 0 ? (
                        <div className="flex flex-wrap gap-1.5 max-w-xs">
                          {Object.entries(u.connectedAccountsMap).map(([p, handle]) => (
                            <span
                              key={p}
                              className="px-2 py-0.5 rounded text-[10px] font-semibold bg-brand-orange/15 text-brand-orange border border-brand-orange/30 font-mono"
                            >
                              {p}: {handle}
                            </span>
                          ))}
                        </div>
                      ) : (
                        <span className="text-slate-500 text-[11px] italic">None linked</span>
                      )}
                    </td>

                    {/* App Version */}
                    <td className="py-4 px-5">
                      <div className="flex items-center gap-1.5 text-slate-300">
                        <Smartphone className="w-3.5 h-3.5 text-slate-400" />
                        <span className="font-mono text-xs">{u.appVersion || '1.0.0'}</span>
                      </div>
                    </td>

                    {/* User UID */}
                    <td className="py-4 px-5">
                      <span className="font-mono text-[10px] text-slate-400 select-all">
                        {u.uid}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </GlassCard>
      )}
    </div>
  );
};
