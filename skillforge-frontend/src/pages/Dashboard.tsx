import React, { useState, useEffect } from 'react';
import { 
  LineChart, Line, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, AreaChart, Area, PieChart, Pie, Cell, Sector
} from 'recharts';
import { Trophy, Target, Clock, Zap, ArrowUpRight, CheckCircle2, AlertCircle, TrendingUp, Brain, History } from 'lucide-react';
import api from '../services/api';
import { motion } from 'motion/react';
import { Leaderboard } from '../components/Leaderboard';
import { useTheme } from '../context/ThemeContext';

const cn = (...classes: any[]) => classes.filter(Boolean).join(' ');

const StatCard = ({ title, value, icon: Icon, trend, color, delay }: any) => (
  <motion.div 
    initial={{ opacity: 0, y: 20 }}
    animate={{ opacity: 1, y: 0 }}
    transition={{ delay }}
    className="bg-white dark:bg-slate-800 p-6 rounded-2xl border border-slate-200 dark:border-slate-700 shadow-sm dark:shadow-xl dark:shadow-black/20"
  >
    <div className="flex justify-between items-start mb-4">
      <div className={cn("p-3 rounded-xl text-white", color)}>
        <Icon size={24} />
      </div>
      {trend && (
        <span className="flex items-center gap-1 text-accent-600 dark:text-accent-400 text-xs font-bold bg-accent-50 dark:bg-accent-900/20 px-2 py-1 rounded-lg">
          <ArrowUpRight size={14} />
          {trend}
        </span>
      )}
    </div>
    <p className="text-slate-500 dark:text-slate-400 text-xs font-bold uppercase tracking-widest mb-1">{title}</p>
    <h3 className="text-2xl font-bold tracking-tight text-slate-900 dark:text-slate-50">{value}</h3>
  </motion.div>
);

const CustomTooltip = ({ active, payload, isDark }: any) => {
  if (active && payload && payload.length) {
    const data = payload[0].payload;
    return (
      <div className={cn(
        "p-3 rounded-xl shadow-2xl border transition-all duration-300 animate-in fade-in zoom-in-95",
        isDark 
          ? "bg-white border-slate-200 text-slate-900" 
          : "bg-slate-900 border-slate-800 text-white"
      )}>
        <p className="text-sm font-bold mb-1">{data.name}</p>
        <div className="flex items-center gap-2">
          <div className="w-2 h-2 rounded-full" style={{ backgroundColor: data.color }} />
          <p className="text-xs font-bold">
            {data.value} <span className="opacity-60 font-medium">({data.value}%)</span>
          </p>
        </div>
      </div>
    );
  }
  return null;
};

