package com.pms.propertymanagement.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OcrIdRequest {
    @JsonProperty("img_front")
    private String imgFront;
    
    @JsonProperty("img_back")
    private String imgBack;
    
    @JsonProperty("client_session")
    private String clientSession;
    
    private Integer type;
    
    @JsonProperty("crop_param")
    private String cropParam;
    
    @JsonProperty("validate_postcode")
    private Boolean validatePostcode;
    
    private String token;
}
