package com.learning.server.controller;

import com.learning.server.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller đơn giản để test API
 */
@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/public")
    public ResponseEntity<ApiResponse<String>> publicEndpoint() {
        return ResponseEntity.ok(
            ApiResponse.success("Đây là endpoint public, không cần authentication", "Hello World!")
        );
    }

    @GetMapping("/protected")
    public ResponseEntity<ApiResponse<String>> protectedEndpoint() {
        return ResponseEntity.ok(
            ApiResponse.success("Đây là endpoint protected, cần authentication", "Hello Authenticated User!")
        );
    }
}
