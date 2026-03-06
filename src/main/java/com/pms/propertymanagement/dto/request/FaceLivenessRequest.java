package com.pms.propertymanagement.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FaceLivenessRequest {
    private String img;
    
    @JsonProperty("client_session")
    private String clientSession;
    
    private String token;
}
