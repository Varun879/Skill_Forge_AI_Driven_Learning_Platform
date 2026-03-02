import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Logo } from '../components/Logo';
import { Mail, Lock, User as UserIcon, ArrowRight, Check, X, Chrome, Sun, Moon } from 'lucide-react';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import { useTheme } from '../context/ThemeContext';
import { motion } from 'motion/react';

export const Signup = () => {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    confirmPassword: '',
    role: 'Learner' as 'Learner' | 'Tutor',
    rememberMe: false
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const { isDark, toggleTheme } = useTheme();
  const navigate = useNavigate();

  useEffect(() => {
    const handleMessage = (event: MessageEvent) => {
      const origin = event.origin;
      if (!origin.endsWith('.run.app') && !origin.includes('localhost')) {
        return;
      }
      if (event.data?.type === 'OAUTH_AUTH_SUCCESS') {
        login(event.data.token, event.data.user, true);
        navigate('/');
      }
    };
    window.addEventListener('message', handleMessage);
    return () => window.removeEventListener('message', handleMessage);
  }, [login, navigate]);

  const handleGoogleSignup = async () => {
    try {
      const response = await api.get(`/auth/google/url?role=${formData.role}`);
      const { url } = response.data;
      window.open(url, 'google_oauth', 'width=600,height=700');
    } catch (err) {
      setError('Failed to initialize Google signup');
    }
  };

  const validatePassword = (pass: string) => {
    const checks = {
      length: pass.length >= 8,
      upper: /[A-Z]/.test(pass),
      number: /[0-9]/.test(pass),
      special: /[!@#$%^&*]/.test(pass)
    };
    return checks;
  };

  const passwordChecks = validatePassword(formData.password);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (formData.password !== formData.confirmPassword) {
      return setError('Passwords do not match');
    }
    if (!Object.values(passwordChecks).every(Boolean)) {
      return setError('Password does not meet requirements');
    }

    setLoading(true);
    setError('');
    try {
      const res = await api.post('/auth/signup', formData);
      login(res.data.token, res.data.user, formData.rememberMe);
      navigate('/');
    } catch (err: any) {
      setError(err.response?.data?.error || 'Failed to sign up');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="h-screen bg-slate-50 dark:bg-slate-950 flex items-center justify-center p-6 overflow-hidden relative">
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
        className="w-full max-w-xl bg-white dark:bg-slate-900 rounded-3xl border border-slate-200 dark:border-slate-800 shadow-2xl overflow-hidden"
      >
        <div className="p-8 md:p-10">
          <div className="flex items-center gap-3 mb-4">
            <Logo size={32} />
            <span className="text-xl font-extrabold tracking-tight text-slate-900 dark:text-slate-50">SkillForge</span>
          </div>

          <h1 className="text-2xl font-bold mb-1.5">Create an account</h1>
          <p className="text-sm text-slate-500 dark:text-slate-400 mb-6">Join thousands of developers mastering algorithms.</p>

          <form onSubmit={handleSubmit} className="space-y-4 md:space-y-5">
            {error && (
              <div className="p-4 bg-rose-50 dark:bg-rose-900/20 border border-rose-100 dark:border-rose-800 rounded-xl text-rose-600 dark:text-rose-400 text-sm font-medium">
                {error}
              </div>
            )}

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <label className="text-sm font-bold text-slate-700 dark:text-slate-300 ml-1">Full Name</label>
                <div className="relative">
                  <UserIcon className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
                  <input 
                    type="text" 
                    required
                    value={formData.name}
                    onChange={(e) => setFormData({...formData, name: e.target.value})}
                    placeholder="Enter your full name"
                    className="w-full pl-12 pr-4 py-3 bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl focus:ring-2 focus:ring-indigo-500 outline-none transition-all text-sm"
                  />
                </div>
              </div>

              <div className="space-y-2">
                <label className="text-sm font-bold text-slate-700 dark:text-slate-300 ml-1">Email Address</label>
                <div className="relative">
                  <Mail className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
                  <input 
                    type="email" 
                    required
                    value={formData.email}
                    onChange={(e) => setFormData({...formData, email: e.target.value})}
                    placeholder="name@example.com"
                    className="w-full pl-12 pr-4 py-3 bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl focus:ring-2 focus:ring-indigo-500 outline-none transition-all text-sm"
                  />
                </div>
              </div>
            </div>

            <div className="space-y-2">
              <label className="text-sm font-bold text-slate-700 dark:text-slate-300 ml-1">Select Role</label>
              <div className="grid grid-cols-2 gap-4">
                <button 
                  type="button"
                  onClick={() => setFormData({...formData, role: 'Learner'})}
                  className={`py-3 rounded-xl border font-bold text-sm transition-all ${formData.role === 'Learner' ? 'bg-accent-600 border-accent-600 text-white shadow-lg shadow-accent-100' : 'bg-slate-50 dark:bg-slate-800 border-slate-200 dark:border-slate-700 text-slate-600 hover:bg-slate-100'}`}
                >
                  Learner
                </button>
                <button 
                  type="button"
                  onClick={() => setFormData({...formData, role: 'Tutor'})}
                  className={`py-3 rounded-xl border font-bold text-sm transition-all ${formData.role === 'Tutor' ? 'bg-accent-600 border-accent-600 text-white shadow-lg shadow-accent-100' : 'bg-slate-50 dark:bg-slate-800 border-slate-200 dark:border-slate-700 text-slate-600 hover:bg-slate-100'}`}
                >
                  Tutor
                </button>
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <label className="text-sm font-bold text-slate-700 dark:text-slate-300 ml-1">Password</label>
                <div className="relative">
                  <Lock className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
                  <input 
                    type="password" 
                    required
                    value={formData.password}
                    onChange={(e) => setFormData({...formData, password: e.target.value})}
                    placeholder="••••••••"
                    className="w-full pl-12 pr-4 py-3 bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl focus:ring-2 focus:ring-indigo-500 outline-none transition-all text-sm"
                  />
                </div>
              </div>

              <div className="space-y-2">
                <label className="text-sm font-bold text-slate-700 dark:text-slate-300 ml-1">Confirm Password</label>
                <div className="relative">
                  <Lock className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
                  <input 
                    type="password" 
                    required
                    value={formData.confirmPassword}
                    onChange={(e) => setFormData({...formData, confirmPassword: e.target.value})}
                    placeholder="••••••••"
                    className="w-full pl-12 pr-4 py-3 bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl focus:ring-2 focus:ring-indigo-500 outline-none transition-all text-sm"
                  />
                </div>
              </div>
            </div>

            <div className="bg-slate-50 dark:bg-slate-800/50 p-3 rounded-2xl border border-slate-100 dark:border-slate-800 grid grid-cols-2 gap-y-1.5 gap-x-4">
              <div className={`flex items-center gap-2 text-xs font-bold ${passwordChecks.length ? 'text-emerald-600' : 'text-slate-400'}`}>
                {passwordChecks.length ? <Check size={14} /> : <X size={14} />}
                Min. 8 characters
              </div>
              <div className={`flex items-center gap-2 text-xs font-bold ${passwordChecks.upper ? 'text-emerald-600' : 'text-slate-400'}`}>
                {passwordChecks.upper ? <Check size={14} /> : <X size={14} />}
                1 Uppercase letter
              </div>
              <div className={`flex items-center gap-2 text-xs font-bold ${passwordChecks.number ? 'text-emerald-600' : 'text-slate-400'}`}>
                {passwordChecks.number ? <Check size={14} /> : <X size={14} />}
                1 Number
              </div>
              <div className={`flex items-center gap-2 text-xs font-bold ${passwordChecks.special ? 'text-emerald-600' : 'text-slate-400'}`}>
                {passwordChecks.special ? <Check size={14} /> : <X size={14} />}
                1 Special character
              </div>
            </div>

            <div className="flex items-center gap-1.5 ml-1">
              <input 
                type="checkbox" 
                id="rememberMe"
                checked={formData.rememberMe}
                onChange={(e) => setFormData({...formData, rememberMe: e.target.checked})}
                className="w-3.5 h-3.5 rounded border-slate-300 text-accent-600 focus:ring-accent-500"
              />
              <label htmlFor="rememberMe" className="text-xs font-medium text-slate-600 dark:text-slate-400 cursor-pointer">
                Remember me
              </label>
            </div>

            <button 
              type="submit" 
              disabled={loading}
              className="w-full py-3.5 bg-accent-600 hover:bg-accent-700 text-white font-bold rounded-xl shadow-lg shadow-accent-200 dark:shadow-none transition-all flex items-center justify-center gap-2 group disabled:opacity-50 text-sm"
            >
              {loading ? 'Creating account...' : 'Create Account'}
              {!loading && <ArrowRight size={18} className="group-hover:translate-x-1 transition-transform" />}
            </button>
          </form>

          <div className="relative my-4">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t border-slate-100 dark:border-slate-800"></div>
            </div>
            <div className="relative flex justify-center text-[10px] uppercase tracking-widest font-bold">
              <span className="bg-white dark:bg-slate-900 px-4 text-slate-400">Or continue with</span>
            </div>
          </div>

          <div className="grid grid-cols-1 gap-4">
            <button 
              onClick={handleGoogleSignup}
              className="flex items-center justify-center gap-2 py-3 border border-slate-200 dark:border-slate-800 rounded-xl hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors font-bold text-sm"
            >
              <Chrome size={18} />
              Continue with Google
            </button>
          </div>
        </div>

        <div className="p-5 bg-slate-50 dark:bg-slate-800/50 border-t border-slate-100 dark:border-slate-800 text-center">
          <p className="text-sm text-slate-500 dark:text-slate-400">
            Already have an account? <Link to="/login" className="font-bold text-accent-600 hover:underline">Sign in instead</Link>
          </p>
        </div>
      </motion.div>
    </div>
  );
};
