package com.skillforge.domain.chat.service;

import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillforge.common.enums.Role;
import com.skillforge.domain.chat.dto.ChatMessageResponse;
import com.skillforge.domain.chat.dto.ChatMessagesPageResponse;
import com.skillforge.domain.chat.dto.ChatRoomResponse;
import com.skillforge.domain.chat.dto.GroupChatMessageResponse;
import com.skillforge.domain.chat.dto.GroupChatMessagesPageResponse;
import com.skillforge.domain.chat.dto.StartChatRequest;
import com.skillforge.domain.chat.entity.ChatMessage;
import com.skillforge.domain.chat.entity.ChatRoom;
import com.skillforge.domain.chat.entity.ChatSenderRole;
import com.skillforge.domain.chat.entity.CourseGroupMessage;
import com.skillforge.domain.chat.repository.ChatMessageRepository;
import com.skillforge.domain.chat.repository.ChatRoomRepository;
import com.skillforge.domain.chat.repository.CourseGroupMessageRepository;
import com.skillforge.domain.course.entity.Course;
import com.skillforge.domain.course.repository.CourseEnrollmentRepository;
import com.skillforge.domain.course.repository.CourseRepository;
import com.skillforge.domain.user.entity.User;
import com.skillforge.domain.user.repository.UserRepository;
import com.skillforge.exception.BadRequestException;
import com.skillforge.exception.ResourceNotFoundException;
import com.skillforge.exception.UnauthorizedException;

