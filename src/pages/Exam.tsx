import React, { useState, useEffect } from 'react';
import Editor from '@monaco-editor/react';
import { 
  Clock, 
  Shield, 
  Send, 
  MessageSquare, 
  AlertTriangle,
  ChevronRight,
  ChevronLeft,
  Zap,
  Terminal,
  XCircle
} from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';
import Markdown from 'react-markdown';
import api from '../services/api';

export const Exam = () => {
  const [timeLeft, setTimeLeft] = useState(3600); // 1 hour
  const [currentQuestion, setCurrentQuestion] = useState(1);
  const [code, setCode] = useState('// Write your solution here\n\nfunction solve() {\n  \n}');
  const [isExamStarted, setIsExamStarted] = useState(false);
  const [showWarning, setShowWarning] = useState(false);
  const [problems, setProblems] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [chatMessages, setChatMessages] = useState([
    { role: 'ai', text: 'Hello! I am your AI proctor. You can ask for a maximum of 3 hints during this exam. Good luck!' }
  ]);
  const [hintCount, setHintCount] = useState(0);
  const [showSubmitModal, setShowSubmitModal] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    const fetchProblems = async () => {
      setLoading(true);
      try {
        const res = await api.get('/problems');
        setProblems(res.data.slice(0, 3));
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchProblems();
  }, []);

  useEffect(() => {
    if (isExamStarted && timeLeft > 0) {
      const timer = setInterval(() => setTimeLeft(prev => prev - 1), 1000);
      return () => clearInterval(timer);
    }
  }, [isExamStarted, timeLeft]);

  // Autosave every 30 seconds
  useEffect(() => {
    if (isExamStarted) {
      const autosave = setInterval(() => {
        console.log('Autosaving code...');
      }, 30000);
      return () => clearInterval(autosave);
    }
  }, [isExamStarted, code]);

  useEffect(() => {
    const handleVisibilityChange = () => {
      if (document.hidden && isExamStarted) {
        setShowWarning(true);
      }
    };
    document.addEventListener('visibilitychange', handleVisibilityChange);
    return () => document.removeEventListener('visibilitychange', handleVisibilityChange);
  }, [isExamStarted]);

  const formatTime = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  const handleAskAI = () => {
    if (hintCount >= 3) return;
    setHintCount(prev => prev + 1);
    setChatMessages(prev => [
      ...prev,
      { role: 'user', text: 'Can I get a hint?' },
      { role: 'ai', text: 'Think about the time complexity. A nested loop might be too slow for the given constraints. Try using a hash map to store seen values.' }
    ]);
  };

  const handleFinishExam = () => {
    setShowSubmitModal(true);
  };

  const confirmSubmit = async () => {
    setIsSubmitting(true);
    await new Promise(resolve => setTimeout(resolve, 2000));
    setIsSubmitting(false);
    setShowSubmitModal(false);
    setIsExamStarted(false);
  };

  const cn = (...classes: any[]) => classes.filter(Boolean).join(' ');

  if (!isExamStarted) {
    return (
      <div className="max-w-2xl mx-auto py-12">
        <div className="bg-white dark:bg-slate-800 p-10 rounded-3xl border border-slate-200 dark:border-slate-700 shadow-xl dark:shadow-black/20 text-center">
          <div className="w-20 h-20 bg-accent-50 dark:bg-accent-900/20 rounded-full flex items-center justify-center mx-auto mb-8 text-accent-600">
            <Shield size={40} />
          </div>
          <h1 className="text-3xl font-bold mb-4 text-slate-900 dark:text-slate-50">Certification Exam</h1>
          <p className="text-slate-500 dark:text-slate-400 mb-8 text-sm font-medium">
            This is a proctored exam. Switching tabs or leaving the window will be flagged. You have 60 minutes to solve 3 algorithmic problems. Your progress is autosaved every 30 seconds.
          </p>
          
          <div className="grid grid-cols-2 gap-6 mb-10 text-left">
            <div className="p-4 bg-slate-50 dark:bg-slate-900 rounded-2xl border border-slate-100 dark:border-slate-800">
              <p className="text-[10px] font-bold text-slate-500 dark:text-slate-400 uppercase tracking-widest mb-1">Duration</p>
              <p className="font-bold text-slate-900 dark:text-slate-50">60 Minutes</p>
            </div>
            <div className="p-4 bg-slate-50 dark:bg-slate-900 rounded-2xl border border-slate-100 dark:border-slate-800">
              <p className="text-[10px] font-bold text-slate-500 dark:text-slate-400 uppercase tracking-widest mb-1">Questions</p>
              <p className="font-bold text-slate-900 dark:text-slate-50">3 Problems</p>
            </div>
            <div className="p-4 bg-slate-50 dark:bg-slate-900 rounded-2xl border border-slate-100 dark:border-slate-800">
              <p className="text-[10px] font-bold text-slate-500 dark:text-slate-400 uppercase tracking-widest mb-1">AI Hints</p>
              <p className="font-bold text-slate-900 dark:text-slate-50">3 Max</p>
            </div>
            <div className="p-4 bg-slate-50 dark:bg-slate-900 rounded-2xl border border-slate-100 dark:border-slate-800">
              <p className="text-[10px] font-bold text-slate-500 dark:text-slate-400 uppercase tracking-widest mb-1">Difficulty</p>
              <p className="font-bold text-slate-900 dark:text-slate-50">Adaptive</p>
            </div>
          </div>

          <button 
            onClick={() => setIsExamStarted(true)}
            className="w-full py-4 bg-accent-600 hover:bg-accent-700 text-white font-bold rounded-2xl shadow-lg shadow-accent-200 dark:shadow-none transition-all"
          >
            Start Secure Exam Session
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="h-[calc(100vh-120px)] flex flex-col gap-6">
      <AnimatePresence>
        {showWarning && (
          <motion.div 
            initial={{ opacity: 0, y: -20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -20 }}
            className="fixed top-6 left-1/2 -translate-x-1/2 z-[100] w-full max-w-md"
          >
            <div className="bg-rose-600 text-white p-4 rounded-2xl shadow-2xl flex items-center justify-between">
              <div className="flex items-center gap-3">
                <AlertTriangle size={24} />
                <div>
                  <p className="font-bold">Security Warning</p>
                  <p className="text-xs opacity-90">Tab switching detected. This incident has been logged.</p>
                </div>
              </div>
              <button onClick={() => setShowWarning(false)} className="p-1 hover:bg-white/20 rounded-lg">
                <XCircle size={20} />
              </button>
            </div>
          </motion.div>
        )}

        {showSubmitModal && (
          <div className="fixed inset-0 z-[110] flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm">
            <motion.div 
              initial={{ scale: 0.9, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              className="bg-white dark:bg-slate-800 p-8 rounded-3xl border border-slate-200 dark:border-slate-700 shadow-2xl max-w-sm w-full text-center"
            >
              <div className="w-16 h-16 bg-rose-50 dark:bg-rose-900/20 rounded-full flex items-center justify-center mx-auto mb-6 text-rose-600">
                <AlertTriangle size={32} />
              </div>
              <h3 className="text-xl font-bold mb-2 text-slate-900 dark:text-slate-50">Finish Exam?</h3>
              <p className="text-sm text-slate-500 dark:text-slate-400 mb-8 font-medium">Are you sure you want to submit your solutions? You cannot return to the exam after this.</p>
              <div className="flex gap-4">
                <button 
                  onClick={() => setShowSubmitModal(false)}
                  className="flex-1 py-3 bg-slate-100 dark:bg-slate-900 text-slate-900 dark:text-slate-100 font-bold rounded-xl border border-slate-200 dark:border-slate-700"
                >
                  Cancel
                </button>
                <button 
                  onClick={confirmSubmit}
                  disabled={isSubmitting}
                  className="flex-1 py-3 bg-accent-600 text-white font-bold rounded-xl disabled:opacity-50"
                >
                  {isSubmitting ? 'Submitting...' : 'Yes, Finish'}
                </button>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      <header className="flex items-center justify-between bg-white dark:bg-slate-800 p-4 rounded-2xl border border-slate-200 dark:border-slate-700 shadow-sm dark:shadow-xl dark:shadow-black/20">
        <div className="flex items-center gap-6">
          <div className="flex items-center gap-2 px-4 py-2 bg-slate-50 dark:bg-slate-900 rounded-xl font-bold">
            <Clock size={18} className="text-accent-600" />
            <span className={cn("font-mono", timeLeft < 300 ? 'text-rose-600 animate-pulse' : 'text-slate-900 dark:text-slate-50')}>
              {formatTime(timeLeft)}
            </span>
          </div>
          <div className="h-6 w-px bg-slate-200 dark:bg-slate-700" />
          <div className="flex items-center gap-2">
            {problems.map((_, idx) => (
              <button 
                key={idx}
                onClick={() => setCurrentQuestion(idx + 1)}
                className={cn(
                  "w-10 h-10 rounded-xl font-bold transition-all",
                  currentQuestion === idx + 1 
                    ? "bg-accent-600 text-white shadow-lg shadow-accent-200" 
                    : "bg-slate-50 dark:bg-slate-900 text-slate-500 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-700"
                )}
              >
                {idx + 1}
              </button>
            ))}
          </div>
        </div>
        
        <div className="flex items-center gap-4">
          <div className="hidden sm:flex items-center gap-2 px-3 py-1.5 bg-accent-50 dark:bg-accent-900/20 text-accent-600 dark:text-accent-400 rounded-lg text-[10px] font-bold border border-accent-100 dark:border-accent-800 uppercase tracking-widest">
            <Shield size={14} />
            Secure Session
          </div>
          <button 
            onClick={handleFinishExam}
            className="px-6 py-2 bg-rose-600 hover:bg-rose-700 text-white font-bold rounded-xl transition-all shadow-md shadow-rose-200 dark:shadow-none"
          >
            Finish Exam
          </button>
        </div>
      </header>

      <div className="flex-1 flex gap-6 min-h-0">
        <div className="w-1/3 flex flex-col gap-6">
          <div className="flex-1 bg-white dark:bg-slate-800 p-6 rounded-2xl border border-slate-200 dark:border-slate-700 shadow-sm dark:shadow-xl dark:shadow-black/20 overflow-y-auto custom-scrollbar">
            {problems[currentQuestion - 1] ? (
              <>
                <div className="flex items-center justify-between mb-4">
                  <h2 className="text-xl font-bold text-slate-900 dark:text-slate-50">Problem {currentQuestion}</h2>
                  <span className="text-[10px] font-bold px-2 py-1 bg-slate-100 dark:bg-slate-900 text-slate-500 dark:text-slate-400 rounded uppercase tracking-wider border border-slate-200 dark:border-slate-700">
                    {problems[currentQuestion - 1].difficulty}
                  </span>
                </div>
                <h3 className="text-lg font-bold text-slate-900 dark:text-slate-50 mb-4">{problems[currentQuestion - 1].title}</h3>
                <div className="prose dark:prose-invert text-sm max-w-none">
                  <div className="text-slate-900 dark:text-slate-200 leading-relaxed mb-6 font-medium">
                    <Markdown>{problems[currentQuestion - 1].description}</Markdown>
                  </div>
                  
                  {problems[currentQuestion - 1].constraints && (
                    <div className="bg-slate-50 dark:bg-slate-900/50 p-4 rounded-xl border border-slate-100 dark:border-slate-700 mb-6">
                      <h4 className="text-[10px] font-bold uppercase tracking-widest text-slate-500 dark:text-slate-400 mb-3">Constraints</h4>
                      <ul className="space-y-1 text-xs text-slate-600 dark:text-slate-400 font-mono">
                        {problems[currentQuestion - 1].constraints?.map((c: string, i: number) => (
                          <li key={i} className="flex items-center gap-2">
                            <div className="w-1 h-1 rounded-full bg-accent-400" />
                            {c}
                          </li>
                        ))}
                      </ul>
                    </div>
                  )}

                  <h4 className="text-[10px] font-bold uppercase tracking-widest text-slate-500 dark:text-slate-400 mb-3">Example 1</h4>
                  <div className="bg-slate-50 dark:bg-slate-900 p-4 rounded-xl border border-slate-100 dark:border-slate-700 text-xs font-mono">
                    <p className="text-accent-600 dark:text-accent-400 font-bold mb-1">Input:</p>
                    <p className="text-slate-900 dark:text-slate-200 mb-3">nums = [-2, 1, -3, 4, -1, 2, 1, -5, 4]</p>
                    <p className="text-rose-600 dark:text-rose-400 font-bold mb-1">Output:</p>
                    <p className="text-slate-900 dark:text-slate-200">6</p>
                  </div>
                </div>
              </>
            ) : (
              <div className="flex items-center justify-center h-full text-slate-400 dark:text-slate-600 italic">
                Loading problem details...
              </div>
            )}
          </div>

          <div className="h-1/2 bg-white dark:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700 shadow-sm dark:shadow-xl dark:shadow-black/20 flex flex-col overflow-hidden">
            <div className="p-4 border-b border-slate-100 dark:border-slate-700 flex items-center justify-between bg-slate-50/50 dark:bg-slate-900/50">
              <div className="flex items-center gap-2 font-bold text-xs text-slate-900 dark:text-slate-100">
                <MessageSquare size={16} className="text-accent-600" />
                AI Proctor Chat
              </div>
              <span className="text-[10px] font-bold uppercase tracking-widest text-slate-500 dark:text-slate-400">
                Hints: {hintCount}/3
              </span>
            </div>
            <div className="flex-1 overflow-y-auto p-4 space-y-4 custom-scrollbar">
              {chatMessages.map((msg, i) => (
                <div key={i} className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}>
                  <div className={cn(
                    "max-w-[85%] p-3 rounded-2xl text-xs leading-relaxed font-medium",
                    msg.role === 'user' 
                      ? 'bg-accent-600 text-white rounded-tr-none' 
                      : 'bg-slate-100 dark:bg-slate-900 text-slate-900 dark:text-slate-100 rounded-tl-none'
                  )}>
                    {msg.text}
                  </div>
                </div>
              ))}
            </div>
            <div className="p-4 border-t border-slate-100 dark:border-slate-700">
              <button 
                onClick={handleAskAI}
                disabled={hintCount >= 3}
                className="w-full py-2.5 bg-slate-50 dark:bg-slate-900 hover:bg-slate-100 dark:hover:bg-slate-700 text-slate-900 dark:text-slate-100 font-bold rounded-xl text-[10px] uppercase tracking-widest transition-all disabled:opacity-50 flex items-center justify-center gap-2 border border-slate-200 dark:border-slate-700"
              >
                <Zap size={14} className="text-accent-600" />
                Request AI Hint
              </button>
            </div>
          </div>
        </div>

        <div className="flex-1 flex flex-col gap-6">
          <div className="flex-1 bg-white dark:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700 overflow-hidden shadow-sm dark:shadow-xl dark:shadow-black/20">
            <Editor
              height="100%"
              language="javascript"
              theme="vs-dark"
              value={code}
              onChange={(v) => setCode(v || '')}
              options={{
                fontSize: 14,
                minimap: { enabled: false },
                scrollBeyondLastLine: false,
                padding: { top: 20, bottom: 20 },
                fontFamily: "'JetBrains Mono', monospace",
                lineNumbers: 'on',
                automaticLayout: true,
              }}
            />
          </div>
          <div className="h-24 bg-white dark:bg-slate-800 p-4 rounded-2xl border border-slate-200 dark:border-slate-700 shadow-sm dark:shadow-xl dark:shadow-black/20 flex items-center justify-between">
            <div className="flex items-center gap-4">
              <button className="flex items-center gap-2 px-5 py-2.5 bg-slate-100 dark:bg-slate-900 hover:bg-slate-200 dark:hover:bg-slate-700 text-slate-900 dark:text-slate-100 font-bold rounded-xl transition-all border border-slate-200 dark:border-slate-700 text-sm">
                <Terminal size={18} />
                Run Tests
              </button>
            </div>
            <div className="flex items-center gap-3">
              <button 
                onClick={() => setCurrentQuestion(prev => Math.max(1, prev - 1))}
                disabled={currentQuestion === 1}
                className="flex items-center gap-2 px-6 py-2.5 bg-slate-50 dark:bg-slate-900 hover:bg-slate-100 dark:hover:bg-slate-700 text-slate-900 dark:text-slate-100 font-bold rounded-xl transition-all disabled:opacity-30 border border-slate-200 dark:border-slate-700 text-sm"
              >
                <ChevronLeft size={18} />
                Previous
              </button>
              <button 
                onClick={() => setCurrentQuestion(prev => Math.min(problems.length, prev + 1))}
                disabled={currentQuestion === problems.length}
                className="flex items-center gap-2 px-6 py-2.5 bg-accent-600 hover:bg-accent-700 text-white font-bold rounded-xl transition-all shadow-md shadow-accent-200 dark:shadow-none disabled:opacity-30 text-sm"
              >
                Next Problem
                <ChevronRight size={18} />
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

