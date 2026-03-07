package com.pms.propertymanagement.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FaceVerifyResponse {
    @JsonProperty("object")
    private FaceVerifyResult result;

    @Data
    public static class FaceVerifyResult {
        private String result;
        private String msg;
        private Double prob;
        
        @JsonProperty("id_card")
        private String idCard;
        
        @JsonProperty("id_type")
        private String idType;
    }
}
