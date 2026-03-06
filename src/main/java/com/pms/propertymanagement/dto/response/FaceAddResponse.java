package com.pms.propertymanagement.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FaceAddResponse {
    @JsonProperty("object")
    private FaceAddResult result;

    @Data
    public static class FaceAddResult {
        private String result;
        private String msg;
        
        @JsonProperty("customer_information")
        private CustomerInformationResponse customerInformation;
    }
    
    @Data
    public static class CustomerInformationResponse {
        private String hometown;
        private String address;
        private String gender;
        private String ipfs;
        
        @JsonProperty("other_type")
        private String otherType;
        
        private String title;
        
        @JsonProperty("card_id")
        private String cardId;
        
        @JsonProperty("passport_id")
        private String passportId;
        
        @JsonProperty("extra_info")
        private Object extraInfo;
        
        @JsonProperty("driver_license_id")
        private String driverLicenseId;
        
        private String nationality;
        private String dob;
        
        @JsonProperty("other_id")
        private String otherId;
        
        private String fullname;
        
        @JsonProperty("military_id")
        private String militaryId;
        
        @JsonProperty("customer_id")
        private String customerId;
        
        @JsonProperty("police_id")
        private String policeId;
    }
}
