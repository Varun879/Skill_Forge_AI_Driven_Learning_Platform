import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ProtectedRoute } from './components/ProtectedRoute';
import { Dashboard } from './pages/Dashboard';
import { Practice } from './pages/Practice';
import { ProblemDetail } from './pages/ProblemDetail';
import { Roadmap } from './pages/Roadmap';
import { Exam } from './pages/Exam';
import { Analytics } from './pages/Analytics';
import { Profile } from './pages/Profile';
import { Login } from './pages/Login';
import { Signup } from './pages/Signup';
import { ExploreCourses } from './pages/ExploreCourses';
import { CourseDetail } from './pages/CourseDetail';

// Tutor Pages
import { TutorDashboard } from './pages/tutor/TutorDashboard';
import { CourseManagement } from './pages/tutor/CourseManagement';
import { CreateCourse } from './pages/tutor/CreateCourse';
import { ModuleProblemCreation } from './pages/tutor/ModuleProblemCreation';
import { CreateProblem } from './pages/tutor/CreateProblem';
import { SubmissionsReview } from './pages/tutor/SubmissionsReview';
import { DoubtManagement } from './pages/tutor/DoubtManagement';
import { TutorAnalytics } from './pages/tutor/TutorAnalytics';
import { useAuth } from './context/AuthContext';

export default function App() {
  const { user } = useAuth();

  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/signup" element={<Signup />} />
      
      {/* Learner Routes */}
      <Route element={<ProtectedRoute allowedRoles={['Learner']} />}>
        <Route path="/" element={<Dashboard />} />
        <Route path="/explore" element={<ExploreCourses />} />
        <Route path="/courses/:id" element={<CourseDetail />} />
        <Route path="/practice" element={<Practice />} />
        <Route path="/practice/:id" element={<ProblemDetail />} />
        <Route path="/roadmap" element={<Roadmap />} />
        <Route path="/exam" element={<Exam />} />
        <Route path="/analytics" element={<Analytics />} />
      </Route>

      {/* Tutor Routes */}
      <Route element={<ProtectedRoute allowedRoles={['Tutor']} />}>
        <Route path="/tutor" element={<TutorDashboard />} />
        <Route path="/tutor/courses" element={<CourseManagement />} />
        <Route path="/tutor/courses/create" element={<CreateCourse />} />
        <Route path="/tutor/courses/:id/edit" element={<CreateCourse />} />
        <Route path="/tutor/courses/:id/modules" element={<ModuleProblemCreation />} />
        <Route path="/tutor/courses/:id/modules/:moduleId/problems/create" element={<CreateProblem />} />
        <Route path="/tutor/problems/create" element={<CreateProblem />} />
        <Route path="/tutor/submissions" element={<SubmissionsReview />} />
        <Route path="/tutor/doubts" element={<DoubtManagement />} />
        <Route path="/tutor/analytics" element={<TutorAnalytics />} />
      </Route>

      {/* Shared Protected Routes */}
      <Route element={<ProtectedRoute />}>
        <Route path="/profile" element={<Profile />} />
      </Route>
      
      <Route path="*" element={<Navigate to={user?.role === 'Tutor' ? "/tutor" : "/"} replace />} />
    </Routes>
  );
}
