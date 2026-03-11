package com.pms.propertymanagement.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OcrBackRequest {
    @JsonProperty("img_back")
    private String imgBack;
    
    @JsonProperty("client_session")
    private String clientSession;
    
    private Integer type;
    
    private String token;
}
