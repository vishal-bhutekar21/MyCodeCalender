import React, { useEffect, useState } from 'react';
import {
  Plus,
  Megaphone,
  Eye,
  EyeOff,
  Edit2,
  Trash2,
  ExternalLink,
  Sparkles,
  Loader2
} from 'lucide-react';
import { GlassCard } from '../components/ui/GlassCard';
import { SlideOverDrawer } from '../components/ui/SlideOverDrawer';
import { ImageUploader } from '../components/ui/ImageUploader';
import { DeleteConfirmModal } from '../components/ui/DeleteConfirmModal';
import type { Broadcast, BroadcastBadge } from '../types';
import {
  subscribeToBroadcasts,
  saveBroadcast,
  deleteBroadcast,
  toggleBroadcastActive
} from '../services/firestoreService';

const BADGES: BroadcastBadge[] = ['NOTICE', 'HOT', 'ALERT', 'UPDATE'];

export const BroadcastsCMS: React.FC = () => {
  const [broadcasts, setBroadcasts] = useState<Broadcast[]>([]);
  const [loading, setLoading] = useState<boolean>(true);

  // Drawer state
  const [isDrawerOpen, setIsDrawerOpen] = useState<boolean>(false);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [formData, setFormData] = useState<Omit<Broadcast, 'id'>>({
    title: '',
    subtitle: '',
    badge: 'HOT',
    actionUrl: '',
    bannerImageUrl: '',
    isActive: true
  });
  const [isSaving, setIsSaving] = useState<boolean>(false);

  // Delete modal state
  const [deleteId, setDeleteId] = useState<string | null>(null);
  const [isDeleting, setIsDeleting] = useState<boolean>(false);

  useEffect(() => {
    const unsubscribe = subscribeToBroadcasts((items) => {
      setBroadcasts(items);
      setLoading(false);
    });
    return () => unsubscribe();
  }, []);

  const handleOpenCreate = () => {
    setEditingId(null);
    setFormData({
      title: '',
      subtitle: '',
      badge: 'HOT',
      actionUrl: '',
      bannerImageUrl: '',
      isActive: true
    });
    setIsDrawerOpen(true);
  };

  const handleOpenEdit = (b: Broadcast) => {
    setEditingId(b.id);
    setFormData({
      title: b.title,
      subtitle: b.subtitle,
      badge: b.badge,
      actionUrl: b.actionUrl,
      bannerImageUrl: b.bannerImageUrl || '',
      isActive: b.isActive
    });
    setIsDrawerOpen(true);
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.title.trim()) return;

    setIsSaving(true);
    try {
      await saveBroadcast(formData, editingId || undefined);
      setIsDrawerOpen(false);
    } catch (err) {
      console.error('Failed to save broadcast:', err);
    } finally {
      setIsSaving(false);
    }
  };

  const handleDeleteConfirm = async () => {
    if (!deleteId) return;
    setIsDeleting(true);
    try {
      await deleteBroadcast(deleteId);
      setDeleteId(null);
    } catch (err) {
      console.error('Failed to delete broadcast:', err);
    } finally {
      setIsDeleting(false);
    }
  };

  const getBadgeStyle = (badge: BroadcastBadge) => {
    switch (badge) {
      case 'HOT':
        return 'bg-rose-500/20 text-rose-400 border-rose-500/30';
      case 'ALERT':
        return 'bg-amber-500/20 text-amber-400 border-amber-500/30';
      case 'UPDATE':
        return 'bg-cyan-500/20 text-cyan-400 border-cyan-500/30';
      default:
        return 'bg-brand-orange/20 text-brand-orange border-brand-orange/30';
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-white flex items-center gap-2">
            <Megaphone className="w-6 h-6 text-brand-orange" />
            <span>In-App Broadcasts & Announcements</span>
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Publish real-time banners and announcements directly atop the Android Home Screen.
          </p>
        </div>

        <button
          type="button"
          onClick={handleOpenCreate}
          className="flex items-center gap-2 px-4 py-2.5 rounded-xl bg-gradient-to-r from-brand-orange to-[#FF8C33] hover:from-[#FF7A1A] hover:to-[#FFA04D] text-white text-xs font-bold shadow-lg shadow-orange-500/25 transition-all"
        >
          <Plus className="w-4 h-4" />
          <span>New Broadcast Banner</span>
        </button>
      </div>

      {/* Broadcasts List */}
      {loading ? (
        <div className="py-20 flex flex-col items-center justify-center text-slate-400">
          <Loader2 className="w-8 h-8 animate-spin text-brand-orange mb-2" />
          <p className="text-xs">Loading Cloud Broadcasts...</p>
        </div>
      ) : broadcasts.length === 0 ? (
        <GlassCard className="py-16 text-center">
          <div className="p-4 rounded-full bg-white/5 w-14 h-14 mx-auto flex items-center justify-center text-slate-400 mb-3">
            <Megaphone className="w-6 h-6" />
          </div>
          <h3 className="text-base font-bold text-white">No active broadcasts</h3>
          <p className="text-xs text-slate-400 mt-1 max-w-sm mx-auto">
            Click "New Broadcast Banner" to alert mobile users about upcoming hackathons or releases.
          </p>
        </GlassCard>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {broadcasts.map((b) => (
            <GlassCard key={b.id} className="p-6 space-y-4">
              <div className="flex items-start justify-between gap-4">
                <div className="flex items-center gap-2">
                  <span className={`px-2 py-0.5 rounded-md text-[10px] font-bold border ${getBadgeStyle(b.badge)}`}>
                    {b.badge}
                  </span>
                  <button
                    type="button"
                    onClick={() => toggleBroadcastActive(b.id, b.isActive)}
                    className={`px-2 py-0.5 rounded-full text-[10px] font-bold flex items-center gap-1 transition-all ${
                      b.isActive
                        ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30'
                        : 'bg-rose-500/20 text-rose-400 border border-rose-500/30'
                    }`}
                  >
                    {b.isActive ? <Eye className="w-3 h-3" /> : <EyeOff className="w-3 h-3" />}
                    <span>{b.isActive ? 'ACTIVE IN APP' : 'PAUSED'}</span>
                  </button>
                </div>

                <div className="flex items-center gap-1">
                  <button
                    type="button"
                    onClick={() => handleOpenEdit(b)}
                    className="p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-white/10"
                    title="Edit"
                  >
                    <Edit2 className="w-3.5 h-3.5" />
                  </button>
                  <button
                    type="button"
                    onClick={() => setDeleteId(b.id)}
                    className="p-1.5 rounded-lg text-slate-400 hover:text-rose-400 hover:bg-rose-500/10"
                    title="Delete"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>

              <div>
                <h3 className="text-base font-bold text-white">{b.title}</h3>
                <p className="text-xs text-slate-300 mt-1 leading-relaxed">{b.subtitle}</p>
              </div>

              {b.bannerImageUrl && (
                <div className="rounded-xl overflow-hidden h-28 border border-white/10 bg-black/40">
                  <img src={b.bannerImageUrl} alt="Banner" className="w-full h-full object-cover" />
                </div>
              )}

              {b.actionUrl && (
                <div className="pt-2 border-t border-white/5 flex items-center justify-between text-xs text-slate-400">
                  <span className="font-mono text-[11px] truncate max-w-xs">{b.actionUrl}</span>
                  <a
                    href={b.actionUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="text-brand-orange flex items-center gap-1 font-semibold hover:underline"
                  >
                    <span>Test CTA</span>
                    <ExternalLink className="w-3 h-3" />
                  </a>
                </div>
              )}
            </GlassCard>
          ))}
        </div>
      )}

      {/* Drawer */}
      <SlideOverDrawer
        isOpen={isDrawerOpen}
        onClose={() => setIsDrawerOpen(false)}
        title={editingId ? 'Edit Broadcast Banner' : 'Create In-App Broadcast'}
        subtitle="This announcement banner will render atop the mobile app Home screen."
      >
        <form onSubmit={handleSave} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1">
              Badge Type
            </label>
            <div className="grid grid-cols-4 gap-2">
              {BADGES.map((badge) => (
                <button
                  key={badge}
                  type="button"
                  onClick={() => setFormData({ ...formData, badge })}
                  className={`py-2 rounded-xl text-xs font-bold border transition-all ${
                    formData.badge === badge
                      ? 'border-brand-orange bg-brand-orange/20 text-brand-orange shadow-md'
                      : 'border-white/10 text-slate-400 hover:border-white/20'
                  }`}
                >
                  {badge}
                </button>
              ))}
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1">
              Headline Title <span className="text-brand-orange">*</span>
            </label>
            <input
              type="text"
              required
              placeholder="e.g. ⚡ Google Kickstart Alumni Cup Live!"
              value={formData.title}
              onChange={(e) => setFormData({ ...formData, title: e.target.value })}
              className="w-full px-3.5 py-2.5 rounded-xl text-xs text-white glass-input"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1">
              Subtitle / Description <span className="text-brand-orange">*</span>
            </label>
            <textarea
              required
              rows={3}
              placeholder="Join 5,000+ competitive coders this Saturday at 8 PM IST. Free registration & certificates."
              value={formData.subtitle}
              onChange={(e) => setFormData({ ...formData, subtitle: e.target.value })}
              className="w-full px-3.5 py-2.5 rounded-xl text-xs text-white glass-input resize-none"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1">
              Action URL (Optional deep link or registration page)
            </label>
            <input
              type="url"
              placeholder="https://mycodecalendar.app/event/kickstart"
              value={formData.actionUrl}
              onChange={(e) => setFormData({ ...formData, actionUrl: e.target.value })}
              className="w-full px-3.5 py-2.5 rounded-xl text-xs text-white glass-input"
            />
          </div>

          <ImageUploader
            currentImageUrl={formData.bannerImageUrl}
            folder="broadcasts"
            onImageUploaded={(url) => setFormData({ ...formData, bannerImageUrl: url })}
            label="Banner Graphic (Optional)"
          />

          <div className="pt-4 border-t border-white/10 flex items-center justify-end gap-3">
            <button
              type="button"
              onClick={() => setIsDrawerOpen(false)}
              className="px-4 py-2 rounded-xl text-xs font-semibold text-slate-300 hover:bg-white/5"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSaving}
              className="px-5 py-2.5 rounded-xl text-xs font-bold bg-gradient-to-r from-brand-orange to-[#FF8C33] text-white shadow-lg shadow-orange-500/25 flex items-center gap-2"
            >
              {isSaving ? <Loader2 className="w-4 h-4 animate-spin" /> : <Sparkles className="w-4 h-4" />}
              <span>{editingId ? 'Update Banner' : 'Publish Banner'}</span>
            </button>
          </div>
        </form>
      </SlideOverDrawer>

      <DeleteConfirmModal
        isOpen={Boolean(deleteId)}
        onClose={() => setDeleteId(null)}
        onConfirm={handleDeleteConfirm}
        title="Delete In-App Broadcast"
        message="Are you sure you want to delete this broadcast? It will immediately stop rendering on mobile devices."
        isLoading={isDeleting}
      />
    </div>
  );
};
