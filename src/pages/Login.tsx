import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Logo } from '../components/Logo';
import { Mail, Lock, ArrowRight, Chrome, Sun, Moon } from 'lucide-react';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import { useTheme } from '../context/ThemeContext';
import { motion } from 'motion/react';
import { signInWithPopup } from 'firebase/auth';
import { firebaseAuth, googleProvider } from '../services/firebase';

const normalizeRole = (role: string): 'Learner' | 'Tutor' => {
  return String(role || '').toUpperCase() === 'TUTOR' ? 'Tutor' : 'Learner';
};

const getApiOrigin = () => {
  const baseUrl = (import.meta as any).env?.VITE_API_BASE_URL || 'http://localhost:8080/api';
  try {
    return new URL(baseUrl).origin;
  } catch {
    return 'http://localhost:8080';
  }
};

const resolveAvatarUrl = (avatarUrl?: string) => {
  if (!avatarUrl) return undefined;
  if (avatarUrl.startsWith('http://') || avatarUrl.startsWith('https://')) {
    return avatarUrl;
  }
  const normalizedPath = avatarUrl.startsWith('/') ? avatarUrl : `/${avatarUrl}`;
  return `${getApiOrigin()}${normalizedPath}`;
};

const mapAuthUser = (payload: any) => {
  const user = payload?.user || {};
  return {
    id: user.id || payload?.userId,
    name: payload?.name || `${user.firstName || ''} ${user.lastName || ''}`.trim() || user.username || 'User',
    email: user.email,
    role: normalizeRole(payload?.role || user.role),
    profile_image: resolveAvatarUrl(user.avatarUrl),
    mastery_percent: 0,
    accuracy: 0,
    avg_solve_time: '0s',
    streak: 0,
  };
};

const getErrorMessage = (err: any, fallback: string) => {
  return (
    err?.response?.data?.message
    || err?.response?.data?.error
    || err?.message
    || fallback
  );
};