@Service
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final CourseGroupMessageRepository courseGroupMessageRepository;
    private final CourseRepository courseRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatService(ChatRoomRepository chatRoomRepository,
                       ChatMessageRepository chatMessageRepository,
                       CourseGroupMessageRepository courseGroupMessageRepository,
                       CourseRepository courseRepository,
                       CourseEnrollmentRepository enrollmentRepository,
                       UserRepository userRepository,
                       SimpMessagingTemplate messagingTemplate) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.courseGroupMessageRepository = courseGroupMessageRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponse> listTutorRooms(String tutorEmail, Long courseId) {
        User tutor = getUserByEmail(tutorEmail);
        if (tutor.getRole() != Role.TUTOR) {
            throw new UnauthorizedException("Only tutors can view tutor chat rooms");
        }

        List<ChatRoom> rooms = courseId == null
            ? chatRoomRepository.findByTutorIdOrderByCreatedAtDesc(tutor.getId())
            : chatRoomRepository.findByTutorIdAndCourseIdOrderByCreatedAtDesc(tutor.getId(), courseId);

        return rooms.stream().map(ChatRoomResponse::from).toList();
    }

    @Transactional
    public ChatRoomResponse startChat(StartChatRequest request, String userEmail) {
        User current = getUserByEmail(userEmail);
        Course course = courseRepository.findById(request.getCourseId())
            .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        Long tutorId = course.getTutor().getId();
        Long studentId;

        if (current.getRole() == Role.LEARNER) {
            studentId = current.getId();
            validateEnrollment(course.getId(), studentId, tutorId);
        } else if (current.getRole() == Role.TUTOR) {
            if (!tutorId.equals(current.getId())) {
                throw new UnauthorizedException("Only the course tutor can start this chat");
            }
            if (request.getStudentId() == null) {
                throw new BadRequestException("studentId is required for tutor-initiated chat");
            }
            studentId = request.getStudentId();
            validateEnrollment(course.getId(), studentId, tutorId);
        } else {
            throw new UnauthorizedException("User role not allowed for chat");
        }

        User student = userRepository.findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        ChatRoom room = chatRoomRepository
            .findByCourseIdAndTutorIdAndStudentId(course.getId(), tutorId, studentId)
            .orElseGet(() -> {
                ChatRoom newRoom = new ChatRoom();
                newRoom.setCourse(course);
                newRoom.setTutor(course.getTutor());
                newRoom.setStudent(student);
                return chatRoomRepository.save(newRoom);
            });

        return ChatRoomResponse.from(room);
    }

    @Transactional(readOnly = true)
    public ChatMessagesPageResponse getMessages(Long roomId, int page, int size, String userEmail) {
        User current = getUserByEmail(userEmail);
        ChatRoom room = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new ResourceNotFoundException("Chat room not found"));
        assertRoomAccess(room, current);

        int safeSize = Math.min(Math.max(size, 1), 100);
        int safePage = Math.max(page, 0);

        Page<ChatMessage> messagePage = chatMessageRepository.findByChatRoomIdOrderByMessageTimeDesc(
            roomId,
            PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "messageTime"))
        );

        List<ChatMessageResponse> ordered = messagePage.getContent().stream()
            .map(ChatMessageResponse::from)
            .toList();
        Collections.reverse(ordered);

        return new ChatMessagesPageResponse(
            ChatRoomResponse.from(room),
            ordered,
            safePage,
            safeSize,
            messagePage.getTotalElements(),
            messagePage.hasNext()
        );
    }

    @Transactional
    public ChatMessageResponse sendMessage(Long roomId, String text, String userEmail) {
        User current = getUserByEmail(userEmail);
        ChatRoom room = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new ResourceNotFoundException("Chat room not found"));
        assertRoomAccess(room, current);

        String messageText = text == null ? "" : text.trim();
        if (messageText.isEmpty()) {
            throw new BadRequestException("Message cannot be empty");
        }

        ChatMessage message = new ChatMessage();
        message.setChatRoom(room);
        message.setSender(current);
        message.setSenderRole(current.getId().equals(room.getTutor().getId()) ? ChatSenderRole.TUTOR : ChatSenderRole.STUDENT);
        message.setMessage(messageText);

        ChatMessageResponse response = ChatMessageResponse.from(chatMessageRepository.save(message));
        messagingTemplate.convertAndSend("/topic/chat/room/" + roomId, response);
        return response;
    }

    @Transactional(readOnly = true)
    public GroupChatMessagesPageResponse getGroupMessages(Long courseId, int page, int size, String userEmail) {
        User current = getUserByEmail(userEmail);
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        assertCourseChatAccess(course, current);

        int safeSize = Math.min(Math.max(size, 1), 100);
        int safePage = Math.max(page, 0);

        Page<CourseGroupMessage> messagePage = courseGroupMessageRepository.findByCourseIdOrderByMessageTimeDesc(
            courseId,
            PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "messageTime"))
        );

        List<GroupChatMessageResponse> ordered = messagePage.getContent().stream()
            .map(GroupChatMessageResponse::from)
            .toList();
        Collections.reverse(ordered);

        return new GroupChatMessagesPageResponse(
            courseId,
            ordered,
            safePage,
            safeSize,
            messagePage.getTotalElements(),
            messagePage.hasNext()
        );
    }

    @Transactional
    public GroupChatMessageResponse sendGroupMessage(Long courseId, String text, String userEmail) {
        User current = getUserByEmail(userEmail);
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        assertCourseChatAccess(course, current);

        String messageText = text == null ? "" : text.trim();
        if (messageText.isEmpty()) {
            throw new BadRequestException("Message cannot be empty");
        }

        CourseGroupMessage message = new CourseGroupMessage();
        message.setCourse(course);
        message.setSender(current);
        message.setSenderRole(current.getRole() == Role.TUTOR ? ChatSenderRole.TUTOR : ChatSenderRole.STUDENT);
        message.setMessage(messageText);

        GroupChatMessageResponse response = GroupChatMessageResponse.from(courseGroupMessageRepository.save(message));
        messagingTemplate.convertAndSend("/topic/chat/course/" + courseId + "/group", response);
        return response;
    }

    private void validateEnrollment(Long courseId, Long studentId, Long tutorId) {
        boolean enrolled = enrollmentRepository.existsByCourseIdAndLearnerIdAndCourseTutorId(courseId, studentId, tutorId);
        if (!enrolled) {
            throw new UnauthorizedException("Student is not enrolled in this tutor's course");
        }
    }

    private void assertRoomAccess(ChatRoom room, User current) {
        boolean allowed = room.getTutor().getId().equals(current.getId()) || room.getStudent().getId().equals(current.getId());
        if (!allowed) {
            throw new UnauthorizedException("You do not have access to this chat room");
        }
    }

    private void assertCourseChatAccess(Course course, User current) {
        boolean tutorAccess = current.getRole() == Role.TUTOR && course.getTutor().getId().equals(current.getId());
        boolean learnerAccess = current.getRole() == Role.LEARNER
                && enrollmentRepository.existsByCourseIdAndLearnerId(course.getId(), current.getId());

        if (!tutorAccess && !learnerAccess) {
            throw new UnauthorizedException("You do not have access to this course chat");
        }
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
