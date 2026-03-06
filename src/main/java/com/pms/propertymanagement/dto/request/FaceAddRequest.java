package com.pms.propertymanagement.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FaceAddRequest {
    private String bbox;
    private String landmark;
    private String unit;
    
    @JsonProperty("customer_information")
    private CustomerInformation customerInformation;
    
    @Data
    public static class CustomerInformation {
        @JsonProperty("card_id")
        private String cardId;
        
        @JsonProperty("passport_id")
        private String passportId;
        
        @JsonProperty("driver_license_id")
        private String driverLicenseId;
        
        @JsonProperty("military_id")
        private String militaryId;
        
        @JsonProperty("police_id")
        private String policeId;
        
        @JsonProperty("other_id")
        private String otherId;
        
        private String fullname;
        private String dob;
        private String gender;
        private String address;
        private String hometown;
        private String nationality;
        private String ipfs;
        private String title;
        
        @JsonProperty("other_type")
        private String otherType;
        
        @JsonProperty("extra_info")
        private Object extraInfo;
    }
}
