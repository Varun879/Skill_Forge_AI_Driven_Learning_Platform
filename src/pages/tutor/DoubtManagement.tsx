import React, { useState, useEffect } from 'react';
import { 
  MessageSquare, 
  Send, 
  CheckCircle, 
  Clock,
  User,
  BookOpen,
  HelpCircle,
  ChevronRight
} from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';
import api from '../../services/api';

interface Doubt {
  id: number;
  studentName: string;
  courseTitle: string;
  problemTitle: string | null;
  question: string;
  answer: string | null;
  status: string;
  created_at: string;
}

export const DoubtManagement = () => {
  const [doubts, setDoubts] = useState<Doubt[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedDoubt, setSelectedDoubt] = useState<Doubt | null>(null);
  const [answer, setAnswer] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    const fetchDoubts = async () => {
      try {
        const res = await api.get('/tutor/doubts');
        setDoubts(res.data);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchDoubts();
  }, []);

  const handleReply = async () => {
    if (!selectedDoubt || !answer) return;
    setSubmitting(true);
    try {
      await api.post(`/doubts/${selectedDoubt.id}/reply`, { answer });
      setDoubts(doubts.map(d => 
        d.id === selectedDoubt.id ? { ...d, answer, status: 'resolved' } : d
      ));
      setSelectedDoubt(null);
      setAnswer('');
    } catch (err) {
      console.error(err);
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <div className="flex items-center justify-center h-96">Loading...</div>;

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-3xl font-bold text-slate-900 dark:text-slate-50">Doubt Management</h1>
        <p className="text-slate-500 dark:text-slate-400 mt-1">Help your students by answering their questions and resolving doubts.</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-1 bg-white dark:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700 shadow-sm overflow-hidden flex flex-col h-[calc(100vh-250px)]">
          <div className="p-4 border-b border-slate-100 dark:border-slate-700 bg-slate-50/50 dark:bg-slate-900/50">
            <h2 className="text-sm font-bold text-slate-900 dark:text-slate-50 uppercase tracking-widest">Active Doubts</h2>
          </div>
          <div className="flex-1 overflow-y-auto divide-y divide-slate-100 dark:divide-slate-700 custom-scrollbar">
            {doubts.map((doubt) => (
              <button 
                key={doubt.id}
                onClick={() => setSelectedDoubt(doubt)}
                className={`w-full p-4 text-left transition-colors flex items-center justify-between group ${
                  selectedDoubt?.id === doubt.id ? 'bg-accent-50 dark:bg-accent-900/20' : 'hover:bg-slate-50 dark:hover:bg-slate-700/50'
                }`}
              >
                <div className="flex items-center gap-3">
                  <div className={`w-2 h-2 rounded-full ${doubt.status === 'resolved' ? 'bg-emerald-500' : 'bg-amber-500'}`} />
                  <div>
                    <p className="text-sm font-bold text-slate-900 dark:text-slate-50">{doubt.studentName}</p>
                    <p className="text-xs text-slate-500 dark:text-slate-400 truncate max-w-[150px]">{doubt.courseTitle}</p>
                  </div>
                </div>
                <ChevronRight size={16} className={`text-slate-300 transition-transform ${selectedDoubt?.id === doubt.id ? 'translate-x-1 text-accent-600' : 'group-hover:translate-x-1'}`} />
              </button>
            ))}
          </div>
        </div>

        <div className="lg:col-span-2 space-y-6">
          <AnimatePresence mode="wait">
            {selectedDoubt ? (
              <motion.div 
                key={selectedDoubt.id}
                initial={{ opacity: 0, x: 20 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: -20 }}
                className="bg-white dark:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700 shadow-sm overflow-hidden flex flex-col h-[calc(100vh-250px)]"
              >
                <div className="p-6 border-b border-slate-100 dark:border-slate-700 flex items-center justify-between">
                  <div className="flex items-center gap-4">
                    <div className="w-12 h-12 rounded-full bg-slate-100 dark:bg-slate-900 flex items-center justify-center text-slate-600 dark:text-slate-400 font-bold text-lg">
                      {selectedDoubt.studentName.charAt(0)}
                    </div>
                    <div>
                      <h2 className="text-lg font-bold text-slate-900 dark:text-slate-50">{selectedDoubt.studentName}</h2>
                      <p className="text-sm text-slate-500 dark:text-slate-400">Course: <span className="text-accent-600 font-bold">{selectedDoubt.courseTitle}</span></p>
                    </div>
                  </div>
                  <div className="text-right">
                    <span className={`text-xs font-bold px-3 py-1 rounded-full uppercase tracking-wider ${
                      selectedDoubt.status === 'resolved' ? 'bg-emerald-100 text-emerald-600' : 'bg-amber-100 text-amber-600'
                    }`}>
                      {selectedDoubt.status}
                    </span>
                    <p className="text-xs text-slate-400 mt-2">{new Date(selectedDoubt.created_at).toLocaleString()}</p>
                  </div>
                </div>

                <div className="flex-1 overflow-y-auto p-6 space-y-6 custom-scrollbar">
                  <div className="p-4 bg-slate-50 dark:bg-slate-900 rounded-2xl border border-slate-100 dark:border-slate-800">
                    <h3 className="text-sm font-bold text-slate-900 dark:text-slate-50 flex items-center gap-2 mb-3">
                      <HelpCircle size={18} className="text-amber-500" />
                      Student Question
                    </h3>
                    <p className="text-sm text-slate-600 dark:text-slate-300 leading-relaxed">
                      {selectedDoubt.question}
                    </p>
                    {selectedDoubt.problemTitle && (
                      <div className="mt-4 pt-4 border-t border-slate-200 dark:border-slate-700">
                        <p className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Related Problem</p>
                        <p className="text-xs font-bold text-accent-600">{selectedDoubt.problemTitle}</p>
                      </div>
                    )}
                  </div>

                  {selectedDoubt.answer && (
                    <div className="p-4 bg-emerald-50 dark:bg-emerald-900/20 border border-emerald-100 dark:border-emerald-800 rounded-xl">
                      <h3 className="text-sm font-bold text-emerald-700 dark:text-emerald-400 flex items-center gap-2 mb-2">
                        <CheckCircle size={16} />
                        Your Answer
                      </h3>
                      <p className="text-sm text-emerald-600 dark:text-emerald-300 italic">"{selectedDoubt.answer}"</p>
                    </div>
                  )}
                </div>

                {!selectedDoubt.answer && (
                  <div className="p-6 border-t border-slate-100 dark:border-slate-700 bg-slate-50/50 dark:bg-slate-900/50">
                    <div className="space-y-4">
                      <div className="space-y-2">
                        <label className="text-sm font-bold text-slate-900 dark:text-slate-50 flex items-center gap-2">
                          <MessageSquare size={16} className="text-accent-600" />
                          Reply to Student
                        </label>
                        <textarea 
                          value={answer}
                          onChange={e => setAnswer(e.target.value)}
                          placeholder="Type your answer here..."
                          rows={3}
                          className="w-full px-4 py-3 bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl outline-none dark:text-slate-100 resize-none text-sm"
                        />
                      </div>
                      <button 
                        onClick={handleReply}
                        disabled={submitting || !answer}
                        className="w-full py-3 bg-accent-600 hover:bg-accent-700 text-white font-bold rounded-xl transition-all flex items-center justify-center gap-2 disabled:opacity-50"
                      >
                        <Send size={18} />
                        {submitting ? 'Sending Reply...' : 'Send Reply'}
                      </button>
                    </div>
                  </div>
                )}
              </motion.div>
            ) : (
              <div className="bg-white dark:bg-slate-800 rounded-3xl border-2 border-dashed border-slate-200 dark:border-slate-700 p-20 text-center h-full flex flex-col items-center justify-center">
                <div className="w-20 h-20 bg-slate-50 dark:bg-slate-900 rounded-full flex items-center justify-center mb-6 text-slate-400">
                  <MessageSquare size={40} />
                </div>
                <h2 className="text-xl font-bold text-slate-900 dark:text-slate-50 mb-2">Select a Doubt</h2>
                <p className="text-slate-500 dark:text-slate-400 max-w-xs mx-auto">Choose a student question from the list on the left to provide an answer and help them progress.</p>
              </div>
            )}
          </AnimatePresence>
        </div>
      </div>
    </div>
  );
};
