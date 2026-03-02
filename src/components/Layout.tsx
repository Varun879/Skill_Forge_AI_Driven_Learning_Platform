import React, { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Logo } from './Logo';
import { 
  LayoutDashboard, 
  Code2, 
  Map, 
  GraduationCap, 
  BarChart3, 
  User, 
  LogOut, 
  Sun, 
  Moon,
  Menu,
  X,
  ChevronDown,
  BookOpen,
  Users,
  MessageSquare,
  CheckCircle
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useTheme } from '../context/ThemeContext';
import { motion, AnimatePresence } from 'motion/react';
import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';

function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

const NavItem = ({ icon: Icon, label, to, active }: { icon: any, label: string, to: string, active: boolean }) => (
  <Link
    to={to}
    className={cn(
      "flex items-center gap-2 px-4 py-2 rounded-xl transition-all duration-200 group relative",
      active 
        ? "text-accent-600 dark:text-accent-400" 
        : "text-slate-600 hover:text-accent-600 dark:text-slate-400 dark:hover:text-accent-400"
    )}
  >
    <Icon size={18} className={cn("transition-transform duration-200 group-hover:scale-110", active ? "text-accent-600 dark:text-accent-400" : "text-slate-500 dark:text-slate-400")} />
    <span className="font-semibold text-sm">{label}</span>
    {active && (
      <motion.div 
        layoutId="nav-underline"
        className="absolute -bottom-[21px] left-0 right-0 h-1 bg-accent-600 dark:bg-accent-400 rounded-t-full"
      />
    )}
  </Link>
);

