package com.learning.server.repository;

import com.learning.server.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository để quản lý User entity
 * Tuân thủ quy tắc Repository trong coding instructions
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Tìm user theo email
     * @param email email của user
     * @return Optional<User>
     */
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);

    /**
     * Kiểm tra xem email đã tồn tại chưa
     * @param email email cần kiểm tra
     * @return true nếu email đã tồn tại
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email")
    boolean existsByEmail(@Param("email") String email);

    /**
     * Tìm user theo email và password (dùng cho authentication)
     * @param email email của user
     * @param password password đã được hash
     * @return Optional<User>
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.password = :password")
    Optional<User> findByEmailAndPassword(@Param("email") String email, @Param("password") String password);

    /**
     * Tìm kiếm users theo tên hiển thị (case insensitive)
     * @param displayName tên hiển thị cần tìm
     * @return List<User>
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.displayName) LIKE LOWER(CONCAT('%', :displayName, '%'))")
    List<User> findByDisplayNameContainingIgnoreCase(@Param("displayName") String displayName);
}
