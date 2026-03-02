import React, { useState, useEffect } from 'react';
import { 
  LineChart, Line, BarChart, Bar, PieChart, Pie, Cell, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer 
} from 'recharts';
import { Target, TrendingUp, Brain, History, Filter, ChevronDown, Download, Loader2 } from 'lucide-react';
import api from '../services/api';
import { motion, AnimatePresence } from 'motion/react';

const COLORS = ['#4f46e5', '#06b6d4', '#10b981', '#f59e0b', '#ef4444'];

const TIME_RANGES = [
  { label: 'All Time', value: 'all' },
  { label: 'Last 7 Days', value: '7d' },
  { label: 'Last 30 Days', value: '30d' },
  { label: 'Last 6 Months', value: '6m' },
];

export const Analytics = () => {
  const [data, setData] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [exporting, setExporting] = useState(false);
  const [timeRange, setTimeRange] = useState(TIME_RANGES[0]);
  const [isFilterOpen, setIsFilterOpen] = useState(false);

  const fetchData = async (range: string) => {
    setLoading(true);
    try {
      const res = await api.get(`/user/metrics?range=${range}`);
      setData(res.data);
    } catch (err) {
      console.error(err);
      // Fallback to mock data if API fails
      setData({
        monthly: [
          { month: "Jan", problems: 10, accuracy: 65 },
          { month: "Feb", problems: 15, accuracy: 72 },
          { month: "Mar", problems: 20, accuracy: 75 },
          { month: "Apr", problems: 25, accuracy: 80 },
          { month: "May", problems: 30, accuracy: 85 },
        ],
        topicDistribution: [
          { name: 'Arrays', value: 40 },
          { name: 'DP', value: 15 },
          { name: 'Graphs', value: 25 },
          { name: 'Recursion', value: 20 },
        ],
        recentActivity: [
          { title: 'Two Sum', status: 'Accepted', time: '2 hours ago', score: 100 },
          { title: 'Maximum Subarray', status: 'Accepted', time: '5 hours ago', score: 100 },
          { title: 'Fibonacci Number', status: 'Wrong Answer', time: '1 day ago', score: 0 },
          { title: 'Two Sum', status: 'Accepted', time: '2 days ago', score: 100 },
        ]
      });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData(timeRange.value);
  }, [timeRange]);

  const handleExport = async () => {
    setExporting(true);
    try {
      const response = await api.get(`/user/export-report?range=${timeRange.value}`, {
        responseType: 'blob'
      });
      
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `skillforge-analytics-report-${new Date().toISOString().split('T')[0]}.csv`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (err) {
      console.error('Export failed:', err);
      alert('Failed to export report. Please try again.');
    } finally {
      setExporting(false);
    }
  };

  const topicData = [
    { name: 'Arrays', value: 40 },
    { name: 'DP', value: 15 },
    { name: 'Graphs', value: 25 },
    { name: 'Recursion', value: 20 },
  ];

  if (loading) return <div className="animate-pulse space-y-8">
    <div className="h-96 bg-slate-200 dark:bg-slate-800 rounded-3xl" />
    <div className="grid grid-cols-2 gap-8">
      <div className="h-64 bg-slate-200 dark:bg-slate-800 rounded-3xl" />
      <div className="h-64 bg-slate-200 dark:bg-slate-800 rounded-3xl" />
    </div>
  </div>;

  return (
    <div className="space-y-8">
      <header className="flex flex-col md:flex-row md:items-end justify-between gap-6">
        <div>
          <h1 className="text-3xl font-bold tracking-tight mb-2">Performance Analytics</h1>
          <p className="text-slate-500 dark:text-slate-400">A data-driven look at your coding journey.</p>
        </div>
        <div className="flex items-center gap-3">
          <div className="relative">
            <button 
              onClick={() => setIsFilterOpen(!isFilterOpen)}
              className="flex items-center gap-2 px-4 py-2 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl text-sm font-bold hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors"
            >
              <Filter size={16} className="text-indigo-600" />
              {timeRange.label}
              <ChevronDown size={16} className={`transition-transform ${isFilterOpen ? 'rotate-180' : ''}`} />
            </button>

            <AnimatePresence>
              {isFilterOpen && (
                <>
                  <div 
                    className="fixed inset-0 z-10" 
                    onClick={() => setIsFilterOpen(false)} 
                  />
                  <motion.div 
                    initial={{ opacity: 0, y: 10, scale: 0.95 }}
                    animate={{ opacity: 1, y: 0, scale: 1 }}
                    exit={{ opacity: 0, y: 10, scale: 0.95 }}
                    className="absolute right-0 mt-2 w-48 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl shadow-xl z-20 overflow-hidden"
                  >
                    <div className="p-2">
                      {TIME_RANGES.map((range) => (
                        <button
                          key={range.value}
                          onClick={() => {
                            setTimeRange(range);
                            setIsFilterOpen(false);
                          }}
                          className={`w-full text-left px-4 py-2.5 rounded-xl text-sm font-medium transition-colors ${
                            timeRange.value === range.value 
                              ? 'bg-indigo-50 dark:bg-indigo-900/20 text-indigo-600 dark:text-indigo-400' 
                              : 'hover:bg-slate-50 dark:hover:bg-slate-800 text-slate-600 dark:text-slate-400'
                          }`}
                        >
                          {range.label}
                        </button>
                      ))}
                    </div>
                  </motion.div>
                </>
              )}
            </AnimatePresence>
          </div>

          <button 
            onClick={handleExport}
            disabled={exporting}
            className="flex items-center gap-2 px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl text-sm font-bold shadow-lg shadow-indigo-200 dark:shadow-none transition-all disabled:opacity-50"
          >
            {exporting ? (
              <>
                <Loader2 size={16} className="animate-spin" />
                Exporting...
              </>
            ) : (
              <>
                <Download size={16} />
                Export Report
              </>
            )}
          </button>
        </div>
      </header>

      <div className="bg-white dark:bg-slate-900 p-8 rounded-3xl border border-slate-200 dark:border-slate-800 shadow-sm">
        <div className="flex items-center gap-2 mb-8">
          <TrendingUp className="text-indigo-600" size={20} />
          <h2 className="text-xl font-bold">Mastery Trend</h2>
        </div>
        <div className="h-[350px] w-full">
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={data?.chartData}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
              <XAxis dataKey="label" axisLine={false} tickLine={false} tick={{fill: '#64748b', fontSize: 12}} dy={10} />
              <YAxis axisLine={false} tickLine={false} tick={{fill: '#64748b', fontSize: 12}} />
              <Tooltip 
                contentStyle={{ backgroundColor: '#fff', borderRadius: '12px', border: 'none', boxShadow: '0 10px 15px -3px rgb(0 0 0 / 0.1)' }}
              />
              <Line type="monotone" dataKey="accuracy" stroke="#4f46e5" strokeWidth={4} dot={{ r: 6, fill: '#4f46e5', strokeWidth: 2, stroke: '#fff' }} activeDot={{ r: 8 }} />
              <Line type="monotone" dataKey="problems" stroke="#06b6d4" strokeWidth={4} dot={{ r: 6, fill: '#06b6d4', strokeWidth: 2, stroke: '#fff' }} activeDot={{ r: 8 }} />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <div className="bg-white dark:bg-slate-900 p-8 rounded-3xl border border-slate-200 dark:border-slate-800 shadow-sm">
          <div className="flex items-center gap-2 mb-8">
            <Brain className="text-indigo-600" size={20} />
            <h2 className="text-xl font-bold">Topic Distribution</h2>
          </div>
          <div className="h-[300px] w-full">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={data?.topicDistribution || []}
                  cx="50%"
                  cy="50%"
                  innerRadius={80}
                  outerRadius={100}
                  paddingAngle={5}
                  dataKey="value"
                >
                  {(data?.topicDistribution || []).map((entry: any, index: number) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </div>
          <div className="grid grid-cols-2 gap-4 mt-4">
            {(data?.topicDistribution || []).map((t: any, i: number) => (
              <div key={t.name} className="flex items-center gap-2">
                <div className="w-3 h-3 rounded-full" style={{ backgroundColor: COLORS[i] }} />
                <span className="text-sm font-medium">{t.name} ({t.value}%)</span>
              </div>
            ))}
          </div>
        </div>

        <div className="bg-white dark:bg-slate-900 p-8 rounded-3xl border border-slate-200 dark:border-slate-800 shadow-sm">
          <div className="flex items-center gap-2 mb-8">
            <History className="text-indigo-600" size={20} />
            <h2 className="text-xl font-bold">Recent Activity</h2>
          </div>
          <div className="space-y-6">
            {(data?.recentActivity || []).map((activity: any, i: number) => (
              <div key={i} className="flex items-center justify-between">
                <div className="flex items-center gap-4">
                  <div className={`w-10 h-10 rounded-xl flex items-center justify-center ${activity.status === 'Accepted' ? 'bg-emerald-50 text-emerald-600' : 'bg-rose-50 text-rose-600'}`}>
                    <Target size={20} />
                  </div>
                  <div>
                    <p className="font-bold text-sm">{activity.title}</p>
                    <p className="text-xs text-slate-500 dark:text-slate-400">
                      {new Date(activity.time).toLocaleDateString() === new Date().toLocaleDateString() 
                        ? 'Today' 
                        : new Date(activity.time).toLocaleDateString()}
                    </p>
                  </div>
                </div>
                <div className="text-right">
                  <p className={`text-sm font-bold ${activity.status === 'Accepted' ? 'text-emerald-600' : 'text-rose-600'}`}>{activity.status}</p>
                  <p className="text-xs text-slate-400">{activity.score} pts</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};
