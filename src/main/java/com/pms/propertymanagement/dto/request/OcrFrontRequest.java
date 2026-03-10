package com.pms.propertymanagement.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OcrFrontRequest {
    @JsonProperty("img_front")
    private String imgFront;
    
    @JsonProperty("client_session")
    private String clientSession;
    
    private Integer type;
    
    @JsonProperty("validate_postcode")
    private Boolean validatePostcode;
    
    private String token;
}
