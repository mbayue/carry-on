package com.bayue.task_manager.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiResponse<T>(
    String status,
    int code,
    String message,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime timestamp,
    T data,
    List<String> errors
) {
    public static <T> ApiResponse<T> success(int code, String message, T data) {
        return new ApiResponse<>("success", code, message, LocalDateTime.now(), data, null);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return success(200, message, data);
    }

    public static <T> ApiResponse<T> success(int code, String message) {
        return new ApiResponse<>("success", code, message, LocalDateTime.now(), null, null);
    }

    public static <T> ApiResponse<T> success(String message) {
        return success(200, message);
    }

    public static <T> ApiResponse<T> error(int code, String message, List<String> errors) {
        HttpStatus resolved = HttpStatus.resolve(code);
        String statusText = resolved != null ? resolved.name().toLowerCase() : "error";
        return new ApiResponse<>(statusText, code, message, LocalDateTime.now(), null, errors);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return error(code, message, null);
    }

    public static <T> ApiResponse<T> error(String status, int code, String message, List<String> errors) {
        return new ApiResponse<>(status, code, message, LocalDateTime.now(), null, errors);
    }
}
