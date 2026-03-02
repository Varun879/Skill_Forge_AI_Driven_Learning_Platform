import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Search, Filter, ChevronRight, Star, Clock, Zap } from 'lucide-react';
import api from '../services/api';
import { motion } from 'motion/react';

const cn = (...classes: any[]) => classes.filter(Boolean).join(' ');

const DifficultyBadge = ({ level }: { level: string }) => {
  const colors: any = {
    'Easy': 'bg-emerald-50 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400 border-emerald-100 dark:border-emerald-800',
    'Medium': 'bg-amber-50 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400 border-amber-100 dark:border-amber-800',
    'Hard': 'bg-rose-50 text-rose-700 dark:bg-rose-900/30 dark:text-rose-400 border-rose-100 dark:border-rose-800',
  };
  return (
    <span className={cn("text-[10px] font-bold px-2.5 py-1 rounded-lg border uppercase tracking-widest", colors[level] || 'bg-slate-100 text-slate-700')}>
      {level}
    </span>
  );
};

export const Practice = () => {
  const [problems, setProblems] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [filter, setFilter] = useState('All');
  const [favorites, setFavorites] = useState<number[]>(() => {
    const saved = localStorage.getItem('favorite_problems');
    return saved ? JSON.parse(saved) : [];
  });

  useEffect(() => {
    localStorage.setItem('favorite_problems', JSON.stringify(favorites));
  }, [favorites]);

  useEffect(() => {
    const fetchProblems = async () => {
      try {
        const res = await api.get('/problems');
        setProblems(res.data);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchProblems();
  }, []);

  const filteredProblems = problems.filter(p => {
    const matchesSearch = p.title.toLowerCase().includes(search.toLowerCase()) || 
                         p.topicTags.some((t: string) => t.toLowerCase().includes(search.toLowerCase()));
    const matchesFilter = filter === 'All' || p.difficulty === filter;
    return matchesSearch && matchesFilter;
  });

  const toggleFavorite = (id: number) => {
    setFavorites(prev => 
      prev.includes(id) ? prev.filter(fid => fid !== id) : [...prev, id]
    );
  };

  return (
    <div className="space-y-8 pb-12">
      <header className="flex flex-col md:flex-row md:items-end justify-between gap-6">
        <div>
          <h1 className="text-3xl font-bold tracking-tight mb-2 text-slate-900 dark:text-slate-50">Practice Problems</h1>
          <p className="text-slate-500 dark:text-slate-400 text-sm font-medium">Sharpen your skills with our curated set of algorithmic challenges.</p>
        </div>
        <div className="flex flex-col md:flex-row items-center gap-4 w-full md:w-auto">
          <div className="flex bg-white dark:bg-slate-800 p-1 rounded-xl border border-slate-200 dark:border-slate-700 shadow-sm">
            {['All', 'Easy', 'Medium', 'Hard'].map((level) => (
              <button
                key={level}
                onClick={() => setFilter(level)}
                className={cn(
                  "px-4 py-1.5 rounded-lg text-xs font-bold transition-all",
                  filter === level 
                    ? "bg-accent-600 text-white shadow-md shadow-accent-200 dark:shadow-none" 
                    : "text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200"
                )}
              >
                {level}
              </button>
            ))}
          </div>
          <div className="relative w-full md:w-64">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
            <input 
              type="text" 
              placeholder="Search problems..." 
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="pl-10 pr-4 py-2.5 bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl focus:ring-2 focus:ring-accent-500/20 outline-none w-full transition-all text-sm text-slate-900 dark:text-slate-100"
            />
          </div>
        </div>
      </header>

      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {[1,2,3,4,5,6].map(i => (
            <div key={i} className="h-64 bg-slate-100 dark:bg-slate-800 rounded-2xl animate-pulse" />
          ))}
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredProblems.map((problem, idx) => (
            <motion.div
              key={problem.id}
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: idx * 0.05 }}
              className="group bg-white dark:bg-slate-800 p-6 rounded-3xl border border-slate-200 dark:border-slate-700 shadow-sm dark:shadow-xl dark:shadow-black/20 hover:shadow-xl hover:border-accent-200 dark:hover:border-accent-800 transition-all flex flex-col"
            >
              <div className="flex justify-between items-start mb-6">
                <DifficultyBadge level={problem.difficulty} />
                <button 
                  onClick={() => toggleFavorite(problem.id)}
                  className={cn(
                    "transition-all duration-300 transform active:scale-125",
                    favorites.includes(problem.id)
                      ? "text-amber-400 scale-110"
                      : "text-slate-300 dark:text-slate-600 hover:text-amber-400/50"
                  )}
                >
                  <Star 
                    size={20} 
                    fill={favorites.includes(problem.id) ? "currentColor" : "none"} 
                  />
                </button>
              </div>
              
              <h3 className="text-lg font-bold mb-2 text-slate-900 dark:text-slate-50 group-hover:text-accent-600 dark:group-hover:text-accent-400 transition-colors leading-snug">
                {problem.title}
              </h3>
              
              <div className="flex flex-wrap gap-2 mb-6">
                {problem.topicTags.map((tag: string) => (
                  <span key={tag} className="text-[10px] font-bold uppercase tracking-widest text-slate-500 dark:text-slate-400 bg-slate-50 dark:bg-slate-900 px-2.5 py-1 rounded-lg border border-slate-100 dark:border-slate-800">
                    {tag}
                  </span>
                ))}
              </div>

              <div className="mt-auto pt-6 border-t border-slate-100 dark:border-slate-700 flex items-center justify-between">
                <div className="flex items-center gap-4 text-[10px] font-bold text-slate-500 dark:text-slate-400 uppercase tracking-widest">
                  <div className="flex items-center gap-1.5">
                    <Clock size={14} className="text-slate-400" />
                    {problem.estimated_time || '20m'}
                  </div>
                  <div className="flex items-center gap-1.5">
                    <Zap size={14} className="text-accent-500" />
                    +{problem.mastery_impact || 5}%
                  </div>
                </div>
                
                <Link 
                  to={`/practice/${problem.id}`}
                  className="w-10 h-10 bg-slate-50 dark:bg-slate-900 text-slate-900 dark:text-slate-100 rounded-full flex items-center justify-center group-hover:bg-accent-600 group-hover:text-white transition-all shadow-sm"
                >
                  <ChevronRight size={20} />
                </Link>
              </div>
            </motion.div>
          ))}
        </div>
      )}

      {!loading && filteredProblems.length === 0 && (
        <div className="text-center py-20">
          <div className="w-24 h-24 bg-slate-50 dark:bg-slate-800 rounded-full flex items-center justify-center mx-auto mb-6 text-slate-300 dark:text-slate-600">
            <Search size={40} />
          </div>
          <h3 className="text-xl font-bold mb-2 text-slate-900 dark:text-slate-50">No problems found</h3>
          <p className="text-slate-500 dark:text-slate-400 text-sm">Try adjusting your search or filters to find what you're looking for.</p>
        </div>
      )}
    </div>
  );
};
