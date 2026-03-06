package com.pms.propertymanagement.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FaceLivenessResponse {
    private String message;
    
    @JsonProperty("object")
    private FaceLivenessResult result;

    @Data
    public static class FaceLivenessResult {
        private String liveness;
        
        @JsonProperty("liveness_msg")
        private String livenessMsg;
        
        @JsonProperty("is_eye_open")
        private String isEyeOpen;
    }
}
