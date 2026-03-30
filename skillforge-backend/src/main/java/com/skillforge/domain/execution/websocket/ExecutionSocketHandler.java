package com.skillforge.domain.execution.websocket;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ExecutionSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final Map<Long, Set<WebSocketSession>> sessionsByExecutionId = new ConcurrentHashMap<>();

    public ExecutionSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long executionId = parseExecutionId(session.getUri());
        if (executionId == null) {
            return;
        }
        sessionsByExecutionId
                .computeIfAbsent(executionId, key -> new CopyOnWriteArraySet<>())
                .add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessionsByExecutionId.values().forEach(set -> set.remove(session));
    }

    public void publishOutput(Long executionId, String chunk) {
        publish(executionId, "OUTPUT", Map.of("chunk", chunk));
    }

    public void publishStatus(Long executionId, String status) {
        publish(executionId, "STATUS", Map.of("status", status));
    }

    private void publish(Long executionId, String type, Map<String, Object> payload) {
        Set<WebSocketSession> sessions = sessionsByExecutionId.get(executionId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        String json;
        try {
            json = objectMapper.writeValueAsString(Map.of(
                    "type", type,
                    "executionId", executionId,
                    "payload", payload));
        } catch (Exception e) {
            return;
        }

        for (WebSocketSession session : sessions) {
            if (!session.isOpen()) {
                continue;
            }
            try {
                session.sendMessage(new TextMessage(json));
            } catch (IOException ignored) {
            }
        }
    }

    private Long parseExecutionId(URI uri) {
        if (uri == null || uri.getQuery() == null) {
            return null;
        }
        for (String token : uri.getQuery().split("&")) {
            String[] parts = token.split("=");
            if (parts.length == 2 && "executionId".equals(parts[0])) {
                try {
                    return Long.parseLong(parts[1]);
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        return null;
    }
}
