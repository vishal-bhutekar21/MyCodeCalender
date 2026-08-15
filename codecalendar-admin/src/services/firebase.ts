import { initializeApp, getApps, getApp } from 'firebase/app';
import { getAuth, GoogleAuthProvider, setPersistence, browserLocalPersistence } from 'firebase/auth';
import { getFirestore, enableIndexedDbPersistence } from 'firebase/firestore';
import { getStorage } from 'firebase/storage';

const firebaseConfig = {
  apiKey: import.meta.env.VITE_FIREBASE_API_KEY || "AIzaSyAj1PnhtZM1hq5zhnS8ujwGBZ3MT_6QFPg",
  authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN || "shetkari-mitra-7721.firebaseapp.com",
  projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID || "shetkari-mitra-7721",
  storageBucket: import.meta.env.VITE_FIREBASE_STORAGE_BUCKET || "shetkari-mitra-7721.appspot.com",
  messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID || "333822226193",
  appId: import.meta.env.VITE_FIREBASE_APP_ID || "1:333822226193:web:3e6104ced5d469ac4aa6b0"
};

const app = getApps().length > 0 ? getApp() : initializeApp(firebaseConfig);

export const auth = getAuth(app);
// Ensure persistent local session for admin
setPersistence(auth, browserLocalPersistence).catch(() => {});

export const googleAuthProvider = new GoogleAuthProvider();
googleAuthProvider.setCustomParameters({ prompt: 'select_account' });

export const firestore = getFirestore(app);

// Enable offline caching if available in browser
if (typeof window !== 'undefined') {
  enableIndexedDbPersistence(firestore).catch(() => {
    // Multi-tab or private mode fallback
  });
}

export const storage = getStorage(app);

export const ADMIN_WHITELIST: string[] = (
  import.meta.env.VITE_ADMIN_WHITELIST || "vishal.bhutekar1@gmail.com,vishalbhutekar33772@gmail.com,admin@mycodecalendar.app,admin@codecalendar.com"
)
  .split(',')
  .map((e: string) => e.trim().toLowerCase());

export default app;
