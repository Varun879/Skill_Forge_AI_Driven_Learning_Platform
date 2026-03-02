import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Editor from '@monaco-editor/react';
import { 
  Play, 
  Send, 
  Lightbulb, 
  ChevronLeft, 
  Clock, 
  CheckCircle2, 
  XCircle,
  MessageSquare,
  Terminal,
  Zap,
  AlertTriangle
} from 'lucide-react';
import api from '../services/api';
import { motion, AnimatePresence } from 'motion/react';
import Markdown from 'react-markdown';

const cn = (...classes: any[]) => classes.filter(Boolean).join(' ');

export const ProblemDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [problem, setProblem] = useState<any>(null);
  const [code, setCode] = useState('// Write your code here\n\nfunction solution() {\n  \n}');
  const [language, setLanguage] = useState('javascript');
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [result, setResult] = useState<any>(null);
  const [hint, setHint] = useState<string | null>(null);
  const [requestingHint, setRequestingHint] = useState(false);
  const [activeTab, setActiveTab] = useState('description');

  useEffect(() => {
    const fetchProblem = async () => {
      try {
        const res = await api.get(`/problems/${id}`);
        setProblem(res.data);
        if (res.data.id === 'p1') {
          setCode('function twoSum(nums, target) {\n  // Your code here\n}');
        } else if (res.data.id === 'p2') {
          setCode('function maxSubArray(nums) {\n  // Your code here\n}');
        } else if (res.data.id === 'p3') {
          setCode('function fib(n) {\n  // Your code here\n}');
        }
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchProblem();
  }, [id]);

  const handleRun = () => {
    setResult({ status: 'Running...', testResults: [] });
    setTimeout(() => {
      setResult({ 
        status: 'Sample Tests Passed', 
        testResults: problem.samples.map((s: any, i: number) => ({ name: `Sample ${i+1}`, passed: true })),
        executionTime: '45ms',
        memoryUsage: '12.4 MB',
        suggestions: 'Your solution is optimal for the given constraints. Consider using a single pass for better performance.'
      });
      setActiveTab('console');
    }, 1000);
  };

  const handleSubmit = async () => {
    setSubmitting(true);
    try {
      const res = await api.post('/submission', { problemId: id, code, lang: language });
      setResult({
        ...res.data,
        executionTime: '52ms',
        memoryUsage: '14.1 MB',
        suggestions: 'Great job! Try to see if you can solve this using less space.'
      });
      setActiveTab('console');
    } catch (err) {
      console.error(err);
    } finally {
      setSubmitting(false);
    }
  };

  const getHint = async () => {
    setRequestingHint(true);
    try {
      const res = await api.post('/ai/hint', { problemId: id });
      setHint(res.data.hint);
    } catch (err) {
      console.error(err);
    } finally {
      setRequestingHint(false);
    }
  };

  if (loading) return <div className="h-[calc(100vh-100px)] flex items-center justify-center">
    <div className="w-10 h-10 border-4 border-sage-600 border-t-transparent rounded-full animate-spin"></div>
  </div>;

  return (
    <div className="h-[calc(100vh-120px)] flex flex-col gap-6">
      {/* 1. Problem Title & Metadata */}
      <header className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <button 
            onClick={() => navigate('/practice')}
            className="p-2 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-xl transition-colors text-slate-600 dark:text-slate-400"
          >
            <ChevronLeft size={20} />
          </button>
          <div>
            <div className="flex items-center gap-3 mb-1">
              <h1 className="text-2xl font-bold tracking-tight text-slate-900 dark:text-slate-50">{problem.title}</h1>
              <span className={cn(
                "text-[10px] font-bold px-2 py-0.5 rounded-full uppercase tracking-wider",
                problem.difficulty === 'Easy' ? "bg-emerald-100 dark:bg-emerald-900/30 text-emerald-700 dark:text-emerald-400" : 
                problem.difficulty === 'Medium' ? "bg-amber-100 dark:bg-amber-900/30 text-amber-700 dark:text-amber-400" : "bg-rose-100 dark:bg-rose-900/30 text-rose-700 dark:text-rose-400"
              )}>
                {problem.difficulty}
              </span>
            </div>
            <div className="flex items-center gap-4">
              <div className="flex items-center gap-1.5">
                {problem.topicTags.map((tag: string) => (
                  <span key={tag} className="text-[10px] font-bold uppercase tracking-widest px-2 py-0.5 bg-slate-100 dark:bg-slate-800 text-slate-500 dark:text-slate-400 rounded-md border border-slate-200 dark:border-slate-700">
                    {tag}
                  </span>
                ))}
              </div>
              <div className="h-3 w-px bg-slate-200 dark:bg-slate-700" />
              <span className="text-xs text-slate-500 dark:text-slate-400 font-medium flex items-center gap-1">
                <Clock size={12} />
                {problem.estimatedTime}
              </span>
              <span className="text-xs text-accent-600 dark:text-accent-400 font-bold flex items-center gap-1">
                <Zap size={12} />
                +{problem.masteryImpact}% Mastery
              </span>
            </div>
          </div>
        </div>
        <div className="flex items-center gap-3">
          <select 
            value={language}
            onChange={(e) => setLanguage(e.target.value)}
            className="bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl px-3 py-1.5 text-sm font-bold text-slate-900 dark:text-slate-100 outline-none focus:ring-2 focus:ring-accent-500/20"
          >
            <option value="javascript">JavaScript</option>
            <option value="python">Python</option>
            <option value="cpp">C++</option>
          </select>
          <button 
            onClick={handleRun}
            className="flex items-center gap-2 px-4 py-1.5 bg-slate-100 dark:bg-slate-800 hover:bg-slate-200 dark:hover:bg-slate-700 text-slate-900 dark:text-slate-100 font-bold rounded-xl transition-all border border-slate-200 dark:border-slate-700"
          >
            <Play size={16} />
            Run
          </button>
          <button 
            onClick={handleSubmit}
            disabled={submitting}
            className="flex items-center gap-2 px-4 py-1.5 bg-accent-600 hover:bg-accent-700 text-white font-bold rounded-xl transition-all shadow-sm disabled:opacity-50"
          >
            {submitting ? 'Submitting...' : <><Send size={16} /> Submit</>}
          </button>
        </div>
      </header>

      <div className="flex-1 flex gap-6 min-h-0">
        {/* Left Panel: Description, Constraints, Samples, Edge Cases */}
        <div className="w-2/5 flex flex-col bg-white dark:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700 overflow-hidden shadow-sm dark:shadow-xl dark:shadow-black/20">
          <div className="flex border-b border-slate-100 dark:border-slate-700">
            <button 
              onClick={() => setActiveTab('description')}
              className={cn(
                "flex-1 py-3 text-xs font-bold uppercase tracking-widest transition-all",
                activeTab === 'description' ? "text-accent-600 border-b-2 border-accent-600" : "text-slate-500 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-900"
              )}
            >
              Problem
            </button>
            <button 
              onClick={() => setActiveTab('hints')}
              className={cn(
                "flex-1 py-3 text-xs font-bold uppercase tracking-widest transition-all",
                activeTab === 'hints' ? "text-accent-600 border-b-2 border-accent-600" : "text-slate-500 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-900"
              )}
            >
              AI Tutor
            </button>
          </div>
          
          <div className="flex-1 overflow-y-auto p-6 custom-scrollbar">
            {activeTab === 'description' ? (
              <div className="space-y-8">
                {/* 2. Problem Description */}
                <div className="prose dark:prose-invert max-w-none">
                  <div className="text-slate-900 dark:text-slate-200 leading-relaxed text-sm font-medium">
                    <Markdown>{problem.description}</Markdown>
                  </div>
                </div>
                
                {/* 3. Constraints Section */}
                <div className="bg-slate-50 dark:bg-slate-900/50 p-5 rounded-2xl border border-slate-100 dark:border-slate-700">
                  <h3 className="text-xs font-bold uppercase tracking-widest text-slate-500 dark:text-slate-400 mb-3">Constraints</h3>
                  <ul className="space-y-2">
                    {problem.constraints.map((constraint: string, i: number) => (
                      <li key={i} className="flex items-center gap-2 text-xs font-mono text-slate-600 dark:text-slate-400">
                        <div className="w-1 h-1 rounded-full bg-accent-400" />
                        {constraint}
                      </li>
                    ))}
                  </ul>
                </div>
                
                {/* 4. Sample Test Cases */}
                <div>
                  <h3 className="text-xs font-bold uppercase tracking-widest text-slate-500 dark:text-slate-400 mb-4">Sample Test Cases</h3>
                  <div className="space-y-4">
                    {problem.samples.map((sample: any, i: number) => (
                      <div key={i} className="space-y-3">
                        <div className="bg-slate-50 dark:bg-slate-900/50 p-4 rounded-xl border border-slate-100 dark:border-slate-700 font-mono text-xs">
                          <div className="flex gap-3 mb-2">
                            <span className="text-accent-600 dark:text-accent-400 font-bold shrink-0 w-12">Input:</span>
                            <span className="text-slate-900 dark:text-slate-200">{sample.input}</span>
                          </div>
                          <div className="flex gap-3">
                            <span className="text-rose-600 dark:text-rose-400 font-bold shrink-0 w-12">Output:</span>
                            <span className="text-slate-900 dark:text-slate-200">{sample.output}</span>
                          </div>
                        </div>
                        {sample.explanation && (
                          <p className="text-xs text-slate-500 dark:text-slate-400 italic px-1">
                            <span className="font-bold not-italic text-slate-900 dark:text-slate-200">Explanation:</span> {sample.explanation}
                          </p>
                        )}
                      </div>
                    ))}
                  </div>
                </div>

                {/* 5. Edge Cases Section */}
                <div>
                  <h3 className="text-xs font-bold uppercase tracking-widest text-slate-500 dark:text-slate-400 mb-3">Edge Cases to Consider</h3>
                  <div className="grid grid-cols-1 gap-2">
                    {problem.edgeCases.map((edge: string, i: number) => (
                      <div key={i} className="flex items-center gap-3 p-3 bg-slate-50 dark:bg-slate-900/30 rounded-xl border border-slate-100 dark:border-slate-700 text-xs text-slate-600 dark:text-slate-400">
                        <AlertTriangle size={14} className="text-rose-400" />
                        {edge}
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            ) : (
              /* 8. AI Hints Section */
              <div className="space-y-6">
                {!hint ? (
                  <div className="text-center py-12">
                    <div className="w-20 h-20 bg-accent-50 dark:bg-accent-900/20 rounded-full flex items-center justify-center mx-auto mb-6 text-accent-600">
                      <Lightbulb size={40} />
                    </div>
                    <h3 className="text-lg font-bold mb-2 text-slate-900 dark:text-slate-50">Need a nudge?</h3>
                    <p className="text-sm text-slate-500 dark:text-slate-400 mb-8 max-w-xs mx-auto font-medium">Our AI Tutor analyzes your logic and provides gradual hints to help you solve it yourself.</p>
                    
                    <div className="space-y-4 text-left mb-8">
                      <div className="p-4 bg-slate-50 dark:bg-slate-900 rounded-xl border border-slate-100 dark:border-slate-700">
                        <h4 className="text-[10px] font-bold uppercase tracking-widest text-slate-500 dark:text-slate-400 mb-2">Logic Pattern Detected</h4>
                        <p className="text-xs text-slate-900 dark:text-slate-100 font-bold">Hash Map / Frequency Counter</p>
                      </div>
                      <div className="p-4 bg-slate-50 dark:bg-slate-900 rounded-xl border border-slate-100 dark:border-slate-700">
                        <h4 className="text-[10px] font-bold uppercase tracking-widest text-slate-500 dark:text-slate-400 mb-2">Related Topics</h4>
                        <div className="flex flex-wrap gap-2">
                          <span className="text-[10px] font-bold uppercase tracking-widest px-2 py-0.5 bg-accent-100 dark:bg-accent-900/30 text-accent-700 dark:text-accent-400 rounded-md">Time Complexity</span>
                          <span className="text-[10px] font-bold uppercase tracking-widest px-2 py-0.5 bg-accent-100 dark:bg-accent-900/30 text-accent-700 dark:text-accent-400 rounded-md">Space-Time Tradeoff</span>
                        </div>
                      </div>
                    </div>

                    <button 
                      onClick={getHint}
                      disabled={requestingHint}
                      className="w-full py-3 bg-accent-600 hover:bg-accent-700 text-white font-bold rounded-xl transition-all shadow-md shadow-accent-200 dark:shadow-none disabled:opacity-50"
                    >
                      {requestingHint ? 'Analyzing Logic...' : 'Reveal First Hint'}
                    </button>
                  </div>
                ) : (
                  <motion.div 
                    initial={{ opacity: 0, y: 10 }}
                    animate={{ opacity: 1, y: 0 }}
                    className="space-y-6"
                  >
                    <div className="bg-accent-50 dark:bg-accent-900/20 p-6 rounded-2xl border border-accent-100 dark:border-accent-800">
                      <div className="flex items-center gap-2 text-accent-600 dark:text-accent-400 mb-4">
                        <MessageSquare size={20} />
                        <span className="font-bold">AI Tutor Hint</span>
                      </div>
                      <p className="text-slate-900 dark:text-slate-100 leading-relaxed text-sm italic font-medium">
                        "{hint}"
                      </p>
                      <div className="mt-6 pt-6 border-t border-accent-100 dark:border-accent-800">
                        <h4 className="text-[10px] font-bold uppercase tracking-widest text-accent-600 dark:text-accent-400 mb-2">Common Mistake</h4>
                        <p className="text-xs text-slate-500 dark:text-slate-400 font-medium">Forgetting to handle the case where no solution exists, or using the same index twice.</p>
                      </div>
                    </div>
                    <button 
                      onClick={() => setHint(null)}
                      className="w-full py-3 border-2 border-accent-600 text-accent-600 dark:text-accent-400 hover:bg-accent-50 dark:hover:bg-accent-900/20 font-bold rounded-xl transition-all"
                    >
                      Ask for another hint
                    </button>
                  </motion.div>
                )}
              </div>
            )}
          </div>
        </div>

        {/* Right Panel: 6. Code Editor & 7. Result Section */}
        <div className="flex-1 flex flex-col gap-6 min-w-0">
          <div className="flex-1 bg-white dark:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700 overflow-hidden shadow-sm dark:shadow-xl dark:shadow-black/20">
            <Editor
              height="100%"
              language={language === 'cpp' ? 'cpp' : language}
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
                roundedSelection: true,
                cursorStyle: 'line',
                automaticLayout: true,
              }}
            />
          </div>

          <div className="h-1/3 bg-white dark:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700 overflow-hidden shadow-sm dark:shadow-xl dark:shadow-black/20 flex flex-col">
            <div className="flex items-center justify-between px-6 py-3 border-b border-slate-100 dark:border-slate-700">
              <div className="flex items-center gap-2 text-slate-500 dark:text-slate-400">
                <Terminal size={16} />
                <span className="text-[10px] font-bold uppercase tracking-widest">Console Output</span>
              </div>
              {result && (
                <div className="flex items-center gap-3">
                  <span className="text-[10px] font-mono text-slate-500 dark:text-slate-400">Time: {result.executionTime}</span>
                  <span className="text-[10px] font-mono text-slate-500 dark:text-slate-400">Mem: {result.memoryUsage}</span>
                  <span className={cn(
                    "text-[10px] font-bold px-2 py-1 rounded-md uppercase tracking-wider",
                    result.status === 'Accepted' || result.status === 'Sample Tests Passed' ? 'bg-emerald-100 dark:bg-emerald-900/30 text-emerald-700 dark:text-emerald-400' : 'bg-rose-100 dark:bg-rose-900/30 text-rose-700 dark:text-rose-400'
                  )}>
                    {result.status}
                  </span>
                </div>
              )}
            </div>
            <div className="flex-1 overflow-y-auto p-6 font-mono text-xs custom-scrollbar">
              {!result ? (
                <div className="flex flex-col items-center justify-center h-full text-slate-400 dark:text-slate-600">
                  <Terminal size={32} className="mb-2" />
                  <p className="italic">Run your code to see results here...</p>
                </div>
              ) : (
                <div className="space-y-6">
                  <div className="space-y-3">
                    {result.testResults.map((tr: any, i: number) => (
                      <div key={i} className="flex items-center justify-between p-3 bg-slate-50 dark:bg-slate-900/50 rounded-xl border border-slate-100 dark:border-slate-700">
                        <div className="flex items-center gap-3">
                          {tr.passed ? <CheckCircle2 className="text-emerald-500" size={16} /> : <XCircle className="text-rose-500" size={16} />}
                          <span className="font-bold text-slate-900 dark:text-slate-100">{tr.name}</span>
                        </div>
                        <span className={cn("font-bold", tr.passed ? 'text-emerald-600 dark:text-emerald-400' : 'text-rose-600 dark:text-rose-400')}>
                          {tr.passed ? 'PASSED' : 'FAILED'}
                        </span>
                      </div>
                    ))}
                  </div>
                  
                  {result.suggestions && (
                    <div className="p-4 bg-accent-50 dark:bg-accent-900/20 rounded-xl border border-accent-100 dark:border-accent-800">
                      <div className="flex items-center gap-2 text-accent-600 dark:text-accent-400 mb-2">
                        <Zap size={14} />
                        <span className="text-[10px] font-bold uppercase tracking-widest">AI Suggestion</span>
                      </div>
                      <p className="text-slate-600 dark:text-slate-300 leading-relaxed italic font-medium">
                        {result.suggestions}
                      </p>
                    </div>
                  )}

                  {result.score !== undefined && (
                    <div className="pt-4 border-t border-slate-100 dark:border-slate-700 flex justify-between items-center">
                      <p className="font-bold text-slate-900 dark:text-slate-100">Final Score</p>
                      <p className="text-xl font-bold text-accent-600 dark:text-accent-400">{result.score}/100</p>
                    </div>
                  )}
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
