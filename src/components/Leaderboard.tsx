import React, { useState, useMemo } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { Trophy, Globe, Users, TrendingUp, TrendingDown, Flame, Medal, ChevronDown, User } from 'lucide-react';

const cn = (...classes: any[]) => classes.filter(Boolean).join(' ');

type TimeFilter = 'Weekly' | 'Monthly' | 'All Time';
type TabType = 'Weekly' | 'Global' | 'Friends';

interface LeaderboardEntry {
  id: number;
  rank: number;
  name: string;
  avatar?: string;
  solved: number;
  accuracy: number;
  points: number;
  streak: number;
  trend: 'up' | 'down' | 'stable';
  isCurrentUser?: boolean;
}

const MOCK_DATA: Record<TabType, Record<TimeFilter, LeaderboardEntry[]>> = {
  Weekly: {
    Weekly: [
      { id: 1, rank: 1, name: 'Varun', solved: 42, accuracy: 98, points: 1250, streak: 12, trend: 'stable', isCurrentUser: true },
      { id: 2, rank: 2, name: 'Manvi', solved: 38, accuracy: 95, points: 1180, streak: 8, trend: 'up' },
      { id: 3, rank: 3, name: 'Yakshu', solved: 35, accuracy: 92, points: 1120, streak: 15, trend: 'down' },
      { id: 4, rank: 4, name: 'Nithin', solved: 32, accuracy: 88, points: 1050, streak: 5, trend: 'up' },
    ],
    Monthly: [
      { id: 2, rank: 1, name: 'Manvi', solved: 156, accuracy: 96, points: 4800, streak: 8, trend: 'up' },
      { id: 1, rank: 2, name: 'Varun', solved: 148, accuracy: 97, points: 4650, streak: 12, trend: 'down', isCurrentUser: true },
      { id: 3, rank: 3, name: 'Yakshu', solved: 142, accuracy: 91, points: 4400, streak: 15, trend: 'stable' },
      { id: 4, rank: 4, name: 'Nithin', solved: 130, accuracy: 85, points: 4000, streak: 5, trend: 'up' },
    ],
    'All Time': [
      { id: 3, rank: 1, name: 'Yakshu', solved: 842, accuracy: 94, points: 25400, streak: 15, trend: 'up' },
      { id: 1, rank: 2, name: 'Varun', solved: 798, accuracy: 98, points: 24200, streak: 12, trend: 'down', isCurrentUser: true },
      { id: 2, rank: 3, name: 'Manvi', solved: 756, accuracy: 95, points: 23100, streak: 8, trend: 'stable' },
      { id: 4, rank: 4, name: 'Nithin', solved: 700, accuracy: 88, points: 20000, streak: 5, trend: 'up' },
    ],
  },
  Global: {
    Weekly: [
      { id: 4, rank: 1, name: 'Nithin', solved: 85, accuracy: 99, points: 2800, streak: 45, trend: 'stable' },
      { id: 2, rank: 2, name: 'Manvi', solved: 38, accuracy: 95, points: 1180, streak: 8, trend: 'up' },
      { id: 3, rank: 3, name: 'Yakshu', solved: 35, accuracy: 92, points: 1120, streak: 15, trend: 'down' },
      { id: 1, rank: 4, name: 'Varun', solved: 25, accuracy: 82, points: 850, streak: 7, trend: 'up', isCurrentUser: true },
    ],
    Monthly: [
      { id: 4, rank: 1, name: 'Nithin', solved: 340, accuracy: 98, points: 11200, streak: 45, trend: 'stable' },
      { id: 2, rank: 2, name: 'Manvi', solved: 300, accuracy: 92, points: 10000, streak: 12, trend: 'stable' },
      { id: 3, rank: 3, name: 'Yakshu', solved: 280, accuracy: 90, points: 9500, streak: 15, trend: 'down' },
      { id: 1, rank: 4, name: 'Varun', solved: 110, accuracy: 84, points: 3200, streak: 7, trend: 'up', isCurrentUser: true },
    ],
    'All Time': [
      { id: 4, rank: 1, name: 'Nithin', solved: 2450, accuracy: 97, points: 84000, streak: 45, trend: 'stable' },
      { id: 2, rank: 2, name: 'Manvi', solved: 2000, accuracy: 94, points: 70000, streak: 20, trend: 'stable' },
      { id: 3, rank: 3, name: 'Yakshu', solved: 1800, accuracy: 92, points: 65000, streak: 15, trend: 'down' },
      { id: 1, rank: 4, name: 'Varun', solved: 450, accuracy: 88, points: 12500, streak: 7, trend: 'up', isCurrentUser: true },
    ],
  },
  Friends: {
    Weekly: [
      { id: 1, rank: 1, name: 'Varun', solved: 42, accuracy: 98, points: 1250, streak: 12, trend: 'stable', isCurrentUser: true },
      { id: 2, rank: 2, name: 'Manvi', solved: 38, accuracy: 95, points: 1180, streak: 8, trend: 'up' },
      { id: 3, rank: 3, name: 'Yakshu', solved: 35, accuracy: 92, points: 1120, streak: 15, trend: 'down' },
      { id: 4, rank: 4, name: 'Nithin', solved: 32, accuracy: 88, points: 1050, streak: 5, trend: 'up' },
    ],
    Monthly: [
      { id: 1, rank: 1, name: 'Varun', solved: 148, accuracy: 97, points: 4650, streak: 12, trend: 'down', isCurrentUser: true },
      { id: 2, rank: 2, name: 'Manvi', solved: 140, accuracy: 95, points: 4400, streak: 8, trend: 'up' },
      { id: 3, rank: 3, name: 'Yakshu', solved: 135, accuracy: 92, points: 4200, streak: 15, trend: 'stable' },
      { id: 4, rank: 4, name: 'Nithin', solved: 130, accuracy: 88, points: 4000, streak: 5, trend: 'up' },
    ],
    'All Time': [
      { id: 1, rank: 1, name: 'Varun', solved: 798, accuracy: 98, points: 24200, streak: 12, trend: 'down', isCurrentUser: true },
      { id: 2, rank: 2, name: 'Manvi', solved: 750, accuracy: 95, points: 23000, streak: 8, trend: 'stable' },
      { id: 3, rank: 3, name: 'Yakshu', solved: 700, accuracy: 92, points: 22000, streak: 15, trend: 'up' },
      { id: 4, rank: 4, name: 'Nithin', solved: 650, accuracy: 88, points: 21000, streak: 5, trend: 'up' },
    ],
  },
};

