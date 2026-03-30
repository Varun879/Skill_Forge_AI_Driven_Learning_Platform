package com.skillforge.domain.chat.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.skillforge.common.response.ApiResponse;
import com.skillforge.domain.chat.dto.ChatMessageResponse;
import com.skillforge.domain.chat.dto.ChatMessagesPageResponse;
import com.skillforge.domain.chat.dto.ChatRoomResponse;
import com.skillforge.domain.chat.dto.GroupChatMessageResponse;
import com.skillforge.domain.chat.dto.GroupChatMessagesPageResponse;
import com.skillforge.domain.chat.dto.SendChatMessageRequest;
import com.skillforge.domain.chat.dto.SendGroupChatMessageRequest;
import com.skillforge.domain.chat.dto.StartChatRequest;
import com.skillforge.domain.chat.service.ChatService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/start")
    @PreAuthorize("hasAnyRole('LEARNER','TUTOR')")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> startChat(
            @Valid @RequestBody StartChatRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        ChatRoomResponse room = chatService.startChat(request, principal.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok("Chat room ready", room));
    }

    @GetMapping("/{roomId}")
    @PreAuthorize("hasAnyRole('LEARNER','TUTOR')")
    public ResponseEntity<ApiResponse<ChatMessagesPageResponse>> getMessages(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size,
            @AuthenticationPrincipal UserDetails principal) {
        ChatMessagesPageResponse response = chatService.getMessages(roomId, page, size, principal.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/tutor/rooms")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<ApiResponse<java.util.List<ChatRoomResponse>>> listTutorRooms(
            @RequestParam(required = false) Long courseId,
            @AuthenticationPrincipal UserDetails principal) {
        java.util.List<ChatRoomResponse> rooms = chatService.listTutorRooms(principal.getUsername(), courseId);
        return ResponseEntity.ok(ApiResponse.ok(rooms));
    }

    @PostMapping("/send")
    @PreAuthorize("hasAnyRole('LEARNER','TUTOR')")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessage(
            @Valid @RequestBody SendChatMessageRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        ChatMessageResponse message = chatService.sendMessage(request.getRoomId(), request.getMessage(), principal.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok("Message sent", message));
    }

    @GetMapping("/course/{courseId}/group/messages")
    @PreAuthorize("hasAnyRole('LEARNER','TUTOR')")
    public ResponseEntity<ApiResponse<GroupChatMessagesPageResponse>> getGroupMessages(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size,
            @AuthenticationPrincipal UserDetails principal) {
        GroupChatMessagesPageResponse response = chatService.getGroupMessages(courseId, page, size, principal.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/course/{courseId}/group/messages")
    @PreAuthorize("hasAnyRole('LEARNER','TUTOR')")
    public ResponseEntity<ApiResponse<GroupChatMessageResponse>> sendGroupMessage(
            @PathVariable Long courseId,
            @Valid @RequestBody SendGroupChatMessageRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        GroupChatMessageResponse message = chatService.sendGroupMessage(courseId, request.getMessage(), principal.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok("Message sent", message));
    }
}
