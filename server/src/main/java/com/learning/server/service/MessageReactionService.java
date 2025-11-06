package com.learning.server.service;

import com.learning.server.dto.websocket.ReactionResponseDTO;

/**
 * Service interface cho MessageReaction
 */
public interface MessageReactionService {

    /**
     * Thêm hoặc xóa reaction (toggle)
     * Nếu user đã react với emoji này -> xóa
     * Nếu chưa react -> thêm mới
     */
    ReactionResponseDTO toggleReaction(Long messageId, Long userId, String emoji);

    /**
     * Lấy reaction counts cho một message
     */
    ReactionResponseDTO getReactionCounts(Long messageId);
}
