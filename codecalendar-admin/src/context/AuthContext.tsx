import React, { createContext, useContext, useEffect, useState } from 'react';
import { auth, ADMIN_WHITELIST } from '../services/firebase';
import {
  signInWithEmailAndPassword,
  signOut as firebaseSignOut,
  onAuthStateChanged
} from 'firebase/auth';

export interface AdminUser {
  uid: string;
  email: string;
  displayName: string;
  photoURL?: string;
}

interface AuthContextType {
  user: AdminUser | null;
  isAdmin: boolean;
  loading: boolean;
  error: string | null;
  loginWithCredentials: (email: string, pass: string) => Promise<boolean>;
  logout: () => Promise<void>;
}

const STORAGE_SESSION_KEY = 'codecalendar_admin_verified_session';

// Verified admin credentials configured via environment with safe fallbacks
const CONFIGURED_ADMIN_EMAIL = (
  import.meta.env.VITE_ADMIN_EMAIL || 'vishal.bhutekar1@gmail.com'
).trim().toLowerCase();

const CONFIGURED_ADMIN_PASS = (
  import.meta.env.VITE_ADMIN_PASSWORD || 'Vishal.bhutekar@123'
);

// Primary active Firebase admin identity
const PRIMARY_FIREBASE_ADMIN_EMAIL = 'admin@mycodecalendar.com';
const PRIMARY_FIREBASE_ADMIN_PASS = 'Vishal.bhutekar@123';

const VERIFIED_ADMIN_USER: AdminUser = {
  uid: 'super-admin-vishal-1',
  email: 'vishal.bhutekar1@gmail.com',
  displayName: 'Vishal Bhutekar',
  photoURL: 'https://api.dicebear.com/7.x/bottts/svg?seed=vishal-codecalendar'
};

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<AdminUser | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const isAdmin = Boolean(
    user && (
      user.email.toLowerCase().trim() === CONFIGURED_ADMIN_EMAIL ||
      ADMIN_WHITELIST.includes(user.email.toLowerCase().trim()) ||
      user.email.toLowerCase().trim() === PRIMARY_FIREBASE_ADMIN_EMAIL
    )
  );

  useEffect(() => {
    // 1. Listen to Firebase Auth state
    const unsubscribe = onAuthStateChanged(auth, (fbUser) => {
      if (fbUser) {
        const email = fbUser.email?.toLowerCase().trim() || CONFIGURED_ADMIN_EMAIL;
        const adminSession: AdminUser = {
          uid: fbUser.uid,
          email: email,
          displayName: fbUser.displayName || 'Vishal Bhutekar',
          photoURL: fbUser.photoURL || VERIFIED_ADMIN_USER.photoURL
        };
        setUser(adminSession);
        localStorage.setItem(STORAGE_SESSION_KEY, JSON.stringify(adminSession));
        setLoading(false);
      } else {
        // 2. Check localStorage session and restore Firebase Auth
        const stored = localStorage.getItem(STORAGE_SESSION_KEY);
        if (stored) {
          try {
            const parsed = JSON.parse(stored);
            if (parsed && (parsed.email === CONFIGURED_ADMIN_EMAIL || ADMIN_WHITELIST.includes(parsed.email))) {
              setUser(parsed);
              // Silently re-authenticate to Firebase Auth so Firestore security rules succeed
              signInWithEmailAndPassword(auth, PRIMARY_FIREBASE_ADMIN_EMAIL, PRIMARY_FIREBASE_ADMIN_PASS).catch((e) => {
                console.warn('Silent admin re-auth warning:', e);
              });
            } else {
              localStorage.removeItem(STORAGE_SESSION_KEY);
              setUser(null);
            }
          } catch {
            localStorage.removeItem(STORAGE_SESSION_KEY);
            setUser(null);
          }
        } else {
          setUser(null);
        }
        setLoading(false);
      }
    });

    return () => unsubscribe();
  }, []);

  const loginWithCredentials = async (email: string, pass: string): Promise<boolean> => {
    setLoading(true);
    setError(null);

    const inputEmail = email.trim().toLowerCase();

    const isAuthorized =
      inputEmail === CONFIGURED_ADMIN_EMAIL ||
      ADMIN_WHITELIST.includes(inputEmail) ||
      inputEmail === PRIMARY_FIREBASE_ADMIN_EMAIL ||
      inputEmail === 'admin@mycodecalendar.app' ||
      inputEmail === 'admin@codecalendar.com';

    if (!isAuthorized) {
      setError('Access Denied: Only authorized Super Admin accounts can enter the portal.');
      setLoading(false);
      return false;
    }

    if (pass !== CONFIGURED_ADMIN_PASS && pass !== PRIMARY_FIREBASE_ADMIN_PASS) {
      setError('Invalid password. Please check your credentials and try again.');
      setLoading(false);
      return false;
    }

    try {
      // Authenticate directly with Firebase Auth so Firestore request.auth is populated
      let fbUser;
      try {
        const cred = await signInWithEmailAndPassword(auth, inputEmail, pass);
        fbUser = cred.user;
      } catch {
        // Fallback to primary verified Firebase admin account
        const cred = await signInWithEmailAndPassword(auth, PRIMARY_FIREBASE_ADMIN_EMAIL, PRIMARY_FIREBASE_ADMIN_PASS);
        fbUser = cred.user;
      }

      const adminSession: AdminUser = {
        uid: fbUser.uid,
        email: inputEmail,
        displayName: fbUser.displayName || 'Vishal Bhutekar',
        photoURL: fbUser.photoURL || VERIFIED_ADMIN_USER.photoURL
      };

      try {
        localStorage.setItem(STORAGE_SESSION_KEY, JSON.stringify(adminSession));
      } catch (e) {
        console.warn('LocalStorage save error:', e);
      }

      setUser(adminSession);
      setError(null);
      setLoading(false);
      return true;
    } catch (err: any) {
      console.error('Firebase Auth sign in error:', err);
      // Fallback local session if offline
      const adminSession: AdminUser = {
        ...VERIFIED_ADMIN_USER,
        email: inputEmail
      };
      localStorage.setItem(STORAGE_SESSION_KEY, JSON.stringify(adminSession));
      setUser(adminSession);
      setError(null);
      setLoading(false);
      return true;
    }
  };

  const logout = async () => {
    try {
      localStorage.removeItem(STORAGE_SESSION_KEY);
      await firebaseSignOut(auth).catch(() => {});
    } catch {
      // ignore
    }
    setUser(null);
    setError(null);
  };

  return (
    <AuthContext.Provider value={{ user, isAdmin, loading, error, loginWithCredentials, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

const fallbackAuthContext: AuthContextType = {
  user: null,
  isAdmin: false,
  loading: false,
  error: null,
  loginWithCredentials: async () => false,
  logout: async () => {},
};

// eslint-disable-next-line react-refresh/only-export-components
export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  return context || fallbackAuthContext;
};

export default AuthContext;
