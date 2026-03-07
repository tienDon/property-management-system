package com.pms.propertymanagement.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class FaceSearchKResponse {
    private String message;
    
    @JsonProperty("object")
    private FaceSearchKResult result;

    @Data
    public static class FaceSearchKResult {
        private String result;
        private String msg;
        
        @JsonProperty("customer_informations")
        private List<FaceSearchItem> customerInformations;
    }
    
    @Data
    public static class FaceSearchItem {
        @JsonProperty("customer_information")
        private FaceAddResponse.CustomerInformationResponse customerInformation;
        
        @JsonProperty("face_probability")
        private Double faceProbability;
    }
}