export const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [otp, setOtp] = useState('');
  const [authMethod, setAuthMethod] = useState<'password' | 'otp'>('password');
  const [otpSent, setOtpSent] = useState(false);
  const [otpMessage, setOtpMessage] = useState('');
  const [rememberMe, setRememberMe] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [otpLoading, setOtpLoading] = useState(false);
  const { login } = useAuth();
  const { isDark, toggleTheme } = useTheme();
  const navigate = useNavigate();

  const handleGoogleLogin = async () => {
    setLoading(true);
    setError('');
    try {
      const credential = await signInWithPopup(firebaseAuth, googleProvider);
      const idToken = await credential.user.getIdToken();
      const res = await api.post('/auth/google', {}, {
        headers: { Authorization: `Bearer ${idToken}` },
      });
      const payload = res?.data?.data || res?.data;
      login(payload.accessToken, mapAuthUser(payload), rememberMe);
      navigate('/');
    } catch (err: any) {
      setError(getErrorMessage(err, 'Google sign-in failed'));
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setOtpMessage('');
    try {
      const res = await api.post('/auth/login', { email, password });
      const payload = res?.data?.data || res?.data;
      login(payload.accessToken, mapAuthUser(payload), rememberMe);
      navigate('/');
    } catch (err: any) {
      setError(getErrorMessage(err, 'Failed to login'));
    } finally {
      setLoading(false);
    }
  };

  const handleSendOtp = async () => {
    if (!email.trim()) {
      setError('Please enter your email address first');
      return;
    }

    setOtpLoading(true);
    setError('');
    setOtpMessage('');
    try {
      const res = await api.post('/auth/send-otp', { email });
      setOtpSent(true);
      setOtp('');
      setOtpMessage(res?.data?.message || `OTP sent to ${email}`);
    } catch (err: any) {
      setError(getErrorMessage(err, 'Failed to send OTP'));
    } finally {
      setOtpLoading(false);
    }
  };

  const handleVerifyOtp = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!otp.trim()) {
      setError('Please enter the OTP');
      return;
    }

    setOtpLoading(true);
    setError('');
    setOtpMessage('');
    try {
      const res = await api.post('/auth/verify-otp', {
        email,
        otp: otp.trim(),
      });
      const payload = res?.data?.data || res?.data;
      login(payload.accessToken, mapAuthUser(payload), rememberMe);
      navigate('/');
    } catch (err: any) {
      setError(getErrorMessage(err, 'Failed to verify OTP'));
    } finally {
      setOtpLoading(false);
    }
  };

  return (
    <div className="h-screen bg-slate-50 dark:bg-slate-950 flex items-center justify-center p-4 md:p-6 overflow-y-auto md:overflow-hidden relative">
      {/* Theme Toggle */}
      <div className="absolute top-4 right-4 md:top-8 md:right-8">
        <button
          onClick={toggleTheme}
          className="p-3 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 text-slate-600 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-800 transition-all shadow-sm hover:shadow-md"
          aria-label="Toggle Theme"
        >
          {isDark ? <Sun size={20} /> : <Moon size={20} />}
        </button>
      </div>

      <motion.div 
        initial={{ opacity: 0, scale: 0.95 }}
        animate={{ opacity: 1, scale: 1 }}
        className="w-full max-w-md bg-white dark:bg-slate-900 rounded-3xl border border-slate-200 dark:border-slate-800 shadow-xl overflow-hidden my-auto"
      >
        <div className="p-6 md:p-8">
          <div className="flex items-center gap-3 mb-4 md:mb-6">
            <Logo size={32} />
            <span className="text-xl font-extrabold tracking-tight text-slate-900 dark:text-slate-50">SkillForge</span>
          </div>

          <h1 className="text-2xl font-bold mb-1">Welcome back</h1>
          <p className="text-sm text-slate-500 dark:text-slate-400 mb-4 md:mb-6">Enter your credentials to access your account.</p>

          <div className="grid grid-cols-2 gap-2 mb-4">
            <button
              type="button"
              onClick={() => {
                setAuthMethod('password');
                setError('');
                setOtpMessage('');
              }}
              className={`py-2 rounded-xl border text-sm font-bold transition-all ${authMethod === 'password' ? 'bg-accent-600 border-accent-600 text-white' : 'bg-slate-50 dark:bg-slate-800 border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-300'}`}
            >
              Password
            </button>
            <button
              type="button"
              onClick={() => {
                setAuthMethod('otp');
                setError('');
                setOtpMessage('');
              }}
              className={`py-2 rounded-xl border text-sm font-bold transition-all ${authMethod === 'otp' ? 'bg-accent-600 border-accent-600 text-white' : 'bg-slate-50 dark:bg-slate-800 border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-300'}`}
            >
              Email OTP
            </button>
          </div>

          {authMethod === 'password' ? (
          <form onSubmit={handleSubmit} className="space-y-4">
            {error && (
              <div className="p-4 bg-rose-50 dark:bg-rose-900/20 border border-rose-100 dark:border-rose-800 rounded-xl text-rose-600 dark:text-rose-400 text-sm font-medium">
                {error}
              </div>
            )}

            <div className="space-y-2">
              <label className="text-sm font-bold text-slate-700 dark:text-slate-300 ml-1">Email Address</label>
              <div className="relative">
                <Mail className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
                <input 
                  type="email" 
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="name@example.com"
                  className="w-full pl-12 pr-4 py-3 bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl focus:ring-2 focus:ring-accent-500 outline-none transition-all"
                />
              </div>
            </div>

            <div className="space-y-2">
              <div className="flex justify-between items-center ml-1">
                <label className="text-sm font-bold text-slate-700 dark:text-slate-300">Password</label>
                <Link to="/forgot-password" title="Forgot password?" className="text-xs font-bold text-accent-600 hover:underline">Forgot password?</Link>
              </div>
              <div className="relative">
                <Lock className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
                <input 
                  type="password" 
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  className="w-full pl-12 pr-4 py-3 bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl focus:ring-2 focus:ring-accent-500 outline-none transition-all"
                />
              </div>
            </div>

            <div className="flex items-center gap-2 ml-1">
              <input 
                type="checkbox" 
                id="rememberMe"
                checked={rememberMe}
                onChange={(e) => setRememberMe(e.target.checked)}
                className="w-4 h-4 rounded border-slate-300 text-accent-600 focus:ring-accent-500"
              />
              <label htmlFor="rememberMe" className="text-sm font-medium text-slate-600 dark:text-slate-400 cursor-pointer">
                Remember me
              </label>
            </div>

            <button 
              type="submit" 
              disabled={loading}
              className="w-full py-4 bg-accent-600 hover:bg-accent-700 text-white font-bold rounded-xl shadow-lg shadow-accent-200 dark:shadow-none transition-all flex items-center justify-center gap-2 group disabled:opacity-50"
            >
              {loading ? 'Signing in...' : 'Sign In'}
              {!loading && <ArrowRight size={18} className="group-hover:translate-x-1 transition-transform" />}
            </button>
          </form>
          ) : (
          <form onSubmit={handleVerifyOtp} className="space-y-4">
            {error && (
              <div className="p-4 bg-rose-50 dark:bg-rose-900/20 border border-rose-100 dark:border-rose-800 rounded-xl text-rose-600 dark:text-rose-400 text-sm font-medium">
                {error}
              </div>
            )}

            {otpMessage && (
              <div className="p-4 bg-emerald-50 dark:bg-emerald-900/20 border border-emerald-100 dark:border-emerald-800 rounded-xl text-emerald-700 dark:text-emerald-300 text-sm font-medium">
                {otpMessage}
              </div>
            )}

            <div className="space-y-2">
              <label className="text-sm font-bold text-slate-700 dark:text-slate-300 ml-1">Email Address</label>
              <div className="relative">
                <Mail className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
                <input
                  type="email"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="name@example.com"
                  className="w-full pl-12 pr-4 py-3 bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl focus:ring-2 focus:ring-accent-500 outline-none transition-all"
                />
              </div>
            </div>

            {otpSent && (
              <div className="space-y-2">
                <label className="text-sm font-bold text-slate-700 dark:text-slate-300 ml-1">One-Time Password (OTP)</label>
                <input
                  type="text"
                  inputMode="numeric"
                  maxLength={6}
                  value={otp}
                  onChange={(e) => setOtp(e.target.value.replace(/\D/g, '').slice(0, 6))}
                  placeholder="Enter 6-digit OTP"
                  className="w-full px-4 py-3 bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl focus:ring-2 focus:ring-accent-500 outline-none transition-all tracking-[0.3em]"
                />
              </div>
            )}

            <div className="flex items-center gap-2 ml-1">
              <input
                type="checkbox"
                id="rememberMeOtp"
                checked={rememberMe}
                onChange={(e) => setRememberMe(e.target.checked)}
                className="w-4 h-4 rounded border-slate-300 text-accent-600 focus:ring-accent-500"
              />
              <label htmlFor="rememberMeOtp" className="text-sm font-medium text-slate-600 dark:text-slate-400 cursor-pointer">
                Remember me
              </label>
            </div>

            {!otpSent ? (
              <button
                type="button"
                onClick={handleSendOtp}
                disabled={otpLoading}
                className="w-full py-4 bg-accent-600 hover:bg-accent-700 text-white font-bold rounded-xl shadow-lg shadow-accent-200 dark:shadow-none transition-all flex items-center justify-center gap-2 group disabled:opacity-50"
              >
                {otpLoading ? 'Sending OTP...' : 'Send OTP'}
              </button>
            ) : (
              <>
                <button
                  type="submit"
                  disabled={otpLoading}
                  className="w-full py-4 bg-accent-600 hover:bg-accent-700 text-white font-bold rounded-xl shadow-lg shadow-accent-200 dark:shadow-none transition-all flex items-center justify-center gap-2 group disabled:opacity-50"
                >
                  {otpLoading ? 'Verifying OTP...' : 'Verify OTP'}
                  {!otpLoading && <ArrowRight size={18} className="group-hover:translate-x-1 transition-transform" />}
                </button>

                <button
                  type="button"
                  onClick={handleSendOtp}
                  disabled={otpLoading}
                  className="w-full py-3 border border-slate-200 dark:border-slate-800 rounded-xl hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors font-bold text-sm disabled:opacity-50"
                >
                  {otpLoading ? 'Resending...' : 'Resend OTP'}
                </button>
              </>
            )}
          </form>
          )}

          <div className="relative my-4 md:my-6">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t border-slate-100 dark:border-slate-800"></div>
            </div>
            <div className="relative flex justify-center text-xs uppercase tracking-widest font-bold">
              <span className="bg-white dark:bg-slate-900 px-4 text-slate-400">Or continue with</span>
            </div>
          </div>

          <div className="grid grid-cols-1 gap-4">
            <button 
              onClick={handleGoogleLogin}
              className="flex items-center justify-center gap-2 py-3 border border-slate-200 dark:border-slate-800 rounded-xl hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors font-bold text-sm"
            >
              <Chrome size={18} />
              Continue with Google
            </button>
          </div>
        </div>

        <div className="p-6 bg-slate-50 dark:bg-slate-800/50 border-t border-slate-100 dark:border-slate-800 text-center">
          <p className="text-sm text-slate-500 dark:text-slate-400">
            Don't have an account? <Link to="/signup" className="font-bold text-accent-600 hover:underline">Sign up for free</Link>
          </p>
        </div>
      </motion.div>
    </div>
  );
};
