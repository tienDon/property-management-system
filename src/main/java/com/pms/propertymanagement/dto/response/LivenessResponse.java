package com.pms.propertymanagement.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class LivenessResponse {
    private String message;
    private String status;
    private String statusCode;
    private List<String> errors;
    
    @JsonProperty("object")
    private LivenessResult result;

    @Data
    public static class LivenessResult {
        private String liveness;
        
        @JsonProperty("liveness_msg")
        private String livenessMsg;
        
        @JsonProperty("face_swapping")
        private Boolean faceSwapping;
        
        @JsonProperty("fake_liveness")
        private Boolean fakeLiveness;
    }
}
