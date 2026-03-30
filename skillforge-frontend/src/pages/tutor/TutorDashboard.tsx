import React, { useState, useEffect } from 'react';
import { 
  Plus, 
  BookOpen, 
  Users, 
  MessageSquare, 
  CheckCircle, 
  TrendingUp,
  ArrowRight,
  Clock,
  FileText
} from 'lucide-react';
import { motion } from 'motion/react';
import { Link } from 'react-router-dom';
import api from '../../services/api';

interface Metrics {
  totalCourses: number;
  totalLearners: number;
  activeDoubts: number;
  pendingReviews: number;
  revenue: number;
}

interface Activity {
  studentName: string;
  studentImage?: string;
  problemTitle: string;
  status: string;
  created_at: string;
}

export const TutorDashboard = () => {
  const [metrics, setMetrics] = useState<Metrics | null>(null);
  const [activities, setActivities] = useState<Activity[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const res = await api.get('/tutor/dashboard');
        setMetrics(res.data.metrics);
        setActivities(res.data.recentActivity);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  const StatCard = ({ icon: Icon, label, value, color }: any) => (
    <div className="bg-white dark:bg-slate-800 p-6 rounded-2xl border border-slate-200 dark:border-slate-700 shadow-sm">
      <div className="flex items-center justify-between mb-4">
        <div className={`p-3 rounded-xl bg-${color}-50 dark:bg-${color}-900/20 text-${color}-600 dark:text-${color}-400`}>
          <Icon size={24} />
        </div>
        <TrendingUp size={16} className="text-emerald-500" />
      </div>
      <p className="text-sm font-medium text-slate-500 dark:text-slate-400">{label}</p>
      <p className="text-2xl font-bold text-slate-900 dark:text-slate-50 mt-1">{value}</p>
    </div>
  );

  if (loading) return <div className="flex items-center justify-center h-96">Loading...</div>;

  return (
    <div className="space-y-8">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-slate-900 dark:text-slate-50">Tutor Dashboard</h1>
          <p className="text-slate-500 dark:text-slate-400 mt-1">Welcome back! Here's what's happening with your courses.</p>
        </div>
        <Link 
          to="/tutor/courses/create"
          className="flex items-center gap-2 px-6 py-3 bg-accent-600 hover:bg-accent-700 text-white font-bold rounded-xl transition-all shadow-lg shadow-accent-200 dark:shadow-none"
        >
          <Plus size={20} />
          Create New Course
        </Link>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <StatCard icon={BookOpen} label="Total Courses" value={metrics?.totalCourses || 0} color="accent" />
        <StatCard icon={Users} label="Total Learners" value={metrics?.totalLearners || 0} color="emerald" />
        <StatCard icon={MessageSquare} label="Active Doubts" value={metrics?.activeDoubts || 0} color="amber" />
        <StatCard icon={CheckCircle} label="Pending Reviews" value={metrics?.pendingReviews || 0} color="rose" />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2 space-y-6">
          <div className="bg-white dark:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700 shadow-sm overflow-hidden">
            <div className="p-6 border-b border-slate-100 dark:border-slate-700 flex items-center justify-between">
              <h2 className="text-lg font-bold text-slate-900 dark:text-slate-50">Recent Student Activity</h2>
              <Link to="/tutor/submissions" className="text-sm font-bold text-accent-600 hover:text-accent-700 flex items-center gap-1">
                View All <ArrowRight size={16} />
              </Link>
            </div>
            <div className="divide-y divide-slate-100 dark:divide-slate-700">
              {activities.length > 0 ? activities.map((activity, i) => (
                <div key={i} className="p-6 flex items-center justify-between hover:bg-slate-50 dark:hover:bg-slate-700/50 transition-colors">
                  <div className="flex items-center gap-4">
                    <div className="w-10 h-10 rounded-full bg-slate-100 dark:bg-slate-900 flex items-center justify-center text-slate-600 dark:text-slate-400 font-bold overflow-hidden">
                      {activity.studentImage ? (
                        <img 
                          src={activity.studentImage} 
                          alt={activity.studentName} 
                          className="w-full h-full object-cover"
                          onError={(e) => {
                            (e.target as HTMLImageElement).src = `https://ui-avatars.com/api/?name=${encodeURIComponent(activity.studentName)}&background=f1f5f9&color=64748b`;
                          }}
                        />
                      ) : (
                        activity.studentName.charAt(0)
                      )}
                    </div>
                    <div>
                      <p className="text-sm font-bold text-slate-900 dark:text-slate-50">{activity.studentName}</p>
                      <p className="text-xs text-slate-500 dark:text-slate-400">Submitted solution for <span className="text-accent-600">{activity.problemTitle}</span></p>
                    </div>
                  </div>
                  <div className="text-right">
                    <span className={`text-[10px] font-bold px-2 py-1 rounded-full uppercase tracking-wider ${
                      activity.status === 'Accepted' ? 'bg-emerald-100 text-emerald-600' : 'bg-rose-100 text-rose-600'
                    }`}>
                      {activity.status}
                    </span>
                    <p className="text-[10px] text-slate-400 mt-1">{new Date(activity.created_at).toLocaleDateString()}</p>
                  </div>
                </div>
              )) : (
                <div className="p-12 text-center text-slate-500">No recent activity</div>
              )}
            </div>
          </div>
        </div>

        <div className="space-y-6">
          <div className="bg-white dark:bg-slate-800 p-6 rounded-2xl border border-slate-200 dark:border-slate-700 shadow-sm">
            <h2 className="text-lg font-bold text-slate-900 dark:text-slate-50 mb-6">Quick Actions</h2>
            <div className="space-y-3">
              <Link to="/tutor/courses/create" className="flex items-center gap-3 p-4 rounded-xl bg-slate-50 dark:bg-slate-900 hover:bg-accent-50 dark:hover:bg-accent-900/20 text-slate-700 dark:text-slate-300 hover:text-accent-600 transition-all border border-slate-100 dark:border-slate-800">
                <Plus size={20} />
                <span className="font-bold text-sm">Create Course</span>
              </Link>
              <Link to="/tutor/problems/create" className="flex items-center gap-3 p-4 rounded-xl bg-slate-50 dark:bg-slate-900 hover:bg-accent-50 dark:hover:bg-accent-900/20 text-slate-700 dark:text-slate-300 hover:text-accent-600 transition-all border border-slate-100 dark:border-slate-800">
                <FileText size={20} />
                <span className="font-bold text-sm">Add Problem</span>
              </Link>
              <Link to="/tutor/submissions" className="flex items-center gap-3 p-4 rounded-xl bg-slate-50 dark:bg-slate-900 hover:bg-accent-50 dark:hover:bg-accent-900/20 text-slate-700 dark:text-slate-300 hover:text-accent-600 transition-all border border-slate-100 dark:border-slate-800">
                <CheckCircle size={20} />
                <span className="font-bold text-sm">View Submissions</span>
              </Link>
              <Link to="/tutor/doubts" className="flex items-center gap-3 p-4 rounded-xl bg-slate-50 dark:bg-slate-900 hover:bg-accent-50 dark:hover:bg-accent-900/20 text-slate-700 dark:text-slate-300 hover:text-accent-600 transition-all border border-slate-100 dark:border-slate-800">
                <MessageSquare size={20} />
                <span className="font-bold text-sm">Answer Doubts</span>
              </Link>
            </div>
          </div>

          <div className="bg-accent-600 p-6 rounded-2xl text-white shadow-lg shadow-accent-200 dark:shadow-none">
            <h3 className="font-bold mb-2">Need Help?</h3>
            <p className="text-sm opacity-90 mb-4">Check out our tutor guide for best practices on creating engaging courses.</p>
            <button className="w-full py-2 bg-white text-accent-600 font-bold rounded-xl text-sm">
              Read Guide
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
