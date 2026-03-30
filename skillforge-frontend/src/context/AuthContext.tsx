import React, { createContext, useContext, useState, useEffect } from 'react';
import api from '../services/api';

interface User {
  id: number;
  name: string;
  email: string;
  role: 'Learner' | 'Tutor';
  profile_image?: string;
  mastery_percent: number;
  accuracy: number;
  avg_solve_time: string;
  streak: number;
}

interface AuthContextType {
  user: User | null;
  loading: boolean;
  login: (token: string, user: User, rememberMe?: boolean) => void;
  logout: () => void;
  refreshUser: () => Promise<void>;
  updateUser: (partial: Partial<User>) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

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

const mapProfileToUser = (profile: any): User => ({
  id: profile.id,
  name: profile.fullName || `${profile.firstName || ''} ${profile.lastName || ''}`.trim() || profile.username || 'User',
  email: profile.email,
  role: normalizeRole(profile.role),
  profile_image: resolveAvatarUrl(profile.avatarUrl),
  mastery_percent: 0,
  accuracy: 0,
  avg_solve_time: '0s',
  streak: 0,
});

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  const refreshUser = async () => {
    try {
      const res = await api.get('/user/me');
      const profile = res?.data?.data || res?.data;
      setUser(mapProfileToUser(profile));
    } catch (err) {
      setUser(null);
      localStorage.removeItem('token');
      sessionStorage.removeItem('token');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    const token = localStorage.getItem('token') || sessionStorage.getItem('token');
    if (token) {
      refreshUser();
    } else {
      setLoading(false);
    }
  }, []);

  const login = (token: string, user: User, rememberMe: boolean = false) => {
    if (rememberMe) {
      localStorage.setItem('token', token);
      sessionStorage.removeItem('token');
    } else {
      sessionStorage.setItem('token', token);
      localStorage.removeItem('token');
    }
    setUser(user);
  };

  const logout = () => {
    localStorage.removeItem('token');
    sessionStorage.removeItem('token');
    setUser(null);
  };

  const updateUser = (partial: Partial<User>) => {
    setUser((prev) => {
      if (!prev) return prev;
      return { ...prev, ...partial };
    });
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, logout, refreshUser, updateUser }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
