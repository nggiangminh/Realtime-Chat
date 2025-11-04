package com.learning.server.config;

import com.learning.server.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

/**
 * WebSocket Configuration cho STOMP over WebSocket
 * Tuân thủ thiết kế trong design.md
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple broker for destinations with prefixes "/topic" and "/queue"
        config.enableSimpleBroker("/topic", "/queue");

        // Set application destination prefix for @MessageMapping
        config.setApplicationDestinationPrefixes("/app");

        // Set user destination prefix for user-specific messages
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register WebSocket endpoint
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Allow all origins for development
                .withSockJS(); // Enable SockJS fallback
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Add JWT authentication interceptor
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // Get JWT token from header
                    List<String> authorization = accessor.getNativeHeader("Authorization");

                    if (authorization != null && !authorization.isEmpty()) {
                        String token = authorization.get(0);

                        // Remove "Bearer " prefix if present
                        if (token.startsWith("Bearer ")) {
                            token = token.substring(7);
                        }

                        try {
                            // Validate token and set authentication
                            if (jwtTokenProvider.validateToken(token)) {
                                Long userId = jwtTokenProvider.getUserIdFromToken(token);
                                String email = jwtTokenProvider.getEmailFromToken(token);

                                // Create authentication object with email as principal (not userId)
                                // This allows principal.getName() to return email in controllers
                                UsernamePasswordAuthenticationToken auth =
                                    new UsernamePasswordAuthenticationToken(email, null, null);

                                // Set authentication in accessor
                                accessor.setUser(auth);
                                SecurityContextHolder.getContext().setAuthentication(auth);
                            }
                        } catch (Exception e) {
                            // Invalid token - connection will be rejected
                            throw new IllegalArgumentException("Invalid JWT token");
                        }
                    } else {
                        // No token provided - connection will be rejected
                        throw new IllegalArgumentException("JWT token required");
                    }
                }

                return message;
            }
        });
    }
}
