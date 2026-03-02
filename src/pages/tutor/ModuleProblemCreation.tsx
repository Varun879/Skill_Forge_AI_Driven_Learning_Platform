import React, { useState, useEffect } from 'react';
import { 
  Plus, 
  Video, 
  FileText, 
  Code2, 
  Save, 
  ChevronRight,
  ArrowLeft,
  Trash2,
  GripVertical
} from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';
import { Link, useParams } from 'react-router-dom';
import api from '../../services/api';

interface Module {
  id: number;
  title: string;
  video_url: string;
  notes: string;
  order_index: number;
}

export const ModuleProblemCreation = () => {
  const { id } = useParams();
  const [course, setCourse] = useState<any>(null);
  const [modules, setModules] = useState<Module[]>([]);
  const [loading, setLoading] = useState(true);
  const [showAddModule, setShowAddModule] = useState(false);
  const [newModule, setNewModule] = useState({ title: '', video_url: '', notes: '' });

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [courseRes, modulesRes] = await Promise.all([
          api.get(`/courses/${id}`),
          api.get(`/courses/${id}/modules`)
        ]);
        setCourse(courseRes.data);
        setModules(modulesRes.data);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [id]);

  const handleAddModule = async () => {
    try {
      const res = await api.post(`/courses/${id}/modules`, {
        ...newModule,
        order_index: modules.length + 1
      });
      setModules([...modules, { ...newModule, id: res.data.id, order_index: modules.length + 1 }]);
      setShowAddModule(false);
      setNewModule({ title: '', video_url: '', notes: '' });
    } catch (err) {
      console.error(err);
    }
  };

  if (loading) return <div className="flex items-center justify-center h-96">Loading...</div>;

  return (
    <div className="max-w-5xl mx-auto space-y-8">
      <div className="flex items-center justify-between">
        <Link to="/tutor/courses" className="flex items-center gap-2 text-slate-500 hover:text-slate-900 dark:hover:text-slate-50 transition-colors font-bold">
          <ArrowLeft size={20} />
          Back to Courses
        </Link>
        <div className="flex items-center gap-3">
          <span className="text-sm font-bold text-slate-400">Course:</span>
          <span className="text-sm font-bold text-slate-900 dark:text-slate-50">{course?.title}</span>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2 space-y-6">
          <div className="flex items-center justify-between">
            <h2 className="text-xl font-bold text-slate-900 dark:text-slate-50">Course Modules</h2>
            <button 
              onClick={() => setShowAddModule(true)}
              className="flex items-center gap-2 px-4 py-2 bg-accent-600 hover:bg-accent-700 text-white font-bold rounded-xl text-sm transition-all"
            >
              <Plus size={18} />
              Add Module
            </button>
          </div>

          <div className="space-y-4">
            {modules.map((mod, i) => (
              <div key={mod.id} className="bg-white dark:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700 shadow-sm overflow-hidden">
                <div className="p-4 flex items-center justify-between bg-slate-50/50 dark:bg-slate-900/50 border-b border-slate-100 dark:border-slate-700">
                  <div className="flex items-center gap-3">
                    <div className="p-2 bg-white dark:bg-slate-800 rounded-lg border border-slate-200 dark:border-slate-700 text-slate-400">
                      <GripVertical size={16} />
                    </div>
                    <span className="text-xs font-bold text-slate-400 uppercase tracking-widest">Module {i + 1}</span>
                    <h3 className="font-bold text-slate-900 dark:text-slate-50">{mod.title}</h3>
                  </div>
                  <div className="flex items-center gap-2">
                    <button className="p-2 text-slate-400 hover:text-rose-600 transition-colors">
                      <Trash2 size={18} />
                    </button>
                  </div>
                </div>
                <div className="p-6 grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div className="space-y-4">
                    <div className="flex items-center gap-3 text-sm text-slate-600 dark:text-slate-400">
                      <Video size={18} className="text-accent-600" />
                      <span className="font-medium truncate">{mod.video_url || 'No video linked'}</span>
                    </div>
                    <div className="flex items-start gap-3 text-sm text-slate-600 dark:text-slate-400">
                      <FileText size={18} className="text-emerald-600" />
                      <p className="font-medium line-clamp-2">{mod.notes || 'No notes added'}</p>
                    </div>
                  </div>
                  <div className="flex flex-col justify-end gap-2">
                    <Link 
                      to={`/tutor/courses/${id}/modules/${mod.id}/problems/create`}
                      className="w-full py-2 bg-accent-50 dark:bg-accent-900/20 text-accent-600 dark:text-accent-400 font-bold rounded-xl text-center text-xs border border-accent-100 dark:border-accent-800 hover:bg-accent-100 transition-all"
                    >
                      Add Problems to Module
                    </Link>
                  </div>
                </div>
              </div>
            ))}

            {showAddModule && (
              <motion.div 
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                className="bg-white dark:bg-slate-800 rounded-2xl border-2 border-accent-500 p-6 space-y-4 shadow-xl shadow-accent-500/10"
              >
                <div className="grid grid-cols-1 gap-4">
                  <input 
                    type="text"
                    placeholder="Module Title"
                    value={newModule.title}
                    onChange={e => setNewModule({...newModule, title: e.target.value})}
                    className="w-full px-4 py-3 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl outline-none dark:text-slate-100"
                  />
                  <input 
                    type="text"
                    placeholder="Video URL (YouTube/Vimeo)"
                    value={newModule.video_url}
                    onChange={e => setNewModule({...newModule, video_url: e.target.value})}
                    className="w-full px-4 py-3 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl outline-none dark:text-slate-100"
                  />
                  <textarea 
                    placeholder="Module Notes (Markdown supported)"
                    value={newModule.notes}
                    onChange={e => setNewModule({...newModule, notes: e.target.value})}
                    rows={3}
                    className="w-full px-4 py-3 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl outline-none dark:text-slate-100 resize-none"
                  />
                </div>
                <div className="flex items-center gap-3">
                  <button 
                    onClick={() => setShowAddModule(false)}
                    className="flex-1 py-3 bg-slate-100 dark:bg-slate-900 text-slate-600 dark:text-slate-400 font-bold rounded-xl"
                  >
                    Cancel
                  </button>
                  <button 
                    onClick={handleAddModule}
                    className="flex-1 py-3 bg-accent-600 text-white font-bold rounded-xl"
                  >
                    Save Module
                  </button>
                </div>
              </motion.div>
            )}
          </div>
        </div>

        <div className="space-y-6">
          <div className="bg-white dark:bg-slate-800 p-6 rounded-2xl border border-slate-200 dark:border-slate-700 shadow-sm">
            <h2 className="text-lg font-bold text-slate-900 dark:text-slate-50 mb-4">Course Progress</h2>
            <div className="space-y-4">
              <div className="flex items-center justify-between text-sm">
                <span className="text-slate-500 dark:text-slate-400">Modules Created</span>
                <span className="font-bold text-slate-900 dark:text-slate-50">{modules.length}</span>
              </div>
              <div className="w-full h-2 bg-slate-100 dark:bg-slate-900 rounded-full overflow-hidden">
                <div className="h-full bg-accent-600 rounded-full" style={{ width: `${Math.min(100, (modules.length / 5) * 100)}%` }} />
              </div>
              <p className="text-xs text-slate-400 italic">Recommended: At least 5 modules for a comprehensive course.</p>
            </div>
          </div>

          <div className="bg-emerald-600 p-6 rounded-2xl text-white shadow-lg shadow-emerald-200 dark:shadow-none">
            <h3 className="font-bold mb-2">Publishing Checklist</h3>
            <ul className="space-y-2 text-sm opacity-90">
              <li className="flex items-center gap-2">
                <CheckCircle size={16} /> Course details complete
              </li>
              <li className="flex items-center gap-2">
                <CheckCircle size={16} /> At least 1 module added
              </li>
              <li className="flex items-center gap-2">
                <div className="w-4 h-4 border-2 border-white/50 rounded-full" /> Add practice problems
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
};

const CheckCircle = ({ size }: { size: number }) => (
  <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round">
    <polyline points="20 6 9 17 4 12" />
  </svg>
);
