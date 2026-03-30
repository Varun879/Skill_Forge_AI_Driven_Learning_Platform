import React, { useEffect, useMemo, useState } from 'react';
import { AlertTriangle, Clock, Flag, Loader2 } from 'lucide-react';
import api from '../services/api';

const unwrapData = <T,>(response: any): T => {
  if (response?.data?.data !== undefined) {
    return response.data.data as T;
  }
  return response?.data as T;
};

type ExamOption = {
  optionId: number;
  text: string;
};

type ExamQuestion = {
  questionId: number;
  title: string;
  prompt: string;
  topic: string;
  difficulty: string;
  order: number;
  options: ExamOption[];
};

type StartExamPayload = {
  examSessionId: number;
  startTime: string;
  endTime: string;
  durationSeconds: number;
  questions: ExamQuestion[];
};

type ExamResultPayload = {
  examSessionId: number;
  score: number;
  totalQuestions: number;
  correctAnswers: number;
  status: string;
};

const cn = (...classes: any[]) => classes.filter(Boolean).join(' ');

export const Exam = () => {
  const [loading, setLoading] = useState(false);
  const [starting, setStarting] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [exam, setExam] = useState<StartExamPayload | null>(null);
  const [result, setResult] = useState<ExamResultPayload | null>(null);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [selectedByQuestion, setSelectedByQuestion] = useState<Record<number, number>>({});
  const [timeLeft, setTimeLeft] = useState(0);
  const [error, setError] = useState<string | null>(null);

  const currentQuestion = exam?.questions?.[currentIndex] || null;

  const answeredCount = useMemo(() => {
    if (!exam) return 0;
    return exam.questions.filter((q) => selectedByQuestion[q.questionId] != null).length;
  }, [exam, selectedByQuestion]);

  useEffect(() => {
    if (!exam || result) {
      return;
    }

    const tick = () => {
      const remaining = Math.max(0, Math.floor((new Date(exam.endTime).getTime() - Date.now()) / 1000));
      setTimeLeft(remaining);
      if (remaining === 0) {
        submitExam(true);
      }
    };

    tick();
    const id = setInterval(tick, 1000);
    return () => clearInterval(id);
  }, [exam, result]);

  const formatTime = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
  };

  const startExam = async () => {
    setStarting(true);
    setError(null);
    setResult(null);
    setSelectedByQuestion({});
    setCurrentIndex(0);

    try {
      const res = await api.post('/exam/start');
      const payload = unwrapData<StartExamPayload>(res);
      setExam(payload);
      const remaining = Math.max(0, Math.floor((new Date(payload.endTime).getTime() - Date.now()) / 1000));
      setTimeLeft(remaining);
    } catch (err) {
      setError((err as any)?.response?.data?.message || 'Unable to start exam. Complete more practice first.');
    } finally {
      setStarting(false);
    }
  };

  const submitExam = async (auto = false) => {
    if (!exam || submitting || result) {
      return;
    }
    setSubmitting(true);
    setError(null);

    try {
      const answers = exam.questions
        .filter((q) => selectedByQuestion[q.questionId] != null)
        .map((q) => ({
          questionId: q.questionId,
          selectedOptionId: selectedByQuestion[q.questionId],
        }));

      const res = await api.post('/exam/submit', {
        examSessionId: exam.examSessionId,
        answers,
      });
      const payload = unwrapData<ExamResultPayload>(res);
      setResult(payload);
      if (auto) {
        setError('Time ended. Exam auto-submitted.');
      }
    } catch (err) {
      setError((err as any)?.response?.data?.message || 'Unable to submit exam right now.');
    } finally {
      setSubmitting(false);
    }
  };

  if (!exam) {
    return (
      <div className="max-w-3xl mx-auto py-10 px-4">
        <div className="rounded-3xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 p-8 shadow-sm space-y-6">
          <h1 className="text-3xl font-bold text-slate-900 dark:text-slate-50">Adaptive Exam</h1>
          <p className="text-sm text-slate-600 dark:text-slate-300">The exam is generated from your practiced MCQs, with higher weight on weak topics. Timer starts immediately after launch.</p>
          <ul className="text-sm text-slate-600 dark:text-slate-300 list-disc pl-5 space-y-1">
            <li>Questions are shuffled from practiced and weak-topic pools.</li>
            <li>Timer auto-submits when it reaches zero.</li>
            <li>You can submit manually at any time.</li>
          </ul>

          {error && (
            <p className="text-sm text-rose-600 dark:text-rose-400">{error}</p>
          )}

          <button
            onClick={startExam}
            disabled={starting}
            className="w-full py-3 rounded-xl bg-accent-600 hover:bg-accent-700 text-white font-bold disabled:opacity-60"
          >
            {starting ? 'Starting Exam...' : 'Start Exam'}
          </button>
        </div>
      </div>
    );
  }

  if (result) {
    return (
      <div className="max-w-3xl mx-auto py-10 px-4">
        <div className="rounded-3xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 p-8 shadow-sm space-y-6">
          <h1 className="text-3xl font-bold text-slate-900 dark:text-slate-50">Exam Result</h1>
          <p className="text-sm text-slate-600 dark:text-slate-300">Session #{result.examSessionId} · {result.status}</p>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="rounded-xl border border-slate-200 dark:border-slate-700 p-4">
              <p className="text-xs uppercase tracking-widest text-slate-500 font-bold">Score</p>
              <p className="text-2xl font-bold text-slate-900 dark:text-slate-50">{Math.round(result.score || 0)}%</p>
            </div>
            <div className="rounded-xl border border-slate-200 dark:border-slate-700 p-4">
              <p className="text-xs uppercase tracking-widest text-slate-500 font-bold">Correct</p>
              <p className="text-2xl font-bold text-slate-900 dark:text-slate-50">{result.correctAnswers}</p>
            </div>
            <div className="rounded-xl border border-slate-200 dark:border-slate-700 p-4">
              <p className="text-xs uppercase tracking-widest text-slate-500 font-bold">Questions</p>
              <p className="text-2xl font-bold text-slate-900 dark:text-slate-50">{result.totalQuestions}</p>
            </div>
          </div>

          <button
            onClick={() => {
              setExam(null);
              setResult(null);
              setError(null);
            }}
            className="w-full py-3 rounded-xl bg-slate-900 hover:bg-slate-700 text-white font-bold"
          >
            Start New Exam
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="h-[calc(100vh-86px)] flex flex-col bg-slate-50 dark:bg-slate-900">
      <header className="px-4 py-3 border-b border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 flex items-center justify-between gap-3">
        <div className="flex items-center gap-3">
          <Clock size={18} className="text-accent-600" />
          <span className={cn('font-mono font-bold', timeLeft <= 60 ? 'text-rose-600' : 'text-slate-900 dark:text-slate-50')}>
            {formatTime(timeLeft)}
          </span>
          <span className="text-xs text-slate-500">Answered {answeredCount}/{exam.questions.length}</span>
        </div>

        <button
          onClick={() => submitExam(false)}
          disabled={submitting}
          className="inline-flex items-center gap-2 px-4 py-2 rounded-lg bg-rose-600 hover:bg-rose-700 text-white text-sm font-bold disabled:opacity-60"
        >
          {submitting ? <Loader2 size={16} className="animate-spin" /> : <Flag size={16} />}
          {submitting ? 'Submitting...' : 'Submit Exam'}
        </button>
      </header>

      {error && (
        <div className="px-4 py-2 bg-rose-100 dark:bg-rose-900/20 text-rose-700 dark:text-rose-300 text-sm flex items-center gap-2">
          <AlertTriangle size={16} />
          {error}
        </div>
      )}

      <div className="flex-1 min-h-0 grid grid-cols-1 lg:grid-cols-12 gap-4 p-4">
        <aside className="lg:col-span-3 rounded-2xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 p-3 overflow-y-auto">
          <p className="text-xs font-bold uppercase tracking-widest text-slate-500 mb-3">Questions</p>
          <div className="grid grid-cols-5 lg:grid-cols-3 gap-2">
            {exam.questions.map((q, idx) => {
              const selected = selectedByQuestion[q.questionId] != null;
              return (
                <button
                  key={q.questionId}
                  onClick={() => setCurrentIndex(idx)}
                  className={cn(
                    'h-10 rounded-lg text-sm font-bold border',
                    idx === currentIndex
                      ? 'bg-accent-600 text-white border-accent-600'
                      : selected
                        ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
                        : 'bg-slate-50 dark:bg-slate-900 text-slate-600 border-slate-200 dark:border-slate-700'
                  )}
                >
                  {idx + 1}
                </button>
              );
            })}
          </div>
        </aside>

        <main className="lg:col-span-9 rounded-2xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 p-6 overflow-y-auto">
          {currentQuestion && (
            <div className="space-y-6">
              <div className="flex items-center justify-between">
                <p className="text-xs font-bold uppercase tracking-widest text-accent-600">{currentQuestion.topic}</p>
                <p className="text-xs font-bold uppercase tracking-widest text-slate-500">{currentQuestion.difficulty}</p>
              </div>

              <h2 className="text-2xl font-bold text-slate-900 dark:text-slate-50">{currentQuestion.title}</h2>
              <p className="text-sm leading-relaxed text-slate-700 dark:text-slate-300">{currentQuestion.prompt}</p>

              <div className="space-y-3">
                {currentQuestion.options.map((option) => (
                  <label
                    key={option.optionId}
                    className={cn(
                      'flex items-start gap-3 p-3 rounded-xl border cursor-pointer transition-colors',
                      selectedByQuestion[currentQuestion.questionId] === option.optionId
                        ? 'border-accent-500 bg-accent-50 dark:bg-accent-900/20'
                        : 'border-slate-200 dark:border-slate-700 hover:border-accent-300'
                    )}
                  >
                    <input
                      type="radio"
                      name={`exam-question-${currentQuestion.questionId}`}
                      checked={selectedByQuestion[currentQuestion.questionId] === option.optionId}
                      onChange={() => {
                        setSelectedByQuestion((prev) => ({
                          ...prev,
                          [currentQuestion.questionId]: option.optionId,
                        }));
                      }}
                    />
                    <span className="text-sm text-slate-800 dark:text-slate-200">{option.text}</span>
                  </label>
                ))}
              </div>

              <div className="flex items-center justify-between pt-3 border-t border-slate-200 dark:border-slate-700">
                <button
                  onClick={() => setCurrentIndex((prev) => Math.max(0, prev - 1))}
                  disabled={currentIndex === 0}
                  className="px-4 py-2 rounded-lg text-sm font-bold bg-slate-100 dark:bg-slate-900 text-slate-700 dark:text-slate-200 disabled:opacity-50"
                >
                  Previous
                </button>
                <button
                  onClick={() => setCurrentIndex((prev) => Math.min(exam.questions.length - 1, prev + 1))}
                  disabled={currentIndex === exam.questions.length - 1}
                  className="px-4 py-2 rounded-lg text-sm font-bold bg-accent-600 hover:bg-accent-700 text-white disabled:opacity-50"
                >
                  Next
                </button>
              </div>
            </div>
          )}
        </main>
      </div>
    </div>
  );
};
