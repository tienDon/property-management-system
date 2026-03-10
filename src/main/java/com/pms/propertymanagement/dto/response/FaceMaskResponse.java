package com.pms.propertymanagement.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FaceMaskResponse {
    private String message;
    
    @JsonProperty("object")
    private FaceMaskResult result;

    @Data
    public static class FaceMaskResult {
        private String masked;
    }
}
