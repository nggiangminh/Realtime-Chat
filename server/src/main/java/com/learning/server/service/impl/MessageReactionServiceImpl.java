package com.learning.server.service.impl;

import com.learning.server.dto.websocket.ReactionResponseDTO;
import com.learning.server.entity.Message;
import com.learning.server.entity.MessageReaction;
import com.learning.server.entity.User;
import com.learning.server.repository.MessageReactionRepository;
import com.learning.server.repository.MessageRepository;
import com.learning.server.repository.UserRepository;
import com.learning.server.service.MessageReactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation của MessageReactionService
 */
@Service
public class MessageReactionServiceImpl implements MessageReactionService {

    @Autowired
    private MessageReactionRepository reactionRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public ReactionResponseDTO toggleReaction(Long messageId, Long userId, String emoji) {
        // Validate message exists
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Tin nhắn không tồn tại"));

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));

        // Check if reaction already exists
        Optional<MessageReaction> existing = reactionRepository
                .findByMessageIdAndUserIdAndEmoji(messageId, userId, emoji);

        String action;
        if (existing.isPresent()) {
            // Remove reaction
            reactionRepository.delete(existing.get());
            action = "REMOVE";
        } else {
            // MỖI TIN NHẮN CHỈ CÓ 1 LOẠI REACTION
            // Xóa TẤT CẢ reactions cũ của message này (không phân biệt user)
            reactionRepository.deleteByMessageId(messageId);
            
            // Add reaction mới
            MessageReaction reaction = new MessageReaction(messageId, userId, emoji);
            reactionRepository.save(reaction);
            action = "ADD";
        }

        // Get updated reaction counts
        Map<String, Integer> counts = getReactionCountsMap(messageId);

        return new ReactionResponseDTO(
                messageId,
                userId,
                user.getDisplayName(),
                emoji,
                action,
                counts
        );
    }

    @Override
    public ReactionResponseDTO getReactionCounts(Long messageId) {
        // Validate message exists
        messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Tin nhắn không tồn tại"));

        Map<String, Integer> counts = getReactionCountsMap(messageId);

        return new ReactionResponseDTO(
                messageId,
                null,
                null,
                null,
                "GET",
                counts
        );
    }

    /**
     * Helper method to get reaction counts as map
     */
    private Map<String, Integer> getReactionCountsMap(Long messageId) {
        List<Object[]> results = reactionRepository.countReactionsByMessageId(messageId);
        Map<String, Integer> counts = new HashMap<>();
        
        for (Object[] result : results) {
            String emoji = (String) result[0];
            Long count = (Long) result[1];
            counts.put(emoji, count.intValue());
        }
        
        return counts;
    }
}
