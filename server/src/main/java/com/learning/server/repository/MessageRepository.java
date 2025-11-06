package com.learning.server.repository;

import com.learning.server.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho Message entity
 * Tuân thủ quy tắc Repository trong coding instructions
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Lấy lịch sử chat giữa 2 users, sắp xếp theo thời gian gửi
     * Sử dụng JPQL theo quy tắc
     * Lọc bỏ tin nhắn đã xóa
     */
    @Query("SELECT m FROM Message m WHERE " +
           "((m.senderId = :userId1 AND m.receiverId = :userId2) OR " +
           "(m.senderId = :userId2 AND m.receiverId = :userId1)) " +
           "AND m.isDeleted = false " +
           "ORDER BY m.sentAt ASC")
    List<Message> findChatHistory(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    /**
     * Lấy danh sách tin nhắn chưa đọc của user
     * Lọc bỏ tin nhắn đã xóa
     */
    @Query("SELECT m FROM Message m WHERE m.receiverId = :userId AND m.isRead = false " +
           "AND m.isDeleted = false " +
           "ORDER BY m.sentAt ASC")
    List<Message> findUnreadMessages(@Param("userId") Long userId);

    /**
     * Đánh dấu tất cả tin nhắn giữa 2 users là đã đọc
     */
    @Query("UPDATE Message m SET m.isRead = true WHERE " +
           "m.senderId = :senderId AND m.receiverId = :receiverId AND m.isRead = false")
    void markMessagesAsRead(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);

    /**
     * Đếm số tin nhắn chưa đọc từ một user cụ thể
     * Lọc bỏ tin nhắn đã xóa
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE " +
           "m.senderId = :senderId AND m.receiverId = :receiverId AND m.isRead = false " +
           "AND m.isDeleted = false")
    Long countUnreadMessages(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);

    /**
     * Lấy tin nhắn mới nhất giữa 2 users
     * Lọc bỏ tin nhắn đã xóa
     */
    @Query("SELECT m FROM Message m WHERE " +
           "((m.senderId = :userId1 AND m.receiverId = :userId2) OR " +
           "(m.senderId = :userId2 AND m.receiverId = :userId1)) " +
           "AND m.isDeleted = false " +
           "ORDER BY m.sentAt DESC")
    List<Message> findLatestMessages(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}
