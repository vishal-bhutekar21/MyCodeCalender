import React, { useEffect, useState } from 'react';
import {
  Plus,
  Trophy,
  Calendar,
  Clock,
  ExternalLink,
  Edit2,
  Trash2,
  Loader2
} from 'lucide-react';
import { format } from 'date-fns';
import { GlassCard } from '../components/ui/GlassCard';
import { SlideOverDrawer } from '../components/ui/SlideOverDrawer';
import { ImageUploader } from '../components/ui/ImageUploader';
import { DeleteConfirmModal } from '../components/ui/DeleteConfirmModal';
import type { CustomContest } from '../types';
import {
  subscribeToCustomContests,
  saveCustomContest,
  deleteCustomContest
} from '../services/firestoreService';

export const CustomContestsCMS: React.FC = () => {
  const [contests, setContests] = useState<CustomContest[]>([]);
  const [loading, setLoading] = useState<boolean>(true);

  // Drawer
  const [isDrawerOpen, setIsDrawerOpen] = useState<boolean>(false);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [formData, setFormData] = useState<Omit<CustomContest, 'id'>>({
    name: '',
    organizer: '',
    bannerUrl: '',
    startTime: Date.now() + 86400000,
    endTime: Date.now() + 86400000 * 2,
    registrationUrl: '',
    tags: [],
    platformName: 'COMMUNITY',
    isActive: true
  });
  const [tagsInput, setTagsInput] = useState<string>('');
  const [isSaving, setIsSaving] = useState<boolean>(false);

  // Delete modal
  const [deleteId, setDeleteId] = useState<string | null>(null);
  const [isDeleting, setIsDeleting] = useState<boolean>(false);

  useEffect(() => {
    const unsubscribe = subscribeToCustomContests((items) => {
      setContests(items);
      setLoading(false);
    });
    return () => unsubscribe();
  }, []);

  const handleOpenCreate = () => {
    setEditingId(null);
    setFormData({
      name: '',
      organizer: '',
      bannerUrl: '',
      startTime: Date.now() + 86400000,
      endTime: Date.now() + 86400000 * 2,
      registrationUrl: '',
      tags: [],
      platformName: 'COMMUNITY',
      isActive: true
    });
    setTagsInput('');
    setIsDrawerOpen(true);
  };

  const handleOpenEdit = (c: CustomContest) => {
    setEditingId(c.id);
    setFormData({
      name: c.name,
      organizer: c.organizer,
      bannerUrl: c.bannerUrl || '',
      startTime: c.startTime,
      endTime: c.endTime,
      registrationUrl: c.registrationUrl,
      tags: c.tags || [],
      platformName: c.platformName || 'COMMUNITY',
      isActive: c.isActive
    });
    setTagsInput((c.tags || []).join(', '));
    setIsDrawerOpen(true);
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.name.trim() || !formData.registrationUrl.trim()) return;

    setIsSaving(true);
    try {
      const parsedTags = tagsInput
        .split(',')
        .map((t) => t.trim().replace(/^#/, ''))
        .filter(Boolean);

      await saveCustomContest(
        {
          ...formData,
          tags: parsedTags
        },
        editingId || undefined
      );
      setIsDrawerOpen(false);
    } catch (err) {
      console.error('Failed to save custom contest:', err);
    } finally {
      setIsSaving(false);
    }
  };

  const handleDeleteConfirm = async () => {
    if (!deleteId) return;
    setIsDeleting(true);
    try {
      await deleteCustomContest(deleteId);
      setDeleteId(null);
    } catch (err) {
      console.error('Failed to delete contest:', err);
    } finally {
      setIsDeleting(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-white flex items-center gap-2">
            <Trophy className="w-6 h-6 text-brand-orange" />
            <span>Hackathons & Custom Contests CMS</span>
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Publish college hackathons, hiring challenges, and community cups into the Contests radar.
          </p>
        </div>

        <button
          type="button"
          onClick={handleOpenCreate}
          className="flex items-center gap-2 px-4 py-2.5 rounded-xl bg-gradient-to-r from-brand-orange to-[#FF8C33] hover:from-[#FF7A1A] text-white text-xs font-bold shadow-lg shadow-orange-500/25 transition-all"
        >
          <Plus className="w-4 h-4" />
          <span>Add Custom Contest</span>
        </button>
      </div>

      {/* Contests Grid */}
      {loading ? (
        <div className="py-20 flex flex-col items-center justify-center text-slate-400">
          <Loader2 className="w-8 h-8 animate-spin text-brand-orange mb-2" />
          <p className="text-xs">Loading Cloud Contests...</p>
        </div>
      ) : contests.length === 0 ? (
        <GlassCard className="py-16 text-center">
          <div className="p-4 rounded-full bg-white/5 w-14 h-14 mx-auto flex items-center justify-center text-slate-400 mb-3">
            <Trophy className="w-6 h-6" />
          </div>
          <h3 className="text-base font-bold text-white">No custom contests</h3>
          <p className="text-xs text-slate-400 mt-1 max-w-sm mx-auto">
            Host or feature a hackathon by clicking "Add Custom Contest".
          </p>
        </GlassCard>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {contests.map((c) => (
            <GlassCard key={c.id} className="p-5 flex flex-col justify-between space-y-4">
              <div className="space-y-3">
                {c.bannerUrl && (
                  <div className="rounded-xl overflow-hidden h-32 bg-black/40 border border-white/10">
                    <img src={c.bannerUrl} alt={c.name} className="w-full h-full object-cover" />
                  </div>
                )}

                <div>
                  <div className="flex items-center gap-2 mb-1">
                    <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-indigo-500/20 text-indigo-400 border border-indigo-500/30">
                      {c.platformName || 'COMMUNITY'}
                    </span>
                    <span className="text-[11px] text-slate-400 font-medium">By {c.organizer}</span>
                  </div>
                  <h3 className="text-sm font-bold text-white line-clamp-1">{c.name}</h3>
                </div>

                <div className="space-y-1.5 text-xs text-slate-300">
                  <div className="flex items-center gap-2">
                    <Calendar className="w-3.5 h-3.5 text-brand-orange" />
                    <span>{format(new Date(c.startTime), 'EEE, dd MMM yyyy • hh:mm a')}</span>
                  </div>
                  <div className="flex items-center gap-2 text-slate-400">
                    <Clock className="w-3.5 h-3.5" />
                    <span>Until {format(new Date(c.endTime), 'dd MMM • hh:mm a')}</span>
                  </div>
                </div>

                {c.tags && c.tags.length > 0 && (
                  <div className="flex flex-wrap gap-1">
                    {c.tags.map((t, idx) => (
                      <span key={idx} className="px-1.5 py-0.5 rounded text-[9px] font-medium bg-white/5 text-slate-300">
                        #{t}
                      </span>
                    ))}
                  </div>
                )}
              </div>

              <div className="pt-3 border-t border-white/5 flex items-center justify-between text-xs">
                <a
                  href={c.registrationUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-brand-orange hover:underline flex items-center gap-1 font-semibold text-xs"
                >
                  <span>Register Page</span>
                  <ExternalLink className="w-3 h-3" />
                </a>

                <div className="flex items-center gap-1">
                  <button
                    type="button"
                    onClick={() => handleOpenEdit(c)}
                    className="p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-white/10"
                  >
                    <Edit2 className="w-3.5 h-3.5" />
                  </button>
                  <button
                    type="button"
                    onClick={() => setDeleteId(c.id)}
                    className="p-1.5 rounded-lg text-slate-400 hover:text-rose-400 hover:bg-rose-500/10"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>
            </GlassCard>
          ))}
        </div>
      )}

      {/* Drawer */}
      <SlideOverDrawer
        isOpen={isDrawerOpen}
        onClose={() => setIsDrawerOpen(false)}
        title={editingId ? 'Edit Custom Contest' : 'Add Custom Contest / Hackathon'}
        subtitle="This event will appear on the Android App Contests screen under Custom Events."
      >
        <form onSubmit={handleSave} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1">
              Contest / Hackathon Name <span className="text-brand-orange">*</span>
            </label>
            <input
              type="text"
              required
              placeholder="e.g. ICPC Asia West Regional Prelims 2026"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              className="w-full px-3.5 py-2.5 rounded-xl text-xs text-white glass-input"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1">
              Organizer / Host <span className="text-brand-orange">*</span>
            </label>
            <input
              type="text"
              required
              placeholder="e.g. IIT Bombay / GDG Community"
              value={formData.organizer}
              onChange={(e) => setFormData({ ...formData, organizer: e.target.value })}
              className="w-full px-3.5 py-2.5 rounded-xl text-xs text-white glass-input"
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1">
                Start Date & Time
              </label>
              <input
                type="datetime-local"
                required
                value={new Date(formData.startTime).toISOString().slice(0, 16)}
                onChange={(e) => setFormData({ ...formData, startTime: new Date(e.target.value).getTime() })}
                className="w-full px-3.5 py-2.5 rounded-xl text-xs text-white glass-input"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1">
                End Date & Time
              </label>
              <input
                type="datetime-local"
                required
                value={new Date(formData.endTime).toISOString().slice(0, 16)}
                onChange={(e) => setFormData({ ...formData, endTime: new Date(e.target.value).getTime() })}
                className="w-full px-3.5 py-2.5 rounded-xl text-xs text-white glass-input"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1">
              Registration Link <span className="text-brand-orange">*</span>
            </label>
            <input
              type="url"
              required
              placeholder="https://unstop.com/hackathons/..."
              value={formData.registrationUrl}
              onChange={(e) => setFormData({ ...formData, registrationUrl: e.target.value })}
              className="w-full px-3.5 py-2.5 rounded-xl text-xs text-white glass-input"
            />
          </div>

          <ImageUploader
            currentImageUrl={formData.bannerUrl}
            folder="contests"
            onImageUploaded={(url) => setFormData({ ...formData, bannerUrl: url })}
            label="Contest Banner Graphic"
          />

          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1">
              Tags
            </label>
            <input
              type="text"
              placeholder="hackathon, dsa, prizes, icpc, students"
              value={tagsInput}
              onChange={(e) => setTagsInput(e.target.value)}
              className="w-full px-3.5 py-2.5 rounded-xl text-xs text-white glass-input"
            />
          </div>

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
              {isSaving && <Loader2 className="w-4 h-4 animate-spin" />}
              <span>{editingId ? 'Update Contest' : 'Publish Contest'}</span>
            </button>
          </div>
        </form>
      </SlideOverDrawer>

      <DeleteConfirmModal
        isOpen={Boolean(deleteId)}
        onClose={() => setDeleteId(null)}
        onConfirm={handleDeleteConfirm}
        title="Delete Custom Contest"
        message="Are you sure you want to remove this contest from the mobile app?"
        isLoading={isDeleting}
      />
    </div>
  );
};
