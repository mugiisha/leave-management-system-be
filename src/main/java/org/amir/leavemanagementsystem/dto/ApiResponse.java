package org.amir.leavemanagementsystem.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime timestamp;
    private int status;
    private String message;
    private T data;
    private boolean success;
    private String path;

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .message(message)
                .data(data)
                .success(true)
                .build();
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Operation successful");
    }

    public static <T> ApiResponse<T> error(HttpStatus status, String message) {
        return ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .message(message)
                .success(false)
                .build();
    }

    public static <T> ApiResponse<T> error(HttpStatus status, String message, String path) {
        return ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .message(message)
                .success(false)
                .path(path)
                .build();
    }

    public static ApiResponse<Map<String, String>> error(HttpStatus httpStatus, String s, String requestURI, Map<String, String> errors) {
        return ApiResponse.<Map<String, String>>builder()
                .timestamp(LocalDateTime.now())
                .status(httpStatus.value())
                .message(s)
                .success(false)
                .path(requestURI)
                .data(errors)
                .build();
    }
} 