import React, { useState, useEffect } from 'react';
import { 
  Plus, 
  Video, 
  FileText, 
  Code2, 
  Save, 
  Pencil,
  ChevronRight,
  ArrowLeft,
  Trash2,
  GripVertical
} from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';
import { Link, useParams } from 'react-router-dom';
import api from '../../services/api';

interface Module {
  id: number;
  title: string;
  videoUrl: string;
  content: string;
  orderIndex: number;
}

interface PracticeQuestionLite {
  id: number;
  title: string;
  topic: string;
  difficultyLevel: string;
}

interface CourseExam {
  id: number;
  title: string;
  durationMinutes: number;
  published: boolean;
  questionCount: number;
}

const unwrapData = <T,>(response: any): T => {
  if (response?.data?.data !== undefined) {
    return response.data.data as T;
  }
  return response?.data as T;
};

export const ModuleProblemCreation = () => {
  const { id } = useParams();
  const [course, setCourse] = useState<any>(null);
  const [modules, setModules] = useState<Module[]>([]);
  const [questionPool, setQuestionPool] = useState<PracticeQuestionLite[]>([]);
  const [courseExams, setCourseExams] = useState<CourseExam[]>([]);
  const [loading, setLoading] = useState(true);
  const [showAddModule, setShowAddModule] = useState(false);
  const [newModule, setNewModule] = useState({ title: '', videoUrl: '', content: '' });
  const [editingModuleId, setEditingModuleId] = useState<number | null>(null);
  const [editModule, setEditModule] = useState({ title: '', videoUrl: '', content: '' });
  const [creatingExam, setCreatingExam] = useState(false);
  const [examError, setExamError] = useState('');
  const [examForm, setExamForm] = useState({
    title: '',
    description: '',
    durationMinutes: 45,
    published: true,
    selectedQuestionIds: [] as number[],
  });

  const normalizeModule = (mod: any): Module => ({
    id: Number(mod.id),
    title: mod.title || '',
    videoUrl: mod.videoUrl || mod.video_url || '',
    content: mod.content || mod.notes || '',
    orderIndex: mod.orderIndex || mod.order_index || 1,
  });

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [courseRes, modulesRes, questionRes, examRes] = await Promise.all([
          api.get(`/courses/${id}`),
          api.get(`/courses/${id}/modules`),
          api.get('/practice/questions'),
          api.get(`/courses/${id}/exams`),
        ]);
        setCourse(unwrapData<any>(courseRes));
        const rawModules = unwrapData<any[]>(modulesRes) || [];
        setModules(rawModules.map(normalizeModule));
        setQuestionPool(unwrapData<PracticeQuestionLite[]>(questionRes) || []);
        setCourseExams(unwrapData<CourseExam[]>(examRes) || []);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [id]);

  const handleAddModule = async () => {
    try {
      const res = await api.post(`/courses/${id}/modules`, {
        title: newModule.title,
        videoUrl: newModule.videoUrl,
        content: newModule.content,
        orderIndex: modules.length + 1,
      });
      const created = normalizeModule(unwrapData<any>(res));
      setModules([...modules, created]);
      setShowAddModule(false);
      setNewModule({ title: '', videoUrl: '', content: '' });
    } catch (err) {
      console.error(err);
    }
  };

  const startEditModule = (mod: Module) => {
    setEditingModuleId(mod.id);
    setEditModule({ title: mod.title, videoUrl: mod.videoUrl || '', content: mod.content || '' });
  };

  const saveEditModule = async (moduleId: number) => {
    try {
      const existing = modules.find((m) => m.id === moduleId);
      if (!existing) return;

      const res = await api.put(`/courses/${id}/modules/${moduleId}`, {
        title: editModule.title,
        videoUrl: editModule.videoUrl,
        content: editModule.content,
        orderIndex: existing.orderIndex,
      });

      const updated = normalizeModule(unwrapData<any>(res));
      setModules((prev) => prev.map((m) => (m.id === moduleId ? updated : m)));
      setEditingModuleId(null);
    } catch (err) {
      console.error(err);
    }
  };

  const deleteModule = async (moduleId: number) => {
    try {
      await api.delete(`/courses/${id}/modules/${moduleId}`);
      setModules((prev) => prev.filter((m) => m.id !== moduleId));
      if (editingModuleId === moduleId) {
        setEditingModuleId(null);
      }
    } catch (err) {
      console.error(err);
    }
  };

  const toggleQuestionSelection = (questionId: number) => {
    setExamForm((prev) => ({
      ...prev,
      selectedQuestionIds: prev.selectedQuestionIds.includes(questionId)
        ? prev.selectedQuestionIds.filter((id) => id !== questionId)
        : [...prev.selectedQuestionIds, questionId],
    }));
  };

  const createCourseExam = async () => {
    if (!id) return;
    setExamError('');

    if (!examForm.title.trim()) {
      setExamError('Exam title is required.');
      return;
    }
    if (examForm.selectedQuestionIds.length < 3) {
      setExamError('Select at least 3 questions for the exam.');
      return;
    }

    setCreatingExam(true);
    try {
      const res = await api.post(`/courses/${id}/exams`, {
        title: examForm.title.trim(),
        description: examForm.description.trim(),
        durationMinutes: Number(examForm.durationMinutes),
        questionIds: examForm.selectedQuestionIds,
        published: examForm.published,
      });
      const createdExam = unwrapData<CourseExam>(res);
      setCourseExams((prev) => [createdExam, ...prev]);
      setExamForm({
        title: '',
        description: '',
        durationMinutes: 45,
        published: true,
        selectedQuestionIds: [],
      });
    } catch (err) {
      console.error(err);
      setExamError((err as any)?.response?.data?.message || 'Unable to create exam right now.');
    } finally {
      setCreatingExam(false);
    }
  };

  if (loading) return <div className="flex items-center justify-center h-96">Loading...</div>;

  return (
    <div className="max-w-5xl mx-auto space-y-8">
      <div className="flex items-center justify-between">
        <Link to="/tutor/courses" className="flex items-center gap-2 text-slate-500 hover:text-slate-900 dark:hover:text-slate-50 transition-colors font-bold">
          <ArrowLeft size={20} />
          Back to Courses
        </Link>
        <div className="flex items-center gap-3">
          <span className="text-sm font-bold text-slate-400">Course:</span>
          <span className="text-sm font-bold text-slate-900 dark:text-slate-50">{course?.title}</span>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2 space-y-6">
          <div className="flex items-center justify-between">
            <h2 className="text-xl font-bold text-slate-900 dark:text-slate-50">Course Modules</h2>
            <button 
              onClick={() => setShowAddModule(true)}
              className="flex items-center gap-2 px-4 py-2 bg-accent-600 hover:bg-accent-700 text-white font-bold rounded-xl text-sm transition-all"
            >
              <Plus size={18} />
              Add Module
            </button>
          </div>

          <div className="space-y-4">
            {modules.map((mod, i) => (
              <div key={mod.id} className="bg-white dark:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700 shadow-sm overflow-hidden">
                <div className="p-4 flex items-center justify-between bg-slate-50/50 dark:bg-slate-900/50 border-b border-slate-100 dark:border-slate-700">
                  <div className="flex items-center gap-3">
                    <div className="p-2 bg-white dark:bg-slate-800 rounded-lg border border-slate-200 dark:border-slate-700 text-slate-400">
                      <GripVertical size={16} />
                    </div>
                    <span className="text-xs font-bold text-slate-400 uppercase tracking-widest">Module {i + 1}</span>
                    <h3 className="font-bold text-slate-900 dark:text-slate-50">{mod.title}</h3>
                  </div>
                  <div className="flex items-center gap-2">
                    <button
                      onClick={() => startEditModule(mod)}
                      className="p-2 text-slate-400 hover:text-accent-600 transition-colors"
                      title="Edit module"
                    >
                      <Pencil size={18} />
                    </button>
                    <button
                      onClick={() => deleteModule(mod.id)}
                      className="p-2 text-slate-400 hover:text-rose-600 transition-colors"
                      title="Delete module"
                    >
                      <Trash2 size={18} />
                    </button>
                  </div>
                </div>
                <div className="p-6 grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div className="space-y-4">
                    <div className="flex items-center gap-3 text-sm text-slate-600 dark:text-slate-400">
                      <Video size={18} className="text-accent-600" />
                      <span className="font-medium truncate">{mod.videoUrl || 'No video linked'}</span>
                    </div>
                    <div className="flex items-start gap-3 text-sm text-slate-600 dark:text-slate-400">
                      <FileText size={18} className="text-emerald-600" />
                      <p className="font-medium line-clamp-2">{mod.content || 'No notes added'}</p>
                    </div>
                  </div>
                  <div className="flex flex-col justify-end gap-2">
                    <Link 
                      to={`/tutor/courses/${id}/modules/${mod.id}/problems/create`}
                      className="w-full py-2 bg-accent-50 dark:bg-accent-900/20 text-accent-600 dark:text-accent-400 font-bold rounded-xl text-center text-xs border border-accent-100 dark:border-accent-800 hover:bg-accent-100 transition-all"
                    >
                      Add Problems to Module
                    </Link>
                  </div>
                </div>

                {editingModuleId === mod.id && (
                  <div className="px-6 pb-6">
                    <div className="p-4 rounded-xl border border-accent-200 bg-accent-50/60 dark:bg-accent-900/20 dark:border-accent-800 space-y-3">
                      <input
                        type="text"
                        placeholder="Module title"
                        value={editModule.title}
                        onChange={(e) => setEditModule((prev) => ({ ...prev, title: e.target.value }))}
                        className="w-full px-4 py-3 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl outline-none dark:text-slate-100"
                      />
                      <input
                        type="text"
                        placeholder="Video URL"
                        value={editModule.videoUrl}
                        onChange={(e) => setEditModule((prev) => ({ ...prev, videoUrl: e.target.value }))}
                        className="w-full px-4 py-3 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl outline-none dark:text-slate-100"
                      />
                      <textarea
                        placeholder="Module notes"
                        value={editModule.content}
                        onChange={(e) => setEditModule((prev) => ({ ...prev, content: e.target.value }))}
                        rows={3}
                        className="w-full px-4 py-3 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl outline-none dark:text-slate-100 resize-none"
                      />
                      <div className="flex gap-2">
                        <button
                          onClick={() => setEditingModuleId(null)}
                          className="px-4 py-2 rounded-xl bg-slate-200 text-slate-700 font-bold text-xs"
                        >
                          Cancel
                        </button>
                        <button
                          onClick={() => saveEditModule(mod.id)}
                          className="px-4 py-2 rounded-xl bg-accent-600 text-white font-bold text-xs inline-flex items-center gap-2"
                        >
                          <Save size={14} />
                          Save Changes
                        </button>
                      </div>
                    </div>
                  </div>
                )}
              </div>
            ))}

            {showAddModule && (
              <motion.div 
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                className="bg-white dark:bg-slate-800 rounded-2xl border-2 border-accent-500 p-6 space-y-4 shadow-xl shadow-accent-500/10"
              >
                <div className="grid grid-cols-1 gap-4">
                  <input 
                    type="text"
                    placeholder="Module Title"
                    value={newModule.title}
                    onChange={e => setNewModule({...newModule, title: e.target.value})}
                    className="w-full px-4 py-3 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl outline-none dark:text-slate-100"
                  />
                  <input 
                    type="text"
                    placeholder="Video URL (YouTube/Vimeo)"
                    value={newModule.videoUrl}
                    onChange={e => setNewModule({...newModule, videoUrl: e.target.value})}
                    className="w-full px-4 py-3 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl outline-none dark:text-slate-100"
                  />
                  <textarea 
                    placeholder="Module Notes (Markdown supported)"
                    value={newModule.content}
                    onChange={e => setNewModule({...newModule, content: e.target.value})}
                    rows={3}
                    className="w-full px-4 py-3 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl outline-none dark:text-slate-100 resize-none"
                  />
                </div>
                <div className="flex items-center gap-3">
                  <button 
                    onClick={() => setShowAddModule(false)}
                    className="flex-1 py-3 bg-slate-100 dark:bg-slate-900 text-slate-600 dark:text-slate-400 font-bold rounded-xl"
                  >
                    Cancel
                  </button>
                  <button 
                    onClick={handleAddModule}
                    className="flex-1 py-3 bg-accent-600 text-white font-bold rounded-xl"
                  >
                    Save Module
                  </button>
                </div>
              </motion.div>
            )}
          </div>
        </div>

        <div className="space-y-6">
          <div className="bg-white dark:bg-slate-800 p-6 rounded-2xl border border-slate-200 dark:border-slate-700 shadow-sm">
            <h2 className="text-lg font-bold text-slate-900 dark:text-slate-50 mb-4">Course Progress</h2>
            <div className="space-y-4">
              <div className="flex items-center justify-between text-sm">
                <span className="text-slate-500 dark:text-slate-400">Modules Created</span>
                <span className="font-bold text-slate-900 dark:text-slate-50">{modules.length}</span>
              </div>
              <div className="w-full h-2 bg-slate-100 dark:bg-slate-900 rounded-full overflow-hidden">
                <div className="h-full bg-accent-600 rounded-full" style={{ width: `${Math.min(100, (modules.length / 5) * 100)}%` }} />
              </div>
              <p className="text-xs text-slate-400 italic">Recommended: At least 5 modules for a comprehensive course.</p>
            </div>
          </div>

          <div className="bg-emerald-600 p-6 rounded-2xl text-white shadow-lg shadow-emerald-200 dark:shadow-none">
            <h3 className="font-bold mb-2">Publishing Checklist</h3>
            <ul className="space-y-2 text-sm opacity-90">
              <li className="flex items-center gap-2">
                <CheckCircle size={16} /> Course details complete
              </li>
              <li className="flex items-center gap-2">
                <CheckCircle size={16} /> At least 1 module added
              </li>
              <li className="flex items-center gap-2">
                <div className="w-4 h-4 border-2 border-white/50 rounded-full" /> Add practice problems
              </li>
            </ul>
          </div>

          <div className="bg-white dark:bg-slate-800 p-6 rounded-2xl border border-slate-200 dark:border-slate-700 shadow-sm space-y-4">
            <h3 className="text-base font-bold text-slate-900 dark:text-slate-50">Course Exams</h3>
            {courseExams.length === 0 ? (
              <p className="text-xs text-slate-500">No exams created yet for this course.</p>
            ) : (
              <div className="space-y-2">
                {courseExams.map((exam) => (
                  <div key={exam.id} className="p-3 rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-900/40">
                    <p className="text-sm font-bold text-slate-900 dark:text-slate-100">{exam.title}</p>
                    <p className="text-xs text-slate-500">{exam.questionCount} questions • {exam.durationMinutes} mins • {exam.published ? 'Published' : 'Draft'}</p>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>

      <div className="bg-white dark:bg-slate-800 p-6 rounded-2xl border border-slate-200 dark:border-slate-700 shadow-sm space-y-5">
        <div className="flex items-center justify-between gap-3">
          <h2 className="text-lg font-bold text-slate-900 dark:text-slate-50">Create Course Exam</h2>
          <span className="text-xs text-slate-500">Select questions from practice bank</span>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <input
            type="text"
            placeholder="Exam title"
            value={examForm.title}
            onChange={(e) => setExamForm((prev) => ({ ...prev, title: e.target.value }))}
            className="w-full px-4 py-3 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl outline-none dark:text-slate-100"
          />
          <input
            type="number"
            min={5}
            max={300}
            placeholder="Duration minutes"
            value={examForm.durationMinutes}
            onChange={(e) => setExamForm((prev) => ({ ...prev, durationMinutes: Number(e.target.value) }))}
            className="w-full px-4 py-3 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl outline-none dark:text-slate-100"
          />
        </div>

        <textarea
          placeholder="Exam description (optional)"
          value={examForm.description}
          onChange={(e) => setExamForm((prev) => ({ ...prev, description: e.target.value }))}
          rows={3}
          className="w-full px-4 py-3 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl outline-none dark:text-slate-100 resize-none"
        />

        <label className="flex items-center gap-2 text-sm text-slate-700 dark:text-slate-300">
          <input
            type="checkbox"
            checked={examForm.published}
            onChange={(e) => setExamForm((prev) => ({ ...prev, published: e.target.checked }))}
          />
          Publish exam immediately
        </label>

        <div className="max-h-72 overflow-y-auto rounded-xl border border-slate-200 dark:border-slate-700">
          {questionPool.length === 0 ? (
            <p className="p-4 text-sm text-slate-500">No practice questions available yet.</p>
          ) : (
            <div className="divide-y divide-slate-100 dark:divide-slate-700">
              {questionPool.map((question) => {
                const checked = examForm.selectedQuestionIds.includes(question.id);
                return (
                  <label key={question.id} className="flex items-start gap-3 p-3 text-sm cursor-pointer hover:bg-slate-50 dark:hover:bg-slate-900/30">
                    <input
                      type="checkbox"
                      checked={checked}
                      onChange={() => toggleQuestionSelection(question.id)}
                    />
                    <div>
                      <p className="font-semibold text-slate-800 dark:text-slate-100">{question.title}</p>
                      <p className="text-xs text-slate-500">{question.topic} • {question.difficultyLevel}</p>
                    </div>
                  </label>
                );
              })}
            </div>
          )}
        </div>

        <div className="flex items-center justify-between gap-4">
          <p className="text-xs text-slate-500">Selected questions: {examForm.selectedQuestionIds.length}</p>
          <button
            onClick={createCourseExam}
            disabled={creatingExam}
            className="px-5 py-2.5 rounded-xl bg-accent-600 hover:bg-accent-700 text-white font-bold text-sm disabled:opacity-60"
          >
            {creatingExam ? 'Creating...' : 'Create Exam'}
          </button>
        </div>

        {examError && <p className="text-sm text-rose-600 dark:text-rose-400">{examError}</p>}
      </div>
    </div>
  );
};

const CheckCircle = ({ size }: { size: number }) => (
  <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round">
    <polyline points="20 6 9 17 4 12" />
  </svg>
);