const renderActiveShape = (props: any) => {
  const { cx, cy, innerRadius, outerRadius, startAngle, endAngle, fill } = props;

  return (
    <g>
      <Sector
        cx={cx}
        cy={cy}
        innerRadius={innerRadius}
        outerRadius={outerRadius + 6}
        startAngle={startAngle}
        endAngle={endAngle}
        fill={fill}
        style={{ filter: 'brightness(1.15)', transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)' }}
      />
    </g>
  );
};

export const Dashboard = () => {
  const { isDark } = useTheme();
  const [metrics, setMetrics] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [timeFilter, setTimeFilter] = useState('Last 6 Months');
  const [chartData, setChartData] = useState<any[]>([]);
  const [activeIndex, setActiveIndex] = useState(-1);

  const masteryDatasets: Record<string, any[]> = {
    'Last 7 Days': [
      { label: 'Mon', value: 62 },
      { label: 'Tue', value: 64 },
      { label: 'Wed', value: 63 },
      { label: 'Thu', value: 67 },
      { label: 'Fri', value: 69 },
      { label: 'Sat', value: 72 },
      { label: 'Sun', value: 75 },
    ],
    'Last 30 Days': Array.from({ length: 30 }, (_, i) => ({
      label: `Day ${i + 1}`,
      value: Math.floor(55 + (i * 0.8) + (Math.sin(i / 2) * 2))
    })),
    'Last 6 Months': [
      { label: 'Sep', value: 45 },
      { label: 'Oct', value: 52 },
      { label: 'Nov', value: 48 },
      { label: 'Dec', value: 61 },
      { label: 'Jan', value: 68 },
      { label: 'Feb', value: 75 },
    ],
    'Last Year': [
      { label: 'Mar', value: 32 },
      { label: 'Apr', value: 35 },
      { label: 'May', value: 38 },
      { label: 'Jun', value: 36 },
      { label: 'Jul', value: 42 },
      { label: 'Aug', value: 45 },
      { label: 'Sep', value: 48 },
      { label: 'Oct', value: 55 },
      { label: 'Nov', value: 52 },
      { label: 'Dec', value: 64 },
      { label: 'Jan', value: 69 },
      { label: 'Feb', value: 75 },
    ]
  };

  useEffect(() => {
    setChartData(masteryDatasets[timeFilter]);
  }, [timeFilter]);

  useEffect(() => {
    const fetchMetrics = async () => {
      try {
        const res = await api.get('/user/metrics');
        setMetrics(res.data);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchMetrics();
  }, []);

  const topicDistribution = [
    { name: 'Arrays', value: 40, color: '#8B5CF6' },
    { name: 'DP', value: 25, color: '#A78BFA' },
    { name: 'Graphs', value: 20, color: '#C4B5FD' },
    { name: 'Trees', value: 15, color: '#DDD6FE' },
  ];

  if (loading) return (
    <div className="animate-pulse space-y-8">
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {[1,2,3,4].map(i => <div key={i} className="h-32 bg-neutral-100 dark:bg-neutral-800 rounded-2xl" />)}
      </div>
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2 h-96 bg-neutral-100 dark:bg-neutral-800 rounded-2xl" />
        <div className="h-96 bg-neutral-100 dark:bg-neutral-800 rounded-2xl" />
      </div>
    </div>
  );

  return (
    <div className="space-y-8 pb-12">
      <header className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight mb-1 text-slate-900 dark:text-slate-50">Welcome back, {metrics?.name || 'Learner'}!</h1>
          <p className="text-slate-500 dark:text-slate-400 text-sm font-medium">Your learning engine is optimized. Here's your performance breakdown.</p>
        </div>
        <div className="flex items-center gap-3">
          <div className="px-4 py-2 bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl shadow-sm flex items-center gap-2 text-sm font-bold text-slate-900 dark:text-slate-100">
            <History size={16} className="text-accent-500" />
            Last Sync: Just now
          </div>
        </div>
      </header>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <StatCard 
          title="Mastery Level" 
          value={`${metrics?.masteryPercent}%`} 
          icon={Trophy} 
          trend="+5.2%" 
          color="bg-accent-600" 
          delay={0}
        />
        <StatCard 
          title="Problem Accuracy" 
          value={`${metrics?.accuracy}%`} 
          icon={Target} 
          trend="+1.8%" 
          color="bg-slate-800 dark:bg-slate-700" 
          delay={0.1}
        />
        <StatCard 
          title="Avg. Solve Time" 
          value={metrics?.avgSolveTime} 
          icon={Clock} 
          color="bg-accent-500" 
          delay={0.2}
        />
        <StatCard 
          title="Current Streak" 
          value={`${metrics?.currentStreakDays} Days`} 
          icon={Zap} 
          color="bg-rose-500" 
          delay={0.3}
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2 bg-white dark:bg-slate-800 p-8 rounded-2xl border border-slate-200 dark:border-slate-700 shadow-sm dark:shadow-xl dark:shadow-black/20">
          <div className="flex items-center justify-between mb-8">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-accent-50 dark:bg-accent-900/20 rounded-xl flex items-center justify-center text-accent-600">
                <TrendingUp size={20} />
              </div>
              <div>
                <h2 className="text-lg font-bold text-slate-900 dark:text-slate-50">Mastery Growth</h2>
                <p className="text-[10px] text-slate-500 dark:text-slate-400 uppercase tracking-widest font-bold">
                  Performance over {timeFilter.toLowerCase()}
                </p>
              </div>
            </div>
            <select 
              value={timeFilter}
              onChange={(e) => setTimeFilter(e.target.value)}
              className="bg-slate-50 dark:bg-slate-900 border-slate-200 dark:border-slate-700 rounded-lg text-xs font-bold px-3 py-1.5 text-slate-900 dark:text-slate-100 outline-none focus:ring-2 focus:ring-accent-500/20 cursor-pointer"
            >
              <option>Last 7 Days</option>
              <option>Last 30 Days</option>
              <option>Last 6 Months</option>
              <option>Last Year</option>
            </select>
          </div>
          <div className="h-[300px] w-full">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={chartData}>
                <defs>
                  <linearGradient id="colorMastery" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#8B5CF6" stopOpacity={0.15}/>
                    <stop offset="95%" stopColor="#8B5CF6" stopOpacity={0}/>
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#334155" opacity={0.5} />
                <XAxis 
                  dataKey="label" 
                  axisLine={false} 
                  tickLine={false} 
                  tick={{fill: '#94A3B8', fontSize: 11, fontWeight: 600}} 
                  dy={10}
                  interval={timeFilter === 'Last 30 Days' ? 6 : 0}
                />
                <YAxis 
                  axisLine={false} 
                  tickLine={false} 
                  tick={{fill: '#94A3B8', fontSize: 11, fontWeight: 600}} 
                  domain={[0, 100]}
                />
                <Tooltip 
                  contentStyle={{ backgroundColor: '#1E293B', borderRadius: '16px', border: '1px solid #334155', boxShadow: '0 10px 15px -3px rgb(0 0 0 / 0.3)', fontSize: '12px', color: '#F1F5F9' }}
                  itemStyle={{ color: '#F1F5F9' }}
                  formatter={(value: number) => [`${value}%`, 'Mastery']}
                />
                <Area 
                  type="monotone" 
                  dataKey="value" 
                  stroke="#8B5CF6" 
                  strokeWidth={3} 
                  fillOpacity={1} 
                  fill="url(#colorMastery)"
                  animationDuration={1000}
                  isAnimationActive={true}
                />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="bg-white dark:bg-slate-800 p-8 rounded-2xl border border-slate-200 dark:border-slate-700 shadow-sm dark:shadow-xl dark:shadow-black/20 flex flex-col">
          <div className="flex items-center gap-3 mb-8">
            <div className="w-10 h-10 bg-slate-50 dark:bg-slate-900 rounded-xl flex items-center justify-center text-slate-900 dark:text-slate-100">
              <Brain size={20} />
            </div>
            <div>
              <h2 className="text-lg font-bold text-slate-900 dark:text-slate-50">Topic Distribution</h2>
              <p className="text-[10px] text-slate-500 dark:text-slate-400 uppercase tracking-widest font-bold">Knowledge coverage</p>
            </div>
          </div>
          <div className="flex-1 h-[200px] w-full relative overflow-visible">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={topicDistribution}
                  cx="50%"
                  cy="50%"
                  innerRadius={60}
                  outerRadius={80}
                  paddingAngle={5}
                  dataKey="value"
                  stroke="none"
                  {...({
                    activeIndex,
                    activeShape: renderActiveShape,
                    onMouseEnter: (_: any, index: number) => setActiveIndex(index),
                    onMouseLeave: () => setActiveIndex(-1)
                  } as any)}
                >
                  {topicDistribution.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip 
                  content={<CustomTooltip isDark={isDark} />}
                  wrapperStyle={{ outline: 'none', zIndex: 1000 }}
                />
              </PieChart>
            </ResponsiveContainer>
            <div className="absolute inset-0 flex flex-col items-center justify-center pointer-events-none">
              <span className="text-2xl font-bold text-slate-900 dark:text-slate-50">8</span>
              <span className="text-[10px] font-bold text-slate-500 dark:text-slate-400 uppercase tracking-widest">Topics</span>
            </div>
          </div>
          <div className="grid grid-cols-2 gap-x-6 gap-y-3 mt-8">
            {topicDistribution.map((topic) => (
              <div key={topic.name} className="flex items-center gap-2 group cursor-default">
                <div className="w-2.5 h-2.5 rounded-full shrink-0 transition-transform group-hover:scale-125" style={{ backgroundColor: topic.color }} />
                <div className="flex flex-col">
                  <span className="text-xs font-bold text-slate-900 dark:text-slate-50 leading-tight">{topic.name}</span>
                  <div className="flex items-center gap-1.5">
                    <span className="text-[10px] font-bold text-slate-400">{topic.value}</span>
                    <span className="text-[10px] font-bold text-accent-500">{topic.value}%</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      <Leaderboard />

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <div className="bg-white dark:bg-slate-800 p-8 rounded-2xl border border-slate-200 dark:border-slate-700 shadow-sm dark:shadow-xl dark:shadow-black/20">
          <div className="flex items-center justify-between mb-8">
            <h2 className="text-lg font-bold text-slate-900 dark:text-slate-50">Weekly Activity</h2>
            <div className="flex items-center gap-4 text-[10px] font-bold uppercase tracking-widest">
              <div className="flex items-center gap-1.5">
                <div className="w-2 h-2 rounded-full bg-accent-600" />
                <span className="text-slate-500 dark:text-slate-400">Solved</span>
              </div>
              <div className="flex items-center gap-1.5">
                <div className="w-2 h-2 rounded-full bg-slate-200 dark:bg-slate-700" />
                <span className="text-slate-500 dark:text-slate-400">Attempted</span>
              </div>
            </div>
          </div>
          <div className="h-[250px] w-full">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={metrics?.weekly}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#334155" opacity={0.5} />
                <XAxis dataKey="day" axisLine={false} tickLine={false} tick={{fill: '#94A3B8', fontSize: 11, fontWeight: 600}} dy={10} />
                <YAxis axisLine={false} tickLine={false} tick={{fill: '#94A3B8', fontSize: 11, fontWeight: 600}} />
                <Tooltip 
                  cursor={{fill: 'rgba(139, 92, 246, 0.05)'}}
                  contentStyle={{ backgroundColor: '#1E293B', borderRadius: '16px', border: '1px solid #334155', boxShadow: '0 10px 15px -3px rgb(0 0 0 / 0.3)', fontSize: '12px', color: '#F1F5F9' }}
                />
                <Bar dataKey="solved" fill="#8B5CF6" radius={[4, 4, 0, 0]} barSize={24} />
                <Bar dataKey="attempted" fill="#334155" radius={[4, 4, 0, 0]} barSize={24} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="bg-white dark:bg-slate-800 p-8 rounded-2xl border border-slate-200 dark:border-slate-700 shadow-sm dark:shadow-xl dark:shadow-black/20">
          <h2 className="text-lg font-bold text-slate-900 dark:text-slate-50 mb-6">Performance Insights</h2>
          <div className="space-y-4">
            {[
              { title: 'Off-by-one Errors', count: 12, severity: 'Medium', icon: AlertCircle, color: 'text-amber-500', bg: 'bg-amber-50 dark:bg-amber-900/20' },
              { title: 'Time Limit Exceeded', count: 8, severity: 'High', icon: Clock, color: 'text-rose-500', bg: 'bg-rose-50 dark:bg-rose-900/20' },
              { title: 'Optimal Space Usage', count: 15, severity: 'Good', icon: CheckCircle2, color: 'text-emerald-500', bg: 'bg-emerald-50 dark:bg-emerald-900/20' },
            ].map((insight) => (
              <div key={insight.title} className="flex items-center justify-between p-4 rounded-2xl border border-slate-100 dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-700/50 transition-all group">
                <div className="flex items-center gap-4">
                  <div className={cn("p-2.5 rounded-xl", insight.bg)}>
                    <insight.icon className={insight.color} size={20} />
                  </div>
                  <div>
                    <p className="font-bold text-sm text-slate-900 dark:text-slate-50">{insight.title}</p>
                    <p className="text-[10px] text-slate-500 dark:text-slate-400 font-bold uppercase tracking-widest">Detected {insight.count} times</p>
                  </div>
                </div>
                <span className={cn(
                  "text-[10px] font-bold uppercase tracking-widest px-2.5 py-1 rounded-lg",
                  insight.severity === 'High' ? 'bg-rose-100 dark:bg-rose-900/30 text-rose-600 dark:text-rose-400' : 
                  insight.severity === 'Medium' ? 'bg-amber-100 dark:bg-amber-900/30 text-amber-600 dark:text-amber-400' : 'bg-emerald-100 dark:bg-emerald-900/30 text-emerald-600 dark:text-emerald-400'
                )}>
                  {insight.severity}
                </span>
              </div>
            ))}
          </div>
          
          <div className="mt-8 p-5 bg-accent-600 rounded-2xl text-white shadow-lg shadow-accent-200 dark:shadow-none">
            <div className="flex gap-4">
              <div className="w-10 h-10 bg-white/20 rounded-xl flex items-center justify-center shrink-0">
                <Zap size={20} fill="currentColor" />
              </div>
              <div>
                <p className="text-sm font-bold">AI Recommendation</p>
                <p className="text-xs text-accent-50 mt-1 leading-relaxed">Focus on "Sliding Window" techniques. Your performance in array-based problems suggests this will yield the highest mastery impact.</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
