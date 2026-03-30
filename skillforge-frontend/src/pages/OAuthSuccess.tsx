import React, { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';

const normalizeRole = (role: string): 'Learner' | 'Tutor' => {
  return String(role || '').toUpperCase() === 'TUTOR' ? 'Tutor' : 'Learner';
};

export const OAuthSuccess = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { login } = useAuth();
  const [message, setMessage] = useState('Completing Google sign-in...');

  useEffect(() => {
    const finalize = async () => {
      const error = searchParams.get('error');
      if (error) {
        navigate(`/oauth-error?error=${encodeURIComponent(error)}`, { replace: true });
        return;
      }

      const token = searchParams.get('token') || searchParams.get('accessToken');
      if (!token) {
        setMessage('Missing authentication token. Please try again.');
        setTimeout(() => navigate('/login', { replace: true }), 1500);
        return;
      }

      localStorage.setItem('token', token);
      sessionStorage.removeItem('token');

      try {
        const res = await api.get('/user/me', {
          headers: { Authorization: `Bearer ${token}` },
        });

        const profile = res?.data?.data || res?.data;
        const mappedUser = {
          id: profile.id,
          name: profile.fullName || `${profile.firstName || ''} ${profile.lastName || ''}`.trim() || 'User',
          email: profile.email,
          role: normalizeRole(profile.role),
          profile_image: profile.avatarUrl,
          mastery_percent: 0,
          accuracy: 0,
          avg_solve_time: '0s',
          streak: 0,
        };

        login(token, mappedUser, true);
        navigate('/', { replace: true });
      } catch (err) {
        localStorage.removeItem('token');
        setMessage('Unable to load profile after Google sign-in. Please try again.');
        setTimeout(() => navigate('/login', { replace: true }), 1500);
      }
    };

    finalize();
  }, [login, navigate, searchParams]);

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-50 dark:bg-slate-950 px-6">
      <div className="text-sm font-semibold text-slate-700 dark:text-slate-200">{message}</div>
    </div>
  );
};
