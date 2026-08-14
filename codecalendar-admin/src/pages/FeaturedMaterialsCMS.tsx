import React, { useEffect, useState } from 'react';
import {
  Plus,
  Search,
  BookOpen,
  Star,
  Zap,
  ExternalLink,
  Edit2,
  Trash2,
  Eye,
  EyeOff,
  Sparkles,
  CheckCircle2,
  Loader2
} from 'lucide-react';
import { GlassCard } from '../components/ui/GlassCard';
import { SlideOverDrawer } from '../components/ui/SlideOverDrawer';
import { ImageUploader } from '../components/ui/ImageUploader';
import { LiveAppMockupPreview } from '../components/ui/LiveAppMockupPreview';
import { DeleteConfirmModal } from '../components/ui/DeleteConfirmModal';
import type {
  FeaturedMaterial,
  MaterialCategory
} from '../types';
import {
  subscribeToFeaturedMaterials,
  saveFeaturedMaterial,
  deleteFeaturedMaterial,
  toggleMaterialActive
} from '../services/firestoreService';

const CATEGORIES: MaterialCategory[] = [
  'DSA & CP Sheets',
  'Articles & Roadmaps',
  'AI & ML',
  'YouTube Playlists',
  'System Design',
  'Web & Mobile'
];

const INITIAL_FORM: Omit<FeaturedMaterial, 'id'> = {
  title: '',
  description: '',
  category: 'DSA & CP Sheets',
  creator: '',
  imageUrl: '',
  redirectUrl: '',
  priority: 1,
  isActive: true,
  tags: [],
  readTimeOrDuration: ''
};

