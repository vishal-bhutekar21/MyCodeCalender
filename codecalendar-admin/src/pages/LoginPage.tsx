import React, { useState } from 'react';
import { useNavigate, Navigate } from 'react-router-dom';
import { Code2, Shield, AlertCircle, Loader2, Mail, Lock, Eye, EyeOff, ShieldCheck } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { GlassCard } from '../components/ui/GlassCard';

export const LoginPage: React.FC = () => {
  const { user, isAdmin, loading, error, loginWithCredentials } = useAuth();
  const navigate = useNavigate();

  const [email, setEmail] = useState<string>('');
  const [password, setPassword] = useState<string>('');
  const [showPassword, setShowPassword] = useState<boolean>(false);
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);

  if (!loading && user && isAdmin) {
    return <Navigate to="/" replace />;
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email.trim() || !password) return;

    setIsSubmitting(true);
    const success = await loginWithCredentials(email, password);
    setIsSubmitting(false);

    if (success) {
      navigate('/');
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-4 bg-[#07090E] relative overflow-hidden select-none">
      {/* Ambient background glows */}
      <div className="absolute top-1/3 left-1/4 w-96 h-96 bg-brand-orange/10 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute bottom-1/3 right-1/4 w-96 h-96 bg-indigo-500/10 rounded-full blur-3xl pointer-events-none" />

      <GlassCard className="w-full max-w-md p-8 sm:p-10 space-y-7 relative z-10 border-white/15 shadow-2xl">
        {/* Brand Header */}
        <div className="flex flex-col items-center text-center">
          <div className="w-16 h-16 rounded-2xl bg-gradient-to-tr from-[#FF6B00] to-[#FFA048] p-0.5 shadow-xl shadow-orange-500/30 flex items-center justify-center mb-4">
            <div className="w-full h-full bg-[#0E121E] rounded-[14px] flex items-center justify-center">
              <Code2 className="w-8 h-8 text-brand-orange" />
            </div>
          </div>
          <h1 className="text-2xl font-black text-white tracking-tight flex items-center gap-2">
            <span>CodeCalendar</span>
            <span className="text-xs px-2 py-0.5 rounded bg-brand-orange/20 text-brand-orange border border-brand-orange/30 font-mono">
              ADMIN
            </span>
          </h1>
          <p className="text-xs text-slate-400 mt-1.5 font-medium">
            Super Administrator Control & CMS Portal
          </p>
        </div>

        {/* Security Warning / Error Box */}
        {error && (
          <div className="p-4 rounded-xl bg-rose-500/10 border border-rose-500/30 text-rose-400 text-xs flex items-start gap-3 animate-shake">
            <AlertCircle className="w-4 h-4 mt-0.5 flex-shrink-0" />
            <p className="leading-relaxed font-medium">{error}</p>
          </div>
        )}

        {/* Exclusive Verified Credentials Form */}
        <form onSubmit={handleSubmit} className="space-y-5">
          {/* Email Input */}
          <div className="space-y-2">
            <label className="block text-xs font-semibold text-slate-300">
              Super Admin Email
            </label>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                <Mail className="w-4 h-4" />
              </div>
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="vishal.bhutekar1@gmail.com"
                autoComplete="email"
                className="w-full pl-10 pr-4 py-3 bg-[#0B0F19] border border-white/10 rounded-xl text-white placeholder-slate-500 text-sm focus:outline-none focus:border-brand-orange/50 focus:ring-1 focus:ring-brand-orange/50 transition-all font-mono"
              />
            </div>
          </div>

          {/* Password Input */}
          <div className="space-y-2">
            <label className="block text-xs font-semibold text-slate-300">
              Admin Password
            </label>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
                <Lock className="w-4 h-4" />
              </div>
              <input
                type={showPassword ? 'text' : 'password'}
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••••••"
                autoComplete="current-password"
                className="w-full pl-10 pr-11 py-3 bg-[#0B0F19] border border-white/10 rounded-xl text-white placeholder-slate-500 text-sm focus:outline-none focus:border-brand-orange/50 focus:ring-1 focus:ring-brand-orange/50 transition-all font-mono"
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute inset-y-0 right-0 pr-3.5 flex items-center text-slate-400 hover:text-slate-200 transition-colors"
                tabIndex={-1}
              >
                {showPassword ? (
                  <EyeOff className="w-4 h-4" />
                ) : (
                  <Eye className="w-4 h-4" />
                )}
              </button>
            </div>
          </div>

          {/* Submit Button */}
          <button
            type="submit"
            disabled={isSubmitting || loading}
            className="w-full mt-2 py-3.5 px-4 rounded-xl bg-gradient-to-r from-[#FF6B00] to-[#FF8800] hover:from-[#FF781A] hover:to-[#FF941A] text-white font-bold text-sm shadow-xl shadow-orange-500/20 flex items-center justify-center gap-2.5 transition-all hover:scale-[1.01] active:scale-[0.99] disabled:opacity-60 disabled:cursor-not-allowed"
          >
            {isSubmitting ? (
              <>
                <Loader2 className="w-4 h-4 animate-spin text-white" />
                <span>Verifying Credentials...</span>
              </>
            ) : (
              <>
                <ShieldCheck className="w-4 h-4 text-white" />
                <span>Sign In to Super Admin</span>
              </>
            )}
          </button>
        </form>

        {/* Security Footer Notice */}
        <div className="pt-4 border-t border-white/5 flex items-center justify-center gap-2 text-slate-400 text-[11px]">
          <Shield className="w-3.5 h-3.5 text-brand-orange" />
          <span>Restricted to Verified Super Admin • Cloudflare Protected</span>
        </div>
      </GlassCard>
    </div>
  );
};

export default LoginPage;
