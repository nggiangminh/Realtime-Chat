package com.learning.server.repository;

import com.learning.server.entity.MessageReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho MessageReaction
 */
@Repository
public interface MessageReactionRepository extends JpaRepository<MessageReaction, Long> {

    /**
     * Tìm tất cả reactions của một message
     */
    List<MessageReaction> findByMessageId(Long messageId);

    /**
     * Tìm reaction của user cho message cụ thể với emoji cụ thể
     */
    Optional<MessageReaction> findByMessageIdAndUserIdAndEmoji(Long messageId, Long userId, String emoji);

    /**
     * Xóa reaction của user cho message với emoji cụ thể
     */
    void deleteByMessageIdAndUserIdAndEmoji(Long messageId, Long userId, String emoji);

    /**
     * Xóa TẤT CẢ reactions của một message (cho logic 1 message chỉ 1 reaction)
     */
    void deleteByMessageId(Long messageId);

    /**
     * Đếm số lượng reactions theo emoji cho message
     */
    @Query("SELECT r.emoji, COUNT(r) FROM MessageReaction r WHERE r.messageId = :messageId GROUP BY r.emoji")
    List<Object[]> countReactionsByMessageId(Long messageId);
}
