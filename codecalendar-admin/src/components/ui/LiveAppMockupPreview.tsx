import React from 'react';
import { ExternalLink, Star, Zap, Bookmark, Sparkles } from 'lucide-react';
import type { FeaturedMaterial } from '../../types';

interface LiveAppMockupPreviewProps {
  material: Partial<FeaturedMaterial>;
}

export const LiveAppMockupPreview: React.FC<LiveAppMockupPreviewProps> = ({ material }) => {
  const priority = material.priority ?? 1;
  const isTopPick = priority === 1;
  const isFeatured = priority === 2;

  return (
    <div className="bg-[#0B0F19] rounded-2xl border border-white/10 p-4 shadow-2xl">
      <div className="flex items-center justify-between pb-3 mb-3 border-b border-white/10">
        <div className="flex items-center gap-2">
          <div className="w-2.5 h-2.5 rounded-full bg-brand-orange animate-pulse" />
          <span className="text-[11px] font-semibold text-slate-300 uppercase tracking-wider">
            Live Android App Preview
          </span>
        </div>
        <span className="text-[10px] px-2 py-0.5 rounded-full bg-white/10 text-slate-300 font-mono">
          Resources Screen
        </span>
      </div>

      {/* Android Card Mockup */}
      <div className="rounded-xl overflow-hidden bg-[#131926] border border-white/10 shadow-lg transition-all duration-300">
        {/* Card Thumbnail / Header */}
        <div className="relative h-36 w-full bg-[#080B11] flex items-center justify-center overflow-hidden">
          {material.imageUrl ? (
            <img
              src={material.imageUrl}
              alt="Thumbnail"
              className="w-full h-full object-cover"
            />
          ) : (
            <div className="flex flex-col items-center justify-center text-slate-500">
              <Sparkles className="w-8 h-8 mb-1 text-slate-600" />
              <span className="text-[11px] font-medium">Card Thumbnail Preview</span>
            </div>
          )}

          {/* Priority Pill */}
          <div className="absolute top-2.5 left-2.5 flex items-center gap-1 px-2 py-0.5 rounded-md text-[10px] font-bold backdrop-blur-md border border-white/15 shadow-sm text-white bg-black/50">
            {isTopPick ? (
              <>
                <Star className="w-3 h-3 text-amber-400 fill-amber-400" />
                <span className="text-amber-300">TOP PICK</span>
              </>
            ) : isFeatured ? (
              <>
                <Zap className="w-3 h-3 text-cyan-400 fill-cyan-400" />
                <span className="text-cyan-300">FEATURED</span>
              </>
            ) : (
              <span className="text-slate-300">STANDARD</span>
            )}
          </div>

          {/* Category Pill */}
          <div className="absolute top-2.5 right-2.5 px-2 py-0.5 rounded-md text-[10px] font-semibold text-white bg-brand-orange/80 backdrop-blur-md">
            {material.category || 'DSA & CP Sheets'}
          </div>
        </div>

        {/* Card Content */}
        <div className="p-3.5 space-y-2">
          <div className="flex items-start justify-between gap-2">
            <h4 className="text-sm font-bold text-white leading-snug line-clamp-1">
              {material.title || 'Untitled Article or Problem Sheet'}
            </h4>
            <Bookmark className="w-4 h-4 text-slate-400 flex-shrink-0" />
          </div>

          <p className="text-xs text-slate-400 line-clamp-2 leading-relaxed">
            {material.description || 'Provide a concise overview of the article, roadmap, or practice set...'}
          </p>

          {/* Tags */}
          {material.tags && material.tags.length > 0 && (
            <div className="flex flex-wrap gap-1 pt-1">
              {material.tags.slice(0, 3).map((tag, idx) => (
                <span
                  key={idx}
                  className="px-1.5 py-0.5 rounded text-[9px] font-medium bg-white/5 text-slate-300 border border-white/5"
                >
                  #{tag}
                </span>
              ))}
            </div>
          )}

          {/* Author & CTA Button */}
          <div className="pt-2 flex items-center justify-between border-t border-white/5 text-xs">
            <span className="text-[11px] text-slate-400">
              By <strong className="text-slate-200">{material.creator || 'Curator'}</strong>
            </span>
            <div className="flex items-center gap-1 text-brand-orange font-semibold text-[11px]">
              <span>Open Guide</span>
              <ExternalLink className="w-3 h-3" />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
