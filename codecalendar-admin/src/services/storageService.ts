import { ref, uploadBytes, getDownloadURL } from 'firebase/storage';
import imageCompression from 'browser-image-compression';
import { storage } from './firebase';

export interface UploadProgressCallback {
  (progress: number): void;
}

export const compressAndUploadImage = async (
  file: File,
  folder: 'articles' | 'broadcasts' | 'contests' = 'articles'
): Promise<string> => {
  // 1. Client-Side Image Compression
  const options = {
    maxSizeMB: 0.2, // max ~200KB
    maxWidthOrHeight: 1200,
    useWebWorker: true,
    fileType: 'image/webp' as const
  };

  let compressedFile: File;
  try {
    const compressedBlob = await imageCompression(file, options);
    compressedFile = new File(
      [compressedBlob],
      `${file.name.replace(/\.[^/.]+$/, '')}.webp`,
      { type: 'image/webp' }
    );
  } catch (err) {
    console.warn('Compression failed, using original file:', err);
    compressedFile = file;
  }

  // 2. Storage Upload
  const cleanName = compressedFile.name.replace(/[^a-zA-Z0-9.-]/g, '_');
  const path = `cms_images/${folder}/${Date.now()}_${cleanName}`;
  const storageRef = ref(storage, path);

  const snapshot = await uploadBytes(storageRef, compressedFile, {
    contentType: compressedFile.type || 'image/webp',
    cacheControl: 'public, max-age=31536000'
  });

  // 3. Return persistent download URL
  return await getDownloadURL(snapshot.ref);
};
