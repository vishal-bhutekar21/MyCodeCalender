import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  Users,
  BookOpen,
  Megaphone,
  Trophy,
  UserX,
  Plus,
  ArrowUpRight,
  ShieldCheck,
  Flame,
  Loader2
} from 'lucide-react';
import { GlassCard } from '../components/ui/GlassCard';
import { MetricCard } from '../components/ui/MetricCard';
import type { DashboardMetrics } from '../types';
import { fetchDashboardMetrics } from '../services/firestoreService';
import { useAuth } from '../context/AuthContext';

export const DashboardOverview: React.FC = () => {
  const { user } = useAuth();
  const [metrics, setMetrics] = useState<DashboardMetrics>({
    totalUsers: 0,
    activeBroadcasts: 0,
    publishedMaterials: 0,
    pendingDeletions: 0,
    totalCustomContests: 0
  });
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    fetchDashboardMetrics().then((m) => {
      setMetrics(m);
      setLoading(false);
    });
  }, []);

  return (
    <div className="space-y-8">
      {/* Welcome Banner */}
      <GlassCard className="p-8 relative overflow-hidden bg-gradient-to-r from-[#121826]/90 via-[#182136]/70 to-[#121826]/90 border-white/10">
        <div className="relative z-10 max-w-2xl">
          <div className="flex items-center gap-2 px-2.5 py-1 rounded-full bg-brand-orange/15 border border-brand-orange/30 text-brand-orange text-xs font-semibold w-fit mb-3">
            <Flame className="w-3.5 h-3.5" />
            <span>Admin Command Center</span>
          </div>
          <h1 className="text-3xl font-black text-white tracking-tight leading-tight">
            Welcome back, {user?.displayName || 'Administrator'}
          </h1>
          <p className="text-sm text-slate-300 mt-2 leading-relaxed">
            Manage live in-app broadcasts, upload curated DSA & AI problem sheets, review user growth, and monitor system health in real-time.
          </p>

          <div className="flex flex-wrap items-center gap-3 mt-6">
            <Link
              to="/featured-materials"
              className="flex items-center gap-2 px-4 py-2.5 rounded-xl bg-brand-orange hover:bg-[#FF7A1A] text-white text-xs font-bold shadow-lg shadow-orange-500/25 transition-all"
            >
              <Plus className="w-4 h-4" />
              <span>Upload New Article / Sheet</span>
            </Link>
            <Link
              to="/broadcasts"
              className="flex items-center gap-2 px-4 py-2.5 rounded-xl bg-white/10 hover:bg-white/15 text-white text-xs font-bold border border-white/10 transition-all"
            >
              <Megaphone className="w-4 h-4" />
              <span>Publish In-App Banner</span>
            </Link>
          </div>
        </div>
      </GlassCard>

      {/* KPI Metrics Grid */}
      {loading ? (
        <div className="py-12 flex justify-center text-brand-orange">
          <Loader2 className="w-8 h-8 animate-spin" />
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
          <MetricCard
            title="Registered Coders"
            value={metrics.totalUsers}
            changeText="Cloud Synced"
            icon={Users}
            accentColor="#FF6B00"
            glowColor="rgba(255, 107, 0, 0.15)"
          />
          <MetricCard
            title="Articles & Sheets"
            value={metrics.publishedMaterials}
            changeText="Active in Hub"
            icon={BookOpen}
            accentColor="#818CF8"
            glowColor="rgba(129, 140, 248, 0.15)"
          />
          <MetricCard
            title="Active Broadcasts"
            value={metrics.activeBroadcasts}
            changeText="Live In App"
            icon={Megaphone}
            accentColor="#10B981"
            glowColor="rgba(16, 185, 129, 0.15)"
          />
          <MetricCard
            title="Erasure Tickets"
            value={metrics.pendingDeletions}
            changeText={metrics.pendingDeletions > 0 ? "Pending Action" : "All Clear"}
            isPositive={metrics.pendingDeletions === 0}
            icon={UserX}
            accentColor={metrics.pendingDeletions > 0 ? "#EF4444" : "#10B981"}
            glowColor={metrics.pendingDeletions > 0 ? "rgba(239, 68, 68, 0.2)" : undefined}
          />
        </div>
      )}

      {/* Quick Navigation Hubs */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <GlassCard className="p-6 space-y-4 hover:border-brand-orange/40 transition-all group">
          <div className="p-3 rounded-xl bg-brand-orange/15 text-brand-orange border border-brand-orange/30 w-fit">
            <BookOpen className="w-6 h-6" />
          </div>
          <div>
            <h3 className="text-base font-bold text-white group-hover:text-brand-orange transition-colors">
              Articles & Sheets CMS
            </h3>
            <p className="text-xs text-slate-400 mt-1 leading-relaxed">
              Curate and order top DSA problem sheets, AI & ML roadmaps, and tutorials with live thumbnail upload and app preview.
            </p>
          </div>
          <Link
            to="/featured-materials"
            className="text-xs font-bold text-brand-orange flex items-center gap-1 hover:underline pt-2"
          >
            <span>Open Studio</span>
            <ArrowUpRight className="w-3.5 h-3.5" />
          </Link>
        </GlassCard>

        <GlassCard className="p-6 space-y-4 hover:border-indigo-400/40 transition-all group">
          <div className="p-3 rounded-xl bg-indigo-500/15 text-indigo-400 border border-indigo-500/30 w-fit">
            <Megaphone className="w-6 h-6" />
          </div>
          <div>
            <h3 className="text-base font-bold text-white group-hover:text-indigo-400 transition-colors">
              In-App Broadcasts
            </h3>
            <p className="text-xs text-slate-400 mt-1 leading-relaxed">
              Deploy real-time announcement banners to thousands of mobile users with single-click activation.
            </p>
          </div>
          <Link
            to="/broadcasts"
            className="text-xs font-bold text-indigo-400 flex items-center gap-1 hover:underline pt-2"
          >
            <span>Manage Broadcasts</span>
            <ArrowUpRight className="w-3.5 h-3.5" />
          </Link>
        </GlassCard>

        <GlassCard className="p-6 space-y-4 hover:border-emerald-400/40 transition-all group">
          <div className="p-3 rounded-xl bg-emerald-500/15 text-emerald-400 border border-emerald-500/30 w-fit">
            <Trophy className="w-6 h-6" />
          </div>
          <div>
            <h3 className="text-base font-bold text-white group-hover:text-emerald-400 transition-colors">
              Hackathons & Contests
            </h3>
            <p className="text-xs text-slate-400 mt-1 leading-relaxed">
              Publish upcoming university coding championships, hackathons, and custom community contests.
            </p>
          </div>
          <Link
            to="/custom-contests"
            className="text-xs font-bold text-emerald-400 flex items-center gap-1 hover:underline pt-2"
          >
            <span>Add Contests</span>
            <ArrowUpRight className="w-3.5 h-3.5" />
          </Link>
        </GlassCard>
      </div>

      {/* System Security & Compliance Bar */}
      <GlassCard className="p-6 flex flex-col sm:flex-row items-center justify-between gap-4 border-emerald-500/20 bg-emerald-950/10">
        <div className="flex items-center gap-3">
          <div className="p-3 rounded-xl bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">
            <ShieldCheck className="w-6 h-6" />
          </div>
          <div>
            <h4 className="text-sm font-bold text-white">Google Play Data Safety & GDPR Protected</h4>
            <p className="text-xs text-slate-400 mt-0.5">
              Admin sessions are signed and validated against the super admin whitelist.
            </p>
          </div>
        </div>

        <Link
          to="/deletions"
          className="px-4 py-2 rounded-xl text-xs font-bold bg-white/10 hover:bg-white/15 text-white border border-white/10 transition-all"
        >
          Review Data Requests
        </Link>
      </GlassCard>
    </div>
  );
};
