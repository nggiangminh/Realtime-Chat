package com.learning.server.dto.websocket;

import java.util.Map;

/**
 * DTO response cho reaction updates
 */
public record ReactionResponseDTO(
        Long messageId,
        Long userId,
        String userDisplayName,
        String emoji,
        String action, // "ADD" or "REMOVE"
        Map<String, Integer> reactionCounts // emoji -> count
) {
    public ReactionResponseDTO {
        if (messageId == null) {
            throw new IllegalArgumentException("Message ID không được để trống");
        }
        if (action == null || (!action.equals("ADD") && !action.equals("REMOVE"))) {
            throw new IllegalArgumentException("Action phải là ADD hoặc REMOVE");
        }
    }
}
