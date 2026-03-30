import React, { useState, useEffect } from 'react';
import { 
  ArrowLeft, 
  Play, 
  FileText, 
  CheckCircle, 
  Lock,
  Users,
  Clock,
  Award,
  ChevronRight,
  MessageSquare,
  List,
  Video
} from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';
import { Link, useParams } from 'react-router-dom';
import api from '../services/api';
import { subscribeTopic } from '../services/chatSocket';

const getApiErrorMessage = (err: any, fallback: string) => {
  return (
    err?.response?.data?.message
    || err?.response?.data?.error
    || err?.message
    || fallback
  );
};

const unwrapData = <T,>(response: any): T => {
  if (response?.data?.data !== undefined) {
    return response.data.data as T;
  }
  return response?.data as T;
};

export const CourseDetail = () => {
  const { id } = useParams();
  const [course, setCourse] = useState<any>(null);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [modules, setModules] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [enrolling, setEnrolling] = useState(false);
  const [isEnrolled, setIsEnrolled] = useState(false);
  const [moduleProgress, setModuleProgress] = useState<any>(null);
  const [completingModuleId, setCompletingModuleId] = useState<number | null>(null);
  const [courseExams, setCourseExams] = useState<any[]>([]);
  const [activeExam, setActiveExam] = useState<any>(null);
  const [selectedAnswers, setSelectedAnswers] = useState<Record<number, number>>({});
  const [examSubmitting, setExamSubmitting] = useState(false);
  const [examResult, setExamResult] = useState<any>(null);
  const [examError, setExamError] = useState<string | null>(null);
  const [isMobileNavOpen, setIsMobileNavOpen] = useState(false);
  const [chatMessages, setChatMessages] = useState<any[]>([]);
  const [chatInput, setChatInput] = useState('');
  const [chatLoading, setChatLoading] = useState(false);
  const [chatPosting, setChatPosting] = useState(false);
  const [chatError, setChatError] = useState<string | null>(null);
  const [chatRoomId, setChatRoomId] = useState<number | null>(null);
  const [groupMessages, setGroupMessages] = useState<any[]>([]);
  const [groupInput, setGroupInput] = useState('');
  const [groupLoading, setGroupLoading] = useState(false);
  const [groupPosting, setGroupPosting] = useState(false);
  const [groupError, setGroupError] = useState<string | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      if (!id) {
        setLoadError('Invalid course link.');
        setLoading(false);
        return;
      }

      try {
        setLoadError(null);
        const [courseResult, enrollmentsResult, modulesResult, examsResult] = await Promise.allSettled([
          api.get(`/courses/${id}`),
          api.get('/user/enrollments'),
          api.get(`/courses/${id}/modules`),
          api.get(`/courses/${id}/exams`),
        ]);

        if (courseResult.status !== 'fulfilled') {
          throw courseResult.reason;
        }

        const courseData = unwrapData<any>(courseResult.value);

        const enrollments = enrollmentsResult.status === 'fulfilled'
          ? (unwrapData<any[]>(enrollmentsResult.value) || [])
          : [];

        const rawModules = modulesResult.status === 'fulfilled'
          ? (unwrapData<any[]>(modulesResult.value) || [])
          : [];

        const exams = examsResult.status === 'fulfilled'
          ? (unwrapData<any[]>(examsResult.value) || [])
          : [];

        const enrolled = enrollments.some((e: any) => e.courseId === Number(id));

        setCourse(courseData);
        setModules(rawModules.map((m) => ({
          ...m,
          videoUrl: m.videoUrl || m.video_url || '',
          content: m.content || m.notes || '',
          orderIndex: m.orderIndex || m.order_index || 1,
        })));
        setCourseExams(exams);
        setIsEnrolled(enrolled);

        if (enrolled) {
          try {
            const progressRes = await api.get(`/courses/${id}/modules/progress`);
            setModuleProgress(unwrapData<any>(progressRes));
          } catch {
            setModuleProgress(null);
          }
        } else {
          setModuleProgress(null);
        }
      } catch (err) {
        console.error(err);
        setLoadError(getApiErrorMessage(err, 'Unable to load this course right now.'));
        setCourse(null);
        setModules([]);
        setCourseExams([]);
        setIsEnrolled(false);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [id]);

  const handleEnroll = async () => {
    setEnrolling(true);
    try {
      await api.post('/courses/enroll', { courseId: Number(id) });
      setIsEnrolled(true);
      try {
        const progressRes = await api.get(`/courses/${id}/modules/progress`);
        setModuleProgress(unwrapData<any>(progressRes));
      } catch {
        setModuleProgress(null);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setEnrolling(false);
    }
  };

  const markModuleComplete = async (moduleId: number) => {
    if (!id) return;
    setCompletingModuleId(moduleId);
    try {
      const res = await api.post(`/courses/${id}/modules/${moduleId}/complete`);
      setModuleProgress(unwrapData<any>(res));
    } catch (err) {
      console.error(err);
    } finally {
      setCompletingModuleId(null);
    }
  };

  const startFinalExam = async (examId: number) => {
    if (!id) return;
    setExamError(null);
    setExamResult(null);
    try {
      const res = await api.post(`/courses/${id}/exams/${examId}/attempts/start`);
      setActiveExam(unwrapData<any>(res));
      setSelectedAnswers({});
    } catch (err) {
      setExamError(getApiErrorMessage(err, 'Unable to start final exam.'));
    }
  };

  const submitFinalExam = async () => {
    if (!id || !activeExam?.attemptId || !activeExam?.courseExamId) return;

    const questions = activeExam.questions || [];
    if (questions.some((q: any) => !selectedAnswers[q.questionId])) {
      setExamError('Answer all questions before submitting.');
      return;
    }

    setExamSubmitting(true);
    setExamError(null);
    try {
      const res = await api.post(`/courses/${id}/exams/${activeExam.courseExamId}/attempts/submit`, {
        attemptId: activeExam.attemptId,
        answers: questions.map((q: any) => ({
          questionId: q.questionId,
          selectedOptionId: selectedAnswers[q.questionId],
        })),
      });
      setExamResult(unwrapData<any>(res));
      setActiveExam(null);
      const certRes = await api.get('/certificate/my');
      void certRes;
    } catch (err) {
      setExamError(getApiErrorMessage(err, 'Unable to submit final exam.'));
    } finally {
      setExamSubmitting(false);
    }
  };

  useEffect(() => {
    const fetchChat = async () => {
      if (!isEnrolled || !id) {
        setChatMessages([]);
        setChatError(null);
        setChatRoomId(null);
        setGroupMessages([]);
        setGroupError(null);
        return;
      }

      setChatLoading(true);
      setGroupLoading(true);
      setChatError(null);
      setGroupError(null);
      try {
        const startRes = await api.post('/chat/start', { courseId: Number(id) });
        const room = unwrapData<any>(startRes);
        setChatRoomId(room?.id ?? null);

        if (!room?.id) {
          setChatMessages([]);
          return;
        }

        const messagesRes = await api.get(`/chat/${room.id}`, {
          params: { page: 0, size: 50 },
        });
        const payload = unwrapData<any>(messagesRes);
        setChatMessages(Array.isArray(payload?.messages) ? payload.messages : []);

        const groupRes = await api.get(`/chat/course/${Number(id)}/group/messages`, {
          params: { page: 0, size: 60 },
        });
        const groupPayload = unwrapData<any>(groupRes);
        setGroupMessages(Array.isArray(groupPayload?.messages) ? groupPayload.messages : []);
      } catch (err) {
        setChatError(getApiErrorMessage(err, 'Unable to load course chat.'));
        setGroupError(getApiErrorMessage(err, 'Unable to load group chat.'));
      } finally {
        setChatLoading(false);
        setGroupLoading(false);
      }
    };

    fetchChat();
  }, [id, isEnrolled]);

  useEffect(() => {
    if (!isEnrolled || !chatRoomId) return;

    const intervalId = setInterval(async () => {
      try {
        const res = await api.get(`/chat/${chatRoomId}`, { params: { page: 0, size: 50 } });
        const payload = unwrapData<any>(res);
        setChatMessages(Array.isArray(payload?.messages) ? payload.messages : []);
      } catch {
        // Best-effort polling, keep UI usable even on transient network issues.
      }
    }, 5000);

    return () => clearInterval(intervalId);
  }, [isEnrolled, chatRoomId]);

  useEffect(() => {
    if (!isEnrolled || !chatRoomId) return;

    let subscription: any;
    subscribeTopic(`/topic/chat/room/${chatRoomId}`, (frame) => {
      const incoming = JSON.parse(frame.body);
      setChatMessages((prev) => {
        if (prev.some((m) => m.id === incoming.id)) {
          return prev;
        }
        return [...prev, incoming];
      });
    })
      .then((sub) => {
        subscription = sub;
      })
      .catch((err) => {
        console.error('Learner room subscription failed', err);
      });

    return () => {
      if (subscription) {
        subscription.unsubscribe();
      }
    };
  }, [isEnrolled, chatRoomId]);

  useEffect(() => {
    if (!isEnrolled || !id) return;

    const intervalId = setInterval(async () => {
      try {
        const res = await api.get(`/chat/course/${Number(id)}/group/messages`, { params: { page: 0, size: 60 } });
        const payload = unwrapData<any>(res);
        setGroupMessages(Array.isArray(payload?.messages) ? payload.messages : []);
      } catch {
        // Best-effort polling, keep UI usable even on transient network issues.
      }
    }, 5000);

    return () => clearInterval(intervalId);
  }, [isEnrolled, id]);

  useEffect(() => {
    if (!isEnrolled || !id) return;

    let subscription: any;
    subscribeTopic(`/topic/chat/course/${Number(id)}/group`, (frame) => {
      const incoming = JSON.parse(frame.body);
      setGroupMessages((prev) => {
        if (prev.some((m) => m.id === incoming.id)) {
          return prev;
        }
        return [...prev, incoming];
      });
    })
      .then((sub) => {
        subscription = sub;
      })
      .catch((err) => {
        console.error('Learner group subscription failed', err);
      });

    return () => {
      if (subscription) {
        subscription.unsubscribe();
      }
    };
  }, [isEnrolled, id]);

  const handleSendChat = async () => {
    if (!chatInput.trim() || !chatRoomId) return;
    setChatPosting(true);
    setChatError(null);
    try {
      const res = await api.post('/chat/send', {
        roomId: chatRoomId,
        message: chatInput.trim(),
      });
      const message = unwrapData<any>(res);
      setChatMessages((prev) => [...prev, message]);
      setChatInput('');
    } catch (err) {
      setChatError(getApiErrorMessage(err, 'Unable to send message.'));
    } finally {
      setChatPosting(false);
    }
  };

  const handleSendGroupChat = async () => {
    if (!groupInput.trim() || !id) return;
    setGroupPosting(true);
    setGroupError(null);
    try {
      const res = await api.post(`/chat/course/${Number(id)}/group/messages`, {
        message: groupInput.trim(),
      });
      const message = unwrapData<any>(res);
      setGroupMessages((prev) => [...prev, message]);
      setGroupInput('');
    } catch (err) {
      setGroupError(getApiErrorMessage(err, 'Unable to send group message.'));
    } finally {
      setGroupPosting(false);
    }
  };

  if (loading) return <div className="flex items-center justify-center h-96">Loading...</div>;

  if (loadError || !course) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-12">
        <div className="bg-rose-50 dark:bg-rose-900/20 border border-rose-200 dark:border-rose-800 rounded-2xl p-6">
          <h1 className="text-xl font-bold text-rose-700 dark:text-rose-300">Unable to open this course</h1>
          <p className="mt-2 text-sm text-rose-700/90 dark:text-rose-300/90">
            {loadError || 'Course data is unavailable.'}
          </p>
          <Link
            to="/explore"
            className="inline-flex mt-4 items-center gap-2 px-4 py-2 rounded-xl bg-slate-900 text-white text-sm font-bold"
          >
            <ArrowLeft size={16} />
            Back to Explore
          </Link>
        </div>
      </div>
    );
  }

  const scrollToModule = (moduleId: number) => {
    const element = document.getElementById(`module-${moduleId}`);
    if (element) {
      const offset = 100;
      const bodyRect = document.body.getBoundingClientRect().top;
      const elementRect = element.getBoundingClientRect().top;
      const elementPosition = elementRect - bodyRect;
      const offsetPosition = elementPosition - offset;

      window.scrollTo({
        top: offsetPosition,
        behavior: 'smooth'
      });
    }
    setIsMobileNavOpen(false);
  };

  return (
    <div className="max-w-7xl mx-auto space-y-8 px-4">
      <div className="flex items-center justify-between">
        <Link to="/explore" className="flex items-center gap-2 text-slate-500 hover:text-slate-900 dark:hover:text-slate-50 transition-colors font-bold">
          <ArrowLeft size={20} />
          Back to Explore
        </Link>

        {/* Mobile Module Nav Toggle */}
        <button 
          onClick={() => setIsMobileNavOpen(!isMobileNavOpen)}
          className="lg:hidden flex items-center gap-2 px-4 py-2 bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-xl text-sm font-bold text-slate-700 dark:text-slate-300 shadow-sm"
        >
          <List size={18} />
          Modules
        </button>
      </div>

      {/* Mobile Module Navigation Overlay */}
      <AnimatePresence>
        {isMobileNavOpen && (
          <>
            <motion.div 
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={() => setIsMobileNavOpen(false)}
              className="fixed inset-0 bg-slate-900/40 backdrop-blur-sm z-60 lg:hidden"
            />
            <motion.div 
              initial={{ x: '100%' }}
              animate={{ x: 0 }}
              exit={{ x: '100%' }}
              className="fixed right-0 top-0 bottom-0 w-80 bg-white dark:bg-slate-900 z-70 lg:hidden shadow-2xl p-6 overflow-y-auto"
            >
              <div className="flex items-center justify-between mb-8">
                <h3 className="text-lg font-bold text-slate-900 dark:text-slate-50">Course Modules</h3>
                <button onClick={() => setIsMobileNavOpen(false)} className="p-2 text-slate-400 hover:text-slate-900 dark:hover:text-slate-50">
                  <ChevronRight size={24} />
                </button>
              </div>
              <div className="space-y-3">
                {modules.map((mod: any, i: number) => (
                  <button
                    key={mod.id}
                    onClick={() => scrollToModule(mod.id)}
                    className="w-full flex items-center gap-4 p-4 rounded-2xl bg-slate-50 dark:bg-slate-800/50 border border-slate-100 dark:border-slate-800 text-left transition-all hover:border-accent-500"
                  >
                    <div className="w-10 h-10 rounded-xl bg-white dark:bg-slate-900 flex items-center justify-center text-slate-400 shrink-0">
                      {isEnrolled ? <Play size={18} /> : <Lock size={18} />}
                    </div>
                    <div>
                      <p className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Module {i + 1}</p>
                      <p className="text-sm font-bold text-slate-900 dark:text-slate-50 line-clamp-1">{mod.title}</p>
                    </div>
                  </button>
                ))}
              </div>
            </motion.div>
          </>
        )}
      </AnimatePresence>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
        {/* Desktop Sidebar Navigation */}
        <aside className="hidden lg:block lg:col-span-3 sticky top-24 space-y-6">
          <div className="bg-white dark:bg-slate-800 rounded-3xl border border-slate-200 dark:border-slate-700 shadow-sm p-6">
            <h3 className="text-xs font-bold text-slate-400 uppercase tracking-widest mb-4 ml-1">Course Progress</h3>
            <div className="space-y-2">
              {modules.map((mod: any, i: number) => (
                <button
                  key={mod.id}
                  onClick={() => scrollToModule(mod.id)}
                  className="w-full flex items-center gap-3 p-3 rounded-xl hover:bg-slate-50 dark:hover:bg-slate-900/50 border border-transparent hover:border-slate-100 dark:hover:border-slate-800 transition-all group text-left"
                >
                  <div className="w-8 h-8 rounded-lg bg-slate-50 dark:bg-slate-900 flex items-center justify-center text-slate-400 group-hover:text-accent-600 transition-colors shrink-0">
                    {isEnrolled ? <Play size={14} /> : <Lock size={14} />}
                  </div>
                  <div className="min-w-0">
                    <p className="text-[10px] font-bold text-slate-400 uppercase tracking-widest truncate">Module {i + 1}</p>
                    <p className="text-xs font-bold text-slate-900 dark:text-slate-50 truncate">{mod.title}</p>
                  </div>
                </button>
              ))}
            </div>
            {isEnrolled && (
              <p className="mt-4 text-xs text-slate-500">
                Module completion: {moduleProgress?.completedModules || 0}/{moduleProgress?.totalModules || modules.length || 0}
              </p>
            )}
          </div>
          
          {isEnrolled && (
            <div className="p-6 bg-accent-600 rounded-3xl text-white shadow-lg shadow-accent-200 dark:shadow-none">
              <p className="text-xs font-bold uppercase tracking-widest opacity-80 mb-1">Learning Tip</p>
              <p className="text-sm font-bold leading-relaxed">Complete modules in sequence to unlock advanced adaptive problems.</p>
            </div>
          )}
        </aside>

        <div className="lg:col-span-6 space-y-8">
          <div className="space-y-6">
            <div className="flex items-center gap-3">
              <span className="text-xs font-bold px-3 py-1 bg-accent-50 dark:bg-accent-900/20 text-accent-600 dark:text-accent-400 rounded-full uppercase tracking-widest border border-accent-100 dark:border-accent-800">
                {course.difficultyLevel}
              </span>
              <span className="text-xs font-bold px-3 py-1 bg-emerald-50 dark:bg-emerald-900/20 text-emerald-600 dark:text-emerald-400 rounded-full uppercase tracking-widest border border-emerald-100 dark:border-emerald-800">
                Adaptive Learning
              </span>
            </div>
            <h1 className="text-4xl font-extrabold text-slate-900 dark:text-slate-50 tracking-tight">{course.title}</h1>
            <p className="text-lg text-slate-600 dark:text-slate-400 leading-relaxed">{course.description}</p>
            
            <div className="flex flex-wrap gap-6 py-6 border-y border-slate-100 dark:border-slate-800">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-full bg-slate-100 dark:bg-slate-900 flex items-center justify-center text-slate-600 dark:text-slate-400">
                  <Users size={20} />
                </div>
                <div>
                  <p className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Tutor</p>
                  <p className="text-sm font-bold text-slate-900 dark:text-slate-50">{course.tutorName}</p>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-full bg-slate-100 dark:bg-slate-900 flex items-center justify-center text-slate-600 dark:text-slate-400">
                  <Clock size={20} />
                </div>
                <div>
                  <p className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Duration</p>
                  <p className="text-sm font-bold text-slate-900 dark:text-slate-50">Self-Paced</p>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-full bg-slate-100 dark:bg-slate-900 flex items-center justify-center text-slate-600 dark:text-slate-400">
                  <Award size={20} />
                </div>
                <div>
                  <p className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Certificate</p>
                  <p className="text-sm font-bold text-slate-900 dark:text-slate-50">Included</p>
                </div>
              </div>
            </div>
          </div>

          <div className="space-y-6">
            <h2 className="text-2xl font-bold text-slate-900 dark:text-slate-50">Course Content</h2>
            <div className="space-y-4">
              {modules.map((mod: any, i: number) => (
                <div 
                  key={mod.id} 
                  id={`module-${mod.id}`}
                  className="bg-white dark:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700 shadow-sm overflow-hidden group transition-all hover:shadow-md"
                >
                  <div className="p-5 flex items-center justify-between">
                    <div className="flex items-center gap-4">
                      <div className="w-10 h-10 rounded-xl bg-slate-50 dark:bg-slate-900 flex items-center justify-center text-slate-400 group-hover:text-accent-600 transition-colors">
                        {isEnrolled ? <Play size={20} /> : <Lock size={20} />}
                      </div>
                      <div>
                        <p className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Module {i + 1}</p>
                        <h3 className="font-bold text-slate-900 dark:text-slate-50">{mod.title}</h3>
                      </div>
                    </div>
                    {isEnrolled && (
                      <div className="flex items-center gap-2">
                        <button
                          onClick={() => markModuleComplete(mod.id)}
                          disabled={completingModuleId === mod.id}
                          className="px-3 py-1 text-[11px] font-bold rounded-lg bg-emerald-100 text-emerald-700 hover:bg-emerald-200 disabled:opacity-50"
                        >
                          {completingModuleId === mod.id ? 'Saving...' : 'Mark Complete'}
                        </button>
                        <button className="p-2 text-slate-400 hover:text-accent-600 transition-colors">
                          <ChevronRight size={20} />
                        </button>
                      </div>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </div>

          {isEnrolled && courseExams.length > 0 && (
            <div className="space-y-4 bg-white dark:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700 p-5">
              <div className="flex items-center justify-between">
                <h2 className="text-xl font-bold text-slate-900 dark:text-slate-50">Final Course Exam</h2>
                <span className="text-xs font-bold text-slate-500">
                  {moduleProgress?.allModulesCompleted ? 'Unlocked' : 'Locked until all modules complete'}
                </span>
              </div>
              {courseExams.map((exam) => (
                <div key={exam.id} className="p-4 rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-900/40">
                  <div className="flex items-center justify-between gap-3">
                    <div>
                      <p className="text-sm font-bold text-slate-900 dark:text-slate-100">{exam.title}</p>
                      <p className="text-xs text-slate-500">{exam.questionCount || 0} questions • {exam.durationMinutes} mins</p>
                    </div>
                    <button
                      onClick={() => startFinalExam(exam.id)}
                      disabled={!moduleProgress?.allModulesCompleted}
                      className="px-4 py-2 rounded-xl text-xs font-bold bg-accent-600 text-white disabled:bg-slate-300"
                    >
                      Start Exam
                    </button>
                  </div>
                </div>
              ))}
              {examError && <p className="text-sm text-rose-600">{examError}</p>}
              {examResult && (
                <div className="p-4 rounded-xl border border-emerald-200 bg-emerald-50 text-emerald-800">
                  <p className="font-bold">Result: {examResult.passed ? 'Passed' : 'Not Passed'}</p>
                  <p className="text-sm">Score: {examResult.score}% ({examResult.correctAnswers}/{examResult.totalQuestions})</p>
                  {examResult.certificateIssued && <p className="text-sm">Certificate generated. Check your profile to view and download.</p>}
                </div>
              )}
            </div>
          )}

          {activeExam && (
            <div className="space-y-4 bg-white dark:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700 p-5">
              <h2 className="text-lg font-bold text-slate-900 dark:text-slate-50">{activeExam.examTitle}</h2>
              <p className="text-xs text-slate-500">Answer all questions and submit.</p>
              <div className="space-y-4">
                {(activeExam.questions || []).map((q: any, idx: number) => (
                  <div key={q.questionId} className="p-4 rounded-xl border border-slate-200 dark:border-slate-700">
                    <p className="text-sm font-bold text-slate-900 dark:text-slate-50 mb-2">Q{idx + 1}. {q.title || q.prompt}</p>
                    <div className="space-y-2">
                      {(q.options || []).map((opt: any) => (
                        <label key={opt.optionId} className="flex items-center gap-2 text-sm text-slate-700 dark:text-slate-300">
                          <input
                            type="radio"
                            name={`q-${q.questionId}`}
                            checked={selectedAnswers[q.questionId] === opt.optionId}
                            onChange={() => setSelectedAnswers((prev) => ({ ...prev, [q.questionId]: opt.optionId }))}
                          />
                          {opt.text}
                        </label>
                      ))}
                    </div>
                  </div>
                ))}
              </div>
              <div className="flex gap-2">
                <button
                  onClick={() => setActiveExam(null)}
                  className="px-4 py-2 rounded-xl text-xs font-bold bg-slate-200 text-slate-700"
                >
                  Cancel
                </button>
                <button
                  onClick={submitFinalExam}
                  disabled={examSubmitting}
                  className="px-4 py-2 rounded-xl text-xs font-bold bg-emerald-600 text-white disabled:opacity-60"
                >
                  {examSubmitting ? 'Submitting...' : 'Submit Exam'}
                </button>
              </div>
            </div>
          )}
        </div>

        <div className="lg:col-span-3 space-y-6">
          <div className="bg-white dark:bg-slate-800 p-8 rounded-3xl border border-slate-200 dark:border-slate-700 shadow-xl sticky top-24">
            <div className="aspect-video bg-slate-100 dark:bg-slate-900 rounded-2xl mb-6 overflow-hidden relative group">
              <div className="w-full h-full flex items-center justify-center text-slate-400">
                <Play size={48} fill="currentColor" />
              </div>
              <div className="absolute inset-0 bg-black/20 group-hover:bg-black/40 transition-colors flex items-center justify-center">
                <div className="w-16 h-16 rounded-full bg-white/90 dark:bg-slate-800/90 flex items-center justify-center text-accent-600 shadow-xl">
                  <Play size={32} fill="currentColor" />
                </div>
              </div>
            </div>

            <div className="space-y-6">
              <div className="flex items-center justify-between">
                <p className="text-3xl font-extrabold text-slate-900 dark:text-slate-50">
                  {course.price === 0 ? 'Free' : `$${course.price}`}
                </p>
                {course.price > 0 && (
                  <span className="text-xs font-bold text-slate-400 line-through">$99.99</span>
                )}
              </div>

              {isEnrolled ? (
                <div className="space-y-2">
                  <button 
                    disabled
                    className="w-full py-4 bg-emerald-100 dark:bg-emerald-900/20 text-emerald-600 dark:text-emerald-400 font-bold rounded-2xl flex items-center justify-center gap-2"
                  >
                    <CheckCircle size={20} />
                    Already Enrolled
                  </button>
                  <button
                    onClick={() => {
                      const el = document.getElementById('course-chat-card');
                      if (el) {
                        el.scrollIntoView({ behavior: 'smooth', block: 'center' });
                      }
                    }}
                    className="w-full py-3 bg-slate-900 hover:bg-slate-700 text-white font-bold rounded-2xl transition-all"
                  >
                    Chat with Tutor
                  </button>
                </div>
              ) : (
                <button 
                  onClick={handleEnroll}
                  disabled={enrolling}
                  className="w-full py-4 bg-accent-600 hover:bg-accent-700 text-white font-bold rounded-2xl transition-all shadow-lg shadow-accent-200 dark:shadow-none flex items-center justify-center gap-2"
                >
                  {enrolling ? 'Enrolling...' : 'Enroll Now'}
                </button>
              )}

              <div className="space-y-4 pt-6 border-t border-slate-100 dark:border-slate-800">
                <h4 className="text-sm font-bold text-slate-900 dark:text-slate-50">This course includes:</h4>
                <ul className="space-y-3">
                  <li className="flex items-center gap-3 text-sm text-slate-600 dark:text-slate-400">
                    <Play size={16} className="text-accent-600" />
                    {modules.length} Adaptive Modules
                  </li>
                  <li className="flex items-center gap-3 text-sm text-slate-600 dark:text-slate-400">
                    <FileText size={16} className="text-accent-600" />
                    Full Lifetime Access
                  </li>
                  <li className="flex items-center gap-3 text-sm text-slate-600 dark:text-slate-400">
                    <MessageSquare size={16} className="text-accent-600" />
                    Direct Tutor Support
                  </li>
                </ul>
              </div>

              {isEnrolled && (
                <div id="course-chat-card" className="space-y-3 pt-6 border-t border-slate-100 dark:border-slate-800">
                  <h4 className="text-sm font-bold text-slate-900 dark:text-slate-50">Private Chat with Tutor</h4>
                    {chatError && (
                      <p className="text-xs font-medium text-rose-600 dark:text-rose-400">{chatError}</p>
                    )}
                  <div className="max-h-48 overflow-y-auto rounded-xl border border-slate-200 dark:border-slate-700 p-3 space-y-2 bg-slate-50 dark:bg-slate-900/40">
                    {chatLoading ? (
                      <p className="text-xs text-slate-500">Loading messages...</p>
                    ) : chatMessages.length === 0 ? (
                      <p className="text-xs text-slate-500">No messages yet. Start the conversation.</p>
                    ) : (
                      chatMessages.map((message: any) => (
                        <div key={message.id} className="text-xs">
                          <p className="font-bold text-slate-700 dark:text-slate-200">{message.senderName}</p>
                          <p className="text-slate-600 dark:text-slate-400">{message.message}</p>
                        </div>
                      ))
                    )}
                  </div>
                  <div className="flex gap-2">
                    <input
                      value={chatInput}
                      onChange={(e) => setChatInput(e.target.value)}
                      onKeyDown={(e) => {
                        if (e.key === 'Enter') {
                          e.preventDefault();
                          handleSendChat();
                        }
                      }}
                      placeholder="Message tutor..."
                      className="flex-1 px-3 py-2 text-sm bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-lg outline-none focus:ring-2 focus:ring-accent-500"
                    />
                    <button
                      onClick={handleSendChat}
                      disabled={chatPosting || !chatInput.trim()}
                      className="px-3 py-2 text-sm font-bold bg-accent-600 hover:bg-accent-700 text-white rounded-lg disabled:opacity-50"
                    >
                      Send
                    </button>
                  </div>

                  <div className="space-y-3 pt-4 border-t border-slate-100 dark:border-slate-800">
                    <h4 className="text-sm font-bold text-slate-900 dark:text-slate-50">Course Group Chat (Tutor + All Learners)</h4>
                    {groupError && (
                      <p className="text-xs font-medium text-rose-600 dark:text-rose-400">{groupError}</p>
                    )}
                    <div className="max-h-48 overflow-y-auto rounded-xl border border-slate-200 dark:border-slate-700 p-3 space-y-2 bg-slate-50 dark:bg-slate-900/40">
                      {groupLoading ? (
                        <p className="text-xs text-slate-500">Loading group messages...</p>
                      ) : groupMessages.length === 0 ? (
                        <p className="text-xs text-slate-500">No group messages yet. Start the discussion.</p>
                      ) : (
                        groupMessages.map((message: any) => (
                          <div key={message.id} className="text-xs">
                            <p className="font-bold text-slate-700 dark:text-slate-200">{message.senderName}</p>
                            <p className="text-slate-600 dark:text-slate-400">{message.message}</p>
                          </div>
                        ))
                      )}
                    </div>
                    <div className="flex gap-2">
                      <input
                        value={groupInput}
                        onChange={(e) => setGroupInput(e.target.value)}
                        onKeyDown={(e) => {
                          if (e.key === 'Enter') {
                            e.preventDefault();
                            handleSendGroupChat();
                          }
                        }}
                        placeholder="Message course group..."
                        className="flex-1 px-3 py-2 text-sm bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-lg outline-none focus:ring-2 focus:ring-accent-500"
                      />
                      <button
                        onClick={handleSendGroupChat}
                        disabled={groupPosting || !groupInput.trim()}
                        className="px-3 py-2 text-sm font-bold bg-accent-600 hover:bg-accent-700 text-white rounded-lg disabled:opacity-50"
                      >
                        Send
                      </button>
                    </div>
                  </div>
                </div>
              )}
            </div>
          </div>

          {course.youtubeVideoUrl && (
            <a
              href={course.youtubeVideoUrl}
              target="_blank"
              rel="noreferrer"
              className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-red-50 dark:bg-red-900/20 text-red-700 dark:text-red-300 border border-red-200 dark:border-red-800 text-sm font-bold"
            >
              <Video size={16} /> Watch Intro Video
            </a>
          )}
        </div>
      </div>
    </div>
  );
};
