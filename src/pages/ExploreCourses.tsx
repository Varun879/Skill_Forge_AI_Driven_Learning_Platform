import React, { useState, useEffect } from 'react';
import { 
  Search, 
  Filter, 
  BookOpen, 
  Users, 
  Star,
  ArrowRight,
  Layers,
  Tag
} from 'lucide-react';
import { motion } from 'motion/react';
import { Link } from 'react-router-dom';
import api from '../services/api';

interface Course {
  id: number;
  title: string;
  description: string;
  difficulty: string;
  tags: string;
  thumbnail: string;
  price: number;
  tutor_name: string;
}

export const ExploreCourses = () => {
  const [courses, setCourses] = useState<Course[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');

  useEffect(() => {
    const fetchCourses = async () => {
      try {
        const res = await api.get('/courses');
        setCourses(res.data);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchCourses();
  }, []);

  const filteredCourses = courses.filter(c => 
    c.title.toLowerCase().includes(search.toLowerCase()) || 
    c.tags.toLowerCase().includes(search.toLowerCase())
  );

  if (loading) return <div className="flex items-center justify-center h-96">Loading...</div>;

  return (
    <div className="space-y-8">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-slate-900 dark:text-slate-50">Explore Courses</h1>
          <p className="text-slate-500 dark:text-slate-400 mt-1">Master new skills with our expert-led adaptive courses.</p>
        </div>
        <div className="relative max-w-md w-full">
          <Search className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" size={20} />
          <input 
            type="text"
            placeholder="Search courses, topics, or tags..."
            value={search}
            onChange={e => setSearch(e.target.value)}
            className="w-full pl-12 pr-4 py-3 bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-2xl outline-none focus:ring-2 focus:ring-accent-500 transition-all dark:text-slate-100 shadow-sm"
          />
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
        {filteredCourses.length > 0 ? filteredCourses.map((course) => (
          <motion.div 
            key={course.id}
            whileHover={{ y: -5 }}
            className="bg-white dark:bg-slate-800 rounded-3xl border border-slate-200 dark:border-slate-700 shadow-sm overflow-hidden flex flex-col group"
          >
            <div className="h-48 bg-slate-100 dark:bg-slate-900 relative overflow-hidden">
              {course.thumbnail ? (
                <img src={course.thumbnail} alt={course.title} className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500" />
              ) : (
                <div className="w-full h-full flex items-center justify-center text-slate-400">
                  <BookOpen size={48} />
                </div>
              )}
              <div className="absolute top-4 right-4">
                <span className="bg-white/90 dark:bg-slate-800/90 backdrop-blur-md text-[10px] font-bold px-2 py-1 rounded-lg uppercase tracking-wider text-slate-900 dark:text-slate-50 border border-slate-200 dark:border-slate-700">
                  {course.difficulty}
                </span>
              </div>
            </div>
            
            <div className="p-6 flex-1 flex flex-col">
              <div className="flex items-center gap-2 mb-3">
                {course.tags.split(',').slice(0, 2).map((tag, i) => (
                  <span key={i} className="text-[10px] font-bold px-2 py-1 bg-accent-50 dark:bg-accent-900/20 text-accent-600 dark:text-accent-400 rounded-md uppercase tracking-widest">
                    {tag.trim()}
                  </span>
                ))}
              </div>
              <h2 className="text-xl font-bold text-slate-900 dark:text-slate-50 mb-2 group-hover:text-accent-600 transition-colors">{course.title}</h2>
              <p className="text-sm text-slate-500 dark:text-slate-400 line-clamp-2 mb-6 flex-1">{course.description}</p>
              
              <div className="flex items-center justify-between pt-6 border-t border-slate-100 dark:border-slate-700">
                <div className="flex items-center gap-2">
                  <div className="w-8 h-8 rounded-full bg-slate-100 dark:bg-slate-900 flex items-center justify-center text-slate-600 dark:text-slate-400 font-bold text-xs">
                    {course.tutor_name.charAt(0)}
                  </div>
                  <span className="text-xs font-bold text-slate-700 dark:text-slate-300">{course.tutor_name}</span>
                </div>
                <div className="text-right">
                  <p className="text-lg font-bold text-slate-900 dark:text-slate-50">
                    {course.price === 0 ? 'Free' : `$${course.price}`}
                  </p>
                </div>
              </div>

              <Link 
                to={`/courses/${course.id}`}
                className="mt-6 w-full py-3 bg-slate-900 dark:bg-slate-700 hover:bg-accent-600 dark:hover:bg-accent-600 text-white font-bold rounded-2xl text-center text-sm transition-all flex items-center justify-center gap-2"
              >
                View Course <ArrowRight size={16} />
              </Link>
            </div>
          </motion.div>
        )) : (
          <div className="col-span-full py-20 text-center">
            <div className="w-20 h-20 bg-slate-50 dark:bg-slate-900 rounded-full flex items-center justify-center mx-auto mb-6 text-slate-400">
              <Search size={40} />
            </div>
            <h2 className="text-xl font-bold text-slate-900 dark:text-slate-50 mb-2">No Courses Found</h2>
            <p className="text-slate-500 dark:text-slate-400">Try adjusting your search or filters to find what you're looking for.</p>
          </div>
        )}
      </div>
    </div>
  );
};
