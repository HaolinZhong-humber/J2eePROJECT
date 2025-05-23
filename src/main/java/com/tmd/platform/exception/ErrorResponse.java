package com.tmd.platform.exception;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponse {
    private String code;
    private String message;
    private String timestamp;
} 