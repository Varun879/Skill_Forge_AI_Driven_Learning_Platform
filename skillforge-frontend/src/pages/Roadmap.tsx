import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { Lock, CheckCircle2, PlayCircle, ChevronRight, Zap, ChevronDown, BookOpen, Target } from 'lucide-react';
import { Link } from 'react-router-dom';
import api from '../services/api';

const cn = (...classes: any[]) => classes.filter(Boolean).join(' ');

const RoadmapTopic = ({ topic, delay }: any) => {
  const [isExpanded, setIsExpanded] = useState(topic.order_index === 1);
  const completionPercent = topic.subtopics.length > 0 ? (topic.subtopics.filter((s: any) => !s.locked).length / topic.subtopics.length) * 100 : 0;

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ delay }}
      className="bg-white dark:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700 overflow-hidden shadow-sm dark:shadow-xl dark:shadow-black/20 mb-6"
    >
      <button 
        onClick={() => setIsExpanded(!isExpanded)}
        className="w-full flex items-center justify-between p-6 hover:bg-slate-50 dark:hover:bg-slate-700/50 transition-all"
      >
        <div className="flex items-center gap-6">
          <div className="w-12 h-12 bg-accent-50 dark:bg-accent-900/20 rounded-xl flex items-center justify-center text-accent-600">
            <BookOpen size={24} />
          </div>
          <div className="text-left">
            <h3 className="text-lg font-bold text-slate-900 dark:text-slate-50">{topic.title}</h3>
            <p className="text-xs text-slate-500 dark:text-slate-400 font-medium">{topic.description}</p>
          </div>
        </div>
        <div className="flex items-center gap-6">
          <div className="hidden sm:flex flex-col items-end gap-1">
            <div className="flex items-center gap-2">
              <span className="text-[10px] font-bold text-accent-600 dark:text-accent-400">{Math.round(completionPercent)}% Complete</span>
            </div>
            <div className="w-24 h-1.5 bg-slate-100 dark:bg-slate-900 rounded-full overflow-hidden">
              <div className="h-full bg-accent-500 rounded-full" style={{ width: `${completionPercent}%` }} />
            </div>
          </div>
          <ChevronDown size={20} className={cn("text-slate-400 transition-transform duration-300", isExpanded && "rotate-180")} />
        </div>
      </button>

      <AnimatePresence>
        {isExpanded && (
          <motion.div
            initial={{ height: 0, opacity: 0 }}
            animate={{ height: 'auto', opacity: 1 }}
            exit={{ height: 0, opacity: 0 }}
            className="border-t border-slate-100 dark:border-slate-700"
          >
            <div className="p-6 space-y-8">
              {topic.subtopics.map((sub: any, idx: number) => (
                <div key={sub.id} className="relative pl-8">
                  {/* Vertical line connector */}
                  {idx < topic.subtopics.length - 1 && (
                    <div className="absolute left-[11px] top-6 bottom-[-32px] w-0.5 bg-slate-100 dark:bg-slate-700" />
                  )}
                  
                  <div className="flex items-start gap-4">
                    <div className={cn(
                      "absolute left-0 w-6 h-6 rounded-full border-2 flex items-center justify-center z-10",
                      sub.locked 
                        ? "bg-slate-100 dark:bg-slate-800 border-slate-200 dark:border-slate-700 text-slate-400" 
                        : "bg-white dark:bg-slate-800 border-accent-500 text-accent-600"
                    )}>
                      {sub.locked ? <Lock size={12} /> : <div className="w-2 h-2 rounded-full bg-accent-500" />}
                    </div>
                    
                    <div className="flex-1">
                      <div className="flex items-center justify-between mb-2">
                        <h4 className={cn("font-bold text-sm", sub.locked ? "text-slate-400 dark:text-slate-600" : "text-slate-900 dark:text-slate-50")}>
                          {sub.title}
                        </h4>
                        {sub.locked && (
                          <span className="text-[10px] font-bold uppercase tracking-widest text-slate-400 bg-slate-100 dark:bg-slate-900 px-2 py-0.5 rounded">Locked</span>
                        )}
                      </div>
                      <p className="text-xs text-slate-500 dark:text-slate-400">{sub.description}</p>
                      
                      {!sub.locked && (
                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 mt-4">
                          {sub.problems.map((p: any) => (
                            <Link 
                              key={p.id}
                              to={`/practice/${p.id}`}
                              className="flex items-center justify-between p-3 rounded-xl bg-slate-50 dark:bg-slate-900/50 hover:bg-accent-50 dark:hover:bg-accent-900/20 border border-slate-100 dark:border-slate-700 transition-all group"
                            >
                              <div className="flex items-center gap-3">
                                <div className="w-8 h-8 rounded-lg bg-white dark:bg-slate-800 flex items-center justify-center text-accent-600 border border-slate-100 dark:border-slate-700 group-hover:border-accent-200">
                                  <Target size={14} />
                                </div>
                                <div>
                                  <p className="text-xs font-bold text-slate-900 dark:text-slate-50 group-hover:text-accent-700 dark:group-hover:text-accent-400">{p.title}</p>
                                  <p className="text-[10px] text-slate-500 dark:text-slate-400 font-bold uppercase tracking-widest">{p.difficulty}</p>
                                </div>
                              </div>
                              <ChevronRight size={14} className="text-slate-300 group-hover:text-accent-500 group-hover:translate-x-1 transition-all" />
                            </Link>
                          ))}
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </motion.div>
  );
};

export const Roadmap = () => {
  const [roadmap, setRoadmap] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchRoadmap = async () => {
      try {
        const res = await api.get('/roadmap');
        setRoadmap(res.data);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchRoadmap();
  }, []);

  if (loading) return (
    <div className="h-[calc(100vh-200px)] flex items-center justify-center">
      <div className="w-10 h-10 border-4 border-sage-600 border-t-transparent rounded-full animate-spin"></div>
    </div>
  );

  return (
    <div className="max-w-4xl mx-auto space-y-12">
      <header className="text-center">
        <h1 className="text-4xl font-bold tracking-tight mb-4 text-slate-900 dark:text-slate-50">Your Adaptive Roadmap</h1>
        <p className="text-slate-500 dark:text-slate-400 max-w-xl mx-auto text-sm font-medium">
          SkillForge analyzes your performance to create a personalized path. Complete modules to unlock advanced topics and master the curriculum.
        </p>
      </header>

      <div className="bg-accent-600 rounded-3xl p-8 text-white flex flex-col md:flex-row items-center justify-between gap-8 shadow-xl shadow-accent-200 dark:shadow-none">
        <div className="flex items-center gap-6">
          <div className="w-16 h-16 bg-white/20 backdrop-blur-md rounded-2xl flex items-center justify-center">
            <Zap size={32} fill="currentColor" />
          </div>
          <div>
            <h2 className="text-2xl font-bold">Overall Progress</h2>
            <p className="text-accent-100 text-sm font-medium">You've mastered 1 out of 8 core modules.</p>
          </div>
        </div>
        <div className="flex flex-col items-end gap-2 w-full md:w-auto">
          <span className="text-sm font-bold">12.5% Complete</span>
          <div className="w-full md:w-64 h-3 bg-white/20 rounded-full overflow-hidden">
            <div className="h-full bg-white w-[12.5%] rounded-full" />
          </div>
        </div>
      </div>

      <div className="space-y-4">
        {roadmap.map((topic, idx) => (
          <RoadmapTopic key={topic.id} topic={topic} delay={idx * 0.1} />
        ))}
      </div>
    </div>
  );
};
