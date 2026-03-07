package com.pms.propertymanagement.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LivenessRequest {
    @JsonProperty("img")
    private String img;
    
    @JsonProperty("client_session")
    private String clientSession;
}
