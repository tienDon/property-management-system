package com.pms.propertymanagement.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FaceCompareResponse {
    @JsonProperty("object")
    private FaceCompareResult result;
    
    @JsonProperty("server_version")
    private String serverVersion;
    
    private String message;

    @Data
    public static class FaceCompareResult {
        private String result;
        private String msg;
        private Double prob;
    }
}
