import React, { useEffect, useMemo, useState } from 'react';
import { MessageSquare, Send, Users } from 'lucide-react';
import api from '../../services/api';
import { subscribeTopic } from '../../services/chatSocket';

const unwrapData = <T,>(response: any): T => {
  if (response?.data?.data !== undefined) {
    return response.data.data as T;
  }
  return response?.data as T;
};

type Room = {
  id: number;
  courseId: number;
  courseTitle?: string;
  tutorId: number;
  tutorName: string;
  studentId: number;
  studentName: string;
};

type ChatMessage = {
  id: number;
  senderId: number;
  senderName: string;
  senderRole: string;
  message: string;
  timestamp: string;
};

type GroupMessage = {
  id: number;
  courseId: number;
  senderId: number;
  senderName: string;
  senderRole: string;
  message: string;
  timestamp: string;
};

export const TutorChat = () => {
  const [rooms, setRooms] = useState<Room[]>([]);
  const [selectedRoomId, setSelectedRoomId] = useState<number | null>(null);
  const [selectedCourseId, setSelectedCourseId] = useState<number | null>(null);
  const [privateMessages, setPrivateMessages] = useState<ChatMessage[]>([]);
  const [groupMessages, setGroupMessages] = useState<GroupMessage[]>([]);
  const [privateInput, setPrivateInput] = useState('');
  const [groupInput, setGroupInput] = useState('');
  const [activeTab, setActiveTab] = useState<'private' | 'group'>('private');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const courses = useMemo(() => {
    const map = new Map<number, string>();
    rooms.forEach((room) => {
      if (!map.has(room.courseId)) {
        map.set(room.courseId, room.courseTitle || `Course #${room.courseId}`);
      }
    });
    return Array.from(map.entries()).map(([id, label]) => ({ id, label }));
  }, [rooms]);

  const selectedRoom = useMemo(() => rooms.find((room) => room.id === selectedRoomId) || null, [rooms, selectedRoomId]);

  const loadRooms = async () => {
    try {
      const res = await api.get('/chat/tutor/rooms');
      const payload = unwrapData<Room[]>(res) || [];
      setRooms(payload);
      if (!selectedRoomId && payload.length > 0) {
        setSelectedRoomId(payload[0].id);
      }
      if (!selectedCourseId && payload.length > 0) {
        setSelectedCourseId(payload[0].courseId);
      }
    } catch (err) {
      console.error(err);
      setError('Unable to load chat rooms right now.');
    }
  };

  const loadPrivateMessages = async (roomId: number) => {
    try {
      const res = await api.get(`/chat/${roomId}`, { params: { page: 0, size: 60 } });
      const payload = unwrapData<any>(res);
      setPrivateMessages(Array.isArray(payload?.messages) ? payload.messages : []);
    } catch (err) {
      console.error(err);
    }
  };

  const loadGroupMessages = async (courseId: number) => {
    try {
      const res = await api.get(`/chat/course/${courseId}/group/messages`, { params: { page: 0, size: 80 } });
      const payload = unwrapData<any>(res);
      setGroupMessages(Array.isArray(payload?.messages) ? payload.messages : []);
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    const bootstrap = async () => {
      setLoading(true);
      await loadRooms();
      setLoading(false);
    };
    bootstrap();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (!selectedRoomId) return;
    loadPrivateMessages(selectedRoomId);

    const id = setInterval(() => {
      loadPrivateMessages(selectedRoomId);
    }, 5000);

    return () => clearInterval(id);
  }, [selectedRoomId]);

  useEffect(() => {
    if (!selectedRoomId) return;

    let subscription: any;
    subscribeTopic(`/topic/chat/room/${selectedRoomId}`, (frame) => {
      const incoming = JSON.parse(frame.body);
      setPrivateMessages((prev) => {
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
        console.error('Room subscription failed', err);
      });

    return () => {
      if (subscription) {
        subscription.unsubscribe();
      }
    };
  }, [selectedRoomId]);

  useEffect(() => {
    if (!selectedCourseId) return;
    loadGroupMessages(selectedCourseId);

    const id = setInterval(() => {
      loadGroupMessages(selectedCourseId);
    }, 5000);

    return () => clearInterval(id);
  }, [selectedCourseId]);

  useEffect(() => {
    if (!selectedCourseId) return;

    let subscription: any;
    subscribeTopic(`/topic/chat/course/${selectedCourseId}/group`, (frame) => {
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
        console.error('Group subscription failed', err);
      });

    return () => {
      if (subscription) {
        subscription.unsubscribe();
      }
    };
  }, [selectedCourseId]);

  const sendPrivateMessage = async () => {
    if (!selectedRoomId || !privateInput.trim()) return;
    const text = privateInput.trim();
    setPrivateInput('');
    try {
      await api.post('/chat/send', { roomId: selectedRoomId, message: text });
      await loadPrivateMessages(selectedRoomId);
    } catch (err) {
      console.error(err);
      setPrivateInput(text);
    }
  };

  const sendGroupMessage = async () => {
    if (!selectedCourseId || !groupInput.trim()) return;
    const text = groupInput.trim();
    setGroupInput('');
    try {
      await api.post(`/chat/course/${selectedCourseId}/group/messages`, { message: text });
      await loadGroupMessages(selectedCourseId);
    } catch (err) {
      console.error(err);
      setGroupInput(text);
    }
  };

  if (loading) {
    return <div className="flex items-center justify-center h-96">Loading chats...</div>;
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-slate-900 dark:text-slate-50">Tutor Chat Center</h1>
        <p className="text-slate-500 dark:text-slate-400 mt-1">Reply to learner DMs and manage course-wide group discussions in real time.</p>
      </div>

      {error && <p className="text-sm text-rose-600 dark:text-rose-400">{error}</p>}

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 min-h-[70vh]">
        <aside className="lg:col-span-4 bg-white dark:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700 overflow-hidden">
          <div className="flex border-b border-slate-200 dark:border-slate-700">
            <button
              onClick={() => setActiveTab('private')}
              className={`flex-1 py-3 text-sm font-bold ${activeTab === 'private' ? 'text-accent-600 border-b-2 border-accent-600' : 'text-slate-500'}`}
            >
              Private Chats
            </button>
            <button
              onClick={() => setActiveTab('group')}
              className={`flex-1 py-3 text-sm font-bold ${activeTab === 'group' ? 'text-accent-600 border-b-2 border-accent-600' : 'text-slate-500'}`}
            >
              Group Chats
            </button>
          </div>

          {activeTab === 'private' ? (
            <div className="max-h-[60vh] overflow-y-auto divide-y divide-slate-100 dark:divide-slate-700">
              {rooms.length === 0 ? (
                <p className="p-4 text-sm text-slate-500">No private chats yet. Learners can start chat from course page.</p>
              ) : (
                rooms.map((room) => (
                  <button
                    key={room.id}
                    onClick={() => {
                      setSelectedRoomId(room.id);
                      setSelectedCourseId(room.courseId);
                      setActiveTab('private');
                    }}
                    className={`w-full text-left p-4 ${selectedRoomId === room.id ? 'bg-accent-50 dark:bg-accent-900/20' : 'hover:bg-slate-50 dark:hover:bg-slate-700/50'}`}
                  >
                    <p className="text-sm font-bold text-slate-900 dark:text-slate-100">{room.studentName}</p>
                    <p className="text-xs text-slate-500">{room.courseTitle || `Course #${room.courseId}`}</p>
                  </button>
                ))
              )}
            </div>
          ) : (
            <div className="p-4 space-y-2">
              <p className="text-xs font-bold uppercase tracking-widest text-slate-500">Choose Course</p>
              {courses.length === 0 ? (
                <p className="text-sm text-slate-500">No courses with chat activity yet.</p>
              ) : (
                courses.map((course) => (
                  <button
                    key={course.id}
                    onClick={() => setSelectedCourseId(course.id)}
                    className={`w-full text-left p-3 rounded-xl border ${selectedCourseId === course.id ? 'border-accent-500 bg-accent-50 dark:bg-accent-900/20' : 'border-slate-200 dark:border-slate-700'}`}
                  >
                    <p className="text-sm font-semibold text-slate-900 dark:text-slate-100">{course.label}</p>
                  </button>
                ))
              )}
            </div>
          )}
        </aside>

        <section className="lg:col-span-8 bg-white dark:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700 flex flex-col">
          <div className="p-4 border-b border-slate-200 dark:border-slate-700">
            {activeTab === 'private' ? (
              <div className="flex items-center gap-2 text-slate-700 dark:text-slate-200">
                <MessageSquare size={18} />
                <span className="font-bold">{selectedRoom ? `DM with ${selectedRoom.studentName}` : 'Select a private chat'}</span>
              </div>
            ) : (
              <div className="flex items-center gap-2 text-slate-700 dark:text-slate-200">
                <Users size={18} />
                <span className="font-bold">{selectedCourseId ? `Course #${selectedCourseId} Group Chat` : 'Select a course group chat'}</span>
              </div>
            )}
          </div>

          <div className="flex-1 overflow-y-auto p-4 space-y-3 max-h-[52vh]">
            {activeTab === 'private' ? (
              privateMessages.length === 0 ? (
                <p className="text-sm text-slate-500">No messages yet.</p>
              ) : (
                privateMessages.map((message) => (
                  <div key={message.id} className="text-sm">
                    <p className="font-bold text-slate-700 dark:text-slate-200">{message.senderName}</p>
                    <p className="text-slate-600 dark:text-slate-400">{message.message}</p>
                  </div>
                ))
              )
            ) : (
              groupMessages.length === 0 ? (
                <p className="text-sm text-slate-500">No group messages yet.</p>
              ) : (
                groupMessages.map((message) => (
                  <div key={message.id} className="text-sm">
                    <p className="font-bold text-slate-700 dark:text-slate-200">{message.senderName}</p>
                    <p className="text-slate-600 dark:text-slate-400">{message.message}</p>
                  </div>
                ))
              )
            )}
          </div>

          <div className="p-4 border-t border-slate-200 dark:border-slate-700 flex gap-2">
            {activeTab === 'private' ? (
              <>
                <input
                  value={privateInput}
                  onChange={(e) => setPrivateInput(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter') {
                      e.preventDefault();
                      sendPrivateMessage();
                    }
                  }}
                  placeholder="Reply to learner..."
                  className="flex-1 px-3 py-2 text-sm bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-lg"
                />
                <button onClick={sendPrivateMessage} className="px-4 py-2 bg-accent-600 hover:bg-accent-700 text-white rounded-lg font-bold text-sm">
                  <Send size={14} />
                </button>
              </>
            ) : (
              <>
                <input
                  value={groupInput}
                  onChange={(e) => setGroupInput(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter') {
                      e.preventDefault();
                      sendGroupMessage();
                    }
                  }}
                  placeholder="Message all learners in this course..."
                  className="flex-1 px-3 py-2 text-sm bg-slate-50 dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-lg"
                />
                <button onClick={sendGroupMessage} className="px-4 py-2 bg-accent-600 hover:bg-accent-700 text-white rounded-lg font-bold text-sm">
                  <Send size={14} />
                </button>
              </>
            )}
          </div>
        </section>
      </div>
    </div>
  );
};
