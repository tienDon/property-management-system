package com.pms.propertymanagement.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ClassifyIdRequest {
    @JsonProperty("img_card")
    private String imgCard;
    
    @JsonProperty("client_session")
    private String clientSession;
    
    private String token;
}
