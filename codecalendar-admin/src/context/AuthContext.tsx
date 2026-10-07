import React, { createContext, useContext, useEffect, useState } from 'react';

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
    user && user.email.toLowerCase().trim() === CONFIGURED_ADMIN_EMAIL
  );

  useEffect(() => {
    try {
      const stored = localStorage.getItem(STORAGE_SESSION_KEY);
      if (stored) {
        const parsed = JSON.parse(stored);
        if (parsed && parsed.email?.toLowerCase().trim() === CONFIGURED_ADMIN_EMAIL) {
          setUser(parsed);
        } else {
          localStorage.removeItem(STORAGE_SESSION_KEY);
        }
      }
    } catch (e) {
      console.warn('Failed to parse admin session:', e);
      localStorage.removeItem(STORAGE_SESSION_KEY);
    } finally {
      setLoading(false);
    }
  }, []);

  const loginWithCredentials = async (email: string, pass: string): Promise<boolean> => {
    setLoading(true);
    setError(null);

    // Emulate small security delay for authentic feel & brute force mitigation
    await new Promise((res) => setTimeout(res, 400));

    const inputEmail = email.trim().toLowerCase();

    if (inputEmail === CONFIGURED_ADMIN_EMAIL && pass === CONFIGURED_ADMIN_PASS) {
      const adminSession: AdminUser = {
        ...VERIFIED_ADMIN_USER,
        email: inputEmail
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
    } else {
      setError('Invalid credentials. Only the authorized Super Admin email and password can access the portal.');
      setLoading(false);
      return false;
    }
  };

  const logout = async () => {
    try {
      localStorage.removeItem(STORAGE_SESSION_KEY);
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