export const FeaturedMaterialsCMS: React.FC = () => {
  const [materials, setMaterials] = useState<FeaturedMaterial[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [selectedCategory, setSelectedCategory] = useState<string>('ALL');

  // Drawer state
  const [isDrawerOpen, setIsDrawerOpen] = useState<boolean>(false);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [formData, setFormData] = useState<Omit<FeaturedMaterial, 'id'>>(INITIAL_FORM);
  const [tagsInput, setTagsInput] = useState<string>('');
  const [isSaving, setIsSaving] = useState<boolean>(false);
  const [saveSuccess, setSaveSuccess] = useState<boolean>(false);

  // Delete modal state
  const [deleteId, setDeleteId] = useState<string | null>(null);
  const [isDeleting, setIsDeleting] = useState<boolean>(false);

  useEffect(() => {
    const unsubscribe = subscribeToFeaturedMaterials((items) => {
      setMaterials(items);
      setLoading(false);
    });
    return () => unsubscribe();
  }, []);

  const handleOpenCreate = () => {
    setEditingId(null);
    setFormData(INITIAL_FORM);
    setTagsInput('');
    setSaveSuccess(false);
    setIsDrawerOpen(true);
  };

  const handleOpenEdit = (material: FeaturedMaterial) => {
    setEditingId(material.id);
    setFormData({
      title: material.title,
      description: material.description,
      category: material.category,
      creator: material.creator,
      imageUrl: material.imageUrl || '',
      redirectUrl: material.redirectUrl,
      priority: material.priority,
      isActive: material.isActive,
      tags: material.tags || [],
      readTimeOrDuration: material.readTimeOrDuration || ''
    });
    setTagsInput((material.tags || []).join(', '));
    setSaveSuccess(false);
    setIsDrawerOpen(true);
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.title.trim() || !formData.redirectUrl.trim()) return;

    setIsSaving(true);
    try {
      const parsedTags = tagsInput
        .split(',')
        .map((t) => t.trim().replace(/^#/, ''))
        .filter(Boolean);

      await saveFeaturedMaterial(
        {
          ...formData,
          tags: parsedTags
        },
        editingId || undefined
      );

      setSaveSuccess(true);
      setTimeout(() => {
        setIsDrawerOpen(false);
        setSaveSuccess(false);
      }, 700);
    } catch (err) {
      console.error('Failed to save material:', err);
    } finally {
      setIsSaving(false);
    }
  };

  const handleDeleteConfirm = async () => {
    if (!deleteId) return;
    setIsDeleting(true);
    try {
      await deleteFeaturedMaterial(deleteId);
      setDeleteId(null);
    } catch (err) {
      console.error('Failed to delete material:', err);
    } finally {
      setIsDeleting(false);
    }
  };

  const filteredMaterials = materials.filter((item) => {
    const matchesSearch =
      item.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
      item.creator.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (item.tags || []).some((t) => t.toLowerCase().includes(searchQuery.toLowerCase()));

    const matchesCategory =
      selectedCategory === 'ALL' || item.category === selectedCategory;

    return matchesSearch && matchesCategory;
  });

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-white flex items-center gap-2">
            <BookOpen className="w-6 h-6 text-brand-orange" />
            <span>Articles & Featured Sheets CMS</span>
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Curate, upload, and rank developer roadmaps, DSA sheets, YouTube guides, and articles.
          </p>
        </div>

        <button
          type="button"
          onClick={handleOpenCreate}
          className="flex items-center gap-2 px-4 py-2.5 rounded-xl bg-gradient-to-r from-brand-orange to-[#FF8C33] hover:from-[#FF7A1A] hover:to-[#FFA04D] text-white text-xs font-bold shadow-lg shadow-orange-500/25 transition-all"
        >
          <Plus className="w-4 h-4" />
          <span>Upload New Article / Sheet</span>
        </button>
      </div>

      {/* Filter & Search Bar */}
      <GlassCard className="p-4 flex flex-col md:flex-row items-center justify-between gap-4">
        {/* Search Input */}
        <div className="relative w-full md:w-80">
          <Search className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" />
          <input
            type="text"
            placeholder="Search by title, creator, or #tag..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full pl-9 pr-4 py-2 rounded-xl text-xs text-white glass-input placeholder:text-slate-500"
          />
        </div>

        {/* Category Pills */}
        <div className="flex items-center gap-1.5 overflow-x-auto w-full md:w-auto pb-1 md:pb-0">
          <button
            type="button"
            onClick={() => setSelectedCategory('ALL')}
            className={`px-3 py-1.5 rounded-xl text-xs font-semibold whitespace-nowrap transition-all ${
              selectedCategory === 'ALL'
                ? 'bg-white/20 text-white border border-white/20'
                : 'text-slate-400 hover:text-slate-200 hover:bg-white/5'
            }`}
          >
            All Categories ({materials.length})
          </button>
          {CATEGORIES.map((cat) => {
            const count = materials.filter((m) => m.category === cat).length;
            return (
              <button
                key={cat}
                type="button"
                onClick={() => setSelectedCategory(cat)}
                className={`px-3 py-1.5 rounded-xl text-xs font-semibold whitespace-nowrap transition-all ${
                  selectedCategory === cat
                    ? 'bg-brand-orange text-white shadow-md shadow-orange-500/20'
                    : 'text-slate-400 hover:text-slate-200 hover:bg-white/5'
                }`}
              >
                {cat} ({count})
              </button>
            );
          })}
        </div>
      </GlassCard>

      {/* Materials Table / Cards Grid */}
      {loading ? (
        <div className="py-20 flex flex-col items-center justify-center text-slate-400">
          <Loader2 className="w-8 h-8 animate-spin text-brand-orange mb-2" />
          <p className="text-xs">Loading Cloud Featured Materials...</p>
        </div>
      ) : filteredMaterials.length === 0 ? (
        <GlassCard className="py-16 text-center">
          <div className="p-4 rounded-full bg-white/5 w-14 h-14 mx-auto flex items-center justify-center text-slate-400 mb-3">
            <BookOpen className="w-6 h-6" />
          </div>
          <h3 className="text-base font-bold text-white">No materials found</h3>
          <p className="text-xs text-slate-400 mt-1 max-w-sm mx-auto">
            {searchQuery || selectedCategory !== 'ALL'
              ? 'No articles match your active search filters.'
              : 'Start by clicking "Upload New Article / Sheet" to curate content for mobile users.'}
          </p>
        </GlassCard>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredMaterials.map((item) => {
            const isTopPick = item.priority === 1;
            const isFeatured = item.priority === 2;

            return (
              <GlassCard
                key={item.id}
                className="overflow-hidden flex flex-col justify-between group hover:border-white/20 transition-all"
              >
                <div>
                  {/* Thumbnail / Header */}
                  <div className="relative h-40 bg-black/40 overflow-hidden">
                    {item.imageUrl ? (
                      <img
                        src={item.imageUrl}
                        alt={item.title}
                        className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105"
                      />
                    ) : (
                      <div className="w-full h-full flex flex-col items-center justify-center text-slate-600 bg-[#0E131F]">
                        <Sparkles className="w-8 h-8 mb-1" />
                        <span className="text-[11px]">No Thumbnail</span>
                      </div>
                    )}

                    {/* Priority Badge */}
                    <div className="absolute top-3 left-3">
                      <span className={`px-2 py-0.5 rounded-md text-[10px] font-bold backdrop-blur-md border shadow flex items-center gap-1 ${
                        isTopPick
                          ? 'bg-amber-500/80 text-white border-amber-400/40'
                          : isFeatured
                          ? 'bg-cyan-500/80 text-white border-cyan-400/40'
                          : 'bg-black/60 text-slate-300 border-white/10'
                      }`}>
                        {isTopPick ? <Star className="w-3 h-3 fill-current" /> : isFeatured ? <Zap className="w-3 h-3 fill-current" /> : null}
                        {isTopPick ? 'TOP PICK' : isFeatured ? 'FEATURED' : `PRIORITY ${item.priority}`}
                      </span>
                    </div>

                    {/* Active Status Switch */}
                    <div className="absolute top-3 right-3">
                      <button
                        type="button"
                        onClick={() => toggleMaterialActive(item.id, item.isActive)}
                        className={`px-2.5 py-1 rounded-full text-[10px] font-bold flex items-center gap-1.5 backdrop-blur-md transition-all ${
                          item.isActive
                            ? 'bg-emerald-500/80 text-white border border-emerald-400/30'
                            : 'bg-rose-500/80 text-white border border-rose-400/30'
                        }`}
                        title="Click to toggle live status"
                      >
                        {item.isActive ? <Eye className="w-3 h-3" /> : <EyeOff className="w-3 h-3" />}
                        <span>{item.isActive ? 'LIVE' : 'HIDDEN'}</span>
                      </button>
                    </div>

                    {/* Category Label */}
                    <div className="absolute bottom-3 left-3">
                      <span className="px-2 py-0.5 rounded text-[10px] font-semibold bg-black/70 text-brand-orange border border-white/10 backdrop-blur-md">
                        {item.category}
                      </span>
                    </div>
                  </div>

                  {/* Body Info */}
                  <div className="p-5 space-y-2">
                    <h3 className="text-sm font-bold text-white group-hover:text-brand-orange transition-colors line-clamp-1">
                      {item.title}
                    </h3>
                    <p className="text-xs text-slate-400 line-clamp-2 leading-relaxed">
                      {item.description}
                    </p>

                    {item.tags && item.tags.length > 0 && (
                      <div className="flex flex-wrap gap-1 pt-1">
                        {item.tags.map((t, idx) => (
                          <span
                            key={idx}
                            className="px-1.5 py-0.5 rounded text-[9px] font-medium bg-white/5 text-slate-300 border border-white/5"
                          >
                            #{t}
                          </span>
                        ))}
                      </div>
                    )}
                  </div>
                </div>

                {/* Footer Controls */}
                <div className="p-4 px-5 border-t border-white/5 flex items-center justify-between text-xs bg-black/20">
                  <span className="text-slate-400 text-[11px]">
                    By <strong className="text-slate-200">{item.creator}</strong>
                  </span>

                  <div className="flex items-center gap-1.5">
                    <a
                      href={item.redirectUrl}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-white/10 transition-colors"
                      title="Open external link"
                    >
                      <ExternalLink className="w-3.5 h-3.5" />
                    </a>
                    <button
                      type="button"
                      onClick={() => handleOpenEdit(item)}
                      className="p-1.5 rounded-lg text-slate-400 hover:text-brand-orange hover:bg-white/10 transition-colors"
                      title="Edit material"
                    >
                      <Edit2 className="w-3.5 h-3.5" />
                    </button>
                    <button
                      type="button"
                      onClick={() => setDeleteId(item.id)}
                      className="p-1.5 rounded-lg text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 transition-colors"
                      title="Delete material"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                    </button>
                  </div>
                </div>
              </GlassCard>
            );
          })}
        </div>
      )}

      {/* SlideOver Drawer Form (Create / Edit) */}
      <SlideOverDrawer
        isOpen={isDrawerOpen}
        onClose={() => setIsDrawerOpen(false)}
        title={editingId ? 'Edit Featured Material' : 'Upload Featured Material / Article'}
        subtitle="This material will sync immediately to the mobile app's Resources Hub."
        width="max-w-2xl"
      >
        <form onSubmit={handleSave} className="space-y-5">
          {/* Realtime Live Mobile Mockup Preview */}
          <LiveAppMockupPreview
            material={{
              ...formData,
              tags: tagsInput.split(',').map((t) => t.trim().replace(/^#/, '')).filter(Boolean)
            }}
          />

          {/* Title */}
          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1">
              Title <span className="text-brand-orange">*</span>
            </label>
            <input
              type="text"
              required
              placeholder="e.g. Striver's SDE Sheet 2026 / LeetCode 75 Roadmap"
              value={formData.title}
              onChange={(e) => setFormData({ ...formData, title: e.target.value })}
              className="w-full px-3.5 py-2.5 rounded-xl text-xs text-white glass-input"
            />
          </div>

          {/* Description */}
          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1">
              Description / Summary <span className="text-brand-orange">*</span>
            </label>
            <textarea
              required
              rows={3}
              placeholder="Detailed summary highlighting the problems, roadmap topics, and concepts covered..."
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              className="w-full px-3.5 py-2.5 rounded-xl text-xs text-white glass-input resize-none"
            />
          </div>

          {/* Category & Creator Grid */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1">
                Category
              </label>
              <select
                value={formData.category}
                onChange={(e) => setFormData({ ...formData, category: e.target.value as MaterialCategory })}
                className="w-full px-3.5 py-2.5 rounded-xl text-xs text-white glass-input bg-[#0A0E17]"
              >
                {CATEGORIES.map((c) => (
                  <option key={c} value={c} className="bg-[#0A0E17]">
                    {c}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1">
                Creator / Author <span className="text-brand-orange">*</span>
              </label>
              <input
                type="text"
                required
                placeholder="e.g. Striver (takeUforward) / NeetCode"
                value={formData.creator}
                onChange={(e) => setFormData({ ...formData, creator: e.target.value })}
                className="w-full px-3.5 py-2.5 rounded-xl text-xs text-white glass-input"
              />
            </div>
          </div>

          {/* Redirect Link & Duration */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div className="sm:col-span-2">
              <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1">
                External Redirect URL <span className="text-brand-orange">*</span>
              </label>
              <input
                type="url"
                required
                placeholder="https://takeuforward.org/strivers-a2z-dsa-course/"
                value={formData.redirectUrl}
                onChange={(e) => setFormData({ ...formData, redirectUrl: e.target.value })}
                className="w-full px-3.5 py-2.5 rounded-xl text-xs text-white glass-input"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1">
                Est. Duration
              </label>
              <input
                type="text"
                placeholder="e.g. 45 Days / 10h"
                value={formData.readTimeOrDuration || ''}
                onChange={(e) => setFormData({ ...formData, readTimeOrDuration: e.target.value })}
                className="w-full px-3.5 py-2.5 rounded-xl text-xs text-white glass-input"
              />
            </div>
          </div>

          {/* Priority Rank Selector */}
          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1.5">
              Priority Ranking (Mobile Ordering)
            </label>
            <div className="grid grid-cols-3 gap-3">
              {[
                { rank: 1, label: '⭐ Top Pick (Rank 1)', desc: 'Pinned to top header' },
                { rank: 2, label: '⚡ Featured (Rank 2)', desc: 'High priority card' },
                { rank: 3, label: 'Standard (Rank 3)', desc: 'Regular list flow' }
              ].map((p) => (
                <button
                  key={p.rank}
                  type="button"
                  onClick={() => setFormData({ ...formData, priority: p.rank })}
                  className={`p-3 rounded-xl border text-left transition-all ${
                    formData.priority === p.rank
                      ? 'border-brand-orange bg-brand-orange/15 shadow-md shadow-orange-500/15'
                      : 'border-white/10 hover:border-white/20 bg-black/20'
                  }`}
                >
                  <p className="text-xs font-bold text-white">{p.label}</p>
                  <p className="text-[10px] text-slate-400 mt-0.5">{p.desc}</p>
                </button>
              ))}
            </div>
          </div>

          {/* Thumbnail Image Uploader */}
          <ImageUploader
            currentImageUrl={formData.imageUrl}
            folder="articles"
            onImageUploaded={(url) => setFormData({ ...formData, imageUrl: url })}
            label="Card Banner / Thumbnail"
          />

          {/* Tags */}
          <div>
            <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300 mb-1">
              Tags (Comma separated)
            </label>
            <input
              type="text"
              placeholder="dsa, striver, graphs, sde-sheet, interview"
              value={tagsInput}
              onChange={(e) => setTagsInput(e.target.value)}
              className="w-full px-3.5 py-2.5 rounded-xl text-xs text-white glass-input"
            />
          </div>

          {/* Submit Actions */}
          <div className="pt-4 border-t border-white/10 flex items-center justify-end gap-3">
            <button
              type="button"
              onClick={() => setIsDrawerOpen(false)}
              className="px-4 py-2 rounded-xl text-xs font-semibold text-slate-300 hover:bg-white/5 transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSaving}
              className={`px-5 py-2.5 rounded-xl text-xs font-bold shadow-lg transition-all flex items-center gap-2 ${
                saveSuccess
                  ? 'bg-emerald-500 text-white shadow-emerald-500/30'
                  : 'bg-gradient-to-r from-brand-orange to-[#FF8C33] text-white shadow-orange-500/25'
              }`}
            >
              {isSaving ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin" />
                  <span>Publishing...</span>
                </>
              ) : saveSuccess ? (
                <>
                  <CheckCircle2 className="w-4 h-4" />
                  <span>Published to Cloud!</span>
                </>
              ) : (
                <>
                  <Sparkles className="w-4 h-4" />
                  <span>{editingId ? 'Update Material' : 'Publish to Cloud'}</span>
                </>
              )}
            </button>
          </div>
        </form>
      </SlideOverDrawer>

      {/* Delete Confirmation Modal */}
      <DeleteConfirmModal
        isOpen={Boolean(deleteId)}
        onClose={() => setDeleteId(null)}
        onConfirm={handleDeleteConfirm}
        title="Delete Featured Material"
        message="Are you sure you want to delete this featured material? It will be removed immediately from the mobile app's Resources Hub."
        isLoading={isDeleting}
      />
    </div>
  );
};
