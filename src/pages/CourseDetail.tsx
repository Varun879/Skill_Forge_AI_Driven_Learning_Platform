import React, { useState, useEffect } from 'react';
import { 
  ArrowLeft, 
  Play, 
  FileText, 
  CheckCircle, 
  Lock,
  Users,
  Clock,
  Award,
  ChevronRight,
  MessageSquare,
  List
} from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';
import { Link, useParams, useNavigate } from 'react-router-dom';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';

export const CourseDetail = () => {
  const { id } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();
  const [course, setCourse] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [enrolling, setEnrolling] = useState(false);
  const [isEnrolled, setIsEnrolled] = useState(false);
  const [isMobileNavOpen, setIsMobileNavOpen] = useState(false);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [courseRes, enrollmentsRes] = await Promise.all([
          api.get(`/courses/${id}`),
          api.get('/user/enrollments')
        ]);
        setCourse(courseRes.data);
        setIsEnrolled(enrollmentsRes.data.some((e: any) => e.id === Number(id)));
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [id]);

  const handleEnroll = async () => {
    setEnrolling(true);
    try {
      await api.post(`/courses/${id}/enroll`);
      setIsEnrolled(true);
    } catch (err) {
      console.error(err);
    } finally {
      setEnrolling(false);
    }
  };

  if (loading) return <div className="flex items-center justify-center h-96">Loading...</div>;

  const scrollToModule = (moduleId: number) => {
    const element = document.getElementById(`module-${moduleId}`);
    if (element) {
      const offset = 100;
      const bodyRect = document.body.getBoundingClientRect().top;
      const elementRect = element.getBoundingClientRect().top;
      const elementPosition = elementRect - bodyRect;
      const offsetPosition = elementPosition - offset;

      window.scrollTo({
        top: offsetPosition,
        behavior: 'smooth'
      });
    }
    setIsMobileNavOpen(false);
  };

  return (
    <div className="max-w-7xl mx-auto space-y-8 px-4">
      <div className="flex items-center justify-between">
        <Link to="/explore" className="flex items-center gap-2 text-slate-500 hover:text-slate-900 dark:hover:text-slate-50 transition-colors font-bold">
          <ArrowLeft size={20} />
          Back to Explore
        </Link>

        {/* Mobile Module Nav Toggle */}
        <button 
          onClick={() => setIsMobileNavOpen(!isMobileNavOpen)}
          className="lg:hidden flex items-center gap-2 px-4 py-2 bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl text-sm font-bold text-slate-700 dark:text-slate-300 shadow-sm"
        >
          <List size={18} />
          Modules
        </button>
      </div>

      {/* Mobile Module Navigation Overlay */}
      <AnimatePresence>
        {isMobileNavOpen && (
          <>
            <motion.div 
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={() => setIsMobileNavOpen(false)}
              className="fixed inset-0 bg-slate-900/40 backdrop-blur-sm z-[60] lg:hidden"
            />
            <motion.div 
              initial={{ x: '100%' }}
              animate={{ x: 0 }}
              exit={{ x: '100%' }}
              className="fixed right-0 top-0 bottom-0 w-80 bg-white dark:bg-slate-900 z-[70] lg:hidden shadow-2xl p-6 overflow-y-auto"
            >
              <div className="flex items-center justify-between mb-8">
                <h3 className="text-lg font-bold text-slate-900 dark:text-slate-50">Course Modules</h3>
                <button onClick={() => setIsMobileNavOpen(false)} className="p-2 text-slate-400 hover:text-slate-900 dark:hover:text-slate-50">
                  <ChevronRight size={24} />
                </button>
              </div>
              <div className="space-y-3">
                {course.modules.map((mod: any, i: number) => (
                  <button
                    key={mod.id}
                    onClick={() => scrollToModule(mod.id)}
                    className="w-full flex items-center gap-4 p-4 rounded-2xl bg-slate-50 dark:bg-slate-800/50 border border-slate-100 dark:border-slate-800 text-left transition-all hover:border-accent-500"
                  >
                    <div className="w-10 h-10 rounded-xl bg-white dark:bg-slate-900 flex items-center justify-center text-slate-400 shrink-0">
                      {isEnrolled ? <Play size={18} /> : <Lock size={18} />}
                    </div>
                    <div>
                      <p className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Module {i + 1}</p>
                      <p className="text-sm font-bold text-slate-900 dark:text-slate-50 line-clamp-1">{mod.title}</p>
                    </div>
                  </button>
                ))}
              </div>
            </motion.div>
          </>
        )}
      </AnimatePresence>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
        {/* Desktop Sidebar Navigation */}
        <aside className="hidden lg:block lg:col-span-3 sticky top-24 space-y-6">
          <div className="bg-white dark:bg-slate-800 rounded-3xl border border-slate-200 dark:border-slate-700 shadow-sm p-6">
            <h3 className="text-xs font-bold text-slate-400 uppercase tracking-widest mb-4 ml-1">Course Progress</h3>
            <div className="space-y-2">
              {course.modules.map((mod: any, i: number) => (
                <button
                  key={mod.id}
                  onClick={() => scrollToModule(mod.id)}
                  className="w-full flex items-center gap-3 p-3 rounded-xl hover:bg-slate-50 dark:hover:bg-slate-900/50 border border-transparent hover:border-slate-100 dark:hover:border-slate-800 transition-all group text-left"
                >
                  <div className="w-8 h-8 rounded-lg bg-slate-50 dark:bg-slate-900 flex items-center justify-center text-slate-400 group-hover:text-accent-600 transition-colors shrink-0">
                    {isEnrolled ? <Play size={14} /> : <Lock size={14} />}
                  </div>
                  <div className="min-w-0">
                    <p className="text-[10px] font-bold text-slate-400 uppercase tracking-widest truncate">Module {i + 1}</p>
                    <p className="text-xs font-bold text-slate-900 dark:text-slate-50 truncate">{mod.title}</p>
                  </div>
                </button>
              ))}
            </div>
          </div>
          
          {isEnrolled && (
            <div className="p-6 bg-accent-600 rounded-3xl text-white shadow-lg shadow-accent-200 dark:shadow-none">
              <p className="text-xs font-bold uppercase tracking-widest opacity-80 mb-1">Learning Tip</p>
              <p className="text-sm font-bold leading-relaxed">Complete modules in sequence to unlock advanced adaptive problems.</p>
            </div>
          )}
        </aside>

        <div className="lg:col-span-6 space-y-8">
          <div className="space-y-6">
            <div className="flex items-center gap-3">
              <span className="text-xs font-bold px-3 py-1 bg-accent-50 dark:bg-accent-900/20 text-accent-600 dark:text-accent-400 rounded-full uppercase tracking-widest border border-accent-100 dark:border-accent-800">
                {course.difficulty}
              </span>
              <span className="text-xs font-bold px-3 py-1 bg-emerald-50 dark:bg-emerald-900/20 text-emerald-600 dark:text-emerald-400 rounded-full uppercase tracking-widest border border-emerald-100 dark:border-emerald-800">
                Adaptive Learning
              </span>
            </div>
            <h1 className="text-4xl font-extrabold text-slate-900 dark:text-slate-50 tracking-tight">{course.title}</h1>
            <p className="text-lg text-slate-600 dark:text-slate-400 leading-relaxed">{course.description}</p>
            
            <div className="flex flex-wrap gap-6 py-6 border-y border-slate-100 dark:border-slate-800">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-full bg-slate-100 dark:bg-slate-900 flex items-center justify-center text-slate-600 dark:text-slate-400">
                  <Users size={20} />
                </div>
                <div>
                  <p className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Tutor</p>
                  <p className="text-sm font-bold text-slate-900 dark:text-slate-50">{course.tutor_name}</p>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-full bg-slate-100 dark:bg-slate-900 flex items-center justify-center text-slate-600 dark:text-slate-400">
                  <Clock size={20} />
                </div>
                <div>
                  <p className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Duration</p>
                  <p className="text-sm font-bold text-slate-900 dark:text-slate-50">Self-Paced</p>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-full bg-slate-100 dark:bg-slate-900 flex items-center justify-center text-slate-600 dark:text-slate-400">
                  <Award size={20} />
                </div>
                <div>
                  <p className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Certificate</p>
                  <p className="text-sm font-bold text-slate-900 dark:text-slate-50">Included</p>
                </div>
              </div>
            </div>
          </div>

          <div className="space-y-6">
            <h2 className="text-2xl font-bold text-slate-900 dark:text-slate-50">Course Content</h2>
            <div className="space-y-4">
              {course.modules.map((mod: any, i: number) => (
                <div 
                  key={mod.id} 
                  id={`module-${mod.id}`}
                  className="bg-white dark:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700 shadow-sm overflow-hidden group transition-all hover:shadow-md"
                >
                  <div className="p-5 flex items-center justify-between">
                    <div className="flex items-center gap-4">
                      <div className="w-10 h-10 rounded-xl bg-slate-50 dark:bg-slate-900 flex items-center justify-center text-slate-400 group-hover:text-accent-600 transition-colors">
                        {isEnrolled ? <Play size={20} /> : <Lock size={20} />}
                      </div>
                      <div>
                        <p className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Module {i + 1}</p>
                        <h3 className="font-bold text-slate-900 dark:text-slate-50">{mod.title}</h3>
                      </div>
                    </div>
                    {isEnrolled && (
                      <button className="p-2 text-slate-400 hover:text-accent-600 transition-colors">
                        <ChevronRight size={20} />
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        <div className="lg:col-span-3 space-y-6">
          <div className="bg-white dark:bg-slate-800 p-8 rounded-3xl border border-slate-200 dark:border-slate-700 shadow-xl sticky top-24">
            <div className="aspect-video bg-slate-100 dark:bg-slate-900 rounded-2xl mb-6 overflow-hidden relative group">
              {course.thumbnail ? (
                <img src={course.thumbnail} alt={course.title} className="w-full h-full object-cover" />
              ) : (
                <div className="w-full h-full flex items-center justify-center text-slate-400">
                  <Play size={48} fill="currentColor" />
                </div>
              )}
              <div className="absolute inset-0 bg-black/20 group-hover:bg-black/40 transition-colors flex items-center justify-center">
                <div className="w-16 h-16 rounded-full bg-white/90 dark:bg-slate-800/90 flex items-center justify-center text-accent-600 shadow-xl">
                  <Play size={32} fill="currentColor" />
                </div>
              </div>
            </div>

            <div className="space-y-6">
              <div className="flex items-center justify-between">
                <p className="text-3xl font-extrabold text-slate-900 dark:text-slate-50">
                  {course.price === 0 ? 'Free' : `$${course.price}`}
                </p>
                {course.price > 0 && (
                  <span className="text-xs font-bold text-slate-400 line-through">$99.99</span>
                )}
              </div>

              {isEnrolled ? (
                <button 
                  disabled
                  className="w-full py-4 bg-emerald-100 dark:bg-emerald-900/20 text-emerald-600 dark:text-emerald-400 font-bold rounded-2xl flex items-center justify-center gap-2"
                >
                  <CheckCircle size={20} />
                  Already Enrolled
                </button>
              ) : (
                <button 
                  onClick={handleEnroll}
                  disabled={enrolling}
                  className="w-full py-4 bg-accent-600 hover:bg-accent-700 text-white font-bold rounded-2xl transition-all shadow-lg shadow-accent-200 dark:shadow-none flex items-center justify-center gap-2"
                >
                  {enrolling ? 'Enrolling...' : 'Enroll Now'}
                </button>
              )}

              <div className="space-y-4 pt-6 border-t border-slate-100 dark:border-slate-800">
                <h4 className="text-sm font-bold text-slate-900 dark:text-slate-50">This course includes:</h4>
                <ul className="space-y-3">
                  <li className="flex items-center gap-3 text-sm text-slate-600 dark:text-slate-400">
                    <Play size={16} className="text-accent-600" />
                    {course.modules.length} Adaptive Modules
                  </li>
                  <li className="flex items-center gap-3 text-sm text-slate-600 dark:text-slate-400">
                    <FileText size={16} className="text-accent-600" />
                    Full Lifetime Access
                  </li>
                  <li className="flex items-center gap-3 text-sm text-slate-600 dark:text-slate-400">
                    <MessageSquare size={16} className="text-accent-600" />
                    Direct Tutor Support
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
