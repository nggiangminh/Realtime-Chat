package com.learning.server.config;

import com.learning.server.controller.ChatWebSocketController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

/**
 * WebSocket Event Listener để xử lý connect/disconnect events
 * Tuân thủ thiết kế trong design.md
 */
@Component
@Slf4j
public class WebSocketEventListener {

    @Autowired
    private ChatWebSocketController chatWebSocketController;

    /**
     * Xử lý khi user connect tới WebSocket
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = accessor.getUser();

        if (user != null) {
            String userEmail = user.getName();
            log.info("User connected: {}, Session ID: {}", userEmail, accessor.getSessionId());

            // Xử lý user online
            chatWebSocketController.handleUserConnect(userEmail);
        }
    }

    /**
     * Xử lý khi user disconnect khỏi WebSocket
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal user = accessor.getUser();

        if (user != null) {
            String userEmail = user.getName();
            log.info("User disconnected: {}, Session ID: {}", userEmail, accessor.getSessionId());

            // Xử lý user offline
            chatWebSocketController.handleUserDisconnect(userEmail);
        }
    }
}
