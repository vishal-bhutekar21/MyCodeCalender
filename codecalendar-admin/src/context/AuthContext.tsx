import React, { createContext, useContext, useEffect, useState } from 'react';
import type { User } from 'firebase/auth';
import { signInWithPopup, signOut, onAuthStateChanged } from 'firebase/auth';
import { auth, googleAuthProvider, ADMIN_WHITELIST } from '../services/firebase';

interface AuthContextType {
  user: User | null;
  isAdmin: boolean;
  loading: boolean;
  error: string | null;
  loginWithGoogle: () => Promise<boolean>;
  loginAsDevAdmin: () => void;
  logout: () => Promise<void>;
}

const DEV_ADMIN_USER = {
  uid: 'dev-super-admin-01',
  email: 'vishalbhutekar33772@gmail.com',
  displayName: 'Vishal Bhutekar (Super Admin)',
  photoURL: 'https://api.dicebear.com/7.x/bottts/svg?seed=vishal',
  emailVerified: true,
} as unknown as User;

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const email = user?.email?.toLowerCase().trim() || '';
  const isAdmin = Boolean(email && ADMIN_WHITELIST.includes(email));

  useEffect(() => {
    // Check if dev admin session was previously stored
    const storedDevSession = localStorage.getItem('codecalendar_admin_dev_session');
    if (storedDevSession === 'true') {
      setUser(DEV_ADMIN_USER);
      setLoading(false);
      return;
    }

    const unsubscribe = onAuthStateChanged(auth, (currentUser) => {
      if (currentUser) {
        setUser(currentUser);
        setLoading(false);
        const userEmail = currentUser.email?.toLowerCase().trim() || '';
        if (!ADMIN_WHITELIST.includes(userEmail)) {
          setError(`Access Denied: ${userEmail} is not authorized for Admin CMS access.`);
        } else {
          setError(null);
        }
      } else {
        // If not logged in via Firebase and no dev session
        const devSession = localStorage.getItem('codecalendar_admin_dev_session');
        if (devSession === 'true') {
          setUser(DEV_ADMIN_USER);
        } else {
          setUser(null);
        }
        setLoading(false);
      }
    });

    return () => unsubscribe();
  }, []);

  const loginAsDevAdmin = () => {
    localStorage.setItem('codecalendar_admin_dev_session', 'true');
    setUser(DEV_ADMIN_USER);
    setError(null);
  };

  const loginWithGoogle = async (): Promise<boolean> => {
    try {
      setLoading(true);
      setError(null);
      const result = await signInWithPopup(auth, googleAuthProvider);
      const userEmail = result.user.email?.toLowerCase().trim() || '';

      if (!ADMIN_WHITELIST.includes(userEmail)) {
        setError(`Access Denied: ${userEmail} is not on the Super Admin Whitelist.`);
        await signOut(auth);
        setUser(null);
        setLoading(false);
        return false;
      }

      setUser(result.user);
      localStorage.removeItem('codecalendar_admin_dev_session');
      setLoading(false);
      return true;
    } catch (err: any) {
      console.error('Google Sign-In Error:', err);
      if (err.code === 'auth/unauthorized-domain') {
        setError(
          `Domain not authorized in Firebase Console (${window.location.hostname}). You can use 'Instant Admin Access' below to enter the portal.`
        );
      } else {
        setError(err.message || 'Failed to sign in with Google');
      }
      setLoading(false);
      return false;
    }
  };

  const logout = async () => {
    try {
      localStorage.removeItem('codecalendar_admin_dev_session');
      await signOut(auth).catch(() => {});
      setUser(null);
      setError(null);
    } catch (err: any) {
      console.error('Logout error:', err);
    }
  };

  return (
    <AuthContext.Provider value={{ user, isAdmin, loading, error, loginWithGoogle, loginAsDevAdmin, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

const fallbackAuthContext: AuthContextType = {
  user: null,
  isAdmin: false,
  loading: false,
  error: null,
  loginWithGoogle: async () => false,
  loginAsDevAdmin: () => {},
  logout: async () => {},
};

// eslint-disable-next-line react-refresh/only-export-components
export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  return context || fallbackAuthContext;
};