export const Leaderboard = () => {
  const [activeTab, setActiveTab] = useState<TabType>('Weekly');
  const [timeFilter, setTimeFilter] = useState<TimeFilter>('Weekly');

  const currentData = useMemo(() => {
    return MOCK_DATA[activeTab][timeFilter];
  }, [activeTab, timeFilter]);

  const currentUserData = useMemo(() => {
    return currentData.find(d => d.isCurrentUser);
  }, [currentData]);

  const nextRankData = useMemo(() => {
    if (!currentUserData) return null;
    const sorted = [...currentData].sort((a, b) => a.rank - b.rank);
    const currentIndex = sorted.findIndex(d => d.isCurrentUser);
    if (currentIndex > 0) {
      return sorted[currentIndex - 1];
    }
    return null;
  }, [currentData, currentUserData]);

  const tabs = [
    { id: 'Weekly', label: 'Weekly Ranking', icon: Trophy },
    { id: 'Global', label: 'Global Ranking', icon: Globe },
    { id: 'Friends', label: 'Friends Ranking', icon: Users },
  ];

  const getRankIcon = (rank: number) => {
    switch (rank) {
      case 1: return <Medal className="text-amber-400" size={20} />;
      case 2: return <Medal className="text-slate-400" size={20} />;
      case 3: return <Medal className="text-amber-700" size={20} />;
      default: return <span className="text-slate-400 font-bold text-sm">{rank}</span>;
    }
  };

  return (
    <div className="bg-white dark:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700 shadow-sm dark:shadow-xl dark:shadow-black/20 overflow-hidden">
      {/* Header */}
      <div className="p-8 border-b border-slate-100 dark:border-slate-700 flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-bold text-slate-900 dark:text-slate-50">Leaderboard</h2>
          <p className="text-sm text-slate-500 dark:text-slate-400 font-medium">Compete with top learners</p>
        </div>
        <div className="relative">
          <select 
            value={timeFilter}
            onChange={(e) => setTimeFilter(e.target.value as TimeFilter)}
            className="appearance-none bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl px-4 py-2 pr-10 text-sm font-bold text-slate-900 dark:text-slate-100 outline-none focus:ring-2 focus:ring-accent-500/20 cursor-pointer transition-all"
          >
            <option>Weekly</option>
            <option>Monthly</option>
            <option>All Time</option>
          </select>
          <ChevronDown size={16} className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none" />
        </div>
      </div>

      {/* Tabs */}
      <div className="px-8 pt-6">
        <div className="flex items-center gap-2 border-b border-slate-100 dark:border-slate-700">
          {tabs.map((tab) => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id as TabType)}
              className={cn(
                "flex items-center gap-2 px-4 py-3 text-sm font-bold transition-all relative",
                activeTab === tab.id 
                  ? "text-accent-600 dark:text-accent-400" 
                  : "text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200"
              )}
            >
              <tab.icon size={18} />
              {tab.label}
              {activeTab === tab.id && (
                <motion.div 
                  layoutId="activeTab"
                  className="absolute bottom-0 left-0 right-0 h-0.5 bg-accent-600 dark:bg-accent-400"
                />
              )}
            </button>
          ))}
        </div>
      </div>

      {/* Table / Mobile Cards */}
      <div className="p-8">
        {/* Desktop Table */}
        <div className="hidden md:block overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="text-left text-[10px] font-bold text-slate-400 uppercase tracking-widest border-b border-slate-100 dark:border-slate-700">
                <th className="pb-4 pl-2">Rank</th>
                <th className="pb-4">User</th>
                <th className="pb-4 text-center">Solved</th>
                <th className="pb-4 text-center">Accuracy</th>
                <th className="pb-4 text-right pr-2">Points</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-50 dark:divide-slate-700/50">
              <AnimatePresence mode="wait">
                {currentData.map((entry, idx) => (
                  <motion.tr 
                    key={`${activeTab}-${timeFilter}-${entry.id}`}
                    initial={{ opacity: 0, x: -10 }}
                    animate={{ opacity: 1, x: 0 }}
                    exit={{ opacity: 0, x: 10 }}
                    transition={{ delay: idx * 0.03 }}
                    className={cn(
                      "group transition-all hover:bg-slate-50 dark:hover:bg-slate-700/30 hover:-translate-y-0.5 hover:shadow-sm",
                      entry.isCurrentUser && "bg-accent-50/50 dark:bg-accent-900/10"
                    )}
                  >
                    <td className="py-4 pl-2">
                      <div className="flex items-center gap-3">
                        <div className="w-8 flex justify-center">
                          {getRankIcon(entry.rank)}
                        </div>
                        <div className={cn(
                          "text-xs",
                          entry.trend === 'up' ? "text-emerald-500" : 
                          entry.trend === 'down' ? "text-rose-500" : "text-slate-300"
                        )}>
                          {entry.trend === 'up' && <TrendingUp size={14} />}
                          {entry.trend === 'down' && <TrendingDown size={14} />}
                        </div>
                      </div>
                    </td>
                    <td className="py-4">
                      <div className="flex items-center gap-3">
                        <div className={cn(
                          "w-10 h-10 rounded-full flex items-center justify-center text-white font-bold text-sm overflow-hidden border border-slate-200 dark:border-slate-700 shadow-inner shrink-0",
                          entry.avatar ? "bg-slate-100 dark:bg-slate-900" : "bg-gradient-to-br from-accent-500 to-accent-700"
                        )}>
                          {entry.avatar ? (
                            <img src={entry.avatar} alt={entry.name} className="w-full h-full object-cover" />
                          ) : (
                            <span>{entry.name.charAt(0).toUpperCase()}</span>
                          )}
                        </div>
                        <div>
                          <div className="flex items-center gap-2">
                            <span className="font-bold text-sm text-slate-900 dark:text-slate-50">{entry.name}</span>
                            {entry.isCurrentUser && (
                              <span className="text-[9px] font-bold bg-accent-600 text-white px-1.5 py-0.5 rounded uppercase tracking-tighter">You</span>
                            )}
                            {entry.streak >= 7 && (
                              <div className="flex items-center gap-0.5 text-rose-500" title={`${entry.streak} day streak!`}>
                                <Flame size={14} fill="currentColor" />
                                <span className="text-[10px] font-bold">{entry.streak}</span>
                              </div>
                            )}
                          </div>
                          <p className="text-[10px] text-slate-500 dark:text-slate-400 font-medium">Level {Math.floor(entry.points / 1000) + 1} Learner</p>
                        </div>
                      </div>
                    </td>
                    <td className="py-4 text-center">
                      <span className="text-sm font-bold text-slate-700 dark:text-slate-300">{entry.solved}</span>
                    </td>
                    <td className="py-4 text-center">
                      <div className="flex flex-col items-center gap-1">
                        <span className="text-sm font-bold text-slate-700 dark:text-slate-300">{entry.accuracy}%</span>
                        <div className="w-12 h-1 bg-slate-100 dark:bg-slate-900 rounded-full overflow-hidden">
                          <div 
                            className="h-full bg-emerald-500" 
                            style={{ width: `${entry.accuracy}%` }}
                          />
                        </div>
                      </div>
                    </td>
                    <td className="py-4 text-right pr-2">
                      <span className="text-sm font-extrabold text-accent-600 dark:text-accent-400">{entry.points.toLocaleString()}</span>
                    </td>
                  </motion.tr>
                ))}
              </AnimatePresence>
            </tbody>
          </table>
        </div>

        {/* Mobile Stacked Cards */}
        <div className="md:hidden space-y-4">
          <AnimatePresence mode="wait">
            {currentData.map((entry, idx) => (
              <motion.div
                key={`${activeTab}-${timeFilter}-${entry.id}-mobile`}
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -10 }}
                transition={{ delay: idx * 0.03 }}
                className={cn(
                  "p-4 rounded-2xl border border-slate-100 dark:border-slate-700 flex items-center gap-4",
                  entry.isCurrentUser ? "bg-accent-50/50 dark:bg-accent-900/10 border-accent-200 dark:border-accent-800" : "bg-white dark:bg-slate-800/50"
                )}
              >
                <div className="flex flex-col items-center gap-1 min-w-[40px]">
                  <div className="text-lg font-black text-slate-900 dark:text-slate-50">
                    {entry.rank}
                  </div>
                  <div className={cn(
                    "text-[10px]",
                    entry.trend === 'up' ? "text-emerald-500" : 
                    entry.trend === 'down' ? "text-rose-500" : "text-slate-300"
                  )}>
                    {entry.trend === 'up' && <TrendingUp size={12} />}
                    {entry.trend === 'down' && <TrendingDown size={12} />}
                  </div>
                </div>

                <div className={cn(
                  "w-12 h-12 rounded-full flex items-center justify-center text-white font-bold text-base overflow-hidden shrink-0 border border-slate-200 dark:border-slate-700 shadow-inner",
                  entry.avatar ? "bg-slate-100 dark:bg-slate-900" : "bg-gradient-to-br from-accent-500 to-accent-700"
                )}>
                  {entry.avatar ? (
                    <img src={entry.avatar} alt={entry.name} className="w-full h-full object-cover" />
                  ) : (
                    <span>{entry.name.charAt(0).toUpperCase()}</span>
                  )}
                </div>

                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2 mb-1">
                    <span className="font-bold text-sm text-slate-900 dark:text-slate-50 truncate">{entry.name}</span>
                    {entry.isCurrentUser && (
                      <span className="text-[8px] font-bold bg-accent-600 text-white px-1 py-0.5 rounded uppercase">You</span>
                    )}
                  </div>
                  <div className="flex items-center gap-3 text-[10px] font-bold text-slate-500 dark:text-slate-400 uppercase tracking-tighter">
                    <span>{entry.solved} Solved</span>
                    <span>{entry.accuracy}% Acc</span>
                    {entry.streak >= 7 && (
                      <span className="text-rose-500 flex items-center gap-0.5">
                        <Flame size={10} fill="currentColor" />
                        {entry.streak}
                      </span>
                    )}
                  </div>
                </div>

                <div className="text-right">
                  <p className="text-xs font-bold text-slate-400 uppercase tracking-widest mb-0.5">Points</p>
                  <p className="text-sm font-black text-accent-600 dark:text-accent-400">{entry.points.toLocaleString()}</p>
                </div>
              </motion.div>
            ))}
          </AnimatePresence>
        </div>

        {/* Performance Summary */}
        {currentUserData && (
          <div className="mt-8 p-6 bg-slate-50 dark:bg-slate-900/50 rounded-2xl border border-slate-100 dark:border-slate-700 grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 bg-white dark:bg-slate-800 rounded-xl flex items-center justify-center text-accent-600 shadow-sm border border-slate-100 dark:border-slate-700">
                <Trophy size={24} />
              </div>
              <div>
                <p className="text-[10px] font-bold text-slate-500 dark:text-slate-400 uppercase tracking-widest">Your Rank</p>
                <p className="text-lg font-bold text-slate-900 dark:text-slate-50">#{currentUserData.rank}</p>
              </div>
            </div>
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 bg-white dark:bg-slate-800 rounded-xl flex items-center justify-center text-emerald-600 shadow-sm border border-slate-100 dark:border-slate-700">
                <TrendingUp size={24} />
              </div>
              <div>
                <p className="text-[10px] font-bold text-slate-500 dark:text-slate-400 uppercase tracking-widest">Percentile</p>
                <p className="text-lg font-bold text-slate-900 dark:text-slate-50">Top {Math.max(1, Math.floor(currentUserData.rank / 10))}%</p>
              </div>
            </div>
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 bg-white dark:bg-slate-800 rounded-xl flex items-center justify-center text-amber-600 shadow-sm border border-slate-100 dark:border-slate-700">
                <Medal size={24} />
              </div>
              <div>
                <p className="text-[10px] font-bold text-slate-500 dark:text-slate-400 uppercase tracking-widest">Next Rank</p>
                {nextRankData ? (
                  <p className="text-sm font-bold text-slate-900 dark:text-slate-50">
                    {nextRankData.points - currentUserData.points} pts behind #{nextRankData.rank}
                  </p>
                ) : (
                  <p className="text-sm font-bold text-emerald-600">You are #1!</p>
                )}
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
