package com.learning.server.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lớp chung để wrap response trả về client
 * Tuân thủ quy tắc ApiResponse trong coding instructions
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private String result;    // SUCCESS hoặc ERROR
    private String message;   // success hoặc error message
    private T data;           // return object từ service class, nếu thành công

    /**
     * Tạo response thành công
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("SUCCESS", message, data);
    }

    /**
     * Tạo response thành công không có data
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>("SUCCESS", message, null);
    }

    /**
     * Tạo response lỗi
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("ERROR", message, null);
    }

    /**
     * Tạo response lỗi với HTTP status code
     */
    public static <T> ApiResponse<T> error(int statusCode, String message) {
        return new ApiResponse<>("ERROR", String.format("[%d] %s", statusCode, message), null);
    }
}
