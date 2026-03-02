import React, { useState } from 'react';
import { 
  ArrowLeft, 
  Save, 
  Rocket, 
  Image as ImageIcon,
  Tag,
  Layers,
  DollarSign
} from 'lucide-react';
import { motion } from 'motion/react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../../services/api';

export const CreateCourse = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    difficulty: 'Beginner',
    tags: '',
    thumbnail: '',
    price: 0,
    status: 'draft'
  });

  const handleSubmit = async (e: React.FormEvent, status: string) => {
    e.preventDefault();
    setLoading(true);
    try {
      const res = await api.post('/courses', { ...formData, status });
      navigate(`/tutor/courses/${res.data.id}/modules`);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-8">
      <div className="flex items-center justify-between">
        <Link to="/tutor/courses" className="flex items-center gap-2 text-slate-500 hover:text-slate-900 dark:hover:text-slate-50 transition-colors font-bold">
          <ArrowLeft size={20} />
          Back to Courses
        </Link>
      </div>

      <div className="bg-white dark:bg-slate-800 rounded-3xl border border-slate-200 dark:border-slate-700 shadow-xl overflow-hidden">
        <div className="p-8 border-b border-slate-100 dark:border-slate-700 bg-slate-50/50 dark:bg-slate-900/50">
          <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-50">Create New Course</h1>
          <p className="text-slate-500 dark:text-slate-400 mt-1">Fill in the details below to start building your course.</p>
        </div>

        <form className="p-8 space-y-8">
          <div className="space-y-6">
            <div className="space-y-2">
              <label className="text-sm font-bold text-slate-900 dark:text-slate-50">Course Title</label>
              <input 
                type="text"
                value={formData.title}
                onChange={e => setFormData({...formData, title: e.target.value})}
                placeholder="e.g. Advanced Data Structures & Algorithms"
                className="w-full px-4 py-3 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl focus:ring-2 focus:ring-accent-500 outline-none transition-all dark:text-slate-100"
                required
              />
            </div>

            <div className="space-y-2">
              <label className="text-sm font-bold text-slate-900 dark:text-slate-50">Description</label>
              <textarea 
                value={formData.description}
                onChange={e => setFormData({...formData, description: e.target.value})}
                placeholder="What will students learn in this course?"
                rows={4}
                className="w-full px-4 py-3 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl focus:ring-2 focus:ring-accent-500 outline-none transition-all dark:text-slate-100 resize-none"
                required
              />
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="space-y-2">
                <label className="text-sm font-bold text-slate-900 dark:text-slate-50 flex items-center gap-2">
                  <Layers size={16} className="text-accent-600" />
                  Difficulty Level
                </label>
                <select 
                  value={formData.difficulty}
                  onChange={e => setFormData({...formData, difficulty: e.target.value})}
                  className="w-full px-4 py-3 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl focus:ring-2 focus:ring-accent-500 outline-none transition-all dark:text-slate-100"
                >
                  <option>Beginner</option>
                  <option>Intermediate</option>
                  <option>Advanced</option>
                </select>
              </div>

              <div className="space-y-2">
                <label className="text-sm font-bold text-slate-900 dark:text-slate-50 flex items-center gap-2">
                  <Tag size={16} className="text-emerald-600" />
                  Tags (comma separated)
                </label>
                <input 
                  type="text"
                  value={formData.tags}
                  onChange={e => setFormData({...formData, tags: e.target.value})}
                  placeholder="e.g. DSA, Python, Interview Prep"
                  className="w-full px-4 py-3 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl focus:ring-2 focus:ring-accent-500 outline-none transition-all dark:text-slate-100"
                />
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="space-y-2">
                <label className="text-sm font-bold text-slate-900 dark:text-slate-50 flex items-center gap-2">
                  <ImageIcon size={16} className="text-amber-600" />
                  Thumbnail URL
                </label>
                <input 
                  type="text"
                  value={formData.thumbnail}
                  onChange={e => setFormData({...formData, thumbnail: e.target.value})}
                  placeholder="https://example.com/image.jpg"
                  className="w-full px-4 py-3 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl focus:ring-2 focus:ring-accent-500 outline-none transition-all dark:text-slate-100"
                />
              </div>

              <div className="space-y-2">
                <label className="text-sm font-bold text-slate-900 dark:text-slate-50 flex items-center gap-2">
                  <DollarSign size={16} className="text-rose-600" />
                  Price (0 for Free)
                </label>
                <input 
                  type="number"
                  value={formData.price}
                  onChange={e => setFormData({...formData, price: Number(e.target.value)})}
                  className="w-full px-4 py-3 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl focus:ring-2 focus:ring-accent-500 outline-none transition-all dark:text-slate-100"
                />
              </div>
            </div>
          </div>

          <div className="flex items-center gap-4 pt-6 border-t border-slate-100 dark:border-slate-700">
            <button 
              type="button"
              onClick={(e) => handleSubmit(e, 'draft')}
              disabled={loading}
              className="flex-1 flex items-center justify-center gap-2 py-4 bg-slate-100 dark:bg-slate-900 hover:bg-slate-200 dark:hover:bg-slate-700 text-slate-900 dark:text-slate-100 font-bold rounded-2xl transition-all border border-slate-200 dark:border-slate-700"
            >
              <Save size={20} />
              Save as Draft
            </button>
            <button 
              type="button"
              onClick={(e) => handleSubmit(e, 'published')}
              disabled={loading}
              className="flex-1 flex items-center justify-center gap-2 py-4 bg-accent-600 hover:bg-accent-700 text-white font-bold rounded-2xl transition-all shadow-lg shadow-accent-200 dark:shadow-none"
            >
              <Rocket size={20} />
              Publish Course
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
