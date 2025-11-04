package com.learning.server.exception;

import com.learning.server.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * GlobalExceptionHandler để xử lý tất cả exceptions trong ứng dụng
 * Tuân thủ quy tắc trong coding instructions
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Tạo ResponseEntity lỗi chuẩn
     */
    public static ResponseEntity<ApiResponse<?>> errorResponseEntity(String message, HttpStatus status) {
        ApiResponse<?> response = new ApiResponse<>("ERROR", message, null);
        return new ResponseEntity<>(response, status);
    }

    /**
     * Xử lý IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return new ResponseEntity<>(ApiResponse.error(400, ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    /**
     * Xử lý RuntimeException chung
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<?>> handleRuntimeException(RuntimeException ex) {
        return new ResponseEntity<>(
                ApiResponse.error(500, "Lỗi server nội bộ: " + ex.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    /**
     * Xử lý Exception chung
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException(Exception ex) {
        return new ResponseEntity<>(
                ApiResponse.error(500, "Lỗi server không xác định: " + ex.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        String errorMessage = "Dữ liệu không hợp lệ: " + String.join(", ", errors);
        return errorResponseEntity(errorMessage, HttpStatus.BAD_REQUEST);
    }

}
