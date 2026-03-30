import React from 'react';
import { Link, useSearchParams } from 'react-router-dom';

export const OAuthError = () => {
  const [searchParams] = useSearchParams();
  const error = searchParams.get('error') || 'Google authentication failed';

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-50 dark:bg-slate-950 px-6">
      <div className="w-full max-w-md p-6 rounded-2xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 text-center">
        <h1 className="text-xl font-bold text-slate-900 dark:text-slate-100 mb-2">Google Sign-in Failed</h1>
        <p className="text-sm text-rose-600 dark:text-rose-400 mb-6">{error}</p>
        <Link
          to="/login"
          className="inline-flex items-center justify-center px-4 py-2 rounded-lg bg-accent-600 hover:bg-accent-700 text-white text-sm font-semibold"
        >
          Back to Login
        </Link>
      </div>
    </div>
  );
};