export const Layout: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user, logout } = useAuth();
  const { isDark, toggleTheme } = useTheme();
  const location = useLocation();
  const navigate = useNavigate();
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [isProfileOpen, setIsProfileOpen] = useState(false);

  const learnerNavItems = [
    { icon: LayoutDashboard, label: 'Dashboard', to: '/' },
    { icon: BookOpen, label: 'Explore', to: '/explore' },
    { icon: Code2, label: 'Practice', to: '/practice' },
    { icon: Map, label: 'Roadmap', to: '/roadmap' },
    { icon: GraduationCap, label: 'Exam', to: '/exam' },
    { icon: BarChart3, label: 'Analytics', to: '/analytics' },
  ];

  const tutorNavItems = [
    { icon: LayoutDashboard, label: 'Dashboard', to: '/tutor' },
    { icon: BookOpen, label: 'Courses', to: '/tutor/courses' },
    { icon: CheckCircle, label: 'Submissions', to: '/tutor/submissions' },
    { icon: MessageSquare, label: 'Doubts', to: '/tutor/doubts' },
    { icon: BarChart3, label: 'Analytics', to: '/tutor/analytics' },
  ];

  const navItems = user?.role === 'Tutor' ? tutorNavItems : learnerNavItems;

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-900 text-slate-900 dark:text-slate-100 transition-colors duration-200">
      {/* Top Navigation Bar */}
      <header className="sticky top-0 z-50 w-full bg-white/80 dark:bg-slate-800/80 backdrop-blur-md border-b border-slate-200 dark:border-slate-700">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            {/* Logo */}
            <Link to={user?.role === 'Tutor' ? "/tutor" : "/"} className="flex items-center gap-4 shrink-0">
              <Logo size={40} />
              <span className="text-2xl font-extrabold tracking-tight hidden sm:block text-slate-900 dark:text-slate-50">SkillForge</span>
            </Link>

            {/* Desktop Navigation */}
            <nav className="hidden lg:flex items-center gap-2">
              {navItems.map((item) => (
                <NavItem
                  key={item.to}
                  {...item}
                  active={item.to === '/' || item.to === '/tutor' 
                    ? location.pathname === item.to 
                    : location.pathname.startsWith(item.to)}
                />
              ))}
            </nav>

            {/* Right Side Actions */}
            <div className="flex items-center gap-2 sm:gap-4">
              <button
                onClick={toggleTheme}
                className="p-2 rounded-xl text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-700 transition-colors"
                aria-label="Toggle Theme"
              >
                {isDark ? <Sun size={20} /> : <Moon size={20} />}
              </button>

              <div className="relative">
                <button 
                  onClick={() => setIsProfileOpen(!isProfileOpen)}
                  className="flex items-center gap-2 p-1 pl-2 rounded-xl hover:bg-slate-100 dark:hover:bg-slate-700 transition-colors border border-transparent hover:border-slate-200 dark:hover:border-slate-600"
                >
                  <div className={cn(
                    "w-8 h-8 rounded-full flex items-center justify-center text-accent-600 dark:text-accent-400 font-bold text-sm overflow-hidden",
                    !user?.profile_image && "bg-accent-100 dark:bg-accent-900/30"
                  )}>
                    {user?.profile_image ? (
                      <img 
                        src={user.profile_image} 
                        alt={user.name} 
                        className="w-full h-full object-cover block border-none outline-none"
                        onError={(e) => {
                          (e.target as HTMLImageElement).src = `https://ui-avatars.com/api/?name=${encodeURIComponent(user.name)}&background=f4f7f5&color=6a8d73`;
                        }}
                      />
                    ) : (
                      user?.name.charAt(0)
                    )}
                  </div>
                  <span className="text-sm font-bold hidden md:block">{user?.name.split(' ')[0]}</span>
                  <ChevronDown size={16} className={cn("text-slate-400 transition-transform", isProfileOpen && "rotate-180")} />
                </button>

                <AnimatePresence>
                  {isProfileOpen && (
                    <>
                      <div className="fixed inset-0 z-10" onClick={() => setIsProfileOpen(false)} />
                      <motion.div
                        initial={{ opacity: 0, y: 10, scale: 0.95 }}
                        animate={{ opacity: 1, y: 0, scale: 1 }}
                        exit={{ opacity: 0, y: 10, scale: 0.95 }}
                        className="absolute right-0 mt-2 w-56 bg-white dark:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700 shadow-xl z-20 overflow-hidden"
                      >
                        <div className="p-4 border-b border-slate-100 dark:border-slate-700">
                          <p className="text-sm font-bold truncate text-slate-900 dark:text-slate-100">{user?.name}</p>
                          <p className="text-xs text-slate-500 dark:text-slate-400 truncate capitalize">{user?.role}</p>
                        </div>
                        <div className="p-2">
                          <Link 
                            to="/profile" 
                            onClick={() => setIsProfileOpen(false)}
                            className="flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium text-slate-600 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-700 transition-colors"
                          >
                            <User size={18} />
                            Profile Settings
                          </Link>
                          <button
                            onClick={() => {
                              logout();
                              navigate('/login');
                            }}
                            className="w-full flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium text-red-600 hover:bg-red-50 dark:hover:bg-red-900/20 transition-colors"
                          >
                            <LogOut size={18} />
                            Logout
                          </button>
                        </div>
                      </motion.div>
                    </>
                  )}
                </AnimatePresence>
              </div>

              {/* Mobile Menu Button */}
              <button 
                onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
                className="lg:hidden p-2 rounded-xl text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-700 transition-colors"
              >
                {isMobileMenuOpen ? <X size={24} /> : <Menu size={24} />}
              </button>
            </div>
          </div>
        </div>

        {/* Mobile Navigation Menu */}
        <AnimatePresence>
          {isMobileMenuOpen && (
            <motion.div
              initial={{ height: 0, opacity: 0 }}
              animate={{ height: 'auto', opacity: 1 }}
              exit={{ height: 0, opacity: 0 }}
              className="lg:hidden bg-white dark:bg-slate-800 border-t border-slate-100 dark:border-slate-700 overflow-hidden"
            >
              <div className="px-4 py-6 space-y-2">
                {navItems.map((item) => (
                  <Link
                    key={item.to}
                    to={item.to}
                    onClick={() => setIsMobileMenuOpen(false)}
                    className={cn(
                      "flex items-center gap-3 px-4 py-3 rounded-xl font-bold transition-all",
                      (item.to === '/' || item.to === '/tutor' 
                        ? location.pathname === item.to 
                        : location.pathname.startsWith(item.to))
                        ? "bg-accent-600 text-white shadow-lg shadow-accent-200 dark:shadow-none"
                        : "text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-700"
                    )}
                  >
                    <item.icon size={20} />
                    {item.label}
                  </Link>
                ))}
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 lg:py-12">
        <motion.div
          key={location.pathname}
          initial={{ opacity: 0, y: 8 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.2, ease: "easeOut" }}
        >
          {children}
        </motion.div>
      </main>

      {/* Footer */}
      <footer className="border-t border-slate-200 dark:border-slate-800 py-12 mt-auto bg-white dark:bg-slate-900">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col md:flex-row justify-between items-center gap-6">
          <div className="flex items-center gap-3">
            <Logo size={32} />
            <span className="text-lg font-extrabold tracking-tight text-slate-900 dark:text-slate-100">SkillForge</span>
          </div>
          <p className="text-sm text-slate-500 dark:text-slate-400">
            © 2026 SkillForge AI. All rights reserved.
          </p>
          <div className="flex gap-6 text-sm font-bold text-slate-500 dark:text-slate-400">
            <a href="#" className="hover:text-accent-600 transition-colors">Privacy</a>
            <a href="#" className="hover:text-accent-600 transition-colors">Terms</a>
            <a href="#" className="hover:text-accent-600 transition-colors">Support</a>
          </div>
        </div>
      </footer>
    </div>
  );
};
