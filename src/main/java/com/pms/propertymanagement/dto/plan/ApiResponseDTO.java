package com.pms.propertymanagement.dto.plan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponseDTO {
    private Boolean success;
    private String message;
    private Object data;
    
    public static ApiResponseDTO success(String message) {
        return ApiResponseDTO.builder()
                .success(true)
                .message(message)
                .build();
    }
    
    public static ApiResponseDTO success(String message, Object data) {
        return ApiResponseDTO.builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }
    
    public static ApiResponseDTO error(String message) {
        return ApiResponseDTO.builder()
                .success(false)
                .message(message)
                .build();
    }
}