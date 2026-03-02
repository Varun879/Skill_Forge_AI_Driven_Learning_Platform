import React, { useState, useRef, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import Cropper, { Area } from 'react-easy-crop';
import getCroppedImg from '../utils/cropImage';
import { 
  User as UserIcon, 
  Mail, 
  Shield, 
  Lock, 
  Bell, 
  Trash2, 
  Check, 
  AlertCircle,
  Camera,
  Zap,
  Key,
  X,
  RotateCcw,
  Plus,
  Minus
} from 'lucide-react';
import api from '../services/api';
import { motion, AnimatePresence } from 'motion/react';

const cn = (...classes: any[]) => classes.filter(Boolean).join(' ');

export const Profile = () => {
  const { user, refreshUser, logout } = useAuth();
  const navigate = useNavigate();
  const [isEditing, setIsEditing] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [formData, setFormData] = useState({
    name: user?.name || '',
    email: user?.email || '',
  });
  const [passwordData, setPasswordData] = useState({
    current: '',
    new: '',
    confirm: ''
  });
  const [status, setStatus] = useState<{ type: 'success' | 'error', message: string } | null>(null);
  const [loading, setLoading] = useState(false);
  const [uploading, setUploading] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  // Cropping State
  const [imageToCrop, setImageToCrop] = useState<string | null>(null);
  const [crop, setCrop] = useState({ x: 0, y: 0 });
  const [zoom, setZoom] = useState(1);
  const [rotation, setRotation] = useState(0);
  const [croppedAreaPixels, setCroppedAreaPixels] = useState<Area | null>(null);
  const [showCropModal, setShowCropModal] = useState(false);

  const onCropComplete = useCallback((_: Area, b: Area) => {
    setCroppedAreaPixels(b);
  }, []);

  const handleImageClick = () => {
    fileInputRef.current?.click();
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    // Validation
    if (file.size > 5 * 1024 * 1024) {
      setStatus({ type: 'error', message: 'Image size must be less than 5MB' });
      return;
    }

    const allowedTypes = ['image/jpeg', 'image/png', 'image/webp'];
    if (!allowedTypes.includes(file.type)) {
      setStatus({ type: 'error', message: 'Only JPG, PNG and WEBP are allowed' });
      return;
    }

    const reader = new FileReader();
    reader.addEventListener('load', () => {
      setImageToCrop(reader.result as string);
      setShowCropModal(true);
      setZoom(1);
      setRotation(0);
    });
    reader.readAsDataURL(file);
  };

  const handleSaveCroppedImage = async () => {
    if (!imageToCrop || !croppedAreaPixels) return;

    setUploading(true);
    setStatus(null);

    try {
      const croppedImageBlob = await getCroppedImg(imageToCrop, croppedAreaPixels, rotation);
      if (!croppedImageBlob) throw new Error('Failed to crop image');

      const file = new File([croppedImageBlob], 'profile.jpg', { type: 'image/jpeg' });
      const formData = new FormData();
      formData.append('image', file);

      await api.post('/user/upload-profile-image', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });

      setStatus({ type: 'success', message: 'Profile picture updated!' });
      await refreshUser();
      setShowCropModal(false);
      setImageToCrop(null);
      setTimeout(() => setStatus(null), 3000);
    } catch (err: any) {
      setStatus({ type: 'error', message: err.response?.data?.error || 'Failed to upload image' });
    } finally {
      setUploading(false);
      if (fileInputRef.current) fileInputRef.current.value = '';
    }
  };

  const handleProfileUpdate = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setStatus(null);
    try {
      await api.put('/user/me', formData);
      setStatus({ type: 'success', message: 'Profile updated successfully!' });
      setIsEditing(false);
      await refreshUser();
      
      // Clear success message after 3 seconds
      setTimeout(() => setStatus(null), 3000);
    } catch (err: any) {
      setStatus({ type: 'error', message: err.response?.data?.error || 'Failed to update profile' });
    } finally {
      setLoading(false);
    }
  };

  const validatePassword = (pass: string) => {
    const checks = {
      length: pass.length >= 8,
      uppercase: /[A-Z]/.test(pass),
      number: /\d/.test(pass),
      special: /[!@#$%^&*]/.test(pass)
    };
    return checks;
  };

  const handlePasswordChange = async (e: React.FormEvent) => {
    e.preventDefault();
    setStatus(null);
    
    if (passwordData.new !== passwordData.confirm) {
      return setStatus({ type: 'error', message: 'Passwords do not match' });
    }
    
    const checks = validatePassword(passwordData.new);
    if (!Object.values(checks).every(Boolean)) {
      return setStatus({ type: 'error', message: 'Password does not meet complexity requirements' });
    }

    setLoading(true);
    try {
      await api.post('/user/change-password', {
        current: passwordData.current,
        new: passwordData.new
      });
      setStatus({ type: 'success', message: 'Password changed successfully!' });
      setPasswordData({ current: '', new: '', confirm: '' });
      setTimeout(() => setStatus(null), 3000);
    } catch (err: any) {
      setStatus({ type: 'error', message: err.response?.data?.error || 'Failed to change password' });
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteAccount = async () => {
    setLoading(true);
    try {
      await api.delete('/user/me');
      logout();
      navigate('/login');
    } catch (err: any) {
      setStatus({ type: 'error', message: err.response?.data?.error || 'Failed to delete account' });
      setShowDeleteModal(false);
    } finally {
      setLoading(false);
    }
  };

  const passChecks = validatePassword(passwordData.new);

  return (
    <div className="max-w-4xl mx-auto space-y-8 pb-12">
      <header>
        <h1 className="text-3xl font-bold tracking-tight mb-2 text-slate-900 dark:text-white">Account Settings</h1>
        <p className="text-slate-500 dark:text-slate-400 text-sm">Manage your personal information and security preferences.</p>
      </header>

      <AnimatePresence>
        {status && (
          <motion.div 
            initial={{ opacity: 0, y: -20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, scale: 0.95 }}
            className={cn(
              "p-4 rounded-2xl flex items-center gap-3 font-bold text-sm shadow-lg",
              status.type === 'success' 
                ? 'bg-sage-600 text-white' 
                : 'bg-rose-600 text-white'
            )}
          >
            {status.type === 'success' ? <Check size={20} /> : <AlertCircle size={20} />}
            {status.message}
            <button onClick={() => setStatus(null)} className="ml-auto p-1 hover:bg-white/20 rounded-lg">
              <Check size={16} />
            </button>
          </motion.div>
        )}
      </AnimatePresence>

      <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
        <div className="lg:col-span-1 space-y-6">
          <div className="bg-white dark:bg-slate-800 p-8 rounded-3xl border border-slate-200 dark:border-slate-700 shadow-sm text-center sticky top-24">
            <div className="relative w-32 h-32 mx-auto mb-6">
              <div className={cn(
                "w-full h-full rounded-full flex items-center justify-center text-sage-600 dark:text-sage-400 text-4xl font-bold overflow-hidden",
                !user?.profile_image && "bg-sage-50 dark:bg-sage-900/20"
              )}>
                {user?.profile_image ? (
                  <img 
                    src={user.profile_image} 
                    alt={user.name} 
                    className="w-full h-full object-cover block border-none outline-none"
                    onError={(e) => {
                      (e.target as HTMLImageElement).src = `https://ui-avatars.com/api/?name=${encodeURIComponent(user.name)}&background=f4f7f5&color=6a8d73`;
                    }}
                  />
                ) : (
                  user?.name.charAt(0)
                )}
                {uploading && (
                  <div className="absolute inset-0 bg-black/40 flex items-center justify-center">
                    <div className="w-6 h-6 border-2 border-white border-t-transparent rounded-full animate-spin" />
                  </div>
                )}
              </div>
              <input 
                type="file" 
                ref={fileInputRef}
                onChange={handleFileChange}
                accept="image/*"
                className="hidden"
              />
              <button 
                onClick={handleImageClick}
                disabled={uploading}
                className="absolute bottom-0 right-0 p-2.5 bg-white dark:bg-slate-800 rounded-full shadow-md text-slate-500 dark:text-slate-400 hover:text-sage-600 transition-all z-10"
              >
                <Camera size={18} />
              </button>
            </div>
            <h2 className="text-xl font-bold text-slate-900 dark:text-white">{user?.name}</h2>
            <p className="text-slate-500 dark:text-slate-400 text-sm mb-6">{user?.email}</p>
            <div className="inline-flex items-center gap-2 px-4 py-1.5 bg-sage-50 dark:bg-sage-900/20 text-sage-600 dark:text-sage-400 rounded-full text-[10px] font-bold border border-sage-100 dark:border-sage-800 uppercase tracking-widest">
              <Shield size={14} />
              {user?.role} Account
            </div>
          </div>
        </div>

        <div className="lg:col-span-3 space-y-8">
          <section className="bg-white dark:bg-slate-800 p-8 rounded-3xl border border-slate-200 dark:border-slate-700 shadow-sm">
            <div className="flex items-center justify-between mb-8">
              <h3 className="text-lg font-bold flex items-center gap-3 text-slate-900 dark:text-white">
                <UserIcon size={20} className="text-sage-600" />
                Profile Information
              </h3>
              {!isEditing && (
                <button 
                  onClick={() => setIsEditing(true)}
                  className="text-xs font-bold text-sage-600 hover:text-sage-700 uppercase tracking-widest"
                >
                  Edit Profile
                </button>
              )}
            </div>

            <form onSubmit={handleProfileUpdate} className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="space-y-2">
                  <label className="text-[10px] font-bold text-slate-500 dark:text-slate-400 uppercase tracking-widest ml-1">Full Name</label>
                  <input 
                    type="text" 
                    disabled={!isEditing}
                    value={formData.name}
                    onChange={(e) => setFormData({...formData, name: e.target.value})}
                    placeholder="Enter your full name"
                    className="w-full px-4 py-3 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl focus:ring-2 focus:ring-sage-500 outline-none transition-all disabled:opacity-50 text-slate-900 dark:text-white dark:placeholder:text-slate-600 text-sm"
                  />
                </div>
                <div className="space-y-2">
                  <label className="text-[10px] font-bold text-slate-500 dark:text-slate-400 uppercase tracking-widest ml-1">Email Address</label>
                  <input 
                    type="email" 
                    disabled={!isEditing}
                    value={formData.email}
                    onChange={(e) => setFormData({...formData, email: e.target.value})}
                    className="w-full px-4 py-3 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl focus:ring-2 focus:ring-sage-500 outline-none transition-all disabled:opacity-50 text-slate-900 dark:text-white dark:placeholder:text-slate-600 text-sm"
                  />
                </div>
              </div>

              {isEditing && (
                <div className="flex gap-3 pt-4">
                  <button 
                    type="submit"
                    disabled={loading}
                    className="px-8 py-3 bg-sage-600 hover:bg-sage-700 text-white font-bold rounded-xl transition-all disabled:opacity-50 shadow-lg shadow-sage-200 dark:shadow-none"
                  >
                    {loading ? 'Saving...' : 'Save Changes'}
                  </button>
                  <button 
                    type="button"
                    onClick={() => setIsEditing(false)}
                    className="px-8 py-3 bg-slate-100 dark:bg-slate-700 hover:bg-slate-200 dark:hover:bg-slate-600 text-slate-900 dark:text-white font-bold rounded-xl transition-all"
                  >
                    Cancel
                  </button>
                </div>
              )}
            </form>
          </section>

          <section className="bg-white dark:bg-slate-800 p-8 rounded-3xl border border-slate-200 dark:border-slate-700 shadow-sm">
            <h3 className="text-lg font-bold flex items-center gap-3 mb-8 text-slate-900 dark:text-white">
              <Key size={20} className="text-sage-600" />
              Security & Password
            </h3>

            <form onSubmit={handlePasswordChange} className="space-y-6">
              <div className="space-y-2">
                <label className="text-[10px] font-bold text-slate-500 dark:text-slate-400 uppercase tracking-widest ml-1">Current Password</label>
                <input 
                  type="password" 
                  value={passwordData.current}
                  onChange={(e) => setPasswordData({...passwordData, current: e.target.value})}
                  placeholder="••••••••"
                  className="w-full px-4 py-3 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl focus:ring-2 focus:ring-sage-500 outline-none transition-all text-slate-900 dark:text-white dark:placeholder:text-slate-600 text-sm"
                />
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="space-y-2">
                  <label className="text-[10px] font-bold text-slate-500 dark:text-slate-400 uppercase tracking-widest ml-1">New Password</label>
                  <input 
                    type="password" 
                    value={passwordData.new}
                    onChange={(e) => setPasswordData({...passwordData, new: e.target.value})}
                    placeholder="••••••••"
                    className="w-full px-4 py-3 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl focus:ring-2 focus:ring-sage-500 outline-none transition-all text-slate-900 dark:text-white dark:placeholder:text-slate-600 text-sm"
                  />
                </div>
                <div className="space-y-2">
                  <label className="text-[10px] font-bold text-slate-500 dark:text-slate-400 uppercase tracking-widest ml-1">Confirm New Password</label>
                  <input 
                    type="password" 
                    value={passwordData.confirm}
                    onChange={(e) => setPasswordData({...passwordData, confirm: e.target.value})}
                    placeholder="••••••••"
                    className="w-full px-4 py-3 bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl focus:ring-2 focus:ring-sage-500 outline-none transition-all text-slate-900 dark:text-white dark:placeholder:text-slate-600 text-sm"
                  />
                </div>
              </div>

              {passwordData.new && (
                <div className="p-4 bg-slate-50 dark:bg-slate-900 rounded-2xl border border-slate-100 dark:border-slate-800 space-y-2">
                  <p className="text-[10px] font-bold text-slate-500 dark:text-slate-400 uppercase tracking-widest mb-2">Password Requirements</p>
                  <div className="grid grid-cols-2 gap-2">
                    {[
                      { label: 'At least 8 characters', met: passChecks.length },
                      { label: 'One uppercase letter', met: passChecks.uppercase },
                      { label: 'One number', met: passChecks.number },
                      { label: 'One special character', met: passChecks.special },
                    ].map((check) => (
                      <div key={check.label} className="flex items-center gap-2">
                        <div className={cn("w-4 h-4 rounded-full flex items-center justify-center", check.met ? "bg-sage-600 text-white" : "bg-slate-200 dark:bg-slate-800 text-slate-400")}>
                          <Check size={10} />
                        </div>
                        <span className={cn("text-[10px] font-bold", check.met ? "text-sage-700" : "text-slate-400")}>{check.label}</span>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              <button 
                type="submit"
                disabled={loading}
                className="px-8 py-3 bg-slate-800 dark:bg-white dark:text-slate-900 text-white font-bold rounded-xl transition-all disabled:opacity-50 shadow-lg shadow-slate-200 dark:shadow-none"
              >
                {loading ? 'Updating...' : 'Update Password'}
              </button>
            </form>
          </section>

          <section className="bg-rose-50 dark:bg-rose-900/10 p-8 rounded-3xl border border-rose-100 dark:border-rose-900/30">
            <div className="flex items-start gap-6">
              <div className="p-3 bg-rose-100 dark:bg-rose-900/30 rounded-xl text-rose-600 dark:text-rose-400">
                <Trash2 size={24} />
              </div>
              <div>
                <h3 className="text-lg font-bold text-rose-900 dark:text-rose-100 mb-1">Delete Account</h3>
                <p className="text-sm text-rose-700 dark:text-rose-300 mb-6">Once you delete your account, all your progress, mastery data, and certification history will be permanently removed. This action is irreversible.</p>
                <button 
                  onClick={() => setShowDeleteModal(true)}
                  className="px-8 py-3 bg-rose-600 hover:bg-rose-700 text-white font-bold rounded-xl transition-all shadow-lg shadow-rose-200 dark:shadow-none"
                >
                  Delete My Account
                </button>
              </div>
            </div>
          </section>
        </div>
      </div>

      {/* Delete Confirmation Modal */}
      <AnimatePresence>
        {showDeleteModal && (
          <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm">
            <motion.div
              initial={{ opacity: 0, scale: 0.95, y: 20 }}
              animate={{ opacity: 1, scale: 1, y: 0 }}
              exit={{ opacity: 0, scale: 0.95, y: 20 }}
              className="bg-white dark:bg-slate-800 w-full max-w-md rounded-3xl p-8 shadow-2xl border border-slate-200 dark:border-slate-700"
            >
              <div className="w-16 h-16 bg-rose-100 dark:bg-rose-900/30 rounded-2xl flex items-center justify-center text-rose-600 dark:text-rose-400 mb-6">
                <AlertCircle size={32} />
              </div>
              <h2 className="text-2xl font-bold text-slate-900 dark:text-white mb-2">Are you sure?</h2>
              <p className="text-slate-500 dark:text-slate-400 mb-8">
                This action is irreversible. All your data, including progress, submissions, and courses will be permanently deleted.
              </p>
              <div className="flex gap-4">
                <button
                  onClick={() => setShowDeleteModal(false)}
                  className="flex-1 px-6 py-3 bg-slate-100 dark:bg-slate-700 hover:bg-slate-200 dark:hover:bg-slate-600 text-slate-900 dark:text-white font-bold rounded-xl transition-all"
                >
                  Cancel
                </button>
                <button
                  onClick={handleDeleteAccount}
                  disabled={loading}
                  className="flex-1 px-6 py-3 bg-rose-600 hover:bg-rose-700 text-white font-bold rounded-xl transition-all shadow-lg shadow-rose-200 dark:shadow-none disabled:opacity-50"
                >
                  {loading ? 'Deleting...' : 'Confirm Delete'}
                </button>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* Image Crop Modal */}
      <AnimatePresence>
        {showCropModal && imageToCrop && (
          <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-slate-900/80 backdrop-blur-md">
            <motion.div
              initial={{ opacity: 0, scale: 0.9, y: 40 }}
              animate={{ opacity: 1, scale: 1, y: 0 }}
              exit={{ opacity: 0, scale: 0.9, y: 40 }}
              className="bg-white dark:bg-slate-900 w-full max-w-2xl rounded-[2.5rem] overflow-hidden shadow-2xl border border-slate-200 dark:border-slate-800"
            >
              <div className="p-8 border-b border-slate-100 dark:border-slate-800 flex items-center justify-between">
                <div>
                  <h2 className="text-2xl font-bold text-slate-900 dark:text-white">Crop Profile Picture</h2>
                  <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">Adjust the image to fit perfectly.</p>
                </div>
                <button 
                  onClick={() => {
                    setShowCropModal(false);
                    setImageToCrop(null);
                  }}
                  className="p-2 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-full text-slate-400 transition-colors"
                >
                  <X size={24} />
                </button>
              </div>

              <div className="relative h-[400px] bg-slate-100 dark:bg-slate-950">
                <Cropper
                  image={imageToCrop}
                  crop={crop}
                  zoom={zoom}
                  rotation={rotation}
                  aspect={1}
                  cropShape="round"
                  showGrid={false}
                  onCropChange={setCrop}
                  onCropComplete={onCropComplete}
                  onZoomChange={setZoom}
                  onRotationChange={setRotation}
                />
              </div>

              <div className="p-8 space-y-8">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                  <div className="space-y-4">
                    <div className="flex items-center justify-between">
                      <label className="text-[10px] font-bold text-slate-500 dark:text-slate-400 uppercase tracking-widest flex items-center gap-2">
                        <Plus size={14} className="text-sage-600" />
                        Zoom
                      </label>
                      <span className="text-xs font-bold text-slate-900 dark:text-white">{Math.round(zoom * 100)}%</span>
                    </div>
                    <input
                      type="range"
                      value={zoom}
                      min={1}
                      max={3}
                      step={0.1}
                      aria-labelledby="Zoom"
                      onChange={(e) => setZoom(Number(e.target.value))}
                      className="w-full h-1.5 bg-slate-100 dark:bg-slate-800 rounded-lg appearance-none cursor-pointer accent-sage-600"
                    />
                  </div>

                  <div className="space-y-4">
                    <div className="flex items-center justify-between">
                      <label className="text-[10px] font-bold text-slate-500 dark:text-slate-400 uppercase tracking-widest flex items-center gap-2">
                        <RotateCcw size={14} className="text-sage-600" />
                        Rotation
                      </label>
                      <span className="text-xs font-bold text-slate-900 dark:text-white">{rotation}°</span>
                    </div>
                    <input
                      type="range"
                      value={rotation}
                      min={0}
                      max={360}
                      step={1}
                      aria-labelledby="Rotation"
                      onChange={(e) => setRotation(Number(e.target.value))}
                      className="w-full h-1.5 bg-slate-100 dark:bg-slate-800 rounded-lg appearance-none cursor-pointer accent-sage-600"
                    />
                  </div>
                </div>

                <div className="flex gap-4">
                  <button
                    onClick={() => {
                      setShowCropModal(false);
                      setImageToCrop(null);
                    }}
                    className="flex-1 px-8 py-4 bg-slate-100 dark:bg-slate-800 hover:bg-slate-200 dark:hover:bg-slate-700 text-slate-900 dark:text-white font-bold rounded-2xl transition-all"
                  >
                    Cancel
                  </button>
                  <button
                    onClick={handleSaveCroppedImage}
                    disabled={uploading}
                    className="flex-1 px-8 py-4 bg-sage-600 hover:bg-sage-700 text-white font-bold rounded-2xl transition-all shadow-lg shadow-sage-200 dark:shadow-none disabled:opacity-50 flex items-center justify-center gap-2"
                  >
                    {uploading ? (
                      <>
                        <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" />
                        Saving...
                      </>
                    ) : (
                      'Save Profile Picture'
                    )}
                  </button>
                </div>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>
    </div>
  );
};
