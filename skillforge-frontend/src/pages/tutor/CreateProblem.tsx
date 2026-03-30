import React, { useState } from 'react';
import { 
  ArrowLeft, 
  Save, 
  Code2, 
  Terminal, 
  FileJson,
  Plus,
  Trash2
} from 'lucide-react';
import { motion } from 'motion/react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import api from '../../services/api';

export const CreateProblem = () => {
  const { id, moduleId } = useParams();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    id: '',
    title: '',
    difficulty: 'Easy',
    topic_tags: '',
    description: '',
    constraints: '',
    edge_cases: '',
    samples: '',
    tests: '',
    mastery_impact: 5,
    estimated_time: '15 mins'
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      await api.post('/tutor/problems', { 
        ...formData, 
        module_id: moduleId ? Number(moduleId) : null,
        constraints: JSON.stringify(formData.constraints.split('\n').filter(Boolean)),
        edge_cases: JSON.stringify(formData.edge_cases.split('\n').filter(Boolean)),
        samples: formData.samples || '[]',
        tests: formData.tests || '[]'
      });
      navigate(id ? `/tutor/courses/${id}/modules` : '/tutor/dashboard');
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-8">
      <div className="flex items-center justify-between">
        <Link to={id ? `/tutor/courses/${id}/modules` : "/tutor/dashboard"} className="flex items-center gap-2 text-slate-500 hover:text-slate-900 dark:hover:text-slate-50 transition-colors font-bold">
          <ArrowLeft size={20} />
          Back
        </Link>
      </div>

      <div className="bg-white dark:bg-slate-800 rounded-3xl border border-slate-200 dark:border-slate-700 shadow-xl overflow-hidden">
        <div className="p-8 border-b border-slate-100 dark:border-slate-700 bg-slate-50/50 dark:bg-slate-900/50">
          <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-50">Create New Problem</h1>
          <p className="text-slate-500 dark:text-slate-400 mt-1">Design a coding challenge for your students.</p>
        </div>

        <form onSubmit={handleSubmit} className="p-8 space-y-8">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-2">
              <label className="text-sm font-bold text-slate-900 dark:text-slate-50">Problem ID (unique slug)</label>
              <input 
                type="text"
                value={formData.id}
                onChange={e => setFormData({...formData, id: e.target.value})}
                placeholder="e.g. reverse-linked-list"
                className="w-full px-4 py-3 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl outline-none dark:text-slate-100"
                required
              />
            </div>
            <div className="space-y-2">
              <label className="text-sm font-bold text-slate-900 dark:text-slate-50">Problem Title</label>
              <input 
                type="text"
                value={formData.title}
                onChange={e => setFormData({...formData, title: e.target.value})}
                placeholder="e.g. Reverse a Linked List"
                className="w-full px-4 py-3 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl outline-none dark:text-slate-100"
                required
              />
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-2">
              <label className="text-sm font-bold text-slate-900 dark:text-slate-50">Difficulty</label>
              <select 
                value={formData.difficulty}
                onChange={e => setFormData({...formData, difficulty: e.target.value})}
                className="w-full px-4 py-3 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl outline-none dark:text-slate-100"
              >
                <option>Easy</option>
                <option>Medium</option>
                <option>Hard</option>
              </select>
            </div>
            <div className="space-y-2">
              <label className="text-sm font-bold text-slate-900 dark:text-slate-50">Topic Tags (comma separated)</label>
              <input 
                type="text"
                value={formData.topic_tags}
                onChange={e => setFormData({...formData, topic_tags: e.target.value})}
                placeholder="e.g. Arrays, Recursion"
                className="w-full px-4 py-3 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl outline-none dark:text-slate-100"
              />
            </div>
          </div>

          <div className="space-y-2">
            <label className="text-sm font-bold text-slate-900 dark:text-slate-50">Description (Markdown)</label>
            <textarea 
              value={formData.description}
              onChange={e => setFormData({...formData, description: e.target.value})}
              rows={6}
              className="w-full px-4 py-3 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl outline-none dark:text-slate-100 resize-none"
              required
            />
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="space-y-2">
              <label className="text-sm font-bold text-slate-900 dark:text-slate-50">Constraints (one per line)</label>
              <textarea 
                value={formData.constraints}
                onChange={e => setFormData({...formData, constraints: e.target.value})}
                rows={4}
                className="w-full px-4 py-3 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl outline-none dark:text-slate-100 resize-none font-mono text-xs"
              />
            </div>
            <div className="space-y-2">
              <label className="text-sm font-bold text-slate-900 dark:text-slate-50">Edge Cases (one per line)</label>
              <textarea 
                value={formData.edge_cases}
                onChange={e => setFormData({...formData, edge_cases: e.target.value})}
                rows={4}
                className="w-full px-4 py-3 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl outline-none dark:text-slate-100 resize-none font-mono text-xs"
              />
            </div>
          </div>

          <div className="space-y-2">
            <label className="text-sm font-bold text-slate-900 dark:text-slate-50 flex items-center gap-2">
              <FileJson size={16} className="text-accent-600" />
              Sample Test Cases (JSON Array)
            </label>
            <textarea 
              value={formData.samples}
              onChange={e => setFormData({...formData, samples: e.target.value})}
              placeholder='[{"input": "...", "output": "...", "explanation": "..."}]'
              rows={4}
              className="w-full px-4 py-3 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl outline-none dark:text-slate-100 resize-none font-mono text-xs"
            />
          </div>

          <div className="space-y-2">
            <label className="text-sm font-bold text-slate-900 dark:text-slate-50 flex items-center gap-2">
              <Terminal size={16} className="text-rose-600" />
              Hidden Test Cases (JSON Array)
            </label>
            <textarea 
              value={formData.tests}
              onChange={e => setFormData({...formData, tests: e.target.value})}
              placeholder='[{"input": "...", "output": "..."}]'
              rows={4}
              className="w-full px-4 py-3 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl outline-none dark:text-slate-100 resize-none font-mono text-xs"
            />
          </div>

          <button 
            type="submit"
            disabled={loading}
            className="w-full py-4 bg-accent-600 hover:bg-accent-700 text-white font-bold rounded-2xl transition-all shadow-lg shadow-accent-200 dark:shadow-none flex items-center justify-center gap-2"
          >
            <Save size={20} />
            {loading ? 'Creating Problem...' : 'Create Problem'}
          </button>
        </form>
      </div>
    </div>
  );
};
