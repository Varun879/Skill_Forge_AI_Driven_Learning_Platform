import React, { useState, useEffect } from 'react';
import { 
  CheckCircle, 
  XCircle, 
  MessageSquare, 
  Code2, 
  Clock,
  User,
  Send,
  ChevronRight
} from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';
import api from '../../services/api';

interface Submission {
  id: number;
  studentName: string;
  problemTitle: string;
  code: string;
  language: string;
  status: string;
  score: number;
  feedback: string | null;
  created_at: string;
}

export const SubmissionsReview = () => {
  const [submissions, setSubmissions] = useState<Submission[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedSubmission, setSelectedSubmission] = useState<Submission | null>(null);
  const [feedback, setFeedback] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    const fetchSubmissions = async () => {
      try {
        const res = await api.get('/tutor/submissions');
        setSubmissions(res.data);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchSubmissions();
  }, []);

  const handleReview = async () => {
    if (!selectedSubmission || !feedback) return;
    setSubmitting(true);
    try {
      await api.post(`/submissions/${selectedSubmission.id}/review`, { feedback });
      setSubmissions(submissions.map(s => 
        s.id === selectedSubmission.id ? { ...s, feedback } : s
      ));
      setSelectedSubmission(null);
      setFeedback('');
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
        <h1 className="text-3xl font-bold text-slate-900 dark:text-slate-50">Submissions Review</h1>
        <p className="text-slate-500 dark:text-slate-400 mt-1">Review student solutions and provide constructive feedback.</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-1 bg-white dark:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700 shadow-sm overflow-hidden flex flex-col h-[calc(100vh-250px)]">
          <div className="p-4 border-b border-slate-100 dark:border-slate-700 bg-slate-50/50 dark:bg-slate-900/50">
            <h2 className="text-sm font-bold text-slate-900 dark:text-slate-50 uppercase tracking-widest">Recent Submissions</h2>
          </div>
          <div className="flex-1 overflow-y-auto divide-y divide-slate-100 dark:divide-slate-700 custom-scrollbar">
            {submissions.map((sub) => (
              <button 
                key={sub.id}
                onClick={() => setSelectedSubmission(sub)}
                className={`w-full p-4 text-left transition-colors flex items-center justify-between group ${
                  selectedSubmission?.id === sub.id ? 'bg-accent-50 dark:bg-accent-900/20' : 'hover:bg-slate-50 dark:hover:bg-slate-700/50'
                }`}
              >
                <div className="flex items-center gap-3">
                  <div className={`w-2 h-2 rounded-full ${sub.feedback ? 'bg-emerald-500' : 'bg-amber-500'}`} />
                  <div>
                    <p className="text-sm font-bold text-slate-900 dark:text-slate-50">{sub.studentName}</p>
                    <p className="text-xs text-slate-500 dark:text-slate-400 truncate max-w-[150px]">{sub.problemTitle}</p>
                  </div>
                </div>
                <ChevronRight size={16} className={`text-slate-300 transition-transform ${selectedSubmission?.id === sub.id ? 'translate-x-1 text-accent-600' : 'group-hover:translate-x-1'}`} />
              </button>
            ))}
          </div>
        </div>

        <div className="lg:col-span-2 space-y-6">
          <AnimatePresence mode="wait">
            {selectedSubmission ? (
              <motion.div 
                key={selectedSubmission.id}
                initial={{ opacity: 0, x: 20 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: -20 }}
                className="bg-white dark:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700 shadow-sm overflow-hidden flex flex-col h-[calc(100vh-250px)]"
              >
                <div className="p-6 border-b border-slate-100 dark:border-slate-700 flex items-center justify-between">
                  <div className="flex items-center gap-4">
                    <div className="w-12 h-12 rounded-full bg-slate-100 dark:bg-slate-900 flex items-center justify-center text-slate-600 dark:text-slate-400 font-bold text-lg">
                      {selectedSubmission.studentName.charAt(0)}
                    </div>
                    <div>
                      <h2 className="text-lg font-bold text-slate-900 dark:text-slate-50">{selectedSubmission.studentName}</h2>
                      <p className="text-sm text-slate-500 dark:text-slate-400">Problem: <span className="text-accent-600 font-bold">{selectedSubmission.problemTitle}</span></p>
                    </div>
                  </div>
                  <div className="text-right">
                    <span className={`text-xs font-bold px-3 py-1 rounded-full uppercase tracking-wider ${
                      selectedSubmission.status === 'Accepted' ? 'bg-emerald-100 text-emerald-600' : 'bg-rose-100 text-rose-600'
                    }`}>
                      {selectedSubmission.status}
                    </span>
                    <p className="text-xs text-slate-400 mt-2">{new Date(selectedSubmission.created_at).toLocaleString()}</p>
                  </div>
                </div>

                <div className="flex-1 overflow-y-auto p-6 space-y-6 custom-scrollbar">
                  <div className="space-y-2">
                    <div className="flex items-center justify-between">
                      <h3 className="text-sm font-bold text-slate-900 dark:text-slate-50 flex items-center gap-2">
                        <Code2 size={16} className="text-accent-600" />
                        Student Solution ({selectedSubmission.language})
                      </h3>
                    </div>
                    <pre className="p-4 bg-slate-900 text-slate-100 rounded-xl text-xs font-mono overflow-x-auto">
                      {selectedSubmission.code}
                    </pre>
                  </div>

                  {selectedSubmission.feedback && (
                    <div className="p-4 bg-emerald-50 dark:bg-emerald-900/20 border border-emerald-100 dark:border-emerald-800 rounded-xl">
                      <h3 className="text-sm font-bold text-emerald-700 dark:text-emerald-400 flex items-center gap-2 mb-2">
                        <CheckCircle size={16} />
                        Your Feedback
                      </h3>
                      <p className="text-sm text-emerald-600 dark:text-emerald-300 italic">"{selectedSubmission.feedback}"</p>
                    </div>
                  )}
                </div>

                {!selectedSubmission.feedback && (
                  <div className="p-6 border-t border-slate-100 dark:border-slate-700 bg-slate-50/50 dark:bg-slate-900/50">
                    <div className="space-y-4">
                      <div className="space-y-2">
                        <label className="text-sm font-bold text-slate-900 dark:text-slate-50 flex items-center gap-2">
                          <MessageSquare size={16} className="text-accent-600" />
                          Add Review Feedback
                        </label>
                        <textarea 
                          value={feedback}
                          onChange={e => setFeedback(e.target.value)}
                          placeholder="Great job! Consider optimizing the space complexity by..."
                          rows={3}
                          className="w-full px-4 py-3 bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl outline-none dark:text-slate-100 resize-none text-sm"
                        />
                      </div>
                      <button 
                        onClick={handleReview}
                        disabled={submitting || !feedback}
                        className="w-full py-3 bg-accent-600 hover:bg-accent-700 text-white font-bold rounded-xl transition-all flex items-center justify-center gap-2 disabled:opacity-50"
                      >
                        <Send size={18} />
                        {submitting ? 'Submitting Review...' : 'Submit Review & Feedback'}
                      </button>
                    </div>
                  </div>
                )}
              </motion.div>
            ) : (
              <div className="bg-white dark:bg-slate-800 rounded-3xl border-2 border-dashed border-slate-200 dark:border-slate-700 p-20 text-center h-full flex flex-col items-center justify-center">
                <div className="w-20 h-20 bg-slate-50 dark:bg-slate-900 rounded-full flex items-center justify-center mb-6 text-slate-400">
                  <Code2 size={40} />
                </div>
                <h2 className="text-xl font-bold text-slate-900 dark:text-slate-50 mb-2">Select a Submission</h2>
                <p className="text-slate-500 dark:text-slate-400 max-w-xs mx-auto">Choose a student submission from the list on the left to review their code and provide feedback.</p>
              </div>
            )}
          </AnimatePresence>
        </div>
      </div>
    </div>
  );
};
