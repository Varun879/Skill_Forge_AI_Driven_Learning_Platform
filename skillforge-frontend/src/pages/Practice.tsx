import React, { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { BarChart3, Bot, ChevronRight, Clock, MessageSquare, Send, X } from 'lucide-react';
import api from '../services/api';
import {
  Bar,
  BarChart,
  CartesianGrid,
  Legend,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';

const cn = (...classes: any[]) => classes.filter(Boolean).join(' ');

type SubmissionResult = {
  isCorrect: boolean;
  selectedOptionId: number;
  correctOptionId: number;
  explanation: string;
  accuracyRate: number;
  averageTimeTakenSeconds?: number;
  attempts?: number;
  timeTakenSeconds: number;
  nextQuestion: any | null;
};

type MCQCategoryGroup = {
  category: string;
  questions: any[];
};

type CategoryPerformance = {
  category: string;
  numberOfAttempts: number;
  accuracy: number;
  averageSolveTimeSeconds: number;
  expectedSolveTimeSeconds: number;
};

type AIMessage = {
  role: 'user' | 'assistant';
  text: string;
};

const unwrapData = <T,>(response: any): T => {
  if (response?.data?.data !== undefined) {
    return response.data.data as T;
  }
  return response?.data as T;
};

const normalizeTags = (rawTags: any): string[] => {
  if (!Array.isArray(rawTags)) {
    return [];
  }
  const seen = new Set<string>();
  rawTags.forEach((raw) => {
    const value = String(raw || '').trim();
    if (!value) return;
    const key = value.toLowerCase();
    if (!seen.has(key)) {
      seen.add(key);
    }
  });
  return Array.from(seen).map((key) => key.replace(/\s+/g, ' '));
};

const formatDifficulty = (value: string) => {
  const normalized = String(value || '').toUpperCase();
  if (normalized === 'BEGINNER' || normalized === 'EASY') return 'Easy';
  if (normalized === 'INTERMEDIATE' || normalized === 'MEDIUM') return 'Medium';
  if (normalized === 'ADVANCED' || normalized === 'HARD') return 'Hard';
  return value || 'Unknown';
};

const strategyDirection = (strategy: string | undefined) => {
  switch ((strategy || '').toUpperCase()) {
    case 'WEAK_CATEGORY':
    case 'UNATTEMPTED_LOW_SUCCESS_RATE':
      return 'Stabilizing fundamentals';
    case 'MEDIUM_CATEGORY':
      return 'Leveling up gradually';
    case 'NEW_CATEGORY':
      return 'Exploring a new category';
    case 'WEAK_TOPIC_EXHAUSTED_UNATTEMPTED':
      return 'Weak topic complete, widening scope';
    case 'FALLBACK':
    case 'FALLBACK_LOW_SUCCESS_RATE':
    case 'GLOBAL_FALLBACK_LOW_SUCCESS_RATE':
      return 'General adaptive fallback';
    default:
      return 'Adaptive recommendation';
  }
};

const DifficultyBadge = ({ level }: { level: string }) => {
  const resolvedLevel = formatDifficulty(level);
  const colors: any = {
    'Easy': 'bg-emerald-50 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400 border-emerald-100 dark:border-emerald-800',
    'Medium': 'bg-amber-50 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400 border-amber-100 dark:border-amber-800',
    'Hard': 'bg-rose-50 text-rose-700 dark:bg-rose-900/30 dark:text-rose-400 border-rose-100 dark:border-rose-800',
  };
  return (
    <span className={cn("text-[10px] font-bold px-2.5 py-1 rounded-lg border uppercase tracking-widest", colors[resolvedLevel] || 'bg-slate-100 text-slate-700')}>
      {resolvedLevel}
    </span>
  );
};

export const Practice = () => {
  const [activeTab, setActiveTab] = useState<'CODING' | 'PROGRAMMING_MCQ' | 'APTITUDE_MCQ'>('CODING');
  const [codingQuestions, setCodingQuestions] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);

  const [categoryGroups, setCategoryGroups] = useState<MCQCategoryGroup[]>([]);
  const [categoryLoadError, setCategoryLoadError] = useState<string>('');
  const [performance, setPerformance] = useState<CategoryPerformance[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<string | null>(null);
  const [sessionQuestions, setSessionQuestions] = useState<any[]>([]);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [selectedOptionId, setSelectedOptionId] = useState<number | null>(null);
  const [submittedResult, setSubmittedResult] = useState<SubmissionResult | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [sessionStartAt, setSessionStartAt] = useState<number>(Date.now());
  const [completedQuestionIds, setCompletedQuestionIds] = useState<number[]>([]);
  const [sessionResults, setSessionResults] = useState<SubmissionResult[]>([]);
  const [feedbackMessage, setFeedbackMessage] = useState<string>('');
  const [aiPanelOpen, setAiPanelOpen] = useState(false);
  const [aiInput, setAiInput] = useState('');
  const [aiMessages, setAiMessages] = useState<AIMessage[]>([
    { role: 'assistant', text: 'Ask me for hints or concept explanations. I will provide guidance, not direct answers.' },
  ]);
  const [aiLoading, setAiLoading] = useState(false);
  const [aiContextQuestionId, setAiContextQuestionId] = useState<number | null>(null);

  const tabs = [
    { id: 'CODING' as const, label: 'Coding Problems' },
    { id: 'PROGRAMMING_MCQ' as const, label: 'Programming MCQs' },
    { id: 'APTITUDE_MCQ' as const, label: 'Aptitude MCQs' },
  ];

  const isMcqTab = activeTab === 'PROGRAMMING_MCQ' || activeTab === 'APTITUDE_MCQ';
  const currentQuestion = sessionQuestions[currentIndex] || null;
  const sessionCompleted = sessionQuestions.length > 0 && currentIndex >= sessionQuestions.length - 1 && !!submittedResult;
  const mcqTypeParam = activeTab === 'PROGRAMMING_MCQ' ? 'PROGRAMMING' : 'APTITUDE';

  useEffect(() => {
    const fetchCodingQuestions = async () => {
      if (activeTab !== 'CODING') {
        return;
      }
      setLoading(true);
      try {
        const res = await api.get('/practice/questions', { params: { type: 'CODING' } });
        const loaded = unwrapData<any[]>(res);
        setCodingQuestions(Array.isArray(loaded) ? loaded : []);
      } catch (err) {
        console.error(err);
        setCodingQuestions([]);
      } finally {
        setLoading(false);
      }
    };

    fetchCodingQuestions();
  }, [activeTab]);

  useEffect(() => {
    const fetchMcqCategories = async () => {
      if (!isMcqTab) {
        return;
      }
      setLoading(true);
      setCategoryLoadError('');
      try {
        const res = await api.get('/practice/mcq/categories', {
          params: { type: mcqTypeParam },
        });
        const loaded = unwrapData<MCQCategoryGroup[]>(res);
        setCategoryGroups(Array.isArray(loaded) ? loaded : []);
      } catch (err) {
        console.error(err);
        setCategoryGroups([]);
        const status = (err as any)?.response?.status;
        if (status === 401 || status === 403) {
          setCategoryLoadError('Unable to load categories for this account. Please sign in as a learner and try again.');
        } else {
          setCategoryLoadError('Unable to load categories right now. Please refresh and try again.');
        }
      } finally {
        setLoading(false);
      }
    };

    fetchMcqCategories();
  }, [isMcqTab, mcqTypeParam]);

  const fetchPerformance = async () => {
    if (!isMcqTab) {
      return;
    }
    try {
      const res = await api.get('/practice/mcq/performance', {
        params: { type: mcqTypeParam },
      });
      const loaded = unwrapData<CategoryPerformance[]>(res);
      setPerformance(Array.isArray(loaded) ? loaded : []);
    } catch (err) {
      console.error(err);
      setPerformance([]);
    }
  };

  useEffect(() => {
    fetchPerformance();
  }, [isMcqTab, mcqTypeParam]);

  const startCategorySession = (category: string) => {
    const group = categoryGroups.find((item) => item.category === category);
    const firstTen = (group?.questions || []).slice(0, 10);
    setSelectedCategory(category);
    setSessionQuestions(firstTen);
    setCurrentIndex(0);
    setSelectedOptionId(null);
    setSubmittedResult(null);
    setSessionResults([]);
    setFeedbackMessage('');
    setSessionStartAt(Date.now());
  };

  const moveToNextQuestion = () => {
    if (currentIndex >= sessionQuestions.length - 1) {
      return;
    }
    setCurrentIndex((prev) => prev + 1);
    setSelectedOptionId(null);
    setSubmittedResult(null);
    setFeedbackMessage('');
    setSessionStartAt(Date.now());
  };

  const submitCurrentMcqAnswer = async () => {
    if (!currentQuestion || !selectedOptionId) {
      setFeedbackMessage('Please select one option before submitting.');
      return;
    }

    const endpoint = activeTab === 'APTITUDE_MCQ'
      ? '/practice/aptitude/answer'
      : '/practice/programming-mcq/answer';
    const timeTaken = Math.max(1, Math.round((Date.now() - sessionStartAt) / 1000));

    setSubmitting(true);
    setFeedbackMessage('');

    try {
      const res = await api.post(endpoint, {
        questionId: currentQuestion.id,
        selectedOptionId,
        timeTaken,
      });

      const responsePayload = unwrapData<any>(res);
      const isCorrect = Boolean(responsePayload?.isCorrect);
      const attempts = Array.isArray(responsePayload?.attemptHistory)
        ? responsePayload.attemptHistory.length
        : 0;
      const result: SubmissionResult = {
        isCorrect,
        selectedOptionId,
        correctOptionId: Number(responsePayload?.correctOptionId || 0),
        explanation: responsePayload?.explanation || 'Review the concept and try one more.',
        accuracyRate: Number(responsePayload?.accuracyRate || 0),
        averageTimeTakenSeconds: Number(responsePayload?.averageTimeTakenSeconds || 0),
        attempts,
        timeTakenSeconds: Number(responsePayload?.timeTakenSeconds || timeTaken),
        nextQuestion: responsePayload?.nextQuestion || null,
      };

      setSubmittedResult(result);
      setSessionResults((prev) => [...prev, result]);
      setCompletedQuestionIds((prev) => (prev.includes(currentQuestion.id) ? prev : [...prev, currentQuestion.id]));
      setFeedbackMessage(isCorrect ? 'Correct answer submitted.' : 'Submitted. Review the explanation below.');
      fetchPerformance();
    } catch (err) {
      setFeedbackMessage('Failed to submit answer. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  const loadAdaptiveNextSet = async () => {
    if (!selectedCategory) {
      setFeedbackMessage('Select a category before loading the adaptive next set.');
      return;
    }

    setLoading(true);
    try {
      const used = new Set<number>(completedQuestionIds);
      const res = await api.get('/practice/mcq/next-set', {
        params: { type: mcqTypeParam, category: selectedCategory, size: 10 },
      });
      const payload = unwrapData<any>(res);
      const rawQuestions = Array.isArray(payload?.questions) ? payload.questions : [];
      const nextQuestions = rawQuestions.filter((question: any) => question?.id && !used.has(question.id));

      if (nextQuestions.length === 0) {
        setFeedbackMessage('No additional adaptive questions available yet.');
        return;
      }

      setSessionQuestions(nextQuestions);
      setCurrentIndex(0);
      setSelectedOptionId(null);
      setSubmittedResult(null);
      setSessionResults([]);
      const accuracyUsed = typeof payload?.lastTenAccuracy === 'number'
        ? Math.round(payload.lastTenAccuracy * 100)
        : null;
      setFeedbackMessage(
        accuracyUsed === null
          ? 'Adaptive next set loaded.'
          : `Adaptive next set loaded based on your last 10 accuracy (${accuracyUsed}%).`
      );
      setSessionStartAt(Date.now());
    } catch (err) {
      console.error('next-set failed, falling back to next loop', err);
      try {
        const used = new Set<number>(completedQuestionIds);
        const nextQuestions: any[] = [];
        let guard = 0;

        while (nextQuestions.length < 10 && guard < 80) {
          guard += 1;
          const res = await api.get('/practice/mcq/next', {
            params: { type: mcqTypeParam, category: selectedCategory },
          });
          const payload = unwrapData<any>(res);
          const candidate = payload?.question || null;

          if (!candidate?.id || used.has(candidate.id)) {
            continue;
          }

          used.add(candidate.id);
          nextQuestions.push(candidate);
        }

        if (nextQuestions.length === 0) {
          const status = (err as any)?.response?.status;
          const backendMessage = (err as any)?.response?.data?.message || (err as any)?.response?.data?.error;
          if (status === 401 || status === 403) {
            setFeedbackMessage('Session expired or unauthorized. Please login again as learner and retry.');
          } else if (backendMessage) {
            setFeedbackMessage(String(backendMessage));
          } else {
            setFeedbackMessage('Unable to load adaptive next set right now.');
          }
          return;
        }

        setSessionQuestions(nextQuestions);
        setCurrentIndex(0);
        setSelectedOptionId(null);
        setSubmittedResult(null);
        setSessionResults([]);
        setFeedbackMessage('Adaptive next set loaded using fallback mode.');
        setSessionStartAt(Date.now());
      } catch (fallbackErr) {
        console.error('fallback next loop failed', fallbackErr);
        const status = (fallbackErr as any)?.response?.status;
        const backendMessage = (fallbackErr as any)?.response?.data?.message || (fallbackErr as any)?.response?.data?.error;
        if (status === 401 || status === 403) {
          setFeedbackMessage('Session expired or unauthorized. Please login again as learner and retry.');
        } else if (backendMessage) {
          setFeedbackMessage(String(backendMessage));
        } else {
          setFeedbackMessage('Unable to load adaptive next set right now.');
        }
      }
    } finally {
      setLoading(false);
    }
  };

  const chartData = useMemo(() => {
    return performance.map((item) => ({
      category: item.category,
      attempts: item.numberOfAttempts,
      accuracy: Math.round(item.accuracy * 100),
      avgTime: Math.round(item.averageSolveTimeSeconds || 0),
      expectedTime: Math.round(item.expectedSolveTimeSeconds || 0),
    }));
  }, [performance]);

  const askAi = async () => {
    const message = aiInput.trim();
    if (!message || aiLoading) {
      return;
    }

    setAiMessages((prev) => [...prev, { role: 'user', text: message }]);
    setAiInput('');
    setAiLoading(true);

    try {
      const res = await api.post('/ai/chat', {
        message,
        questionId: aiContextQuestionId,
      });
      const payload = unwrapData<any>(res);
      const reply = payload?.reply || 'Try identifying constraints and validating your reasoning with a simple test case.';
      setAiMessages((prev) => [...prev, { role: 'assistant', text: String(reply) }]);
    } catch (err) {
      const fallback = (err as any)?.response?.data?.message || 'Unable to fetch AI hint right now.';
      setAiMessages((prev) => [...prev, { role: 'assistant', text: String(fallback) }]);
    } finally {
      setAiLoading(false);
    }
  };

  const handleAskDoubtForQuestion = (questionId?: number) => {
    setAiPanelOpen(true);
    if (questionId) {
      setAiContextQuestionId(questionId);
      setAiInput('Give me a hint for this question.');
    }
  };

  return (
    <div className="space-y-8 pb-12">
      <header className="flex flex-col md:flex-row md:items-end justify-between gap-6">
        <div>
          <h1 className="text-3xl font-bold tracking-tight mb-2 text-slate-900 dark:text-slate-50">Practice Problems</h1>
          <p className="text-slate-500 dark:text-slate-400 text-sm font-medium">Pick Programming MCQ or Aptitude MCQ, solve a category-wise set of 10, then continue with adaptive next sets.</p>
        </div>
      </header>

      <div className="flex flex-wrap items-center gap-2 p-1 bg-white dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-700 shadow-sm w-fit">
        {tabs.map((tab) => (
          <button
            key={tab.id}
            onClick={() => setActiveTab(tab.id)}
            className={cn(
              "px-4 py-2 rounded-lg text-xs font-bold transition-all",
              activeTab === tab.id
                ? "bg-accent-600 text-white shadow-md shadow-accent-200 dark:shadow-none"
                : "text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-200"
            )}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {activeTab === 'CODING' && (
        <section className="bg-white dark:bg-slate-800 p-6 rounded-2xl border border-slate-200 dark:border-slate-700 shadow-sm space-y-4">
          <h2 className="text-lg font-bold text-slate-900 dark:text-slate-50">Coding Problems</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {codingQuestions.map((question) => (
              <div key={question.id} className="border border-slate-200 dark:border-slate-700 rounded-xl p-4">
                <p className="text-xs font-bold uppercase tracking-widest text-accent-600 mb-2">{question.topic}</p>
                <h3 className="font-semibold text-slate-900 dark:text-slate-100 mb-2 line-clamp-2">{question.title}</h3>
                <div className="flex items-center justify-between text-xs text-slate-500">
                  <span className="inline-flex items-center gap-1"><Clock size={12} />{question.estimatedSolveTimeMinutes || 20}m</span>
                  <Link to={`/practice/${question.id}`} className="text-accent-600 font-bold inline-flex items-center gap-1">
                    Open <ChevronRight size={14} />
                  </Link>
                </div>
              </div>
            ))}
          </div>
          {!loading && codingQuestions.length === 0 && (
            <p className="text-sm text-slate-500">No coding problems available right now.</p>
          )}
        </section>
      )}

      {isMcqTab && (
        <>
          <section className="bg-white dark:bg-slate-800 p-6 rounded-2xl border border-slate-200 dark:border-slate-700 shadow-sm space-y-4">
            <h2 className="text-lg font-bold text-slate-900 dark:text-slate-50">Select Category</h2>
            <p className="text-sm text-slate-500">Each category starts with 10 questions. After finishing, the next 10 are recommended adaptively.</p>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {categoryGroups.map((group) => (
                <button
                  key={group.category}
                  onClick={() => startCategorySession(group.category)}
                  className={cn(
                    'text-left rounded-xl border p-4 transition-all',
                    selectedCategory === group.category
                      ? 'border-accent-500 bg-accent-50 dark:bg-accent-900/20'
                      : 'border-slate-200 dark:border-slate-700 hover:border-accent-300'
                  )}
                >
                  <p className="text-sm font-bold text-slate-900 dark:text-slate-100">{group.category}</p>
                  <p className="text-xs text-slate-500 mt-1">{Math.min(group.questions.length, 10)} questions ready</p>
                </button>
              ))}
            </div>

            {loading && (
              <p className="text-sm text-slate-500">Loading categories...</p>
            )}

            {!loading && !!categoryLoadError && (
              <p className="text-sm text-rose-600 dark:text-rose-400">{categoryLoadError}</p>
            )}

            {!loading && !categoryLoadError && categoryGroups.length === 0 && (
              <p className="text-sm text-slate-500">
                No categories are available for this section yet. Please try again in a moment.
              </p>
            )}
          </section>

          {selectedCategory && currentQuestion && (
            <section className="bg-white dark:bg-slate-800 p-6 rounded-2xl border border-slate-200 dark:border-slate-700 shadow-sm space-y-4">
              <div className="flex items-center justify-between">
                <p className="text-xs font-bold uppercase tracking-widest text-accent-600">{selectedCategory}</p>
                <p className="text-xs font-bold text-slate-500">Question {currentIndex + 1} / {sessionQuestions.length}</p>
              </div>

              <h3 className="text-lg font-semibold text-slate-900 dark:text-slate-100">{currentQuestion.title}</h3>
              <p className="text-sm text-slate-600 dark:text-slate-300">{currentQuestion.prompt}</p>

              <button
                onClick={() => handleAskDoubtForQuestion(currentQuestion.id)}
                className="inline-flex items-center gap-2 px-3 py-2 text-xs font-bold rounded-lg border border-slate-200 dark:border-slate-700 hover:border-accent-400 text-slate-700 dark:text-slate-200"
              >
                <MessageSquare size={14} /> Ask Doubt
              </button>

              <div className="space-y-2">
                {(currentQuestion.options || []).map((option: any) => {
                  const isCorrectOption = submittedResult && option.id === submittedResult.correctOptionId;
                  const isWrongSelected = submittedResult && option.id === submittedResult.selectedOptionId && !submittedResult.isCorrect;

                  return (
                    <label
                      key={option.id}
                      className={cn(
                        'flex items-center gap-3 rounded-lg border px-3 py-2 text-sm',
                        isCorrectOption
                          ? 'border-emerald-300 bg-emerald-50 dark:bg-emerald-900/20'
                          : isWrongSelected
                            ? 'border-rose-300 bg-rose-50 dark:bg-rose-900/20'
                            : 'border-slate-200 dark:border-slate-700'
                      )}
                    >
                      <input
                        type="radio"
                        name={`question-${currentQuestion.id}`}
                        disabled={!!submittedResult}
                        checked={selectedOptionId === option.id}
                        onChange={() => setSelectedOptionId(option.id)}
                      />
                      <span>{option.optionText}</span>
                    </label>
                  );
                })}
              </div>

              {!submittedResult && (
                <button
                  onClick={submitCurrentMcqAnswer}
                  disabled={submitting || !selectedOptionId}
                  className="px-4 py-2 bg-accent-600 text-white rounded-lg text-sm font-bold hover:bg-accent-700 disabled:opacity-60"
                >
                  {submitting ? 'Submitting...' : 'Submit Answer'}
                </button>
              )}

              {feedbackMessage && (
                <p className="text-sm font-medium text-slate-700 dark:text-slate-200">{feedbackMessage}</p>
              )}

              {submittedResult && (
                <div className="rounded-lg border border-slate-200 dark:border-slate-700 p-3 bg-slate-50 dark:bg-slate-900/40 space-y-2">
                  <p className={cn('text-sm font-bold', submittedResult.isCorrect ? 'text-emerald-600' : 'text-rose-600')}>
                    {submittedResult.isCorrect ? 'Correct answer' : 'Incorrect answer'}
                  </p>
                  <p className="text-xs text-slate-600 dark:text-slate-300">{submittedResult.explanation}</p>
                  <p className="text-xs text-slate-500">
                    Accuracy: {Math.round(submittedResult.accuracyRate || 0)}% · Time: {Math.round(submittedResult.timeTakenSeconds || 0)}s
                  </p>
                  {!sessionCompleted && (
                    <button
                      onClick={moveToNextQuestion}
                      className="text-sm font-bold text-accent-600"
                    >
                      Next Question
                    </button>
                  )}
                </div>
              )}
            </section>
          )}

          {sessionCompleted && (
            <section className="bg-white dark:bg-slate-800 p-6 rounded-2xl border border-slate-200 dark:border-slate-700 shadow-sm space-y-3">
              <h3 className="text-lg font-bold text-slate-900 dark:text-slate-50">Set Completed</h3>
              <p className="text-sm text-slate-600 dark:text-slate-300">
                You finished this 10-question set with {sessionResults.filter((r) => r.isCorrect).length} correct answers.
              </p>
              <button
                onClick={loadAdaptiveNextSet}
                className="px-4 py-2 bg-accent-600 text-white rounded-lg text-sm font-bold hover:bg-accent-700"
              >
                Load Adaptive Next 10
              </button>
            </section>
          )}

          <section className="bg-white dark:bg-slate-800 p-6 rounded-2xl border border-slate-200 dark:border-slate-700 shadow-sm space-y-4">
            <div className="flex items-center gap-2 text-slate-900 dark:text-slate-100">
              <BarChart3 size={18} />
              <h3 className="text-lg font-bold">Category Analytics</h3>
            </div>
            <p className="text-sm text-slate-500">Accuracy and solve time trends for each category in this section.</p>

            <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
              <div className="h-72">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={chartData}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="category" hide />
                    <YAxis domain={[0, 100]} />
                    <Tooltip />
                    <Legend />
                    <Bar dataKey="accuracy" name="Accuracy %" />
                  </BarChart>
                </ResponsiveContainer>
              </div>

              <div className="h-72">
                <ResponsiveContainer width="100%" height="100%">
                  <LineChart data={chartData}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="category" hide />
                    <YAxis />
                    <Tooltip />
                    <Legend />
                    <Line type="monotone" dataKey="avgTime" strokeWidth={2} name="Avg Time (s)" />
                    <Line type="monotone" dataKey="expectedTime" strokeWidth={2} name="Expected Time (s)" />
                  </LineChart>
                </ResponsiveContainer>
              </div>
            </div>
          </section>
        </>
      )}

      <button
        onClick={() => setAiPanelOpen((prev) => !prev)}
        className="fixed bottom-6 right-6 z-40 w-14 h-14 rounded-full bg-accent-600 hover:bg-accent-700 text-white shadow-xl flex items-center justify-center"
        aria-label="Toggle AI Chatbot"
      >
        {aiPanelOpen ? <X size={20} /> : <Bot size={20} />}
      </button>

      {aiPanelOpen && (
        <section className="fixed bottom-24 right-6 z-40 w-[360px] max-w-[calc(100vw-2rem)] bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-2xl shadow-2xl overflow-hidden">
          <header className="px-4 py-3 border-b border-slate-200 dark:border-slate-700 flex items-center justify-between">
            <div>
              <p className="text-xs font-bold uppercase tracking-widest text-accent-600">AI Tutor</p>
              <p className="text-sm font-semibold text-slate-900 dark:text-slate-100">
                {aiContextQuestionId ? `Question context: ${aiContextQuestionId}` : 'General context'}
              </p>
            </div>
            <button
              onClick={() => setAiPanelOpen(false)}
              className="text-slate-500 hover:text-slate-900 dark:hover:text-slate-100"
            >
              <X size={16} />
            </button>
          </header>

          <div className="h-72 overflow-y-auto p-4 space-y-3 bg-slate-50/70 dark:bg-slate-900/30">
            {aiMessages.map((msg, idx) => (
              <div
                key={`${msg.role}-${idx}`}
                className={cn('text-sm p-3 rounded-xl max-w-[90%]', msg.role === 'user'
                  ? 'ml-auto bg-accent-600 text-white'
                  : 'bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-200')}
              >
                {msg.text}
              </div>
            ))}
          </div>

          <div className="p-3 border-t border-slate-200 dark:border-slate-700 space-y-2">
            <textarea
              value={aiInput}
              onChange={(e) => setAiInput(e.target.value)}
              className="w-full resize-none h-20 rounded-lg border border-slate-200 dark:border-slate-700 px-3 py-2 text-sm bg-white dark:bg-slate-900"
              placeholder="Ask for a hint or concept explanation..."
            />
            <div className="flex items-center justify-between gap-2">
              <button
                onClick={() => setAiContextQuestionId(null)}
                className="text-xs font-bold text-slate-500 hover:text-slate-800"
              >
                Clear context
              </button>
              <button
                onClick={askAi}
                disabled={aiLoading || !aiInput.trim()}
                className="inline-flex items-center gap-2 px-3 py-2 rounded-lg text-xs font-bold bg-accent-600 text-white hover:bg-accent-700 disabled:opacity-60"
              >
                <Send size={14} /> {aiLoading ? 'Thinking...' : 'Send'}
              </button>
            </div>
          </div>
        </section>
      )}
    </div>
  );
};
