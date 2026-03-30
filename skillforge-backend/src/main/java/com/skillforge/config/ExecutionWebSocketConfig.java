package com.skillforge.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.skillforge.domain.execution.websocket.ExecutionSocketHandler;

@Configuration
@EnableWebSocket
public class ExecutionWebSocketConfig implements WebSocketConfigurer {

    private final ExecutionSocketHandler executionSocketHandler;

    public ExecutionWebSocketConfig(ExecutionSocketHandler executionSocketHandler) {
        this.executionSocketHandler = executionSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(executionSocketHandler, "/ws/execution")
                .setAllowedOriginPatterns("*");
    }
}
