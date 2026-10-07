import { initializeApp, getApps, getApp } from 'firebase/app';
import { getAuth, GoogleAuthProvider, setPersistence, browserLocalPersistence } from 'firebase/auth';
import { getFirestore, enableIndexedDbPersistence } from 'firebase/firestore';
import { getStorage } from 'firebase/storage';

const firebaseConfig = {
  apiKey: import.meta.env.VITE_FIREBASE_API_KEY || "",
  authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN || "mycodecalendar.firebaseapp.com",
  projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID || "mycodecalendar",
  storageBucket: import.meta.env.VITE_FIREBASE_STORAGE_BUCKET || "mycodecalendar.appspot.com",
  messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID || "",
  appId: import.meta.env.VITE_FIREBASE_APP_ID || ""
};

if (!import.meta.env.VITE_FIREBASE_PROJECT_ID) {
  console.warn('[Firebase] Running with default configuration. Set VITE_FIREBASE_* in .env for production.');
}

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
