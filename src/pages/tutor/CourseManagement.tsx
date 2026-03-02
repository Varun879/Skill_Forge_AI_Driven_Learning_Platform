import React, { useState, useEffect } from 'react';
import { 
  Plus, 
  Edit2, 
  Trash2, 
  Users, 
  BookOpen,
  MoreVertical,
  ExternalLink,
  Eye
} from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';
import { Link } from 'react-router-dom';
import api from '../../services/api';

interface Course {
  id: number;
  title: string;
  description: string;
  difficulty: string;
  status: string;
  created_at: string;
  thumbnail: string;
}

export const CourseManagement = () => {
  const [courses, setCourses] = useState<Course[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchCourses = async () => {
      try {
        const res = await api.get('/tutor/courses');
        setCourses(res.data);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchCourses();
  }, []);

  const handleDelete = async (id: number) => {
    if (!window.confirm('Are you sure you want to delete this course?')) return;
    try {
      await api.delete(`/courses/${id}`);
      setCourses(courses.filter(c => c.id !== id));
    } catch (err) {
      console.error(err);
    }
  };

  if (loading) return <div className="flex items-center justify-center h-96">Loading...</div>;

  return (
    <div className="space-y-8">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-slate-900 dark:text-slate-50">Course Management</h1>
          <p className="text-slate-500 dark:text-slate-400 mt-1">Manage your existing courses and create new ones.</p>
        </div>
        <Link 
          to="/tutor/courses/create"
          className="flex items-center gap-2 px-6 py-3 bg-accent-600 hover:bg-accent-700 text-white font-bold rounded-xl transition-all shadow-lg shadow-accent-200 dark:shadow-none"
        >
          <Plus size={20} />
          Create New Course
        </Link>
      </div>

      <div className="grid grid-cols-1 gap-6">
        {courses.length > 0 ? courses.map((course) => (
          <div key={course.id} className="bg-white dark:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700 shadow-sm overflow-hidden flex flex-col md:flex-row">
            <div className="w-full md:w-64 h-48 md:h-auto bg-slate-100 dark:bg-slate-900 relative overflow-hidden">
              {course.thumbnail ? (
                <img src={course.thumbnail} alt={course.title} className="w-full h-full object-cover" />
              ) : (
                <div className="w-full h-full flex items-center justify-center text-slate-400">
                  <BookOpen size={48} />
                </div>
              )}
              <div className="absolute top-4 left-4">
                <span className={`text-[10px] font-bold px-2 py-1 rounded-full uppercase tracking-wider ${
                  course.status === 'published' ? 'bg-emerald-100 text-emerald-600' : 'bg-amber-100 text-amber-600'
                }`}>
                  {course.status}
                </span>
              </div>
            </div>
            
            <div className="flex-1 p-6 flex flex-col justify-between">
              <div>
                <div className="flex items-start justify-between mb-2">
                  <h2 className="text-xl font-bold text-slate-900 dark:text-slate-50">{course.title}</h2>
                  <div className="flex items-center gap-2">
                    <Link to={`/tutor/courses/${course.id}/edit`} className="p-2 text-slate-400 hover:text-accent-600 transition-colors">
                      <Edit2 size={18} />
                    </Link>
                    <button onClick={() => handleDelete(course.id)} className="p-2 text-slate-400 hover:text-rose-600 transition-colors">
                      <Trash2 size={18} />
                    </button>
                  </div>
                </div>
                <p className="text-sm text-slate-500 dark:text-slate-400 line-clamp-2 mb-4">{course.description}</p>
                <div className="flex flex-wrap gap-4 text-xs font-bold text-slate-500 dark:text-slate-400">
                  <div className="flex items-center gap-1.5">
                    <BookOpen size={14} className="text-accent-600" />
                    Adaptive Modules
                  </div>
                  <div className="flex items-center gap-1.5">
                    <Users size={14} className="text-emerald-600" />
                    Enrolled Students
                  </div>
                  <div className="flex items-center gap-1.5 uppercase tracking-widest">
                    {course.difficulty}
                  </div>
                </div>
              </div>

              <div className="mt-6 flex items-center gap-3">
                <Link 
                  to={`/tutor/courses/${course.id}/modules`}
                  className="flex-1 py-2.5 bg-slate-50 dark:bg-slate-900 hover:bg-slate-100 dark:hover:bg-slate-700 text-slate-900 dark:text-slate-100 font-bold rounded-xl text-center text-sm border border-slate-200 dark:border-slate-700 transition-all"
                >
                  Manage Content
                </Link>
                <Link 
                  to={`/courses/${course.id}`}
                  className="px-4 py-2.5 bg-accent-50 dark:bg-accent-900/20 text-accent-600 dark:text-accent-400 font-bold rounded-xl hover:bg-accent-100 transition-all border border-accent-100 dark:border-accent-800"
                >
                  <Eye size={18} />
                </Link>
              </div>
            </div>
          </div>
        )) : (
          <div className="bg-white dark:bg-slate-800 rounded-3xl border-2 border-dashed border-slate-200 dark:border-slate-700 p-20 text-center">
            <div className="w-20 h-20 bg-slate-50 dark:bg-slate-900 rounded-full flex items-center justify-center mx-auto mb-6 text-slate-400">
              <BookOpen size={40} />
            </div>
            <h2 className="text-xl font-bold text-slate-900 dark:text-slate-50 mb-2">No Courses Created Yet</h2>
            <p className="text-slate-500 dark:text-slate-400 mb-8 max-w-sm mx-auto">Start building your first course and share your knowledge with the SkillForge community.</p>
            <Link 
              to="/tutor/courses/create"
              className="inline-flex items-center gap-2 px-8 py-3 bg-accent-600 hover:bg-accent-700 text-white font-bold rounded-xl transition-all shadow-lg shadow-accent-200 dark:shadow-none"
            >
              <Plus size={20} />
              Create Your First Course
            </Link>
          </div>
        )}
      </div>
    </div>
  );
};
