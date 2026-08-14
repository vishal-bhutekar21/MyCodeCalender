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
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const email = user?.email?.toLowerCase().trim() || '';
  const isAdmin = Boolean(email && ADMIN_WHITELIST.includes(email));

  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, (currentUser) => {
      setUser(currentUser);
      setLoading(false);
      if (currentUser && currentUser.email) {
        const userEmail = currentUser.email.toLowerCase().trim();
        if (!ADMIN_WHITELIST.includes(userEmail)) {
          setError(`Access Denied: ${userEmail} is not authorized for Admin CMS access.`);
        } else {
          setError(null);
        }
      }
    });

    return () => unsubscribe();
  }, []);

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
      setLoading(false);
      return true;
    } catch (err: any) {
      console.error('Google Sign-In Error:', err);
      setError(err.message || 'Failed to sign in with Google');
      setLoading(false);
      return false;
    }
  };

  const logout = async () => {
    try {
      await signOut(auth);
      setUser(null);
      setError(null);
    } catch (err: any) {
      console.error('Logout error:', err);
    }
  };

  return (
    <AuthContext.Provider value={{ user, isAdmin, loading, error, loginWithGoogle, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
