package com.pms.propertymanagement.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FaceSearchResponse {
    private String message;
    
    @JsonProperty("object")
    private FaceSearchResult result;

    @Data
    public static class FaceSearchResult {
        private String result;
        private String msg;
        
        @JsonProperty("customer_information")
        private FaceAddResponse.CustomerInformationResponse customerInformation;
        
        @JsonProperty("face_probability")
        private Double faceProbability;
    }
}
