import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { AdminLayout } from './components/layout/AdminLayout';
import { DashboardOverview } from './pages/DashboardOverview';
import { FeaturedMaterialsCMS } from './pages/FeaturedMaterialsCMS';
import { BroadcastsCMS } from './pages/BroadcastsCMS';
import { CustomContestsCMS } from './pages/CustomContestsCMS';
import { UsersDirectory } from './pages/UsersDirectory';
import { DeletionRequestsPage } from './pages/DeletionRequestsPage';
import { LoginPage } from './pages/LoginPage';
import { UnauthorizedPage } from './pages/UnauthorizedPage';
import { Loader2 } from 'lucide-react';

const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user, isAdmin, loading } = useAuth();

  if (loading) {
    return (
      <div className="min-h-screen bg-[#07090E] flex flex-col items-center justify-center text-brand-orange gap-3">
        <Loader2 className="w-10 h-10 animate-spin" />
        <p className="text-xs font-semibold text-slate-300">Validating Super Admin Privileges...</p>
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (!isAdmin) {
    return <UnauthorizedPage />;
  }

  return <>{children}</>;
};

export const App: React.FC = () => {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Public Login Route */}
          <Route path="/login" element={<LoginPage />} />

          {/* Protected Admin Console */}
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <AdminLayout />
              </ProtectedRoute>
            }
          >
            <Route index element={<DashboardOverview />} />
            <Route path="featured-materials" element={<FeaturedMaterialsCMS />} />
            <Route path="broadcasts" element={<BroadcastsCMS />} />
            <Route path="custom-contests" element={<CustomContestsCMS />} />
            <Route path="users" element={<UsersDirectory />} />
            <Route path="deletions" element={<DeletionRequestsPage />} />
          </Route>

          {/* Catch-all redirect */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
};

export default App;
