import React, { useState, useRef } from 'react';
import { UploadCloud, X, Loader2 } from 'lucide-react';
import { compressAndUploadImage } from '../../services/storageService';

interface ImageUploaderProps {
  currentImageUrl?: string;
  folder: 'articles' | 'broadcasts' | 'contests';
  onImageUploaded: (url: string) => void;
  label?: string;
  recommendedSize?: string;
}

export const ImageUploader: React.FC<ImageUploaderProps> = ({
  currentImageUrl,
  folder,
  onImageUploaded,
  label = 'Upload Thumbnail Image',
  recommendedSize = '1200 x 630px (Auto-compressed to < 150KB WebP)'
}) => {
  const [uploading, setUploading] = useState<boolean>(false);
  const [dragOver, setDragOver] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleFile = async (file: File) => {
    if (!file.type.startsWith('image/')) {
      setError('Please select a valid image file (JPEG, PNG, WebP).');
      return;
    }
    setError(null);
    setUploading(true);

    try {
      const downloadUrl = await compressAndUploadImage(file, folder);
      onImageUploaded(downloadUrl);
    } catch (err: any) {
      console.error('Upload failed:', err);
      setError(err.message || 'Image upload failed. Check connection.');
    } finally {
      setUploading(false);
    }
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setDragOver(false);
    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      handleFile(e.dataTransfer.files[0]);
    }
  };

  return (
    <div className="space-y-2">
      <label className="block text-xs font-semibold uppercase tracking-wider text-slate-300">
        {label}
      </label>

      {currentImageUrl ? (
        <div className="relative group rounded-xl overflow-hidden border border-white/10 bg-black/40">
          <img
            src={currentImageUrl}
            alt="Preview"
            className="w-full h-44 object-cover transition-transform duration-300 group-hover:scale-105"
          />
          <div className="absolute inset-0 bg-black/60 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center gap-3">
            <button
              type="button"
              onClick={() => fileInputRef.current?.click()}
              className="px-3 py-1.5 rounded-lg bg-white/20 hover:bg-white/30 text-white text-xs font-medium backdrop-blur-md transition-colors"
            >
              Replace Image
            </button>
            <button
              type="button"
              onClick={() => onImageUploaded('')}
              className="p-1.5 rounded-lg bg-rose-500/80 hover:bg-rose-500 text-white text-xs backdrop-blur-md transition-colors"
              title="Remove"
            >
              <X className="w-4 h-4" />
            </button>
          </div>
        </div>
      ) : (
        <div
          onDragOver={(e) => { e.preventDefault(); setDragOver(true); }}
          onDragLeave={() => setDragOver(false)}
          onDrop={handleDrop}
          onClick={() => fileInputRef.current?.click()}
          className={`border-2 border-dashed rounded-xl p-6 text-center cursor-pointer transition-all duration-200 ${
            dragOver
              ? 'border-brand-orange bg-brand-orange/10 scale-[0.99]'
              : 'border-white/15 hover:border-white/30 bg-canvas-card'
          }`}
        >
          {uploading ? (
            <div className="flex flex-col items-center justify-center py-4 text-brand-orange">
              <Loader2 className="w-8 h-8 animate-spin mb-2" />
              <p className="text-xs font-semibold">Compressing & Uploading...</p>
              <p className="text-[10px] text-slate-400 mt-1">Generating WebP thumbnail</p>
            </div>
          ) : (
            <div className="flex flex-col items-center justify-center py-2">
              <div className="p-3 rounded-full bg-white/5 border border-white/10 text-brand-orange mb-3">
                <UploadCloud className="w-6 h-6" />
              </div>
              <p className="text-xs font-medium text-slate-200">
                <span className="text-brand-orange font-semibold">Click to upload</span> or drag and drop
              </p>
              <p className="text-[11px] text-slate-400 mt-1">
                {recommendedSize}
              </p>
            </div>
          )}
        </div>
      )}

      <input
        ref={fileInputRef}
        type="file"
        accept="image/*"
        className="hidden"
        onChange={(e) => {
          if (e.target.files && e.target.files[0]) {
            handleFile(e.target.files[0]);
          }
        }}
      />

      {error && (
        <p className="text-xs text-rose-400 mt-1">{error}</p>
      )}
    </div>
  );
};
